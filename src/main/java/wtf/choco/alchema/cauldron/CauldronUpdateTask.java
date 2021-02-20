package wtf.choco.alchema.cauldron;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
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
import wtf.choco.alchema.api.event.entity.EntityDeathByCauldronEvent;
import wtf.choco.alchema.crafting.CauldronIngredient;
import wtf.choco.alchema.crafting.CauldronIngredientEntityEssence;
import wtf.choco.alchema.crafting.CauldronIngredientItemStack;
import wtf.choco.alchema.crafting.CauldronRecipe;
import wtf.choco.alchema.crafting.CauldronRecipeRegistry;
import wtf.choco.alchema.essence.EntityEssenceData;
import wtf.choco.alchema.essence.EntityEssenceEffectRegistry;
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.alchema.util.AlchemaEventFactory;
import wtf.choco.alchema.util.MathUtil;

/**
 * An implementation of {@link BukkitRunnable} that handles the updating and ticking of
 * in-world {@link AlchemicalCauldron} instances.
 *
 * @author Parker Hawke - Choco
 */
public final class CauldronUpdateTask extends BukkitRunnable {

    private static CauldronUpdateTask instance = null;

    private int currentTick = 0;

    private final List<AlchemicalCauldron> forRemoval = new ArrayList<>(4);

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

        Collection<@NotNull AlchemicalCauldron> cauldrons = cauldronManager.getCauldrons();
        if (cauldrons.isEmpty()) {
            return;
        }

        // Pull configuration values every tick, but only once for every cauldron iteration
        FileConfiguration config = plugin.getConfig();
        int itemSearchInterval = Math.max(config.getInt(AlchemaConstants.CONFIG_CAULDRON_ITEM_SEARCH_INTERVAL, 1), 1);
        int millisecondsToHeatUp = Math.max(config.getInt(AlchemaConstants.CONFIG_CAULDRON_MILLISECONDS_TO_HEAT_UP, 5000), 0);

        boolean damageEntities = config.getBoolean(AlchemaConstants.CONFIG_CAULDRON_ENTITIES_DAMAGE, true);
        int minEssenceOnDeath = Math.max(config.getInt(AlchemaConstants.CONFIG_CAULDRON_ENTITIES_MIN_ESSENCE_ON_DEATH, 50), 0);
        int maxEssenceOnDeath = Math.max(config.getInt(AlchemaConstants.CONFIG_CAULDRON_ENTITIES_MAX_ESSENCE_ON_DEATH, 100), minEssenceOnDeath);

        float volumeAmbientBubble = (float) config.getDouble(AlchemaConstants.CONFIG_CAULDRON_SOUND_AMBIENT_BUBBLE_VOLUME, 0.45);
        float volumeItemSplash = (float) config.getDouble(AlchemaConstants.CONFIG_CAULDRON_SOUND_ITEM_SPLASH_VOLUME, 1.0);
        float volumeSuccessfulCraft = (float) config.getDouble(AlchemaConstants.CONFIG_CAULDRON_SOUND_SUCCESSFUL_CRAFT_VOLUME, 0.5);

        EntityEssenceEffectRegistry essenceEffectRegistry = plugin.getEntityEssenceEffectRegistry();

