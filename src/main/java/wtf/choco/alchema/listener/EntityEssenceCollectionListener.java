package wtf.choco.alchema.listener;

import java.util.concurrent.ThreadLocalRandom;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.api.event.entity.EntityDropEssenceEvent;
import wtf.choco.alchema.api.event.player.PlayerEssenceCollectEvent;
import wtf.choco.alchema.essence.EntityEssenceData;
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.alchema.util.AlchemaEventFactory;
import wtf.choco.alchema.util.EssenceUtil;
import wtf.choco.alchema.util.RefreshableEnumSets;
import wtf.choco.commons.collection.RefreshableEnumSet;

public final class EntityEssenceCollectionListener implements Listener {

    private final Alchema plugin;
    private final RefreshableEnumSet<@NotNull EntityType> deathBlacklist, interactBlacklist;

    public EntityEssenceCollectionListener(@NotNull Alchema plugin) {
        this.plugin = plugin;
        this.deathBlacklist = RefreshableEnumSets.entityType(() -> plugin.getConfig().getStringList(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_DEATH_BLACKLIST));
        this.interactBlacklist = RefreshableEnumSets.entityType(() -> plugin.getConfig().getStringList(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_BLACKLIST));
    }

    @EventHandler(priority = EventPriority.LOW)
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

        // If the entity was recently interacted with, don't attempt to drop essence
        if (!EssenceUtil.canHaveEssenceExtracted(entity, plugin)) {
            return;
        }

        EntityEssenceData essenceData = plugin.getEntityEssenceEffectRegistry().getEntityEssenceData(type);
        if (essenceData == null) {
            return;
        }

        int lootingModifier = 0;

        if (entity instanceof LivingEntity livingEntity) {
            Player killer = livingEntity.getKiller();
            if (killer != null) {
                ItemStack item = killer.getInventory().getItemInMainHand();
                lootingModifier = item.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
            }
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextDouble() * 100.0 >= baseDropChance + (lootingModifier * 0.25)) { // 0.75% chance
            return;
        }

        int amountOfEssence = getRandomEssenceAmount(random, config, AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_DEATH_MIN, 50, AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_DEATH_MAX, 250);

        EntityDropEssenceEvent entityDropEssenceEvent = AlchemaEventFactory.callEntityDropEssenceEvent(entity, essenceData, amountOfEssence);
        if (entityDropEssenceEvent.isCancelled()) {
            return;
        }

        event.getDrops().add(essenceData.createItemStack(entityDropEssenceEvent.getAmountOfEssence()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
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

        if (item == null) {
            return;
        }

        boolean creativeMode = player.getGameMode() == GameMode.CREATIVE;

        if (EntityEssenceData.isEmptyVial(item)) {
            if (!creativeMode && !EssenceUtil.canHaveEssenceExtracted(entity, plugin)) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("This ")
                        .append(entityTypeComponent(type))
                        .append(" has had its essence extracted recently.")
                        .create());
                event.setCancelled(true);
                return;
            }

            int amountOfEssence = getRandomEssenceAmount(config, AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_MIN, 10, AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_MAX, 25);

            PlayerEssenceCollectEvent playerEssenceCollectEvent = AlchemaEventFactory.callPlayerEssenceCollectEvent(player, hand, item, entity, essenceData, amountOfEssence);
            if (playerEssenceCollectEvent.isCancelled()) {
                return;
            }

            amountOfEssence = playerEssenceCollectEvent.getEssenceAmount();

            if (!creativeMode) {
                item.setAmount(item.getAmount() - 1);
                inventory.setItem(hand, item);
            }

            // If the item does not fit in the inventory, we'll drop it on the ground
            inventory.addItem(essenceData.createItemStack(amountOfEssence)).forEach((slot, newItem) -> player.getWorld().dropItemNaturally(player.getLocation(), newItem));
            player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0F, 1.25F);

            if (!creativeMode) {
                entity.setMetadata(AlchemaConstants.METADATA_KEY_INTERACTED_WITH_VIAL, new FixedMetadataValue(plugin, System.currentTimeMillis()));
            }
        }

        else if (EntityEssenceData.isVialOfEntityEssence(item)) {
            EntityType essenceType = EntityEssenceData.getEntityEssenceType(item);
            if (essenceType != type) {
                return;
            }

            if (!creativeMode && !EssenceUtil.canHaveEssenceExtracted(entity, plugin)) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("This ")
                        .append(entityTypeComponent(type))
                        .append(" has had its essence extracted recently.")
                        .create());
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

            int amountOfEssence = getRandomEssenceAmount(config, AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_MIN, 10, AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_FROM_ENTITIES_ON_INTERACT_MAX, 25);

            PlayerEssenceCollectEvent playerEssenceCollectEvent = AlchemaEventFactory.callPlayerEssenceCollectEvent(player, hand, item, entity, essenceData, amountOfEssence);
            if (playerEssenceCollectEvent.isCancelled()) {
                return;
            }

            amountOfEssence = playerEssenceCollectEvent.getEssenceAmount();

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

            if (!creativeMode) {
                entity.setMetadata(AlchemaConstants.METADATA_KEY_INTERACTED_WITH_VIAL, new FixedMetadataValue(plugin, System.currentTimeMillis()));
            }
        }
    }

    private int getRandomEssenceAmount(ThreadLocalRandom random, FileConfiguration config, String minPath, int minDefault, String maxPath, int maxDefault) {
        int minimumEssence = config.getInt(minPath, minDefault);
        int maximumEssence = config.getInt(maxPath, maxDefault);

        return random.nextInt(maximumEssence - minimumEssence) + minimumEssence;
    }

    private int getRandomEssenceAmount(FileConfiguration config, String minPath, int minDefault, String maxPath, int maxDefault) {
        return getRandomEssenceAmount(ThreadLocalRandom.current(), config, minPath, minDefault, maxPath, maxDefault);
    }

    private TranslatableComponent entityTypeComponent(EntityType entityType) {
        NamespacedKey key = entityType.getKey();
        String translationKey = "entity." + key.getNamespace() + "." + key.getKey();
        return new TranslatableComponent(translationKey);
    }

    public void refreshBlacklists() {
        this.deathBlacklist.refresh();
        this.interactBlacklist.refresh();
    }

}
