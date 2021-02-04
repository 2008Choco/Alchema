package wtf.choco.alchema.util;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class to assist in the creation of ItemStacks in the confines of a single line.
 *
 * @author Parker Hawke - Choco
 */
public final class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    private ItemBuilder(@NotNull Material type, int amount) {
        Preconditions.checkArgument(type != null, "Cannot create ItemBuilder for null Material");
        Preconditions.checkArgument(type.isItem(), "Illegal material!");

        this.item = new ItemStack(type);
        this.meta = item.getItemMeta();
        this.amount(amount);
    }

    private ItemBuilder(@NotNull ItemStack item) {
        Preconditions.checkArgument(item != null, "Cannot modify a null item");
        Preconditions.checkArgument(item.getType().isItem(), "Illegal material!");

        this.item = item.clone();
        this.meta = item.getItemMeta();
    }

    /**
     * Get a new instance of an ItemBuilder given a (non-null and non-air)
     * {@link Material} and a quantity greater than 0 and less than or equal to
     * {@link Material#getMaxStackSize()}.
     *
     * @param type the type of item to build
     * @param amount the item amount
     *
     * @return the ItemBuilder instance for the provided values
     */
    @NotNull
    public static ItemBuilder of(@NotNull Material type, int amount) {
        return new ItemBuilder(type, amount);
    }

    /**
     * Get a new instance of an ItemBuilder given a (non-null and non-air)
     * {@link Material}.
     *
     * @param type the type of item to build
     *
     * @return the ItemBuilder instance for the provided material
     */
    @NotNull
    public static ItemBuilder of(@NotNull Material type) {
        return new ItemBuilder(type, 1);
    }

    /**
     * Get a new instance of ItemBuilder to modify an existing {@link ItemStack}. The
     * ItemStack passed will be cloned, therefore the passed reference will not be
     * modified, but rather a copy of it. The result of {@link #build()} will be a
     * separate item with the changes applied from this builder instance. The provided
     * item acts as a base for the values in this builder.
     *
     * @param item the item to build
     *
     * @return the ItemBuilder instance for the provided item
     */
    @NotNull
    public static ItemBuilder modify(@NotNull ItemStack item) {
        return new ItemBuilder(item);
    }

    /**
     * Check whether the specified type of ItemMeta is supported by this ItemBuilder.
     *
     * @param type the type of meta to check
     *
     * @return true if supported, false otherwise or if null
     */
    public boolean isSupportedMeta(@NotNull Class<? extends ItemMeta> type) {
        return type != null && type.isInstance(meta);
    }

    /**
     * Apply a method from a more specific type of ItemMeta to this ItemBuilder instance.
     * If the type provided is unsupported by this ItemBuilder (according to
     * {@link #isSupportedMeta(Class)}), this method will throw an exception, therefore it
     * is recommended that it be checked before invoking this method if you are unsure as
     * to what is and is not supported.
     *
     * @param type the type of ItemMeta to apply
     * @param applier the function to apply to the ItemMeta instance
     * @param <T> The ItemMeta type to be applied in the consumer function
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public <T extends ItemMeta> ItemBuilder specific(@NotNull Class<@NotNull T> type, @NotNull Consumer<@NotNull T> applier) {
        Preconditions.checkArgument(type != null, "Cannot apply meta for type null");
        Preconditions.checkArgument(isSupportedMeta(type), "The specified ItemMeta type is not supported by this ItemBuilder instance");
        Preconditions.checkArgument(applier != null, "Application function must not be null");

        applier.accept(type.cast(meta));
        return this;
    }

    /**
     * Apply data to the item's {@link PersistentDataContainer}.
     *
     * @param applier the function to apply to the persistent data container
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public ItemBuilder applyPersistentData(@NotNull Consumer<@NotNull PersistentDataContainer> applier) {
        Preconditions.checkArgument(applier != null, "Application function must not be null");

        applier.accept(meta.getPersistentDataContainer());
        return this;
    }

    /**
     * Set the item name.
     *
     * @param name the name to set
     *
     * @return this instance. Allows for chained method calls
     *
     * @see ItemMeta#setDisplayName(String)
     */
    @NotNull
    public ItemBuilder name(@NotNull String name) {
        this.meta.setDisplayName(name);
        return this;
    }

    /**
     * Set the item lore in the form of varargs.
     *
     * @param lore the lore to set
     *
     * @return this instance. Allows for chained method calls
     *
     * @see ItemBuilder#lore(List)
     * @see ItemMeta#setLore(List)
     */
    @NotNull
    public ItemBuilder lore(@Nullable String... lore) {
        if (lore == null || lore.length > 0) {
            this.meta.setLore(Arrays.asList(lore));
        }

        return this;
    }

    /**
     * Set the item lore in the form of a {@literal List<String>}.
     *
     * @param lore the lore to set
     *
     * @return this instance. Allows for chained method calls
     *
     * @see ItemBuilder#lore(String...)
     * @see ItemMeta#setLore(List)
     */
    @NotNull
    public ItemBuilder lore(@NotNull List<String> lore) {
        this.meta.setLore(lore);
        return this;
    }

    /**
     * Set the item damage. Some items may not display damage or accept the damage
     * attribute at all, in which case this method will simply fail silently.
     *
     * @param damage the damage to set
     *
     * @return this instance. Allows for chained method calls
     *
     * @see Damageable#setDamage(int)
     */
    @NotNull
    public ItemBuilder damage(int damage) {
        ((Damageable) meta).setDamage(damage);
        return this;
    }

    /**
     * Set the item amount. This damage must range between 1 and
     * {@link Material#getMaxStackSize()} according to the type being built in this
     * ItemBuilder instance.
     *
     * @param amount the amount to set
     *
     * @return this instance. Allows for chained method calls
     *
     * @see ItemStack#setAmount(int)
     */
    @NotNull
    public ItemBuilder amount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    /**
     * Apply an enchantment with the specified level to the item. This method does not
     * respect the level limitations of an enchantment (i.e. Sharpness VI may be applied
     * if desired).
     *
     * @param enchantment the enchantment to add
     * @param level the enchantment level to set
     *
     * @return this instance. Allows for chained method calls
     *
     * @see ItemMeta#addEnchant(Enchantment, int, boolean)
     */
    @NotNull
    public ItemBuilder enchantment(@NotNull Enchantment enchantment, int level) {
        this.meta.addEnchant(enchantment, level, true);
        return this;
    }

    /**
     * Add an attribute modifier to this item.
     *
     * @param attribute the attribute for which to add a modifier
     * @param modifier the modifier to apply
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public ItemBuilder attribute(@NotNull Attribute attribute, @NotNull AttributeModifier modifier) {
        this.meta.addAttributeModifier(attribute, modifier);
        return this;
    }

    /**
     * Apply flags to the item.
     *
     * @param flags the flags to set
     *
     * @return this instance. Allows for chained method calls
     *
     * @see ItemMeta#addItemFlags(ItemFlag...)
     */
    @NotNull
    public ItemBuilder flags(@Nullable ItemFlag @NotNull... flags) {
        if (flags.length > 0) {
            this.meta.addItemFlags(flags);
        }

        return this;
    }

    /**
     * Set the unbreakable state of this item to true.
     *
     * @return this instance. Allows for chained method calls
     *
     * @see ItemMeta#setUnbreakable(boolean)
     */
    @NotNull
    public ItemBuilder unbreakable() {
        this.meta.setUnbreakable(true);
        return this;
    }

    /**
     * Set the item's localized name.
     *
     * @param name the localized name to set
     *
     * @return this instance. Allows for chained method calls
     *
     * @see ItemMeta#setLocalizedName(String)
     */
    @NotNull
    public ItemBuilder localizedName(@NotNull String name) {
        this.meta.setLocalizedName(name);
        return this;
    }

    /**
     * Set the item's custom model data flag.
     *
     * @param data the data to set
     *
     * @return this instance. Allows for chained method calls
     *
     * @see ItemMeta#setCustomModelData(Integer)
     */
    @NotNull
    public ItemBuilder modelData(int data) {
        this.meta.setCustomModelData(data);
        return this;
    }

    /**
     * Complete the building of this ItemBuilder and return the resulting ItemStack.
     *
     * @return the completed {@link ItemStack} instance built by this builder
     */
    @NotNull
    public ItemStack build() {
        this.item.setItemMeta(meta);
        return item;
    }

}
