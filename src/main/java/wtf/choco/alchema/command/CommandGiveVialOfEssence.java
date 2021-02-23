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
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.commons.util.NamespacedKeyUtil;

import static wtf.choco.alchema.Alchema.CHAT_PREFIX;

public final class CommandGiveVialOfEssence implements TabExecutor {

    private static final List<String> ARG_AMOUNTS = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");

    // usage: /<command> [player] [amount] [entity] [amount of essence]

    private final Alchema plugin;

    public CommandGiveVialOfEssence(Alchema plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length < 1 && !(sender instanceof Player)) {
            sender.sendMessage("You must specify a player when running this command from the console.");
            return true;
        }

        // Target selector argument
        List<Player> targets = (sender instanceof Player) ? Arrays.asList((Player) sender) : Collections.emptyList();
        if (args.length >= 1) {
            targets = Bukkit.selectEntities(sender, args[0]).stream()
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
        int itemAmount = (args.length >= 2 ? NumberUtils.toInt(args[1], -1) : 1);
        if (itemAmount <= 0) {
            sender.sendMessage(CHAT_PREFIX + "Item amout must not be zero or negative.");
            return true;
        }

        int maximumEssence = plugin.getConfig().getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_MAXIMUM_ESSENCE, 1000);
        NamespacedKey entityTypeKey = null;
        EntityEssenceData essenceData = null;

        // Entity type argument
        if (args.length >= 3) {
            entityTypeKey = NamespacedKeyUtil.fromString(args[2], null);
            if (entityTypeKey == null) {
                sender.sendMessage(CHAT_PREFIX + "Invalid entity type id. Expected format, " + ChatColor.YELLOW + "minecraft:entity_id");
                return true;
            }

            EntityType type = Registry.ENTITY_TYPE.get(entityTypeKey);
            if (type == null) {
                sender.sendMessage(CHAT_PREFIX + "Could not find an entity with the id " + ChatColor.YELLOW + entityTypeKey + ChatColor.GRAY + ". Does it exist?");
                return true;
            }

            essenceData = plugin.getEntityEssenceEffectRegistry().getEntityEssenceData(type);
            if (essenceData == null) {
                sender.sendMessage(CHAT_PREFIX + ChatColor.YELLOW + entityTypeKey + ChatColor.GRAY + " does not have any essence registered with " + plugin.getName() + ".");
                return true;
            }
        }

        // Amount of essence argument
        int amountOfEssence = (args.length >= 4 ? NumberUtils.toInt(args[3], -1) : maximumEssence);
        if (amountOfEssence <= 0 || amountOfEssence > maximumEssence) {
            sender.sendMessage(CHAT_PREFIX + "Amount of essence must not be zero, negative or exceed " + ChatColor.YELLOW + maximumEssence + ChatColor.GRAY + ".");
            return true;
        }

        ItemStack vialOfEssence = essenceData != null ? essenceData.createItemStack(amountOfEssence) : EntityEssenceData.createEmptyVial(itemAmount);
        ItemMeta vialOfEssenceMeta = vialOfEssence.getItemMeta();
        String itemName = vialOfEssenceMeta != null ? vialOfEssenceMeta.getDisplayName() : "vials of " + (entityTypeKey != null ? entityTypeKey.getKey().replace('_', ' ') : "null") + " essence";

        boolean isEntityEssence = essenceData != null;

        targets.forEach(player -> {
            World world = player.getWorld();
            Location location = player.getLocation();
            Inventory inventory = player.getInventory();

            if (isEntityEssence) {
                // We want to treat entity essence a little differently because they shouldn't stack. Give them individually
                for (int i = 0; i < itemAmount; i++) {
                    inventory.addItem(vialOfEssence).forEach((slot, item) -> world.dropItem(location, item));
                }
            }
            else {
                inventory.addItem(vialOfEssence).forEach((slot, item) -> world.dropItem(location, item));
            }

            player.sendMessage(CHAT_PREFIX + "You were given " + ChatColor.YELLOW + itemAmount + "x " + itemName + ChatColor.GRAY + ".");
        });

        if (targets.size() > 1 || (targets.size() == 1 && targets.get(0) != sender)) {
            sender.sendMessage(CHAT_PREFIX + "You have given " + ChatColor.GREEN + (targets.size() == 1 ? targets.get(0).getName() : targets.size() + " players") + ChatColor.YELLOW + " " + itemAmount + "x " + itemName + ChatColor.GRAY + ".");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (args.length == 1) {
            String arg = args[0];
            List<String> suggestions = StringUtil.copyPartialMatches(arg, Arrays.asList("@a", "@p", "@r", "@s"), new ArrayList<>());

            Bukkit.getOnlinePlayers().forEach(player -> {
                if (arg.isEmpty() || player.getName().toLowerCase().startsWith(arg.toLowerCase())) {
                    suggestions.add(player.getName());
                }
            });

            return suggestions;
        }

        else if (args.length == 2 && args[1].isEmpty()) {
            return ARG_AMOUNTS;
        }

        else if (args.length == 3) {
            String arg = args[2];

            Set<@NotNull EntityType> essenceTypes = plugin.getEntityEssenceEffectRegistry().getRegisteredEntityEssenceTypes();
            List<String> suggestions = new ArrayList<>(essenceTypes.size());

            essenceTypes.forEach(type -> {
                NamespacedKey key = type.getKey();
                if (key.getKey().startsWith(arg) || key.toString().startsWith(arg)) {
                    suggestions.add(type.getKey().toString());
                }
            });

            return suggestions;
        }

        else if (args.length == 4 && args[3].isEmpty()) {
            List<String> suggestions = new ArrayList<>();

            int maximumEssence = plugin.getConfig().getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_MAXIMUM_ESSENCE, 1000);

            // Suggest factors worth of essence (full, 1/2 or 1/4)
            for (int divisionFactor = 1; divisionFactor <= 4; divisionFactor *= 2) {
                suggestions.add(String.valueOf(maximumEssence / divisionFactor));
            }

            if (!suggestions.contains("1")) {
                suggestions.add("1");
            }

            return suggestions;
        }

        return Collections.emptyList();
    }

}
