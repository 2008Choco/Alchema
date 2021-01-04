package wtf.choco.alchema.essence;

/**
 * Represents a set of constant {@link EssenceConsumptionCallback EssenceConsumptionCallbacks}
 * for use in default entity registration for Alchema.
 * <p>
 * Constants in this class are not made public as access to these constants should be done
 * using {@link EntityEssenceData#getConsumptionCallback()} instead. This class exists purely
 * for internal use to avoid writing entity callbacks in the {@link EntityEssenceEffectRegistry}.
 *
 * @author Parker Hawke - Choco
 */
public final class DefaultEntityEffects {

    // B
    static final EssenceConsumptionCallback BAT = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback BEE = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback BLAZE = (player, essenceData, item, amountOfEssence, potency) -> { };

    // C
    static final EssenceConsumptionCallback CAT = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback CAVE_SPIDER = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback CHICKEN = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback COD = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback COW = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback CREEPER = (player, essenceData, item, amountOfEssence, potency) -> { };

    // D
    static final EssenceConsumptionCallback DONKEY = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback DOLPHIN = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback DROWNED = (player, essenceData, item, amountOfEssence, potency) -> { };

    // E
    static final EssenceConsumptionCallback ELDER_GUARDIAN = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback ENDERMAN = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback ENDERMITE = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback EVOKER = (player, essenceData, item, amountOfEssence, potency) -> { };

    // F
    static final EssenceConsumptionCallback FOX = (player, essenceData, item, amountOfEssence, potency) -> { };

    // G
    static final EssenceConsumptionCallback GHAST = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback GUARDIAN = (player, essenceData, item, amountOfEssence, potency) -> { };

    // H
    static final EssenceConsumptionCallback HOGLIN = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback HORSE = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback HUSK = (player, essenceData, item, amountOfEssence, potency) -> { };

    // I
    static final EssenceConsumptionCallback ILLUSIONER = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback IRON_GOLEM = (player, essenceData, item, amountOfEssence, potency) -> { };

    // L
    static final EssenceConsumptionCallback LLAMA = (player, essenceData, item, amountOfEssence, potency) -> { };

    // M
    static final EssenceConsumptionCallback MAGMA_CUBE = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback MULE = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback MUSHROOM_COW = (player, essenceData, item, amountOfEssence, potency) -> { };

    // O
    static final EssenceConsumptionCallback OCELOT = (player, essenceData, item, amountOfEssence, potency) -> { };

    // P
    static final EssenceConsumptionCallback PANDA = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback PARROT = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback PHANTOM = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback PIG = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback PIGLIN = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback PIGLIN_BRUTE = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback PILLAGER = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback PLAYER = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback POLAR_BEAR = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback PUFFERFISH = (player, essenceData, item, amountOfEssence, potency) -> { };

    // R
    static final EssenceConsumptionCallback RABBIT = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback RAVAGER = (player, essenceData, item, amountOfEssence, potency) -> { };

    // S
    static final EssenceConsumptionCallback SALMON = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback SHEEP = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback SHULKER = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback SILVERFISH = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback SKELETON = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback SKELETON_HORSE = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback SLIME = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback SNOWMAN = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback SPIDER = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback SQUID = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback STRAY = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback STRIDER = (player, essenceData, item, amountOfEssence, potency) -> { };

    // T
    static final EssenceConsumptionCallback TRADER_LLAMA = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback TROPICAL_FISH = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback TURTLE = (player, essenceData, item, amountOfEssence, potency) -> { };

    // V
    static final EssenceConsumptionCallback VEX = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback VILLAGER = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback VINDICATOR = (player, essenceData, item, amountOfEssence, potency) -> { };

    // W
    static final EssenceConsumptionCallback WANDERING_TRADER = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback WITCH = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback WITHER = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback WITHER_SKELETON = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback WOLF = (player, essenceData, item, amountOfEssence, potency) -> { };

    // Z
    static final EssenceConsumptionCallback ZOGLIN = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback ZOMBIE = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback ZOMBIE_HORSE = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback ZOMBIE_VILLAGER = (player, essenceData, item, amountOfEssence, potency) -> { };
    static final EssenceConsumptionCallback ZOMBIFIED_PIGLIN = (player, essenceData, item, amountOfEssence, potency) -> { };

    private DefaultEntityEffects() { }

}
