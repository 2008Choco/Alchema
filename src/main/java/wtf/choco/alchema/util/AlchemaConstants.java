package wtf.choco.alchema.util;

import org.bukkit.NamespacedKey;

import wtf.choco.alchema.Alchema;

/**
 * Various constants for the Alchema plugin. Non-documented but field names should
 * be self-descriptive.
 *
 * @author Parker Hawke - Choco
 */
public final class AlchemaConstants {

    /** A metadata key used by Alchema to denote that an entity was damaged by a cauldron */
    public static final String METADATA_KEY_DAMAGED_BY_CAULDRON = "alchema:damaged_by_cauldron";

    /** A metadata key used by Alchema to denote that an item entity was recently crafted by a cauldron */
    public static final String METADATA_KEY_CAULDRON_CRAFTED = "alchema:cauldron_crafted";


    /** A {@link NamespacedKey} used as an NBT key for entity essence items */
    public static final NamespacedKey NBT_KEY_ENTITY_ESSENCE = Alchema.key("entity_essence");


    /** Configuration path, Cauldron.MillisecondsToHeatUp */
    public static final String CONFIG_CAULDRON_MILLISECONDS_TO_HEAT_UP = "Cauldron.MillisecondsToHeatUp";

    /** Configuration path, Cauldron.DamageEntities */
    public static final String CONFIG_CAULDRON_DAMAGE_ENTITIES = "Cauldron.DamageEntities";

    /** Configuration path, Cauldron.Sound.AmbientBubbleVolume */
    public static final String CONFIG_CAULDRON_SOUND_AMBIENT_BUBBLE_VOLUME = "Cauldron.Sound.AmbientBubbleVolume";

    /** Configuration path, Cauldron.Sound.ItemSplashVolume */
    public static final String CONFIG_CAULDRON_SOUND_ITEM_SPLASH_VOLUME = "Cauldron.Sound.ItemSplashVolume";

    /** Configuration path, Cauldron.Sound.SuccessfulCraftVolume */
    public static final String CONFIG_CAULDRON_SOUND_SUCCESSFUL_CRAFT_VOLUME = "Cauldron.Sound.SuccessfulCraftVolume";

    private AlchemaConstants() { }

}
