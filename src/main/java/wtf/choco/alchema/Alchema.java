package wtf.choco.alchema;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.cauldron.CauldronManager;
import wtf.choco.alchema.cauldron.CauldronUpdateTask;
import wtf.choco.alchema.command.CommandAlchema;
import wtf.choco.alchema.command.CommandGiveVialOfEssence;
import wtf.choco.alchema.crafting.CauldronIngredientEntityEssence;
import wtf.choco.alchema.crafting.CauldronIngredientItemStack;
import wtf.choco.alchema.crafting.CauldronIngredientMaterial;
import wtf.choco.alchema.crafting.CauldronRecipeRegistry;
import wtf.choco.alchema.essence.EntityEssenceData;
import wtf.choco.alchema.essence.EntityEssenceEffectRegistry;
import wtf.choco.alchema.listener.CauldronDeathMessageListener;
import wtf.choco.alchema.listener.CauldronManipulationListener;
import wtf.choco.alchema.listener.EmptyVialRecipeDiscoverListener;
import wtf.choco.alchema.listener.EntityEssenceCollectionListener;
import wtf.choco.alchema.listener.UpdateReminderListener;
import wtf.choco.alchema.listener.VialOfEssenceConsumptionListener;
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.alchema.util.UpdateChecker;
import wtf.choco.alchema.util.UpdateChecker.UpdateReason;

/**
 * The main plugin class of Alchema.
 *
 * @author Parker Hawke - Choco
 */
public final class Alchema extends JavaPlugin {

    /** The chat prefix used by Alchema */
    public static final String CHAT_PREFIX = ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Alchema | " + ChatColor.GRAY;

    /** The GSON instance provided by Alchema */
    public static final Gson GSON = new Gson();

    private static Alchema instance;

    private final CauldronManager cauldronManager = new CauldronManager();
    private final CauldronRecipeRegistry recipeRegistry = new CauldronRecipeRegistry();
    private final EntityEssenceEffectRegistry entityEssenceEffectRegistry = new EntityEssenceEffectRegistry();

    private File cauldronFile;
    private File recipesDirectory;

    private CauldronUpdateTask cauldronUpdateTask;

    private EntityEssenceCollectionListener entityEssenceLootListener;

