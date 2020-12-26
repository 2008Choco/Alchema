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
    @SuppressWarnings("deprecation")
    public NamespacedKey fromPrimitive(@NotNull String string, @NotNull PersistentDataAdapterContext context) {
        String[] split = string.split(":");
        return split.length >= 2 ? new NamespacedKey(split[0], split[1]) : NamespacedKey.minecraft(split[0]);
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
