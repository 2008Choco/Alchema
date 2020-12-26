package wtf.choco.alchema.essence;

import com.google.common.base.Preconditions;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Color;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

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

        register(registry, EntityType.ZOMBIE, 64, 120, 47);
        // TODO: Register all the other entity types I want
    }

    private static void register(@NotNull EntityEssenceEffectRegistry registry, @NotNull EntityType entityType, int red, int green, int blue, @Nullable Consumer<@NotNull LivingEntity> effectApplier) {
        // No point in precondition checking the registry. This is done above in the registerDefaultAlchemaEssences() method call.

        Preconditions.checkArgument(entityType != null, "entityType must not be null");
        Preconditions.checkArgument(red >= 0 && red <= 255, "red must not exceed 0 - 255 (inclusive)");
        Preconditions.checkArgument(green >= 0 && green <= 255, "green must not exceed 0 - 255 (inclusive)");
        Preconditions.checkArgument(blue >= 0 && blue <= 255, "blue must not exceed 0 - 255 (inclusive)");

        registry.registerEntityEssenceData(entityType, new EntityEssenceData(entityType, Color.fromRGB(red, green, blue), effectApplier), true);
    }

    private static void register(@NotNull EntityEssenceEffectRegistry registry, @NotNull EntityType entityType, int red, int green, int blue) {
        register(registry, entityType, red, green, blue, null);
    }

}
