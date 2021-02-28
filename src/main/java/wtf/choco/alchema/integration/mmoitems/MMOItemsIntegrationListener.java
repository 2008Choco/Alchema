package wtf.choco.alchema.integration.mmoitems;

import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import wtf.choco.alchema.api.event.CauldronIngredientAddEvent;
import wtf.choco.alchema.crafting.CauldronIngredient;
import wtf.choco.alchema.crafting.CauldronIngredientItemStack;

import io.lumine.mythic.lib.api.item.NBTItem;

public final class MMOItemsIntegrationListener implements Listener {

    MMOItemsIntegrationListener() { }

    @EventHandler
    private void onAddMMOItemIngredient(CauldronIngredientAddEvent event) {
        CauldronIngredient ingredient = event.getIngredient();
        if (!(ingredient instanceof CauldronIngredientItemStack)) {
            return;
        }

        ItemStack item = ingredient.asItemStack();
        if (item == null) {
            return;
        }

        NBTItem nbtItem = NBTItem.get(item);
        if (!nbtItem.hasType()) {
            return;
        }

        MMOItem mmoItem = new LiveMMOItem(nbtItem);
        event.setIngredient(new CauldronIngredientMMOItem(mmoItem, item, item.getAmount()));
    }

}
