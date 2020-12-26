package wtf.choco.alchema.listener;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.essence.EntityEssenceData;

public final class EntityEssenceLootListener implements Listener {

    private final Alchema plugin;

    public EntityEssenceLootListener(@NotNull Alchema plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        EntityEssenceData essenceData = plugin.getEntityEssenceEffectRegistry().getEntityEssenceData(entity.getType());
        if (essenceData == null) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextDouble(100) >= 0.1) { // 0.1% chance
            return;
        }

        // TODO: Make this amount of essence configurable and slightly random
        event.getDrops().add(essenceData.createItemStack(50));
    }

}
