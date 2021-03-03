package wtf.choco.alchema.cauldron;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.api.event.CauldronIngredientAddEvent;
import wtf.choco.alchema.api.event.CauldronIngredientsDropEvent;
import wtf.choco.alchema.api.event.CauldronItemCraftEvent;
import wtf.choco.alchema.api.event.entity.EntityDamageByCauldronEvent;
import wtf.choco.alchema.api.event.entity.EntityDeathByCauldronEvent;
import wtf.choco.alchema.config.CauldronConfigurationContext;
import wtf.choco.alchema.crafting.CauldronIngredient;
import wtf.choco.alchema.crafting.CauldronIngredientEntityEssence;
import wtf.choco.alchema.crafting.CauldronIngredientItemStack;
import wtf.choco.alchema.crafting.CauldronRecipe;
import wtf.choco.alchema.crafting.CauldronRecipeRegistry;
import wtf.choco.alchema.essence.EntityEssenceData;
import wtf.choco.alchema.essence.EntityEssenceEffectRegistry;
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.alchema.util.AlchemaEventFactory;
import wtf.choco.commons.util.MathUtil;
import wtf.choco.commons.util.NamespacedKeyUtil;

/**
 * Represents a special cauldron provided by Alchema. These cauldrons require a source
 * of heat, therefore a lit flame must be present below the cauldron before it may be
 * used to brew recipes.
 *
 * @author Parker Hawke - 2008Choco
 */
public class AlchemicalCauldron {

    private static final Map<@NotNull Material, @NotNull Predicate<@NotNull BlockData>> HEAT_SOURCE_BLOCKS = new EnumMap<>(Material.class);
    static {
        HEAT_SOURCE_BLOCKS.put(Material.FIRE, Predicates.alwaysTrue());
        HEAT_SOURCE_BLOCKS.put(Material.SOUL_FIRE, Predicates.alwaysTrue());
        HEAT_SOURCE_BLOCKS.put(Material.CAMPFIRE, blockData -> ((Campfire) blockData).isLit());
        HEAT_SOURCE_BLOCKS.put(Material.SOUL_CAMPFIRE, blockData -> ((Campfire) blockData).isLit());
        HEAT_SOURCE_BLOCKS.put(Material.LAVA, Predicates.alwaysTrue());
    }

    private long heatingStartTime;
    private boolean heatingUp = false, bubbling = false;

    private UUID lastInteracted;

    private Block cauldronBlock, heatSourceBlock;
    private BoundingBox itemConsumptionBounds;

    private final List<@NotNull CauldronIngredient> ingredients = new ArrayList<>();

    /**
     * Construct a new {@link AlchemicalCauldron}.
     *
     * @param block the block at which the cauldron is located
     */
    public AlchemicalCauldron(@NotNull Block block) {
        Preconditions.checkArgument(block.getType() == Material.CAULDRON, "AlchemicalCauldron block type must be CAULDRON");

        this.cauldronBlock = block;
        this.heatSourceBlock = block.getRelative(BlockFace.DOWN);
        this.itemConsumptionBounds = new BoundingBox(
            block.getX() + 0.125, block.getY() + 0.125, block.getZ() + 0.125,
            block.getX() + 1 - 0.125, block.getY() + 1 - 0.125, block.getZ() + 1 - 0.125
        );

        this.heatingStartTime = hasValidHeatSource() ? System.currentTimeMillis() : -1;
    }

    /**
     * Check whether or not this cauldron is in a loaded chunk.
     *
     * @return true if loaded, false otherwise
     */
    public boolean isLoaded() {
        return cauldronBlock.getWorld().isChunkLoaded(cauldronBlock.getX() >> 4, cauldronBlock.getZ() >> 4);
    }

    /**
     * Get the block at which the cauldron resides.
     *
     * @return the cauldron block
     */
    @NotNull
    public Block getCauldronBlock() {
        return cauldronBlock;
    }

