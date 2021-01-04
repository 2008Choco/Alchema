package wtf.choco.alchema.api.event.player;

import com.google.common.base.Preconditions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.essence.EntityEssenceData;

/**
 * Called when a {@link Player} collects essence from an {@link Entity} with a vial
 * of essence or an empty vial.
 *
 * @author Parker Hawke - Choco
 */
public class PlayerEssenceCollectEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;
    private int essenceAmount;

    private final EquipmentSlot hand;
    private final ItemStack item;
    private final Entity entity;
    private final EntityEssenceData essenceData;

    /**
     * Construct a new {@link PlayerEssenceCollectEvent}.
     *
     * @param player the player collecting the essence
     * @param hand the hand used to collect the essence
     * @param item the item used to collect the essence
     * @param entity the entity from which the essence was collected
     * @param essenceData the essence data being collected
     * @param essenceAmount the amount of essence being collected
     */
    public PlayerEssenceCollectEvent(@NotNull Player player, @NotNull EquipmentSlot hand, @NotNull ItemStack item, @NotNull Entity entity, @NotNull EntityEssenceData essenceData, int essenceAmount) {
        super(player);

        Preconditions.checkArgument(hand != null, "hand must not be null");
        Preconditions.checkArgument(item != null, "item must not be null");
        Preconditions.checkArgument(entity != null, "entity must not be null");
        Preconditions.checkArgument(essenceData != null, "essenceData must not be null");
        Preconditions.checkArgument(essenceAmount >= 0, "essenceAmount must be >= 0");

        this.hand = hand;
        this.item = item;
        this.entity = entity;
        this.essenceData = essenceData;
        this.essenceAmount = essenceAmount;
    }

    /**
     * Get the hand used in this event.
     *
     * @return the hand
     */
    @NotNull
    public EquipmentSlot getHand() {
        return hand;
    }

    /**
     * Get the vial of entity essence {@link ItemStack} that was consumed to cause
     * this event.
     *
     * @return the item
     */
    @NotNull
    public ItemStack getItem() {
        return item.clone();
    }

    /**
     * Get the {@link Entity} from which the essence is being collected.
     *
     * @return the entity
     */
    @NotNull
    public Entity getEntity() {
        return entity;
    }

    /**
     * Get the entity essence data that was consumed.
     *
     * @return the essence data
     */
    @NotNull
    public EntityEssenceData getEssenceData() {
        return essenceData;
    }

    /**
     * Set the amount of essence to be collected.
     *
     * @param essenceAmount the amount of essence to collect
     */
    public void setEssenceAmount(int essenceAmount) {
        Preconditions.checkArgument(essenceAmount >= 0, "essenceAmount must be >= 0");

        this.essenceAmount = essenceAmount;
    }

    /**
     * Get the amount of essence to be collected.
     *
     * @return the amount of essence
     */
    public int getEssenceAmount() {
        return essenceAmount;
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
