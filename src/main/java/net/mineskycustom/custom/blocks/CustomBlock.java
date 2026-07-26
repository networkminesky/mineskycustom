package net.mineskycustom.custom.blocks;

import net.mineskycustom.MineSkyCustom;
import net.mineskycustom.custom.machines.Machine;
import net.mineskycustom.handler.InstrumentConverter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class CustomBlock {

    private final String id;
    private final YamlConfiguration config;
    private final int note;
    private final String instrument;
    private final CustomBlockProperties properties;
    private final CustomBlockItem item;

    private final @Nullable Machine machine;

    private final @Nullable OrientedValues orientedValues;

    public static record OrientedValue(String instrument, int note, List<Integer> fakeBlocks) {}
    public static record OrientedValues(OrientedValue north, OrientedValue south, OrientedValue west, OrientedValue east) {
        public List<OrientedValue> values() {
            return List.of(north, south, west, east);
        }

        public static @Nullable OrientedValues read(final ConfigurationSection section) {
            if(section == null)
                return null;

            OrientedValue north = new OrientedValue(section.getString("north.instrument"),
                    section.getInt("north.note"), section.getIntegerList("north.fake-blocks"));
            OrientedValue south = new OrientedValue(section.getString("south.instrument"),
                    section.getInt("south.note"), section.getIntegerList("south.fake-blocks"));
            OrientedValue west = new OrientedValue(section.getString("west.instrument"),
                    section.getInt("west.note"), section.getIntegerList("west.fake-blocks"));
            OrientedValue east = new OrientedValue(section.getString("east.instrument"),
                    section.getInt("east.note"), section.getIntegerList("east.fake-blocks"));

            return new OrientedValues(north, south, west, east);
        }
    }

    public CustomBlock(String blockID, YamlConfiguration cs, int note, String instrument) {
        this.id = blockID;

        this.note = note;
        this.instrument = instrument;

        this.config = cs;

        if(cs.contains("machine")) {
            this.machine = new Machine(this);
        } else
            this.machine = null;

        ConfigurationSection orientedBlocks = cs.getConfigurationSection("oriented-facing.blocks");
        if(orientedBlocks != null) {
            this.orientedValues = OrientedValues.read(orientedBlocks);
        } else
            this.orientedValues = null;

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

    public @Nullable OrientedValues getOrientedValues() {
        return orientedValues;
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

        return this.getInstrument().equalsIgnoreCase(nbins) && this.getNote()==(int)nb.getNote().getId();
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