    /**
     * Get the {@link Location} of this cauldron.
     *
     * @return the cauldron location
     */
    @NotNull
    public Location getLocation() {
        return cauldronBlock.getLocation();
    }

    /**
     * Get the x coordinate of this alchemical cauldron.
     *
     * @return the x coordinate
     */
    public int getX() {
        return cauldronBlock.getX();
    }

    /**
     * Get the y coordinate of this alchemical cauldron.
     *
     * @return the y coordinate
     */
    public int getY() {
        return cauldronBlock.getY();
    }

    /**
     * Get the z coordinate of this alchemical cauldron.
     *
     * @return the z coordinate
     */
    public int getZ() {
        return cauldronBlock.getZ();
    }

    /**
     * Get the {@link World} in which this cauldron resides.
     *
     * @return the cauldron's world
     */
    @NotNull
    public World getWorld() {
        return cauldronBlock.getWorld();
    }

    /**
     * Get the block used to ignite the cauldron (below {@link #getCauldronBlock()}, y-1).
     *
     * @return the fire block
     *
     * @deprecated poor naming as heat sources can be more than just fire. See
     * {@link #getHeatSourceBlock()} instead. This method will be removed in the near future.
     */
    @NotNull
    @Deprecated
    public Block getFireBlock() {
        return heatSourceBlock;
    }

    /**
     * Get the block used as the heat source for the cauldron (below {@link #getCauldronBlock()},
     * block y - 1)
     *
     * @return the heat source block
     */
    @NotNull
    public Block getHeatSourceBlock() {
        return heatSourceBlock;
    }

    /**
     * Get the {@link Location} of this cauldron's heat source.
     *
     * @return the heat source location
     */
    @NotNull
    public Location getHeatSourceLocation() {
        return heatSourceBlock.getLocation();
    }

    /**
     * Get the x coordinate of this cauldron's heat source.
     *
     * @return the x coordinate
     */
    public int getHeatSourceX() {
        return heatSourceBlock.getX();
    }

    /**
     * Get the y coordinate of this cauldron's heat source.
     *
     * @return the y coordinate
     */
    public int getHeatSourceY() {
        return heatSourceBlock.getY();
    }

    /**
     * Get the z coordinate of this cauldron's heat source.
     *
     * @return the z coordinate
     */
    public int getHeatSourceZ() {
        return heatSourceBlock.getZ();
    }

    /**
     * Get the {@link BoundingBox} in which {@link Item} instances will be consumed by the
     * cauldron and considered an ingredient.
     *
     * @return the consumption bounds
     */
    @NotNull
    public BoundingBox getItemConsumptionBounds() {
        return itemConsumptionBounds;
    }

    /**
     * Set the {@link OfflinePlayer} that last interacted with this cauldron.
     *
     * @param player the player to set
     */
    public void setLastInteracted(@Nullable OfflinePlayer player) {
        this.lastInteracted = player != null ? player.getUniqueId() : null;
    }

    /**
     * Get the {@link OfflinePlayer} that last interacted with this cauldron.
     *
     * @return the player that last interacted
     */
    @Nullable
    public OfflinePlayer getLastInteracted() {
        return lastInteracted != null ? Bukkit.getOfflinePlayer(lastInteracted) : null;
    }

    /**
     * Check whether or not this cauldron has a valid heat source.
     *
     * @return true if a valid heat source is present, false otherwise
     */
    public boolean hasValidHeatSource() {
        Predicate<@NotNull BlockData> heatSourcePredicate = HEAT_SOURCE_BLOCKS.get(heatSourceBlock.getType());
        return heatSourcePredicate != null && heatSourcePredicate.test(heatSourceBlock.getBlockData());
    }

