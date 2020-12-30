package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.util.ItemUtil;

/**
 * A {@link CauldronIngredient} implementation wrapped around an {@link ItemStack}.
 * The item stack must match exactly in the cauldron (with the exception of the item
 * amount).
 *
 * @author Parker Hawke - Choco
 */
public class CauldronIngredientItemStack implements CauldronIngredient {

    /** The {@link NamespacedKey} used for this ingredient type */
    public static final NamespacedKey KEY = Alchema.key("item");

    private final ItemStack item;

    /**
     * Construct a new {@link CauldronIngredientItemStack} with a given amount.
     *
     * @param item the item
     * @param amount the amount of material
     */
    public CauldronIngredientItemStack(@NotNull ItemStack item, int amount) {
        Preconditions.checkArgument(item != null, "item cannot be null");
        Preconditions.checkArgument(amount > 0, "amount must be > 0");

        this.item = item.clone();
        this.item.setAmount(amount);
    }

    /**
     * Construct a new {@link CauldronIngredientItemStack} with an amount of 1.
     *
     * @param item the item
     */
    public CauldronIngredientItemStack(@NotNull ItemStack item) {
        this(item, 1);
    }

    /**
     * Construct a new {@link CauldronIngredientItemStack} deserialized from the
     * provided {@link JsonObject}.
     *
     * @param object the object from which to deserialize
     */
    public CauldronIngredientItemStack(@NotNull JsonObject object) {
        this.item = ItemUtil.deserializeItemStack(object);
        this.item.setAmount(object.has("amount") ? Math.max(object.get("amount").getAsInt(), 1) : 1);
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @Override
    public int getAmount() {
        return item.getAmount();
    }

    @Override
    public ItemStack asItemStack() {
        return item.clone();
    }

    @Override
    public boolean isSimilar(@NotNull CauldronIngredient other) {
        if (!(other instanceof CauldronIngredientItemStack)) {
            return false;
        }

        CauldronIngredientItemStack ingredient = (CauldronIngredientItemStack) other;
        return item.isSimilar(ingredient.item);
    }

    @NotNull
    @Override
    public CauldronIngredient merge(@NotNull CauldronIngredient other) {
        Preconditions.checkArgument(other instanceof CauldronIngredientItemStack, "Cannot merge %s with %s", getClass().getName(), other.getClass().getName());
        return new CauldronIngredientItemStack(item, getAmount() + other.getAmount());
    }

    @NotNull
    @Override
    public CauldronIngredient adjustAmountBy(int amount) {
        Preconditions.checkArgument(amount < getAmount(), "amount must be < getAmount(), %d", getAmount());

        return new CauldronIngredientItemStack(item, getAmount() + amount);
    }

    @NotNull
    @Override
    public JsonObject toJson() {
        JsonObject object = ItemUtil.serializeItemStack(item);
        object.addProperty("amount", getAmount()); // Adjust "amount" to match getAmount()
        return object;
    }

}
