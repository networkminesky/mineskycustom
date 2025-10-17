package net.mineskycustom.custom;

import net.mineskycustom.custom.blocks.CustomBlock;
import net.mineskycustom.custom.plants.CustomPlant;

public class CustomObject {

    public String plantOrBlock() {
        return isCustomBlock() ? "block" : "plant";
    }

    private final Object object;
    private final boolean isPlant;

    public CustomObject(Object object) {
        this.object = object;
        this.isPlant = object instanceof CustomPlant;
    }

    public Object getObject() {
        return object;
    }

    public boolean isCustomPlant() {
        return isPlant;
    }
    public boolean isCustomBlock() {
        return !isPlant;
    }

}
