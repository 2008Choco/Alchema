package wtf.choco.alchema.listener;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.api.event.player.PlayerConsumeEntityEssenceEvent;
import wtf.choco.alchema.essence.EntityEssenceData;
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.alchema.util.AlchemaEventFactory;

public final class VialOfEssenceConsumptionListener implements Listener {

    private final Alchema plugin;

    public VialOfEssenceConsumptionListener(Alchema plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onConsumeVialOfEssence(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!EntityEssenceData.isVialOfEntityEssence(item)) {
            return;
        }

        // TODO: Create pull request to Bukkit
//        event.setReplacement(EntityEssenceData.createEmptyVial(item.getAmount()));

        EntityType type = EntityEssenceData.getEntityEssenceType(item);
        if (type == null) {
            return;
        }

        EntityEssenceData essenceData = plugin.getEntityEssenceEffectRegistry().getEntityEssenceData(type);
        if (essenceData == null) {
            return;
        }

        Player player = event.getPlayer();
        PlayerConsumeEntityEssenceEvent playerConsumeEntityEssenceEvent = AlchemaEventFactory.callPlayerConsumeEntityEssenceEvent(player, item, essenceData);

        if (playerConsumeEntityEssenceEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        boolean applyEffect = playerConsumeEntityEssenceEvent.shouldApplyEffect();
        if (!applyEffect || (applyEffect && !essenceData.applyConsumptionEffectTo(player, item))) {
            List<String> tastelessThoughts = plugin.getConfig().getStringList(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_CONSUMPTION_TASTELESS_THOUGHTS);
            if (tastelessThoughts.isEmpty()) {
                return;
            }

            String tastelessThought = tastelessThoughts.get(ThreadLocalRandom.current().nextInt(tastelessThoughts.size()));
            if (tastelessThought.isEmpty()) {
                return;
            }

            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + tastelessThought);
        }
    }

}
