package wtf.choco.alchema.api.event;

import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.cauldron.AlchemicalCauldron;

/**
 * Represents a more specific derivative of {@link BlockEvent} for an
 * {@link AlchemicalCauldron} related event.
 *
 * @author Parker Hawke
 */
public abstract class CauldronEvent extends BlockEvent {

    protected final AlchemicalCauldron cauldron;

    public CauldronEvent(@NotNull AlchemicalCauldron cauldron) {
        super(cauldron.getCauldronBlock());

        this.cauldron = cauldron;
    }

    /**
     * Get the {@link AlchemicalCauldron} involved in this event.
     *
     * @return the cauldron
     */
    @NotNull
    public AlchemicalCauldron getCauldron() {
        return cauldron;
    }

}
