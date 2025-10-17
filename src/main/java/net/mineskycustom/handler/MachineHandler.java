package net.mineskycustom.handler;

import net.mineskycustom.MineSkyCustom;
import net.mineskycustom.custom.blocks.CustomBlock;
import net.mineskycustom.custom.machines.CoalGenerator;
import net.mineskycustom.custom.machines.Machine;
import net.mineskycustom.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.LightningRod;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public class MachineHandler {

    public static void logMachine(CustomBlock cb, Block placehere, Player placer) {
        Machine m = cb.getMachine();
        String f = BlockHandler.machineFormatter(placehere);
        MineSkyCustom.data.set(f+".id", cb.getId());
        MineSkyCustom.data.set(f+".action", cb.getProperties().getAction());
        MineSkyCustom.data.set(f+".type", m.getMachineType()+"");
        MineSkyCustom.data.set(f+".placer", placer.getUniqueId()+"");
        MineSkyCustom.data.set(f+".facing", "NORTH");
        MineSkyCustom.data.set(f+".activated", false);

        if(m.getMachineType() == Machine.Type.GENERATOR) {
            MineSkyCustom.data.set(f+".energy-per-second", m.getEnergyPerSecond());
        } else {
            MineSkyCustom.data.set(f+".energy", 0);
        }
        if(m.isInventoryEnabled())
            MineSkyCustom.data.set(f+".inventory.0", " ");

        saveData();
    }

    public static BlockFace getEnergySide(BlockFace f) {
        return switch (f) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> f;
        };
    }

    public static void saveData() {
        File file = new File(MineSkyCustom.getInstance().getDataFolder(), "machinerydata.yml");
        try {
            MineSkyCustom.data.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MineSkyCustom.data = YamlConfiguration.loadConfiguration(file);
    }

    public static void addEnergy(Block b, double power) {
        String form = BlockHandler.machineFormatter(b);

        if(MineSkyCustom.data.contains(form)) {
            CustomBlock cb = new CustomBlock(MineSkyCustom.data.getString(form+".id"));
            if(cb.isMachine() && MineSkyCustom.data.getString(form+".type").equals("MACHINE")) {

                double calc = MineSkyCustom.data.getDouble(form+".energy")+power;
                if(calc > cb.getMachine().getMaxEnergy()) {
                    calc = cb.getMachine().getMaxEnergy();
                }

                MineSkyCustom.data.set(form+".energy", calc);
            }

        }

    }

    public static void generateEnergyOnAllGenerators() {
        for(String s : MineSkyCustom.data.getKeys(false)) {
            // Bukkit.broadcastMessage(s);
            if(!MineSkyCustom.data.getString(s+".type").equals("GENERATOR"))
                continue;
            if(!MineSkyCustom.data.getBoolean(s+".activated"))
                continue;

            String[] args = s.split(" ");
            Location l = new Location(Bukkit.getWorld(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));

            if(!l.getChunk().isLoaded())
                continue;

            Block b = l.getBlock();

            CustomBlock thisGenerator = new CustomBlock(MineSkyCustom.data.getString(s+".id"));
            if(thisGenerator.getMachine() != null && thisGenerator.getMachine().isInventoryEnabled()) {
                for (String sd : MineSkyCustom.data.getConfigurationSection(s + ".inventory").getKeys(false)) {
                    try {
                        String base64 = MineSkyCustom.data.getString(s + ".inventory." + sd);
                        if(base64 == null || base64.isEmpty()) continue;
                        ItemStack it = Utils.itemStackFromBase64(base64);
                        it.setAmount(it.getAmount()-1);

                        if(it.getAmount()-1 <= 0)
                            it = new ItemStack(Material.AIR);

                        if(it.getType().isAir() || it.getAmount() <= 0) {
                            MineSkyCustom.data.set(s+".inventory."+sd, "");
                            MineSkyCustom.data.set(s+".activated", false);
                            if(thisGenerator.getProperties().getAction().equals("GERADOR_CARVAO")) {
                                NoteBlock nb = (NoteBlock)b.getBlockData();
                                nb.setNote(new Note(2));
                                b.setBlockData(nb);
                            }
                        } else {
                            MineSkyCustom.data.set(s+".inventory."+sd, Utils.formatBase64(Utils.itemStackToBase64(it)));
                        }

                        for(Inventory inv : CoalGenerator.invblock.keySet()) {
                            Block bs = CoalGenerator.invblock.get(inv);
                            if (bs.getLocation().equals(b.getLocation())) {
                                CoalGenerator.loadInventory(inv, b, thisGenerator);
                                CoalGenerator.showBurnInfo(inv);
                            }
                        }
                    } catch (Exception ex) {
                        continue;
                    }
                }
            }

            BlockFace energySide = getEnergySide(BlockFace.valueOf(MineSkyCustom.data.getString(s+".facing")));

            b.getWorld().playSound(b.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.5f, 0.8f);

            new BukkitRunnable() {
                Block touse = b;
                BlockFace nextFace = energySide;
                int n = 0;
                @Override
                public void run() {
                    touse = touse.getRelative(nextFace);

                    if(touse.getType() == Material.LIGHTNING_ROD) {
                        b.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, touse.getLocation().add(0.5, 0.5, 0.5), 4, 0.1, 0.1, 0.1, 0);

                        LightningRod rod = (LightningRod)touse.getBlockData();
                        nextFace = rod.getFacing();
                        n++;
                    }
                    else if(touse.getType() == Material.NOTE_BLOCK) {

                        addEnergy(touse, thisGenerator.getMachine().getEnergyPerSecond());
                        this.cancel();
                        return;
                    }
                    else {
                        this.cancel();
                        return;
                    }

                    if(n == 15) {
                        this.cancel();
                        return;
                    }
                }
            }.runTaskTimerAsynchronously(MineSkyCustom.getInstance(), 0, 5);

        }

        saveData();

    }

}