    /**
     * Check whether or not this cauldron may be heated up.
     *
     * @return true if heating is possible, false otherwise
     */
    public boolean canHeatUp() {
        if (cauldronBlock.getType() != Material.CAULDRON) {
            return false;
        }

        Levelled cauldron = (Levelled) cauldronBlock.getBlockData();
        return cauldron.getLevel() == cauldron.getMaximumLevel() && hasValidHeatSource();
    }

    /**
     * Attempt to heat this cauldron.
     *
     * @return true if the attempt is successful and heating has started, false otherwise
     */
    public boolean attemptToHeatUp() {
        if (heatingUp || bubbling || !canHeatUp()) {
            return false;
        }

        this.heatingStartTime = System.currentTimeMillis();
        this.heatingUp = true;
        return true;
    }

    /**
     * Check whether or not this cauldron is currently heating up.
     *
     * @return true if heating up, false otherwise
     */
    public boolean isHeatingUp() {
        return heatingUp;
    }

    /**
     * Stop this cauldron from heating up.
     */
    public void stopHeatingUp() {
        this.heatingStartTime = -1;
        this.heatingUp = false;
    }

    /**
     * Get the time in milliseconds (according to {@link System#currentTimeMillis()}) at which this
     * cauldron started heating up. If the cauldron is not heating up (i.e. {@link #isHeatingUp()}
     * == false), this will return -1.
     *
     * @return the heating start time. -1 if the cauldron is not heating up
     */
    public long getHeatingStartTime() {
        return heatingStartTime;
    }

    /**
     * Set whether or not this cauldron is bubbling.
     *
     * @param bubbling the new bubbling state
     */
    public void setBubbling(boolean bubbling) {
        this.bubbling = bubbling;
    }

    /**
     * Check whether or not this cauldron is bubbling.
     *
     * @return true if bubbling, false otherwise
     */
    public boolean isBubbling() {
        return bubbling;
    }

    /**
     * Add the ingredient to this cauldron. If the ingredient matches that of
     * another ingredient already in this cauldron, it will be merged.
     *
     * @param ingredient the ingredient to add
     */
    public void addIngredient(@NotNull CauldronIngredient ingredient) {
        int existingIndex = -1;

        for (int i = 0; i < ingredients.size(); i++) {
            CauldronIngredient cauldronIngredient = ingredients.get(i);
            if (cauldronIngredient.isSimilar(ingredient)) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex != -1) {
            // If possible, merge existing ingredients to not overflow the cauldron with many of the same type
            this.ingredients.set(existingIndex, ingredients.get(existingIndex).merge(ingredient));
        } else {
            this.ingredients.add(ingredient);
        }
    }

    /**
     * Remove the ingredients listed by the provided {@link CauldronRecipe}.
     *
     * @param recipe the recipe whose ingredients should be removed
     */
    public void removeIngredients(@NotNull CauldronRecipe recipe) {
        recipe.getIngredients().forEach(recipeIngredient -> {
            for (int i = 0; i < ingredients.size(); i++) {
                CauldronIngredient cauldronIngredient = ingredients.get(i);
                if (!recipeIngredient.isSimilar(cauldronIngredient)) {
                    continue;
                }

                int recipeIngredientAmount = recipeIngredient.getAmount();
                int cauldronIngredientAmount = cauldronIngredient.getAmount();

                if (recipeIngredientAmount >= cauldronIngredientAmount) {
                    this.ingredients.remove(i--); // Adjust value of i to the new index
                } else {
                    this.ingredients.set(i, cauldronIngredient.adjustAmountBy(-recipeIngredientAmount));
                }
            }
        });
    }

    /**
     * Check whether or not this cauldron has ANY ingredients.
     *
     * @return true if at least one ingredient is present, false otherwise
     */
    public boolean hasIngredients() {
        return !ingredients.isEmpty();
    }

