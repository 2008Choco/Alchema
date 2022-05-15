package wtf.choco.alchema.util;

import com.google.common.collect.ImmutableList;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import wtf.choco.alchema.Alchema;

/**
 * Various constants for the Alchema plugin. Non-documented but field names should
 * be self-descriptive.
 *
 * @author Parker Hawke - Choco
 */
public final class AlchemaConstants {

    // Metadata keys
    /** A metadata key used by Alchema to denote that a block is an alchemical cauldron */
    public static final String METADATA_KEY_ALCHEMICAL_CAULDRON = "alchema:alchemical_cauldron";

    /** A metadata key used by Alchema to denote whether or not an alchemical cauldron is bubbling */
    public static final String METADATA_KEY_ALCHEMICAL_CAULDRON_BUBBLING = "alchema:alchemical_cauldron_bubbling";

    /** A metadata key used by Alchema to denote that an entity was damaged by a cauldron */
    public static final String METADATA_KEY_DAMAGED_BY_CAULDRON = "alchema:damaged_by_cauldron";

    /** A metadata key used by Alchema to denote that an item entity was recently crafted by a cauldron */
    public static final String METADATA_KEY_CAULDRON_CRAFTED = "alchema:cauldron_crafted";

    /** A metadata key used by Alchema to denote that an entity was interacted with by a player and a vial of essence */
    public static final String METADATA_KEY_INTERACTED_WITH_VIAL = "alchema:interacted_with_vial";


    // NBT keys
    /** A {@link NamespacedKey} used as an NBT key for entity essence items. Determines the type of entity essence */
    public static final NamespacedKey NBT_KEY_ESSENCE_TYPE = Alchema.key("essence_type");

    /** A {@link NamespacedKey} used as an NBT key for entity essence items. Determines the quantity */
    public static final NamespacedKey NBT_KEY_ESSENCE_AMOUNT = Alchema.key("essence_amount");

    /** A {@link NamespacedKey} used as an NBT key for empty vial items. Acts as an identifier */
    public static final NamespacedKey NBT_KEY_EMPTY_VIAL = Alchema.key("empty_vial");


    // Recipe keys
    /** A {@link NamespacedKey} used as a key for the empty vial crafting recipe */
    public static final NamespacedKey RECIPE_KEY_EMPTY_VIAL = Alchema.key("empty_vial");


    // Configuration keys
    /** Configuration path, CheckForUpdates */
    public static final String CONFIG_CHECK_FOR_UPDATES = "CheckForUpdates";

    /** Configuration path, Metrics.Enabled */
    public static final String CONFIG_METRICS_ENABLED = "Metrics.Enabled";

    /** Configuration path, Metrics.AnonymousCustomRecipeTypes */
    public static final String CONFIG_METRICS_ANONYMOUS_CUSTOM_RECIPE_TYPES = "Metrics.AnonymousCustomRecipeTypes";

    /** Configuration path, Cauldron.ItemSearchInterval */
    public static final String CONFIG_CAULDRON_ITEM_SEARCH_INTERVAL = "Cauldron.ItemSearchInterval";

    /** Configuration path, Cauldron.MillisecondsToHeatUp */
    public static final String CONFIG_CAULDRON_MILLISECONDS_TO_HEAT_UP = "Cauldron.MillisecondsToHeatUp";

    /** Configuration path, Cauldron.EnforcePlayerDroppedItems */
    public static final String CONFIG_CAULDRON_ENFORCE_PLAYER_DROPPED_ITEMS = "Cauldron.EnforcePlayerDroppedItems";

    /** Configuration path, Cauldron.Entities.Damage */
    public static final String CONFIG_CAULDRON_ENTITIES_DAMAGE = "Cauldron.Entities.Damage";

    /** Configuration path, Cauldron.Entities.MinEssenceOnDeath */
    public static final String CONFIG_CAULDRON_ENTITIES_MIN_ESSENCE_ON_DEATH = "Cauldron.Entities.MinEssenceOnDeath";

