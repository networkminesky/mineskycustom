package net.mineskycustom.custom.machines;

import net.mineskycustom.MineSkyCustom;
import net.mineskycustom.custom.blocks.CustomBlock;
import net.mineskycustom.events.InventoryEvents;
import net.mineskycustom.handler.BlockHandler;
import net.mineskycustom.handler.MachineHandler;
import net.mineskycustom.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CoalGenerator {
    public static ArrayList<Inventory> invs = new ArrayList<>();
    public static HashMap<Inventory, Block> invblock = new HashMap<>();

    public static void loadInventory(Inventory inv, Block origin, CustomBlock cb) {
        String format = BlockHandler.machineFormatter(origin);
        if (MineSkyCustom.data.contains(format) && cb.getMachine() != null && cb.getMachine().isInventoryEnabled()) {
            for (String s : MineSkyCustom.data.getConfigurationSection(format + ".inventory").getKeys(false)) {
                try {
                    String base64 = MineSkyCustom.data.getString(format + ".inventory." + s.trim());
                    if(base64 == null || base64.isEmpty()) continue;
                    ItemStack it = Utils.itemStackFromBase64(base64);
                    inv.setItem(Integer.parseInt(s), it);
                } catch (Exception ex) {
                    continue;
                }
            }
        }
    }

    public static List<ItemStack> saveInventory(Inventory inv, Block origin) {
        String format = BlockHandler.machineFormatter(origin);
        List<ItemStack> list = new ArrayList<>();
        if (MineSkyCustom.data.contains(format)) {
            ItemStack it = inv.getItem(4);
            if(it == null || it.getType().isAir()) {
                NoteBlock nb = (NoteBlock)origin.getBlockData();
                nb.setNote(new Note(2));
                origin.setBlockData(nb);

                // Bukkit.broadcastMessage(".");

                MineSkyCustom.data.set(format+".activated", false);
                MineSkyCustom.data.set(format+".inventory.4", "");

                MachineHandler.saveData();

                return list;
            }
            list.add(it);
            String base64 = Utils.formatBase64(Utils.itemStackToBase64(it));
            MineSkyCustom.data.set(format+".activated", true);

            NoteBlock nb = (NoteBlock)origin.getBlockData();
            nb.setNote(new Note(3));
            origin.setBlockData(nb);

            MineSkyCustom.data.set(format+".inventory.4", base64);
            MachineHandler.saveData();
        }
        return list;
    }

    public static List<ItemStack> saveInventory(Inventory inv, Block origin, ItemStack it) {
        String format = BlockHandler.machineFormatter(origin);
        List<ItemStack> list = new ArrayList<>();
        if (MineSkyCustom.data.contains(format)) {
            if(it == null || it.getType().isAir()) {
                NoteBlock nb = (NoteBlock)origin.getBlockData();
                nb.setNote(new Note(2));
                origin.setBlockData(nb);

                // Bukkit.broadcastMessage(".");

                MineSkyCustom.data.set(format+".activated", false);
                MineSkyCustom.data.set(format+".inventory.4", "");

                MachineHandler.saveData();

                return list;
            }
            list.add(it);
            String base64 = Utils.formatBase64(Utils.itemStackToBase64(it));
            MineSkyCustom.data.set(format+".activated", true);

            NoteBlock nb = (NoteBlock)origin.getBlockData();
            nb.setNote(new Note(3));
            origin.setBlockData(nb);

            MineSkyCustom.data.set(format+".inventory.4", base64);
            MachineHandler.saveData();
        }
        return list;
    }

    public static void openInventory(Player opener, Block origin, CustomBlock cb) {
        Inventory inv = Bukkit.createInventory(null, 27, "§f\uF808\uF839\uF825\uF825\uF802\uF802\uF802\uF802§8Gerador de carvão");

        InventoryEvents.OPEN_CUSTOM_INVENTORY.add(opener.getUniqueId());

        invs.add(inv);
        opener.openInventory(inv);
        invblock.put(inv, origin);

        loadInventory(inv, origin, cb);

        showBurnInfo(inv);
    }

    public static void inventoryClick(Inventory inv, int slot, ItemStack clickedItem, Player p, InventoryClickEvent e) {

        if(slot == 4) {
            if(clickedItem == null || clickedItem.getType().isAir()) {
                inv.setItem(22, new ItemStack(Material.AIR));
                Block b = invblock.get(inv);
                if(b != null) {
                    saveInventory(inv, b, clickedItem);
                }
                return;
            }
            if(clickedItem.getType() == Material.COAL) {
                showBurnInfo(inv);

                Block b = invblock.get(inv);
                if(b != null) {
                    saveInventory(inv, b);
                }
                return;
            }
        }

        e.setCancelled(true);

    }

    public static void showBurnInfo(Inventory inv) {
        ItemStack it = inv.getItem(4);
        if(it == null) return;

        ItemStack burn = new ItemStack(Material.PAPER);
        ItemMeta burnim = burn.getItemMeta();
        burnim.setDisplayName("§6Gerando 1 de energia");
        burnim.setCustomModelData(40);
        burnim.setLore(Arrays.asList("§7Gerando a cada §e2 segundos", "§7Queimando "+it.getAmount()+" carvões"));
        burn.setItemMeta(burnim);

        inv.setItem(22, burn);
    }

}
