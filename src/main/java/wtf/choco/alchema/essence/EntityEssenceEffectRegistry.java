package wtf.choco.alchema.essence;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a registry by which {@link EntityType EntityTypes} may be mapped to their
 * {@link EntityEssenceData} counterparts.
 *
 * @author Parker Hawke - Choco
 */
public final class EntityEssenceEffectRegistry {

    private final Map<@NotNull EntityType, @NotNull EntityEssenceData> essenceDataByEntityType = new EnumMap<>(EntityType.class);

    /**
     * Register an {@link EntityEssenceData} for the given {@link EntityType}.
     *
     * @param essenceData the essence data to register
     * @param override whether or not to override an existing entry (if one exists)
     *
     * @return true if registered successfully. false if override is false and an entry already
     * exists for the given EntityType
     */
    public boolean registerEntityEssenceData(@NotNull EntityEssenceData essenceData, boolean override) {
        Preconditions.checkArgument(essenceData != null, "essenceData must not be null");

        EntityType entityType = essenceData.getEntityType();
        if (!override && essenceDataByEntityType.containsKey(entityType)) {
            return false;
        }

        this.essenceDataByEntityType.put(entityType, essenceData);
        return true;
    }

    /**
     * Register an {@link EntityEssenceData} for the given {@link EntityType}. If essence data
     * has already been registered for the given EntityType, this method will not attempt to
     * override, fail silently and return false.
     *
     * @param essenceData the essence data to register
     *
     * @return true if registered successfully. false if an entry already exists for the given
     * EntityType
     */
    public boolean registerEntityEssenceData(@NotNull EntityEssenceData essenceData) {
        return registerEntityEssenceData(essenceData, false);
    }

    /**
     * Get the {@link EntityEssenceData} registered to the given {@link EntityType}.
     *
     * @param entityType the entity type whose essence data to get
     *
     * @return the entity essence data. null if none registered
     */
    @Nullable
    public EntityEssenceData getEntityEssenceData(@NotNull EntityType entityType) {
        Preconditions.checkArgument(entityType != null, "entityType must not be null");

        return essenceDataByEntityType.get(entityType);
    }

    /**
     * Check whether or not the given {@link EntityType} has {@link EntityEssenceData}
     * registered.
     *
     * @param entityType the entity type to check
     *
     * @return true if registered, false otherwise
     */
    public boolean hasEntityEssenceData(@NotNull EntityType entityType) {
        Preconditions.checkArgument(entityType != null, "entityType must not be null");

        return essenceDataByEntityType.containsKey(entityType);
    }

    /**
     * Get an unmodifiable Set of {@link EntityType EntityTypes} for which essence data has
     * been registered in this registry.
     *
     * @return the registered entity types
     */
    @NotNull
    public Set<@NotNull EntityType> getRegisteredEntityEssenceTypes() {
        return Collections.unmodifiableSet(essenceDataByEntityType.keySet());
    }

    /**
     * Clear all registered entity essence data in this registry.
     */
    public void clearEntityEssenceData() {
        this.essenceDataByEntityType.clear();
    }

