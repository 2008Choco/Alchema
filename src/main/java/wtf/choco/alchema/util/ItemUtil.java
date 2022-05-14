package wtf.choco.alchema.util;

import com.google.common.base.Enums;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import wtf.choco.commons.util.NamespacedKeyUtil;

/**
 * A series of utilities pertaining to {@link ItemStack ItemStacks}.
 *
 * @author Parker Hawke - Choco
 */
public final class ItemUtil {

    private ItemUtil() { }

    /**
     * Serialize an {@link ItemStack} to a {@link JsonObject}.
     *
     * @param item the item to serialize
     *
     * @return the object into which the item was serialized
     */
    @NotNull
    public static JsonObject serializeItemStack(@NotNull ItemStack item) {
        Preconditions.checkArgument(item != null, "item must not be null");

        JsonObject object = new JsonObject();

        object.addProperty("item", item.getType().getKey().toString());
        object.addProperty("amount", item.getAmount());

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return object;
        }

        // Base meta (ItemMeta)
        if (meta.hasDisplayName()) {
            object.addProperty("name", meta.getDisplayName().replace(ChatColor.COLOR_CHAR, '&'));
        }

        List<String> lore = meta.getLore();
        if (lore != null && !lore.isEmpty()) {
            JsonArray loreArray = new JsonArray();
            lore.forEach(loreLine -> loreArray.add(loreLine.replace(ChatColor.COLOR_CHAR, '&')));
            object.add("lore", loreArray);
        }

        Map<Enchantment, Integer> enchantments = meta.getEnchants();
        if (enchantments.size() > 0) {
            JsonObject enchantmentObject = new JsonObject();
            enchantments.forEach((enchantment, level) -> enchantmentObject.addProperty(enchantment.getKey().toString(), level));
            object.add("enchantments", enchantmentObject);
        }

        if (meta instanceof Damageable) {
            int damage = ((Damageable) meta).getDamage();
            if (damage > 0) {
                object.addProperty("damage", damage);
            }
        }

        if (meta.isUnbreakable()) {
            object.addProperty("unbreakable", meta.isUnbreakable());
        }

        if (meta.hasCustomModelData()) {
            object.addProperty("custom_model_data", meta.getCustomModelData());
        }

        Multimap<Attribute, AttributeModifier> attributeModifiers = meta.getAttributeModifiers();
        if (attributeModifiers != null && attributeModifiers.size() > 0) {
            JsonObject attributeModifiersObject = new JsonObject();

            attributeModifiers.forEach((attribute, modifier) -> {
                JsonObject modifierObject = new JsonObject();

                modifierObject.addProperty("name", modifier.getName());
                modifierObject.addProperty("operation", modifier.getOperation().name());
                modifierObject.addProperty("value", modifier.getAmount());
                modifierObject.addProperty("uuid", modifier.getUniqueId().toString());

                EquipmentSlot slot = modifier.getSlot();
                if (slot != null) {
                    modifierObject.addProperty("slot", slot.name());
                }

                attributeModifiersObject.add(attribute.name(), modifierObject);
            });

            object.add("attribute_modifiers", attributeModifiersObject);
        }

        Set<ItemFlag> itemFlags = meta.getItemFlags();
        if (itemFlags.size() > 0) {
            JsonArray itemFlagsArray = new JsonArray();
            itemFlags.forEach(itemFlag -> itemFlagsArray.add(itemFlag.name()));
            object.add("item_flags", itemFlagsArray);
        }

        // Banners (BannerMeta)
        if (meta instanceof BannerMeta) {
            BannerMeta metaSpecific = (BannerMeta) meta;

            List<Pattern> patterns = metaSpecific.getPatterns();
            if (patterns.size() > 0) {
                JsonArray patternsArray = new JsonArray();

                patterns.forEach(pattern -> {
                    JsonObject patternObject = new JsonObject();

                    patternObject.addProperty("pattern", pattern.getPattern().name());
                    patternObject.addProperty("color", pattern.getColor().name());

                    patternsArray.add(patternObject);
                });

                object.add("patterns", patternsArray);
            }
        }

        // Books (BookMeta)
        else if (meta instanceof BookMeta) {
            BookMeta metaSpecific = (BookMeta) meta;

            if (metaSpecific.hasAuthor()) {
                object.addProperty("author", metaSpecific.getAuthor());
            }

            if (metaSpecific.hasTitle()) {
                object.addProperty("title", metaSpecific.getTitle());
            }

            BookMeta.Generation generation = metaSpecific.getGeneration();
            if (generation != null) {
                object.addProperty("generation", generation.name());
            }

            List<String> pages = metaSpecific.getPages();
            if (pages.size() > 0) {
                JsonArray pagesArray = new JsonArray();
                pages.forEach(page -> pagesArray.add(page.replace(ChatColor.COLOR_CHAR, '&')));
                object.add("pages", pagesArray);
            }
        }

