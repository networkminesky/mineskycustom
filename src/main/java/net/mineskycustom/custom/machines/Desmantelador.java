package net.mineskycustom.custom.machines;

import net.mineskycustom.MineSkyCustom;
import net.mineskycustom.custom.blocks.CustomBlock;
import net.mineskycustom.events.InventoryEvents;
import net.mineskycustom.handler.BlockHandler;
import net.mineskycustom.handler.ItemHandler;
import net.mineskycustom.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Desmantelador {

    public static ArrayList<Inventory> invs = new ArrayList<>();

    public static ItemStack getConfirmItem() {
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta im = it.getItemMeta();
        im.setCustomModelData(3);
        im.setLore(Arrays.asList("§7Construindo, você irá transformar", "§7o item em sua receita original.","§7 ", "§6Você precisa estar com", "§6seu inventário vazio!"));
        im.setDisplayName("§aConstruir");
        it.setItemMeta(im);
        return it;
    }

    public static void inventoryClick(Inventory inv, int slot, ItemStack clickedItem, Player p, InventoryClickEvent e) {

        if (slot == 10) {

            if(clickedItem == null || clickedItem.getType().isAir()) {

                Desmantelador.clearOutputItems(inv);
                inv.setItem(21, new ItemStack(Material.AIR));

                return;
            }

            if(clickedItem.isSimilar(inv.getItem(10))) {
                e.setCancelled(true);
                p.sendMessage("§cO Desmantelador apenas permite um item por vez!");
                return;
            }

            if(clickedItem.getAmount() > 1) {
                e.setCancelled(true);
                p.sendMessage("§cO Desmantelador apenas permite um item por vez!");
                return;
            }

            inv.setItem(21, getConfirmItem());

            Desmantelador.updateOutputItems(inv, clickedItem);

            return;
        }

        if(slot == 21) {

            if(clickedItem == null)
                return;

            int availableemptys = 0;
            for(ItemStack items : p.getInventory().getStorageContents()) {
                if(items == null || items.getType().isAir())
                    availableemptys++;
            }

            if(availableemptys < 9) {
                e.setCancelled(true);
                p.sendMessage(Utils.c("&cVocê precisa ter pelo menos 9 espaços vazios em seu inventário!"));
                return;
            }

            int resultitems = 0;
            int touse = 5;
            for(int i = 0; i < 9; i++) {
                if (touse == 8) touse = 14;
                if (touse == 17) touse = 23;
                touse++;
                ItemStack it = inv.getItem(touse-1);
                if(it == null || it.getType().isAir()) continue;

                p.getInventory().addItem(it);
                resultitems++;
            }

            if(resultitems == 0) {
                e.setCancelled(true);
                return;
            }else {
                inv.setItem(10, new ItemStack(Material.AIR));
                Desmantelador.clearOutputItems(inv);
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_VILLAGER_WORK_TOOLSMITH, 1, 0.7f);
            }
        }

        e.setCancelled(true);
    }

    public static void updateOutputItems(Inventory inv, ItemStack it) {
        List<ItemStack> ingredients = new ArrayList<>();
        /*for(int i = 0; i < 9; i++) {
            ingredients.add(new ItemStack(Material.AIR));
        }*/

        if(isBlacklisted(it.getType())) return;

        for (Recipe recipe : Bukkit.getServer().getRecipesFor(it)) {
            if (recipe instanceof ShapedRecipe) {
                ShapedRecipe shaped = (ShapedRecipe) recipe;
                for (ItemStack ingredient : shaped.getIngredientMap().values()) {
                    if(ingredient == null) {
                        ingredients.add(new ItemStack(Material.AIR));
                        continue;
                    }
                    ItemStack fixed = new ItemStack(ingredient.getType(), 1);
                    ingredients.add(fixed);
                }
            } else if (recipe instanceof ShapelessRecipe) {
                ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
                for (ItemStack ingredient : shapeless.getIngredientList()) {
                    if(ingredient == null) continue;
                    ItemStack fixed = new ItemStack(ingredient.getType(), 1);
                    ingredients.add(fixed);
                }
            }
        }

        if(ingredients.isEmpty())
            return;

        int touse = 5;
        for (ItemStack ingredient : ingredients) {
            if (touse == 8) touse = 14;
            if (touse == 17) touse = 23;
            touse++;
            inv.setItem(touse-1, ingredient);
        }
    }

    public static boolean isBlacklisted(Material mm) {
        String m = mm.name();
        if(m.equals("DIAMOND")) return true;
        if(m.equals("GOLD_INGOT")) return true;
        if(m.equals("IRON_INGOT")) return true;
        if(m.equals("DIORITE")) return true;
        if(m.equals("EMERALD")) return true;
        if(m.equals("LAPIS_LAZULI")) return true;
        if(m.equals("BEACON")) return true;
        if(m.equals("COPPER")) return true;
        if(m.equals("BLAZE")) return true;

        if(m.equals("IRON_BLOCK")) return true;

        if(m.contains("ANVIL")) return true;
        if(m.contains("_SLAB")) return true;
        if(m.contains("GLASS")) return true;

        if(m.equals("LEAD")) return true;
        if(m.contains("NETHERITE")) return true;
        if(m.contains("_PLANKS")) return true;
        if(m.contains("DYE")) return true;
        if(m.contains("RAW_")) return true;
        return false;
    }

    // metodo teste
    // TODO: melhorar futuramente
    public static void clearOutputItems(Inventory inv) {
        ItemStack it = new ItemStack(Material.AIR);
        inv.setItem(5, it);
        inv.setItem(6, it);
        inv.setItem(7, it);
        inv.setItem(14, it);        inv.setItem(23, it);
        inv.setItem(15, it);        inv.setItem(24, it);
        inv.setItem(16, it);        inv.setItem(25, it);
    }

    public static void openInventory(Player opener, Block origin, CustomBlock cb) {

        String form = BlockHandler.machineFormatter(origin);
        double energy = MineSkyCustom.data.getDouble(form+".energy");
        double max = cb.getMachine().getMaxEnergy();

        InventoryEvents.OPEN_CUSTOM_INVENTORY.add(opener.getUniqueId());

        Inventory inv = Bukkit.createInventory(null, 27, "§f\uF808\uF834\uF825\uF825\uF802\uF802\uF802\uF802§8Desmantelador\uF826\uF826\uF826\uF826§f"+ Utils.energyBarFormatter(energy, max));

        ItemHandler.setEnergyBars(inv, energy, max, Utils.getPercent(energy, max));

        invs.add(inv);
        opener.openInventory(inv);
    }

}
