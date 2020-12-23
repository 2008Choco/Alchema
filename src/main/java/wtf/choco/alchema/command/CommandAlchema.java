package wtf.choco.alchema.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.util.UpdateChecker;
import wtf.choco.alchema.util.UpdateChecker.UpdateResult;

public final class CommandAlchema implements TabExecutor {

    private static final List<String> BASE_ARGS = Arrays.asList("version", "reload");

    private final Alchema plugin;

    public CommandAlchema(Alchema plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            sender.sendMessage(Alchema.CHAT_PREFIX + "Insufficient arguments. " + ChatColor.YELLOW + "/" + label + " " + BASE_ARGS);
            return true;
        }

        if (args[0].equalsIgnoreCase("version")) {
            sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------------");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Version: " + ChatColor.GRAY  + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Developer / Maintainer: " + ChatColor.GRAY + "Choco " + ChatColor.YELLOW + "( https://choco.wtf/ )");
            sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Development Page: " + ChatColor.GRAY + "To Be Determined");
            sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Report Bugs To: " + ChatColor.GRAY + "https://github.com/2008Choco/Alchema/issues/");

            if (UpdateChecker.isInitialized()) {
                UpdateResult result = UpdateChecker.get().getLastResult();
                sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "New Version Available: " + (result == null ? ChatColor.YELLOW + "N/A (Unchecked)" : (result.requiresUpdate() ? ChatColor.GREEN + "Yes! " + ChatColor.YELLOW + result.getNewestVersion() : ChatColor.RED + "No")));
            }

            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------------");
        }

        else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("alchema.command.reload")) {
                sender.sendMessage(Alchema.CHAT_PREFIX + "You have insufficient privileges to run this command.");
                return true;
            }

            this.plugin.getRecipeRegistry().clearRecipes();
            this.plugin.reloadConfig();

            this.plugin.loadCauldronRecipes().whenComplete((result, exception) -> {
                if (exception != null) {
                    exception.printStackTrace();
                    sender.sendMessage(Alchema.CHAT_PREFIX + ChatColor.RED + "Something went wrong while loading recipes... check the console for errors.");
                    return;
                }

                sender.sendMessage(Alchema.CHAT_PREFIX + "Loaded " + ChatColor.YELLOW + "(" + result.getTotal() + ") " + ChatColor.GRAY + "cauldron recipes." + (result.getNative() != result.getTotal()
                    ? ChatColor.YELLOW + " (" + result.getNative() + ") " + ChatColor.GRAY + "internal recipes and " + ChatColor.YELLOW + "(" + result.getThirdParty() + ") " + ChatColor.GRAY + "third-party recipes (other plugins)."
                    : ""));
            });

            sender.sendMessage(Alchema.CHAT_PREFIX + ChatColor.GREEN + "Successfully reloaded the configuration file.");
        }

        else {
            sender.sendMessage(Alchema.CHAT_PREFIX + "Unknown command argument, " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + ".");
        }

        return true;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        return args.length == 1 ? StringUtil.copyPartialMatches(args[0], BASE_ARGS, new ArrayList<>()) : Collections.emptyList();
    }

}
