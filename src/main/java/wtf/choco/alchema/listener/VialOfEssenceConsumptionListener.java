package wtf.choco.alchema.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.essence.EntityEssenceData;

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

        EntityType type = EntityEssenceData.getEntityEssenceType(item);
        if (type == null) {
            return;
        }

        EntityEssenceData essenceData = plugin.getEntityEssenceEffectRegistry().getEntityEssenceData(type);
        if (essenceData == null || !essenceData.isConsumable()) {
            return;
        }

        essenceData.applyConsumptionEffectTo(event.getPlayer(), item);
    }

}
