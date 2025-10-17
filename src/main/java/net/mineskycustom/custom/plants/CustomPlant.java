package net.mineskycustom.custom.plants;

import net.mineskycustom.MineSkyCustom;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

public class CustomPlant {
    private final String id;
    private final File file;
    private final YamlConfiguration config;

    private final CustomPlantItem item;
    private final CustomPlantProperties properties;

    private final boolean attached;
    private final boolean powered;
    private final boolean disarmed;
    private final boolean east;
    private final boolean north;
    private final boolean south;
    private final boolean west;

    public CustomPlant(String plantID) {
        File f = new File(MineSkyCustom.customPlantsFolder, plantID + ".yml");
        YamlConfiguration cs = YamlConfiguration.loadConfiguration(f);
        this.file = f;
        this.id = plantID;
        this.config = cs;

        this.attached = cs.getBoolean("block.attached");
        this.powered = cs.getBoolean("block.powered");
        this.disarmed = cs.getBoolean("block.disarmed");
        this.east = cs.getBoolean("block.east");
        this.north = cs.getBoolean("block.north");
        this.south = cs.getBoolean("block.south");
        this.west = cs.getBoolean("block.west");

        this.properties = new CustomPlantProperties(this);
        this.item = new CustomPlantItem(this);
    }

    public CustomPlantProperties getPlantProperties() {
        return properties;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public boolean isDisarmed() {
        return disarmed;
    }

    public boolean isAttached() {
        return attached;
    }

    public boolean isEast() {
        return east;
    }

    public String getId() {
        return id;
    }

    public boolean isNorth() {
        return north;
    }

    public boolean isSouth() {
        return south;
    }

    public boolean isPowered() {
        return powered;
    }

    public boolean isWest() {
        return west;
    }

    public CustomPlantItem getItem() {
        return item;
    }

    public boolean isSame(Block t) {
        if(t.getType() != Material.TRIPWIRE)
            return false;

        Tripwire wire = (Tripwire) t.getBlockData();
        boolean[] DANSEWP = new boolean[]{wire.isDisarmed(), wire.isAttached(), wire.hasFace(BlockFace.NORTH),
                wire.hasFace(BlockFace.SOUTH), wire.hasFace(BlockFace.EAST), wire.hasFace(BlockFace.WEST), wire.isPowered()};

        return Arrays.equals(DANSEWP, getDANSEWP());
    }

    public boolean[] getDANSEWP() {
        return new boolean[]{disarmed, attached, north, south, east, west, powered};
    }

}