    @Override
    public void onLoad() {
        instance = this;

        this.recipeRegistry.registerIngredientType(CauldronIngredientItemStack.KEY, CauldronIngredientItemStack::new);
        this.recipeRegistry.registerIngredientType(CauldronIngredientMaterial.KEY, CauldronIngredientMaterial::new);
        this.recipeRegistry.registerIngredientType(CauldronIngredientEntityEssence.KEY, object -> new CauldronIngredientEntityEssence(object, entityEssenceEffectRegistry));
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.recipesDirectory = new File(getDataFolder(), "recipes");
        if (!recipesDirectory.exists()) {
            this.saveDefaultDirectory("recipes", true);
        }

        // Load cauldrons from file
        this.cauldronFile = new File(getDataFolder(), "cauldrons.json");
        if (cauldronFile.exists()) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try (BufferedReader reader = Files.newReader(cauldronFile, Charset.defaultCharset())) {
                    JsonArray cauldronArray = GSON.fromJson(reader, JsonArray.class);
                    cauldronArray.forEach(element -> {
                        if (!element.isJsonObject()) {
                            return;
                        }

                        AlchemicalCauldron cauldron = AlchemicalCauldron.fromJson(element.getAsJsonObject(), recipeRegistry);
                        if (cauldron == null) {
                            this.getLogger().info("Attempted to load cauldron at a position where a cauldron was not present.");
                            return;
                        }

                        this.cauldronManager.addCauldron(cauldron);
                    });
                } catch (IOException | JsonSyntaxException e) {
                    e.printStackTrace();
                }
            });
        }

        // Load cauldron recipes (asynchronously)
        this.recipeRegistry.loadCauldronRecipes(this, recipesDirectory).whenComplete((result, exception) -> {
            if (exception != null) {
                exception.printStackTrace();
                return;
            }

            if (result.getTotal() > 0) {
                this.getLogger().info("Registered " + result.getTotal() + " cauldron recipes. (" + result.getThirdParty() + " third-party). Completed in " + result.getTimeToComplete() + "ms");
            }

            result.getFailures().forEach(failureReport -> {
                this.getLogger().warning("Failed to load recipe " + failureReport.getRecipeKey() + ". Reason: " + failureReport.getReason());
            });
        });

        // Register entity essence effects
        EntityEssenceEffectRegistry.registerDefaultAlchemaEssences(entityEssenceEffectRegistry);

        // Register listeners
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new CauldronDeathMessageListener(this), this);
        manager.registerEvents(new CauldronManipulationListener(this), this);
        manager.registerEvents(new EmptyVialRecipeDiscoverListener(), this);
        manager.registerEvents(this.entityEssenceLootListener = new EntityEssenceCollectionListener(this), this);
        manager.registerEvents(new UpdateReminderListener(this), this);
        manager.registerEvents(new VialOfEssenceConsumptionListener(this), this);

        // Register commands
        this.registerCommandSafely("alchema", new CommandAlchema(this));
        this.registerCommandSafely("givevialofessence", new CommandGiveVialOfEssence(this));

        // Register crafting recipes
        Bukkit.addRecipe(new ShapedRecipe(AlchemaConstants.RECIPE_KEY_EMPTY_VIAL, EntityEssenceData.createEmptyVial(3)).shape("G G", " G ").setIngredient('G', Material.GLASS_PANE));

        this.cauldronUpdateTask = CauldronUpdateTask.startTask(this);

        // Load Metrics
        if (getConfig().getBoolean("MetricsEnabled", true)) {
            this.getLogger().info("Enabling plugin metrics");
            new Metrics(this, 9741); // https://bstats.org/what-is-my-plugin-id
        }

        UpdateChecker updateChecker = UpdateChecker.init(this, 87078);
        if (getConfig().getBoolean("CheckForUpdates", true)) {
            this.getLogger().info("Getting version information...");
            updateChecker.requestUpdateCheck().whenComplete((result, exception) -> {
                if (result.requiresUpdate()) {
                    this.getLogger().info(String.format("An update is available! Alchema %s may be downloaded on SpigotMC", result.getNewestVersion()));
                    return;
                }

                UpdateReason reason = result.getReason();
                if (reason == UpdateReason.UP_TO_DATE) {
                    this.getLogger().info(String.format("Your version of Alchema (%s) is up to date!", result.getNewestVersion()));
                } else if (reason == UpdateReason.UNRELEASED_VERSION) {
                    this.getLogger().info(String.format("Your version of Alchema (%s) is more recent than the one publicly available. Are you on a development build?", result.getNewestVersion()));
                } else {
                    this.getLogger().warning("Could not check for a new version of Alchema. Reason: " + reason);
                }
            });
        }
    }

    @Override
    public void onDisable() {
        // Write all cauldrons to file
        Collection<@NotNull AlchemicalCauldron> cauldrons = cauldronManager.getCauldrons();
        if (cauldrons.size() > 0) {
            JsonArray cauldronsArray = new JsonArray();
            cauldrons.forEach(cauldron -> cauldronsArray.add(cauldron.write(new JsonObject())));

            try (PrintWriter writer = new PrintWriter(cauldronFile)) {
                GSON.toJson(cauldronsArray, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.cauldronFile.delete();
        }

        this.cauldronManager.clearCauldrons();
        this.recipeRegistry.clearRecipes();
        this.recipeRegistry.clearIngredientTypes();
        this.entityEssenceEffectRegistry.clearEntityEssenceData();

        if (cauldronUpdateTask != null) {
            this.cauldronUpdateTask.cancel();
        }
    }

    /**
     * Get the {@link CauldronManager} instance.
     *
     * @return the cauldron manager
     */
    @NotNull
    public CauldronManager getCauldronManager() {
        return cauldronManager;
    }

    /**
     * Get the {@link CauldronRecipeRegistry} instance.
     *
     * @return the cauldron recipe registry
     */
    @NotNull
    public CauldronRecipeRegistry getRecipeRegistry() {
        return recipeRegistry;
    }

    /**
     * Get the {@link EntityEssenceEffectRegistry} instance.
     *
     * @return the entity essence effect registry
     */
    public EntityEssenceEffectRegistry getEntityEssenceEffectRegistry() {
        return entityEssenceEffectRegistry;
    }

    /**
     * Get the directory from which Alchema's recipes are loaded.
     *
     * @return the recipes directory
     */
    @NotNull
    public File getRecipesDirectory() {
        return recipesDirectory;
    }

    /**
     * Refresh the entity blacklists from the configuration loaded into memory.
     */
    public void refreshEntityBlacklists() {
        this.entityEssenceLootListener.refreshBlacklists();
    }

    /**
     * Get an instance of {@link Alchema}.
     *
     * @return the alchema instance
     */
    @NotNull
    public static Alchema getInstance() {
        return instance;
    }

    /**
     * Get a {@link NamespacedKey} where the namespace is {@link Alchema}.
     *
     * @param key the key
     *
     * @return the Alchema namespaced key
     */
    @NotNull
    public static NamespacedKey key(@NotNull String key) {
        Preconditions.checkArgument(key != null, "key must not be null");

        return new NamespacedKey(instance, key);
    }

    private void saveDefaultDirectory(@NotNull String directory, boolean saveChildDirectories) {
        Preconditions.checkArgument(directory != null, "directory cannot be null");

        try (JarFile jar = new JarFile(getFile())) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (!name.startsWith(directory + "/")) {
                    continue;
                }

                if (entry.isDirectory()) {
                    if (saveChildDirectories) {
                        this.saveDefaultDirectory(entry.getName(), saveChildDirectories);
                    }

                    continue;
                }

                this.saveResource(name, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerCommandSafely(@NotNull String commandString, @NotNull CommandExecutor executor) {
        PluginCommand command = getCommand(commandString);
        if (command == null) {
            return;
        }

        command.setExecutor(executor);

        if (executor instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) executor);
        }
    }

}
