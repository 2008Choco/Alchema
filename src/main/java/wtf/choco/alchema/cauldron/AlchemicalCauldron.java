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
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.entity.Item;
import org.bukkit.util.BoundingBox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.crafting.CauldronIngredient;
import wtf.choco.alchema.crafting.CauldronRecipe;
import wtf.choco.alchema.crafting.CauldronRecipeRegistry;
import wtf.choco.alchema.util.NamespacedKeyUtil;

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

    private Block cauldronBlock, fireBlock;
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
        this.fireBlock = block.getRelative(BlockFace.DOWN);
        this.itemConsumptionBounds = new BoundingBox(
            block.getX() + 0.125, block.getY() + 0.125, block.getZ() + 0.125,
            block.getX() + 1 - 0.125, block.getY() + 1 - 0.125, block.getZ() + 1 - 0.125
        );

        this.heatingStartTime = hasValidHeatSource() ? System.currentTimeMillis() : -1;
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
     */
    @NotNull
    public Block getFireBlock() {
        return fireBlock;
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
        Predicate<@NotNull BlockData> heatSourcePredicate = HEAT_SOURCE_BLOCKS.get(fireBlock.getType());
        return heatSourcePredicate != null && heatSourcePredicate.test(fireBlock.getBlockData());
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
     * Get the time in millis (according to {@link System#currentTimeMillis()}) at which this
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
     * Clear all ingredients from this cauldron.
     */
    public void clearIngredients() {
        this.ingredients.clear();
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