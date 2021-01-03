package wtf.choco.alchema.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.util.UpdateChecker;
import wtf.choco.alchema.util.UpdateChecker.UpdateResult;

public final class UpdateReminderListener implements Listener {

    private final Alchema plugin;

    public UpdateReminderListener(Alchema plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (!UpdateChecker.isInitialized()) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("alchema.updatenotify")) {
            return;
        }

        UpdateResult result = UpdateChecker.get().getLastResult();
        if (result == null /*|| !result.requiresUpdate()*/) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.5F);
            player.sendMessage(Alchema.CHAT_PREFIX + String.format(
                "An update is available! Alchema " + ChatColor.AQUA + "%s " + ChatColor.GRAY + "may be downloaded on SpigotMC. Use " + ChatColor.YELLOW + "/alchema version " + ChatColor.GRAY + "for more information.",
                result.getNewestVersion()
            ));
        }, 1);
    }

}
