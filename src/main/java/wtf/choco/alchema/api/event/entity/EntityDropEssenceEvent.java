package wtf.choco.alchema.api.event.entity;

import com.google.common.base.Preconditions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.essence.EntityEssenceData;

/**
 * Called when a {@link LivingEntity} is killed and drops a vial of entity essence.
 *
 * @author Parker Hawke - Choco
 */
public class EntityDropEssenceEvent extends EntityEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;
    private int amountOfEssence;

    private final EntityEssenceData essenceData;

    /**
     * Construct a new {@link EntityDamageByCauldronEvent}.
     *
     * @param entity the entity damaged by the cauldron
     * @param essenceData the essence data associated with the entity
     * @param amountOfEssence the amount of essence to be dropped
     */
    public EntityDropEssenceEvent(@NotNull Entity entity, @NotNull EntityEssenceData essenceData, int amountOfEssence) {
        super(entity);

        this.essenceData = essenceData;
        this.amountOfEssence = amountOfEssence;
    }

    /**
     * Set the amount of essence to be dropped by this entity.
     *
     * @param amountOfEssence the amount of essence to drop. Must be > 0
     */
    public void setAmountOfEssence(int amountOfEssence) {
        Preconditions.checkArgument(amountOfEssence > 0, "amountOfEssence must be > 0");

        this.amountOfEssence = amountOfEssence;
    }

    /**
     * Get the amount of essence to be dropped by this entity.
     *
     * @return the amount of essence to drop
     */
    public int getAmountOfEssence() {
        return amountOfEssence;
    }

    /**
     * Get the {@link EntityEssenceData} dropped in this event.
     *
     * @return the entity essence data
     */
    public EntityEssenceData getEssenceData() {
        return essenceData;
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
