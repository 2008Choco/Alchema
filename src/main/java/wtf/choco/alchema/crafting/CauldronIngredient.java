package wtf.choco.alchema.crafting;

import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.api.event.CauldronIngredientAddEvent;
import wtf.choco.alchema.cauldron.AlchemicalCauldron;

/**
 * Represents an ingredient useable in an {@link AlchemicalCauldron} defined by a
 * {@link CauldronRecipe}.
 * <p>
 * CauldronIngredient is meant to be extendable. Should a plugin choose to add a new
 * implementation of CauldronIngredient, the {@link CauldronIngredientAddEvent} should
 * be listened to in order to inject custom implementations into a cauldron based on
 * the item being thrown in. All implementations need to be registered with
 * {@link CauldronRecipeRegistry#registerIngredientType(NamespacedKey, Function)}
 * with the {@link #getKey()} matching that of the registered key.
 *
 * @author Parker Hawke - Choco
 */
public interface CauldronIngredient {

    /**
     * Get the key for this ingredient type.
     *
     * @return the ingredient key
     */
    @NotNull
    public NamespacedKey getKey();

    /**
     * Get the amount of this ingredient.
     *
     * @return the ingredient amount
     */
    public int getAmount();

    /**
     * Get this ingredient represented as an {@link ItemStack}, if possible.
     *
     * @return the item stack. null if no item stack representation
     */
    @Nullable
    public ItemStack asItemStack();

    /**
     * Check whether this ingredient is similar to the provided ingredient. The
     * ingredient amount is not taken into consideration when comparing.
     *
     * @param other the other ingredient against which to compare
     *
     * @return true if similar, false otherwise
     */
    public boolean isSimilar(@NotNull CauldronIngredient other);

    /**
     * Merge this ingredient with another ingredient. The result of this method
     * should be a new ingredient with the combined amounts of this ingredient
     * and the one passed.
     *
     * @param other the other ingredient
     *
     * @return the merged ingredient
     */
    @NotNull
    public CauldronIngredient merge(@NotNull CauldronIngredient other);

    /**
     * Return a new cauldron ingredient with the amount changed by the specified
     * amount. The amount can be either negative or positive but must not result
     * in a negative or zero amount (i.e. if {@code getAmount() - amount} is 0
     * or negative, an exception will be thrown).
     *
     * @param amount the change in amount to apply
     *
     * @return the new ingredient
     */
    @NotNull
    public CauldronIngredient adjustAmountBy(int amount);

    /**
     * Drop this ingredient as one or more {@link Item} from the provided cauldron.
     * <p>
     * Default implementation of this method will, if not null, drop the result of
     * {@link #asItemStack()}.
     *
     * @param cauldron the cauldron from which to drop the ingredients
     * @param world the world in which the cauldron resides
     * @param location the location at which the items should be dropped
     *
     * @return the list of Item entities that were dropped. If none, the returned
     * list should be empty, never null
     */
    @NotNull
    public default List<@NotNull Item> drop(@NotNull AlchemicalCauldron cauldron, @NotNull World world, @NotNull Location location) {
        ItemStack itemStack = asItemStack();
        return itemStack != null ? Arrays.asList(world.dropItem(location, itemStack)) : Collections.emptyList();
    }

    /**
     * Serialize this ingredient to a {@link JsonObject}.
     *
     * @return the serialized json
     */
    @NotNull
    public JsonObject toJson();

}