    /** Configuration path, Cauldron.Entities.MaxEssenceOnDeath */
    public static final String CONFIG_CAULDRON_ENTITIES_MAX_ESSENCE_ON_DEATH = "Cauldron.Entities.MaxEssenceOnDeath";

    /** Configuration path, Cauldron.DeathMessages */
    public static final String CONFIG_CAULDRON_DEATH_MESSAGES = "Cauldron.DeathMessages";

    /** Configuration path, Cauldron.Sound.AmbientBubbleVolume */
    public static final String CONFIG_CAULDRON_SOUND_AMBIENT_BUBBLE_VOLUME = "Cauldron.Sound.AmbientBubbleVolume";

    /** Configuration path, Cauldron.Sound.ItemSplashVolume */
    public static final String CONFIG_CAULDRON_SOUND_ITEM_SPLASH_VOLUME = "Cauldron.Sound.ItemSplashVolume";

    /** Configuration path, Cauldron.Sound.SuccessfulCraftVolume */
    public static final String CONFIG_CAULDRON_SOUND_SUCCESSFUL_CRAFT_VOLUME = "Cauldron.Sound.SuccessfulCraftVolume";

    /** Configuration path, VialOfEssence.MaximumEssence */
    public static final String CONFIG_VIAL_OF_ESSENCE_MAXIMUM_ESSENCE = "VialOfEssence.MaximumEssence";

    /** Configuration path, VialOfEssence.FromEntities.OnDeath.BaseDropChance */
    public static final String CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_DEATH_BASE_DROP_CHANCE = "VialOfEssence.FromEntities.OnDeath.BaseDropChance";

    /** Configuration path, VialOfEssence.FromEntities.OnDeath.Min */
    public static final String CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_DEATH_MIN = "VialOfEssence.FromEntities.OnDeath.Min";

    /** Configuration path, VialOfEssence.FromEntities.OnDeath.Max */
    public static final String CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_DEATH_MAX = "VialOfEssence.FromEntities.OnDeath.Max";

    /** Configuration path, VialOfEssence.FromEntities.OnDeath.Blacklist */
    public static final String CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_DEATH_BLACKLIST = "VialOfEssence.FromEntities.OnDeath.Blacklist";

    /** Configuration path, VialOfEssence.FromEntities.OnInteract.Enabled */
    public static final String CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_ENABLED = "VialOfEssence.FromEntities.OnInteract.Enabled";

    /** Configuration path, VialOfEssence.FromEntities.OnInteract.TimeoutSeconds */
    public static final String CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_TIMEOUT_SECONDS = "VialOfEssence.FromEntities.OnInteract.TimeoutSeconds";

    /** Configuration path, VialOfEssence.FromEntities.OnInteract.Min */
    public static final String CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_MIN = "VialOfEssence.FromEntities.OnInteract.Min";

    /** Configuration path, VialOfEssence.FromEntities.OnInteract.Max */
    public static final String CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_MAX = "VialOfEssence.FromEntities.OnInteract.Max";

    /** Configuration path, VialOfEssence.FromEntities.OnInteract.Blacklist */
    public static final String CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_BLACKLIST = "VialOfEssence.FromEntities.OnInteract.Blacklist";

    /** Configuration path, VialOfEssence.Consumption.TastelessThoughts */
    public static final String CONFIG_VIAL_OF_ESSENCE_CONSUMPTION_TASTELESS_THOUGHTS = "VialOfEssence.Consumption.TastelessThoughts";

    /** Configuration path, VialOfEssence.Recipe.Enabled */
    public static final String CONFIG_VIAL_OF_ESSENCE_RECIPE_ENABLED = "VialOfEssence.Recipe.Enabled";

    /** Configuration path. VialOfEssence.Recipe.Yield */
    public static final String CONFIG_VIAL_OF_ESSENCE_RECIPE_YIELD = "VialOfEssence.Recipe.Yield";

    /** Configuration path, VialOfEssence.Recipe.Shape */
    public static final String CONFIG_VIAL_OF_ESSENCE_RECIPE_SHAPE = "VialOfEssence.Recipe.Shape";

