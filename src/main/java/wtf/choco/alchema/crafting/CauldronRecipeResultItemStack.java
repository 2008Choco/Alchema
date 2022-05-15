package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import java.util.Objects;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.util.ItemUtil;

/**
 * A {@link CauldronRecipeResult} implementation wrapped around an {@link ItemStack}.
 *
 * @author Parker Hawke - Choco
 */
public class CauldronRecipeResultItemStack implements CauldronRecipeResult {

    /** The {@link NamespacedKey} used for this result type */
    public static final NamespacedKey KEY = Alchema.key("item");

    private final ItemStack item;

    /**
     * Construct a new {@link CauldronRecipeResultItemStack} with a given amount.
     *
     * @param item the item
     * @param amount the amount of material
     */
    public CauldronRecipeResultItemStack(@NotNull ItemStack item, int amount) {
        Preconditions.checkArgument(item != null, "item must not be null");
        Preconditions.checkArgument(amount > 0, "amount must be > 0");

        this.item = item.clone();
        this.item.setAmount(amount);
    }

    /**
     * Construct a new {@link CauldronRecipeResultItemStack} with an amount of
     * {@link ItemStack#getAmount()}.
     *
     * @param item the item
     */
    public CauldronRecipeResultItemStack(@NotNull ItemStack item) {
        this(item, item.getAmount());
    }

    /**
     * Construct a new {@link CauldronRecipeResultItemStack} deserialized from the
     * provided {@link JsonObject}.
     *
     * @param object the object from which to deserialize
     */
    public CauldronRecipeResultItemStack(@NotNull JsonObject object) {
        this.item = ItemUtil.deserializeItemStackModern(object);
        this.item.setAmount(object.has("amount") ? object.get("amount").getAsInt() : 1);
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

    @NotNull
    @Override
    public ItemStack asItemStack() {
        return item.clone();
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof CauldronRecipeResultItemStack other && Objects.equals(item, other.item));
    }

    @Override
    public String toString() {
        return String.format("RecipeResultItemStack[amount=%s, item=%s]", getAmount(), item);
    }

}
