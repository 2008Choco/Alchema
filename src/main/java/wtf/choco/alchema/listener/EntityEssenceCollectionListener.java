package wtf.choco.alchema.listener;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.api.event.entity.EntityDropEssenceEvent;
import wtf.choco.alchema.essence.EntityEssenceData;
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.alchema.util.AlchemaEventFactory;
import wtf.choco.alchema.util.EntityBlacklist;

public final class EntityEssenceCollectionListener implements Listener {

    private final Alchema plugin;
    private final EntityBlacklist deathBlacklist, interactBlacklist;

    public EntityEssenceCollectionListener(@NotNull Alchema plugin) {
        this.plugin = plugin;
        this.deathBlacklist = new EntityBlacklist(() -> plugin.getConfig().getStringList(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_DEATH_BLACKLIST));
        this.interactBlacklist = new EntityBlacklist(() -> plugin.getConfig().getStringList(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_BLACKLIST));
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        EntityType type = entity.getType();
        if (deathBlacklist.contains(type)) {
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

    @EventHandler
    private void onPlayerRightClickEntity(PlayerInteractEntityEvent event) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_ENABLED, true)) {
            return;
        }

        Entity entity = event.getRightClicked();
        EntityType type = entity.getType();
        if (interactBlacklist.contains(type)) {
            return;
        }

        EntityEssenceData essenceData = plugin.getEntityEssenceEffectRegistry().getEntityEssenceData(type);
        if (essenceData == null) {
            return;
        }

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        EquipmentSlot hand = event.getHand();
        ItemStack item = inventory.getItem(hand);

        if (EntityEssenceData.isEmptyVial(item)) {
            if (!canHaveEssenceExtracted(player, entity, config)) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("This " + type.getKey().getKey().replace('_', ' ') + " has had its essence extracted recently."));
                event.setCancelled(true);
                return;
            }

            ThreadLocalRandom random = ThreadLocalRandom.current();
            int minimumEssence = config.getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_MIN, 10);
            int maximumEssence = config.getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_MAX, 25);
            int amountOfEssence = random.nextInt(maximumEssence - minimumEssence) + minimumEssence;

            if (player.getGameMode() != GameMode.CREATIVE) {
                item.setAmount(item.getAmount() - 1);
                inventory.setItem(hand, item);
            }

            // If the item does not fit in the inventory, we'll drop it on the ground
            inventory.addItem(essenceData.createItemStack(amountOfEssence)).forEach((slot, newItem) -> player.getWorld().dropItemNaturally(player.getLocation(), newItem));
            player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0F, 1.25F);

            if (player.getGameMode() != GameMode.CREATIVE) {
                entity.setMetadata(AlchemaConstants.METADATA_KEY_INTERACTED_WITH_VIAL, new FixedMetadataValue(plugin, System.currentTimeMillis()));
            }
        }

        else if (EntityEssenceData.isVialOfEntityEssence(item)) {
            EntityType essenceType = EntityEssenceData.getEntityEssenceType(item);
            if (essenceType != type) {
                return;
            }

            if (!canHaveEssenceExtracted(player, entity, config)) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("This " + type.getKey().getKey().replace('_', ' ') + " has had its essence extracted recently."));
                event.setCancelled(true);
                return;
            }

            int maximumTotalEssence = config.getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_MAXIMUM_ESSENCE, 1000);
            int currentEssence = EntityEssenceData.getEntityEssenceAmount(item);

            if (currentEssence >= maximumTotalEssence) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("This vial of essence is full."));
                event.setCancelled(true);
                return;
            }

            ThreadLocalRandom random = ThreadLocalRandom.current();
            int minimumEssence = config.getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_MIN, 10);
            int maximumEssence = config.getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_MAX, 25);
            int amountOfEssence = random.nextInt(maximumEssence - minimumEssence) + minimumEssence;

            if (item.getAmount() == 1) {
                item = essenceData.applyTo(item, Math.min(currentEssence + amountOfEssence, maximumTotalEssence));
                inventory.setItem(hand, item);
            }
            else {
                item.setAmount(item.getAmount() - 1);
                inventory.setItem(hand, item);

                ItemStack itemToAdd = essenceData.applyTo(item.clone(), Math.min(currentEssence + amountOfEssence, maximumTotalEssence));
                itemToAdd.setAmount(1);

                // If the item does not fit in the inventory, we'll drop it on the ground
                inventory.addItem(essenceData.createItemStack(amountOfEssence)).forEach((slot, newItem) -> player.getWorld().dropItemNaturally(player.getLocation(), newItem));
            }

            player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0F, 1.25F);

            if (player.getGameMode() != GameMode.CREATIVE) {
                entity.setMetadata(AlchemaConstants.METADATA_KEY_INTERACTED_WITH_VIAL, new FixedMetadataValue(plugin, System.currentTimeMillis()));
            }
        }
    }

    private boolean canHaveEssenceExtracted(Player player, Entity entity, FileConfiguration config) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }

        int timeoutSeconds = config.getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_TIMEOUT_SECONDS, 300);
        if (timeoutSeconds <= 0) {
            return true;
        }

        List<MetadataValue> interactionMetadata = entity.getMetadata(AlchemaConstants.METADATA_KEY_INTERACTED_WITH_VIAL);
        long lastInteractedWith = -1;

        for (MetadataValue value : interactionMetadata) {
            lastInteractedWith = Math.max(lastInteractedWith, value.asLong());
        }

        long secondsSinceLastInteraction = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastInteractedWith);
        return secondsSinceLastInteraction >= timeoutSeconds;
    }

    public void refreshBlacklists() {
        this.deathBlacklist.refresh();
        this.interactBlacklist.refresh();
    }

}
