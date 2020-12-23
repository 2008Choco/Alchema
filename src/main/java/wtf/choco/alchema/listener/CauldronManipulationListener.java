package wtf.choco.alchema.listener;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.api.event.CauldronIngredientsDropEvent;
import wtf.choco.alchema.cauldron.AlchemicalCauldron;
import wtf.choco.alchema.cauldron.CauldronManager;
import wtf.choco.alchema.crafting.CauldronIngredient;
import wtf.choco.alchema.util.AlchemaEventFactory;

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
            this.dropIngredients(cauldron, entity instanceof Player ? (Player) entity : null, CauldronIngredientsDropEvent.Reason.EMPTIED_BY_PLAYER);
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

        this.dropIngredients(cauldron, event.getPlayer(), CauldronIngredientsDropEvent.Reason.DESTROYED);
    }

    private void dropIngredients(@NotNull AlchemicalCauldron cauldron, @Nullable Player player, @NotNull CauldronIngredientsDropEvent.Reason reason) {
        if (!cauldron.hasIngredients()) {
            return;
        }

        List<ItemStack> items = cauldron.getIngredients().stream().map(CauldronIngredient::asItemStack).filter(Objects::nonNull).collect(Collectors.toList());
        CauldronIngredientsDropEvent ingredientsDropEvent = AlchemaEventFactory.callCauldronIngredientsDropEvent(cauldron, items, player, reason);
        if (ingredientsDropEvent.isCancelled()) {
            return;
        }

        World world = cauldron.getWorld();
        Location location = cauldron.getLocation().add(0.5, 0.25, 0.5);

        ingredientsDropEvent.getItems().forEach(item -> world.dropItem(location, item));
        cauldron.clearIngredients();
    }

}