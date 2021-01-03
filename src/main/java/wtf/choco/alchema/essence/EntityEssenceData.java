package wtf.choco.alchema.essence;

import com.google.common.base.Preconditions;

import java.util.Arrays;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.persistence.AlchemaPersistentDataTypes;
import wtf.choco.alchema.util.AlchemaConstants;

/**
 * Represents information about entity essence bottles and its properties.
 *
 * @author Parker Hawke - Choco
 */
public class EntityEssenceData {

    public static final int MAX_AMOUNT_OF_ESSENCE = 1000; // TODO: Configurable

    private final EntityType entityType;
    private final Color essenceColor;
    private final boolean glowing;
    private final EssenceConsumptionCallback consumptionCallback;

    /**
     * Construct a new {@link EntityEssenceData}.
     *
     * @param entityType the target entity type
     * @param essenceColor the colour of the essence bottle
     * @param glowing whether or not the essence bottle should glow
     * @param consumptionCallback the applier to be run when a player consumes the essence
     */
    public EntityEssenceData(@NotNull EntityType entityType, @NotNull Color essenceColor, boolean glowing, @Nullable EssenceConsumptionCallback consumptionCallback) {
        Preconditions.checkArgument(entityType != null, "entityType must not be null");
        Preconditions.checkArgument(essenceColor != null, "essenceColor must not be null");

        this.entityType = entityType;
        this.essenceColor = essenceColor;
        this.glowing = glowing;
        this.consumptionCallback = consumptionCallback;
    }

    /**
     * Construct a new {@link EntityEssenceData} with {@code glowing} set to false.
     *
     * @param entityType the target entity type
     * @param essenceColor the colour of the essence bottle
     * @param consumptionCallback the applier to be run when a player consumes the essence
     */
    public EntityEssenceData(@NotNull EntityType entityType, @NotNull Color essenceColor, @Nullable EssenceConsumptionCallback consumptionCallback) {
        this(entityType, essenceColor, false, consumptionCallback);
    }

    /**
     * Construct a new unconsumable {@link EntityEssenceData}.
     *
     * @param entityType the target entity type
     * @param essenceColor the colour of the essence bottle
     * @param glowing whether or not the essence bottle should glow
     */
    public EntityEssenceData(@NotNull EntityType entityType, @NotNull Color essenceColor, boolean glowing) {
        this(entityType, essenceColor, glowing, null);
    }

    /**
     * Construct a new unconsumable {@link EntityEssenceData} with {@code glowing} set to false.
     *
     * @param entityType the target entity type
     * @param essenceColor the colour of the essence bottle
     */
    public EntityEssenceData(@NotNull EntityType entityType, @NotNull Color essenceColor) {
        this(entityType, essenceColor, false, null);
    }

    /**
     * Get the {@link EntityType} to which this essence data belongs.
     *
     * @return the entity type
     */
    @NotNull
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * Get the {@link Color} of the essence bottle item.
     *
     * @return the essence colour
     */
    @NotNull
    public Color getEssenceColor() {
        return Color.fromRGB(essenceColor.asRGB()); // Cloning the Color instance
    }

    /**
     * Check whether or not the essence bottle item will glow.
     *
     * @return true if glowing, false otherwise
     */
    public boolean isGlowing() {
        return glowing;
    }

    /**
     * Check whether or not this entity essence has any effect on player consumption.
     *
     * @return true if an effect is applied, false otherwise
     */
    public boolean hasConsumptionCallback() {
        return consumptionCallback != null;
    }

    /**
     * Apply the consumption effect for this entity essence to the provided living entity.
     * If this essence data does not have any consumption effect (such that
     * {@link #hasConsumptionCallback()} is {@code false}), this method will do nothing and
     * return false.
     *
     * @param player the player to which the effect should be applied
     * @param item the vial that was consumed
     *
     * @return true if an effect was applied, false otherwise
     */
    public boolean applyConsumptionEffectTo(@NotNull Player player, @NotNull ItemStack item) {
        if (consumptionCallback == null) {
            return false;
        }

        Preconditions.checkArgument(isVialOfEntityEssence(item), "tried to consume item that is not a vial of essence. " + item.toString());

        int amountOfEssence = getEntityEssenceAmount(item);
        this.consumptionCallback.consume(player, this, item, amountOfEssence, amountOfEssence / (float) MAX_AMOUNT_OF_ESSENCE);
        return true;
    }