    /**
     * Register all default {@link EntityEssenceData} for every {@link EntityType} supported by
     * Alchema on initial install. This method will override any existing registrations in the
     * provided registry.
     *
     * @param registry the registry to which the essence data should be registered.
     */
    public static void registerDefaultAlchemaEssences(@NotNull EntityEssenceEffectRegistry registry) {
        Preconditions.checkArgument(registry != null, "registry must not be null");

        // A
        register(registry, EntityType.AXOLOTL, 0xF59DE0, DefaultEntityEffects.AXOLOTL);

        // B
        register(registry, EntityType.BAT, 0x3C2F17, DefaultEntityEffects.BAT);
        register(registry, EntityType.BEE, 0xFBD367, DefaultEntityEffects.BEE);
        register(registry, EntityType.BLAZE, 0xB76D00, DefaultEntityEffects.BLAZE);

        // C
        register(registry, EntityType.CAT, 0x937155, DefaultEntityEffects.CAT);
        register(registry, EntityType.CAVE_SPIDER, 0x002D31, DefaultEntityEffects.CAVE_SPIDER);
        register(registry, EntityType.CHICKEN, 0xB90000, DefaultEntityEffects.CHICKEN);
        register(registry, EntityType.COD, 0x453834, DefaultEntityEffects.COD);
        register(registry, EntityType.COW, 0x2E231A, DefaultEntityEffects.COW);
        register(registry, EntityType.CREEPER, 0x52A044, DefaultEntityEffects.CREEPER);

        // D
        register(registry, EntityType.DONKEY, 0x897766, DefaultEntityEffects.DONKEY);
        register(registry, EntityType.DOLPHIN, 0xC1CFE0, DefaultEntityEffects.DOLPHIN);
        register(registry, EntityType.DROWNED, 0x386A5D, DefaultEntityEffects.DROWNED);

        // E
        register(registry, EntityType.ELDER_GUARDIAN, 0x9A988E, DefaultEntityEffects.ELDER_GUARDIAN);
        register(registry, EntityType.ENDERMAN, 0xC140D6, DefaultEntityEffects.ENDERMAN);
        register(registry, EntityType.ENDERMITE, 0x3F2F53, DefaultEntityEffects.ENDERMITE);
        register(registry, EntityType.EVOKER, 0xC5B26A, DefaultEntityEffects.EVOKER);

        // F
        register(registry, EntityType.FOX, 0xE48D40, DefaultEntityEffects.FOX);

        // G
        register(registry, EntityType.GHAST, 0xAFAFAF, DefaultEntityEffects.GHAST);
        register(registry, EntityType.GLOW_SQUID, 0x2F9799, DefaultEntityEffects.GLOW_SQUID);
        register(registry, EntityType.GOAT, 0xE6E6E6, DefaultEntityEffects.GOAT);
        register(registry, EntityType.GUARDIAN, 0x699381, DefaultEntityEffects.GUARDIAN);

        // H
        register(registry, EntityType.HOGLIN, 0xE59E73, DefaultEntityEffects.HOGLIN);
        register(registry, EntityType.HORSE, 0xBA9979, DefaultEntityEffects.HORSE);
        register(registry, EntityType.HUSK, 0x937E52, DefaultEntityEffects.HUSK);

        // I
        register(registry, EntityType.ILLUSIONER, 0x0E406B, DefaultEntityEffects.ILLUSIONER);
        register(registry, EntityType.IRON_GOLEM, 0x909696, DefaultEntityEffects.IRON_GOLEM);

        // L
        register(registry, EntityType.LLAMA, 0xB8AB90, DefaultEntityEffects.LLAMA);

        // M
        register(registry, EntityType.MAGMA_CUBE, 0x942C05, DefaultEntityEffects.MAGMA_CUBE);
        register(registry, EntityType.MULE, 0x4F2B1A, DefaultEntityEffects.MULE);
        register(registry, EntityType.MUSHROOM_COW, 0x7A0C0D, DefaultEntityEffects.MUSHROOM_COW);

        // O
        register(registry, EntityType.OCELOT, 0xEEB162, DefaultEntityEffects.OCELOT);

        // P
        register(registry, EntityType.PANDA, 0xE4E4E4, DefaultEntityEffects.PANDA);
        register(registry, EntityType.PARROT, 0x0054B0, DefaultEntityEffects.PARROT);
        register(registry, EntityType.PHANTOM, 0x4F60A2, DefaultEntityEffects.PHANTOM);
        register(registry, EntityType.PIG, 0xEBA3A2, DefaultEntityEffects.PIG);
        register(registry, EntityType.PIGLIN, 0xA97454, DefaultEntityEffects.PIGLIN);
        register(registry, EntityType.PIGLIN_BRUTE, 0x131316, DefaultEntityEffects.PIGLIN_BRUTE);
        register(registry, EntityType.PILLAGER, 0x6D7171, DefaultEntityEffects.PILLAGER);
        register(registry, EntityType.PLAYER, 0xB48B64, true, DefaultEntityEffects.PLAYER);
        register(registry, EntityType.POLAR_BEAR, 0xF3F3F3, DefaultEntityEffects.POLAR_BEAR);
        register(registry, EntityType.PUFFERFISH, 0xF8A50C, DefaultEntityEffects.PUFFERFISH);

        // R
        register(registry, EntityType.RABBIT, 0xA28B72, DefaultEntityEffects.RABBIT);
        register(registry, EntityType.RAVAGER, 0x555552, DefaultEntityEffects.RAVAGER);

        // S
        register(registry, EntityType.SALMON, 0xBC3E3C, DefaultEntityEffects.SALMON);
        register(registry, EntityType.SHEEP, 0xA8A8A8, DefaultEntityEffects.SHEEP);
        register(registry, EntityType.SHULKER, 0x895F89, DefaultEntityEffects.SHULKER);
        register(registry, EntityType.SILVERFISH, 0x768C98, DefaultEntityEffects.SILVERFISH);
        register(registry, EntityType.SKELETON, 0xFFFFFF, DefaultEntityEffects.SKELETON);
        register(registry, EntityType.SKELETON_HORSE, 0x707070, DefaultEntityEffects.SKELETON_HORSE);
        register(registry, EntityType.SLIME, 0x75BF65, DefaultEntityEffects.SLIME);
        register(registry, EntityType.SNOWMAN, 0xADC8C8, DefaultEntityEffects.SNOWMAN);
        register(registry, EntityType.SPIDER, 0x241F1C, DefaultEntityEffects.SPIDER);
        register(registry, EntityType.SQUID, 0x3D4f5D, DefaultEntityEffects.SQUID);
        register(registry, EntityType.STRAY, 0x475657, DefaultEntityEffects.STRAY);
        register(registry, EntityType.STRIDER, 0x913032, DefaultEntityEffects.STRIDER);

        // T
        register(registry, EntityType.TRADER_LLAMA, 0x38507A, DefaultEntityEffects.TRADER_LLAMA);
        register(registry, EntityType.TROPICAL_FISH, 0xD85F13, DefaultEntityEffects.TROPICAL_FISH);
        register(registry, EntityType.TURTLE, 0x46BD49, DefaultEntityEffects.TURTLE);

        // V
        register(registry, EntityType.VEX, 0x798EA2, DefaultEntityEffects.VEX);
        register(registry, EntityType.VILLAGER, 0xB48B64, DefaultEntityEffects.VILLAGER);
        register(registry, EntityType.VINDICATOR, 0x6C7171, DefaultEntityEffects.VINDICATOR);

        // W
        register(registry, EntityType.WANDERING_TRADER, 0x435F91, DefaultEntityEffects.WANDERING_TRADER);
        register(registry, EntityType.WITCH, 0x351757, DefaultEntityEffects.WITCH);
        register(registry, EntityType.WITHER, 0x262626, DefaultEntityEffects.WITHER);
        register(registry, EntityType.WITHER_SKELETON, 0x1E1E1E, DefaultEntityEffects.WITHER_SKELETON);
        register(registry, EntityType.WOLF, 0xAEA8A5, DefaultEntityEffects.WOLF);

        // Z
        register(registry, EntityType.ZOGLIN, 0xE68E87, DefaultEntityEffects.ZOGLIN);
        register(registry, EntityType.ZOMBIE, 0x40782F, DefaultEntityEffects.ZOMBIE);
        register(registry, EntityType.ZOMBIE_HORSE, 0x4D7342, DefaultEntityEffects.ZOMBIE_HORSE);
        register(registry, EntityType.ZOMBIE_VILLAGER, 0x40782F, DefaultEntityEffects.ZOMBIE_VILLAGER);
        register(registry, EntityType.ZOMBIFIED_PIGLIN, 0xBC4F4C, DefaultEntityEffects.ZOMBIFIED_PIGLIN);
    }

    private static void register(@NotNull EntityEssenceEffectRegistry registry, @NotNull EntityType entityType, int rgb, boolean glowing, @Nullable EssenceConsumptionCallback consumptionCallback) {
        // No point in precondition checking the registry. This is done above in the registerDefaultAlchemaEssences() method call.

        Preconditions.checkArgument(entityType != null, "entityType must not be null");
        Preconditions.checkArgument((rgb >> 24) == 0, "rgb data exceeds maximum colour space of 24 bits (3 bytes): ", rgb);

        registry.registerEntityEssenceData(new EntityEssenceData(entityType, Color.fromRGB(rgb), glowing, consumptionCallback), true);
    }

    private static void register(@NotNull EntityEssenceEffectRegistry registry, @NotNull EntityType entityType, int rgb, @Nullable EssenceConsumptionCallback consumptionCallback) {
        register(registry, entityType, rgb, false, consumptionCallback);
    }

}
