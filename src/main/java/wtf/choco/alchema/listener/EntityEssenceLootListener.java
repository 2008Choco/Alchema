package wtf.choco.alchema.listener;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.api.event.entity.EntityDropEssenceEvent;
import wtf.choco.alchema.essence.EntityEssenceData;
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.alchema.util.AlchemaEventFactory;
import wtf.choco.alchema.util.EntityBlacklist;

public final class EntityEssenceLootListener implements Listener {

    private final Alchema plugin;
    private final EntityBlacklist entityBlacklist;

    public EntityEssenceLootListener(@NotNull Alchema plugin) {
        this.plugin = plugin;
        this.entityBlacklist = new EntityBlacklist(() -> plugin.getConfig().getStringList(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_DEATH_BLACKLIST));
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        EntityType type = entity.getType();
        if (entityBlacklist.contains(type)) {
            return;
        }

        FileConfiguration config = plugin.getConfig();
        double baseDropChance = config.getDouble(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_DEATH_BASE_DROP_CHANCE, 0.75);
        if (baseDropChance <= 0.0) {
            return;
        }

        EntityEssenceData essenceData = plugin.getEntityEssenceEffectRegistry().getEntityEssenceData(type);
        if (essenceData == null) {
            return;
        }

        int lootingModifier = 0;

        if (entity instanceof LivingEntity) {
            Player killer = ((LivingEntity) entity).getKiller();
            if (killer != null) {
                ItemStack item = killer.getInventory().getItemInMainHand();
                lootingModifier = item.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
            }
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextDouble() * 100.0 >= baseDropChance + (lootingModifier * 0.25)) { // 0.75% chance
            return;
        }

        int minimumEssence = config.getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_DEATH_MIN, 50);
        int maximumEssence = config.getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_DEATH_MAX, 250);
        int amountOfEssence = random.nextInt(maximumEssence - minimumEssence) + minimumEssence;

        EntityDropEssenceEvent entityDropEssenceEvent = AlchemaEventFactory.callEntityDropEssenceEvent(entity, essenceData, amountOfEssence);
        if (entityDropEssenceEvent.isCancelled()) {
            return;
        }

        event.getDrops().add(essenceData.createItemStack(entityDropEssenceEvent.getAmountOfEssence()));
    }

    public void refreshBlacklist() {
        this.entityBlacklist.refresh();
    }

}
