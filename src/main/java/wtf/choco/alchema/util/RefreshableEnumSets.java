package wtf.choco.alchema.util;

import java.util.List;
import java.util.function.Supplier;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import wtf.choco.commons.collection.RefreshableEnumSet;
import wtf.choco.commons.util.NamespacedKeyUtil;

/**
 * A set of utility methods to create {@link RefreshableEnumSet RefreshableEnumSets}.
 *
 * @author Parker Hawke - Choco
 */
public final class RefreshableEnumSets {

    private RefreshableEnumSets() { }

    /**
     * Create a {@link RefreshableEnumSet} for {@link EntityType} elements.
     *
     * @param refresher the refresher
     *
     * @return the enum set
     */
    @NotNull
    public static RefreshableEnumSet<@NotNull EntityType> entityType(@NotNull Supplier<@NotNull List<@NotNull String>> refresher) {
        return new RefreshableEnumSet<>(EntityType.class, refresher, string -> {
            NamespacedKey entityTypeKey = NamespacedKeyUtil.fromString(string);
            return entityTypeKey != null ? Registry.ENTITY_TYPE.get(entityTypeKey) : null;
        });
    }

}
