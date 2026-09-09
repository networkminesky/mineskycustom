package net.mineskycustom.custom.machines;

import net.minesky.mineskygameplay.advancements.AdvancementsAPI;
import net.mineskycustom.MineSkyCustom;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TinkeringTable {

    public static void openMineSkyItemsGUI(final Player player, Block origin) {
        try {
            net.mineskyitems.gui.tinkering.TinkeringGUI.openGUI(player, origin);
            AdvancementsAPI.get().grantAsync(player, "minesky:invencao/aprendiz");
        } catch(Exception ex) {
            MineSkyCustom.l.severe("Método TinkeringGUI retornou erro: "+ex.getMessage());
        }
    }
}