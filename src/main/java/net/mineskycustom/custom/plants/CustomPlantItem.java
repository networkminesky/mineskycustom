package net.mineskycustom.custom.plants;

import net.mineskycustom.custom.MineSkyItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

public class CustomPlantItem {
    private final String name;
    private final int model;
    private final String material;
    private final CustomPlant customPlant;

    public CustomPlantItem(CustomPlant cb) {
        this.customPlant = cb;

        YamlConfiguration cs = cb.getConfig();
        this.model = cs.getInt("item.model");
        this.material = cs.getString("item.type", "PAPER");
        this.name = cs.getString("item.name");
    }

    public MineSkyItem createMineSkyItem() {
        MineSkyItem it = new MineSkyItem(getName(), getSpigotMaterial(), 1);
        it.setCustomModelData(getModel());
        return it;
    }

    public CustomPlant getCustomPlant() {
        return this.customPlant;
    }

    public String getName() {
        return this.name;
    }

    public int getModel() {
        return this.model;
    }

    public String getMaterial() {
        return this.material;
    }
    public Material getSpigotMaterial() {
        return Material.valueOf(this.material);
    }
}
