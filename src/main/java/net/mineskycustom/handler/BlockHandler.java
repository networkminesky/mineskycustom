package net.mineskycustom.handler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.mineskycustom.MineSkyCustom;
import net.mineskycustom.custom.CustomObject;
import net.mineskycustom.custom.blocks.CustomBlock;
import net.mineskycustom.custom.blocks.CustomBlockItem;
import net.mineskycustom.custom.blocks.CustomBlockProperties;
import net.mineskycustom.custom.HardnessResult;
import net.mineskycustom.custom.plants.CustomPlant;
import net.mineskycustom.custom.plants.CustomPlantItem;
import net.mineskycustom.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BlockHandler {

    public static String machineFormatter(Block b) {
        return b.getWorld().getName()+ " "+b.getX() + " "+b.getY()+" "+b.getZ();
    }

    public static ArrayList<UUID> hasblocksregistered = new ArrayList<>();
    public static HashMap<Block, BukkitTask> BLOCKS = new HashMap<>();

    public static void placeCustomBlock(Player placer, CustomBlock cb, Block origin, Block placehere, ItemStack it, EquipmentSlot eq) {

        BlockPlaceEvent ev = new BlockPlaceEvent(placehere, origin.getState(), origin, it, placer, false, eq);
        Bukkit.getPluginManager().callEvent(ev);
        if(ev.isCancelled())
            return;

        if(origin.getWorld().getNearbyEntities(origin.getLocation().add(0.5, 0.5, 0.5), 0.5D, 0.5D, 0.5D).size() > 0)
            return;

        NoteBlock nb = (NoteBlock)Material.NOTE_BLOCK.createBlockData();
        nb.setNote(new Note(cb.getNote()));
        nb.setInstrument(InstrumentConverter.fromMinecraft(cb.getInstrument()));

        placehere.setBlockData(nb);
        placehere.getWorld().playSound(placehere.getLocation(), cb.getProperties().getSound()+".place", 1, 0.8F);

        ClientboundAnimatePacket animation = new ClientboundAnimatePacket(((CraftPlayer) placer).getHandle(), 0);
        for(Player bs : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) bs).getHandle().connection.send(animation);
        }

        if(placer.getGameMode() == GameMode.SURVIVAL || placer.getGameMode() == GameMode.ADVENTURE) {
            it.setAmount(it.getAmount()-1);
            placer.getInventory().setItem(eq, it);
        }

        if(cb.isMachine()) {
            MachineHandler.logMachine(cb, placehere, placer);
        }
    }

    public static void placeCustomPlant(Player placer, CustomPlant cb, Block origin, Block placehere, ItemStack it, EquipmentSlot eq) {

        BlockPlaceEvent ev = new BlockPlaceEvent(placehere, origin.getState(), origin, it, placer, false, eq);
        Bukkit.getPluginManager().callEvent(ev);
        if(ev.isCancelled())
            return;

        /*if(origin.getWorld().getNearbyEntities(origin.getLocation().add(0.5, 0.5, 0.5), 0.5D, 0.5D, 0.5D).size() > 0)
            return;*/

        Tripwire wire = (Tripwire)Material.TRIPWIRE.createBlockData();
        wire.setFace(BlockFace.NORTH, cb.isNorth());
        wire.setFace(BlockFace.SOUTH, cb.isSouth());
        wire.setFace(BlockFace.EAST, cb.isEast());
        wire.setFace(BlockFace.WEST, cb.isWest());
        wire.setAttached(cb.isAttached());
        wire.setDisarmed(cb.isDisarmed());
        wire.setPowered(cb.isPowered());

        placehere.setBlockData(wire, false);
        placehere.getWorld().playSound(placehere.getLocation(), cb.getPlantProperties().getSound()+".place", 1,
                cb.getPlantProperties().getSoundPitch());

        ClientboundAnimatePacket animation = new ClientboundAnimatePacket(((CraftPlayer) placer).getHandle(), 0);
        for(Player bs : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) bs).getHandle().connection.send(animation);
        }

        if(placer.getGameMode() == GameMode.SURVIVAL || placer.getGameMode() == GameMode.ADVENTURE) {
            it.setAmount(it.getAmount()-1);
            placer.getInventory().setItem(eq, it);
        }
    }

    public static void cancelBreaking(Player p, Block bd) {
        if(BlockHandler.BLOCKS.containsKey(bd)) {
            BlockHandler.BLOCKS.get(bd).cancel();
            if(!bd.getType().isAir()) {
                BlockPos bp = new BlockPos(bd.getX(), bd.getY(), bd.getZ());
                ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(0, bp, -1);
                for (Player bs : Bukkit.getOnlinePlayers()) {
                    ((CraftPlayer) bs).getHandle().connection.send(packet);
                }
            }
        }
    }

    public static HardnessResult calculateHardnessItem(Player p, CustomBlock cb, int dh) {
        ItemStack it = p.getInventory().getItemInMainHand();
        Material m = it.getType();
        ItemHandler.ItemType itt = ItemHandler.axeOrPickaxe(m);
        String baseMaterial = m.name().replace("_PICKAXE", "").replace("_AXE", "").trim();

        List<String> tools = cb.getProperties().getTools();

        if(it.containsEnchantment(Enchantment.EFFICIENCY)) {
            int level = it.getEnchantmentLevel(Enchantment.EFFICIENCY);
            dh = dh - level;
        }
        if(p.hasPotionEffect(PotionEffectType.HASTE)) {
            int level = p.getPotionEffect(PotionEffectType.HASTE).getAmplifier();
            dh = dh - level * 2;
        }

        boolean atLeastUsingRightTool = false;
        for(String s : tools) {
            if(s.equals("*") || s.equalsIgnoreCase(itt.name())) {
                atLeastUsingRightTool = true;
                switch(baseMaterial) {
                    case "WOODEN":
                        return new HardnessResult(dh-6, true);
                    case "STONE":
                        return new HardnessResult(dh-13, true);
                    case "IRON":
                        return new HardnessResult(dh-17, true);
                    case "DIAMOND":
                    case "GOLDEN": {
                        return new HardnessResult(dh-25, true);
                    }
                    case "NETHERITE": return new HardnessResult(dh-28, true);
                }
            }
        }

        if(m.isAir() || itt == ItemHandler.ItemType.OTHER)
            return new HardnessResult(dh*3, atLeastUsingRightTool);

        return new HardnessResult(dh, atLeastUsingRightTool);
    }

    public static void breakCustomPlant(@Nullable Player p, Block bd, CustomPlant cb) {
        /*BlockBreakEvent ev = new BlockBreakEvent(bd, p);
        Bukkit.getPluginManager().callEvent(ev);
        if(ev.isCancelled()) {
            return;
        }*/

        Location l = bd.getLocation().add(0.5,0.5,0.5);

        BlockPos bp = new BlockPos(bd.getX(), bd.getY(), bd.getZ());
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(0, bp, -1);
        for (Player bs : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) bs).getHandle().connection.send(packet);
        }

        if(p != null) {
            if(p.getGameMode() != GameMode.CREATIVE)
                bd.getWorld().spawnParticle(Particle.BLOCK, l, 80, 0.3, 0.3, 0.3, 1, bd.getBlockData(), true);
        }

        bd.getWorld().spawn(l, Item.class, is -> {
            is.setItemStack(cb.getItem().createMineSkyItem().toSpigotItem());
            is.setPickupDelay(15);
        });

        bd.getWorld().playSound(l, cb.getPlantProperties().getSound()+".break", 1, cb.getPlantProperties().getSoundPitch());

        bd.setType(Material.AIR, false);
    }

    public static void breakCustomBlock(Player p, Block bd, CustomBlock cb, boolean shouldDrop) {

        if(cb.isAlt())
            cb = new CustomBlock(cb.getIdWithoutAlt());

        BlockBreakEvent ev = new BlockBreakEvent(bd, p);
        ev.setDropItems(false);
        Bukkit.getPluginManager().callEvent(ev);
        if(ev.isCancelled()) {
            return;
        }

        ev.setDropItems(false);
        Location l = bd.getLocation().add(0.5,0.5,0.5);

        BlockPos bp = new BlockPos(bd.getX(), bd.getY(), bd.getZ());
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(0, bp, -1);
        for (Player bs : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) bs).getHandle().connection.send(packet);
        }

        if(p.getGameMode() != GameMode.CREATIVE)
            bd.getWorld().spawnParticle(Particle.BLOCK, l, 80, 0.3, 0.3, 0.3, 1, bd.getBlockData(), true);

        if(cb.isMachine()) {
            String formatter = machineFormatter(bd);
            try {
                if (cb.getMachine().isInventoryEnabled()) {
                    for (String s : MineSkyCustom.data.getConfigurationSection(formatter + ".inventory").getKeys(false)) {
                        String base64 = MineSkyCustom.data.getString(formatter + ".inventory." + s.trim());
                        if(base64 == null || base64.trim().isEmpty()) continue;
                        // p.sendMessage(base64 + " | "+s);
                        ItemStack it = Utils.itemStackFromBase64(base64);
                        if(it == null || it.getType().isAir()) continue;
                        bd.getWorld().spawn(l, Item.class, is -> {
                            is.setItemStack(it);
                            is.setPickupDelay(15);
                        });
                    }
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                return;
            }

            MineSkyCustom.data.set(formatter, null);
            MachineHandler.saveData();
        }

        if(shouldDrop) {
            CustomBlock finalCb = cb;
            bd.getWorld().spawn(l, Item.class, is -> {
                is.setItemStack(finalCb.getItem().createMineSkyItem().toSpigotItem());
                is.setPickupDelay(15);
            });
        }

        bd.getWorld().playSound(l, cb.getProperties().getSound()+".break", 1, 0.8f);

        bd.setType(Material.AIR);

    }

    public static boolean canBlockBeCustom(Material m) {
        return m == Material.NOTE_BLOCK || m == Material.TRIPWIRE || m == Material.STRING;
    }

    public static boolean canBlockBeCustom(Block block) {
        return block.getType() == Material.NOTE_BLOCK || block.getType() == Material.TRIPWIRE;
    }

    public static void playerTryingToBreak(Player p, Block origin, CustomBlock cb) {
        CustomBlockProperties properties = cb.getProperties();
        HardnessResult result = calculateHardnessItem(p, cb, properties.getHardness());

        BlockPos bp = new BlockPos(origin.getX(), origin.getY(), origin.getZ());

        BukkitTask b = new BukkitRunnable() {
            int n = 0;
            int soundN = 0;
            int breaktime = 0;
            Location l = p.getLocation();
            @Override
            public void run() {

                float f = ((float)n / (float)result.getHardness()) * (float)1;

                int stage = (int) (f * 10.0f);

                // Bukkit.broadcastMessage(stage+" | "+f);

                if(soundN == 4)
                    soundN = 0;

                if(soundN == 0) {
                    p.playSound(l, cb.getProperties().getSound()+".hit", 0.4f, 0);
                }

                if(stage != breaktime) {
                    if(breaktime <= 9) {
                        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(0, bp, breaktime);
                        for (Player bs : Bukkit.getOnlinePlayers()) {
                            ((CraftPlayer) bs).getHandle().connection.send(packet);
                        }
                    }
                    breaktime++;
                }

                // Bukkit.broadcastMessage(breaktime+"");

                if(breaktime == 12 || result.getHardness() <= 0/* || n == hardness*/ ) {
                    breakCustomBlock(p, origin, cb, result.isUsingRightTool());

                    this.cancel();
                    return;

                }

                RayTraceResult r = p.rayTraceBlocks(5, FluidCollisionMode.NEVER);
                if(r != null && r.getHitBlock() != null && !r.getHitBlock().getLocation().equals(origin.getLocation())) {
                    cancelBreaking(p, origin);
                    this.cancel();
                    return;
                }

                n++;
                soundN++;
            }
        }.runTaskTimer(MineSkyCustom.getInstance(), 0, 0);

        BLOCKS.put(origin, b);
    }

    // 120 = ADICIONAR BLOCO
    // 121 = REGISTRAR OS BLOCOS
    private static void sendAddBlockPacket(Player p, String s, int i) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(i);
        out.writeUTF(s);
        p.sendPluginMessage(MineSkyCustom.getInstance(), "minesky:main", out.toByteArray());
    }

    public static void loadBlocksToMineSkyMod(Player p) {
        if(hasblocksregistered.contains(p.getUniqueId()))
            return;

        int size = 0;
        for (CustomBlock cb : MineSkyCustom.REGISTERED_BLOCKS) {
            if(!cb.isAlt())
                size++;
        }

        final int sized = size;
        new BukkitRunnable() {
            int n = 0;
            @Override
            public void run() {
                if (n < sized) {
                    CustomBlockItem it = MineSkyCustom.REGISTERED_BLOCKS.get(n).getItem();
                    sendAddBlockPacket(p, it.getMaterial() + "," + it.getModel() + "," + it.getName(), 120);
                }
                if (sized == 0 || n >= sized) {
                    sendAddBlockPacket(p, "finish", 121);
                    hasblocksregistered.add(p.getUniqueId());
                    this.cancel();
                    return;
                }
                n++;
            }
        }.runTaskTimer(MineSkyCustom.getInstance(), 0, 3);
    }

    public static CustomObject getCustomObjectFromItemStack(ItemStack it) {
        int cmd = 0;
        final Material material = it.getType();

        if(it.hasItemMeta()) {
            ItemMeta im = it.getItemMeta();
            if(im.hasCustomModelData())
                cmd = im.getCustomModelData();
        }

        for(CustomBlock cb : MineSkyCustom.REGISTERED_BLOCKS) {
            CustomBlockItem cbitem = cb.getItem();
            if(cbitem.getModel() == cmd && material == cbitem.getSpigotMaterial())
                return new CustomObject(cb);
        }

        for(CustomPlant cp : MineSkyCustom.REGISTERED_PLANTS) {
            CustomPlantItem cpitem = cp.getItem();
            if(cpitem.getModel() == cmd && material == cpitem.getSpigotMaterial())
                return new CustomObject(cp);
        }

        return null;
    }
}
