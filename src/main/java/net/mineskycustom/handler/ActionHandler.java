package net.mineskycustom.handler;

import net.mineskycustom.custom.blocks.CustomBlock;
import net.mineskycustom.custom.machines.CoalGenerator;
import net.mineskycustom.custom.machines.Desmantelador;
import net.mineskycustom.custom.machines.TinkeringTable;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ActionHandler {

    public static void executeAction(Player p, String action, Block origin, CustomBlock cb) {
        switch(action) {
            case "DESMANTELADOR": {
                Desmantelador.openInventory(p, origin, cb);
                break;
            }
            case "TINKERING": {
                TinkeringTable.openMineSkyItemsGUI(p, origin);
                break;
            }
            case "GERADOR_CARVAO": {
                CoalGenerator.openInventory(p, origin, cb);
                break;
            }
        }
    }

}
