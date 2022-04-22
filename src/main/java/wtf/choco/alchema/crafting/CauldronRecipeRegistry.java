package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.api.event.CauldronRecipeRegisterEvent;
import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.util.AlchemaEventFactory;
import wtf.choco.commons.util.NamespacedKeyUtil;

/**
 * Represents a registry in which recipes and recipe types may be registered.
 *
 * @author Parker Hawke - Choco
 */
public class CauldronRecipeRegistry {

    private static final Gson GSON = new Gson();

    private boolean acceptingRegistrations = true;

    private final Map<@NotNull NamespacedKey, @NotNull CauldronRecipe> recipes = new HashMap<>();
    private final Map<@NotNull NamespacedKey, Function<@NotNull JsonObject, @NotNull ? extends CauldronIngredient>> ingredientTypes = new HashMap<>();
    private final Map<@NotNull NamespacedKey, Function<@NotNull JsonObject, @NotNull ? extends RecipeResult>> resultTypes = new HashMap<>();

    /**
     * Register a {@link CauldronRecipe} to be used by any {@link AlchemicalCauldron}.
     *
     * @param recipe the recipe to register
     */
    public void registerCauldronRecipe(@NotNull CauldronRecipe recipe) {
        Preconditions.checkNotNull(recipe, "Cannot register null recipe");
        this.recipes.put(recipe.getKey(), recipe);
    }

    /**
     * Unregister a {@link CauldronRecipe}. Upon unregistration, cauldrons will not longer be able
     * to execute its recipe.
     *
     * @param recipe the recipe to unregister
     */
    public void unregisterCauldronRecipe(@NotNull CauldronRecipe recipe) {
        this.recipes.remove(recipe.getKey());
    }

    /**
     * Unregister a {@link CauldronRecipe} associated with the provided ID. Upon unregistration,
     * cauldrons will no longer be able to execute its recipe.
     *
     * @param key the key of the recipe to unregister
     *
     * @return the unregistered recipe. null if none
     */
    @Nullable
    public CauldronRecipe unregisterCauldronRecipe(@NotNull NamespacedKey key) {
        return recipes.remove(key);
    }

    /**
     * Get the {@link CauldronRecipe} associated with the provided key.
     *
     * @param key the recipe key to get
     *
     * @return the recipe. null if none registered with the given key
     */
    @Nullable
    public CauldronRecipe getCauldronRecipe(@NotNull NamespacedKey key) {
        return recipes.get(key);
    }

    /**
     * Get the {@link CauldronRecipe} that applies given a set of ingredients. If no recipe can consume
     * the ingredients, null is returned. If more than one recipe is valid, the first is selected.
     *
     * @param ingredients the available ingredients
     * @param mostComplex whether or not to find the most complex applicable recipe if more than one is
     * applicable. Note that if this value is true, all recipes will be iterated over and will result in
     * a fixed operation time of O(n) rather than O(n) worst case.
     *
     * @return the cauldron recipe that applies. null if none
     *
     * @see #getApplicableRecipes(List) to get a list of all applicable recipes
     */
    @Nullable
    public CauldronRecipe getApplicableRecipe(@NotNull List<@NotNull CauldronIngredient> ingredients, boolean mostComplex) {
        CauldronRecipe result = null;

        for (CauldronRecipe recipe : recipes.values()) {
            if (recipe.getYieldFromIngredients(ingredients) == 0) {
                continue;
            }

            if (!mostComplex) {
                return recipe;
            }

            if (result == null || recipe.getComplexity() > result.getComplexity()) {
                result = recipe;
            }
        }

        return result;
    }

    /**
     * Get the {@link CauldronRecipe} that applies given a set of ingredients. If no recipe can consume
     * the ingredients, null is returned. If more than one recipe is valid, the first is selected.
     * <p>
     * The complexity of the recipe is not taken into consideration. The recipe returned by this method
     * is not guaranteed.
     *
     * @param ingredients the available ingredients
     *
     * @return the cauldron recipe that applies. null if none
     *
     * @see #getApplicableRecipes(List) to get a list of all applicable recipes
     */
    @Nullable
    public CauldronRecipe getApplicableRecipe(@NotNull List<@NotNull CauldronIngredient> ingredients) {
        return getApplicableRecipe(ingredients, false);
    }

    /**
     * Get a list of {@link CauldronRecipe CauldronRecipes} that apply given a set of ingredients sorted by
     * their complexity (0th index = most complex, last index = least complex). If no recipe can consume the
     * ingredients, an empty list is returned.
     *
     * @param ingredients the available ingredients
     *
     * @return all applicable cauldron recipes. empty list if none
     */
    @NotNull
    public List<@NotNull CauldronRecipe> getApplicableRecipes(@NotNull List<@NotNull CauldronIngredient> ingredients) {
        List<@NotNull CauldronRecipe> applicable = new ArrayList<>();

        this.recipes.values().forEach(recipe -> {
            if (recipe.getYieldFromIngredients(ingredients) == 0) {
                return;
            }

            applicable.add(recipe);
        });

        applicable.sort(Comparator.comparingInt(CauldronRecipe::getComplexity));
        return applicable;
    }

