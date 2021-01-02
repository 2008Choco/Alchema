package wtf.choco.alchema.api.event.entity;

import com.google.common.base.Preconditions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.cauldron.AlchemicalCauldron;

/**
 * Called when an {@link LivingEntity} is damaged by an {@link AlchemicalCauldron}.
 *
 * @author Parker Hawke - Choco
 */
public class EntityDamageByCauldronEvent extends EntityEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;
    private double damage;

    private final AlchemicalCauldron cauldron;

    /**
     * Construct a new {@link EntityDamageByCauldronEvent}.
     *
     * @param entity the entity damaged by the cauldron
     * @param cauldron the cauldron that inflicted the damage
     * @param damage the damage inflicted upon the entity
     */
    public EntityDamageByCauldronEvent(@NotNull LivingEntity entity, @NotNull AlchemicalCauldron cauldron, double damage) {
        super(entity);

        Preconditions.checkArgument(cauldron != null, "cauldron must not be null");

        this.cauldron = cauldron;
        this.damage = damage;
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
     * Set the damage to be inflicted on the entity.
     *
     * @param damage the damage to inflict. Must be {@literal >}= 0.0
     */
    public void setDamage(double damage) {
        Preconditions.checkArgument(damage >= 0.0, "damage must be >= 0.0");
        this.damage = damage;
    }

    /**
     * Get the damage to be inflicted on the entity.
     *
     * @return the damage
     */
    public double getDamage() {
        return damage;
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