        // Firework effects (FireworkEffectMeta)
        else if (meta instanceof FireworkEffectMeta) {
            FireworkEffectMeta metaSpecific = (FireworkEffectMeta) meta;
            FireworkEffect effect = metaSpecific.getEffect();

            if (effect != null) {
                object.addProperty("effect", effect.getType().name());
                object.addProperty("flicker", effect.hasFlicker());
                object.addProperty("trail", effect.hasTrail());

                List<Color> primaryColors = effect.getColors(), fadeColors = effect.getFadeColors();
                if (primaryColors.size() > 0 || fadeColors.size() > 0) {
                    JsonObject colorObject = new JsonObject();

                    if (primaryColors.size() > 0) {
                        JsonArray primaryColorArray = new JsonArray();
                        primaryColors.forEach(color -> primaryColorArray.add(color.asRGB()));
                        colorObject.add("primary", primaryColorArray);
                    }

                    if (fadeColors.size() > 0) {
                        JsonArray fadeColorArray = new JsonArray();
                        fadeColors.forEach(color -> fadeColorArray.add(color.asRGB()));
                        colorObject.add("fade", fadeColorArray);
                    }

                    object.add("color", colorObject);
                }
            }
        }

        // Firework rockets (FireworkMeta)
        else if (meta instanceof FireworkMeta) {
            FireworkMeta metaSpecific = (FireworkMeta) meta;

            object.addProperty("power", metaSpecific.getPower());

            List<FireworkEffect> effects = metaSpecific.getEffects();
            if (effects.size() > 0) {
                JsonArray effectsArray = new JsonArray();

                effects.forEach(effect -> {
                    JsonObject effectObject = new JsonObject();

                    effectObject.addProperty("effect", effect.getType().name());
                    effectObject.addProperty("flicker", effect.hasFlicker());
                    effectObject.addProperty("trail", effect.hasTrail());

                    List<Color> primaryColors = effect.getColors(), fadeColors = effect.getFadeColors();
                    if (primaryColors.size() > 0 || fadeColors.size() > 0) {
                        JsonObject colorObject = new JsonObject();

                        if (primaryColors.size() > 0) {
                            JsonArray primaryColorArray = new JsonArray();
                            primaryColors.forEach(color -> primaryColorArray.add(color.asRGB()));
                            colorObject.add("primary", primaryColorArray);
                        }

                        if (fadeColors.size() > 0) {
                            JsonArray fadeColorArray = new JsonArray();
                            fadeColors.forEach(color -> fadeColorArray.add(color.asRGB()));
                            colorObject.add("fade", fadeColorArray);
                        }

                        effectObject.add("color", colorObject);
                    }

                    effectsArray.add(effectObject);
                });

                object.add("effects", effectsArray);
            }
        }

        // Knowledge book (KnowledgeBookMeta)
        else if (meta instanceof KnowledgeBookMeta) {
            KnowledgeBookMeta metaSpecific = (KnowledgeBookMeta) meta;

            List<NamespacedKey> recipes = metaSpecific.getRecipes();
            if (recipes.size() > 0) {
                JsonArray recipesArray = new JsonArray();
                recipes.forEach(recipe -> recipesArray.add(recipe.toString()));
                object.add("recipes", recipesArray);
            }
        }

        // Leather armor (LeatherArmorMeta)
        else if (meta instanceof LeatherArmorMeta) {
            object.addProperty("color", ((LeatherArmorMeta) meta).getColor().asRGB());
        }

        // Maps (MapMeta)
        else if (meta instanceof MapMeta) {
            MapMeta metaSpecific = (MapMeta) meta;

            Color color = metaSpecific.getColor();
            if (color != null) {
                object.addProperty("color", color.asRGB());
            }

            String location = metaSpecific.getLocationName();
            if (location != null) {
                object.addProperty("location", location);
            }

            object.addProperty("scaling", metaSpecific.isScaling());
        }

