package net.mineskycustom.handler;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemHandler {

    public static List<Material> AXE = Arrays.asList(Material.NETHERITE_AXE, Material.DIAMOND_AXE, Material.GOLDEN_AXE, Material.IRON_AXE, Material.STONE_AXE, Material.WOODEN_AXE);
    public static List<Material> PICKAXE = Arrays.asList(Material.NETHERITE_PICKAXE, Material.DIAMOND_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE, Material.STONE_PICKAXE, Material.WOODEN_PICKAXE);

    public enum ItemType {
        AXE,
        PICKAXE,
        OTHER
    }

    public static void setEnergyBars(Inventory inv, double energia, double max, double percent) {
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta im = it.getItemMeta();
        im.setCustomModelData(2);
        im.setDisplayName("§aEnergia atual: "+energia);
        im.setLore(Arrays.asList("§7Carregado §2"+(int)percent+"% de 100%","§7 ", "§7O máximo de energia é §a"+max));
        it.setItemMeta(im);

        inv.setItem(8, it);
        inv.setItem(17, it);
        inv.setItem(26, it);
    }

    public static ItemType axeOrPickaxe(Material m) {

        if(m.name().contains("_AXE"))
            return ItemType.AXE;
        if(m.name().contains("_PICKAXE"))
            return ItemType.PICKAXE;

        return ItemType.OTHER;

    }

}
