package wtf.choco.alchema.crafting;

import java.io.File;

import wtf.choco.alchema.Alchema;

/**
 * Represents a result of the {@link CauldronRecipeRegistry#loadCauldronRecipes(Alchema, File)}
 * asynchronous recipe loading.
 */
public interface RecipeLoadResult {

    /**
     * Get the amount of recipes loaded natively from Alchema's file system.
     *
     * @return the amount of native recipes loaded
     */
    public int getNative();

    /**
     * Get the amount of recipes loaded from third-party plugins.
     *
     * @return the amount of third-party recipes loaded
     */
    public int getThirdParty();

    /**
     * Get the amount of total recipes loaded.
     *
     * @return the total recipe count
     */
    public default int getTotal() {
        return getNative() + getThirdParty();
    }

    /**
     * Get the time, in milliseconds, it took for this process to complete.
     *
     * @return the completion time
     */
    public long getTimeToComplete();

}