        for (AlchemicalCauldron cauldron : cauldrons) {
            if (!cauldron.isLoaded()) {
                continue;
            }

            // Remove cauldrons that aren't cauldrons anymore. Ingredients are dropped during removal after this iteration.
            Block block = cauldron.getCauldronBlock();
            if (block.getType() != Material.CAULDRON) {
                this.forRemoval.add(cauldron);
                continue;
            }

            World world = block.getWorld();
            Location location = block.getLocation().add(0.5, 0.25, 0.5);
            Location particleLocation = block.getLocation().add(0.5, 1, 0.5);

            // Unheat if conditions are not met
            if (!cauldron.canHeatUp()) {
                cauldron.stopHeatingUp();
                cauldron.setBubbling(false);

                cauldron.dropIngredients(CauldronIngredientsDropEvent.Reason.UNHEATED, null);
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

                if (!AlchemaEventFactory.handleCauldronBubbleEvent(cauldron)) {
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
            if (currentTick % itemSearchInterval == 0) {
                world.getNearbyEntities(cauldron.getItemConsumptionBounds()).forEach(entity -> {
                    if (entity instanceof Item) {
                        Item item = (Item) entity;
                        if (item.hasMetadata(AlchemaConstants.METADATA_KEY_CAULDRON_CRAFTED)) {
                            return;
                        }

                        ItemStack itemStack = item.getItemStack();

                        // Apparently this can be 0 sometimes on Spigot (I guess due to item merging)
                        int amount = itemStack.getAmount();
                        if (amount <= 0) {
                            return;
                        }

                        // Entity essence
                        CauldronIngredient ingredient = null;
                        if (EntityEssenceData.isVialOfEntityEssence(itemStack)) {
                            EntityType entityType = EntityEssenceData.getEntityEssenceType(itemStack);
                            if (entityType != null) {
                                int essenceAmount = EntityEssenceData.getEntityEssenceAmount(itemStack);
                                ingredient = new CauldronIngredientEntityEssence(entityType, essenceEffectRegistry, essenceAmount);
                            }
                        }

                        if (ingredient == null) {
                            ingredient = new CauldronIngredientItemStack(itemStack, amount);
                        }

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
                        if (currentTick % 20 == 0 && !livingEntity.isDead()) {
                            EntityDamageByCauldronEvent entityDamageByCauldronEvent = AlchemaEventFactory.callEntityDamageByCauldronEvent(livingEntity, cauldron, 1.0);

                            double damage = entityDamageByCauldronEvent.getDamage();
                            if (entityDamageByCauldronEvent.isCancelled() || damage <= 0.0) {
                                return;
                            }

                            livingEntity.setMetadata(AlchemaConstants.METADATA_KEY_DAMAGED_BY_CAULDRON, new FixedMetadataValue(plugin, System.currentTimeMillis()));
                            livingEntity.damage(damage);

                            // Entity died due to cauldron damage. Insert essence into the cauldron
                            if (livingEntity.isDead()) {
                                EntityType type = livingEntity.getType();
                                boolean hasEntityEssenceData = essenceEffectRegistry.hasEntityEssenceData(type);

                                int amountOfEssence = hasEntityEssenceData ? MathUtil.generateNumberBetween(minEssenceOnDeath, maxEssenceOnDeath) : 0;
                                EntityDeathByCauldronEvent entityDeathByCauldronEvent = AlchemaEventFactory.callEntityDeathByCauldronEvent(livingEntity, cauldron, amountOfEssence);
                                amountOfEssence = entityDeathByCauldronEvent.getEssence();

                                if (hasEntityEssenceData && amountOfEssence > 0) {
                                    cauldron.addIngredient(new CauldronIngredientEntityEssence(type, essenceEffectRegistry, amountOfEssence));
                                }
                            }
                        }
                    }
                });
            }

            if (!cauldron.hasIngredients()) {
                continue;
            }

            CauldronRecipe activeRecipe = recipeRegistry.getApplicableRecipe(cauldron.getIngredients());
            if (activeRecipe == null) {
                continue;
            }

            OfflinePlayer lastInteracted = cauldron.getLastInteracted();
            CauldronItemCraftEvent cauldronCraftEvent = AlchemaEventFactory.callCauldronItemCraftEvent(cauldron, activeRecipe, lastInteracted != null ? lastInteracted.getPlayer() : null);
            if (cauldronCraftEvent.isCancelled()) {
                continue;
            }

            ThreadLocalRandom random = ThreadLocalRandom.current();
            Vector itemVelocity = new Vector(random.nextDouble() / 10.0, 0.10 + (random.nextDouble() / 3), random.nextDouble() / 10.0);
            Location resultSpawnLocation = block.getLocation().add(0.5, 1.1, 0.5);

            // Item result
            ItemStack result = cauldronCraftEvent.getResult();
            if (result != null) {
                Item item = world.dropItem(resultSpawnLocation, result);
                item.setVelocity(itemVelocity);
                item.setMetadata(AlchemaConstants.METADATA_KEY_CAULDRON_CRAFTED, new FixedMetadataValue(plugin, true));
            }

            // Experience
            int experience = cauldronCraftEvent.getExperience();
            if (experience > 0) {
                world.spawn(resultSpawnLocation, ExperienceOrb.class, orb -> orb.setExperience(experience));
            }

            cauldron.removeIngredients(activeRecipe);

            world.spawnParticle(Particle.SPELL_WITCH, particleLocation, 10, 0.3, 0.2, 0.3, 0.0);

            if (volumeSuccessfulCraft > 0.0) {
                world.playSound(location, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, volumeSuccessfulCraft, 1.5F);
                world.playSound(location, Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, volumeSuccessfulCraft, 0.8F);
            }
        }

        if (!forRemoval.isEmpty()) {
            this.forRemoval.forEach(cauldron -> {
                cauldron.dropIngredients(CauldronIngredientsDropEvent.Reason.DESTROYED, null);
                this.cauldronManager.removeCauldron(cauldron);
            });

            this.forRemoval.clear();
        }
    }

    /**
     * Start the singleton {@link CauldronUpdateTask}.
     * <p>
     * <strong>NOTE:</strong> This is for internal use only
     *
     * @param plugin the plugin to start the task
     *
     * @return the task instance
     */
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
