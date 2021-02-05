package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.util.ItemUtil;
import wtf.choco.alchema.util.NamespacedKeyUtil;

/**
 * Represents a recipe that may be crafted in an {@link AlchemicalCauldron}.
 *
 * @author Parker Hawke - Choco
 */
public class CauldronRecipe {

    private Optional<@NotNull String> comment = Optional.empty();

    private final NamespacedKey key;
    private final ItemStack result;

    private final List<@NotNull CauldronIngredient> ingredients = new ArrayList<>();

    /**
     * Construct a new CauldronRecipe with a unique ID, {@link ItemStack} result, and a set of
     * required ingredients
     *
     * @param key the unique recipe key
     * @param result the result of the recipe
     * @param ingredients the set of ingredients
     */
    public CauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull List<@NotNull CauldronIngredient> ingredients) {
        this(key, result);
        this.ingredients.addAll(ingredients);
    }

    /**
     * Construct a new CauldronRecipe with a unique ID, {@link ItemStack} result, and a set of
     * required ingredients
     *
     * @param key the unique recipe key
     * @param result the result of the recipe
     * @param ingredients the set of ingredients
     */
    public CauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull CauldronIngredient... ingredients) {
        this(key, result);

        Preconditions.checkState(ingredients.length > 0, "Recipes contain at least one ingredient (excluding the catalyst)");
        for (CauldronIngredient ingredient : ingredients) {
            this.ingredients.add(ingredient);
        }
    }

    /**
     * Construct a new CauldronRecipe with a unique ID, {@link ItemStack} resultand a single ingredient.
     *
     * @param key the unique recipe key
     * @param result the result of the recipe
     * @param ingredient the recipe ingredient
     */
    public CauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull CauldronIngredient ingredient) {
        this(key, result);
        this.ingredients.add(ingredient);
    }

    // Convenience constructor
    private CauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result) {
        Preconditions.checkArgument(key != null, "key must not be null");
        Preconditions.checkArgument(result != null, "result must not be null");

        this.key = key;
        this.result = result;
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
     */
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

        Optional<@NotNull String> comment = Optional.empty();
        if (object.has("comment")) {
            comment = Optional.ofNullable(object.get("comment").getAsString());
        }

        CauldronRecipe recipe = new CauldronRecipe(key, result, ingredients);
        recipe.setComment(comment);
        return recipe;
    }

}
