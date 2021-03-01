package wtf.choco.alchema.integration.mmoitems;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.Objects;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.crafting.CauldronIngredient;

/**
 * A {@link CauldronIngredient} implementation wrapped around an {@link MMOItem}.
 * The MMO item must match in type and in id. Neither rarities nor levels are taken
 * into consideration.
 *
 * @author Parker Hawke - Choco
 */
public final class CauldronIngredientMMOItem implements CauldronIngredient {

    static NamespacedKey key; // Set by PluginIntegrationMMOItems

    private final MMOItem mmoItem;
    private final ItemStack item;

    /**
     * Construct a new {@link CauldronIngredientMMOItem} with a given amount and {@link ItemStack}.
     *
     * @param mmoItem the MMOItem instance
     * @param item the ItemStack representation of the MMOItem
     * @param amount the amount
     */
    public CauldronIngredientMMOItem(@NotNull MMOItem mmoItem, @NotNull ItemStack item, int amount) {
        this.mmoItem = mmoItem;
        this.item = item.clone();
        this.item.setAmount(amount);
    }

    /**
     * Construct a new {@link CauldronIngredientMMOItem} with a given amount. The {@link ItemStack}
     * will be newly created.
     *
     * @param mmoItem the MMOItem instance
     * @param amount the amount
     */
    public CauldronIngredientMMOItem(@NotNull MMOItem mmoItem, int amount) {
        this(mmoItem, mmoItem.newBuilder().build(), amount);
    }

    /**
     * Construct a new {@link CauldronIngredientMMOItem}. The {@link ItemStack} will be newly
     * created.
     *
     * @param mmoItem the MMOItem instance
     */
    public CauldronIngredientMMOItem(@NotNull MMOItem mmoItem) {
        this(mmoItem, mmoItem.newBuilder().build(), 1);
    }

    /**
     * Construct a new {@link CauldronIngredientMMOItem} deserialized from the
     * provided {@link JsonObject}.
     *
     * @param object the object from which to deserialize
     */
    public CauldronIngredientMMOItem(@NotNull JsonObject object) {
        if (!object.has("item_type")) {
            throw new JsonParseException("Missing element \"item_type\"");
        }

        Type itemType = MMOItems.plugin.getTypes().get(object.get("item_type").getAsString());
        if (itemType == null) {
            throw new JsonParseException("Unknown MMOItems type: " + object.get("item_type").getAsString());
        }

        if (!object.has("id")) {
            throw new JsonParseException("Missing element \"id\"");
        }

        this.mmoItem = MMOItems.plugin.getMMOItem(itemType, object.get("id").getAsString());
        if (mmoItem == null) {
            throw new JsonParseException("MMOItems item id \"" + object.get("id").getAsString() + "\" could not be found under type " + itemType);
        }

        this.item = mmoItem.newBuilder().build();
        this.item.setAmount(object.has("amount") ? object.get("amount").getAsInt() : 1);
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public int getAmount() {
        return item.getAmount();
    }

    @Nullable
    @Override
    public ItemStack asItemStack() {
        return item;
    }

    @Override
    public boolean isSimilar(@NotNull CauldronIngredient other) {
        if (!(other instanceof CauldronIngredientMMOItem)) {
            return false;
        }

        CauldronIngredientMMOItem ingredient = (CauldronIngredientMMOItem) other;
        return mmoItem.getId().equals(ingredient.mmoItem.getId()) && mmoItem.getType().equals(ingredient.mmoItem.getType());
    }

    @NotNull
    @Override
    public CauldronIngredient merge(@NotNull CauldronIngredient other) {
        Preconditions.checkArgument(other instanceof CauldronIngredientMMOItem, "Cannot merge %s with %s", getClass().getName(), other.getClass().getName());
        return new CauldronIngredientMMOItem(mmoItem, item, getAmount() + other.getAmount());
    }

    @NotNull
    @Override
    public CauldronIngredient adjustAmountBy(int amount) {
        Preconditions.checkArgument(amount < getAmount(), "amount must be < getAmount(), %d", getAmount());
        return new CauldronIngredientMMOItem(mmoItem, item, getAmount() - amount);
    }

    @Override
    @NotNull
    public String describe() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = mmoItem.newBuilder().build().getItemMeta();
            if (meta == null) { // Last attempt. Just going based on data we have available I suppose
                return getAmount() + "x " + StringUtils.capitalize(mmoItem.getId().replace('_', ' ').toLowerCase()) + " (" + mmoItem.getType().getId().replace('_', ' ').toLowerCase() + ")";
            }

            return getAmount() + "x " + meta.getDisplayName();
        }

        return getAmount() + "x " + ChatColor.stripColor(meta.getDisplayName());
    }

    @NotNull
    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("item_type", mmoItem.getType().getId());
        object.addProperty("id", mmoItem.getId());
        object.addProperty("amount", getAmount());

        return object;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, getAmount(), mmoItem.getType(), mmoItem.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof CauldronIngredientMMOItem)) {
            return false;
        }

        CauldronIngredientMMOItem other = (CauldronIngredientMMOItem) obj;
        return getAmount() == other.getAmount() && mmoItem.getType() == other.mmoItem.getType()
                && Objects.equals(mmoItem.getId(), other.mmoItem.getId()) && Objects.equals(item, other.item);
    }

    @Override
    public String toString() {
        return String.format("CauldronIngredientMMOItem[amount=%s, item=%s, mmoItem=%s]", getAmount(), item, mmoItem);
    }

}
