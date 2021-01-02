package wtf.choco.alchema.persistence;

import com.google.common.base.Preconditions;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link PersistentDataType} implementation for {@link EntityType EntityTypes}.
 *
 * @author Parker Hawke - Choco
 */
public final class PersistentDataTypeEntityType implements PersistentDataType<String, EntityType> {

    private final EntityType defaultEntityType;

    PersistentDataTypeEntityType(@NotNull EntityType defaultEntityType) {
        Preconditions.checkArgument(defaultEntityType != null, "defaultEntityType must not be null");

        this.defaultEntityType = defaultEntityType;
    }

    @NotNull
    @Override
    public EntityType fromPrimitive(@NotNull String string, @NotNull PersistentDataAdapterContext context) {
        NamespacedKey entityKey = AlchemaPersistentDataTypes.NAMESPACED_KEY.fromPrimitive(string, context);
        EntityType entityType = Registry.ENTITY_TYPE.get(entityKey);
        return entityType != null ? entityType : defaultEntityType;
    }

    @NotNull
    @Override
    public String toPrimitive(@NotNull EntityType key, @NotNull PersistentDataAdapterContext context) {
        return key.getKey().toString();
    }

    @NotNull
    @Override
    @SuppressWarnings("null") // Eclipse being strange with nullability annotations
    public Class<EntityType> getComplexType() {
        return EntityType.class;
    }

    @NotNull
    @Override
    @SuppressWarnings("null") // Eclipse being strange with nullability annotations
    public Class<String> getPrimitiveType() {
        return String.class;
    }

}
