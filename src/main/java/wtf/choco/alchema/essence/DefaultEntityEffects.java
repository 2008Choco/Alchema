package wtf.choco.alchema.essence;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Represents a set of constant {@link EssenceConsumptionCallback EssenceConsumptionCallbacks}
 * for use in default entity registration for Alchema.
 * <p>
 * Constants in this class are not made public as access to these constants should be done
 * using {@link EntityEssenceData#getConsumptionCallback()} instead. This class exists purely
 * for internal use to avoid writing entity callbacks in the {@link EntityEssenceEffectRegistry}.
 *
 * @author Parker Hawke - Choco
 */
final class DefaultEntityEffects {

    // A
    static final EssenceConsumptionCallback AXOLOTL = (player, essenceData, item, amountOfEssence, potency) -> {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80 + (int) (potency * 60), 0, true));
    };

    // B
    static final EssenceConsumptionCallback BAT = (player, essenceData, item, amountOfEssence, potency) -> {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 + (int) (180 * potency), 0, true));
    };
    static final EssenceConsumptionCallback BEE = null;
    static final EssenceConsumptionCallback BLAZE = (player, essenceData, item, amountOfEssence, potency) -> {
        player.setFireTicks(player.getFireTicks() + (40 + (int) (80 * potency)));
    };

    // C
    static final EssenceConsumptionCallback CAT = (player, essenceData, item, amountOfEssence, potency) -> {
        Random random = ThreadLocalRandom.current();
        Location location = player.getLocation();

        AttributeInstance playerLuckAttribute = player.getAttribute(Attribute.GENERIC_LUCK);
        LootContext context = new LootContext.Builder(location)
                .lootedEntity(player)
                .luck(potency + (playerLuckAttribute != null ? (float) playerLuckAttribute.getValue() : 0F))
                .build();

        Collection<ItemStack> loot = LootTables.CAT_MORNING_GIFT.getLootTable().populateLoot(random, context);
        if (loot.isEmpty() || random.nextDouble() > 0.45) {
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Gross. Tastes like hair ball.");
            return;
        }

        loot.forEach(lootItem -> {
            Item droppedItem = player.getWorld().dropItem(player.getEyeLocation(), lootItem);
            droppedItem.setPickupDelay(20);
            droppedItem.setVelocity(location.getDirection().multiply(0.25));

            World world = location.getWorld();
            if (world != null) {
                world.playSound(location, Sound.ENTITY_CAT_AMBIENT, 1.0F, 1.2F);
            }
        });
    };
    static final EssenceConsumptionCallback CAVE_SPIDER = (player, essenceData, item, amountOfEssence, potency) -> {
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100 + (int) (200 * potency), 0, true));
    };
    static final EssenceConsumptionCallback CHICKEN = (player, essenceData, item, amountOfEssence, potency) -> {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 + (int) (180 * potency), 0, true));
    };
    static final EssenceConsumptionCallback COD = null;
    static final EssenceConsumptionCallback COW = null;
    static final EssenceConsumptionCallback CREEPER = (player, essenceData, item, amountOfEssence, potency) -> {
        double range = 2.0 + (potency * 5.0);
        List<Entity> nearbyEntities = player.getNearbyEntities(range, range, range);

        if (nearbyEntities.isEmpty()) {
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Nothing happened... tasted a bit like gunpowder.");
            return;
        }

        nearbyEntities.forEach(entity -> {
            if (entity == player || !(entity instanceof Mob)) {
                return;
            }

            Location location = entity.getLocation();
            World world = location.getWorld();
            if (world == null) {
                return;
            }

            double width = entity.getWidth() / 4.0;
            double height = entity.getHeight() / 2.0;

            world.spawnParticle(Particle.EXPLOSION_LARGE, location.add(0, height, 0), 4, width, height, width);
            world.spawnParticle(Particle.EXPLOSION_NORMAL, location, 3, width, height, width, 0.01);

            for (float pitch = 1.25F; pitch <= 1.75F; pitch += 0.25F) {
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, pitch);
            }
        });
    };

    // D
    static final EssenceConsumptionCallback DONKEY = null;
    static final EssenceConsumptionCallback DOLPHIN = (player, essenceData, item, amountOfEssence, potency) -> {
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 100 + (int) (300 * potency), 1, true));
    };
    static final EssenceConsumptionCallback DROWNED = null;

    // E
    static final EssenceConsumptionCallback ELDER_GUARDIAN = null;
    static final EssenceConsumptionCallback ENDER_DRAGON = null;
    static final EssenceConsumptionCallback ENDERMAN = (player, essenceData, item, amountOfEssence, potency) -> {
        World world = player.getWorld();
        Location playerLocation = player.getLocation();
        Location randomLocation = playerLocation.clone();

        // Calculate random offset based on potency
        Random random = ThreadLocalRandom.current();
        double x = (random.nextDouble() * 16.0 * potency) - (8.0 * potency);
        double y = (random.nextInt((int) (16.0 * potency) + 1));
        double z = (random.nextDouble() * 16.0 * potency) - (8.0 * potency);
        randomLocation.add(x, y, z);

        // Find lowest ground block
        Block block = world.getBlockAt(randomLocation);
        while (block.getY() > 0 && !(block.getType().isSolid() && block.getRelative(BlockFace.UP).isEmpty() && block.getRelative(0, 2, 0).isEmpty())) {
            block = block.getRelative(BlockFace.DOWN);
        }

        // Sanity checks
        if (!world.isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
            return;
        }

        if (!block.getType().isSolid() || !block.getRelative(BlockFace.UP).isEmpty() || !block.getRelative(0, 2, 0).isEmpty()) {
            return;
        }

        // Teleport
        randomLocation = block.getLocation().add(0.5, 1.0, 0.5);
        randomLocation.setDirection(playerLocation.getDirection());

        player.teleport(randomLocation);
        world.playSound(randomLocation, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        world.playSound(playerLocation, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);

        world.spawnParticle(Particle.PORTAL, playerLocation, 50, 0.25, 0.1, 0.25);
        player.playEffect(EntityEffect.TELEPORT_ENDER);
    };
    static final EssenceConsumptionCallback ENDERMITE = null;
    static final EssenceConsumptionCallback EVOKER = null;

    // F
    static final EssenceConsumptionCallback FOX = null;

    // G
    static final EssenceConsumptionCallback GHAST = null;
    static final EssenceConsumptionCallback GIANT = null;
    static final EssenceConsumptionCallback GLOW_SQUID = (player, essenceData, item, amountOfEssence, potency) -> {
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100 + (int) (300 * potency), 0, true));
    };
    static final EssenceConsumptionCallback GOAT = (player, essenceData, item, amountOfEssence, potency) -> {
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40 + (int) (60 * potency), 0, true));
    };
    static final EssenceConsumptionCallback GUARDIAN = null;

    // H
    static final EssenceConsumptionCallback HOGLIN = null;
    static final EssenceConsumptionCallback HORSE = null;
    static final EssenceConsumptionCallback HUSK = null;

    // I
    static final EssenceConsumptionCallback ILLUSIONER = null;
    static final EssenceConsumptionCallback IRON_GOLEM = null;

    // L
    static final EssenceConsumptionCallback LLAMA = null;

    // M
    static final EssenceConsumptionCallback MAGMA_CUBE = null;
    static final EssenceConsumptionCallback MULE = null;
    static final EssenceConsumptionCallback MUSHROOM_COW = null;

    // O
    static final EssenceConsumptionCallback OCELOT = null;

    // P
    static final EssenceConsumptionCallback PANDA = null;
    static final EssenceConsumptionCallback PARROT = null;
    static final EssenceConsumptionCallback PHANTOM = (player, essenceData, item, amountOfEssence, potency) -> {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40 + (int) (160 * potency), 0, true));
    };
    static final EssenceConsumptionCallback PIG = null;
    static final EssenceConsumptionCallback PIGLIN = null;
    static final EssenceConsumptionCallback PIGLIN_BRUTE = null;
    static final EssenceConsumptionCallback PILLAGER = null;
    static final EssenceConsumptionCallback PLAYER = (player, essenceData, item, amountOfEssence, potency) -> {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0F, 1.0F);
        player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Gross...");
    };
    static final EssenceConsumptionCallback POLAR_BEAR = null;
    static final EssenceConsumptionCallback PUFFERFISH = null;

    // R
    static final EssenceConsumptionCallback RABBIT = null;
    static final EssenceConsumptionCallback RAVAGER = null;

    // S
    static final EssenceConsumptionCallback SALMON = null;
    static final EssenceConsumptionCallback SHEEP = null;
    static final EssenceConsumptionCallback SHULKER = null;
    static final EssenceConsumptionCallback SILVERFISH = null;
    static final EssenceConsumptionCallback SKELETON = null;
    static final EssenceConsumptionCallback SKELETON_HORSE = null;
    static final EssenceConsumptionCallback SLIME = null;
    static final EssenceConsumptionCallback SNOWMAN = null;
    static final EssenceConsumptionCallback SPIDER = null;
    static final EssenceConsumptionCallback SQUID = null;
    static final EssenceConsumptionCallback STRAY = null;
    static final EssenceConsumptionCallback STRIDER = null;

    // T
    static final EssenceConsumptionCallback TRADER_LLAMA = null;
    static final EssenceConsumptionCallback TROPICAL_FISH = null;
    static final EssenceConsumptionCallback TURTLE = null;

    // V
    static final EssenceConsumptionCallback VEX = null;
    static final EssenceConsumptionCallback VILLAGER = null;
    static final EssenceConsumptionCallback VINDICATOR = null;

    // W
    static final EssenceConsumptionCallback WANDERING_TRADER = null;
    static final EssenceConsumptionCallback WITCH = null;
    static final EssenceConsumptionCallback WITHER = null;
    static final EssenceConsumptionCallback WITHER_SKELETON = null;
    static final EssenceConsumptionCallback WOLF = null;

    // Z
    static final EssenceConsumptionCallback ZOGLIN = null;
    static final EssenceConsumptionCallback ZOMBIE = null;
    static final EssenceConsumptionCallback ZOMBIE_HORSE = null;
    static final EssenceConsumptionCallback ZOMBIE_VILLAGER = null;
    static final EssenceConsumptionCallback ZOMBIFIED_PIGLIN = null;

    private DefaultEntityEffects() { }

}