    /**
     * Get the ingredients present in this cauldron. Changes made to this List will not affect
     * the contents of this cauldron.
     *
     * @return the ingredients
     */
    @NotNull
    public List<@NotNull CauldronIngredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    /**
     * Clear all ingredients from this cauldron and drop them into the world.
     *
     * @param reason the reason for the items to be dropped. null if none
     * @param player the player that caused the ingredients to drop. null if none
     *
     * @return true if ingredients were cleared, false if cancelled by the
     * {@link CauldronIngredientsDropEvent}
     */
    public boolean dropIngredients(@Nullable CauldronIngredientsDropEvent.Reason reason, @Nullable Player player) {
        if (!hasIngredients()) {
            return true;
        }

        List<@NotNull Item> items = new ArrayList<>();
        this.getIngredients().forEach(ingredient -> items.addAll(ingredient.drop(this, getWorld(), getLocation().add(0.5, 0.5, 0.5))));

        CauldronIngredientsDropEvent ingredientsDropEvent = AlchemaEventFactory.callCauldronIngredientsDropEvent(this, items, player, reason);
        if (ingredientsDropEvent.isCancelled()) {
            items.forEach(Item::remove);
            return false;
        }

        this.ingredients.clear();
        return true;
    }

    /**
     * Clear all ingredients from this cauldron.
     */
    public void clearIngredients() {
        this.ingredients.clear();
    }

