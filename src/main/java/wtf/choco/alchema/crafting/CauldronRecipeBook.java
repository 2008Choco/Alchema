package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.commons.util.ItemBuilder;
import wtf.choco.commons.util.NamespacedKeyUtil;

/**
 * A collection of utility methods pertaining to the cauldron recipe book.
 *
 * @author Parker Hawke - Choco
 */
public final class CauldronRecipeBook implements Listener {

    private static final String COMPONENT_NAVIGATION_COMMAND = "/alchema:navigate_recipe_book";

    private static final String BOOK_AUTHOR = "Ancient Alchemists";
    private static final String BOOK_TITLE = "Alchemical Recipe Book";
    private static final Generation BOOK_GENERATION = Generation.TATTERED;

    private static final int MAXIMUM_PAGE_LINES = 14;
    private static final int MAXIMUM_LINE_LENGTH = 256 / MAXIMUM_PAGE_LINES; // It's based on character length but this is the best possible estimate

    private static boolean initialized = false;
    private static Alchema plugin;

    private CauldronRecipeBook() { }

    /**
     * Register listeners and declare fields required for recipe book functionality.
     * <p>
     * This method is not meant for public invocation.
     *
     * @param plugin the plugin instance
     */
    public static void initialize(@NotNull Alchema plugin) {
        Preconditions.checkState(!initialized, "The recipe book has already been initialized");
        Preconditions.checkArgument(plugin != null, "plugin must not be null");

        Bukkit.getPluginManager().registerEvents(new CauldronRecipeBook(), plugin);
        CauldronRecipeBook.plugin = plugin;

        initialized = true;
    }

