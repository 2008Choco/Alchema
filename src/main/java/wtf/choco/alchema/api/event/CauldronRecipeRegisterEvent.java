package wtf.choco.alchema.api.event;

import com.google.common.base.Preconditions;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.crafting.CauldronRecipe;
import wtf.choco.alchema.crafting.CauldronRecipeRegistry;

/**
 * Called on startup and on reloads of {@link Alchema} to register any third-party
 * recipes.
 * <p>
 * While {@link CauldronRecipeRegistry#registerCauldronRecipe(CauldronRecipe)}
 * may be used directly, it will not be retained when the plugin's reload command
 * is issued by a command executor, therefore it is recommended that registration be
 * done in an event listener for this event instead.
 * <p>
 * By the time this event has been called, all recipes provided by Alchema will have
 * been registered already.
 *
 * @author Parker Hawke - Choco
 */
public class CauldronRecipeRegisterEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CauldronRecipeRegistry recipeRegistry;

    /**
     * Construct a new {@link CauldronRecipeRegisterEvent}.
     *
     * @param recipeRegistry the recipe registry instance
     */
    public CauldronRecipeRegisterEvent(@NotNull CauldronRecipeRegistry recipeRegistry) {
        Preconditions.checkArgument(recipeRegistry != null, "recipeRegistry must not be null");

        this.recipeRegistry = recipeRegistry;
    }

    /**
     * Get the {@link CauldronRecipeRegistry} instance.
     *
     * @return the recipe registry
     */
    @NotNull
    public CauldronRecipeRegistry getRecipeRegistry() {
        return recipeRegistry;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
