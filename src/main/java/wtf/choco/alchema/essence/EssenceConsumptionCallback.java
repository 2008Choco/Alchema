package wtf.choco.alchema.essence;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a callback to be executed when a player consumes a vial of entity essence
 * in the world.
 *
 * @author Parker Hawke - Choco
 */
@FunctionalInterface
public interface EssenceConsumptionCallback {

    /**
     * Called when a player consumes a vial of entity essence in the world.
     *
     * @param player the player that consumed the vial
     * @param essenceData the essence data of the vial
     * @param item the item stack in the inventory
     * @param amountOfEssence the amount of essence in the vial
     * @param potency the potency of essence relative to max essence (0.0 - 1.0 where 0.0
     * is no essence and 1.0 is maximum essence)
     */
    public void consume(@NotNull Player player, @NotNull EntityEssenceData essenceData, @NotNull ItemStack item, int amountOfEssence, float potency);

}
