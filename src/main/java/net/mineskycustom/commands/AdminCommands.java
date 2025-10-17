package net.mineskycustom.commands;

import net.mineskycustom.gui.CustomGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("cb")) {
            if (!(s instanceof Player p) || !s.hasPermission("mineskycustom.command.cb"))
                return true;

            CustomGUI.openInventory(p, 0, true);
        }

        if (cmd.getName().equalsIgnoreCase("mscustom")) {
            if (!(s instanceof Player) || !s.hasPermission("mineskycustom.command.mscustom"))
                return true;

            if (args.length <= 1) {
                s.sendMessage("§e/mscustom vfx (nome do vfx)" +
                        "\n§e/mscustom vfxgroup (nome do grupo vfx)");
                return true;
            }
        }

        return false;
    }
}

