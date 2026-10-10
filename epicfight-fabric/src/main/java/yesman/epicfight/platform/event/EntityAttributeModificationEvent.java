package yesman.epicfight.platform.event;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

/// Same API as NeoForge's `EntityAttributeModificationEvent`: adds attributes to the default
/// attribute suppliers of existing entity types. [#apply()] rebuilds each touched supplier
/// (existing attributes + added ones) and re-registers it through [FabricDefaultAttributeRegistry].
public final class EntityAttributeModificationEvent {
    private final Map<EntityType<? extends LivingEntity>, Map<Holder<Attribute>, Double>> attributes = new LinkedHashMap<> ();

    public void add(EntityType<? extends LivingEntity> entityType, Holder<Attribute> attribute, double value) {
        this.attributes(entityType).put(attribute, value);
    }

    public void add(EntityType<? extends LivingEntity> entityType, Holder<Attribute> attribute) {
        this.add(entityType, attribute, attribute.value().getDefaultValue());
    }

    public boolean has(EntityType<? extends LivingEntity> entityType, Holder<Attribute> attribute) {
        return this.attributes(entityType).containsKey(attribute);
    }

    private Map<Holder<Attribute>, Double> attributes(EntityType<? extends LivingEntity> entityType) {
        return this.attributes.computeIfAbsent(entityType, type -> {
            Map<Holder<Attribute>, Double> map = new LinkedHashMap<> ();

            if (DefaultAttributes.hasSupplier(type)) {
                DefaultAttributes.getSupplier(type).instances.forEach((attribute, instance) -> map.put(attribute, instance.getBaseValue()));
            }

            return map;
        });
    }

    public void apply() {
        this.attributes.forEach((entityType, map) -> {
            AttributeSupplier.Builder builder = AttributeSupplier.builder();
            map.forEach(builder::add);
            FabricDefaultAttributeRegistry.register(entityType, builder);
        });
    }
}
