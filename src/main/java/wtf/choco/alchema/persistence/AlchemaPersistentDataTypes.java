package wtf.choco.alchema.persistence;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;

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
    public static final PersistentDataType<String, EntityType> ENTITY_TYPE = new PersistentDataTypeEntityType(EntityType.UNKNOWN);

    /**
     * A {@link NamespacedKey} {@link PersistentDataType}.
     */
    public static final PersistentDataType<String, NamespacedKey> NAMESPACED_KEY = new PersistentDataTypeNamespacedKey();

}
