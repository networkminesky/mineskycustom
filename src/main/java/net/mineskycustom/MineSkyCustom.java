package net.mineskycustom;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.mineskycustom.commands.AdminCommands;
import net.mineskycustom.gui.CustomGUI;
import net.mineskycustom.custom.blocks.CustomBlock;
import net.mineskycustom.custom.plants.CustomPlant;
import net.mineskycustom.custom.machines.Machine;
import net.mineskycustom.events.InteractEvents;
import net.mineskycustom.events.InventoryEvents;
import net.mineskycustom.events.MMODCreativeTab;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.geyser.api.event.EventRegistrar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class MineSkyCustom extends JavaPlugin implements EventRegistrar {

    public static ProtocolManager protocolManager;

    public static File customBlocksFolder = null;
    public static File customPlantsFolder = null;
    public static File vfxFolder = null;

    public static HashMap<Player, Integer> blockingTicks = new HashMap<>();

    public static YamlConfiguration predefs;
    public static YamlConfiguration vfxGroups;
    public static YamlConfiguration data;

    public static Logger l;

    public static MineSkyCustom instance;

    public static boolean MYTHICMOBS_HOOKED = false;

    public static final PotionEffect slowDiggingPotionEffect = new PotionEffect(PotionEffectType.MINING_FATIGUE, 99999, -1, false, false, false);

    public static ArrayList<CustomBlock> REGISTERED_BLOCKS = new ArrayList<>();
    public static ArrayList<CustomPlant> REGISTERED_PLANTS = new ArrayList<>();
    public static ArrayList<Machine> REGISTERED_MACHINES = new ArrayList<>();

    public static MineSkyCustom getInstance() {
        return instance;
    }

    private static final PotionEffect jumpPadEffect = new PotionEffect(PotionEffectType.JUMP_BOOST, 8, 9);
    private static final PotionEffect upgradedJumpPadEffect = new PotionEffect(PotionEffectType.JUMP_BOOST, 8, 46);

    @Override
    public void onEnable() {

        instance = this;
        l = this.getLogger();

        l.info("\n   ____                 _                       \n" +
                "  / ___|  _   _   ___  | |_    ___    _ __ ___  \n" +
                " | |     | | | | / __| | __|  / _ \\  | '_ ` _ \\ \n" +
                " | |___  | |_| | \\__ \\ | |_  | (_) | | | | | | |\n" +
                "  \\____|  \\__,_| |___/  \\__|  \\___/  |_| |_| |_|\n" +
                "                                                ");
        l.info("Criado por Drawn e feito exclusivamente para o MineSky.");

        protocolManager = ProtocolLibrary.getProtocolManager();

        File file = new File(this.getDataFolder(), "predefs.yml");
        MineSkyCustom.predefs = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            try {
                l.info("Arquivo predefs.yml criado");
                MineSkyCustom.predefs.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File file2 = new File(this.getDataFolder(), "machinerydata.yml");
        MineSkyCustom.data = YamlConfiguration.loadConfiguration(file2);
        if (!file2.exists()) {
            try {
                l.info("Arquivo machinerydata.yml criado");
                MineSkyCustom.data.save(file2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            MYTHICMOBS_HOOKED = true;
        }

        File file3 = new File(this.getDataFolder(), "vfxgroups.yml");
        MineSkyCustom.vfxGroups = YamlConfiguration.loadConfiguration(file3);
        if (!file3.exists()) {
            try {
                l.info("Arquivo vfxgroups.yml criado");
                MineSkyCustom.vfxGroups.save(file3);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                for(Player p : Bukkit.getOnlinePlayers()) {

                    if(p.isBlocking()) {
                        blockingTicks.put(p, blockingTicks.getOrDefault(p, 0)+1);
                    } else {
                        blockingTicks.remove(p);
                    }

                }

            }
        }.runTaskTimer(this, 20, 0);

        this.getServer().getMessenger().registerIncomingPluginChannel(this, "mineskymod:main", new MMODCreativeTab());

        customBlocksFolder = new File(getDataFolder(), "blocks");
        if(!customBlocksFolder.exists()) {
            customBlocksFolder.mkdir();
            l.info("Pasta blocks criada!");
        }

        customPlantsFolder = new File(getDataFolder(), "plants");
        if(!customPlantsFolder.exists()) {
            customPlantsFolder.mkdir();
            l.info("Pasta plants criada!");
        }

        if(customBlocksFolder.exists()) {
            for (File f : customBlocksFolder.listFiles()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(f);

                String ID = f.getName();
                String formatted = ID.split("\\.")[0].trim();

                CustomBlock cb = new CustomBlock(formatted);

                l.info("[BLOCKS] Registrando bloco: "+ID + ", Hardness: "+cb.getProperties().getHardness());

                MineSkyCustom.REGISTERED_BLOCKS.add(cb);
            }
        }

        if(customPlantsFolder.exists()) {
            for (File f : customPlantsFolder.listFiles()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(f);

                String ID = f.getName();
                String formatted = ID.split("\\.")[0].trim();

                CustomPlant cb = new CustomPlant(formatted);

                l.info("[PLANTS] Registrando planta: "+ID + ", Sound: "+cb.getPlantProperties().getSound());

                MineSkyCustom.REGISTERED_PLANTS.add(cb);
            }
        }

        /*vfxFolder = new File(getDataFolder(), "vfx");
        if(!vfxFolder.exists()) {
            vfxFolder.mkdir();
            l.info("Pasta vfx criada!");
        }

        if(vfxFolder.exists()) {
            for (File f : vfxFolder.listFiles()) {
                String ID = f.getName();
                String formatted = ID.split("\\.")[0].trim();

                RegisteredVFX vf = new RegisteredVFX(formatted, f);

                l.info("[VFX] Registrando VFX: "+ID + " | Possui directional: "+
                        (vf.isDirectional() ? "Sim: "+vf.getDirectionalVector() : "Não"));

                VFXHandler.VFX_LIST.add(vf);
            }
        }

        for(String s : vfxGroups.getKeys(false)) {
            VFXGroup vfxGroup = new VFXGroup(s, vfxGroups.getConfigurationSection(s));

            l.info("[VFX] Registrando Grupo VFX: "+s + " | Contém som: "+vfxGroup.hasSound());

            VFXHandler.VFX_GROUPS.add(vfxGroup);
        }*/

        InteractEvents.registerDigEvent();

        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) continue;

                    try {
                        Block z = p.getTargetBlock((Set<Material>) null, 5);
                        if (z.getType() == Material.NOTE_BLOCK) {
                            p.addPotionEffect(slowDiggingPotionEffect);
                        }
                    } catch(Exception ignored) {}
                }
            }
        }.runTaskTimer(this, 10, 2);

        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p.isDead() || !p.isOnGround())
                        continue;

                    Block b = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
                    if(b.getType() == Material.NOTE_BLOCK) {
                        if(b.getBlockData() instanceof NoteBlock nb) {
                            if(nb.getInstrument() == Instrument.BANJO) {
                                int note = nb.getNote().getId();
                                if (note == 8)
                                    p.addPotionEffect(jumpPadEffect);
                                if (note == 9)
                                    p.addPotionEffect(upgradedJumpPadEffect);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20, 6);

        /*new BukkitRunnable() {
            @Override
            public void run() {
                MachineHandler.generateEnergyOnAllGenerators();
            }
        }.runTaskTimer(this, 0, 40);*/

        l.info("Registrando comandos...");
        this.getCommand("cb").setExecutor(new AdminCommands());
        this.getCommand("mscustom").setExecutor(new AdminCommands());

        l.info("Registrando eventos...");
        getServer().getPluginManager().registerEvents(new InteractEvents(), this);
        getServer().getPluginManager().registerEvents(new CustomGUI(), this);
        getServer().getPluginManager().registerEvents(new InventoryEvents(), this);

        l.info("Registrando packets");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "minesky:main");

        l.info("Registrando novos itens");

        l.info("Verificando existência do Geyser no servidor.");
        if(getServer().getPluginManager().isPluginEnabled("Geyser-Spigot")) {
            l.info("[GEYSER] Registrando blocos custom dentro do Geyser.");
            //GeyserHook.registerBlocks();
        }
        // Desativado temporariamente
        //ThrowableItemRegistry.registerItem(84, Stormlander.class);
        //ThrowableItemRegistry.registerItem(80, Leviathan.class);

        //ThrowableItem stormlander2 = ThrowableItemRegistry.createItem("Stormlander2", player);
    }

    public static StateFlag PARRY_FLAG;

    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("parry", true);
            registry.register(flag);
            PARRY_FLAG = flag; // only set our field if there was no error

        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("parry");
            if (existing instanceof StateFlag) {
                PARRY_FLAG = (StateFlag) existing;
            } else {
            }
        }
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);

        for(Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        }

        if(protocolManager != null)
            protocolManager.removePacketListeners(this);
    }

}
