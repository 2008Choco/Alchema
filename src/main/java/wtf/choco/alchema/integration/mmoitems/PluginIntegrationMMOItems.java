package wtf.choco.alchema.integration.mmoitems;

import net.Indyuce.mmoitems.MMOItems;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.commons.integration.PluginIntegration;

public final class PluginIntegrationMMOItems implements PluginIntegration {

    private final MMOItems mmoItemsPlugin;

    public PluginIntegrationMMOItems(Plugin plugin) {
        this.mmoItemsPlugin = (MMOItems) plugin;
    }

    @NotNull
    @Override
    public Plugin getIntegratedPlugin() {
        return mmoItemsPlugin;
    }

    @Override
    public void load() {
        Alchema alchema = Alchema.getInstance();

        CauldronIngredientMMOItem.key = new NamespacedKey(mmoItemsPlugin, "mmoitem");
        alchema.getRecipeRegistry().registerIngredientType(CauldronIngredientMMOItem.key, CauldronIngredientMMOItem::new);

        alchema.getLogger().info("Registered foreign ingredient type: " + CauldronIngredientMMOItem.key);
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(new MMOItemsIntegrationListener(), Alchema.getInstance());
    }

    @Override
    public void disable() { }

    @Override
    public boolean isSupported() {
        try { // MMOItems 6.5.4+ uses MythicLib, a different import for NBTItem from its previous MMOLib
            Class.forName("io.lumine.mythic.lib.api.item.NBTItem");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
