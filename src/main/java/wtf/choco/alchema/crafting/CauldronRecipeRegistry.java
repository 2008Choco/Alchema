package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.cauldron.AlchemicalCauldron;

/**
 * Represents a registry in which recipes and recipe types may be registered.
 *
 * @author Parker Hawke - Choco
 */
public class CauldronRecipeRegistry {

    private final Map<@NotNull NamespacedKey, CauldronRecipe> recipes = new HashMap<>();
    private final Map<@NotNull NamespacedKey, Function<@NotNull JsonObject, @NotNull ? extends CauldronIngredient>> ingredientTypes = new HashMap<>();

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
     * Get the {@link CauldronRecipe} that applies given a set of ingredients. If no recipe can consume
     * the ingredients and catalyst, null is returned. If more than one recipe is valid, the first is
     * selected.
     *
     * @param ingredients the available ingredients
     *
     * @return the cauldron recipe that applies. null if none
     */
    @Nullable
    public CauldronRecipe getApplicableRecipe(@NotNull List<@NotNull CauldronIngredient> ingredients) {
        return recipes.values().stream()
                .filter(recipe -> recipe.getYieldFromIngredients(ingredients) >= 1)
                .findFirst().orElse(null);
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
     * Register a new type of {@link CauldronIngredient}. This registration should be done during
     * the plugin's load phase (i.e. {@link JavaPlugin#onLoad()}).
     *
     * @param key the ingredient key. Should match that of {@link CauldronIngredient#getKey()}
     * @param ingredientProvider the ingredient provider
     */
    public void registerIngredientType(@NotNull NamespacedKey key, Function<@NotNull JsonObject, @NotNull ? extends CauldronIngredient> ingredientProvider) {
        Preconditions.checkArgument(key != null, "key must not be null");
        Preconditions.checkArgument(ingredientProvider != null, "ingredientProvider must not be null");

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

}
