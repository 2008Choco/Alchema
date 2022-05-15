package wtf.choco.alchema.crafting;

import java.util.function.Supplier;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link CauldronRecipe} result.
 */
public interface CauldronRecipeResult extends Supplier<ItemStack> {

    /**
     * Get the key for this recipe result type.
     *
     * @return the ingredient key
     */
    @NotNull
    public NamespacedKey getKey();

    /**
     * Get the amount of this result.
     *
     * @return the result amount
     */
    public int getAmount();

    /**
     * Get this result represented as an {@link ItemStack}.
     *
     * @return the item stack
     */
    @NotNull
    public ItemStack asItemStack();

    @NotNull
    @Override
    public default ItemStack get() {
        return asItemStack();
    }

}
