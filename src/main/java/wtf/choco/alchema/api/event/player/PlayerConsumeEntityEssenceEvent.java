package wtf.choco.alchema.api.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.essence.EntityEssenceData;

/**
 * Called when a player consumes a vial of entity essence and has applied an effect.
 * This event is still called even if the entity essence data is tasteless and does
 * not apply an effect to the player. If cancelled, the vial of essence is not
 * consumed.
 *
 * @author Parker Hawke - Choco
 */
public class PlayerConsumeEntityEssenceEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;
    private boolean applyEffect = true;

    private final ItemStack item;
    private final EntityEssenceData essenceData;

    public PlayerConsumeEntityEssenceEvent(@NotNull Player player, @NotNull ItemStack item, @NotNull EntityEssenceData essenceData) {
        super(player);

        this.item = item;
        this.essenceData = essenceData;
    }

    /**
     * Get the vial of entity essence {@link ItemStack} that was consumed to cause
     * this event.
     *
     * @return the item
     */
    public ItemStack getItem() {
        return item.clone();
    }

    /**
     * Get the entity essence data that was consumed.
     *
     * @return the essence data
     */
    public EntityEssenceData getEssenceData() {
        return essenceData;
    }

    /**
     * Set whether or not this event should apply the effect of the entity essence
     * data. If set to false, the vial will be consumed (assuming {@link #isCancelled()}
     * is {@code false}) but the effect will not apply.
     *
     * @param applyEffect whether or not to apply the effect
     */
    public void setApplyEffect(boolean applyEffect) {
        this.applyEffect = applyEffect;
    }

    /**
     * Check whether or not this event should apply the effect of the entity essence
     * data.
     *
     * @return true if the effect should apply, false otherwise
     */
    public boolean shouldApplyEffect() {
        return applyEffect;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Get the HandlerList instance for this event.
     *
     * @return the handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
