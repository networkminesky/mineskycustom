package net.mineskycustom.custom.machines;

import net.mineskycustom.custom.blocks.CustomBlock;
import org.bukkit.configuration.file.YamlConfiguration;

public class Machine {

    public enum Type {
        GENERATOR,
        MACHINE
    }

    private final double maxenergy;
    private final boolean inventoryEnabled;
    private final double energypersecond;
    private final CustomBlock customBlock;
    private final Type type;

    public Machine(CustomBlock cb) {
        YamlConfiguration config = cb.getConfig();
        this.type = config.getString("machine.type").equalsIgnoreCase("generator") ? Type.GENERATOR : Type.MACHINE;
        this.inventoryEnabled = config.getBoolean("machine.inventory-enabled");

        if(type == Type.MACHINE) {
            this.maxenergy = config.getDouble("machine.max-energy");
            this.energypersecond = -1;
        } else {
            this.energypersecond = config.getDouble("machine.energy-per-second");
            this.maxenergy = -1;
        }

        this.customBlock = cb;
    }

    /*
      SE FOR "MACHINE" O VALOR É "energy"!

      SE FOR "GENERATOR" O VALOR É "energy-per-second"!
     */

    public Type getMachineType() {
        return this.type;
    }

    public double getEnergyPerSecond() {
        return energypersecond;
    }

    public CustomBlock getCustomBlock() {
        return this.customBlock;
    }

    public boolean isInventoryEnabled() {
        return this.inventoryEnabled;
    }

    public double getMaxEnergy() {
        return this.maxenergy;
    }
}
