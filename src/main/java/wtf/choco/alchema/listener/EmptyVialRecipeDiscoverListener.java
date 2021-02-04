package wtf.choco.alchema.listener;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import wtf.choco.alchema.util.AlchemaConstants;

public final class EmptyVialRecipeDiscoverListener implements Listener {

    @EventHandler
    private void onDiscoverGlassBottle(PlayerRecipeDiscoverEvent event) {
        Player player = event.getPlayer();
        if (!event.getRecipe().equals(Material.GLASS_BOTTLE.getKey()) || player.hasDiscoveredRecipe(AlchemaConstants.RECIPE_KEY_EMPTY_VIAL)) {
            return;
        }

        player.discoverRecipe(AlchemaConstants.RECIPE_KEY_EMPTY_VIAL);
    }

    @EventHandler
    private void onCraftGlassPane(CraftItemEvent event) {
        Recipe recipe = event.getRecipe();
        if (!(recipe instanceof ShapedRecipe)) {
            return;
        }

        NamespacedKey key = ((ShapedRecipe) recipe).getKey();
        if (!key.equals(Material.GLASS_PANE.getKey())) {
            return;
        }

        HumanEntity player = event.getWhoClicked();
        if (player.hasDiscoveredRecipe(AlchemaConstants.RECIPE_KEY_EMPTY_VIAL)) {
            return;
        }

        player.discoverRecipe(AlchemaConstants.RECIPE_KEY_EMPTY_VIAL);
    }

}
