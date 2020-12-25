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
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.api.event.CauldronRecipeRegisterEvent;
import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.cauldron.CauldronManager;
import wtf.choco.alchema.cauldron.CauldronUpdateTask;
import wtf.choco.alchema.command.CommandAlchema;
import wtf.choco.alchema.crafting.CauldronIngredientItemStack;
import wtf.choco.alchema.crafting.CauldronIngredientMaterial;
import wtf.choco.alchema.crafting.CauldronRecipe;
import wtf.choco.alchema.crafting.CauldronRecipeRegistry;
import wtf.choco.alchema.listener.CauldronDeathMessageListener;
import wtf.choco.alchema.listener.CauldronManipulationListener;
import wtf.choco.alchema.util.AlchemaEventFactory;
import wtf.choco.alchema.util.NamespacedKeyUtil;
import wtf.choco.alchema.util.UpdateChecker;
import wtf.choco.alchema.util.UpdateChecker.UpdateReason;

public final class Alchema extends JavaPlugin {

    public static final String CHAT_PREFIX = ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Alchema | " + ChatColor.GRAY;

    private static final Gson GSON = new Gson();

    private static Alchema instance;

    private final CauldronManager cauldronManager = new CauldronManager();
    private final CauldronRecipeRegistry recipeRegistry = new CauldronRecipeRegistry();

    private File cauldronFile;
    private File recipesDirectory;

    private CauldronUpdateTask cauldronUpdateTask;

    @Override
    public void onLoad() {
        instance = this;

        this.recipeRegistry.registerIngredientType(CauldronIngredientItemStack.KEY, CauldronIngredientItemStack::new);
        this.recipeRegistry.registerIngredientType(CauldronIngredientMaterial.KEY, CauldronIngredientMaterial::new);
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.recipesDirectory = new File(getDataFolder(), "recipes");
        if (!recipesDirectory.exists()) {
            this.saveDefaultDirectory("recipes");
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
        this.loadCauldronRecipes().whenComplete((result, exception) -> {
            if (exception != null) {
                exception.printStackTrace();
                return;
            }

            if (result.getTotal() > 0) {
                this.getLogger().info("Registered " + result.getTotal() + " cauldron recipes. (" + result.getThirdParty() + " third-party)");
            }
        });

        // Register listeners
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new CauldronDeathMessageListener(this), this);
        manager.registerEvents(new CauldronManipulationListener(this), this);

        // Register commands
        this.registerCommandSafely("alchema", new CommandAlchema(this));

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
     * Asynchronously load all cauldron recipes from file, as well as any recipes from
     * third-party plugins listening to the {@link CauldronRecipeRegisterEvent}. The returned
     * {@link CompletableFuture} instance provides the load result.
     *
     * @return a completable future where the supplied value is the amount of loaded recipes
     */
    @NotNull
    public CompletableFuture<@NotNull RecipeLoadResult> loadCauldronRecipes() {
        return CompletableFuture.supplyAsync(() -> {
            int registered = 0;

            for (File recipeFile : recipesDirectory.listFiles((file, name) -> name.endsWith(".json"))) {
                String fileName = recipeFile.getName();
                fileName = fileName.substring(0, fileName.indexOf(".json"));

                if (!NamespacedKeyUtil.isValidKey(fileName)) {
                    this.getLogger().warning("Invalid recipe file name, \"" + recipeFile.getName() + "\". Must be alphanumerical, lowercased and separated by underscores.");
                    continue;
                }

                NamespacedKey key = new NamespacedKey(this, fileName);

                try (BufferedReader reader = Files.newReader(recipeFile, Charset.defaultCharset())) {
                    JsonObject recipeObject = GSON.fromJson(reader, JsonObject.class);
                    CauldronRecipe recipe = CauldronRecipe.fromJson(key, recipeObject, recipeRegistry);

                    this.recipeRegistry.registerCauldronRecipe(recipe);
                    registered++;
                } catch (IOException | JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            return registered;
        })
        .thenCompose(registered -> {
            CompletableFuture<@NotNull RecipeLoadResult> registryEventFuture = new CompletableFuture<>();

            /*
             * Events need to be called synchronously.
             *
             * This also forces the event to be called AFTER all plugins have finished enabling and registering their listeners.
             * runTask() is run on the next server tick which is done post-plugin enable.
             */
            Bukkit.getScheduler().runTask(this, () -> {
                AlchemaEventFactory.callCauldronRecipeRegisterEvent(recipeRegistry);
                registryEventFuture.complete(
                    new RecipeLoadResult() {

                        @Override
                        public int getNative() {
                            return registered;
                        }

                        @Override
                        public int getThirdParty() {
                            return recipeRegistry.getRecipes().size() - registered;
                        }

                    }
                );
            });

            return registryEventFuture;
        });
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

    private void saveDefaultDirectory(@NotNull String directory) {
        Preconditions.checkArgument(directory != null, "directory cannot be null");

        try (JarFile jar = new JarFile(getFile())) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (!name.startsWith(directory + "/") || entry.isDirectory()) {
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


    /**
     * Represents a result of the {@link Alchema#loadCauldronRecipes()} asynchronous
     * recipe loading.
     */
    public interface RecipeLoadResult {

        /**
         * Get the amount of recipes loaded natively from Alchema's file system.
         *
         * @return the amount of native recipes loaded
         */
        public int getNative();

        /**
         * Get the amount of recipes loaded from third-party plugins.
         *
         * @return the amount of third-party recipes loaded
         */
        public int getThirdParty();

        /**
         * Get the amount of total recipes loaded.
         *
         * @return the total recipe count
         */
        public default int getTotal() {
            return getNative() + getThirdParty();
        }

    }

}
