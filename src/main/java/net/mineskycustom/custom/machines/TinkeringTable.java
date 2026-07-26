package net.mineskycustom.custom.machines;

import net.mineskycustom.MineSkyCustom;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TinkeringTable {

    public static void openMineSkyItemsGUI(final Player player, Block origin) {
        try {
            net.mineskyitems.gui.tinkering.TinkeringGUI.openGUI(player, origin);
        } catch(Exception ex) {
            MineSkyCustom.l.severe("Método TinkeringGUI retornou erro: "+ex.getMessage());
        }
    }
}