    /**
     * Get a collection of all registered recipes. Changes made to the returned collection will be
     * reflected internally to this instance.
     *
     * @return the collection of registered recipes
     */
    @NotNull
    public Collection<@NotNull CauldronRecipe> getRecipes() {
        return recipes.values(); // Intentionally mutable
    }

    /**
     * Clear all recipes in the manager.
     */
    public void clearRecipes() {
        this.recipes.clear();
    }

    /**
     * Declare that this registry is no longer accepting registrations.
     * <p>
     * This method should be called internally by Alchema to ensure that ingredient types are not
     * registered after all plugins have been loaded. It is expected that all calls to
     * {@link #registerIngredientType(NamespacedKey, Function)} be made in {@link JavaPlugin#onLoad()}.
     */
    @Internal
    public void stopAcceptingRegistrations() {
        this.acceptingRegistrations = false;
    }

    /**
     * Register a new type of {@link CauldronIngredient}. This registration should be done during
     * the plugin's load phase (i.e. {@link JavaPlugin#onLoad()}).
     * <p>
     * <strong>NOTE:</strong> This method should be called in {@link JavaPlugin#onLoad()}. Registrations
     * will no longer be accepted in {@link JavaPlugin#onEnable()} and an IllegalStateException will be
     * thrown.
     *
     * @param key the ingredient key. Should match that of {@link CauldronIngredient#getKey()}
     * @param ingredientProvider the ingredient provider
     */
    public void registerIngredientType(@NotNull NamespacedKey key, @NotNull Function<@NotNull JsonObject, @NotNull ? extends CauldronIngredient> ingredientProvider) {
        Preconditions.checkArgument(key != null, "key must not be null");
        Preconditions.checkArgument(ingredientProvider != null, "ingredientProvider must not be null");

        if (!acceptingRegistrations) {
            throw new IllegalStateException("Attempted to register ingredient type (" + key + ") while the registry is no longer accepting registrations. Ingredient registration should be done onLoad()");
        }

        this.ingredientTypes.put(key, ingredientProvider);
    }

    /**
     * Parse a {@link CauldronIngredient} with the ingredient type matching the provided
     * {@link NamespacedKey} from a {@link JsonObject}.
     *
     * @param key the key of the ingredient type to parse
     * @param object the object from which to parse the ingredient
     *
     * @return the parsed ingredient. null if invalid
     */
    @Nullable
    public CauldronIngredient parseIngredientType(@NotNull NamespacedKey key, @NotNull JsonObject object) {
        Preconditions.checkArgument(key != null, "key must not be null");
        Preconditions.checkArgument(object != null, "object must not be null");

        Function<@NotNull JsonObject, @NotNull ? extends CauldronIngredient> ingredientProvider = ingredientTypes.get(key);
        if (ingredientProvider == null) {
            return null;
        }

        return ingredientProvider.apply(object);
    }

    /**
     * Clear all registered ingredient types.
     */
    public void clearIngredientTypes() {
        this.ingredientTypes.clear();
    }

    /**
     * Register a new type of {@link RecipeResult}. This registration should be done during the plugin's
     * load phase (i.e. {@link JavaPlugin#onLoad()}).
     * <p>
     * <strong>NOTE:</strong> This method should be called in {@link JavaPlugin#onLoad()}. Registrations
     * will no longer be accepted in {@link JavaPlugin#onEnable()} and an IllegalStateException will be
     * thrown.
     *
     * @param key the ingredient key. Should match that of {@link RecipeResult#getKey()}
     * @param resultProvider the result provider
     */
    public void registerResultType(@NotNull NamespacedKey key, @NotNull Function<@NotNull JsonObject, @NotNull ? extends RecipeResult> resultProvider) {
        Preconditions.checkArgument(key != null, "key must not be null");
        Preconditions.checkArgument(resultProvider != null, "ingredientProvider must not be null");

        if (!acceptingRegistrations) {
            throw new IllegalStateException("Attempted to register result type (" + key + ") while the registry is no longer accepting registrations. Result registration should be done onLoad()");
        }

        this.resultTypes.put(key, resultProvider);
    }

    /**
     * Parse a {@link RecipeResult} with the result type matching the provided {@link NamespacedKey}
     * from a {@link JsonObject}.
     *
     * @param key the key of the result type to parse
     * @param object the object from which to parse the ingredient
     *
     * @return the parsed result. null if invalid
     */
    @Nullable
    public RecipeResult parseResultType(@NotNull NamespacedKey key, @NotNull JsonObject object) {
        Preconditions.checkArgument(key != null, "key must not be null");
        Preconditions.checkArgument(object != null, "object must not be null");

        Function<@NotNull JsonObject, @NotNull ? extends RecipeResult> resultProvider = resultTypes.get(key);
        if (resultProvider == null) {
            return null;
        }

        return resultProvider.apply(object);
    }

