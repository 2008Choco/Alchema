package wtf.choco.alchema.api.event;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.cauldron.AlchemicalCauldron;

/**
 * Called when a cauldron drops its ingredients in the world.
 *
 * @author Parker Hawke - Choco
 */
public class CauldronIngredientsDropEvent extends CauldronEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    private final List<@NotNull Item> items;
    private final Player player;
    private final Reason reason;
    private final boolean cancellable;

    /**
     * Construct a new {@link CauldronIngredientsDropEvent}.
     *
     * @param cauldron the cauldron that caused the craft
     * @param items the items to be dropped
     * @param player the player
     * @param reason the reason for this event
     * @param cancellable whether or not this event will respect cancelled states
     */
    public CauldronIngredientsDropEvent(@NotNull AlchemicalCauldron cauldron, @NotNull Collection<@NotNull Item> items, @Nullable Player player, @NotNull Reason reason, boolean cancellable) {
        super(cauldron);

        Preconditions.checkArgument(items != null, "items must not be null");
        Preconditions.checkArgument(reason != null, "reast must not be null");

        this.items = new ArrayList<>(items);
        this.player = player;
        this.reason = reason;
        this.cancellable = cancellable;
    }

    /**
     * Get the {@link Item Items} to be dropped by the cauldron. The returned list is
     * mutable. Any changes made to the returned collection will directly affect which
     * items are dropped after this event has been processed.
     *
     * @return the list of items to drop
     */
    @NotNull
    public List<@NotNull Item> getItems() {
        return items;
    }

    /**
     * Get the player that caused this event (if any).
     *
     * @return the player
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the reason for this event being called.
     *
     * @return the reason
     */
    @NotNull
    public Reason getReason() {
        return reason;
    }

    /**
     * Check whether or not this event is cancellable.
     * <p>
     * If this event is not cancellable, any attempt to cancel it with {@link #setCancelled(boolean)}
     * will not be respected by the event caller.
     *
     * @return true if cancellable, false otherwise
     *
     * @apiNote according to Alchema, this will only ever be true if the reason is
     * {@link Reason#NO_PERMISSION}. Cancellation is respected in all other instances.
     */
    public boolean isCancellable() {
        return cancellable;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancellable && cancelled;
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

    /**
     * Represents a reason for the {@link CauldronIngredientsDropEvent} to be called.
     */
    public enum Reason {

        /**
         * A player has emptied the cauldron with a bucket.
         */
        EMPTIED_BY_PLAYER,

        /**
         * The cauldron has lost its source of heat.
         */
        UNHEATED,

        /**
         * The cauldron was destroyed.
         */
        DESTROYED,

        /**
         * A player did not have permission to craft the recipe.
         */
        NO_PERMISSION;

    }

}
