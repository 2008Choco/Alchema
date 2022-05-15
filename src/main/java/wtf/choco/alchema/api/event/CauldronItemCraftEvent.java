package wtf.choco.alchema.api.event;

import com.google.common.base.Preconditions;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.crafting.CauldronRecipe;
import wtf.choco.alchema.crafting.RecipeResult;
import wtf.choco.alchema.crafting.RecipeResultItemStack;

/**
 * Called when an {@link AlchemicalCauldron} has successfully prepared a crafting recipe.
 *
 * @author Parker Hawke - Choco
 */
public class CauldronItemCraftEvent extends CauldronEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;
    private RecipeResult result;
    private int experience;

    private final CauldronRecipe recipe;
    private final Player player;

    /**
     * Construct a new {@link CauldronItemCraftEvent} with a result.
     *
     * @param cauldron the cauldron that caused the craft
     * @param recipe the recipe that was crafted
     * @param player the player that caused this craft event. Can be null
     * @param result the result of the recipe. May not necessarily be equal to
     * {@link CauldronRecipe#getRecipeResult()}
     * @param experience the experience yielded from the recipe
     */
    public CauldronItemCraftEvent(@NotNull AlchemicalCauldron cauldron, @NotNull CauldronRecipe recipe, @Nullable Player player, @Nullable RecipeResult result, int experience) {
        super(cauldron);

        Preconditions.checkArgument(recipe != null, "recipe must not be null");

        this.recipe = recipe;
        this.player = player;

        this.result = result;
        this.experience = experience;
    }

    /**
     * Construct a new {@link CauldronItemCraftEvent}.
     *
     * @param cauldron the cauldron that caused the craft
     * @param recipe the recipe that was crafted
     * @param player the player that caused this craft event. Can be null
     */
    public CauldronItemCraftEvent(@NotNull AlchemicalCauldron cauldron, @NotNull CauldronRecipe recipe, @Nullable Player player) {
        this(cauldron, recipe, player, recipe.getRecipeResult(), recipe.getExperience());
    }

    /**
     * Get the recipe that was prepared for this event.
     *
     * @return the recipe
     */
    @NotNull
    public CauldronRecipe getRecipe() {
        return recipe;
    }

    /**
     * Get the player that (most likely) caused this event (if any).
     *
     * @return the player that crafted. null if none
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Set the resulting {@link ItemStack} of the cauldron crafting process. Null if none.
     *
     * @param result the result to set
     *
     * @deprecated use {@link #setRecipeResult(RecipeResult)} instead
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    public void setResult(@Nullable ItemStack result) {
        this.result = (result != null) ? new RecipeResultItemStack(result) : null;
    }

    /**
     * Get the resulting {@link ItemStack} for this cauldron crafting process.
     *
     * @return the result
     *
     * @deprecated use {@link #getRecipeResult()} instead
     */
    @Nullable
    @Deprecated(since = "1.3.0", forRemoval = true)
    public ItemStack getResult() {
        return result != null ? result.asItemStack() : null;
    }

    /**
     * Set the resulting {@link RecipeResult} of the cauldron crafting process. Null if none.
     *
     * @param result the result to set
     */
    public void setRecipeResult(@Nullable RecipeResult result) {
        this.result = result;
    }

    /**
     * Get the resulting {@link RecipeResult} for this cauldron crafting process.
     *
     * @return the result
     */
    @Nullable
    public RecipeResult getRecipeResult() {
        return result;
    }

    /**
     * Set the experience yielded from this craft.
     *
     * @param experience the experience to set. Must be 0 or positive
     */
    public void setExperience(int experience) {
        Preconditions.checkArgument(experience >= 0, "experience must not be negative");
        this.experience = experience;
    }

    /**
     * Get the experience yielded from this craft.
     *
     * @return the experience
     */
    public int getExperience() {
        return experience;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Get the HandlerList instance for this event.
     *
     * @return the handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