    /**
     * Get the consumption callback.
     *
     * @return the consumption callback
     */
    public EssenceConsumptionCallback getConsumptionCallback() {
        return consumptionCallback;
    }

    /**
     * Create an {@link ItemStack} that encapsulates this essence data in an inventory.
     *
     * @param essenceAmount the amount of essence in the vial
     * @param amount the amount of the item to create
     *
     * @return the item
     */
    @NotNull
    public ItemStack createItemStack(int essenceAmount, int amount) {
        Preconditions.checkArgument(essenceAmount > 0, "essenceAmount must be > 0");
        Preconditions.checkArgument(amount > 0, "amount must be > 0");

        ItemStack item = new ItemStack(Material.POTION, amount);
        this.applyTo(item, essenceAmount);
        return item;
    }

    /**
     * Create an {@link ItemStack} that encapsulates this essence data in an inventory.
     *
     * @param essenceAmount the amount of essence in the vial
     *
     * @return the item
     */
    @NotNull
    public ItemStack createItemStack(int essenceAmount) {
        return createItemStack(essenceAmount, 1);
    }

    /**
     * Apply the required ItemMeta and change the type of the provided {@link ItemStack} to
     * more appropriately represent this essence data as an ItemStack.
     *
     * @param item the item to update
     * @param essenceAmount the amount of essence to set
     *
     * @return the item stack instance for convenience
     */
    @NotNull
    public ItemStack applyTo(@NotNull ItemStack item, int essenceAmount) {
        Preconditions.checkArgument(item != null, "item must not be null");
        Preconditions.checkArgument(essenceAmount > 0, "essenceAmount must be > 0");

        if (item.getType() != Material.POTION) {
            item.setType(Material.POTION);
        }

        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) {
            throw new IllegalStateException("trap"); // Theoretically impossible
        }

