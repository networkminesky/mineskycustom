package net.mineskycustom.custom;

import net.kyori.adventure.text.Component;
import net.mineskyitems.entities.item.ItemHandler;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public record CustomItem(String name, int model, String material, String mineskyItemId) {
    public static CustomItem serialize(final YamlConfiguration configuration) {
        int model = configuration.getInt("item.model");
        String material = configuration.getString("item.type", "PAPER");
        String name = configuration.getString("item.name");
        String mineskyItemId = configuration.getString("item.mineskyitem", "");

        return new CustomItem(name, model, material, mineskyItemId);
    }

    public boolean isMineSkyItem() {
        return mineskyItemId != null;
    }

    public ItemStack toSpigotItem() {
        if(isMineSkyItem())
            return ItemHandler.getItemById(mineskyItemId).buildStack();
        else {
            ItemStack stack = new ItemStack(spigotMaterial());
            ItemMeta meta = stack.getItemMeta();
            meta.itemName(Component.translatable(name));
            meta.setCustomModelData(model);
            stack.setItemMeta(meta);

            return stack;
        }
    }

    public Material spigotMaterial() {
        return Material.matchMaterial(this.material);
    }
}