    /**
     * Clear all registered result types.
     */
    public void clearResultTypes() {
        this.resultTypes.clear();
    }

    /**
     * Asynchronously load all cauldron recipes from Alchema's file system, as well as any
     * recipes from third-party plugins listening to the {@link CauldronRecipeRegisterEvent}.
     * The returned {@link CompletableFuture} instance provides the load result.
     *
     * @param plugin the instance of Alchema (for logging purposes)
     * @param recipesDirectory the directory from which to load recipes
     *
     * @return a CompletableFuture where the supplied value is the amount of loaded recipes
     */
    @NotNull
    public CompletableFuture<@NotNull RecipeLoadResult> loadCauldronRecipes(@NotNull Alchema plugin, @NotNull File recipesDirectory) {
        long now = System.currentTimeMillis();

        return CompletableFuture.supplyAsync(() -> loadCauldronRecipesFromDirectory(plugin, new StandardRecipeLoadResult(), recipesDirectory, recipesDirectory))
        .thenCompose(result -> {
            CompletableFuture<@NotNull RecipeLoadResult> registryEventFuture = new CompletableFuture<>();

            /*
             * Events need to be called synchronously.
             *
             * This also forces the event to be called AFTER all plugins have finished enabling and registering their listeners.
             * runTask() is run on the next server tick which is done post-plugin enable.
             */
            Bukkit.getScheduler().runTask(plugin, () -> {
                AlchemaEventFactory.callCauldronRecipeRegisterEvent(this);

                long timeToComplete = System.currentTimeMillis() - now;

                result.setThirdParty(getRecipes().size() - result.getNative());
                result.setTimeToComplete(timeToComplete);

                registryEventFuture.complete(result);
            });

            return registryEventFuture;
        });
    }

    private StandardRecipeLoadResult loadCauldronRecipesFromDirectory(@NotNull Alchema plugin, @NotNull StandardRecipeLoadResult result, @NotNull File recipesDirectory, File subdirectory) throws JsonSyntaxException {
        for (File recipeFile : subdirectory.listFiles(file -> file.isDirectory() || file.getName().endsWith(".json"))) {
            if (recipeFile.isDirectory()) {
                this.loadCauldronRecipesFromDirectory(plugin, result, recipesDirectory, recipeFile);
                continue;
            }

            String fileName = recipeFile.getName();
            fileName = fileName.substring(0, fileName.indexOf(".json"));

            String joinedRecipeKey = null;
            if (recipesDirectory.equals(subdirectory)) {
                joinedRecipeKey = fileName; // The root directory is too short to substring itself + 1
            } else {
                /*
                 * Converts file paths to valid keys. Example:
                 *
                 * Given: File#getAbsolutePath() (C:\Users\foo\bar\baz)
                 * Parsed: bar/baz + / + fileName
                 */
                joinedRecipeKey = subdirectory.getAbsolutePath().substring(recipesDirectory.getAbsolutePath().length() + 1).replace('\\', '/') + "/" + fileName;
            }

            if (!NamespacedKeyUtil.isValidKey(joinedRecipeKey)) {
                plugin.getLogger().warning("Invalid recipe file name, \"" + recipeFile.getName() + "\". Must be alphanumerical, lowercased and separated by underscores.");
                continue;
            }

            NamespacedKey key = new NamespacedKey(plugin, joinedRecipeKey);

            try (BufferedReader reader = Files.newReader(recipeFile, Charset.defaultCharset())) {
                JsonObject recipeObject = GSON.fromJson(reader, JsonObject.class);
                CauldronRecipe recipe = CauldronRecipe.fromJson(key, recipeObject, this);

                this.registerCauldronRecipe(recipe);
                result.setNative(result.getNative() + 1);
            } catch (Exception e) {
                result.addFailureInfo(new RecipeLoadFailureReport(key, e));
            }
        }

        return result;
    }


    private class StandardRecipeLoadResult implements RecipeLoadResult {

        private int nativelyRegistered, thirdPartyRegistered;
        private long timeToComplete;

        private final List<@NotNull RecipeLoadFailureReport> failures = new ArrayList<>();

        StandardRecipeLoadResult() { }

        private void setNative(int nativelyRegistered) {
            this.nativelyRegistered = nativelyRegistered;
        }

        @Override
        public int getNative() {
            return nativelyRegistered;
        }

        private void setThirdParty(int thirdPartyRegistered) {
            this.thirdPartyRegistered = thirdPartyRegistered;
        }

        @Override
        public int getThirdParty() {
            return thirdPartyRegistered;
        }

        private void setTimeToComplete(long timeToComplete) {
            this.timeToComplete = timeToComplete;
        }

        @Override
        public long getTimeToComplete() {
            return timeToComplete;
        }

        private void addFailureInfo(@NotNull RecipeLoadFailureReport report) {
            this.failures.add(report);
        }

        @NotNull
        @Override
        public List<@NotNull RecipeLoadFailureReport> getFailures() {
            return Collections.unmodifiableList(failures);
        }

    }

}
