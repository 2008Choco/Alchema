package wtf.choco.alchema.essence;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
 * Tests:
 * - testRegisteredEntityTypes(): Ensure that all EntityTypes are registered to the EntityEssenceEffectRegistry
 * - testDefaultEntityEffectConstants(): Ensure that all EntityTypes have a corresponding DefaultEntityEffects constants
 */
class EntityEssenceEffectRegistryTest {

    // Entities that will never have registered entity essence. Either impossible entity types or not necessary
    private static final Set<@NotNull EntityType> IGNORED_ENTITY_TYPES = EnumSet.of(
            EntityType.ARMOR_STAND,
            EntityType.UNKNOWN
    );

    private static final Set<@NotNull EntityType> EXPECTED_ENTITY_TYPES = EnumSet.noneOf(EntityType.class);

    static {
        // Collect all valid entity types that should have a default entity effect
        for (EntityType entityType : EntityType.values()) {
            if (!entityType.isAlive()) {
                continue;
            }

            EXPECTED_ENTITY_TYPES.add(entityType);
        }

        EXPECTED_ENTITY_TYPES.removeAll(IGNORED_ENTITY_TYPES);
    }

    private final EntityEssenceEffectRegistry essenceEffectRegistry = new EntityEssenceEffectRegistry();

    {
        // Register the entity types for testing
        EntityEssenceEffectRegistry.registerDefaultAlchemaEssences(essenceEffectRegistry);
    }

    @Test
    void testRegisteredEntityTypes() {
        List<@NotNull EntityType> missing = new ArrayList<>();

        EXPECTED_ENTITY_TYPES.forEach(entityType -> {
            if (essenceEffectRegistry.getEntityEssenceData(entityType) != null) {
                return;
            }

            missing.add(entityType);
        });

        if (missing.isEmpty()) {
            return; // Assume we passed
        }

        System.out.println("Missing " + missing.size() + " entity registrations in entity effect registry:");
        missing.forEach(entityType -> {
            System.out.println("\t - " + entityType.getKey() + " (EntityType." + entityType.name() + ")");
        });

        Assertions.fail("Missing registrations for " + missing.size() + " entities.");
    }

    @Test
    void testDefaultEntityEffectConstants() {
        List<@NotNull EntityType> missing = new ArrayList<>();

        Class<@NotNull DefaultEntityEffects> defaultEffectsClass = DefaultEntityEffects.class;

        EXPECTED_ENTITY_TYPES.forEach(entityType -> {
            try {
                defaultEffectsClass.getDeclaredField(entityType.name());
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                // The type was missing, we'll keep track of this for later
                missing.add(entityType);
            }
        });

        if (missing.isEmpty()) {
            return; // Assume we passed
        }

        System.out.println("Missing " + missing.size() + " default entity effect constants:");
        missing.forEach(entityType -> {
            System.out.println("\t - " + entityType.getKey() + " (" + defaultEffectsClass.getSimpleName() + "." + entityType.name() + ")");
        });

        Assertions.fail("Missing default effect constants for " + missing.size() + " entities.");
    }

}
