package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a set of information regarding a recipe that failed to load from
 * Alchema's file system.
 *
 * @author Parker Hawke - Choco
 */
public final class RecipeLoadFailureReport {

    private final NamespacedKey recipeKey;
    private final Throwable exception;
    private final String reason;

    /**
     * Construct a new {@link RecipeLoadFailureReport}.
     *
     * @param recipeKey the key of the recipe that failed to load
     * @param exception the exception that was thrown
     * @param reason a custom reason for the failure report
     */
    public RecipeLoadFailureReport(@NotNull NamespacedKey recipeKey, @NotNull Throwable exception, @Nullable String reason) {
        Preconditions.checkArgument(recipeKey != null, "recipeKey must not be null");
        Preconditions.checkArgument(exception != null, "exception must not be null");

        this.recipeKey = recipeKey;
        this.exception = exception;
        this.reason = reason;
    }

    /**
     * Construct a new {@link RecipeLoadFailureReport}.
     *
     * @param recipeKey the key of the recipe that failed to load
     * @param exception the exception that was thrown
     */
    public RecipeLoadFailureReport(@NotNull NamespacedKey recipeKey, @NotNull Throwable exception) {
        this(recipeKey, exception, exception.getLocalizedMessage());
    }

    /**
     * Get the {@link NamespacedKey} of the recipe that failed to load.
     *
     * @return the recipe key
     */
    @NotNull
    public NamespacedKey getRecipeKey() {
        return recipeKey;
    }

    /**
     * Get the exception that was thrown to cause this failure.
     *
     * @return the exception
     */
    @NotNull
    public Throwable getException() {
        return exception;
    }

    /**
     * Get the reason message for this failure report.
     *
     * @return the reason
     */
    @Nullable
    public String getReason() {
        return reason;
    }

}
