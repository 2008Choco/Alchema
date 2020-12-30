package wtf.choco.alchema.util;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.api.event.CauldronIngredientAddEvent;
import wtf.choco.alchema.api.event.CauldronIngredientsDropEvent;
import wtf.choco.alchema.api.event.CauldronItemCraftEvent;
import wtf.choco.alchema.api.event.CauldronRecipeRegisterEvent;
import wtf.choco.alchema.api.event.entity.EntityDamageByCauldronEvent;
import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.crafting.CauldronIngredient;
import wtf.choco.alchema.crafting.CauldronRecipe;
import wtf.choco.alchema.crafting.CauldronRecipeRegistry;

/**
 * A utility class to more easily call the various events in Alchema.
 *
 * @author Parker Hawke - Choco
 */
public final class AlchemaEventFactory {

    private AlchemaEventFactory() { }

    /**
     * Call and return the {@link EntityDamageByCauldronEvent}.
     *
     * @param entity the damaged entity
     * @param cauldron the cauldron
     * @param damage the damage inflicted
     *
     * @return the event
     */
    public static EntityDamageByCauldronEvent callEntityDamageByCauldronEvent(@NotNull LivingEntity entity, @NotNull AlchemicalCauldron cauldron, double damage) {
        EntityDamageByCauldronEvent event = new EntityDamageByCauldronEvent(entity, cauldron, damage);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Call and return the {@link CauldronIngredientAddEvent}.
     *
     * @param cauldron the cauldron
     * @param ingredient the ingredient to be added
     * @param item the item entity
     *
     * @return the event
     */
    @NotNull
    public static CauldronIngredientAddEvent callCauldronIngredientAddEvent(@NotNull AlchemicalCauldron cauldron, @NotNull CauldronIngredient ingredient, @NotNull Item item) {
        CauldronIngredientAddEvent event = new CauldronIngredientAddEvent(cauldron, ingredient, item);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Call and return the {@link CauldronIngredientsDropEvent}.
     *
     * @param cauldron the cauldron
     * @param items the items to drop
     * @param player the player
     * @param reason the reason for the event call
     *
     * @return the event
     */
    @NotNull
    public static CauldronIngredientsDropEvent callCauldronIngredientsDropEvent(@NotNull AlchemicalCauldron cauldron, Collection<@NotNull ItemStack> items, @Nullable Player player, @NotNull CauldronIngredientsDropEvent.Reason reason) {
        CauldronIngredientsDropEvent event = new CauldronIngredientsDropEvent(cauldron, items, player, reason);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Call and return the {@link CauldronItemCraftEvent}.
     *
     * @param cauldron the cauldron
     * @param recipe the recipe
     * @param player the player
     *
     * @return the event
     */
    @NotNull
    public static CauldronItemCraftEvent callCauldronItemCraftEvent(@NotNull AlchemicalCauldron cauldron, @NotNull CauldronRecipe recipe, @Nullable Player player) {
        CauldronItemCraftEvent event = new CauldronItemCraftEvent(cauldron, recipe, player);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Call the {@link CauldronRecipeRegisterEvent}.
     *
     * @param recipeRegistry the recipe registry
     */
    public static void callCauldronRecipeRegisterEvent(@NotNull CauldronRecipeRegistry recipeRegistry) {
        CauldronRecipeRegisterEvent event = new CauldronRecipeRegisterEvent(recipeRegistry);
        Bukkit.getPluginManager().callEvent(event);
    }

}
