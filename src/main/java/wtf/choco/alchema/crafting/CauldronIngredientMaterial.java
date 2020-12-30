package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;

/**
 * A {@link CauldronIngredient} implementation wrapped around {@link Material}.
 * This ingredient does not care about specific {@link ItemMeta} and will match
 * any item such that the material matches.
 *
 * @author Parker Hawke - Choco
 */
public class CauldronIngredientMaterial implements CauldronIngredient {

    /** The {@link NamespacedKey} used for this ingredient type */
    public static final NamespacedKey KEY = Alchema.key("material");

    private final Material material;
    private final int amount;

    /**
     * Construct a new {@link CauldronIngredientMaterial} with a given amount.
     *
     * @param material the material
     * @param amount the amount of material
     */
    public CauldronIngredientMaterial(@NotNull Material material, int amount) {
        Preconditions.checkArgument(material != null, "material cannot be null");
        Preconditions.checkArgument(amount > 0, "amount must be > 0");

        this.material = material;
        this.amount = amount;
    }

    /**
     * Construct a new {@link CauldronIngredientMaterial} with an amount of 1.
     *
     * @param material the material
     */
    public CauldronIngredientMaterial(@NotNull Material material) {
        this(material, 1);
    }

    /**
     * Construct a new {@link CauldronIngredientMaterial} deserialized from the
     * provided {@link JsonObject}.
     *
     * @param object the object from which to deserialize
     */
    public CauldronIngredientMaterial(@NotNull JsonObject object) {
        if (!object.has("item")) {
            throw new JsonParseException("object does not contain item.");
        }

        this.material = Material.matchMaterial(object.get("item").getAsString());
        this.amount = object.has("amount") ? object.get("amount").getAsInt() : 1;

        if (material == null) {
            throw new JsonParseException("Could not find material with id " + object.get("item").getAsString());
        }
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public ItemStack asItemStack() {
        return new ItemStack(material, amount);
    }

    @Override
    public boolean isSimilar(@NotNull CauldronIngredient other) {
        Material otherMaterial = null;

        if (other instanceof CauldronIngredientItemStack) {
            ItemStack otherItem = other.asItemStack();
            otherMaterial = otherItem != null ? otherItem.getType() : null;
        }
        else if (other instanceof CauldronIngredientMaterial) {
            otherMaterial = ((CauldronIngredientMaterial) other).material;
        }

        return otherMaterial != null && material == otherMaterial;
    }

    @NotNull
    @Override
    public CauldronIngredient merge(@NotNull CauldronIngredient other) {
        Preconditions.checkArgument(other instanceof CauldronIngredientMaterial, "Cannot merge %s with %s", getClass().getName(), other.getClass().getName());
        return new CauldronIngredientMaterial(material, amount + other.getAmount());
    }

    @NotNull
    @Override
    public CauldronIngredient adjustAmountBy(int amount) {
        Preconditions.checkArgument(amount < getAmount(), "amount must be < getAmount(), %d", getAmount());

        return new CauldronIngredientMaterial(material, getAmount() + amount);
    }

    @NotNull
    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("item", material.getKey().toString());
        object.addProperty("amount", getAmount());

        return object;
    }

}
