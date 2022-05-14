package wtf.choco.alchema.util;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;

/**
 * A utility class containing methods that relate to essence.
 */
public final class EssenceUtil {

    private EssenceUtil() { }

    /**
     * Check whether or not the given {@link Entity} can have its essence extracted.
     * <p>
     * An entity can have essence extracted only if it has not had its essence extracted
     * by a player within the timeout seconds specified by the configuration file.
     *
     * @param entity the entity to check
     * @param plugin the plugin instance
     *
     * @return true if the entity's essence may be extracted, false otherwise
     */
    public static boolean canHaveEssenceExtracted(@NotNull Entity entity, @NotNull Alchema plugin) {
        int timeoutSeconds = plugin.getConfig().getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_TIMEOUT_SECONDS, 300);
        if (timeoutSeconds <= 0) {
            return true;
        }

        List<MetadataValue> interactionMetadata = entity.getMetadata(AlchemaConstants.METADATA_KEY_INTERACTED_WITH_VIAL);
        long lastInteractedWith = -1;

        for (MetadataValue value : interactionMetadata) {
            lastInteractedWith = Math.max(lastInteractedWith, value.asLong());
        }

        long secondsSinceLastInteraction = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastInteractedWith);
        return secondsSinceLastInteraction >= timeoutSeconds;
    }

}
