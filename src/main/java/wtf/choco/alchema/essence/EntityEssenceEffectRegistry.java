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
     * <p>
     * <strong>NOTE:</strong> The EntityType to which the data is being registered MUST match
     * that of {@link EntityEssenceData#getEntityType()}.
     *
     * @param entityType the entity type for which to register essence data
     * @param essenceData the essence data to register
     * @param override whether or not to override an existing entry (if one exists)
     *
     * @return true if registered successfully. false if override is false and an entry already
     * exists for the given EntityType
     *
     * @throws IllegalArgumentException if {@code entityType != essenceData.getEntityType()}
     */
    public boolean registerEntityEssenceData(@NotNull EntityType entityType, @NotNull EntityEssenceData essenceData, boolean override) {
        Preconditions.checkArgument(entityType != null, "entityType must not be null");
        Preconditions.checkArgument(essenceData != null, "essenceData must not be null");
        Preconditions.checkArgument(entityType == essenceData.getEntityType(), "essenceData.getEntityType() must match the entityType to which it is registered.");

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
     * <p>
     * <strong>NOTE:</strong> The EntityType to which the data is being registered MUST match
     * that of {@link EntityEssenceData#getEntityType()}.
     *
     * @param entityType the entity type for which to register essence data
     * @param essenceData the essence data to register
     *
     * @return true if registered successfully. false if an entry already exists for the given
     * EntityType
     *
     * @throws IllegalArgumentException if {@code entityType != essenceData.getEntityType()}
     */
    public boolean registerEntityEssenceData(@NotNull EntityType entityType, @NotNull EntityEssenceData essenceData) {
        return registerEntityEssenceData(entityType, essenceData, false);
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

        // Not yet configured
        register(registry, EntityType.BAT, 0x3C2F17);
        register(registry, EntityType.BEE, 0xFBD367);
        register(registry, EntityType.BLAZE, 0xB76D00);

        register(registry, EntityType.CAT, 0x937155);
        register(registry, EntityType.CAVE_SPIDER, 0x002D31);
        register(registry, EntityType.CHICKEN, 0xB90000);
        register(registry, EntityType.COD, 0x453834);
        register(registry, EntityType.COW, 0x2E231A);
        register(registry, EntityType.CREEPER, 0x52A044);

        register(registry, EntityType.DONKEY, 0x897766);
        register(registry, EntityType.DOLPHIN, 0xC1CFE0);
        register(registry, EntityType.DROWNED, 0x386A5D);

        register(registry, EntityType.ELDER_GUARDIAN, 0x9A988E);
        register(registry, EntityType.ENDERMAN, 0xC140D6);
        register(registry, EntityType.ENDERMITE, 0x3F2F53);
        register(registry, EntityType.EVOKER, 0xC5B26A);

        register(registry, EntityType.FOX, 0xE48D40);

        register(registry, EntityType.GHAST, 0xAFAFAF);
        register(registry, EntityType.GUARDIAN, 0x699381);

        register(registry, EntityType.HOGLIN, 0xE59E73);
        register(registry, EntityType.HORSE, 0xBA9979);
        register(registry, EntityType.HUSK, 0x937E52);

        register(registry, EntityType.ILLUSIONER, 0x0E406B);
        register(registry, EntityType.IRON_GOLEM, 0x909696);

        register(registry, EntityType.LLAMA, 0xB8AB90);

        register(registry, EntityType.MAGMA_CUBE, 0x942C05);
        register(registry, EntityType.MULE, 0x4F2B1A);
        register(registry, EntityType.MUSHROOM_COW, 0x7A0C0D);

        register(registry, EntityType.OCELOT, 0xEEB162);

        register(registry, EntityType.PANDA, 0xE4E4E4);
        register(registry, EntityType.PARROT, 0x0054B0);
        register(registry, EntityType.PHANTOM, 0x4F60A2);
        register(registry, EntityType.PIG, 0xEBA3A2);
        register(registry, EntityType.PIGLIN, 0xA97454);
        register(registry, EntityType.PIGLIN_BRUTE, 0x131316);
        register(registry, EntityType.PILLAGER, 0x6D7171);
        register(registry, EntityType.PLAYER, 0xB48B64, true); // Skin coloured
        register(registry, EntityType.POLAR_BEAR, 0xF3F3F3);
        register(registry, EntityType.PUFFERFISH, 0xF8A50C);

        register(registry, EntityType.RABBIT, 0xA28B72);
        register(registry, EntityType.RAVAGER, 0x555552);

        register(registry, EntityType.SALMON, 0xBC3E3C);
        register(registry, EntityType.SHEEP, 0xA8A8A8);
        register(registry, EntityType.SHULKER, 0x895F89);
        register(registry, EntityType.SILVERFISH, 0x768C98);
        register(registry, EntityType.SKELETON, 0xFFFFFF);
        register(registry, EntityType.SKELETON_HORSE, 0x707070);
        register(registry, EntityType.SLIME, 0x75BF65);
        register(registry, EntityType.SNOWMAN, 0xADC8C8);
        register(registry, EntityType.SPIDER, 0x241F1C);
        register(registry, EntityType.SQUID, 0x3D4f5D);
        register(registry, EntityType.STRAY, 0x475657);
        register(registry, EntityType.STRIDER, 0x913032);

        register(registry, EntityType.TRADER_LLAMA, 0x38507A);
        register(registry, EntityType.TROPICAL_FISH, 0xD85F13);
        register(registry, EntityType.TURTLE, 0x46BD49);

        register(registry, EntityType.VEX, 0x798EA2);
        register(registry, EntityType.VILLAGER, 0xB48B64);
        register(registry, EntityType.VINDICATOR, 0x6C7171);

        register(registry, EntityType.WANDERING_TRADER, 0x435F91);
        register(registry, EntityType.WITCH, 0x351757);
        register(registry, EntityType.WITHER, 0x262626);
        register(registry, EntityType.WITHER_SKELETON, 0x1E1E1E);
        register(registry, EntityType.WOLF, 0xAEA8A5);

        register(registry, EntityType.ZOGLIN, 0xE68E87);
        register(registry, EntityType.ZOMBIE, 0x40782F);
        register(registry, EntityType.ZOMBIE_HORSE, 0x4D7342);
        register(registry, EntityType.ZOMBIE_VILLAGER, 0x40782F);
        register(registry, EntityType.ZOMBIFIED_PIGLIN, 0xBC4F4C);
    }

    private static void register(@NotNull EntityEssenceEffectRegistry registry, @NotNull EntityType entityType, int rgb, boolean glowing, @Nullable EssenceConsumptionCallback consumptionCallback) {
        // No point in precondition checking the registry. This is done above in the registerDefaultAlchemaEssences() method call.

        Preconditions.checkArgument(entityType != null, "entityType must not be null");
        Preconditions.checkArgument((rgb >> 24) == 0, "rgb data exceeds maximum colour space of 24 bits (3 bytes): ", rgb);

        registry.registerEntityEssenceData(entityType, new EntityEssenceData(entityType, Color.fromRGB(rgb), glowing, consumptionCallback != null, consumptionCallback), true);
    }

    private static void register(@NotNull EntityEssenceEffectRegistry registry, @NotNull EntityType entityType, int rgb, boolean glowing) {
        register(registry, entityType, rgb, glowing, null);
    }

    private static void register(@NotNull EntityEssenceEffectRegistry registry, @NotNull EntityType entityType, int rgb) {
        register(registry, entityType, rgb, false, null);
    }

}
