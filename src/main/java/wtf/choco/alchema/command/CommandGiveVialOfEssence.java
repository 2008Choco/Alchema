package wtf.choco.alchema.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.essence.EntityEssenceData;
import wtf.choco.alchema.util.NamespacedKeyUtil;

import static wtf.choco.alchema.Alchema.CHAT_PREFIX;

public final class CommandGiveVialOfEssence implements TabExecutor {

    private static final int MAX_AMOUNT_OF_ESSENCE = 1000; // TODO: Configurable

    private static final List<String> ARG_AMOUNTS = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");

    // usage: /<command> <entity> [amount of essence] [player] [amount]

    private final Alchema plugin;

    public CommandGiveVialOfEssence(Alchema plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length < 1) {
            sender.sendMessage(CHAT_PREFIX + "Which type of entity essence would you like to give? " + ChatColor.YELLOW + "/" + label + " <entity> [amount of essence] [player] [amount]");
            return true;
        }

        // Entity type argument
        NamespacedKey entityTypeKey = NamespacedKeyUtil.fromString(args[0], null);
        if (entityTypeKey == null) {
            sender.sendMessage(CHAT_PREFIX + "Invalid entity type id. Expected format, " + ChatColor.YELLOW + "minecraft:entity_id");
            return true;
        }

        EntityType type = Registry.ENTITY_TYPE.get(entityTypeKey);
        if (type == null) {
            sender.sendMessage(CHAT_PREFIX + "Could not find an entity with the id " + ChatColor.YELLOW + entityTypeKey + ChatColor.GRAY + ". Does it exist?");
            return true;
        }

        EntityEssenceData essenceData = plugin.getEntityEssenceEffectRegistry().getEntityEssenceData(type);
        if (essenceData == null) {
            sender.sendMessage(CHAT_PREFIX + ChatColor.YELLOW + entityTypeKey + ChatColor.GRAY + " does not have any essence registered with " + plugin.getName() + ".");
            return true;
        }

        // Amount of essence argument
        int amountOfEssence = (args.length >= 2 ? NumberUtils.toInt(args[1], -1) : MAX_AMOUNT_OF_ESSENCE);
        if (amountOfEssence <= 0 || amountOfEssence > MAX_AMOUNT_OF_ESSENCE) {
            sender.sendMessage(CHAT_PREFIX + "Amount of essence must not be zero, negative or exceed " + ChatColor.YELLOW + MAX_AMOUNT_OF_ESSENCE + ChatColor.GRAY + ".");
            return true;
        }

        // Target selector argument
        List<Player> targets = (sender instanceof Player) ? Arrays.asList((Player) sender) : Collections.emptyList();
        if (args.length >= 3) {
            targets = Bukkit.selectEntities(sender, args[2]).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .distinct()
                .collect(Collectors.toList());

            if (targets.isEmpty()) {
                sender.sendMessage(CHAT_PREFIX + "Invalid entity selection, " + ChatColor.YELLOW + args[2]);
                return true;
            }
        }

        if (targets.isEmpty()) {
            sender.sendMessage(CHAT_PREFIX + "You must select a player (or player selector) in order to run this command from the console");
            return true;
        }

        // Item amount argument
        int itemAmount = (args.length >= 4 ? NumberUtils.toInt(args[3], -1) : 1);
        if (itemAmount <= 0) {
            sender.sendMessage(CHAT_PREFIX + "Item amout must not be zero or negative.");
            return true;
        }

        ItemStack vialOfEssence = essenceData.createItemStack(amountOfEssence);
        ItemMeta vialOfEssenceMeta = vialOfEssence.getItemMeta();
        String itemName = vialOfEssenceMeta != null ? vialOfEssenceMeta.getDisplayName() : "vials of " + entityTypeKey.getKey().replace('_', ' ') + " essence";

        targets.forEach(player -> {
            World world = player.getWorld();
            Location location = player.getLocation();
            Inventory inventory = player.getInventory();

            for (int i = 0; i < itemAmount; i++) {
                inventory.addItem(vialOfEssence).forEach((slot, item) -> world.dropItem(location, item));
            }

            player.sendMessage(CHAT_PREFIX + "You were given " + ChatColor.YELLOW + itemAmount + "x " + itemName + ChatColor.GRAY + ".");
        });

        sender.sendMessage(CHAT_PREFIX + "You have given " + ChatColor.GREEN + (targets.size() == 1 ? targets.get(0).getName() : targets.size() + " players") + ChatColor.YELLOW + " " + itemAmount + "x " + itemName + ChatColor.GRAY + ".");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (args.length == 1) {
            Set<@NotNull EntityType> essenceTypes = plugin.getEntityEssenceEffectRegistry().getRegisteredEntityEssenceTypes();
            List<String> suggestions = new ArrayList<>(essenceTypes.size());

            essenceTypes.forEach(type -> {
                NamespacedKey key = type.getKey();
                if (key.getKey().startsWith(args[0]) || key.toString().startsWith(args[0])) {
                    suggestions.add(type.getKey().toString());
                }
            });

            return suggestions;
        }

        else if (args.length == 2 && args[1].isEmpty()) {
            List<String> suggestions = new ArrayList<>();

            // Suggest factors worth of essence (full, 1/2 or 1/4)
            for (int divisionFactor = 1; divisionFactor <= 4; divisionFactor *= 2) {
                suggestions.add(String.valueOf(MAX_AMOUNT_OF_ESSENCE / divisionFactor));
            }

            if (!suggestions.contains("1")) {
                suggestions.add("1");
            }

            return suggestions;
        }

        else if (args.length == 3) {
            String arg = args[2];
            List<String> suggestions = StringUtil.copyPartialMatches(arg, Arrays.asList("@a", "@p", "@r", "@s"), new ArrayList<>());

            Bukkit.getOnlinePlayers().forEach(player -> {
                if (arg.isEmpty() || player.getName().toLowerCase().startsWith(arg.toLowerCase())) {
                    suggestions.add(player.getName());
                }
            });

            return suggestions;
        }

        else if (args.length == 4 && args[3].isEmpty()) {
            return ARG_AMOUNTS;
        }

        return Collections.emptyList();
    }

}
