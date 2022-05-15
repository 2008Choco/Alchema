package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.commons.util.NamespacedKeyUtil;

/**
 * Represents a recipe that may be crafted in an {@link AlchemicalCauldron}.
 *
 * @author Parker Hawke - Choco
 */
public interface CauldronRecipe {

    /**
     * Get the key of this recipe.
     *
     * @return the key
     */
    @NotNull
    public NamespacedKey getKey();

    /**
     * Get the result of this recipe.
     *
     * @return the result
     *
     * @deprecated use {@link #getRecipeResult()} instead
     */
    @NotNull
    @Deprecated(since = "1.3.0", forRemoval = true)
    public ItemStack getResult();

    /**
     * Get the result of this recipe.
     *
     * @return the result
     */
    @NotNull
    public RecipeResult getRecipeResult();

    /**
     * Get the name of this recipe.
     *
     * @return the recipe name
     */
    @NotNull
    public Optional<@NotNull String> getName();

    /**
     * Get the description of this recipe.
     *
     * @return the recipe description
     */
    @NotNull
    public Optional<@NotNull String> getDescription();

    /**
     * Get the comment for this recipe if one is set.
     *
     * @return the comment
     */
    @NotNull
    public Optional<@NotNull String> getComment();

    /**
     * Get the permission node required by a player in order to craft this recipe
     * in addition to the generic crafting permission.
     *
     * @return the crafting permission
     */
    @NotNull
    public String getCraftingPermission();

    /**
     * Get the experience yielded from this recipe.
     *
     * @return the experience
     */
    public int getExperience();

    /**
     * Check whether this recipe contains the specified ingredient. Quantity is not
     * accounted for in this check.
     *
     * @param ingredient the ingredient to check
     *
     * @return true if the ingredient is present
     */
    public boolean hasIngredient(@NotNull CauldronIngredient ingredient);

    /**
     * Get an unmodifiable set of all required ingredients.
     *
     * @return the required ingredients
     */
    @NotNull
    public List<@NotNull CauldronIngredient> getIngredients();

    /**
     * Get the numerical complexity of this recipe.
     * <p>
     * Complexity is determined on the sum of all ingredients'
     * {@link CauldronIngredient#getComplexity()} values.
     *
     * @return the complexity. Higher numbers are more complex. Will always be {@literal >=} 0
     */
    public int getComplexity();

    /**
     * Get the expected yield (i.e. quantity of result) that may be produced such that the provided
     * ingredients will not be over-consumed.
     *
     * @param availableIngredients the ingredients available in the cauldron.
     *
     * @return the recipe yield
     */
    public int getYieldFromIngredients(@NotNull List<@NotNull CauldronIngredient> availableIngredients);

    /**
     * Create a new CauldronRecipe builder instance.
     *
     * @param key the recipe key
     * @param result the result of the cauldron recipe
     *
     * @return the builder instance
     */
    @NotNull
    public static CauldronRecipe.Builder builder(@NotNull NamespacedKey key, @NotNull ItemStack result) {
        return builder(key, new RecipeResultItemStack(result));
    }

    /**
     * Create a new CauldronRecipe builder instance.
     *
     * @param key the recipe key
     * @param result the result of the cauldron recipe
     *
     * @return the builder instance
     */
    @NotNull
    public static CauldronRecipe.Builder builder(@NotNull NamespacedKey key, @NotNull RecipeResult result) {
        Preconditions.checkArgument(key != null, "key must not be null");
        Preconditions.checkArgument(result != null, "result must not be null");

        return new CauldronRecipe.Builder(key, result);
    }

    /**
     * Read the contents of the provided {@link JsonObject} into a new {@link CauldronRecipe}
     * instance.
     *
     * @param key the key of the recipe to create
     * @param object the object from which to read
     * @param recipeRegistry the recipe registry
     *
     * @return the cauldron recipe
     */
    @NotNull
    public static CauldronRecipe fromJson(@NotNull NamespacedKey key, @NotNull JsonObject object, @NotNull CauldronRecipeRegistry recipeRegistry) {
        Preconditions.checkArgument(key != null, "key cannot be null");
        Preconditions.checkArgument(object != null, "object cannot be null");

        if (!object.has("result")) {
            throw new JsonParseException("Missing result object");
        }

        if (!object.has("ingredients") || !object.get("ingredients").isJsonArray()) {
            throw new JsonParseException("Missing ingredients array");
        }

        // Parse the result
        JsonObject resultObject = object.getAsJsonObject("result");
        NamespacedKey resultTypeKey = (resultObject.has("type") ? NamespacedKeyUtil.fromString(resultObject.get("type").getAsString(), Alchema.getInstance()) : RecipeResultItemStack.KEY);
        if (resultTypeKey == null) {
            throw new JsonParseException("Invalid namespaced key \"" + resultObject.get("type").getAsString() + "\". Expected format is \"alchema:example\"");
        }

        RecipeResult result = recipeRegistry.parseResultType(resultTypeKey, resultObject);

        if (result == null) {
            throw new JsonParseException("Could not find result type with id \"" + resultTypeKey + "\"");
        }

        // Parse ingredients
        JsonArray ingredientsArray = object.getAsJsonArray("ingredients");
        if (ingredientsArray.size() < 2) {
            throw new JsonParseException("ingredients array must contain at least two ingredients");
        }

        List<@NotNull CauldronIngredient> ingredients = new ArrayList<>(ingredientsArray.size());

        for (int i = 0; i < ingredientsArray.size(); i++) {
            JsonElement ingredientElement = ingredientsArray.get(i);
            if (!ingredientElement.isJsonObject()) {
                throw new JsonParseException("ingredient at index " + i + " was a " + ingredientElement.getClass().getSimpleName() + ". Expected object");
            }

            JsonObject ingredientObject = ingredientElement.getAsJsonObject();
            if (!ingredientObject.has("type")) {
                throw new JsonParseException("ingredient at index " + i + " does not have an ingredient type");
            }

            NamespacedKey typeKey = NamespacedKeyUtil.fromString(ingredientObject.get("type").getAsString(), Alchema.getInstance());
            if (typeKey == null) {
                throw new JsonParseException("Invalid namespaced key \"" + typeKey + "\". Expected format is \"alchema:example\"");
            }

            CauldronIngredient ingredient = recipeRegistry.parseIngredientType(typeKey, ingredientObject);
            if (ingredient == null) {
                throw new JsonParseException("Could not find ingredient type with id \"" + typeKey + "\"");
            }

            ingredients.add(ingredient);
        }

        int experience = object.has("experience") ? object.get("experience").getAsInt() : 0;
        String name = object.has("name") ? object.get("name").getAsString() : null;
        String description = object.has("description") ? object.get("description").getAsString() : null;
        String comment = object.has("comment") ? object.get("comment").getAsString() : null;

        return new SimpleCauldronRecipe(key, result, name, description, comment, experience, ingredients);
    }