        // Potions (PotionMeta)
        else if (meta instanceof PotionMeta) {
            PotionMeta metaSpecific = (PotionMeta) meta;

            PotionData basePotionData = metaSpecific.getBasePotionData();
            object.addProperty("base", basePotionData.getType().name());
            object.addProperty("upgraded", basePotionData.isUpgraded());
            object.addProperty("extended", basePotionData.isExtended());

            Color color = metaSpecific.getColor();
            if (color != null) {
                object.addProperty("color", color.asRGB());
            }

            List<PotionEffect> effects = metaSpecific.getCustomEffects();
            if (effects.size() > 0) {
                JsonObject effectsObject = new JsonObject();

                effects.forEach(effect -> {
                    JsonObject effectDataObject = new JsonObject();

                    effectDataObject.addProperty("duration", effect.getDuration());
                    effectDataObject.addProperty("amplifier", effect.getAmplifier());
                    effectDataObject.addProperty("ambient", effect.isAmbient());
                    effectDataObject.addProperty("particles", effect.hasParticles());
                    effectDataObject.addProperty("icon", effect.hasIcon());

                    effectsObject.add(effect.getType().getName(), effectDataObject);
                });

                object.add("effects", effectsObject);
            }
        }

        // Player skulls (SkullMeta)
        if (meta instanceof SkullMeta) {
            OfflinePlayer owner = ((SkullMeta) meta).getOwningPlayer();
            if (owner != null) {
                object.addProperty("owner", owner.getUniqueId().toString());
            }
        }

        // Suspicious stew (SuspiciousStewMeta)
        if (meta instanceof SuspiciousStewMeta) {
            SuspiciousStewMeta metaSpecific = (SuspiciousStewMeta) meta;

            List<PotionEffect> effects = metaSpecific.getCustomEffects();
            if (effects.size() > 0) {
                JsonObject effectsObject = new JsonObject();

                effects.forEach(effect -> {
                    JsonObject effectDataObject = new JsonObject();

                    effectDataObject.addProperty("duration", effect.getDuration());
                    effectDataObject.addProperty("amplifier", effect.getAmplifier());
                    effectDataObject.addProperty("ambient", effect.isAmbient());
                    effectDataObject.addProperty("particles", effect.hasParticles());
                    effectDataObject.addProperty("icon", effect.hasIcon());

                    effectsObject.add(effect.getType().getName(), effectDataObject);
                });

                object.add("effects", effectsObject);
            }
        }

        // Tropical fish bucket (TropicalFishBucketMeta)
        if (meta instanceof TropicalFishBucketMeta) {
            TropicalFishBucketMeta metaSpecific = (TropicalFishBucketMeta) meta;

            object.addProperty("pattern", metaSpecific.getPattern().name());

            JsonObject colorObject = new JsonObject();
            colorObject.addProperty("body", metaSpecific.getBodyColor().name());
            colorObject.addProperty("pattern", metaSpecific.getPatternColor().name());
            object.add("color", colorObject);
        }

