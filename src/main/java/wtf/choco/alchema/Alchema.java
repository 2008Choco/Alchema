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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.cauldron.CauldronManager;
import wtf.choco.alchema.cauldron.CauldronUpdateHandler;
import wtf.choco.alchema.command.CommandAlchema;
import wtf.choco.alchema.command.CommandGiveVialOfEssence;
import wtf.choco.alchema.crafting.CauldronIngredientEntityEssence;
import wtf.choco.alchema.crafting.CauldronIngredientItemStack;
import wtf.choco.alchema.crafting.CauldronIngredientMaterial;
import wtf.choco.alchema.crafting.CauldronRecipeRegistry;
import wtf.choco.alchema.crafting.RecipeResultItemStack;
import wtf.choco.alchema.essence.EntityEssenceData;
import wtf.choco.alchema.essence.EntityEssenceEffectRegistry;
import wtf.choco.alchema.integration.mmoitems.PluginIntegrationMMOItems;
import wtf.choco.alchema.listener.CauldronDeathMessageListener;
import wtf.choco.alchema.listener.CauldronManipulationListener;
import wtf.choco.alchema.listener.EmptyVialRecipeDiscoverListener;
import wtf.choco.alchema.listener.EntityEssenceCollectionListener;
import wtf.choco.alchema.listener.UpdateReminderListener;
import wtf.choco.alchema.listener.VialOfEssenceConsumptionListener;
import wtf.choco.alchema.metrics.MetricsHelper;
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.commons.integration.IntegrationHandler;
import wtf.choco.commons.util.UpdateChecker;
import wtf.choco.commons.util.UpdateChecker.UpdateReason;

/**
 * The main plugin class of Alchema.
 *
 * @author Parker Hawke - Choco
 */
public final class Alchema extends JavaPlugin {

    /** The chat prefix used by Alchema */
    public static final String CHAT_PREFIX = ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Alchema | " + ChatColor.GRAY;

    private static final Gson GSON = new Gson();

    private static Alchema instance;

    private final CauldronManager cauldronManager = new CauldronManager(this);
    private final CauldronRecipeRegistry recipeRegistry = new CauldronRecipeRegistry();
    private final EntityEssenceEffectRegistry entityEssenceEffectRegistry = new EntityEssenceEffectRegistry();

    private final IntegrationHandler integrationHandler = new IntegrationHandler(this);

    private File cauldronFile;
    private File recipesDirectory;

    private CauldronUpdateHandler cauldronUpdateTask;

    private EntityEssenceCollectionListener entityEssenceLootListener;

    @Override
    public void onLoad() {
        instance = this; // Needs to be set here so the CauldronIngredient* keys can be defined by Alchema#key()

        /*
         * These are done on load so other plugins may have the opportunity to use them as well.
         *
         * It is expected that other plugins hooking into VeinMiner with custom ingredient types also register onLoad()
         * because we need them for the recipe files. By the time their onEnable() is called, recipes files are already
         * being loaded so it's a tad too late to be registering new ingredient types.
         */
        // Ingredient types
        this.recipeRegistry.registerIngredientType(CauldronIngredientItemStack.KEY, CauldronIngredientItemStack::new);
        this.recipeRegistry.registerIngredientType(CauldronIngredientMaterial.KEY, CauldronIngredientMaterial::new);
        this.recipeRegistry.registerIngredientType(CauldronIngredientEntityEssence.KEY, object -> new CauldronIngredientEntityEssence(object, entityEssenceEffectRegistry));

        // Result types
        this.recipeRegistry.registerResultType(RecipeResultItemStack.KEY, RecipeResultItemStack::new);

        /*
         * We're also going to handle plugin integration registrations on load just to jump the gun a bit.
         * Calling #integrate() here ensures that PluginIntegration's load() methods are called onLoad().
         * For plugins like WorldGuard where registering flags must be done onLoad(), this is necessary.
         */
        this.integrationHandler.registerIntegrations("MMOItems", () -> PluginIntegrationMMOItems::new);
        this.integrationHandler.integrate();

        // Handle ingredient and result types for metrics (do this early so that third-party types aren't known)
        this.recipeRegistry.getIngredientTypes().forEach(MetricsHelper::addKnownIngredientKey);
        this.recipeRegistry.getResultTypes().forEach(MetricsHelper::addKnownResultKey);
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);

