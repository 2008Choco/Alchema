package wtf.choco.alchema.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.crafting.CauldronRecipeRegistry;
import wtf.choco.alchema.util.AlchemaConstants;

/**
 * A utility class handling statistics of custom Metrics charts.
 *
 * @author Parker Hawke
 */
@Internal
public final class MetricsHelper {

    /*
     * For the sake of avoiding invasion of privacy, we're only going to send to bStats the
     * namespaced keys whose keys we are aware of. If we are not aware of it, we're going to
     * label them as "Third-Party Ingredient Type", or "Third-Party Result Type".
     */
    private static final Set<NamespacedKey> INGREDIENT_KEY_WHITELIST = new HashSet<>();
    private static final Set<NamespacedKey> RESULT_KEY_WHITELIST = new HashSet<>();

    private static int successfulCrafts = 0;

    private MetricsHelper() { }

    /**
     * Register custom Metrics charts.
     *
     * @param metrics the metrics instance
     * @param plugin the plugin instance
     */
    public static void registerCustomCharts(@NotNull Metrics metrics, @NotNull Alchema plugin) {
        metrics.addCustomChart(new SimplePie("loaded_cauldrons", () -> String.valueOf(plugin.getCauldronManager().getCauldrons().size())));
        metrics.addCustomChart(new SimplePie("cauldron_recipes", () -> String.valueOf(plugin.getRecipeRegistry().getRecipes().size())));
        metrics.addCustomChart(new SingleLineChart("cauldron_crafts", MetricsHelper::getSuccessfulCraftsAndReset));
        metrics.addCustomChart(new AdvancedPie("cauldron_recipe_ingredient_types", () -> calculateCauldronRecipeIngredientTypes(plugin.getRecipeRegistry(), shouldAnonymizeRecipeTypes(plugin))));
        metrics.addCustomChart(new AdvancedPie("cauldron_recipe_result_types", () -> calculateCauldronRecipeResultTypes(plugin.getRecipeRegistry(), shouldAnonymizeRecipeTypes(plugin))));
    }

    /**
     * Add one new successful craft to the count.
     */
    public static void addSuccessfulCraft() {
        successfulCrafts++;
    }

    /**
     * Add a known ingredient type.
     * <p>
     * If an ingredient type is known, it will be sent to bStats Metrics. Alchema will only
     * ever add the keys of native types or integrations to avoid invasion of privacy.
     *
     * @param key the key to add to the whitelist
     */
    public static void addKnownIngredientKey(@NotNull NamespacedKey key) {
        INGREDIENT_KEY_WHITELIST.add(key);
    }

    /**
     * Add a known result type.
     * <p>
     * If a result type is known, it will be sent to bStats Metrics. Alchema will only
     * ever add the keys of native types or integrations to avoid invasion of privacy.
     *
     * @param key the key to add to the whitelist
     */
    public static void addKnownResultKey(@NotNull NamespacedKey key) {
        RESULT_KEY_WHITELIST.add(key);
    }

    /**
     * Clear the key whitelists.
     */
    public static void clearKeyWhitelists() {
        INGREDIENT_KEY_WHITELIST.clear();
        RESULT_KEY_WHITELIST.clear();
    }

    private static int getSuccessfulCraftsAndReset() {
        int result = successfulCrafts;
        successfulCrafts = 0;
        return result;
    }

    @NotNull
    private static Map<String, Integer> calculateCauldronRecipeIngredientTypes(@NotNull CauldronRecipeRegistry recipeRegistry, boolean anonymize) {
        Map<String, Integer> ingredientTypes = new HashMap<>();

        recipeRegistry.getRecipes().forEach(recipe -> recipe.getIngredients().forEach(ingredient -> {
            NamespacedKey key = ingredient.getKey();
            String keyString = (!anonymize || INGREDIENT_KEY_WHITELIST.contains(key)) ? key.toString() : "Third-Party Ingredient Type";
            ingredientTypes.merge(keyString, 1, Integer::sum);
        }));

        return ingredientTypes;
    }

    @NotNull
    private static Map<String, Integer> calculateCauldronRecipeResultTypes(@NotNull CauldronRecipeRegistry recipeRegistry, boolean anonymize) {
        Map<String, Integer> resultTypes = new HashMap<>();

        recipeRegistry.getRecipes().forEach(recipe -> {
            NamespacedKey key = recipe.getRecipeResult().getKey();
            String keyString = (!anonymize || RESULT_KEY_WHITELIST.contains(key)) ? key.toString() : "Third-Party Result Type";
            resultTypes.merge(keyString, 1, Integer::sum);
        });

        return resultTypes;
    }

    private static boolean shouldAnonymizeRecipeTypes(Alchema plugin) {
        return plugin.getConfig().getBoolean(AlchemaConstants.CONFIG_METRICS_ANONYMOUS_CUSTOM_RECIPE_TYPES, false);
    }

}
