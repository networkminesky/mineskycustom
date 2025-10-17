package net.mineskycustom.custom.codeditems;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ThrowableItemRegistry {

    public static Map<Integer, Class<? extends ThrowableItem>> registeredItems = new HashMap<>();

    public static void registerItem(int modelData, Class<? extends ThrowableItem> itemClass) {
        registeredItems.put(modelData, itemClass);
    }

    public static ThrowableItem createItem(String itemName, Player thrower) {
        try {
            Class<? extends ThrowableItem> itemClass = registeredItems.get(itemName);
            if (itemClass != null) {
                return itemClass.getDeclaredConstructor(Player.class).newInstance(thrower);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
