package wtf.choco.alchema.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.api.event.CauldronIngredientsDropEvent;
import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.cauldron.CauldronManager;

public final class CauldronManipulationListener implements Listener {

    private final Alchema plugin;

    public CauldronManipulationListener(@NotNull Alchema plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onPlaceCauldron(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.CAULDRON) {
            return;
        }

        this.plugin.getCauldronManager().addCauldron(new AlchemicalCauldron(block));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onCauldronLevelChange(CauldronLevelChangeEvent event) {
        Block block = event.getBlock();
        BlockData data = block.getBlockData();
        if (!(data instanceof Levelled)) {
            return;
        }

        CauldronManager manager = plugin.getCauldronManager();
        AlchemicalCauldron cauldron = manager.getCauldron(block);

        if (event.getNewLevel() == ((Levelled) data).getMaximumLevel() && cauldron == null) {
            manager.addCauldron(new AlchemicalCauldron(block));
            return;
        }

        else if (event.getNewLevel() < ((Levelled) data).getMaximumLevel() && cauldron != null) {
            Entity entity = event.getEntity();
            cauldron.dropIngredients(CauldronIngredientsDropEvent.Reason.EMPTIED_BY_PLAYER, entity instanceof Player ? (Player) entity : null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onDestroyCauldron(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.CAULDRON) {
            return;
        }

        CauldronManager manager = plugin.getCauldronManager();
        AlchemicalCauldron cauldron = manager.getCauldron(block);
        if (cauldron == null) {
            return;
        }

        manager.removeCauldron(cauldron);

        cauldron.dropIngredients(CauldronIngredientsDropEvent.Reason.DESTROYED, event.getPlayer());
    }

}
