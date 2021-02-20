package wtf.choco.alchema.cauldron;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.api.event.CauldronIngredientsDropEvent;
import wtf.choco.alchema.config.CauldronConfigurationContext;
import wtf.choco.alchema.util.AlchemaConstants;

/**
 * An implementation of {@link BukkitRunnable} that handles the updating and ticking of
 * in-world {@link AlchemicalCauldron} instances.
 *
 * @author Parker Hawke - Choco
 */
public final class CauldronUpdateTask extends BukkitRunnable {

    private static CauldronUpdateTask instance = null;

    private int currentTick = 0;

    private CauldronConfigurationContext cauldronConfiguration;
    private boolean dirty = true;

    private final List<AlchemicalCauldron> forRemoval = new ArrayList<>(4);

    private final Alchema plugin;
    private final CauldronManager cauldronManager;

    private CauldronUpdateTask(@NotNull Alchema plugin) {
        this.plugin = plugin;
        this.cauldronManager = plugin.getCauldronManager();
    }

    @Override
    public void run() {
        this.currentTick++;

        Collection<@NotNull AlchemicalCauldron> cauldrons = cauldronManager.getCauldrons();
        if (cauldrons.isEmpty()) {
            return;
        }

        // Pull configuration values if dirty
        if (dirty) {
            FileConfiguration config = plugin.getConfig();

            int minEssenceOnDeath = Math.max(config.getInt(AlchemaConstants.CONFIG_CAULDRON_ENTITIES_MIN_ESSENCE_ON_DEATH, 50), 0);
            this.cauldronConfiguration = CauldronConfigurationContext.builder()
                    .itemSearchInterval(Math.max(config.getInt(AlchemaConstants.CONFIG_CAULDRON_ITEM_SEARCH_INTERVAL, 1), 1))
                    .millisecondsToHeatUp(Math.max(config.getInt(AlchemaConstants.CONFIG_CAULDRON_MILLISECONDS_TO_HEAT_UP, 5000), 0))
                    .damageEntities(config.getBoolean(AlchemaConstants.CONFIG_CAULDRON_ENTITIES_DAMAGE, true))
                    .minEssenceOnDeath(minEssenceOnDeath)
                    .maxEssenceOnDeath(Math.max(config.getInt(AlchemaConstants.CONFIG_CAULDRON_ENTITIES_MAX_ESSENCE_ON_DEATH, 100), minEssenceOnDeath))
                    .ambientBubbleVolume((float) config.getDouble(AlchemaConstants.CONFIG_CAULDRON_SOUND_AMBIENT_BUBBLE_VOLUME, 0.45))
                    .itemSplashVolume((float) config.getDouble(AlchemaConstants.CONFIG_CAULDRON_SOUND_ITEM_SPLASH_VOLUME, 1.0))
                    .successfulCraftVolume((float) config.getDouble(AlchemaConstants.CONFIG_CAULDRON_SOUND_SUCCESSFUL_CRAFT_VOLUME, 0.5))
                    .build();

            this.dirty = false;
        }

        for (AlchemicalCauldron cauldron : cauldrons) {
            if (!cauldron.isLoaded()) {
                continue;
            }

            // Remove cauldrons that aren't cauldrons anymore. Ingredients are dropped during removal after this iteration.
            Block block = cauldron.getCauldronBlock();
            if (block.getType() != Material.CAULDRON) {
                this.forRemoval.add(cauldron);
                continue;
            }

            cauldron.update(plugin, cauldronConfiguration, currentTick);
        }

        if (!forRemoval.isEmpty()) {
            this.forRemoval.forEach(cauldron -> {
                cauldron.dropIngredients(CauldronIngredientsDropEvent.Reason.DESTROYED, null);
                this.cauldronManager.removeCauldron(cauldron);
            });

            this.forRemoval.clear();
        }
    }

    /**
     * Mark this cauldron update task as dirty.
     * <p>
     * When marked as dirty, this update task will re-fetch cached configuration values from
     * Alchema's configuration file.
     */
    public void markAsDirty() {
        this.dirty = true;
    }

    /**
     * Get the cauldron update task instance if it exists.
     * <p>
     * If this method is called before {@link #startTask(Alchema)}, an exception will be thrown.
     *
     * @return the update task instance
     */
    @NotNull
    public static CauldronUpdateTask get() {
        Preconditions.checkState(instance != null, "CauldronUpdateTask has not yet been started. Cannot fetch instance.");
        return instance;
    }

    /**
     * Start the singleton {@link CauldronUpdateTask}.
     * <p>
     * <strong>NOTE:</strong> This is for internal use only
     *
     * @param plugin the plugin to start the task
     *
     * @return the task instance
     */
    @NotNull
    public static CauldronUpdateTask startTask(@NotNull Alchema plugin) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");

        if (instance == null) {
            instance = new CauldronUpdateTask(plugin);
            instance.runTaskTimer(plugin, 0, 1);
        }

        return instance;
    }

}