    /** Configuration path, VialOfEssence.Recipe.Ingredients */
    public static final String CONFIG_VIAL_OF_ESSENCE_RECIPE_INGREDIENTS = "VialOfEssence.Recipe.Ingredients";

    /** Configuration path, VialOfEssence.Item.Empty.Name */
    public static final String CONFIG_VIAL_OF_ESSENCE_ITEM_EMPTY_NAME = "VialOfEssence.Item.Empty.Name";

    /** Configuration path, VialOfEssence.Item.Empty.Lore */
    public static final String CONFIG_VIAL_OF_ESSENCE_ITEM_EMPTY_LORE = "VialOfEssence.Item.Empty.Lore";

    /** Configuration path, VialOfEssence.Item.Empty.CustomModelData */
    public static final String CONFIG_VIAL_OF_ESSENCE_ITEM_EMPTY_CUSTOM_MODEL_DATA = "VialOfEssence.Item.Empty.CustomModelData";

    /** Configuration path, VialOfEssence.Item.Filled.Name */
    public static final String CONFIG_VIAL_OF_ESSENCE_ITEM_FILLED_NAME = "VialOfEssence.Item.Filled.Name";

    /** Configuration path, VialOfEssence.Item.Filled.Lore */
    public static final String CONFIG_VIAL_OF_ESSENCE_ITEM_FILLED_LORE = "VialOfEssence.Item.Filled.Lore";

    /** Configuration path, VialOfEssence.Item.Filled.CustomModelData */
    public static final String CONFIG_VIAL_OF_ESSENCE_ITEM_FILLED_CUSTOM_MODEL_DATA = "VialOfEssence.Item.Filled.CustomModelData";


    // Permission nodes
    /** Permission node, alchema.command.reload, grants access to /alchema reload */
    public static final String PERMISSION_COMMAND_RELOAD = "alchema.command.reload";

    /** Permission node, alchema.command.integrations, grants access to /alchema integrations */
    public static final String PERMISSION_COMMAND_INTEGRATIONS = "alchema.command.integrations";

    /** Permission node, alchema.command.saverecipe, grants access to /alchema saverecipe */
    public static final String PERMISSION_COMMAND_SAVERECIPE = "alchema.command.saverecipe";

    /** Permission node, alchema.command.givevialofessence, grants access to /givevialofessence */
    public static final String PERMISSION_COMMAND_GIVE_VIAL_OF_ESSENCE = "alchema.command.givevialofessence";

    /** Permission node, alchema.command.reload.verbose, grants access to /alchema reload verbose */
    public static final String PERMISSION_COMMAND_RELOAD_VERBOSE = "alchema.command.reload.verbose";

    /** Permission node, alchema.updatenotify, grants access to update notifications */
    public static final String PERMISSION_UPDATE_NOTIFY = "alchema.updatenotify";

    /** Permission node, alchema.craft, allows a player to craft recipes in the cauldron */
    public static final String PERMISSION_CRAFT = "alchema.craft";


    // General constants
    /** A list of glass panes and stained glass panes */
    public static final List<Material> MATERIALS_GLASS_PANES = ImmutableList.of(
        Material.GLASS_PANE,
        Material.BLACK_STAINED_GLASS_PANE,
        Material.BLUE_STAINED_GLASS_PANE,
        Material.BROWN_STAINED_GLASS_PANE,
        Material.CYAN_STAINED_GLASS_PANE,
        Material.GRAY_STAINED_GLASS_PANE,
        Material.GREEN_STAINED_GLASS_PANE,
        Material.LIGHT_BLUE_STAINED_GLASS_PANE,
        Material.LIGHT_GRAY_STAINED_GLASS_PANE,
        Material.LIME_STAINED_GLASS_PANE,
        Material.MAGENTA_STAINED_GLASS_PANE,
        Material.ORANGE_STAINED_GLASS_PANE,
        Material.PINK_STAINED_GLASS_PANE,
        Material.PURPLE_STAINED_GLASS_PANE,
        Material.RED_STAINED_GLASS_PANE,
        Material.WHITE_STAINED_GLASS_PANE,
        Material.YELLOW_STAINED_GLASS_PANE
    );

    private AlchemaConstants() { }

}
