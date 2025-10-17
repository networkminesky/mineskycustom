package net.mineskycustom.custom.blocks;

import net.mineskycustom.MineSkyCustom;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class CustomBlockProperties {

    private final CustomBlock customBlock;
    private final boolean canLight;
    private final int lightlevel;
    private final boolean haveCustomPredef;
    private String customPreDef;
    private final String action;
    private List<String> tools;
    private int hardness;
    private String sound;

    public CustomBlockProperties(CustomBlock cb) {
        this.customBlock = cb;

        YamlConfiguration config = cb.getConfig();

        this.canLight = config.contains("properties.light");
        if(canLight)
            this.lightlevel = config.getInt("properties.light");
        else this.lightlevel = -1;

        this.haveCustomPredef = config.contains("properties.type.predef");
        this.action = config.contains("properties.action") ? config.getString("properties.action") : "";

        if(config.contains("properties.type.predef")) {

            for(String s : MineSkyCustom.predefs.getKeys(false)) {

                if(s.equalsIgnoreCase(config.getString("properties.type.predef"))) {

                    this.sound = MineSkyCustom.predefs.getString(s+".sound");
                    this.hardness = MineSkyCustom.predefs.getInt(s+".hardness");
                    this.tools = MineSkyCustom.predefs.getStringList(s+".tools");
                    this.customPreDef = config.getString("properties.type.predef");

                    break;

                }

            }

        } else {
            this.hardness = config.getInt("properties.type.hardness");
            this.sound = config.getString("properties.type.sound");
            this.customPreDef = "CUSTOM";
            this.tools = config.getStringList("properties.tools");
        }
    }

    public CustomBlock getCustomBlock() {
        return customBlock;
    }

    public boolean canLight() {
        return canLight;
    }

    public boolean haveCustomPredef() {
        return haveCustomPredef;
    }

    public int getHardness() {
        return hardness;
    }

    public int getLightlevel() {
        return lightlevel;
    }

    public List<String> getTools() {
        return tools;
    }

    public String getAction() {
        return this.action;
    }

    public String getCustomPreDef() {
        return customPreDef;
    }

    public String getSound() {
        return sound;
    }
}
