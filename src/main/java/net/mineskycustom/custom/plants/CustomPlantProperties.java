package net.mineskycustom.custom.plants;

import net.mineskycustom.custom.blocks.CustomBlockProperties;
import org.bukkit.configuration.ConfigurationSection;

public class CustomPlantProperties {

    private final CustomPlant plant;

    private final String sound;
    private final float soundPitch;
    private final String tag;
    private final boolean clientsidemining;
    private final int minlight;
    private final int maxlight;
    private final boolean placeOnAnyBlock;

    public CustomPlantProperties(CustomPlant plant) {
        this.plant = plant;
        ConfigurationSection sec = plant.getConfig().getConfigurationSection("plant-properties");
        assert sec != null;

        this.sound = sec.getString("sound", "block.grass");
        this.soundPitch = (float)sec.getDouble("pitch", 1f);
        this.tag = sec.getString("tag", "");
        this.minlight = sec.getInt("min-light", -1);
        this.maxlight = sec.getInt("max-light", -1);
        this.clientsidemining = sec.getBoolean("player-clientside-map-mining", false);
        this.placeOnAnyBlock = sec.getBoolean("place-on-any-block", false);
    }

    public int getMinLight() {
        return minlight;
    }

    public boolean hasMinLight() {
        return minlight != -1;
    }

    public int getMaxLight() {
        return maxlight;
    }

    public boolean hasMaxLight() {
        return maxlight != -1;
    }

    // Any solid block*
    public boolean canPlaceOnAnyBlock() {
        return placeOnAnyBlock;
    }

    public String getSound() {
        return sound;
    }

    public String getTag() {
        return tag;
    }

    public float getSoundPitch() {
        return soundPitch;
    }

    public boolean canPlayerClientsideMineInMap() {
        return clientsidemining;
    }

    public CustomPlant getPlant() {
        return plant;
    }
}
