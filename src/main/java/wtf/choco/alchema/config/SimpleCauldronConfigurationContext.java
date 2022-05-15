package wtf.choco.alchema.config;

/**
 * A simple implementation of {@link CauldronConfigurationContext}.
 */
// TODO: This would be a really nice class to use a record ._.
final class SimpleCauldronConfigurationContext implements CauldronConfigurationContext {

    private final int itemSearchInterval;
    private final int millisecondsToHeatUp;
    private final boolean enforcePlayerDroppedItems;

    private final boolean damageEntities;
    private final int minEssenceOnDeath, maxEssenceOnDeath;

    private final float volumeAmbientBubble, volumeItemSplash, volumeSuccessfulCraft;

    SimpleCauldronConfigurationContext(int itemSearchInterval, int millisecondsToHeatUp, boolean enforcePlayerDroppedItems, boolean damageEntities, int minEssenceOnDeath, int maxEssenceOnDeath, float volumeAmbientBubble, float volumeItemSplash, float volumeSuccessfulCraft) {
        this.itemSearchInterval = itemSearchInterval;
        this.millisecondsToHeatUp = millisecondsToHeatUp;
        this.enforcePlayerDroppedItems = enforcePlayerDroppedItems;
        this.damageEntities = damageEntities;
        this.minEssenceOnDeath = minEssenceOnDeath;
        this.maxEssenceOnDeath = maxEssenceOnDeath;
        this.volumeAmbientBubble = volumeAmbientBubble;
        this.volumeItemSplash = volumeItemSplash;
        this.volumeSuccessfulCraft = volumeSuccessfulCraft;
    }

    @Override
    public int getItemSearchInterval() {
        return itemSearchInterval;
    }

    @Override
    public int getMillisecondsToHeatUp() {
        return millisecondsToHeatUp;
    }

    @Override
    public boolean shouldEnforcePlayerDroppedItems() {
        return enforcePlayerDroppedItems;
    }

    @Override
    public boolean shouldDamageEntities() {
        return damageEntities;
    }

    @Override
    public int getMinEssenceOnDeath() {
        return minEssenceOnDeath;
    }

    @Override
    public int getMaxEssenceOnDeath() {
        return maxEssenceOnDeath;
    }

    @Override
    public float getAmbientBubbleVolume() {
        return volumeAmbientBubble;
    }

    @Override
    public float getItemSplashVolume() {
        return volumeItemSplash;
    }

    @Override
    public float getSuccessfulCraftVolume() {
        return volumeSuccessfulCraft;
    }

}
