package wtf.choco.alchema.command;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.api.event.CauldronRecipeRegisterEvent;
import wtf.choco.alchema.cauldron.CauldronUpdateHandler;
import wtf.choco.alchema.crafting.RecipeLoadFailureReport;
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.commons.integration.PluginIntegration;
import wtf.choco.commons.util.UpdateChecker;
import wtf.choco.commons.util.UpdateChecker.UpdateReason;
import wtf.choco.commons.util.UpdateChecker.UpdateResult;

public final class CommandAlchema implements TabExecutor {

    private static final List<String> RELOAD_ARGS = Arrays.asList("verbose");
    private static final List<String> SAVE_FLAG_ARGS = Arrays.asList("-f");

    private static final Map<String, String> BASE_ARGS = new HashMap<>();
    static {
        BASE_ARGS.put("version", null);
        BASE_ARGS.put("reload", AlchemaConstants.PERMISSION_COMMAND_RELOAD);
        BASE_ARGS.put("integrations", AlchemaConstants.PERMISSION_COMMAND_INTEGRATIONS);
        BASE_ARGS.put("saverecipe", AlchemaConstants.PERMISSION_COMMAND_SAVERECIPE);
    }

    private final Alchema plugin;
    private final List<String> defaultRecipePaths;

    public CommandAlchema(@NotNull Alchema plugin) {
        this.plugin = plugin;
        this.defaultRecipePaths = plugin.getDefaultRecipePaths();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Alchema.CHAT_PREFIX + "Insufficient arguments. " + ChatColor.YELLOW + "/" + label + " <" + String.join(" | ", getBaseArgsFor(sender)) + ">");
            return true;
        }

        if (args[0].equalsIgnoreCase("version")) {
            String versionSuffix = "";

            UpdateResult updateResult = UpdateChecker.isInitialized() ? UpdateChecker.get().getLastResult() : null;
            if (updateResult != null && sender.hasPermission(AlchemaConstants.PERMISSION_UPDATE_NOTIFY)) {
                StringBuilder versionSuffixBuilder = new StringBuilder(" ").append(ChatColor.WHITE).append('(');

                UpdateReason reason = updateResult.getReason();
                if (updateResult.requiresUpdate()) {
                    versionSuffixBuilder.append(ChatColor.YELLOW).append("update available: ").append(ChatColor.GREEN).append(updateResult.getNewestVersion());
                }
                else if (reason == UpdateReason.UNRELEASED_VERSION) {
                    versionSuffixBuilder.append(ChatColor.AQUA).append("dev build");
                }
                else if (reason == UpdateReason.COULD_NOT_CONNECT || reason == UpdateReason.INVALID_JSON || reason == UpdateReason.UNAUTHORIZED_QUERY || reason == UpdateReason.UNKNOWN_ERROR) {
                    versionSuffixBuilder.append(ChatColor.RED).append("failed to check");
                }
                else {
                    versionSuffixBuilder.append(ChatColor.GREEN).append("latest");
                }

                versionSuffixBuilder.append(ChatColor.WHITE).append(')');
                versionSuffix = versionSuffixBuilder.toString();
            }

            sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------------");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Version: " + ChatColor.GRAY + plugin.getDescription().getVersion() + versionSuffix);
            sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Developer / Maintainer: " + ChatColor.GRAY + "Choco " + ChatColor.YELLOW + "( https://choco.wtf/ )");
            sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Development Page: " + ChatColor.GRAY + "https://www.spigotmc.org/resources/87078/");
            sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Report Bugs To: " + ChatColor.GRAY + "https://github.com/2008Choco/Alchema/issues/");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------------");
        }

        else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(AlchemaConstants.PERMISSION_COMMAND_RELOAD)) {
                sender.sendMessage(Alchema.CHAT_PREFIX + "You have insufficient permissions to run this command.");
                return true;
            }

            boolean verbose = args.length >= 2 && args[1].equalsIgnoreCase("verbose");
            boolean isPlayer = sender instanceof Player;

            this.plugin.getRecipeRegistry().clearRecipes();
            this.plugin.reloadConfig();
            this.plugin.parseAndRegisterVialRecipe();
            this.plugin.refreshEntityBlacklists();
            CauldronUpdateHandler.get().markAsDirty();

            this.plugin.getRecipeRegistry().loadCauldronRecipes(plugin, plugin.getRecipesDirectory()).whenComplete((result, exception) -> {
                if (exception != null) {
                    sender.sendMessage(Alchema.CHAT_PREFIX + ChatColor.RED + "Something went wrong while loading recipes... check the console and report any errors to the developer of " + plugin.getName() + ".");
                    exception.printStackTrace();
                    return;
                }

                sender.sendMessage(Alchema.CHAT_PREFIX + "Loaded " + ChatColor.YELLOW + "(" + result.getTotal() + ") " + ChatColor.GRAY + "cauldron recipes" + (result.getNative() != result.getTotal()
                    ? ChatColor.YELLOW + ". (" + result.getNative() + ") " + ChatColor.GRAY + "internal recipes and " + ChatColor.YELLOW + "(" + result.getThirdParty() + ") " + ChatColor.GRAY + "third-party recipes (other plugins)."
                    : ".")
                        + " Took " + ChatColor.AQUA + result.getTimeToComplete() + "ms" + ChatColor.GRAY + ".");

                List<RecipeLoadFailureReport> failures = result.getFailures();
                if (!failures.isEmpty()) {
                    String errorMessage = ChatColor.RED.toString() + ChatColor.BOLD + "(!) " + ChatColor.RED + "Failed to load " + ChatColor.YELLOW + "(" + failures.size() + ") " + ChatColor.RED + "recipes.";

                    if (isPlayer) {
                        Player player = (Player) sender;
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 0.05F);

                        if (!verbose) {
                            errorMessage += " See the console for errors";

                            if (sender.hasPermission(AlchemaConstants.PERMISSION_COMMAND_RELOAD_VERBOSE)) {
                                errorMessage += " or use " + ChatColor.YELLOW + "/" + label + " " + args[0] + " verbose " + ChatColor.RED + "for more information";
                            }

                            errorMessage += ".";
                        }
                    }

                    sender.sendMessage(errorMessage);
                }

                failures.forEach(failureReport -> {
                    this.plugin.getLogger().warning("Failed to load recipe " + failureReport.getRecipeKey() + ". Reason: " + failureReport.getReason());

                    if (verbose && isPlayer) {
                        sender.sendMessage(" - " + ChatColor.YELLOW + failureReport.getRecipeKey() + ": " + ChatColor.WHITE + failureReport.getReason());
                    }
                });
            });

            sender.sendMessage(Alchema.CHAT_PREFIX + ChatColor.GREEN + "Successfully reloaded the configuration file.");
        }

        else if (args[0].equalsIgnoreCase("integrations")) {
            if (!sender.hasPermission(AlchemaConstants.PERMISSION_COMMAND_INTEGRATIONS)) {
                sender.sendMessage(Alchema.CHAT_PREFIX + "You have insufficient permissions to run this command.");
                return true;
            }

            List<Plugin> integrations = Arrays.stream(CauldronRecipeRegisterEvent.getHandlerList().getRegisteredListeners())
                .map(RegisteredListener::getPlugin)
                .distinct()
                .filter(plugin -> plugin != this.plugin) // Remove Alchema from the list... just in case
                .collect(Collectors.toList());
            Collection<? extends PluginIntegration> nativeIntegrations = plugin.getIntegrationHandler().getIntegrations();

            if (integrations.isEmpty() && nativeIntegrations.isEmpty()) {
                sender.sendMessage(Alchema.CHAT_PREFIX + "No plugins are currently integrating with Alchema.");
                return true;
            }

            int integrationCount = integrations.size() + nativeIntegrations.size();
            sender.sendMessage(Alchema.CHAT_PREFIX + "Currently integrating with " + ChatColor.YELLOW + "(" + integrationCount + ") plugin" + (integrationCount > 1 ? "s" : "") + ChatColor.GRAY + ":");

            this.displayListOfIntegrations(sender, integrations, false);
            this.displayListOfIntegrations(sender, nativeIntegrations, PluginIntegration::getIntegratedPlugin, true);
        }

        else if (args[0].equalsIgnoreCase("saverecipe")) {
            if (!sender.hasPermission(AlchemaConstants.PERMISSION_COMMAND_SAVERECIPE)) {
                sender.sendMessage(Alchema.CHAT_PREFIX + "You have insufficient permissions to run this command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(Alchema.CHAT_PREFIX + "Which recipe would you like to save?");
                return true;
            }

            boolean force = args.length >= 3 && args[2].equalsIgnoreCase("-f");

            if (args[1].endsWith(".json")) {
                if (!defaultRecipePaths.contains(args[1])) {
                    sender.sendMessage(Alchema.CHAT_PREFIX + "Unrecognized default recipe with path " + ChatColor.YELLOW + args[1] + ChatColor.GRAY + ". Did you spell it right?");
                    return true;
                }

                String recipePath = "recipes/" + args[1];
                File recipeFile = new File(plugin.getDataFolder(), recipePath);
                if (recipeFile.exists() && !force) {
                    sender.sendMessage(Alchema.CHAT_PREFIX + "A recipe file already exists at " + ChatColor.YELLOW + recipePath + ChatColor.GRAY + ".");
                    return true;
                }

                this.plugin.saveResource(recipePath, force);
                sender.sendMessage(Alchema.CHAT_PREFIX + "Successfully saved the default recipe at path " + ChatColor.YELLOW + recipePath + ChatColor.GRAY + ". You must " + ChatColor.AQUA + "/" + label + " reload " + ChatColor.GRAY + "in order for changes to apply.");
            }
            else if (args[1].endsWith("/*")) {
                List<String> applicableRecipePaths = new ArrayList<>(defaultRecipePaths.size());

                String recipePathDirectory = args[1].substring(0, args[1].length() - 1);
                this.defaultRecipePaths.forEach(recipePath -> {
                    if (recipePath.startsWith(recipePathDirectory)) {
                        applicableRecipePaths.add("recipes/" + recipePath);
                    }
                });

                if (applicableRecipePaths.isEmpty()) {
                    sender.sendMessage(Alchema.CHAT_PREFIX + "The path located at " + ChatColor.YELLOW + "recipes/" + recipePathDirectory + ChatColor.GRAY + " contains no recipes.");
                    return true;
                }

                int existingRecipes = 0;
                for (String recipePath : applicableRecipePaths) {
                    File recipeFile = new File(plugin.getDataFolder(), recipePath);
                    if (recipeFile.exists() && !force) {
                        existingRecipes++;
                        continue;
                    }

                    this.plugin.saveResource(recipePath, force);
                }

                int loaded = applicableRecipePaths.size() - existingRecipes;
                if (loaded > 0) {
                    sender.sendMessage(Alchema.CHAT_PREFIX + "Successfully saved " + ChatColor.YELLOW + "(" + loaded + ") " + ChatColor.GRAY + "default recipes" + (existingRecipes >= 1 && !force ? " (could not save " + ChatColor.RED + existingRecipes + ChatColor.GRAY + ")" : "") + ". You must " + ChatColor.AQUA + "/" + label + " reload " + ChatColor.GRAY + "in order for changes to apply.");
                } else {
                    sender.sendMessage(Alchema.CHAT_PREFIX + "Could not save any default recipes in the path " + ChatColor.YELLOW + "recipes/" + recipePathDirectory + ChatColor.GRAY + ". All files exist.");
                }
            }
            else {
                sender.sendMessage(Alchema.CHAT_PREFIX + "Invalid recipe path. Must end with " + ChatColor.YELLOW + ".json " + ChatColor.GRAY + "or " + ChatColor.YELLOW + "/* " + ChatColor.GRAY + ".");
            }
        }

        else {
            sender.sendMessage(Alchema.CHAT_PREFIX + "Unknown command argument, " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + ".");
        }

        return true;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], getBaseArgsFor(sender), new ArrayList<>());
        }

        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission(AlchemaConstants.PERMISSION_COMMAND_RELOAD_VERBOSE)) {
                return StringUtil.copyPartialMatches(args[1], RELOAD_ARGS, new ArrayList<>());
            }
            else if (args[0].equalsIgnoreCase("saverecipe") && sender.hasPermission(AlchemaConstants.PERMISSION_COMMAND_SAVERECIPE)) {
                List<String> suggestions = new ArrayList<>();

                this.defaultRecipePaths.forEach(recipePath -> {
                    if (recipePath.contains(args[1])) {
                        suggestions.add(recipePath);
                    }
                });

                if (args[1].endsWith("/")) {
                    suggestions.add(args[1] + "*");
                }

                return suggestions;
            }
        }

        else if (args.length == 3 && args[0].equalsIgnoreCase("saverecipe") && sender.hasPermission(AlchemaConstants.PERMISSION_COMMAND_SAVERECIPE)) {
            return StringUtil.copyPartialMatches(args[2], SAVE_FLAG_ARGS, new ArrayList<>());
        }

        return Collections.emptyList();
    }

    @NotNull
    private List<String> getBaseArgsFor(@NotNull CommandSender sender) {
        Preconditions.checkArgument(sender != null, "sender must not be null");

        List<String> args = new ArrayList<>(BASE_ARGS.size());

        for (Entry<@NotNull String, @Nullable String> arg : BASE_ARGS.entrySet()) {
            String permission = arg.getValue();
            if (permission == null || sender.hasPermission(permission)) {
                args.add(arg.getKey());
            }
        }

        return args;
    }

    private <T> void displayListOfIntegrations(@NotNull CommandSender sender, @NotNull Collection<T> integrations, @NotNull Function<T, Plugin> toPluginFunction, boolean isNative) {
        integrations.forEach(integration -> {
            Plugin plugin = toPluginFunction.apply(integration);
            PluginDescriptionFile description = plugin.getDescription();
            String authors = generateListOfAuthors(plugin);

            sender.sendMessage(" - " + ChatColor.YELLOW + plugin.getName() + " " + ChatColor.GREEN + description.getVersion() + ChatColor.GRAY + " by " + authors + ChatColor.GRAY + "." + (isNative ? " " + ChatColor.AQUA + "(native)" : ""));
        });
    }

    private void displayListOfIntegrations(@NotNull CommandSender sender, @NotNull Collection<Plugin> integrations, boolean isNative) {
        this.displayListOfIntegrations(sender, integrations, Function.identity(), isNative);
    }

    @NotNull
    private String generateListOfAuthors(@NotNull Plugin plugin) {
        Preconditions.checkArgument(plugin != null, "plugin must not be null");

        List<String> authors = plugin.getDescription().getAuthors();
        if (authors.isEmpty()) {
            return ChatColor.GREEN + "Unknown";
        }
        else if (authors.size() == 1) {
            return ChatColor.GREEN + authors.get(0);
        }

        StringBuilder authorsString = new StringBuilder();

        for (int i = 0; i < authors.size(); i++) {
            String author = authors.get(i);
            if (i == authors.size() - 1) {
                authorsString.append(ChatColor.GRAY + ", and ");
            } else if (i != 0) {
                authorsString.append(ChatColor.GRAY + ", ");
            }

            authorsString.append(ChatColor.GREEN + author);
        }

        return authorsString.toString();
    }

}
