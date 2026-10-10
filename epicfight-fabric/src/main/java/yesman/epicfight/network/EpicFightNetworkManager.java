package yesman.epicfight.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.network.client.*;
import yesman.epicfight.network.common.BiDirectionalAnimationVariable;
import yesman.epicfight.network.common.BiDirectionalSyncAnimationPositionPacket;
import yesman.epicfight.network.common.BiDirectionalSyncEmoteSlots;
import yesman.epicfight.network.server.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/// Fabric networking. Upstream registers the payloads through NeoForge's `PayloadRegistrar` and
/// sends them with `PacketDistributor`; here payload types go to [PayloadTypeRegistry], server
/// handlers to [ServerPlayNetworking] and client handlers to [ClientPlayNetworking]
/// (see `yesman.epicfight.fabric.client.EpicFightClientNetworking`).
public class EpicFightNetworkManager {
	@Nullable
	private static MinecraftServer currentServer;

	/// Called from the common entrypoint
	public static void register() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);

		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_ABSORPTION, SPAbsorption.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_ADD_LEARNED_SKILL, SPAddLearnedSkill.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_HANDLE_SKILL_DATA, SPHandleSkillData.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_ANIMATOR_CONTROL, SPAnimatorControl.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_CHANGE_GAMERULE, SPChangeGamerule.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_CHANGE_LIVING_MOTION, SPChangeLivingMotion.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_CHANGE_PLAYER_MODE, SPChangePlayerMode.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_CHANGE_SKILL, SPChangeSkill.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_CLEAR_SKILLS, SPClearSkills.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_DATAPACK_SYNC, SPDatapackSync.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_ENTITY_PAIRING, SPEntityPairingPacket.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_CREATE_FRACTURE, SPCreateTerrainFracture.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_SET_PLAYER_Y_ROT, SPModifyPlayerData.SetPlayerYRot.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_DISABLE_Y_ROT, SPModifyPlayerData.DisablePlayerYRot.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_SET_LAST_ATTACK_RESULT, SPModifyPlayerData.SetLastAttackResult.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_SET_PLAYER_MODE, SPModifyPlayerData.SetPlayerMode.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_SET_GRAPPLING_TARGET, SPModifyPlayerData.SetGrapplingTarget.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_MOB_EFFECT, SPMobEffectControl.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_MODIFY_EXPANDED_ENTITY_DATA, SPModifyExpandedEntityData.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_PLAY_UI_SOUND, SPPlayUISound.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_REMOVE_SKILL, SPRemoveSkillAndLearn.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_SET_ATTACK_TARGET, SPSetAttackTarget.STREAM_CODEC);
		s2c(SPSetRemotePlayerSkill.CLIENT_BOUND_SET_REMOTE_PLAYER_SKILL, SPSetRemotePlayerSkill.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_SET_SKILL_CONTAINER_VALUE, SPSetSkillContainerValue.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_SKILL_FEEDBACK, SPSkillFeedback.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_UPDATE_PLAYER_INPUT, SPUpdatePlayerInput.STREAM_CODEC);
		s2c(ManagedCustomPacketPayload.CLIENT_BOUND_INIT_SKILLS, SPInitSkills.STREAM_CODEC);
		c2s(ManagedCustomPacketPayload.SERVER_BOUND_ANIMATOR_CONTROL, CPAnimatorControl.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handleAnimatorControl);
		c2s(ManagedCustomPacketPayload.SERVER_BOUND_CHANGE_PLAYER_MODE, CPChangePlayerMode.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handleChangePlayerMode);
		c2s(ManagedCustomPacketPayload.SERVER_BOUND_CHANGE_SKILL, CPChangeSkill.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handleChangeSkill);
		c2s(ManagedCustomPacketPayload.SERVER_BOUND_EXECUTE_SKILL, CPSkillRequest.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handleExecuteSkill);
		c2s(ManagedCustomPacketPayload.SERVER_BOUND_MODIFY_ENTITY_PATCH_Y_ROT, CPModifyEntityModelYRot.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handleModifyPlayerModelYRot);
		c2s(ManagedCustomPacketPayload.SERVER_BOUND_MODIFY_SKILL_DATA, CPHandleSkillData.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handleSkillData);
		c2s(ManagedCustomPacketPayload.SERVER_BOUND_PAIRING_ANIMATION_REGISTRY, CPPairingAnimationRegistry.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handlePairingAnimationRegistry);
		c2s(ManagedCustomPacketPayload.SERVER_BOUND_SET_PLAYER_TARGET, CPSetPlayerTarget.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handleSetPlayerTarget);
		c2s(ManagedCustomPacketPayload.SERVER_BOUND_SET_STAMINA, CPSetStamina.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handleSetStamina);
		c2s(ManagedCustomPacketPayload.SERVER_BOUND_UPDATE_PLAYER_INPUT, CPUpdatePlayerInput.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handleUpdatePlayerInput);
		bidirectional(ManagedCustomPacketPayload.BI_DIRECTIONAL_MODIFY_ANIMATION_VARIABLE, BiDirectionalAnimationVariable.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handleAnimationVariablePacket);
		bidirectional(ManagedCustomPacketPayload.BI_DIRECTIONAL_SYNC_ANIMATION_POSITION, BiDirectionalSyncAnimationPositionPacket.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handleSyncAnimationPosition);
		bidirectional(ManagedCustomPacketPayload.BI_DIRECTIONAL_SYNC_EMOTE_SLOTS, BiDirectionalSyncEmoteSlots.STREAM_CODEC, EpicFightServerBoundPayloadHandler::handleSyncEmoteSlot);
	}

	private static <T extends CustomPacketPayload> void s2c(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		PayloadTypeRegistry.playS2C().register(type, codec);
	}

	private static <T extends CustomPacketPayload> void c2s(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, IPayloadContext> handler) {
		PayloadTypeRegistry.playC2S().register(type, codec);
		ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.accept(payload, context::player));
	}

	private static <T extends CustomPacketPayload> void bidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, IPayloadContext> serverHandler) {
		s2c(type, codec);
		c2s(type, codec, serverHandler);
	}

	@SuppressWarnings("unchecked")
	public static FriendlyByteBuf encodeObjectToBuffer(StreamEncoder<ByteBuf, ?> encoder, Object value) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		((StreamEncoder<ByteBuf, Object>)encoder).encode(buf, value);
		return buf;
	}
	
	public static void sendToServer(CustomPacketPayload message, CustomPacketPayload... others) {
		ClientPlayNetworking.send(message);

		for (CustomPacketPayload other : others) {
			ClientPlayNetworking.send(other);
		}
	}

	public static void sendToAll(CustomPacketPayload message, CustomPacketPayload... others) {
		if (currentServer != null) {
			Packet<?> packet = makeClientboundPacket(message, others);
			currentServer.getPlayerList().getPlayers().forEach(player -> player.connection.send(packet));
		}
	}

	public static void sendToAllPlayerTrackingThisEntity(CustomPacketPayload message, Entity entity, CustomPacketPayload... others) {
		if (entity.level() instanceof ServerLevel serverLevel) {
			serverLevel.getChunkSource().broadcast(entity, makeClientboundPacket(message, others));
		}
	}

	public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player, CustomPacketPayload... others) {
		player.connection.send(makeClientboundPacket(message, others));
	}

	public static void sendToAllPlayerTrackingThisEntityWithSelf(CustomPacketPayload message, ServerPlayer entity, CustomPacketPayload... others) {
		entity.serverLevel().getChunkSource().broadcastAndSend(entity, makeClientboundPacket(message, others));
	}

	public static void sendToAllPlayerTrackingThisChunkWithSelf(CustomPacketPayload message, ServerLevel serverLevel, ChunkPos chunkPos, CustomPacketPayload... others) {
		Packet<?> packet = makeClientboundPacket(message, others);
		PlayerLookup.tracking(serverLevel, chunkPos).forEach(player -> player.connection.send(packet));
	}

	/// Same as NeoForge's `PacketDistributor`: several payloads go out as one bundle packet
	private static Packet<? super ClientGamePacketListener> makeClientboundPacket(CustomPacketPayload message, CustomPacketPayload... others) {
		if (others.length == 0) {
			return ServerPlayNetworking.createS2CPacket(message);
		}

		List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<> ();
		packets.add(ServerPlayNetworking.createS2CPacket(message));

		for (CustomPacketPayload other : others) {
			packets.add(ServerPlayNetworking.createS2CPacket(other));
		}

		return new ClientboundBundlePacket(packets);
	}

	public static class PayloadBundleBuilder {
		public static PayloadBundleBuilder create() {
			return new PayloadBundleBuilder();
		}
		
		public static PayloadBundleBuilder beginWith(CustomPacketPayload payload) {
			return new PayloadBundleBuilder().and(payload);
		}
		
		private final List<CustomPacketPayload> payloads = new ArrayList<> ();
		
		public PayloadBundleBuilder and(CustomPacketPayload payload) {
			this.payloads.add(payload);
			return this;
		}
		
		public void send(BiConsumer<CustomPacketPayload, CustomPacketPayload[]> sendTo) {
			if (this.payloads.size() == 1) {
				sendTo.accept(this.payloads.getFirst(), new CustomPacketPayload[0]);
			} else if (!this.payloads.isEmpty()) {
				sendTo.accept(this.payloads.getFirst(), this.payloads.subList(1, this.payloads.size()).toArray(new CustomPacketPayload[0]));
			}
		}
	}
}