        // Save default recipes
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
        this.recipeRegistry.stopAcceptingRegistrations(); // Stop accepting registrations now. We're ready to load.
        this.recipeRegistry.loadCauldronRecipes(this, recipesDirectory).whenComplete((result, exception) -> {
            if (exception != null) {
                exception.printStackTrace();
                return;
            }

            if (result.getTotal() > 0) {
                this.getLogger().info("Registered " + result.getTotal() + " cauldron recipes. (" + result.getThirdParty() + " third-party). Completed in " + result.getTimeToComplete() + "ms");
            }

            result.getFailures().forEach(failureReport -> getLogger().warning("Failed to load recipe " + failureReport.getRecipeKey() + ". Reason: " + failureReport.getReason()));
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
        this.parseAndRegisterVialRecipe();

        this.cauldronUpdateTask = CauldronUpdateHandler.init(this);
        this.cauldronUpdateTask.startTask();

        // Enable integrations
        this.integrationHandler.enableIntegrations();

        // Load Metrics
        if (getConfig().getBoolean(AlchemaConstants.CONFIG_METRICS_ENABLED, true)) {
            this.getLogger().info("Enabling plugin metrics");

            Metrics metrics = new Metrics(this, 9741); // https://bstats.org/what-is-my-plugin-id
            MetricsHelper.registerCustomCharts(metrics, this);
        }

        UpdateChecker updateChecker = UpdateChecker.init(this, 87078);
        if (getConfig().getBoolean(AlchemaConstants.CONFIG_CHECK_FOR_UPDATES, true)) {
            this.getLogger().info("Getting version information...");
            updateChecker.requestUpdateCheck().whenComplete((result, exception) -> {
                if (result.requiresUpdate()) {
                    this.getLogger().info(String.format("An update is available! %s %s may be downloaded on SpigotMC", getName(), result.getNewestVersion()));
                    this.getLogger().info(String.format("For more information, run /%s version", getName().toLowerCase()));
                    return;
                }

                UpdateReason reason = result.getReason();
                if (reason == UpdateReason.UP_TO_DATE) {
                    this.getLogger().info(String.format("Your version of %s (%s) is up to date!", getName(), result.getNewestVersion()));
                } else if (reason == UpdateReason.UNRELEASED_VERSION) {
                    this.getLogger().info(String.format("Your version of %s (%s) is more recent than the one publicly available. Are you on a development build?", getName(), result.getNewestVersion()));
                } else {
                    this.getLogger().warning(String.format("Could not check for a new version of %s. Reason: %s", getName(), reason));
                }
            });
        }
    }

    @Override
    public void onDisable() {
        this.integrationHandler.disableIntegrations(true);

        // Write all cauldrons to file
        Collection<@NotNull AlchemicalCauldron> cauldrons = cauldronManager.getCauldrons();
        if (!cauldrons.isEmpty()) {
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
        this.recipeRegistry.clearResultTypes();
        this.entityEssenceEffectRegistry.clearEntityEssenceData();

        this.cauldronUpdateTask.cancelTask();

        MetricsHelper.clearKeyWhitelists();
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
    @NotNull
    public EntityEssenceEffectRegistry getEntityEssenceEffectRegistry() {
        return entityEssenceEffectRegistry;
    }

    /**
     * Get the {@link IntegrationHandler} instance.
     *
     * @return the integration handler
     */
    @NotNull
    public IntegrationHandler getIntegrationHandler() {
        return integrationHandler;
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
     * Get a list of paths to the default recipes provided by Alchema.
     * <p>
     * These paths are NOT the same as those in the plugin's recipe directory. These
     * are paths to the recipe files present in the plugin's jar file.
     *
     * @return the recipe paths
     */
    @NotNull
    public List<@NotNull String> getDefaultRecipePaths() {
        return getDefaultRecipePaths("recipes");
    }

    /**
     * Parse the empty vial of essence recipe from the config.yml and register it.
     * If the recipe was already registered, this method will unregister it. If the
     * recipe is not enabled, it will not be registered.
     */
    public void parseAndRegisterVialRecipe() {
        Bukkit.removeRecipe(AlchemaConstants.RECIPE_KEY_EMPTY_VIAL);

        if (!getConfig().getBoolean(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_RECIPE_ENABLED, true)) {
            return;
        }

        // Ensure that there are three entries with length of three. If not, don't register the recipe
        String[] shape = getConfig().getStringList(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_RECIPE_SHAPE).toArray(String[]::new);
        if (shape.length != 3 || !Arrays.stream(shape).allMatch(string -> string.length() == 3)) {
            return;
        }

        Map<Character, RecipeChoice> ingredients = new HashMap<>();
        ConfigurationSection ingredientsSection = getConfig().getConfigurationSection(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_RECIPE_INGREDIENTS);
        if (ingredientsSection != null) {
            ingredientsSection.getKeys(false).forEach(ingredientKeyString -> {
                char ingredientKey = ingredientKeyString.charAt(0);

                // Single material
                if (ingredientsSection.isString(ingredientKeyString)) {
                    String materialString = ingredientsSection.getString(ingredientKeyString);
                    Material material = Material.matchMaterial(materialString != null ? materialString : Material.AIR.getKey().toString());

                    if (material == null || material.isAir()) {
                        return;
                    }

                    ingredients.put(ingredientKey, new RecipeChoice.MaterialChoice(material));
                }

                // List of materials
                else if (ingredientsSection.isList(ingredientKeyString)) {
                    List<Material> materials = ingredientsSection.getStringList(ingredientKeyString).stream()
                            .map(Material::matchMaterial)
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();

                    if (materials.isEmpty()) {
                        return;
                    }

                    ingredients.put(ingredientKey, new RecipeChoice.MaterialChoice(materials));
                }
            });
        }

        int yield = getConfig().getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_RECIPE_YIELD, 3);
        yield = (yield <= 64) ? (yield >= 1 ? yield : 1) : 64; // Clamp between 1 and 64

        ShapedRecipe recipe = new ShapedRecipe(AlchemaConstants.RECIPE_KEY_EMPTY_VIAL, EntityEssenceData.createEmptyVial(yield)).shape(shape);
        for (Map.Entry<Character, RecipeChoice> choice : ingredients.entrySet()) {
            try {
                recipe.setIngredient(choice.getKey(), choice.getValue());
            } catch (IllegalArgumentException e) {
                return;
            }
        }

        Bukkit.addRecipe(recipe);
    }

    @NotNull
    private List<@NotNull String> getDefaultRecipePaths(String path) {
        List<@NotNull String> paths = new ArrayList<>();

        try (JarFile jar = new JarFile(getFile())) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (!name.startsWith(path + "/")) {
                    continue;
                }

                if (entry.isDirectory()) {
                    paths.addAll(getDefaultRecipePaths(name));
                    continue;
                }

                paths.add(name.substring("recipes/".length()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return paths;
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
                        this.saveDefaultDirectory(name, saveChildDirectories);
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
