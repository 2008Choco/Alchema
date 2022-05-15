package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.essence.EntityEssenceData;
import wtf.choco.alchema.essence.EntityEssenceEffectRegistry;
import wtf.choco.alchema.util.AlchemaConstants;
import wtf.choco.commons.util.NamespacedKeyUtil;

/**
 * A {@link CauldronIngredient} implementation wrapped around an {@link EntityEssenceData}.
 * The item stack must match that of a vial of entity essence.
 *
 * @author Parker Hawke - Choco
 */
public class CauldronIngredientEntityEssence implements CauldronIngredient {

    /** The {@link NamespacedKey} used for this ingredient type */
    public static final NamespacedKey KEY = Alchema.key("entity_essence");

    private final EntityType entityType;
    private final EntityEssenceEffectRegistry essenceEffectRegistry;
    private final int amount;

    /**
     * Construct a new {@link CauldronIngredientEntityEssence} with a given amount.
     *
     * @param entityType the type of entity essence
     * @param essenceEffectRegistry the effect registry
     * @param amount the amount of essence
     */
    public CauldronIngredientEntityEssence(@NotNull EntityType entityType, @NotNull EntityEssenceEffectRegistry essenceEffectRegistry, int amount) {
        Preconditions.checkArgument(entityType != null, "entityType must not be null");
        Preconditions.checkArgument(essenceEffectRegistry != null, "essenceEffectRegistry must not be null");
        Preconditions.checkArgument(amount > 0, "amount must be > 0");

        this.entityType = entityType;
        this.essenceEffectRegistry = essenceEffectRegistry;
        this.amount = amount;
    }

    /**
     * Construct a new {@link CauldronIngredientEntityEssence} with an amount of 1
     *
     * @param entityType the type of entity essence
     * @param essenceEffectRegistry the effect registry
     */
    public CauldronIngredientEntityEssence(@NotNull EntityType entityType, @NotNull EntityEssenceEffectRegistry essenceEffectRegistry) {
        this(entityType, essenceEffectRegistry, 1);
    }

    /**
     * Construct a new {@link CauldronIngredientItemStack} deserialized from the
     * provided {@link JsonObject}.
     *
     * @param object the object from which to deserialize
     * @param essenceEffectRegistry the effect registry
     */
    public CauldronIngredientEntityEssence(@NotNull JsonObject object, @NotNull EntityEssenceEffectRegistry essenceEffectRegistry) {
        Preconditions.checkArgument(object != null, "object must not be null");
        Preconditions.checkArgument(essenceEffectRegistry != null, "essenceEffectRegistry must not be null");

        if (!object.has("entity")) {
            throw new JsonParseException("object does not contain item.");
        }

        NamespacedKey entityKey = NamespacedKeyUtil.fromString(object.get("entity").getAsString(), null);
        if (entityKey == null) {
            throw new JsonParseException("entity has an invalid registry key");
        }

        this.entityType = Registry.ENTITY_TYPE.get(entityKey);
        if (entityType == null) {
            throw new JsonParseException("Could not find entity type with id " + entityType);
        }

        this.essenceEffectRegistry = essenceEffectRegistry;
        this.amount = object.has("amount") ? object.get("amount").getAsInt() : 1;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Nullable
    @Override
    public ItemStack asItemStack() {
        EntityEssenceData essenceData = essenceEffectRegistry.getEntityEssenceData(entityType);
        return essenceData != null ? essenceData.createItemStack(getAmount()) : EntityEssenceData.createEmptyVial();
    }

    @Override
    public boolean isSimilar(@NotNull CauldronIngredient other) {
        return other instanceof CauldronIngredientEntityEssence ingredient && entityType == ingredient.entityType;
    }

    @NotNull
    @Override
    public CauldronIngredient merge(@NotNull CauldronIngredient other) {
        Preconditions.checkArgument(other instanceof CauldronIngredientEntityEssence, "Cannot merge %s with %s", getClass().getName(), other.getClass().getName());
        return new CauldronIngredientEntityEssence(entityType, essenceEffectRegistry, getAmount() + other.getAmount());
    }

    @NotNull
    @Override
    public CauldronIngredient adjustAmountBy(int amount) {
        Preconditions.checkArgument(amount < getAmount(), "amount must be < getAmount(), %d", getAmount());
        return new CauldronIngredientEntityEssence(entityType, essenceEffectRegistry, getAmount() + amount);
    }

    @NotNull
    @Override
    public List<@NotNull Item> drop(@NotNull AlchemicalCauldron cauldron, @NotNull World world, @NotNull Location location) {
        List<@NotNull Item> drops = new ArrayList<>();

        FileConfiguration config = Alchema.getInstance().getConfig();
        int maximumEssence = config.getInt(AlchemaConstants.CONFIG_VIAL_OF_ESSENCE_MAXIMUM_ESSENCE, 1000);

        EntityEssenceData essenceData = essenceEffectRegistry.getEntityEssenceData(entityType);
        if (essenceData == null) {
            drops.add(world.dropItem(location, EntityEssenceData.createEmptyVial((getAmount() / maximumEssence) + 1)));
            return drops;
        }

        for (int essenceToDrop = getAmount(); essenceToDrop > 0; essenceToDrop -= maximumEssence) {
            ItemStack itemStack = essenceData.createItemStack(Math.min(essenceToDrop, maximumEssence));
            drops.add(world.dropItem(location, itemStack));
        }

        return drops;
    }

    @NotNull
    @Override
    public String describe() {
        return amount + "x " + StringUtils.capitalize(entityType.getKey().getKey().replace('_', ' ')) + " essence";
    }

    @NotNull
    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("entity", entityType.getKey().toString());
        object.addProperty("amount", amount);

        return object;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, entityType);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof CauldronIngredientEntityEssence other && amount == other.amount && entityType == other.entityType);
    }

    @Override
    public String toString() {
        return String.format("CauldronIngredientEntityEssence[amount=%s, entityType=%s]", getAmount(), entityType);
    }

}
