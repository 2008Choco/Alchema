package wtf.choco.alchema.listener;

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
import wtf.choco.alchema.util.AlchemaEventFactory;

public final class VialOfEssenceConsumptionListener implements Listener {

    // TODO: Configurable
    private static final String[] TASTELESS_THOUGHTS = {
        "That was rather tasteless... I shouldn't do that again.",
        "What a waste of essence... I shouldn't drink this stuff.",
        "Interestingly tasteless, disappointingly wasteful.",
        "Surely there was a better use for that essence than drinking it."
    };

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

        // TODO: Future PR for 1.16.4
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
        PlayerConsumeEntityEssenceEvent playerConsumeEntityEssenceEvent = AlchemaEventFactory.handlePlayerConsumeEntityEssenceEvent(player, item, essenceData);

        if (playerConsumeEntityEssenceEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        boolean applyEffect = playerConsumeEntityEssenceEvent.shouldApplyEffect();
        if (!applyEffect || (applyEffect && !essenceData.applyConsumptionEffectTo(player, item))) {
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + TASTELESS_THOUGHTS[ThreadLocalRandom.current().nextInt(TASTELESS_THOUGHTS.length)]);
        }
    }

}
