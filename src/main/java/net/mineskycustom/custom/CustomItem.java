package net.mineskycustom.custom;

import net.kyori.adventure.text.Component;
import net.mineskyitems.entities.item.ItemHandler;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public record CustomItem(String name, int model, String material, String mineskyItemId) {
    public static CustomItem serialize(final ConfigurationSection itemSection) {
        if(itemSection == null)
            return null;

        int model = itemSection.getInt("model", -1);
        String material = itemSection.getString("type", "PAPER");
        String name = itemSection.getString("name", "");
        String mineskyItemId = itemSection.getString("mineskyitem", "");

        return new CustomItem(name, model, material, mineskyItemId);
    }

    public boolean isMineSkyItem() {
        return !mineskyItemId.isBlank();
    }

    public ItemStack toSpigotItem() {
        return toSpigotItem(1);
    }

    public ItemStack toSpigotItem(final int amount) {
        if(isMineSkyItem()) {
            ItemStack custom = ItemHandler.getItemById(mineskyItemId).buildStack();
            custom.setAmount(amount);
            return custom;
        } else {
            ItemStack stack = new ItemStack(spigotMaterial(), amount);
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
