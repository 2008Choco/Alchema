package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.util.ItemUtil;
import wtf.choco.commons.util.NamespacedKeyUtil;

/**
 * Represents a recipe that may be crafted in an {@link AlchemicalCauldron}.
 *
 * @author Parker Hawke - Choco
 */
public class CauldronRecipe {

    private Optional<@NotNull String> comment = Optional.empty();

    private final NamespacedKey key;
    private final ItemStack result;
    private final int experience;

    private final String name, description;

    private final List<@NotNull CauldronIngredient> ingredients = new ArrayList<>();

    /**
     * Construct a new CauldronRecipe with a unique ID, {@link ItemStack} result, and a set of
     * required ingredients
     *
     * @param key the unique recipe key
     * @param result the result of the recipe
     * @param name the recipe name
     * @param description the recipe description
     * @param experience the experience to reward the player
     * @param ingredients the set of ingredients
     *
     * @deprecated this class will not be constructible in 1.2.0. See {@link #builder(NamespacedKey, ItemStack)}
     */
    @Deprecated
    public CauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @Nullable String name, @Nullable String description, int experience, @NotNull List<@NotNull CauldronIngredient> ingredients) {
        this(key, result, name, description, experience);
        this.ingredients.addAll(ingredients);
    }

    /**
     * Construct a new CauldronRecipe with a unique ID, {@link ItemStack} result, and a set of
     * required ingredients
     *
     * @param key the unique recipe key
     * @param result the result of the recipe
     * @param experience the experience to reward the player
     * @param ingredients the set of ingredients
     *
     * @deprecated this class will not be constructible in 1.2.0. See {@link #builder(NamespacedKey, ItemStack)}
     */
    @Deprecated
    public CauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, int experience, @NotNull List<@NotNull CauldronIngredient> ingredients) {
        this(key, result, null, null, experience);
    }

    /**
     * Construct a new CauldronRecipe with a unique ID, {@link ItemStack} result, and a set of
     * required ingredients
     *
     * @param key the unique recipe key
     * @param result the result of the recipe
     * @param experience the experience to reward the player
     * @param ingredients the set of ingredients
     *
     * @deprecated this class will not be constructible in 1.2.0. See {@link #builder(NamespacedKey, ItemStack)}
     */
    @Deprecated
    public CauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, int experience, @NotNull CauldronIngredient... ingredients) {
        this(key, result, null, null, experience);

        Preconditions.checkState(ingredients.length > 0, "Recipes contain at least one ingredient (excluding the catalyst)");
        for (CauldronIngredient ingredient : ingredients) {
            this.ingredients.add(ingredient);
        }
    }

    /**
     * Construct a new CauldronRecipe with a unique ID, {@link ItemStack} result and a single ingredient.
     *
     * @param key the unique recipe key
     * @param result the result of the recipe
     * @param experience the experience to reward the player
     * @param ingredient the recipe ingredient
     *
     * @deprecated this class will not be constructible in 1.2.0. See {@link #builder(NamespacedKey, ItemStack)}
     */
    @Deprecated
    public CauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, int experience, @NotNull CauldronIngredient ingredient) {
        this(key, result, null, null, experience);
        this.ingredients.add(ingredient);
    }

    /**
     * Construct a new CauldronRecipe with a unique ID, {@link ItemStack} result, and a set of
     * required ingredients
     *
     * @param key the unique recipe key
     * @param result the result of the recipe
     * @param ingredients the set of ingredients
     *
     * @deprecated this class will not be constructible in 1.2.0. See {@link #builder(NamespacedKey, ItemStack)}
     */
    @Deprecated
    public CauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull List<@NotNull CauldronIngredient> ingredients) {
        this(key, result, null, null, 0, ingredients);
    }

    /**
     * Construct a new CauldronRecipe with a unique ID, {@link ItemStack} result, and a set of
     * required ingredients
     *
     * @param key the unique recipe key
     * @param result the result of the recipe
     * @param ingredients the set of ingredients
     *
     * @deprecated this class will not be constructible in 1.2.0. See {@link #builder(NamespacedKey, ItemStack)}
     */
    @Deprecated
    public CauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull CauldronIngredient... ingredients) {
        this(key, result, 0, ingredients);
    }

    /**
     * Construct a new CauldronRecipe with a unique ID, {@link ItemStack} result and a single ingredient.
     *
     * @param key the unique recipe key
     * @param result the result of the recipe
     * @param ingredient the recipe ingredient
     *
     * @deprecated this class will not be constructible in 1.2.0. See {@link #builder(NamespacedKey, ItemStack)}
     */
    @Deprecated
    public CauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull CauldronIngredient ingredient) {
        this(key, result, 0, ingredient);
    }

    // Convenience constructor
    private CauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @Nullable String name, @Nullable String description, int experience) {
        Preconditions.checkArgument(key != null, "key must not be null");
        Preconditions.checkArgument(result != null, "result must not be null");

        this.key = key;
        this.result = result;
        this.experience = experience;

        this.name = name;
        this.description = description;
    }

    /**
     * Get the key of this recipe.
     *
     * @return the key
     */
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Get the result of this recipe.
     *
     * @return the result
     */
    @NotNull
    public ItemStack getResult() {
        return result.clone();
    }

    /**
     * Get the experience yielded from this recipe.
     *
     * @return the experience
     */
    public int getExperience() {
        return experience;
    }

    /**
     * Set the comment for this recipe.
     *
     * @param comment the comment to set or null
     *
     * @deprecated inconvenient parameter. See {@link #setComment(String)} instead
     */
    @Deprecated
    public void setComment(@Nullable Optional<@NotNull String> comment) {
        if (comment == null) {
            comment = Optional.empty();
        }

        this.comment = comment;
    }

    /**
     * Set the comment for this recipe.
     *
     * @param comment the comment to set or null
     *
     * @deprecated this class will not be constructable in 1.2.0. See {@link #builder(NamespacedKey, ItemStack)}
     */
    @Deprecated
    public void setComment(@Nullable String comment) {
        this.comment = Optional.<String>ofNullable(comment); // Nullability annotations are funny sometimes ;p
    }

    /**
     * Get the comment for this recipe if one is set.
     *
     * @return the comment
     */
    @NotNull
    public Optional<@NotNull String> getComment() {
        return comment;
    }

    /**
     * Get the name of this recipe.
     *
     * @return the recipe name
     */
    @NotNull
    public Optional<@NotNull String> getName() {
        return Optional.ofNullable(name);
    }

    /**
     * Get the description of this recipe.
     *
     * @return the recipe description
     */
    @NotNull
    public Optional<@NotNull String> getDescription() {
        return Optional.ofNullable(description);
    }

    /**
     * Check whether this recipe contains the specified ingredient. Quantity is not
     * accounted for in this check.
     *
     * @param ingredient the ingredient to check
     *
     * @return true if the ingredient is present
     */
    public boolean hasIngredient(@NotNull CauldronIngredient ingredient) {
        Preconditions.checkArgument(ingredient != null, "ingredient must not be null");

        for (CauldronIngredient recipeIngredient : ingredients) {
            if (recipeIngredient.isSimilar(ingredient)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get an unmodifiable set of all required ingredients.
     *
     * @return the required ingredients
     */
    @NotNull
    public List<@NotNull CauldronIngredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    /**
     * Get the numerical complexity of this recipe.
     * <p>
     * Complexity is determined on the sum of all ingredients'
     * {@link CauldronIngredient#getComplexity()} values.
     *
     * @return the complexity. Higher numbers are more complex. Will always be {@literal >=} 0
     */
    public int getComplexity() {
        int complexity = 0;

        for (CauldronIngredient ingredient : ingredients) {
            complexity += ingredient.getComplexity();
        }

        return complexity;
    }

    /**
     * Get the expected yield (i.e. quantity of result) that may be produced such that the provided
     * ingredients will not be over-consumed.
     *
     * @param availableIngredients the ingredients available in the cauldron.
     *
     * @return the recipe yield
     */
    public int getYieldFromIngredients(@NotNull List<@NotNull CauldronIngredient> availableIngredients) {
        int yield = 0;
        boolean initialFind = true;

        // This can probably be done a lot better...
        for (CauldronIngredient requiredIngredient : ingredients) {
            CauldronIngredient availableIngredient = null;

            for (CauldronIngredient localIngredient : availableIngredients) {
                if (!requiredIngredient.isSimilar(localIngredient)) {
                    continue;
                }

                int requiredCount = requiredIngredient.getAmount();
                int availableCount = localIngredient.getAmount();

                if (availableCount >= requiredCount) {
                    availableIngredient = localIngredient;
                    break;
                }
            }

            if (availableIngredient == null) {
                return 0;
            }

            // Compute yield
            int requiredCount = requiredIngredient.getAmount();
            int availableCount = availableIngredient.getAmount();
            yield = initialFind ? availableCount / requiredCount : Math.min(availableCount / requiredCount, yield);

            initialFind = false;
        }

        return yield;
    }

    @Override
    public int hashCode() {
        return Objects.hash(comment, experience, ingredients, key, result);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof CauldronRecipe)) {
            return false;
        }

        CauldronRecipe other = (CauldronRecipe) obj;
        return experience == other.experience && Objects.equals(key, other.key) && Objects.equals(comment, other.comment)
                && Objects.equals(result, other.result) && Objects.equals(ingredients, other.ingredients);
    }

    @Override
    public String toString() {
        return String.format("CauldronRecipe[key=%s, comment=%s]", key, comment);
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
    public static CauldronRecipe.Builder builder(@NotNull NamespacedKey key, @NotNull ItemStack result) {
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

        JsonArray ingredientsArray = object.getAsJsonArray("ingredients");
        if (ingredientsArray.size() < 2) {
            throw new JsonParseException("ingredients array must contain at least two ingredients");
        }

        ItemStack result = ItemUtil.deserializeItemStack(object.getAsJsonObject("result"));

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
        String comment = object.has("comment") ? object.get("comment").getAsString() : null;

        CauldronRecipe recipe = new CauldronRecipe(key, result, experience, ingredients);
        recipe.setComment(comment);
        return recipe;
    }

    /**
     * A builder for immutable {@link CauldronRecipe} instances.
     */
    public static final class Builder {

        private String comment = null;
        private int experience = 0;

        private String name = null, description = null;

        private final List<@NotNull CauldronIngredient> ingredients = new ArrayList<>();

        private final NamespacedKey key;
        private final ItemStack result;

        private Builder(NamespacedKey key, ItemStack result) {
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
         * Set the experience to be yielded from crafting this recipe. The experience set is
         * directly proportional to {@link ExperienceOrb#setExperience(int)}.
         *
         * @param experience the experience to set. Must be positive or 0
         *
         * @return this instance. Allows for chained method calls
         */
        @NotNull
        public CauldronRecipe.Builder setExperience(int experience) {
            Preconditions.checkArgument(experience >= 0, "experience must be positive or 0");

            this.experience = experience;
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
        public CauldronRecipe.Builder setComment(@Nullable String comment) {
            this.comment = comment;
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
            Preconditions.checkArgument(name != null && !StringUtils.isBlank(name), "name must not be null or empty");

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
            Preconditions.checkArgument(description != null && !StringUtils.isBlank(description), "description must not be null or empty");

            this.description = description;
            return this;
        }

        /**
         * Build a new {@link CauldronRecipe} instance.
         *
         * @return the cauldron recipe
         */
        @NotNull
        public CauldronRecipe build() {
            // TODO: In future versions, construct a new instance of the implementation rather than using this deprecated constructor
            CauldronRecipe recipe = new CauldronRecipe(key, result, name, description, experience, ingredients);
            recipe.setComment(comment);
            return recipe;
        }

    }

}
