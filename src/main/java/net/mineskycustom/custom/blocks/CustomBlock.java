package net.mineskycustom.custom.blocks;

import net.mineskycustom.MineSkyCustom;
import net.mineskycustom.custom.machines.Machine;
import net.mineskycustom.handler.InstrumentConverter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.io.File;

public class CustomBlock {

    private final String id;
    private final File file;
    private final YamlConfiguration config;
    private final int note;
    private final String instrument;
    private final CustomBlockProperties properties;
    private final CustomBlockItem item;
    private final Machine machine;
    public CustomBlock(String blockID) {
        this.id = blockID;
        File f = new File(MineSkyCustom.customBlocksFolder, blockID + ".yml");
        YamlConfiguration cs = YamlConfiguration.loadConfiguration(f);

        this.file = f;

        this.config = cs;

        if(cs.contains("machine")) {
            this.machine = new Machine(this);
        }else
            this.machine = null;

        this.note = cs.getInt("block.note");
        this.instrument = cs.getString("block.instrument");

        this.properties = new CustomBlockProperties(this);
        this.item = new CustomBlockItem(this);
    }

    public CustomBlockProperties getProperties() {
        return properties;
    }

    public boolean isMachine() {
        return this.machine != null;
    }

    @Nullable
    public Machine getMachine() {
        return this.machine;
    }

    public File getFile() {
        return this.file;
    }

    public boolean isAlt() {
        return getId().contains("ALT_");
    }

    public String getIdWithoutAlt() {
        return getId().replace("ALT_", "").trim();
    }

    public int getNote() {
        return this.note;
    }

    public String getId() {
        return this.id;
    }

    public String getInstrument() {
        return this.instrument;
    }

    public CustomBlockItem getItem() {
        return this.item;
    }

    public YamlConfiguration getConfig() {
        return this.config;
    }

    public boolean isSame(Block t) {
        if(t.getType() != Material.NOTE_BLOCK)
            return false;

        NoteBlock nb = (NoteBlock) t.getBlockData();
        String nbins = InstrumentConverter.fromSpigot(nb.getInstrument());

        if(this.getInstrument().equalsIgnoreCase(nbins) && this.getNote()==(int)nb.getNote().getId()) {
            return true;
        }
        return false;
    }

    public boolean isSame(ItemStack t) {
        if(!t.hasItemMeta()) return false;
        ItemMeta im = t.getItemMeta();
        if(!im.hasCustomModelData())
            return false;
        CustomBlockItem item = this.getItem();

        return im.getCustomModelData() == item.getModel() && item.getSpigotMaterial() == t.getType();
    }

}
