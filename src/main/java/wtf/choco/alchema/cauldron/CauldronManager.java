package wtf.choco.alchema.cauldron;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;

/**
 * Manages instances of {@link AlchemicalCauldron} in any given world.
 *
 * @author Parker Hawke - Choco
 */
public final class CauldronManager {

    private final Map<Block, AlchemicalCauldron> cauldrons = new HashMap<>();
    private final Alchema plugin;

    /**
     * Construct a new {@link CauldronManager}.
     *
     * @param plugin the plugin instance
     */
    public CauldronManager(@NotNull Alchema plugin) {
        this.plugin = plugin;
    }

    /**
     * Add an {@link AlchemicalCauldron} to the world.
     *
     * @param cauldron the cauldron to add
     */
    public void addCauldron(@NotNull AlchemicalCauldron cauldron) {
        Preconditions.checkNotNull(cauldron, "Cannot add null alchemical cauldron");
        this.cauldrons.put(cauldron.getCauldronBlock(), cauldron);
        cauldron.attachMetadata(plugin);
    }

    /**
     * Remove an {@link AlchemicalCauldron} from the world.
     *
     * @param cauldron the cauldron to remove
     */
    public void removeCauldron(@NotNull AlchemicalCauldron cauldron) {
        this.cauldrons.remove(cauldron.getCauldronBlock());
        cauldron.detachMetadata(plugin);
    }

    /**
     * Get an {@link AlchemicalCauldron} at the specified {@link Block}. If no cauldron is
     * present, null is returned.
     *
     * @param block the block from which to get a cauldron
     *
     * @return the alchemical cauldron at the block. null if none
     */
    @Nullable
    public AlchemicalCauldron getCauldron(@NotNull Block block) {
        return cauldrons.get(block);
    }

    /**
     * Get an {@link AlchemicalCauldron} at the specified {@link Location}. If no cauldron is
     * present, null is returned.
     *
     * @param location the location at which to get a cauldron
     *
     * @return the alchemical cauldron at the location. null if none
     */
    @Nullable
    public AlchemicalCauldron getCauldron(@NotNull Location location) {
        return (location != null) ? getCauldron(location.getBlock()) : null;
    }

    /**
     * Get an unmodifiable collection of all {@link AlchemicalCauldron}s in this manager.
     *
     * @return all cauldrons
     */
    @NotNull
    public Collection<@NotNull AlchemicalCauldron> getCauldrons() {
        return Collections.unmodifiableCollection(cauldrons.values());
    }

    /**
     * Clear all alchemical cauldrons from the world.
     */
    public void clearCauldrons() {
        this.cauldrons.values().forEach(cauldron -> cauldron.detachMetadata(plugin));
        this.cauldrons.clear();
    }

}
