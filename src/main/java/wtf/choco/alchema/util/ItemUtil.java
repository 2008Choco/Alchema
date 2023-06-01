package wtf.choco.alchema.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

/**
 * A series of utilities pertaining to {@link ItemStack ItemStacks}.
 * <p>
 * <strong>NOTE:</strong>This class it not a part of Alchema's API contract and may
 * be subject to breakages without prior warning. Use of methods in this class should
 * be done at ones own risk.
 *
 * @author Parker Hawke - Choco
 */
@Internal
public final class ItemUtil {

    private ItemUtil() { }

    /**
     * Serialize an {@link ItemStack} as a byte array.
     *
     * @param itemStack the item to serialize
     *
     * @return the serialized item stack
     */
    public static byte[] serialize(@NotNull ItemStack itemStack) {
        try (ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream outputStream = new BukkitObjectOutputStream(byteArrayStream)) {
            outputStream.writeObject(itemStack);
            return byteArrayStream.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    /**
     * Deserialize an {@link ItemStack} from a byte array.
     *
     * @param bytes the bytes
     *
     * @return the deserialized item stack
     */
    @NotNull
    public static ItemStack deserialize(byte[] bytes) {
        try (ByteArrayInputStream byteArrayStream = new ByteArrayInputStream(bytes);
                BukkitObjectInputStream inputStream = new BukkitObjectInputStream(byteArrayStream)) {
            Object read = inputStream.readObject();

            if (read instanceof ItemStack itemStack) {
                return itemStack;
            }
        } catch (IOException | ClassNotFoundException e) { }

        return new ItemStack(Material.AIR);
    }

    /**
     * Parse an {@link ItemStack} from a {@link JsonObject}. The object should contain
     * an entry for "item" which is a /give-formatted item stack string, and an optional
     * "amount" integer.
     *
     * @param object the object from which to deserialize an ItemStack
     *
     * @return the deserialized ItemStack
     */
    @NotNull
    public static ItemStack parseItemStack(@NotNull JsonObject object) {
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
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Malformatted \"item\" input. Got: \"" + resultString + "\"");
        }

        return result;
    }

}
