package wtf.choco.alchema.integration.mmoitems;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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

public final class CauldronIngredientMMOItem implements CauldronIngredient {

    static NamespacedKey key; // Set by PluginIntegrationMMOItems

    private final MMOItem mmoItem;
    private final ItemStack item;

    public CauldronIngredientMMOItem(@NotNull MMOItem mmoItem, @NotNull ItemStack item, int amount) {
        this.mmoItem = mmoItem;
        this.item = item.clone();
        this.item.setAmount(amount);
    }

    public CauldronIngredientMMOItem(@NotNull MMOItem mmoItem, int amount) {
        this(mmoItem, mmoItem.newBuilder().build(), amount);
    }

    public CauldronIngredientMMOItem(@NotNull MMOItem mmoItem) {
        this(mmoItem, mmoItem.newBuilder().build(), 1);
    }

    public CauldronIngredientMMOItem(@NotNull JsonObject object) {
        if (!object.has("item_type")) {
            throw new JsonParseException("Missing element \"item_type\"");
        }

        Type itemType = MMOItems.plugin.getTypes().get(object.get("item_type").getAsString());
        if (itemType == null) {
            throw new JsonParseException("Unkown MMOItems type: " + object.get("item_type").getAsString());
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

}
