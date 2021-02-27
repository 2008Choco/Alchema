package wtf.choco.alchema.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.crafting.CauldronRecipeBook;

public final class CommandGiveRecipeBook implements TabExecutor {

    private final Alchema plugin;

    public CommandGiveRecipeBook(Alchema plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may execute this command");
            return true;
        }

        ((Player) sender).getInventory().addItem(CauldronRecipeBook.createRecipeBook(plugin.getRecipeRegistry()));
        sender.sendMessage(Alchema.CHAT_PREFIX + "You were given a recipe book.");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        return Collections.emptyList();
    }

}
