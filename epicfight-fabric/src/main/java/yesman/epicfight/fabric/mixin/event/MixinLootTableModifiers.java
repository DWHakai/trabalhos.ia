package yesman.epicfight.fabric.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.data.loot.OnSkillBookDroppedByEntity;

import java.util.Optional;
import java.util.function.Consumer;

/// NeoForge applies the global loot modifiers (`CommonHooks#modifyLoot`) after every non-raw
/// `LootTable#getRandomItems`; the raw variants (nested tables, the modifiers themselves) skip them.
/// Epic Fight's only modifier is the mob skill book drop.
@Mixin(LootTable.class)
public abstract class MixinLootTableModifiers {
    @Shadow @Final private Optional<ResourceLocation> randomSequence;

    @WrapOperation(method = {
        "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V",
        "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V"))
    private void epicfight$modifyLoot(LootTable self, LootContext context, Consumer<ItemStack> output, Operation<Void> original) {
        original.call(self, context, output);
        OnSkillBookDroppedByEntity.apply(context, output);
    }

    @WrapOperation(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;Ljava/util/function/Consumer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootParams;Ljava/util/function/Consumer;)V"))
    private void epicfight$modifyLootFromParams(LootTable self, LootParams params, Consumer<ItemStack> output, Operation<Void> original) {
        LootContext context = new LootContext.Builder(params).create(this.randomSequence);
        self.getRandomItemsRaw(context, output);
        OnSkillBookDroppedByEntity.apply(context, output);
    }
}