    /**
     * Update this cauldron.
     *
     * @param plugin the alchema plugin instance
     * @param cauldronConfiguration the cauldron configuration
     * @param currentTick the current update tick
     */
    protected void update(@NotNull Alchema plugin, @NotNull CauldronConfigurationContext cauldronConfiguration, int currentTick) {
        Preconditions.checkArgument(plugin != null, "plugin must not be null");
        Preconditions.checkArgument(cauldronConfiguration != null, "cauldronConfiguration must not be null");

        World world = getWorld();
        Location location = getLocation().add(0.5, 0.25, 0.5);
        Location particleLocation = getLocation().add(0.5, 1, 0.5);

        // Unheat if conditions are not met
        if (!canHeatUp()) {
            this.stopHeatingUp();
            this.setBubbling(false);

            this.dropIngredients(CauldronIngredientsDropEvent.Reason.UNHEATED, null);
            return;
        }

        // Attempt to heat cauldron (if valid)
        if (!isBubbling() && !isHeatingUp() && !attemptToHeatUp()) {
            return;
        }

        // Prepare bubbling cauldrons
        if (isHeatingUp()) {
            long timeSinceHeatingUp = System.currentTimeMillis() - getHeatingStartTime();
            if (timeSinceHeatingUp < cauldronConfiguration.getMillisecondsToHeatUp()) {
                return;
            }

            if (!AlchemaEventFactory.handleCauldronBubbleEvent(this)) {
                return;
            }

            this.stopHeatingUp();
            this.setBubbling(true);
        }

        world.spawnParticle(Particle.BUBBLE_COLUMN_UP, getLocation().add(0.5, 0.95, 0.5), 2, 0.15F, 0F, 0.15F, 0F);
        if (currentTick % 40 == 0 && cauldronConfiguration.getAmbientBubbleVolume() > 0.0) {
            world.playSound(location, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, cauldronConfiguration.getAmbientBubbleVolume(), 0.8F);
        }

        // Dissolve items in bubbling cauldrons
        if (currentTick % cauldronConfiguration.getItemSearchInterval() == 0) {
            EntityEssenceEffectRegistry essenceEffectRegistry = plugin.getEntityEssenceEffectRegistry();

            world.getNearbyEntities(getItemConsumptionBounds()).forEach(entity -> {
                if (entity instanceof Item) {
                    Item item = (Item) entity;
                    if (item.hasMetadata(AlchemaConstants.METADATA_KEY_CAULDRON_CRAFTED)) {
                        return;
                    }

                    // Don't collect non-player-sourced items (configuration based)
                    UUID itemThrowerUUID = item.getThrower();
                    if (cauldronConfiguration.shouldEnforcePlayerDroppedItems() && (itemThrowerUUID == null || Bukkit.getPlayer(itemThrowerUUID) != null)) {
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

                    CauldronIngredientAddEvent ingredientAddEvent = AlchemaEventFactory.callCauldronIngredientAddEvent(this, ingredient, item);

                    this.addIngredient(ingredientAddEvent.getIngredient());
                    item.remove();

                    world.spawnParticle(Particle.WATER_SPLASH, particleLocation, 4);

                    if (cauldronConfiguration.getItemSplashVolume() > 0.0) {
                        world.playSound(location, Sound.ENTITY_PLAYER_SPLASH, cauldronConfiguration.getItemSplashVolume(), 2F);
                    }
                }
                else if (cauldronConfiguration.shouldDamageEntities() && entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (currentTick % 20 == 0 && !livingEntity.isDead()) {
                        EntityDamageByCauldronEvent entityDamageByCauldronEvent = AlchemaEventFactory.callEntityDamageByCauldronEvent(livingEntity, this, 1.0);

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

                            int amountOfEssence = hasEntityEssenceData ? MathUtil.generateNumberBetween(cauldronConfiguration.getMinEssenceOnDeath(), cauldronConfiguration.getMaxEssenceOnDeath()) : 0;
                            EntityDeathByCauldronEvent entityDeathByCauldronEvent = AlchemaEventFactory.callEntityDeathByCauldronEvent(livingEntity, this, amountOfEssence);
                            amountOfEssence = entityDeathByCauldronEvent.getEssence();

                            if (hasEntityEssenceData && amountOfEssence > 0) {
                                this.addIngredient(new CauldronIngredientEntityEssence(type, essenceEffectRegistry, amountOfEssence));
                            }
                        }
                    }
                }
            });
        }

        if (!hasIngredients()) {
            return;
        }

        CauldronRecipeRegistry recipeRegistry = plugin.getRecipeRegistry();
        CauldronRecipe activeRecipe = recipeRegistry.getApplicableRecipe(getIngredients(), true);
        if (activeRecipe == null) {
            return;
        }

        OfflinePlayer lastInteracted = getLastInteracted();
        CauldronItemCraftEvent cauldronCraftEvent = AlchemaEventFactory.callCauldronItemCraftEvent(this, activeRecipe, lastInteracted != null ? lastInteracted.getPlayer() : null);
        if (cauldronCraftEvent.isCancelled()) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        Vector itemVelocity = new Vector(random.nextDouble() / 10.0, 0.10 + (random.nextDouble() / 3), random.nextDouble() / 10.0);
        Location resultSpawnLocation = getLocation().add(0.5, 1.1, 0.5);

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

        this.removeIngredients(activeRecipe);

        world.spawnParticle(Particle.SPELL_WITCH, particleLocation, 10, 0.3, 0.2, 0.3, 0.0);

        if (cauldronConfiguration.getSuccessfulCraftVolume() > 0.0) {
            world.playSound(location, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, cauldronConfiguration.getSuccessfulCraftVolume(), 1.5F);
            world.playSound(location, Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, cauldronConfiguration.getSuccessfulCraftVolume(), 0.8F);
        }
    }

    /**
     * Write and serialize this object into the given {@link JsonObject}.
     *
     * @param object the object in which to write
     *
     * @return the modified object
     */
    @NotNull
    public JsonObject write(@NotNull JsonObject object) {
        Preconditions.checkArgument(object != null, "object cannot be null");

        object.addProperty("heatingStartTime", heatingStartTime);
        object.addProperty("heatingUp", heatingUp);
        object.addProperty("bubbling", bubbling);
        object.addProperty("world", getWorld().getUID().toString());

        JsonObject cauldronBlockObject = new JsonObject();
        cauldronBlockObject.addProperty("x", cauldronBlock.getX());
        cauldronBlockObject.addProperty("y", cauldronBlock.getY());
        cauldronBlockObject.addProperty("z", cauldronBlock.getZ());
        object.add("cauldron", cauldronBlockObject);

        if (ingredients.size() > 0) {
            JsonArray ingredientsArray = new JsonArray();
            this.ingredients.forEach(ingredient -> {
                JsonObject ingredientObject = ingredient.toJson();
                ingredientObject.addProperty("type", ingredient.getKey().toString());
                ingredientsArray.add(ingredientObject);
            });
            object.add("ingredients", ingredientsArray);
        }

        return object;
    }

