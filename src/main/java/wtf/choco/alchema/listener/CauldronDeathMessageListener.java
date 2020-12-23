package wtf.choco.alchema.listener;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.MetadataValue;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.util.AlchemaConstants;

public final class CauldronDeathMessageListener implements Listener {

    private static final long BOIL_TO_DEATH_TIME_FRAME = TimeUnit.SECONDS.toMillis(3);

    private final Alchema plugin;

    public CauldronDeathMessageListener(Alchema plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onKilledByBoilingCauldron(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!player.hasMetadata(AlchemaConstants.METADATA_KEY_DAMAGED_BY_CAULDRON)) {
            return;
        }

        List<MetadataValue> metadataValues = player.getMetadata(AlchemaConstants.METADATA_KEY_DAMAGED_BY_CAULDRON);
        long lastDamaged = -1;

        for (MetadataValue value : metadataValues) {
            lastDamaged = Math.max(lastDamaged, value.asLong());
        }

        player.removeMetadata(AlchemaConstants.METADATA_KEY_DAMAGED_BY_CAULDRON, plugin);

        if (System.currentTimeMillis() - lastDamaged < BOIL_TO_DEATH_TIME_FRAME) {
            event.setDeathMessage(player.getName() + " has boiled to death in a cauldron.");
        }
    }

}
