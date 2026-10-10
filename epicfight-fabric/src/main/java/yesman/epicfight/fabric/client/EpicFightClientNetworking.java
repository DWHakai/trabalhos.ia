package yesman.epicfight.fabric.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import yesman.epicfight.network.EpicFightClientBoundPayloadHandler;
import yesman.epicfight.network.IPayloadContext;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.network.server.SPSetRemotePlayerSkill;

import java.util.function.BiConsumer;

/// Client side of `EpicFightNetworkManager`: handlers of the client-bound payloads
@Environment(EnvType.CLIENT)
public final class EpicFightClientNetworking {
	public static void register() {
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_ABSORPTION, EpicFightClientBoundPayloadHandler::handleAbsorption);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_ADD_LEARNED_SKILL, EpicFightClientBoundPayloadHandler::handleAddLearnedSkill);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_HANDLE_SKILL_DATA, EpicFightClientBoundPayloadHandler::handleSkillData);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_ANIMATOR_CONTROL, EpicFightClientBoundPayloadHandler::handleAnimatorControl);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_CHANGE_GAMERULE, EpicFightClientBoundPayloadHandler::handleChangeGameRule);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_CHANGE_LIVING_MOTION, EpicFightClientBoundPayloadHandler::handleChangeLivingMotion);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_CHANGE_PLAYER_MODE, EpicFightClientBoundPayloadHandler::handleChangePlayerMode);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_CHANGE_SKILL, EpicFightClientBoundPayloadHandler::handleChangeSkill);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_CLEAR_SKILLS, EpicFightClientBoundPayloadHandler::handleClearSkills);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_DATAPACK_SYNC, EpicFightClientBoundPayloadHandler::handleDataPack);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_ENTITY_PAIRING, EpicFightClientBoundPayloadHandler::handleEntityPairing);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_CREATE_FRACTURE, EpicFightClientBoundPayloadHandler::handleFracture);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_SET_PLAYER_Y_ROT, EpicFightClientBoundPayloadHandler::handleModelYRot);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_DISABLE_Y_ROT, EpicFightClientBoundPayloadHandler::handleDisableModelYRot);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_SET_LAST_ATTACK_RESULT, EpicFightClientBoundPayloadHandler::handleSetLastAttackResult);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_SET_PLAYER_MODE, EpicFightClientBoundPayloadHandler::handleSetPlayerMode);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_SET_GRAPPLING_TARGET, EpicFightClientBoundPayloadHandler::handleSetGrapplingTarget);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_MOB_EFFECT, EpicFightClientBoundPayloadHandler::handleMobEffect);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_MODIFY_EXPANDED_ENTITY_DATA, EpicFightClientBoundPayloadHandler::handleModifyExpandedEntityData);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_PLAY_UI_SOUND, EpicFightClientBoundPayloadHandler::handlePlayUiSound);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_REMOVE_SKILL, EpicFightClientBoundPayloadHandler::handleRemoveSkill);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_SET_ATTACK_TARGET, EpicFightClientBoundPayloadHandler::handleSetAttackTarget);
		receive(SPSetRemotePlayerSkill.CLIENT_BOUND_SET_REMOTE_PLAYER_SKILL, EpicFightClientBoundPayloadHandler::handleSetRemotePlayerSkill);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_SET_SKILL_CONTAINER_VALUE, EpicFightClientBoundPayloadHandler::handleSetSkillContainerValue);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_SKILL_FEEDBACK, EpicFightClientBoundPayloadHandler::handleSkillFeedback);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_UPDATE_PLAYER_INPUT, EpicFightClientBoundPayloadHandler::handleUpdatePlayerInput);
		receive(ManagedCustomPacketPayload.CLIENT_BOUND_INIT_SKILLS, EpicFightClientBoundPayloadHandler::handleInitSkills);
		receive(ManagedCustomPacketPayload.BI_DIRECTIONAL_MODIFY_ANIMATION_VARIABLE, EpicFightClientBoundPayloadHandler::handleAnimationVariablePacket);
		receive(ManagedCustomPacketPayload.BI_DIRECTIONAL_SYNC_ANIMATION_POSITION, EpicFightClientBoundPayloadHandler::handleSyncAnimationPosition);
		receive(ManagedCustomPacketPayload.BI_DIRECTIONAL_SYNC_EMOTE_SLOTS, EpicFightClientBoundPayloadHandler::handleSyncEmoteSlot);
	}

	private static <T extends CustomPacketPayload> void receive(CustomPacketPayload.Type<T> type, BiConsumer<T, IPayloadContext> handler) {
		ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.accept(payload, context::player));
	}

	private EpicFightClientNetworking() {}
}
