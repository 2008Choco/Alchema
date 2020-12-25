package wtf.choco.alchema.api.event;

import com.google.common.base.Preconditions;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.crafting.CauldronRecipe;

/**
 * Called when an {@link AlchemicalCauldron} has successfully prepared a crafting recipe.
 *
 * @author Parker Hawke - Choco
 */
public class CauldronItemCraftEvent extends BlockEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;
    private ItemStack result;

    private final AlchemicalCauldron cauldron;
    private final CauldronRecipe recipe;
    private final Player player;

    /**
     * Construct a new {@link CauldronItemCraftEvent} with a result.
     *
     * @param cauldron the cauldron that caused the craft
     * @param recipe the recipe that was crafted
     * @param player the player that caused this craft event. Can be null
     * @param result the result of the recipe. May not necessarily be equal to
     * {@link CauldronRecipe#getResult()}
     */
    public CauldronItemCraftEvent(@NotNull AlchemicalCauldron cauldron, @NotNull CauldronRecipe recipe, @Nullable Player player, @Nullable ItemStack result) {
        super(cauldron.getCauldronBlock());

        Preconditions.checkArgument(cauldron != null, "cauldron must not be null");
        Preconditions.checkArgument(recipe != null, "recipe must not be null");

        this.cauldron = cauldron;
        this.recipe = recipe;
        this.player = player;
        this.result = result;
    }

    /**
     * Construct a new {@link CauldronItemCraftEvent}.
     *
     * @param cauldron the cauldron that caused the craft
     * @param recipe the recipe that was crafted
     * @param player the player that caused this craft event. Can be null
     */
    public CauldronItemCraftEvent(@NotNull AlchemicalCauldron cauldron, @NotNull CauldronRecipe recipe, @Nullable Player player) {
        this(cauldron, recipe, player, recipe.getResult());
    }

    /**
     * Get the {@link AlchemicalCauldron} involved in this event.
     *
     * @return the cauldron
     */
    @NotNull
    public AlchemicalCauldron getCauldron() {
        return cauldron;
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
     */
    public void setResult(@Nullable ItemStack result) {
        this.result = result;
    }

    /**
     * Get the resulting {@link ItemStack} for this cauldron crafting process.
     *
     * @return the result
     */
    @Nullable
    public ItemStack getResult() {
        return result != null ? result.clone() : null;
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