    /**
     * Read the contents of the provided {@link JsonObject} into a new {@link AlchemicalCauldron}
     * instance.
     *
     * @param object the object from which to read
     * @param recipeRegistry the recipe registry
     *
     * @return the cauldron
     */
    @Nullable
    public static AlchemicalCauldron fromJson(@NotNull JsonObject object, @NotNull CauldronRecipeRegistry recipeRegistry) {
        Preconditions.checkArgument(object != null, "object must not be null");
        Preconditions.checkArgument(recipeRegistry != null, "recipeRegistry must not be null");

        UUID worldUUID = object.has("world") ? UUID.fromString(object.get("world").getAsString()) : null;
        World world = worldUUID != null ? Bukkit.getWorld(worldUUID) : null;
        if (world == null) {
            throw new JsonParseException("World could not be deserialized for cauldron.");
        }

        JsonObject cauldronBlockObject = object.has("cauldron") ? object.getAsJsonObject("cauldron") : null;
        if (cauldronBlockObject == null) {
            throw new JsonParseException("Location could not be deserialized for cauldron.");
        }

        int x = cauldronBlockObject.get("x").getAsInt();
        int y = cauldronBlockObject.get("y").getAsInt();
        int z = cauldronBlockObject.get("z").getAsInt();
        Block block = world.getBlockAt(x, y, z);

        if (block.getType() != Material.CAULDRON) {
            return null;
        }

        AlchemicalCauldron cauldron = new AlchemicalCauldron(block);
        cauldron.heatingStartTime = object.has("heatingStartTime") ? object.get("heatingStartTime").getAsLong() : -1;
        cauldron.heatingUp = object.has("heatingUp") && object.get("heatingUp").getAsBoolean();
        cauldron.bubbling = object.has("bubbling") && object.get("bubbling").getAsBoolean();

        // Parse ingredients
        if (object.has("ingredients")) {
            JsonArray ingredientsArray = object.getAsJsonArray("ingredients");
            for (int i = 0; i < ingredientsArray.size(); i++) {
                JsonElement ingredientElement = ingredientsArray.get(i);
                if (!ingredientElement.isJsonObject()) {
                    continue;
                }

                JsonObject ingredientObject = ingredientElement.getAsJsonObject();
                if (!ingredientObject.has("type")) {
                    throw new JsonParseException("ingredient at index " + i + " does not have an ingredient type");
                }

                NamespacedKey typeKey = NamespacedKeyUtil.fromString(ingredientObject.get("type").getAsString(), Alchema.getInstance());
                if (typeKey == null) {
                    throw new JsonParseException("Invalid namespaced key \"" + typeKey + "\". Expected format is \"alchema:example\"");
                }

                CauldronIngredient ingredient = recipeRegistry.parseIngredientType(typeKey, ingredientObject);
                if (ingredient == null) {
                    throw new JsonParseException("Could not find ingredient type with id \"" + typeKey + "\"");
                }

                cauldron.addIngredient(ingredient);
            }
        }

        return cauldron;
    }

    @Override
    public int hashCode() {
        return 31 * (cauldronBlock == null ? 0 : cauldronBlock.hashCode());
    }

    @Override
    public boolean equals(Object object) {
        return object == this || (object instanceof AlchemicalCauldron
            && Objects.equals(cauldronBlock, ((AlchemicalCauldron) object).cauldronBlock));
    }

}
