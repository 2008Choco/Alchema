package wtf.choco.alchema.api.event;

import com.google.common.base.Preconditions;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.cauldron.AlchemicalCauldron;

/**
 * Called when an {@link AlchemicalCauldron} begins to bubble.
 *
 * @author Parker Hawke - Choco
 */
public class CauldronBubbleEvent extends BlockEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    private final AlchemicalCauldron cauldron;

    /**
     * Construct a new {@link CauldronBubbleEvent}.
     *
     * @param cauldron the cauldron that began bubbling
     */
    public CauldronBubbleEvent(@NotNull AlchemicalCauldron cauldron) {
        super(cauldron.getCauldronBlock());

        Preconditions.checkArgument(cauldron != null, "cauldron must not be null");

        this.cauldron = cauldron;
    }

    /**
     * Get the {@link AlchemicalCauldron} involved in this event.
     *
     * @return the cauldron
     */
    @NotNull
    public AlchemicalCauldron getCauldron() {
        return cauldron;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