    /**
     * A builder for immutable {@link CauldronRecipe} instances.
     */
    public static final class Builder {

        private String name = null, description = null, comment = null;
        private int experience = 0;

        private final List<@NotNull CauldronIngredient> ingredients = new ArrayList<>();

        private final NamespacedKey key;
        private final RecipeResult result;

        private Builder(NamespacedKey key, RecipeResult result) {
            Preconditions.checkArgument(key != null, "key must not be null");
            Preconditions.checkArgument(result != null, "result must not be null");

            this.key = key;
            this.result = result;
        }

        /**
         * Add an ingredient to the recipe.
         *
         * @param ingredient the ingredient to add
         *
         * @return this instance. Allows for chained method calls
         */
        @NotNull
        public CauldronRecipe.Builder addIngredient(@NotNull CauldronIngredient ingredient) {
            Preconditions.checkArgument(ingredient != null, "ingredient must not be null");

            this.ingredients.add(ingredient);
            return this;
        }

        /**
         * Set the name for this recipe.
         *
         * @param name the name to set
         *
         * @return this instance. Allows for chained method calls
         */
        @NotNull
        public CauldronRecipe.Builder name(@NotNull String name) {
            Preconditions.checkArgument(!StringUtils.isBlank(name), "name must not be null or empty");

            this.name = name;
            return this;
        }

        /**
         * Set the description for this recipe.
         *
         * @param description the name to set
         *
         * @return this instance. Allows for chained method calls
         */
        @NotNull
        public CauldronRecipe.Builder description(@NotNull String description) {
            Preconditions.checkArgument(!StringUtils.isBlank(description), "description must not be null or empty");

            this.description = description;
            return this;
        }

        /**
         * Set the comment for this recipe. Comments are purely aesthetic.
         *
         * @param comment the comment to set
         *
         * @return this instance. Allows for chained method calls
         *
         * @deprecated poor naming. See {@link #comment(String)}. Will be removed in a future release
         */
        @NotNull
        @Deprecated(since = "1.2.0", forRemoval = true)
        public CauldronRecipe.Builder setComment(@Nullable String comment) {
            this.comment = comment; // Not calling #comment() because of the change in nullability handling
            return this;
        }

        /**
         * Set the comment for this recipe. Comments are purely aesthetic.
         *
         * @param comment the comment to set
         *
         * @return this instance. Allows for chained method calls
         */
        @NotNull
        public CauldronRecipe.Builder comment(@NotNull String comment) {
            Preconditions.checkArgument(!StringUtils.isBlank(name), "comemnt must not be null or empty");

            this.comment = comment;
            return this;
        }

        /**
         * Set the experience to be yielded from crafting this recipe. The experience set is
         * directly proportional to {@link ExperienceOrb#setExperience(int)}.
         *
         * @param experience the experience to set. Must be positive or 0
         *
         * @return this instance. Allows for chained method calls
         *
         * @deprecated poor naming. See {@link #experience(int)}. Will be removed in a future release
         */
        @NotNull
        @Deprecated(since = "1.2.0", forRemoval = true)
        public CauldronRecipe.Builder setExperience(int experience) {
            return experience(experience);
        }

        /**
         * Set the experience to be yielded from crafting this recipe. The experience set is
         * directly proportional to {@link ExperienceOrb#setExperience(int)}.
         *
         * @param experience the experience to set. Must be positive or 0
         *
         * @return this instance. Allows for chained method calls
         */
        @NotNull
        public CauldronRecipe.Builder experience(int experience) {
            Preconditions.checkArgument(experience >= 0, "experience must be positive or 0");

            this.experience = experience;
            return this;
        }

        /**
         * Build a new {@link CauldronRecipe} instance.
         *
         * @return the cauldron recipe
         */
        @NotNull
        public CauldronRecipe build() {
            return new SimpleCauldronRecipe(key, result, name, description, comment, experience, ingredients);
        }

    }

}
