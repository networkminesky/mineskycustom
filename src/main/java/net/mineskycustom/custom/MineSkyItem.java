package net.mineskycustom.custom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.mineskycustom.MineSkyCustom;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.json.simple.JSONObject;

public class MineSkyItem {

    public static ItemStack EMPTY_ITEM = new ItemStack(Material.AIR);

    private ItemStack spigotItem;
    private final String translate;

    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(MineSkyCustom.getInstance(), "item");

    public MineSkyItem(String translateReq, Material m, int amount) {
        ItemStack spigot = new ItemStack(m, amount);
        ItemMeta im = spigot.getItemMeta();

        im.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, translateReq);

        im.displayName(Component.translatable(translateReq)
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.WHITE)
        );

        this.translate = translateReq;
        this.spigotItem = spigot;
    }

    public void setCustomModelData(int i) {
        ItemStack spigot = toSpigotItem();
        ItemMeta im = spigot.getItemMeta();
        im.setCustomModelData(i);
        spigot.setItemMeta(im);

        this.spigotItem = spigot;
    }

    public static boolean isMineSkyItem(ItemStack item) {
        return item != null
                && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(NAMESPACED_KEY);
    }

    public JSONObject getTranslateJSON() {
        JSONObject translateName = new JSONObject();
        translateName.put("translate", this.translate);
        translateName.put("color", "white");
        translateName.put("italic", false);
        return translateName;
    }

    public TranslatableComponent getSpigotTranslatableComponent() {
        return new TranslatableComponent(this.translate);
    }

    public String getTranslatePath() {
        return this.translate;
    }

    public ItemStack toSpigotItem() {
        return this.spigotItem;
    }

}