    /**
     * Create a new recipe book item.
     *
     * @param recipeRegistry an instance of the registry from which recipes should
     * be pulled
     * @param generateRecipes whether or not recipes should be generated
     *
     * @return the recipe book
     */
    @NotNull
    public static ItemStack createRecipeBook(@NotNull CauldronRecipeRegistry recipeRegistry, boolean generateRecipes) {
        Preconditions.checkArgument(generateRecipes || recipeRegistry != null, "recipeRegistry must not be null");

        ItemBuilder itemBuilder = ItemBuilder.of(Material.WRITTEN_BOOK)
                .lore(
                    "", // Separator
                    ChatColor.GRAY + "A mystical tome with information about the",
                    ChatColor.GRAY + "curious world of Alchema and its recipes."
                );

        itemBuilder.specific(BookMeta.class, meta -> {
            // Base book meta
            meta.setAuthor(BOOK_AUTHOR);
            meta.setGeneration(BOOK_GENERATION);
            meta.setTitle(BOOK_TITLE);

            if (!generateRecipes) {
                return;
            }

            // Sort recipes by first letter
            Multimap<@NotNull Character, @NotNull CauldronRecipe> recipesByCharacter = getSortedCauldronRecipes(recipeRegistry);

            int line = 0;
            ComponentBuilder pageBuilder = new ComponentBuilder();

            // Generate pages
            for (Character character : recipesByCharacter.keySet()) {
                Collection<@NotNull CauldronRecipe> recipes = recipesByCharacter.get(character);

                int recipeCount = recipes.size();
                String recipeCountString = recipeCount > 1 ? "There are " + recipeCount + " recipes." : "There is " + recipeCount + " recipe.";

                pageBuilder.append(" - ").append(String.valueOf(character))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMPONENT_NAVIGATION_COMMAND + " selectindex " + character))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to show all recipes\nstarting with " + character + ". \n\n" + recipeCountString)));
                pageBuilder.append("\n", FormatRetention.NONE);

                if (++line >= MAXIMUM_PAGE_LINES) {
                    meta.spigot().addPage(pageBuilder.create());
                    pageBuilder = new ComponentBuilder();
                    line = 0;
                }
            }

            if (!pageBuilder.getParts().isEmpty()) {
                meta.spigot().addPage(pageBuilder.create());
            }
        });

        return itemBuilder.build();
    }

    /**
     * Create a new recipe book item.
     *
     * @param recipeRegistry an instance of the registry from which recipes should
     * be pulled
     *
     * @return the recipe book
     */
    @NotNull
    public static ItemStack createRecipeBook(@NotNull CauldronRecipeRegistry recipeRegistry) {
        return createRecipeBook(recipeRegistry, true);
    }

    /**
     * Create a new recipe book item with no recipe pages.
     *
     * @return the empty recipe book
     */
    @NotNull
    public static ItemStack createRecipeBook() {
        return createRecipeBook(Alchema.getInstance().getRecipeRegistry(), false);
    }

    @NotNull
    public static ItemStack createRecipeBookForCharacter(@NotNull CauldronRecipeRegistry recipeRegistry, char character) {
        Preconditions.checkArgument(recipeRegistry != null, "recipeRegistry must not be null");
        Preconditions.checkArgument(character >= 'A' && character <= 'Z', "character must be between A and Z (uppercase)");

        ItemBuilder itemBuilder = ItemBuilder.of(Material.WRITTEN_BOOK);
        itemBuilder.specific(BookMeta.class, meta -> {
            // Base book meta
            meta.setAuthor(BOOK_AUTHOR);
            meta.setGeneration(BOOK_GENERATION);
            meta.setTitle(BOOK_TITLE);

            // Sort recipes by first letter
            Collection<@NotNull CauldronRecipe> recipes = getSortedCauldronRecipes(recipeRegistry).get(character);

            int line = 0;
            ComponentBuilder pageBuilder = new ComponentBuilder();

            // Generate pages
            for (CauldronRecipe recipe : recipes) {
                String recipeName = getNameOrBestAttempt(recipe);

                /*
                 * Some lines might overflow onto multiple lines... give it a best attempt to calculate that and account for additional lines
                 * This isn't an exact science. Length depends entirely on character width so some lines with more characters may not exceed
                 * our calculated MAXIMUM_LINE_LENGTH. This results in a few pages that don't reach the bottom, but it's better than pages that
                 * overflow below the page and are clickable but not visible. Have to compromise here. This is vanilla we're talking about.
                 */
                int lineLength = ("- " + recipeName).length();
                if (lineLength > MAXIMUM_LINE_LENGTH) {
                    line += (lineLength / MAXIMUM_LINE_LENGTH);

                    if (++line >= MAXIMUM_PAGE_LINES) {
                        meta.spigot().addPage(pageBuilder.create());
                        pageBuilder = new ComponentBuilder();
                        line = 0;
                    }
                }

                pageBuilder.append("- ").append(recipeName)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMPONENT_NAVIGATION_COMMAND + " viewrecipe " + recipe.getKey()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to view information about this recipe.")));
                pageBuilder.append("\n", FormatRetention.NONE);

                if (++line >= MAXIMUM_PAGE_LINES) {
                    meta.spigot().addPage(pageBuilder.create());
                    pageBuilder = new ComponentBuilder();
                    line = 0;
                }
            }

            if (!pageBuilder.getParts().isEmpty()) {
                meta.spigot().addPage(pageBuilder.create());
            }
        });

        return itemBuilder.build();
    }

    /**
     * Create the {@link BaseComponent} array for a page in the recipe book for
     * the given {@link CauldronRecipe}.
     *
     * @param recipe the recipe
     *
     * @return the page components
     */
    @NotNull
    public static BaseComponent[] createRecipePage(@NotNull CauldronRecipe recipe) {
        Preconditions.checkArgument(recipe != null, "recipe must not be null");

        ComponentBuilder builder = new ComponentBuilder();

        // Clickable back button
        builder.append("[Go Back]").bold(true)
            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMPONENT_NAVIGATION_COMMAND + " viewindex 0"))
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Return to the recipe book index.")));

        builder.append("\n\n", FormatRetention.NONE);

        // Name
        builder.append("Name: ").bold(true);
        builder.append(getNameOrBestAttempt(recipe), FormatRetention.NONE);

        // Description
        recipe.getDescription().ifPresent(description -> builder.append("\n").append(description).italic(true));

        // Ingredients
        builder.append("\n\n", FormatRetention.NONE);
        builder.append("Ingredients: ").bold(true);
        builder.append("\n", FormatRetention.NONE);
        recipe.getIngredients().forEach(ingredient -> builder.append("- ").append(ingredient.describe()).append("\n"));

        // Comment
        recipe.getComment().ifPresent(comment -> builder.append("\n").append("(i) ").bold(true).append(comment, FormatRetention.NONE).italic(true));

        return builder.create();
    }

    @NotNull
    private static String getNameOrBestAttempt(@NotNull CauldronRecipe recipe) {
        Optional<@NotNull String> name = recipe.getName();
        if (name.isPresent()) {
            return name.get();
        }

        String recipeKey = recipe.getKey().getKey();
        int lastSlash = recipeKey.lastIndexOf('/');

        if (lastSlash > -1) {
            recipeKey = recipeKey.substring(lastSlash + 1);
        }

        return StringUtils.capitalize(recipeKey.replace('_', ' '));
    }

    @NotNull
    private static Multimap<@NotNull Character, @NotNull CauldronRecipe> getSortedCauldronRecipes(@NotNull CauldronRecipeRegistry recipeRegistry) {
        return recipeRegistry.getRecipes().stream().collect(
                ArrayListMultimap::create,
                (map, recipe) -> {
                    String recipeName = getNameOrBestAttempt(recipe);
                    map.get(recipeName.charAt(0)).add(recipe);
                },
                ArrayListMultimap::putAll
            );
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onNavigateRecipeBook(PlayerCommandPreprocessEvent event) {
        if (!initialized) {
            return;
        }

        String[] fullCommand = event.getMessage().split("\\s+");
        String command = fullCommand[0];
        if (!command.equals(COMPONENT_NAVIGATION_COMMAND)) {
            return;
        }

        event.setCancelled(true); // At this point, we want to handle this command entirely

        Player player = event.getPlayer();
        String[] args = (fullCommand.length > 1) ? Arrays.copyOfRange(fullCommand, 1, fullCommand.length) : new String[0];

        if (args.length >= 2) {
            if (args[0].equals("viewrecipe")) {
                NamespacedKey recipeKey = NamespacedKeyUtil.fromString(args[1]);
                if (recipeKey == null) {
                    player.sendMessage("Could not find information about this recipe.");
                    return;
                }

                CauldronRecipe recipe = plugin.getRecipeRegistry().getCauldronRecipe(recipeKey);
                if (recipe == null) {
                    player.sendMessage("Could not find information about this recipe.");
                    return;
                }

                ItemStack recipeBookPageItem = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta meta = (BookMeta) recipeBookPageItem.getItemMeta();
                assert meta != null; // Impossible

                meta.setAuthor(BOOK_AUTHOR);
                meta.setGeneration(BOOK_GENERATION);
                meta.setTitle(BOOK_TITLE);

                meta.spigot().addPage(createRecipePage(recipe));
                recipeBookPageItem.setItemMeta(meta);

                player.openBook(recipeBookPageItem);
            }

            else if (args[0].equals("selectindex")) {
                char character = args[1].charAt(0);

                ItemStack bookItem = createRecipeBookForCharacter(plugin.getRecipeRegistry(), character);

                player.openBook(bookItem);
            }

            else if (args[0].equals("viewindex")) {
                // Find the recipe book in the hand
                ItemStack recipeBook = player.getInventory().getItemInMainHand();
                if (recipeBook.getType().isAir()) {
                    recipeBook = player.getInventory().getItemInOffHand();

                    if (recipeBook.getType().isAir()) {
                        player.sendMessage("Something went wrong...");
                        return;
                    }
                }

                // Open it
                player.openBook(recipeBook);
            }
        }
    }

}
