package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.cauldron.AlchemicalCauldron;

/**
 * Represents a recipe that may be crafted in an {@link AlchemicalCauldron}.
 *
 * @author Parker Hawke - Choco
 */
public class SimpleCauldronRecipe implements CauldronRecipe {

    private final NamespacedKey key;
    private final ItemStack result;

    private final String name, description, comment;
    private final int experience;

    private final List<@NotNull CauldronIngredient> ingredients = new ArrayList<>();

    SimpleCauldronRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @Nullable String name, @Nullable String description, @Nullable String comment, int experience, @NotNull List<@NotNull CauldronIngredient> ingredients) {
        Preconditions.checkArgument(key != null, "key must not be null");
        Preconditions.checkArgument(result != null, "result must not be null");

        this.key = key;
        this.result = result;
        this.experience = experience;

        this.name = name;
        this.description = description;
        this.comment = comment;

        this.ingredients.addAll(ingredients);
    }

    /**
     * Get the key of this recipe.
     *
     * @return the key
     */
    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Get the result of this recipe.
     *
     * @return the result
     */
    @NotNull
    @Override
    public ItemStack getResult() {
        return result.clone();
    }

    /**
     * Get the name of this recipe.
     *
     * @return the recipe name
     */
    @NotNull
    @Override
    public Optional<@NotNull String> getName() {
        return Optional.ofNullable(name);
    }

    /**
     * Get the description of this recipe.
     *
     * @return the recipe description
     */
    @NotNull
    @Override
    public Optional<@NotNull String> getDescription() {
        return Optional.ofNullable(description);
    }

    /**
     * Get the comment for this recipe if one is set.
     *
     * @return the comment
     */
    @NotNull
    @Override
    public Optional<@NotNull String> getComment() {
        return Optional.ofNullable(comment);
    }

    /**
     * Get the experience yielded from this recipe.
     *
     * @return the experience
     */
    @Override
    public int getExperience() {
        return experience;
    }

    /**
     * Check whether this recipe contains the specified ingredient. Quantity is not
     * accounted for in this check.
     *
     * @param ingredient the ingredient to check
     *
     * @return true if the ingredient is present
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
        return Objects.hash(key, experience, ingredients, name, description, comment, result);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SimpleCauldronRecipe)) {
            return false;
        }

        SimpleCauldronRecipe other = (SimpleCauldronRecipe) obj;
        return experience == other.experience && Objects.equals(key, other.key) && Objects.equals(comment, other.comment)
                && Objects.equals(name, other.name) && Objects.equals(description, other.description)
                && Objects.equals(result, other.result) && Objects.equals(ingredients, other.ingredients);
    }

    @Override
    public String toString() {
        return String.format("SimpleCauldronRecipe[key=%s, comment=%s]", key, comment);
    }

}
