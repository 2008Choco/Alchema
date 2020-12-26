package wtf.choco.alchema.essence;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.persistence.AlchemaPersistentDataTypes;
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.alchema.util.ItemBuilder;

/**
 * Represents information about entity essence bottles and its properties.
 *
 * @author Parker Hawke - Choco
 */
public class EntityEssenceData {

    private static final ItemStack EMPTY_VIAL = ItemBuilder.of(Material.GLASS_BOTTLE)
        .name("Empty Vial")
        .lore(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Collects entity essence.")
        .build();

    private final EntityType entityType;
    private final Color essenceColor;
    private final boolean glowing, consumable;
    private final Consumer<@NotNull LivingEntity> consumptionEffectApplier;

    /**
     * Construct a new {@link EntityEssenceData}.
     *
     * @param entityType the target entity type
     * @param essenceColor the colour of the essence bottle
     * @param glowing whether or not the essence bottle should glow
     * @param consumable whether or not the essence is consumable by the player
     * @param consumptionEffectApplier the applier to be run when a player consumes the essence
     */
    public EntityEssenceData(@NotNull EntityType entityType, @NotNull Color essenceColor, boolean glowing, boolean consumable, @Nullable Consumer<@NotNull LivingEntity> consumptionEffectApplier) {
        Preconditions.checkArgument(entityType != null, "entityType must not be null");
        Preconditions.checkArgument(essenceColor != null, "essenceColor must not be null");

        this.entityType = entityType;
        this.essenceColor = essenceColor;
        this.glowing = glowing;
        this.consumable = consumable;
        this.consumptionEffectApplier = consumptionEffectApplier;
    }

    /**
     * Construct a new {@link EntityEssenceData} with {@code glowing} set to false.
     *
     * @param entityType the target entity type
     * @param essenceColor the colour of the essence bottle
     * @param consumable whether or not the essence is consumable by the player
     * @param consumptionEffectApplier the applier to be run when a player consumes the essence
     */
    public EntityEssenceData(@NotNull EntityType entityType, @NotNull Color essenceColor, boolean consumable, @Nullable Consumer<@NotNull LivingEntity> consumptionEffectApplier) {
        this(entityType, essenceColor, false, consumable, consumptionEffectApplier);
    }

    /**
     * Construct a new {@link EntityEssenceData} with {@code glowing} set to false.
     *
     * @param entityType the target entity type
     * @param essenceColor the colour of the essence bottle
     * @param consumptionEffectApplier the applier to be run when a player consumes the essence
     */
    public EntityEssenceData(@NotNull EntityType entityType, @NotNull Color essenceColor, @Nullable Consumer<@NotNull LivingEntity> consumptionEffectApplier) {
        this(entityType, essenceColor, false, consumptionEffectApplier != null, consumptionEffectApplier);
    }

    /**
     * Construct a new unconsumable {@link EntityEssenceData}.
     *
     * @param entityType the target entity type
     * @param essenceColor the colour of the essence bottle
     * @param glowing whether or not the essence bottle should glow
     */
    public EntityEssenceData(@NotNull EntityType entityType, @NotNull Color essenceColor, boolean glowing) {
        this(entityType, essenceColor, glowing, false, null);
    }

    /**
     * Construct a new unconsumable {@link EntityEssenceData} with {@code glowing} set to false.
     *
     * @param entityType the target entity type
     * @param essenceColor the colour of the essence bottle
     */
    public EntityEssenceData(@NotNull EntityType entityType, @NotNull Color essenceColor) {
        this(entityType, essenceColor, false, false, null);
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
     * Check whether or not the essence bottle is consumable by players.
     *
     * @return true if consumable, false otherwise
     */
    public boolean isConsumable() {
        return consumable;
    }

    /**
     * Check whether or not this entity essence has any effect on player consumption.
     *
     * @return true if an effect is applied, false otherwise
     */
    public boolean hasConsumptionEffect() {
        return consumptionEffectApplier != null;
    }

    /**
     * Apply the consumption effect for this entity essence to the provided living entity.
     * If this essence data does not have any consumption effect (such that
     * {@link #hasConsumptionEffect()} is {@code false}), this method will do nothing and
     * return false.
     *
     * @param entity the entity to which the effect should be applied
     *
     * @return true if an effect was applied, false otherwise
     */
    public boolean applyConsumptionEffectTo(@NotNull LivingEntity entity) {
        if (consumptionEffectApplier == null) {
            return false;
        }

        this.consumptionEffectApplier.accept(entity);
        return true;
    }

    /**
     * Create an {@link ItemStack} that encapsulates this essence data in an inventory.
     *
     * @param amount the amount of the item to create
     *
     * @return the item
     */
    @NotNull
    public ItemStack createItemStack(int amount) {
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) {
            throw new IllegalStateException("trap"); // Theoretically impossible
        }

        String entityName = WordUtils.capitalizeFully(entityType.getKey().getKey().replace("_", " "));
        meta.setDisplayName("Vial of Essence " + ChatColor.GRAY + "(" + entityName + ")");
        meta.setLore(Arrays.asList(
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
        container.set(AlchemaConstants.NBT_KEY_ENTITY_ESSENCE, AlchemaPersistentDataTypes.ENTITY_TYPE, entityType);

        item.setItemMeta(meta);
        item.setAmount(amount);
        return item;
    }

    /**
     * Create an {@link ItemStack} that encapsulates this essence data in an inventory.
     *
     * @return the item
     */
    @NotNull
    public ItemStack createItemStack() {
        return createItemStack(1);
    }

    /**
     * Check whether or not the provided {@link ItemStack} is a vial of entity essence.
     *
     * @param item the item to check
     *
     * @return true if it is essence, false otherwise
     */
    public static boolean isEntityEssence(@Nullable ItemStack item) {
        return getEntityEssenceType(item) != null;
    }

    /**
     * Get the {@link EntityType} for which the provided {@link ItemStack} represents.
     * <p>
     * This method acts under the assumption that the provided ItemStack is a vial of entity
     * essence (i.e. {@link #isEntityEssence(ItemStack)} is true). If this is not the case,
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

        return meta.getPersistentDataContainer().get(AlchemaConstants.NBT_KEY_ENTITY_ESSENCE, AlchemaPersistentDataTypes.ENTITY_TYPE);
    }

    /**
     * Check whether or not the provided item is an empty vial.
     *
     * @param item the empty vial
     *
     * @return true if it is an empty vial, false otherwise
     */
    public static boolean isEmptyVial(@Nullable ItemStack item) {
        return EMPTY_VIAL.isSimilar(item);
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
        ItemStack item = EMPTY_VIAL.clone();
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
