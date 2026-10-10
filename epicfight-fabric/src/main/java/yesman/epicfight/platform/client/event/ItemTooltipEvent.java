package yesman.epicfight.platform.client.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.platform.event.ListenerList;

import java.util.List;

/// NeoForge `ItemTooltipEvent`, fired at the end of `ItemStack#getTooltipLines`.
public class ItemTooltipEvent {
    public static final ListenerList<ItemTooltipEvent> BUS = new ListenerList<>();

    private final ItemStack itemStack;
    @Nullable
    private final Player player;
    private final List<Component> toolTip;
    private final TooltipFlag flags;
    private final Item.TooltipContext context;

    public ItemTooltipEvent(ItemStack itemStack, @Nullable Player player, List<Component> list, TooltipFlag flags, Item.TooltipContext tooltipContext) {
        this.itemStack = itemStack;
        this.player = player;
        this.toolTip = list;
        this.flags = flags;
        this.context = tooltipContext;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    /// The player the tooltip is built for; null when tooltips are built without one (e.g. the
    /// creative search index at startup).
    @Nullable
    public Player getEntity() {
        return this.player;
    }

    public List<Component> getToolTip() {
        return this.toolTip;
    }

    public TooltipFlag getFlags() {
        return this.flags;
    }

    public Item.TooltipContext getContext() {
        return this.context;
    }
}
