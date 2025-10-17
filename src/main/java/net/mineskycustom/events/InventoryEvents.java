package net.mineskycustom.events;

import net.mineskycustom.custom.machines.CoalGenerator;
import net.mineskycustom.custom.machines.Desmantelador;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class InventoryEvents implements Listener {

    public static ArrayList<UUID> OPEN_CUSTOM_INVENTORY = new ArrayList<>();

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if(Desmantelador.invs.contains(e.getInventory())) {
            e.setCancelled(true);
        }
        if(CoalGenerator.invs.contains(e.getInventory())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Inventory inv = e.getClickedInventory();

        int slot = e.getRawSlot();  

        // Bukkit.broadcastMessage(slot+"");

        ItemStack clickedItem = e.getCursor();

        Player p = (Player) e.getWhoClicked();

        if(e.getInventory() != e.getClickedInventory() && e.getClick().toString().contains("SHIFT") && OPEN_CUSTOM_INVENTORY.contains(p.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        if(Desmantelador.invs.contains(inv)) {
            Desmantelador.inventoryClick(inv, slot, clickedItem, p, e);
        }
        if(CoalGenerator.invs.contains(inv)) {
            CoalGenerator.inventoryClick(inv, slot, clickedItem, p, e);
        }
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        Player p = (Player)e.getPlayer();
        if(Desmantelador.invs.contains(inv)) {

            ItemStack it = inv.getItem(10);
            if(it != null) {
                e.getPlayer().getWorld().spawn(e.getPlayer().getLocation().add(0, 1.2, 0), Item.class, i -> {
                    i.setVelocity(e.getPlayer().getLocation().getDirection().multiply(0.3));
                    i.setPickupDelay(15);
                    i.setItemStack(it);
                });
            }

            Desmantelador.invs.remove(inv);
            OPEN_CUSTOM_INVENTORY.remove(p.getUniqueId());

        }
        if(CoalGenerator.invs.contains(inv)) {

            if(CoalGenerator.invblock.containsKey(inv)) {
                Block b = CoalGenerator.invblock.get(inv);
                CoalGenerator.saveInventory(inv, b);
                //e.getPlayer().sendMessage("§7§oSalvando inventário...");
            }

            OPEN_CUSTOM_INVENTORY.remove(p.getUniqueId());
            CoalGenerator.invblock.remove(inv);
            CoalGenerator.invs.remove(inv);
        }
    }

}
