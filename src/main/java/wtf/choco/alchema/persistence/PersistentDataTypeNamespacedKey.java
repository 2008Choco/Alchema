package wtf.choco.alchema.persistence;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link PersistentDataType} implementation for {@link NamespacedKey NamespacedKeys}.
 *
 * @author Parker Hawke - Choco
 */
public final class PersistentDataTypeNamespacedKey implements PersistentDataType<String, NamespacedKey> {

    PersistentDataTypeNamespacedKey() { }

    @NotNull
    @Override
    public NamespacedKey fromPrimitive(@NotNull String string, @NotNull PersistentDataAdapterContext context) {
        NamespacedKey key = NamespacedKey.fromString(string);
        if (key == null) {
            throw new IllegalStateException("Invalid namespaced key, \"" + string + "\"");
        }

        return key;
    }

    @NotNull
    @Override
    public String toPrimitive(@NotNull NamespacedKey key, @NotNull PersistentDataAdapterContext context) {
        return key.toString();
    }

    @NotNull
    @Override
    @SuppressWarnings("null") // Eclipse being strange with nullability annotations
    public Class<NamespacedKey> getComplexType() {
        return NamespacedKey.class;
    }

    @NotNull
    @Override
    @SuppressWarnings("null") // Eclipse being strange with nullability annotations
    public Class<String> getPrimitiveType() {
        return String.class;
    }

}
