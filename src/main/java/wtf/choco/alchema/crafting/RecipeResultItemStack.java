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
 * A {@link RecipeResult} implementation wrapped around an {@link ItemStack}.
 *
 * @author Parker Hawke - Choco
 */
public class RecipeResultItemStack implements RecipeResult {

    /** The {@link NamespacedKey} used for this result type */
    public static final NamespacedKey KEY = Alchema.key("item");

    private final ItemStack item;

    /**
     * Construct a new {@link RecipeResultItemStack} with a given amount.
     *
     * @param item the item
     * @param amount the amount of material
     */
    public RecipeResultItemStack(@NotNull ItemStack item, int amount) {
        Preconditions.checkArgument(item != null, "item must not be null");
        Preconditions.checkArgument(amount > 0, "amount must be > 0");

        this.item = item.clone();
        this.item.setAmount(amount);
    }

    /**
     * Construct a new {@link RecipeResultItemStack} with an amount of
     * {@link ItemStack#getAmount()}.
     *
     * @param item the item
     */
    public RecipeResultItemStack(@NotNull ItemStack item) {
        this(item, item.getAmount());
    }

    /**
     * Construct a new {@link RecipeResultItemStack} deserialized from the
     * provided {@link JsonObject}.
     *
     * @param object the object from which to deserialize
     */
    public RecipeResultItemStack(@NotNull JsonObject object) {
        this.item = ItemUtil.deserializeItemStack(object);
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
        return obj == this || (obj instanceof RecipeResultItemStack other && Objects.equals(item, other.item));
    }

    @Override
    public String toString() {
        return String.format("RecipeResultItemStack[amount=%s, item=%s]", getAmount(), item);
    }

}
