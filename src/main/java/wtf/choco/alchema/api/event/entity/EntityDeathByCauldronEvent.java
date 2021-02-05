package wtf.choco.alchema.api.event.entity;

import com.google.common.base.Preconditions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.essence.EntityEssenceEffectRegistry;

/**
 * Called when a {@link LivingEntity} is damaged and killed by an {@link AlchemicalCauldron}.
 *
 * Whether or not the entity drops essence into the cauldron depends on if any essence data
 * is registered to the {@link EntityEssenceEffectRegistry}. This event is called whether or
 * not essence is inserted, though essence may be 0.
 *
 * @author Parker Hawke - Choco
 */
public class EntityDeathByCauldronEvent extends EntityEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private int essence;

    private final AlchemicalCauldron cauldron;

    /**
     * Construct a new {@link EntityDeathByCauldronEvent}.
     *
     * @param entity the entity damaged by the cauldron
     * @param cauldron the cauldron that inflicted the damage
     * @param essence the amount of essence to be inserted into the cauldron
     */
    public EntityDeathByCauldronEvent(@NotNull LivingEntity entity, @NotNull AlchemicalCauldron cauldron, int essence) {
        super(entity);

        Preconditions.checkArgument(cauldron != null, "cauldron must not be null");
        Preconditions.checkArgument(essence >= 0, "essence must be positive or 0");

        this.cauldron = cauldron;
        this.essence = essence;
    }

    @NotNull
    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) super.getEntity();
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

    /**
     * Set the amount of essence to be inserted into the cauldron.
     *
     * If the entity being killed does not have any entity essence registered in the
     * {@link EntityEssenceEffectRegistry}, this value will have no effect.
     *
     * @param essence the essence to set
     */
    public void setEssence(int essence) {
        Preconditions.checkArgument(essence >= 0, "essence must be positive or 0");
        this.essence = essence;
    }

    /**
     * Get the amount of essence to be inserted into the cauldron.
     *
     * @return the amount of essence
     */
    public int getEssence() {
        return essence;
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
