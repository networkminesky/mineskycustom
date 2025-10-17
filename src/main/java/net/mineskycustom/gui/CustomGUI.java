package net.mineskycustom.gui;

import net.mineskycustom.MineSkyCustom;
import net.mineskycustom.custom.blocks.CustomBlock;
import net.mineskycustom.custom.plants.CustomPlant;
import net.mineskycustom.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomGUI implements Listener {

    //                      PAGE | INVENTORY
    public static HashMap<Integer, Inventory> pages = new HashMap<>();

    public static HashMap<Player, Integer> playerCurrentPage = new HashMap<>();

    public static void openInventory(Player player, int page, boolean closeOther) {
        if(closeOther) player.closeInventory();

        if(pages.isEmpty())
            initializeItems();

        Inventory inv = pages.get(page);
        if(inv == null) {
            player.sendMessage("§cUm erro ocorreu, provavelmente essa página não existe.");
            return;
        }

        player.openInventory(inv);
        playerCurrentPage.put(player, page);
    }

    public static ItemStack createNewButton(int cmd, String name) {
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta im = it.getItemMeta();
        im.setCustomModelData(cmd);
        im.setDisplayName("§6"+name);
        it.setItemMeta(im);
        return it;
    }

    public static Inventory createNewInventory(final int totalContents, final int currentPage, final int maxPages) {
        Inventory invNew = Bukkit.createInventory(null, 54, "Total: "+totalContents+" - Página "+(currentPage+1)+"/"+maxPages);

        final ItemStack back = createNewButton(6, "Voltar");
        final ItemStack next = createNewButton(5, "Próxima");

        invNew.setItem(47, back);
        invNew.setItem(51, next);

        return invNew;
    }

    public static void initializeItems() {
        List<Object> contents = new ArrayList<>();
        List<Inventory> inventories = new ArrayList<>();

        contents.addAll(MineSkyCustom.REGISTERED_PLANTS);
        contents.addAll(MineSkyCustom.REGISTERED_BLOCKS);

        final int total = contents.size();

        int currentSlot = 0;
        int currentPage = 0;
        final int totalPages = (total / 45);

        final Inventory firstInventory = createNewInventory(total, currentPage, totalPages);

        inventories.add( firstInventory );
        pages.put(currentPage, firstInventory);

        for(int i = 0; i < total; i++) {
            final Inventory inv = inventories.get(currentPage);

            Object obj = contents.get(i);
            if(obj instanceof CustomPlant cp) {
                inv.addItem(extraLoreOnItem(cp.getItem().createMineSkyItem().toSpigotItem(), "§7PLANT"));
            }
            if(obj instanceof CustomBlock cb) {
                inv.addItem(extraLoreOnItem(cb.getItem().createMineSkyItem().toSpigotItem(), "§7BLOCK"));
            }

            currentSlot++;

            if(currentSlot >= 45) {
                currentSlot = 0;
                currentPage++;

                Inventory invNew = createNewInventory(total, currentPage, totalPages);
                inventories.add(invNew);
                pages.put(currentPage, invNew);
            }
        }
    }

    public static ItemStack extraLoreOnItem(ItemStack it, String lore) {
        ItemMeta im = it.getItemMeta();
        im.setLore(List.of(Utils.c(lore)));
        it.setItemMeta(im);
        return it;
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!pages.containsValue(e.getInventory()))
            return;

        e.setCancelled(true);

        final int slot = e.getSlot();
        final Player p = (Player)e.getWhoClicked();
        final ItemStack clickedItem = e.getCurrentItem();

        // return page
        if(slot == 47) {
            int relation = (playerCurrentPage.getOrDefault(p, 1)-1);
            if(!pages.containsKey(relation))
                return;

            openInventory(p, relation, false);
            return;
        }
        // next page
        if(slot == 51) {
            int relation = (playerCurrentPage.getOrDefault(p, -1)+1);
            if(!pages.containsKey(relation))
                return;

            openInventory(p, relation, false);
            return;
        }

        if(clickedItem == null) return;

        ItemMeta im = clickedItem.getItemMeta();
        im.setLore(null);

        clickedItem.setItemMeta(im);

        p.getInventory().addItem(clickedItem);
    }

    // cancel dragging
    @EventHandler
    public void onInventoryDragEvent(final InventoryDragEvent e) {
        if (pages.containsValue(e.getInventory()))
            e.setCancelled(true);
    }

}
