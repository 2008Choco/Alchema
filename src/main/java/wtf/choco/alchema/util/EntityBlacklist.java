package wtf.choco.alchema.util;

import com.google.common.base.Preconditions;

import java.util.AbstractSet;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a blacklist of {@link EntityType EntityTypes} refreshable from a {@link Supplier}.
 *
 * @author Parker Hawke - Choco
 */
public class EntityBlacklist extends AbstractSet<EntityType> {

    private Set<@NotNull EntityType> entities = EnumSet.noneOf(EntityType.class);

    private final Supplier<@NotNull List<@NotNull String>> refresher;

    /**
     * Construct a new {@link EntityBlacklist}.
     *
     * @param refresher the refreshing function
     */
    public EntityBlacklist(@NotNull Supplier<@NotNull List<@NotNull String>> refresher) {
        Preconditions.checkArgument(refresher != null, "refresher must not be null");

        this.refresher = refresher;
        this.refresh();
    }

    @Override
    public boolean add(EntityType type) {
        Preconditions.checkArgument(type != null, "type must not be null");

        return entities.add(type);
    }

    @Override
    public boolean remove(Object object) {
        return entities.remove(object);
    }

    @Override
    public boolean contains(Object object) {
        return entities.contains(object);
    }

    @Override
    public int size() {
        return entities.size();
    }

    @Override
    @SuppressWarnings("null")
    public Iterator<EntityType> iterator() {
        return entities.iterator();
    }

    /**
     * Refresh this entity blacklist from the given refresher.
     */
    public void refresh() {
        if (refresher == null) {
            return;
        }

        this.entities.clear();
        this.refresher.get().forEach(entityIdString -> {
            NamespacedKey entityTypeKey = NamespacedKeyUtil.fromString(entityIdString, null);
            if (entityTypeKey == null) {
                return;
            }

            EntityType entityType = Registry.ENTITY_TYPE.get(entityTypeKey);
            if (entityType == null) {
                return;
            }

            this.add(entityType);
        });
    }

}
