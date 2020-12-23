package wtf.choco.alchema.cauldron;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.api.event.CauldronIngredientAddEvent;
import wtf.choco.alchema.api.event.CauldronIngredientsDropEvent;
import wtf.choco.alchema.api.event.CauldronItemCraftEvent;
import wtf.choco.alchema.api.event.entity.EntityDamageByCauldronEvent;
import wtf.choco.alchema.crafting.CauldronIngredient;
import wtf.choco.alchema.crafting.CauldronIngredientItemStack;
import wtf.choco.alchema.crafting.CauldronRecipe;
import wtf.choco.alchema.crafting.CauldronRecipeRegistry;
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.alchema.util.AlchemaEventFactory;

public class CauldronUpdateTask extends BukkitRunnable {

    private static CauldronUpdateTask instance = null;

    private int currentTick = 0;

    private final Alchema plugin;
    private final CauldronManager cauldronManager;
    private final CauldronRecipeRegistry recipeRegistry;

    private CauldronUpdateTask(@NotNull Alchema plugin) {
        this.plugin = plugin;
        this.cauldronManager = plugin.getCauldronManager();
        this.recipeRegistry = plugin.getRecipeRegistry();
    }

    @Override
    public void run() {
        this.currentTick++;

        // Pull configuration values every tick, but only once for every cauldron iteration
        FileConfiguration config = plugin.getConfig();
        int millisecondsToHeatUp = Math.max(config.getInt(AlchemaConstants.CONFIG_CAULDRON_MILLISECONDS_TO_HEAT_UP, 5000), 0);
        boolean damageEntities = config.getBoolean(AlchemaConstants.CONFIG_CAULDRON_DAMAGE_ENTITIES, true);

        float volumeAmbientBubble = (float) config.getDouble(AlchemaConstants.CONFIG_CAULDRON_SOUND_AMBIENT_BUBBLE_VOLUME, 0.45);
        float volumeItemSplash = (float) config.getDouble(AlchemaConstants.CONFIG_CAULDRON_SOUND_ITEM_SPLASH_VOLUME, 1.0);
        float volumeSuccessfulCraft = (float) config.getDouble(AlchemaConstants.CONFIG_CAULDRON_SOUND_SUCCESSFUL_CRAFT_VOLUME, 0.5);

        for (AlchemicalCauldron cauldron : cauldronManager.getCauldrons()) {
            Block block = cauldron.getCauldronBlock();
            Location location = block.getLocation().add(0.5, 0.25, 0.5);
            Location particleLocation = block.getLocation().add(0.5, 1, 0.5);
            World world = block.getWorld();

            // Unheat if conditions are not met
            if (!cauldron.canHeatUp()) {
                cauldron.stopHeatingUp();
                cauldron.setBubbling(false);

                // Drop ingredients if any
                if (!cauldron.hasIngredients()) {
                    continue;
                }

                List<ItemStack> items = cauldron.getIngredients().stream().map(CauldronIngredient::asItemStack).filter(Objects::nonNull).collect(Collectors.toList());
                CauldronIngredientsDropEvent ingredientsDropEvent = AlchemaEventFactory.callCauldronIngredientsDropEvent(cauldron, items, null, CauldronIngredientsDropEvent.Reason.UNHEATED);
                if (ingredientsDropEvent.isCancelled()) {
                    continue;
                }

                ingredientsDropEvent.getItems().forEach(item -> world.dropItem(location, item));
                cauldron.clearIngredients();
                continue;
            }

            // Attempt to heat cauldron (if valid)
            if (!cauldron.isBubbling() && !cauldron.isHeatingUp() && !cauldron.attemptToHeatUp()) {
                continue;
            }

            // Prepare bubbling cauldrons
            if (cauldron.isHeatingUp()) {
                long timeSinceHeatingUp = System.currentTimeMillis() - cauldron.getHeatingStartTime();
                if (timeSinceHeatingUp < millisecondsToHeatUp) {
                    continue;
                }

                cauldron.stopHeatingUp();
                cauldron.setBubbling(true);
            }

            world.spawnParticle(Particle.BUBBLE_COLUMN_UP, block.getLocation().add(0.5, 0.95, 0.5), 2, 0.15F, 0F, 0.15F, 0F);
            if (currentTick % 40 == 0 && volumeAmbientBubble > 0.0) {
                world.playSound(location, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, volumeAmbientBubble, 0.8F);
            }

            // Dissolve items in bubbling cauldrons
            world.getNearbyEntities(cauldron.getItemConsumptionBounds()).forEach(entity -> {
                if (entity instanceof Item) {
                    Item item = (Item) entity;
                    if (item.hasMetadata(AlchemaConstants.METADATA_KEY_CAULDRON_CRAFTED)) {
                        return;
                    }

                    ItemStack itemStack = item.getItemStack();
                    CauldronIngredient ingredient = new CauldronIngredientItemStack(itemStack, itemStack.getAmount());

                    CauldronIngredientAddEvent ingredientAddEvent = AlchemaEventFactory.callCauldronIngredientAddEvent(cauldron, ingredient, item);

                    cauldron.addIngredient(ingredientAddEvent.getIngredient());
                    item.remove();

                    world.spawnParticle(Particle.WATER_SPLASH, particleLocation, 4);

                    if (volumeItemSplash > 0.0) {
                        world.playSound(location, Sound.ENTITY_PLAYER_SPLASH, volumeItemSplash, 2F);
                    }
                }
                else if (damageEntities && entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (currentTick % 20 == 0) {
                        EntityDamageByCauldronEvent entityDamageByCauldronEvent = AlchemaEventFactory.callEntityDamageByCauldronEvent(livingEntity, cauldron, 1.0);

                        double damage = entityDamageByCauldronEvent.getDamage();
                        if (entityDamageByCauldronEvent.isCancelled() || damage <= 0.0) {
                            return;
                        }

                        livingEntity.damage(damage);
                        livingEntity.setMetadata(AlchemaConstants.METADATA_KEY_DAMAGED_BY_CAULDRON, new FixedMetadataValue(plugin, System.currentTimeMillis()));
                    }
                }
            });

            if (!cauldron.hasIngredients()) {
                return;
            }

            CauldronRecipe activeRecipe = recipeRegistry.getApplicableRecipe(cauldron.getIngredients());
            if (activeRecipe == null) {
                return;
            }

            OfflinePlayer lastInteracted = cauldron.getLastInteracted();
            CauldronItemCraftEvent cauldronCraftEvent = AlchemaEventFactory.callCauldronItemCraftEvent(cauldron, activeRecipe, lastInteracted != null ? lastInteracted.getPlayer() : null);
            if (cauldronCraftEvent.isCancelled()) {
                break;
            }

            ThreadLocalRandom random = ThreadLocalRandom.current();
            Vector itemVelocity = new Vector(random.nextDouble() / 10.0, 0.10 + (random.nextDouble() / 3), random.nextDouble() / 10.0);

            ItemStack result = cauldronCraftEvent.getResult();
            if (result != null) {
                Item item = world.dropItem(block.getLocation().add(0.5, 1.1, 0.5), result);
                item.setVelocity(itemVelocity);
                item.setMetadata(AlchemaConstants.METADATA_KEY_CAULDRON_CRAFTED, new FixedMetadataValue(plugin, true));
            }

            cauldron.removeIngredients(activeRecipe);

            world.spawnParticle(Particle.SPELL_WITCH, particleLocation, 10, 0.3, 0.2, 0.3, 0.0);

            if (volumeSuccessfulCraft > 0.0) {
                world.playSound(location, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, volumeSuccessfulCraft, 1.5F);
                world.playSound(location, Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, volumeSuccessfulCraft, 0.8F);
            }
        }
    }

    @NotNull
    public static CauldronUpdateTask startTask(@NotNull Alchema plugin) {
        Preconditions.checkNotNull(plugin, "plugin cannot be null");

        if (instance == null) {
            instance = new CauldronUpdateTask(plugin);
            instance.runTaskTimer(plugin, 0, 1);
        }

        return instance;
    }

}