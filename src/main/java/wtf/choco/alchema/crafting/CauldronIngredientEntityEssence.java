package wtf.choco.alchema.crafting;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.essence.EntityEssenceData;
import wtf.choco.alchema.essence.EntityEssenceEffectRegistry;
import wtf.choco.alchema.util.NamespacedKeyUtil;

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

    public CauldronIngredientEntityEssence(@NotNull EntityType entityType, @NotNull EntityEssenceEffectRegistry essenceEffectRegistry, int amount) {
        Preconditions.checkArgument(entityType != null, "entityType must not be null");
        Preconditions.checkArgument(essenceEffectRegistry != null, "essenceEffectRegistry must not be null");
        Preconditions.checkArgument(amount > 0, "amount must be > 0");

        this.entityType = entityType;
        this.essenceEffectRegistry = essenceEffectRegistry;
        this.amount = amount;
    }

    public CauldronIngredientEntityEssence(@NotNull EntityType essenceData, @NotNull EntityEssenceEffectRegistry essenceEffectRegistry) {
        this(essenceData, essenceEffectRegistry, 1);
    }

    public CauldronIngredientEntityEssence(@NotNull JsonObject object, @NotNull EntityEssenceEffectRegistry essenceEffectRegistry) {
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
        return essenceData != null ? essenceData.createItemStack(amount) : EntityEssenceData.createEmptyVial();
    }

    @Override
    public boolean isSimilar(@NotNull CauldronIngredient other) {
        if (!(other instanceof CauldronIngredientEntityEssence)) {
            return false;
        }

        CauldronIngredientEntityEssence ingredient = (CauldronIngredientEntityEssence) other;
        return entityType == ingredient.entityType;
    }

    @NotNull
    @Override
    public CauldronIngredient merge(@NotNull CauldronIngredient other) {
        Preconditions.checkArgument(other instanceof CauldronIngredientItemStack, "Cannot merge %s with %s", getClass().getName(), other.getClass().getName());

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
    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("entity", entityType.getKey().toString());
        object.addProperty("amount", getAmount());

        return object;
    }

}