        return object;
    }

    /**
     * Deserialize an {@link ItemStack} from a {@link JsonObject}.
     *
     * @param item the item to which the deserialization should apply
     * @param object the object from which to deserialize an ItemStack
     *
     * @return the deserialized ItemStack
     */
    @NotNull
    @SuppressWarnings("unused") // Eclipse thinks Optional#orNull() never returns null... wot?
    public static ItemStack deserializeItemStack(@NotNull ItemStack item, @NotNull JsonObject object) {
        Preconditions.checkArgument(object != null, "object must not be null");

        // Base meta (ItemMeta)
        if (object.has("amount")) {
            item.setAmount(Math.max(object.get("amount").getAsInt(), 1));
        }

        // This should only happen if the item type is air, at which point no other types of meta applies
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        if (object.has("name")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', object.get("name").getAsString()));
        }

        if (object.has("lore")) {
            JsonElement loreElement = object.get("lore");
            if (!loreElement.isJsonArray()) {
                throw new JsonParseException("Element \"lore\" is of unexpected type. Expected array, got " + loreElement.getClass().getSimpleName());
            }

            List<String> lore = new ArrayList<>();

            for (JsonElement element : loreElement.getAsJsonArray()) {
                if (!element.isJsonPrimitive()) {
                    throw new JsonParseException("Malformated lore. Expected string, got " + element.getClass().getSimpleName());
                }

                lore.add(ChatColor.translateAlternateColorCodes('&', element.getAsString()));
            }

            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
        }

        if (object.has("enchantments")) {
            JsonElement enchantmentsElement = object.get("enchantments");
            if (!enchantmentsElement.isJsonObject()) {
                throw new JsonParseException("Element \"enchantments\" is of unexpected type. Expected object, got " + enchantmentsElement.getClass().getSimpleName());
            }

            JsonObject enchantmentsObject = enchantmentsElement.getAsJsonObject();
            for (Entry<String, JsonElement> enchantmentElement : enchantmentsObject.entrySet()) {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKeyUtil.fromString(enchantmentElement.getKey(), null));
                if (enchantment == null) {
                    throw new JsonParseException("Could not find enchantment with id \"" + enchantmentElement.getKey() + "\". Does it exist?");
                }

                meta.addEnchant(enchantment, enchantmentElement.getValue().getAsInt(), true);
            }
        }

        if (object.has("damage") && meta instanceof Damageable) {
            ((Damageable) meta).setDamage(object.get("damage").getAsInt());
        }

        if (object.has("unbreakable")) {
            meta.setUnbreakable(object.get("unbreakable").getAsBoolean());
        }

        if (object.has("custom_model_data")) {
            meta.setCustomModelData(object.get("custom_model_data").getAsInt());
        }

        if (object.has("attribute_modifiers")) {
            JsonElement modifiersElement = object.get("attribute_modifiers");
            if (!modifiersElement.isJsonObject()) {
                throw new JsonParseException("Element \"attribute_modifiers\" is of unexpected type. Expected object, got " + modifiersElement.getClass().getSimpleName());
            }

            JsonObject modifiersRoot = modifiersElement.getAsJsonObject();
            for (Entry<String, JsonElement> modifierEntry : modifiersRoot.entrySet()) {
                Attribute attribute = Enums.getIfPresent(Attribute.class, modifierEntry.getKey().toUpperCase()).orNull();
                if (attribute == null) {
                    throw new JsonParseException("Unexpected attribute modifier key. Given \"" + modifierEntry.getKey() + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html");
                }

                JsonElement modifierElement = modifierEntry.getValue();
                if (!modifierElement.isJsonObject()) {
                    throw new JsonParseException("Element \"" + modifierEntry.getKey() + "\" is of unexpected type. Expected object, got " + modifiersElement.getClass().getSimpleName());
                }

                JsonObject modifierRoot = modifierElement.getAsJsonObject();
                UUID uuid = modifierRoot.has("uuid") ? UUID.fromString(modifierRoot.get("uuid").getAsString()) : UUID.randomUUID();
                EquipmentSlot slot = modifierRoot.has("slot") ? Enums.getIfPresent(EquipmentSlot.class, modifierRoot.get("slot").getAsString().toUpperCase()).orNull() : null;

                if (!modifierRoot.has("name")) {
                    throw new JsonParseException("Attribute modifier missing element \"name\".");
                }
                if (!modifierRoot.has("value")) {
                    throw new JsonParseException("Attribute modifier missing element \"value\".");
                }
                if (!modifierRoot.has("operation")) {
                    throw new JsonParseException("Attribute modifier missing element \"operation\". Expected \"add_number\", \"add_scalar\" or \"multiply_scalar_1\"");
                }

                String name = modifierRoot.get("name").getAsString();
                double value = modifierRoot.get("value").getAsDouble();
                AttributeModifier.Operation operation = Enums.getIfPresent(AttributeModifier.Operation.class, modifierRoot.get("operation").getAsString().toUpperCase()).orNull();
                if (operation == null) {
                    throw new JsonParseException("Unknown operation for attribute modifier \"" + modifierEntry.getKey() + "\". Expected \"add_number\", \"add_scalar\" or \"multiply_scalar_1\"");
                }

                AttributeModifier modifier = (slot != null) ? new AttributeModifier(uuid, name, value, operation, slot) : new AttributeModifier(uuid, name, value, operation);
                meta.addAttributeModifier(attribute, modifier);
            }
        }

        if (object.has("item_flags")) {
            JsonElement flagsElement = object.get("item_flags");
            if (!flagsElement.isJsonArray()) {
                throw new JsonParseException("Element \"item_flags\" is of unexpected type. Expected array, got " + flagsElement.getClass().getSimpleName());
            }

            flagsElement.getAsJsonArray().forEach(e -> {
                // Guava's Optionals don't have #ifPresent() >:[
                ItemFlag flag = Enums.getIfPresent(ItemFlag.class, e.getAsString().toUpperCase()).orNull();
                if (flag != null) {
                    meta.addItemFlags(flag);
                }
            });
        }

        // Banner meta (BannerMeta)
        if (meta instanceof BannerMeta) {
            BannerMeta metaSpecific = (BannerMeta) meta;

            if (object.has("patterns")) {
                JsonElement patternsElement = object.get("patterns");
                if (!patternsElement.isJsonArray()) {
                    throw new JsonParseException("Element \"patterns\" is of unexpected type. Expected array, got " + patternsElement.getClass().getSimpleName());
                }

                for (JsonElement patternElement : patternsElement.getAsJsonArray()) {
                    if (!patternElement.isJsonObject()) {
                        throw new JsonParseException("Element \"patterns\" has an unexpected type. Expected object, got " + patternElement.getClass().getSimpleName());
                    }

                    JsonObject patternRoot = patternElement.getAsJsonObject();
                    if (!patternRoot.has("color")) {
                        throw new JsonParseException("Pattern missing element \"color\".");
                    }
                    if (!patternRoot.has("pattern")) {
                        throw new JsonParseException("Pattern missing element \"pattern\".");
                    }

                    DyeColor colour = Enums.getIfPresent(DyeColor.class, patternRoot.get("color").getAsString().toUpperCase()).or(DyeColor.WHITE);
                    PatternType pattern = Enums.getIfPresent(PatternType.class, patternRoot.get("pattern").getAsString().toUpperCase()).orNull();
                    if (pattern == null) {
                        throw new JsonParseException("Unexpected value for \"pattern\". Given \"" + object.get("pattern").getAsString() + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/banner/PatternType.html");
                    }

                    metaSpecific.addPattern(new Pattern(colour, pattern));
                }
            }
        }

        // Book meta (BookMeta)
        if (meta instanceof BookMeta) {
            BookMeta metaSpecific = (BookMeta) meta;

            if (object.has("author")) {
                metaSpecific.setAuthor(object.get("author").getAsString());
            }

            if (object.has("title")) {
                metaSpecific.setTitle(object.get("title").getAsString());
            }

            if (object.has("generation")) {
                BookMeta.Generation generation = Enums.getIfPresent(BookMeta.Generation.class, object.get("generation").getAsString().toUpperCase()).orNull();
                if (generation == null) {
                    throw new JsonParseException("Unexpected value for \"generation\". Given \"" + object.get("generation").getAsString() + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/BookMeta.Generation.html");
                }

                metaSpecific.setGeneration(generation);
            }

            if (object.has("pages") && object.get("pages").isJsonArray()) {
                object.getAsJsonArray("pages").forEach(p -> metaSpecific.addPage(p.getAsString()));
            }
        }

        // Firework star meta (FireworkEffectMeta)
        if (meta instanceof FireworkEffectMeta) {
            FireworkEffectMeta metaSpecific = (FireworkEffectMeta) meta;

            if (!object.has("effect")) {
                throw new JsonParseException("Firework effect missing element \"effect\".");
            }

            FireworkEffect.Builder effectBuilder = FireworkEffect.builder();

            FireworkEffect.Type effectType = Enums.getIfPresent(FireworkEffect.Type.class, object.get("effect").getAsString().toUpperCase()).orNull();
            if (effectType == null) {
                throw new JsonParseException("Unexpected value for \"effect\". Given \"" + object.get("effect").getAsString() + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/FireworkEffect.Type.html");
            }

            effectBuilder.with(effectType);
            effectBuilder.flicker(object.has("flicker") ? object.get("flicker").getAsBoolean() : false);
            effectBuilder.flicker(object.has("trail") ? object.get("trail").getAsBoolean() : true);

            if (object.has("color")) {
                JsonElement colourElement = object.get("color");
                if (!colourElement.isJsonObject()) {
                    throw new JsonParseException("Element \"color\" is of unexpected type. Expected object, got " + colourElement.getClass().getSimpleName());
                }

                JsonObject colourRoot = colourElement.getAsJsonObject();
                if (colourRoot.has("primary")) {
                    JsonElement primaryElement = colourRoot.get("primary");

                    if (primaryElement.isJsonPrimitive()) {
                        effectBuilder.withColor(Color.fromRGB(Integer.decode(primaryElement.getAsString())));
                    }

                    else if (primaryElement.isJsonArray()) {
                        JsonArray primaryArray = primaryElement.getAsJsonArray();
                        List<Color> colours = new ArrayList<>(primaryArray.size());
                        primaryArray.forEach(e -> colours.add(Color.fromRGB(Integer.decode(e.getAsString()))));
                        effectBuilder.withColor(colours);
                    }

                    else {
                        throw new JsonParseException("Element \"primary\" is of unexpected type. Expected number (decimal, hex, binary, etc.) or object, got " + primaryElement.getClass().getSimpleName());
                    }
                }

                if (colourRoot.has("fade")) {
                    JsonElement primaryElement = colourRoot.get("fade");

                    if (primaryElement.isJsonPrimitive()) {
                        effectBuilder.withColor(Color.fromRGB(Integer.decode(primaryElement.getAsString())));
                    }

                    else if (primaryElement.isJsonArray()) {
                        JsonArray primaryArray = primaryElement.getAsJsonArray();
                        List<Color> colours = new ArrayList<>(primaryArray.size());
                        primaryArray.forEach(e -> colours.add(Color.fromRGB(Integer.decode(e.getAsString()))));
                        effectBuilder.withColor(colours);
                    }

                    else {
                        throw new JsonParseException("Element \"fade\" is of unexpected type. Expected number (decimal, hex, binary, etc.) or object, got " + primaryElement.getClass().getSimpleName());
                    }
                }
            }

            metaSpecific.setEffect(effectBuilder.build());
        }

        // Firework rocket meta (FireworkMeta)
        if (meta instanceof FireworkMeta) {
            FireworkMeta metaSpecific = (FireworkMeta) meta;

            if (object.has("power")) {
                metaSpecific.setPower(Math.max(object.get("power").getAsInt(), 0));
            }

            if (object.has("effects") && object.get("effects").isJsonArray()) {
                JsonArray effectsArray = object.getAsJsonArray("effects");
                for (JsonElement effectElement : effectsArray) {
                    if (!effectElement.isJsonObject()) {
                        throw new JsonParseException("\"effects\" array element is of unexpected type. Expected object, got " + effectElement.getClass().getSimpleName());
                    }

                    JsonObject effectRoot = effectElement.getAsJsonObject();
                    FireworkEffect.Builder effectBuilder = FireworkEffect.builder();

                    FireworkEffect.Type effectType = Enums.getIfPresent(FireworkEffect.Type.class, effectRoot.get("effect").getAsString().toUpperCase()).orNull();
                    if (effectType == null) {
                        throw new JsonParseException("Unexpected value for \"effect\". Given \"" + effectRoot.get("effect").getAsString() + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/FireworkEffect.Type.html");
                    }

                    effectBuilder.with(effectType);
                    effectBuilder.flicker(effectRoot.has("flicker") ? effectRoot.get("flicker").getAsBoolean() : false);
                    effectBuilder.flicker(effectRoot.has("trail") ? effectRoot.get("trail").getAsBoolean() : true);

                    if (effectRoot.has("color")) {
                        JsonElement colourElement = effectRoot.get("color");
                        if (!colourElement.isJsonObject()) {
                            throw new JsonParseException("Element \"color\" is of unexpected type. Expected JsonObject, got " + colourElement.getClass().getSimpleName());
                        }

                        JsonObject colourRoot = colourElement.getAsJsonObject();
                        if (colourRoot.has("primary")) {
                            JsonElement primaryElement = colourRoot.get("primary");

                            if (primaryElement.isJsonPrimitive()) {
                                effectBuilder.withColor(Color.fromRGB(Integer.decode(primaryElement.getAsString())));
                            }

                            else if (primaryElement.isJsonArray()) {
                                JsonArray primaryArray = primaryElement.getAsJsonArray();
                                List<Color> colours = new ArrayList<>(primaryArray.size());
                                primaryArray.forEach(e -> colours.add(Color.fromRGB(Integer.decode(e.getAsString()))));
                                effectBuilder.withColor(colours);
                            }

                            else {
                                throw new JsonParseException("Element \"primary\" is of unexpected type. Expected number (decimal, hex, binary, etc.) or object, got " + primaryElement.getClass().getSimpleName());
                            }
                        }

                        if (colourRoot.has("fade")) {
                            JsonElement primaryElement = colourRoot.get("fade");

                            if (primaryElement.isJsonPrimitive()) {
                                effectBuilder.withColor(Color.fromRGB(Integer.decode(primaryElement.getAsString())));
                            }

                            else if (primaryElement.isJsonArray()) {
                                JsonArray primaryArray = primaryElement.getAsJsonArray();
                                List<Color> colours = new ArrayList<>(primaryArray.size());
                                primaryArray.forEach(e -> colours.add(Color.fromRGB(Integer.decode(e.getAsString()))));
                                effectBuilder.withColor(colours);
                            }

                            else {
                                throw new JsonParseException("Element \"fade\" is of unexpected type. Expected number (decimal, hex, binary, etc.) or object, got " + primaryElement.getClass().getSimpleName());
                            }
                        }
                    }

                    metaSpecific.addEffect(effectBuilder.build());
                }
            }
        }

        // Knowledge book meta (KnowledgeBookMeta)
        if (meta instanceof KnowledgeBookMeta) {
            KnowledgeBookMeta metaSpecific = (KnowledgeBookMeta) meta;

            if (object.has("recipes")) {
                JsonElement recipesElement = object.get("recipes");
                if (!recipesElement.isJsonArray()) {
                    throw new JsonParseException("Element \"recipes\" is of unexpected type. Expected array, got " + recipesElement.getClass().getSimpleName());
                }

                recipesElement.getAsJsonArray().forEach(e -> {
                    NamespacedKey recipe = NamespacedKeyUtil.fromString(e.getAsString(), null);
                    if (recipe != null) {
                        metaSpecific.addRecipe(recipe);
                    }
                });
            }
        }

        // Leather armour meta (LeatherArmorMeta)
        if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta metaSpecific = (LeatherArmorMeta) meta;

            if (object.has("color")) {
                metaSpecific.setColor(Color.fromRGB(Integer.decode(object.get("color").getAsString())));
            }
        }

        // Map meta (MapMeta)
        if (meta instanceof MapMeta) {
            MapMeta metaSpecific = (MapMeta) meta;

            if (object.has("color")) {
                metaSpecific.setColor(Color.fromRGB(Integer.decode(object.get("color").getAsString())));
            }

            if (object.has("location")) {
                metaSpecific.setLocationName(object.get("location").getAsString());
            }

            if (object.has("scaling")) {
                metaSpecific.setScaling(object.get("scaling").getAsBoolean());
            }
        }

        // Potion meta (PotionMeta)
        if (meta instanceof PotionMeta) {
            PotionMeta metaSpecific = (PotionMeta) meta;

            PotionType basePotionType = (object.has("base")) ? Enums.getIfPresent(PotionType.class, object.get("base").getAsString().toUpperCase()).or(PotionType.UNCRAFTABLE) : PotionType.UNCRAFTABLE;
            boolean upgraded = basePotionType.isUpgradeable() && object.has("upgraded") && object.get("upgraded").getAsBoolean();
            boolean extended = basePotionType.isExtendable() && object.has("extended") && object.get("extended").getAsBoolean();

            metaSpecific.setBasePotionData(new PotionData(basePotionType, upgraded, extended));

            if (object.has("color")) {
                metaSpecific.setColor(Color.fromRGB(Integer.decode(object.get("color").getAsString())));
            }

            if (object.has("effects") && object.get("effects").isJsonObject()) {
                JsonElement effectsElement = object.get("effects");
                if (!effectsElement.isJsonObject()) {
                    throw new JsonParseException("Element \"effects\" is of unexpected type. Expected object, got " + effectsElement.getClass().getSimpleName());
                }

                for (Entry<String, JsonElement> effectElement : effectsElement.getAsJsonObject().entrySet()) {
                    PotionEffectType effect = PotionEffectType.getByName(effectElement.getKey());
                    if (effect == null) {
                        throw new JsonParseException("Could not find potion effect with id \"" + effectElement.getKey() + "\". Does it exist? https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html");
                    }

                    // Default to 30 seconds
                    int duration = 600, amplifier = 0;
                    boolean ambient = false, particles = true, icon = true;

                    JsonElement effectDataElement = effectElement.getValue();
                    if (effectDataElement.isJsonPrimitive()) {
                        duration = effectDataElement.getAsInt();
                    }

                    else if (effectDataElement.isJsonObject()) {
                        JsonObject effectDataRoot = effectDataElement.getAsJsonObject();

                        if (effectDataRoot.has("duration")) {
                            duration = effectDataRoot.get("duration").getAsInt();
                        }

                        if (effectDataRoot.has("amplifier")) {
                            amplifier = effectDataRoot.get("amplifier").getAsInt();
                        }

                        if (effectDataRoot.has("ambient")) {
                            ambient = effectDataRoot.get("ambient").getAsBoolean();
                        }

                        if (effectDataRoot.has("particles")) {
                            particles = effectDataRoot.get("particles").getAsBoolean();
                        }

                        if (effectDataRoot.has("icon")) {
                            icon = effectDataRoot.get("icon").getAsBoolean();
                        }
                    }

                    else {
                        throw new JsonParseException("Effect element is of unexpected type. Expected number (duration) or object, got " + effectsElement.getClass().getSimpleName());
                    }

                    metaSpecific.addCustomEffect(new PotionEffect(effect, duration, amplifier, ambient, particles, icon), false);
                }
            }
        }

        // Skull meta (SkullMeta)
        if (meta instanceof SkullMeta) {
            SkullMeta metaSpecific = (SkullMeta) meta;

            if (object.has("owner")) {
                metaSpecific.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(object.get("owner").getAsString())));
            }
        }

        // Suspicious Stew Meta (SuspiciousStewMeta)
        if (meta instanceof SuspiciousStewMeta) {
            SuspiciousStewMeta metaSpecific = (SuspiciousStewMeta) meta;

            if (object.has("effects")) {
                JsonObject effectsRoot = object.getAsJsonObject("effects");
                for (Entry<String, JsonElement> effectElement : effectsRoot.entrySet()) {
                    PotionEffectType effect = PotionEffectType.getByName(effectElement.getKey());
                    if (effect == null) {
                        throw new JsonParseException("Could not find potion effect with id \"" + effectElement.getKey() + "\". Does it exist? https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html");
                    }

                    // Default to 30 seconds
                    int duration = 600, amplifier = 0;
                    boolean ambient = false, particles = true, icon = true;

                    JsonElement effectDataElement = effectElement.getValue();
                    if (effectDataElement.isJsonPrimitive()) {
                        duration = effectDataElement.getAsInt();
                    }

                    else if (effectDataElement.isJsonObject()) {
                        JsonObject effectDataRoot = effectDataElement.getAsJsonObject();

                        if (effectDataRoot.has("duration")) {
                            duration = effectDataRoot.get("duration").getAsInt();
                        }

                        if (effectDataRoot.has("amplifier")) {
                            amplifier = effectDataRoot.get("amplifier").getAsInt();
                        }

                        if (effectDataRoot.has("ambient")) {
                            ambient = effectDataRoot.get("ambient").getAsBoolean();
                        }

                        if (effectDataRoot.has("particles")) {
                            particles = effectDataRoot.get("particles").getAsBoolean();
                        }

                        if (effectDataRoot.has("icon")) {
                            icon = effectDataRoot.get("icon").getAsBoolean();
                        }
                    }

                    else {
                        throw new JsonParseException("Effect element is of unexpected type. Expected number (duration) or object, got " + effectElement.getClass().getSimpleName());
                    }

                    metaSpecific.addCustomEffect(new PotionEffect(effect, duration, amplifier, ambient, particles, icon), false);
                }
            }
        }

        // Fish bucket meta (TropicalFishBucketMeta)
        if (meta instanceof TropicalFishBucketMeta) {
            TropicalFishBucketMeta metaSpecific = (TropicalFishBucketMeta) meta;

            if (object.has("pattern")) {
                TropicalFish.Pattern pattern = Enums.getIfPresent(TropicalFish.Pattern.class, object.get("pattern").getAsString().toUpperCase()).orNull();
                if (pattern == null) {
                    throw new JsonParseException("Unexpected value for \"pattern\". Given \"" + object.get("pattern").getAsString() + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/TropicalFish.Pattern.html");
                }

                metaSpecific.setPattern(pattern);
            }

            if (object.has("color") && object.get("color").isJsonObject()) {
                JsonObject colorRoot = object.getAsJsonObject("color");

                if (colorRoot.has("body")) {
                    metaSpecific.setBodyColor(Enums.getIfPresent(DyeColor.class, colorRoot.get("body").getAsString().toUpperCase()).or(DyeColor.WHITE));
                }

                if (colorRoot.has("pattern")) {
                    metaSpecific.setPatternColor(Enums.getIfPresent(DyeColor.class, colorRoot.get("pattern").getAsString().toUpperCase()).or(DyeColor.WHITE));
                }
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Deserialize an {@link ItemStack} from a {@link JsonObject}.
     *
     * @param object the object from which to deserialize an ItemStack
     *
     * @return the deserialized ItemStack
     */
    @NotNull
    public static ItemStack deserializeItemStack(@NotNull JsonObject object) {
        Preconditions.checkArgument(object != null, "object must not be null");

        if (!object.has("item")) {
            throw new JsonParseException("Could not find \"item\"");
        }

        Material type = Material.matchMaterial(object.get("item").getAsString());
        if (type == null) {
            throw new JsonParseException("Could not create item of type \"" + object.get("item").getAsString() + "\". Does it exist?");
        }

        ItemStack item = new ItemStack(type);
        return deserializeItemStack(item, object);
    }

    /**
     * Deserialize an {@link ItemStack} from a {@link JsonObject}, but first attempting to use
     * modern methods, {@link ItemFactory#createItemStack(String)}, to create the item and apply
     * legacy deserialization to the created item.
     *
     * @param object the object from which to deserialize an ItemStack
     *
     * @return the deserialized ItemStack
     */
    @NotNull
    public static ItemStack deserializeItemStackModern(@NotNull JsonObject object) {
        if (!object.has("item")) {
            throw new JsonParseException("Could not find \"item\"");
        }

        String resultString = object.get("item").getAsString();
        ItemStack result = null;

        try {
            result = Bukkit.getItemFactory().createItemStack(resultString);

            if (object.has("amount")) {
                result.setAmount(Math.max(object.get("amount").getAsInt(), 1));
            }

            // Still apply any legacy deserialization even though we're using modern methods so that legacy recipes are supported
            result = deserializeItemStack(result, object);
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Malformatted \"item\" input. Got: \"" + resultString + "\"");
        } catch (Exception e) { // If the method doesn't exist, we'll fall back to old serialization
            result = deserializeItemStack(object);
        }

        return result;
    }

}
