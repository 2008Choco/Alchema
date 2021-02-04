package wtf.choco.alchema.persistence;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class defining constants of all types of {@link PersistentDataType PersistentDataTypes}
 * defined by Alchema.
 *
 * @author Parker Hawke - Choco
 */
public final class AlchemaPersistentDataTypes {

    /**
     * An {@link EntityType} {@link PersistentDataType}.
     */
    public static final PersistentDataType<@NotNull String, @NotNull EntityType> ENTITY_TYPE = new PersistentDataTypeEntityType(EntityType.UNKNOWN);

    /**
     * A {@link NamespacedKey} {@link PersistentDataType}.
     */
    public static final PersistentDataType<@NotNull String, @NotNull NamespacedKey> NAMESPACED_KEY = new PersistentDataTypeNamespacedKey();

}