        String entityName = WordUtils.capitalizeFully(entityType.getKey().getKey().replace("_", " "));
        meta.setDisplayName(ChatColor.WHITE + "Vial of Essence " + ChatColor.GRAY + "(" + entityName + ")");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Quantity: " + ChatColor.WHITE + essenceAmount + "/" + MAX_AMOUNT_OF_ESSENCE,
            "",
            ChatColor.GRAY.toString() + ChatColor.ITALIC + "Cauldron crafting ingredient."
        ));

        meta.setBasePotionData(new PotionData(PotionType.UNCRAFTABLE));
        meta.setColor(essenceColor);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);

        if (glowing) {
            meta.addEnchant(Enchantment.OXYGEN, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // Apply custom NBT data
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(AlchemaConstants.NBT_KEY_ESSENCE_TYPE, AlchemaPersistentDataTypes.ENTITY_TYPE, entityType);
        container.set(AlchemaConstants.NBT_KEY_ESSENCE_AMOUNT, PersistentDataType.INTEGER, essenceAmount);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Apply the required ItemMeta and change the type of the provided {@link ItemStack} to
     * more appropriately represent this essence data as an ItemStack. If the item is already
     * a vial of essence, the amount of essence will remain the same. Else, the essence amount
     * will be set to 1.
     *
     * @param item the item to update
     *
     * @return the item stack instance for convenience
     */
    @NotNull
    public ItemStack applyTo(@NotNull ItemStack item) {
        Preconditions.checkArgument(item != null, "item must not be null");

        int essenceAmount = getEntityEssenceAmount(item);
        return applyTo(item, Math.max(essenceAmount, 1));
    }

    /**
     * Check whether or not the provided {@link ItemStack} is a vial of entity essence.
     *
     * @param item the item to check
     *
     * @return true if it is essence, false otherwise
     */
    public static boolean isVialOfEntityEssence(@Nullable ItemStack item) {
        return getEntityEssenceType(item) != null;
    }

    /**
     * Set the {@link EntityType} of the essence for the provided {@link ItemStack}.
     *
     * @param item the item to update
     * @param type the type of entity to set
     *
     * @return the item stack instance for convenience
     */
    @NotNull
    public static ItemStack setEntityEssenceType(@NotNull ItemStack item, @NotNull EntityType type) {
        Preconditions.checkArgument(item != null, "item must not be null");
        Preconditions.checkArgument(type != null, "type must not be null");

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.getPersistentDataContainer().set(AlchemaConstants.NBT_KEY_ESSENCE_TYPE, AlchemaPersistentDataTypes.ENTITY_TYPE, type);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get the {@link EntityType} for which the provided {@link ItemStack} represents.
     * <p>
     * This method acts under the assumption that the provided ItemStack is a vial of entity
     * essence (i.e. {@link #isVialOfEntityEssence(ItemStack)} is true). If this is not the case,
     * this method will return null.
     *
     * @param item the item to check
     *
     * @return the type of entity essence
     */
    @Nullable
    public static EntityType getEntityEssenceType(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        return meta.getPersistentDataContainer().get(AlchemaConstants.NBT_KEY_ESSENCE_TYPE, AlchemaPersistentDataTypes.ENTITY_TYPE);
    }

    /**
     * Set the amount of essence for the provided {@link ItemStack}.
     *
     * @param item the item to update
     * @param amount the amount of essence to set
     *
     * @return the item stack instance for convenience
     */
    @NotNull
    public static ItemStack setEntityEssenceAmount(@NotNull ItemStack item, int amount) {
        Preconditions.checkArgument(item != null, "item must not be null");
        Preconditions.checkArgument(amount > 0, "amount must be > 0");

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.getPersistentDataContainer().set(AlchemaConstants.NBT_KEY_ESSENCE_AMOUNT, PersistentDataType.INTEGER, amount);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get the amount of essence contained in the provided {@link ItemStack}.
     *
     * @param item the item to check
     *
     * @return the amount of essence
     */
    public static int getEntityEssenceAmount(@Nullable ItemStack item) {
        if (item == null) {
            return -1;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return -1;
        }

        Integer amount = meta.getPersistentDataContainer().get(AlchemaConstants.NBT_KEY_ESSENCE_AMOUNT, PersistentDataType.INTEGER);
        return amount != null ? amount.intValue() : -1;
    }

    /**
     * Check whether or not the provided item is an empty vial.
     *
     * @param item the empty vial
     *
     * @return true if it is an empty vial, false otherwise
     */
    public static boolean isEmptyVial(@Nullable ItemStack item) {
        if (item == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(AlchemaConstants.NBT_KEY_EMPTY_VIAL, PersistentDataType.BYTE) && container.get(AlchemaConstants.NBT_KEY_EMPTY_VIAL, PersistentDataType.BYTE) == 1;
    }

    /**
     * Create an {@link ItemStack} representing an empty vial.
     *
     * @param amount the amount of the item to create
     *
     * @return the item
     */
    @NotNull
    public static ItemStack createEmptyVial(int amount) {
        ItemStack item = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new IllegalStateException("trap"); // Theoretically impossible
        }

        meta.setDisplayName(ChatColor.WHITE + "Empty Vial");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY.toString() + ChatColor.ITALIC + "Collects entity essence."
        ));

        // Apply custom NBT data
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(AlchemaConstants.NBT_KEY_EMPTY_VIAL, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        item.setAmount(amount);
        return item;
    }

    /**
     * Create an {@link ItemStack} representing an empty vial.
     *
     * @return the item
     */
    @NotNull
    public static ItemStack createEmptyVial() {
        return createEmptyVial(1);
    }

}
