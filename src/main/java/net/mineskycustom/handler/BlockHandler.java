package net.mineskycustom.handler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.mineskycustom.MineSkyCustom;
import net.mineskycustom.custom.CustomItem;
import net.mineskycustom.custom.CustomObject;
import net.mineskycustom.custom.blocks.CustomBlock;
import net.mineskycustom.custom.blocks.CustomBlockProperties;
import net.mineskycustom.custom.HardnessResult;
import net.mineskycustom.custom.plants.CustomPlant;
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
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockHandler {

    public static String machineFormatter(Block b) {
        return b.getWorld().getName()+ " "+b.getX() + " "+b.getY()+" "+b.getZ();
    }

    public static ArrayList<UUID> hasblocksregistered = new ArrayList<>();
    public static HashMap<Block, ScheduledTask> BLOCKS = new HashMap<>();

    public static Set<UUID> breakingWood = new HashSet<>();

    public static void placeCustomBlock(Player placer, CustomBlock cb, Block origin, Block placehere, ItemStack it, EquipmentSlot eq) {
        BlockPlaceEvent ev = new BlockPlaceEvent(placehere, origin.getState(), origin, it, placer, false, eq);
        Bukkit.getPluginManager().callEvent(ev);
        if(ev.isCancelled())
            return;

        if(!origin.getWorld().getNearbyEntities(origin.getLocation().add(0.5, 0.5, 0.5), 0.5D, 0.5D, 0.5D).isEmpty())
            return;

        NoteBlock nb = (NoteBlock)Material.NOTE_BLOCK.createBlockData();

        if(cb.getOrientedValues() != null) {
            CustomBlock.OrientedValues oriented = cb.getOrientedValues();
            BlockFace opposite = placer.getFacing().getOppositeFace();

            CustomBlock.OrientedValue value = switch (opposite) {
                case SOUTH -> oriented.south();
                case WEST -> oriented.west();
                case EAST -> oriented.east();
                default -> oriented.north();
            };

            nb.setNote(new Note(value.note()));
            nb.setInstrument(InstrumentConverter.fromMinecraft(value.instrument()));

            if(value.fakeBlocks().size() >= 3) {
                final Location relative = placehere.getLocation().clone()
                        .add(value.fakeBlocks().get(0), value.fakeBlocks().get(1), value.fakeBlocks().get(2));

                if(!placehere.getWorld().getType(relative).isAir()
                || !placehere.getWorld().getNearbyEntities(
                        relative.clone().add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5).isEmpty()) {
                    return;
                }

                placehere.getWorld().setType(relative, Material.BARRIER);
            }
        } else {
            nb.setNote(new Note(cb.getNote()));
            nb.setInstrument(InstrumentConverter.fromMinecraft(cb.getInstrument()));
        }

        placehere.setBlockData(nb);
        placehere.getWorld().playSound(placehere.getLocation(), cb.getProperties().getSound()+".place", 1, 0.8F);

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

        armSwingAnimation(placer);

        if(placer.getGameMode() == GameMode.SURVIVAL || placer.getGameMode() == GameMode.ADVENTURE) {
            it.setAmount(it.getAmount()-1);
            placer.getInventory().setItem(eq, it);
        }
    }

    public static void armSwingAnimation(Player p) {
        ClientboundAnimatePacket animation = new ClientboundAnimatePacket(((CraftPlayer) p).getHandle(), 0);
        for(Player bs : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) bs).getHandle().connection.send(animation);
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

        bd.getWorld().playSound(l, cb.getPlantProperties().getSound()+".break", 1, cb.getPlantProperties().getSoundPitch());

        bd.setType(Material.AIR, false);

        if(p != null && p.getGameMode() == GameMode.CREATIVE)
            return;

        bd.getWorld().spawn(l, Item.class, is -> {
            is.setItemStack(cb.getItem().toSpigotItem());
            is.setPickupDelay(15);
        });
    }

    public static void breakCustomBlock(@Nullable Player p, @Nullable ItemStack item, Block bd, CustomBlock cb, boolean particles, boolean shouldDrop) {
        if(cb == null)
            return;

        if(cb.isAlt()) {
            CustomBlock alt = null;
            for(CustomBlock block : MineSkyCustom.REGISTERED_BLOCKS) {
                if(block.getId().equalsIgnoreCase(cb.getIdWithoutAlt())) {
                    alt = block;
                    break;
                }
            }

            if(alt != null) {
                if(p != null)
                    p.sendMessage(Component.text("Um erro ocorreu ao quebrar esse bloco.").color(NamedTextColor.RED));
                return;
            }
        }

        if(p != null) {
            BlockBreakEvent ev = new BlockBreakEvent(bd, p);
            ev.setDropItems(false);
            Bukkit.getPluginManager().callEvent(ev);
            if (ev.isCancelled()) {
                return;
            }
            ev.setDropItems(false);
        }

        Location l = bd.getLocation().add(0.5,0.5,0.5);

        BlockPos bp = new BlockPos(bd.getX(), bd.getY(), bd.getZ());
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(0, bp, -1);
        for (Player bs : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) bs).getHandle().connection.send(packet);
        }

        if(particles)
            bd.getWorld().spawnParticle(Particle.BLOCK, l,
                    80, 0.3, 0.3, 0.3, 1, bd.getBlockData(), true);

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
            bd.getWorld().spawn(l, Item.class, is -> {
                if (cb.getGemItem() != null && item != null && item.getEnchantmentLevel(Enchantment.SILK_TOUCH) == 0) {
                    int level = item.getEnchantmentLevel(Enchantment.FORTUNE);
                    int amount = 1;

                    if (level > 0) { // simulates fortune
                        int roll = java.util.concurrent.ThreadLocalRandom.current().nextInt(level + 2);
                        int bonus = Math.max(0, roll - 1);
                        amount += bonus;
                    }

                    is.setItemStack(cb.getGemItem().toSpigotItem(amount));
                } else {
                    is.setItemStack(cb.getItem().toSpigotItem());
                }
                is.setPickupDelay(15);
            });
        }

        bd.getWorld().playSound(l, cb.getProperties().getSound()+".break", 1, 0.8f);
        bd.setType(Material.AIR);

        breakOrientedValues(cb, bd, bd.getLocation().clone());
    }

    public static void breakOrientedValues(final CustomBlock cb, final Block bd, final Location origin) {
        if(cb.getOrientedValues() == null)
            return;

        final CustomBlock.OrientedValues values = cb.getOrientedValues();

        CustomBlock.OrientedValue value = null;
        for(CustomBlock.OrientedValue find : values.values()) {
            if(find.note() == cb.getNote()
                    && find.instrument().equalsIgnoreCase(cb.getInstrument())) {
                value = find;
                break;
            }
        }

        if(value == null)
            return;

        if(value.fakeBlocks().size() < 3)
            return;

        final Location barrier = origin.add(value.fakeBlocks().get(0), value.fakeBlocks().get(1), value.fakeBlocks().get(2));
        Bukkit.getRegionScheduler().run(MineSkyCustom.getInstance(), barrier, (task) -> {
            if(origin.getWorld().getType(barrier) == Material.BARRIER) {
                origin.getWorld().setType(barrier, Material.AIR);
                origin.getWorld().spawnParticle(Particle.BLOCK, barrier, 80, 0.3, 0.3, 0.3, 1, bd.getBlockData(), true);
            }
        });
    }

    public static boolean canBlockBeCustom(Material m) {
        return m == Material.NOTE_BLOCK || m == Material.TRIPWIRE || m == Material.STRING;
    }

    public static boolean canBlockBeCustom(Block block) {
        return block.getType() == Material.NOTE_BLOCK || block.getType() == Material.TRIPWIRE;
    }

    public static final float fakeSoundVolume = 0.3F;

    public static void playerBreakingWood(Player p, final Block origin) {
        final UUID uuid = p.getUniqueId();

        if(breakingWood.contains(uuid))
            return;

        breakingWood.add(uuid);

        p.getScheduler().runAtFixedRate(MineSkyCustom.getInstance(), new java.util.function.Consumer<>() {
            final Location l = p.getLocation();
            int n = 0;
            @Override
            public void accept(ScheduledTask task) {
                if(origin.getType().isAir() || n >= 100 || !p.isOnline() || p.isDead() || !breakingWood.contains(uuid)) {
                    task.cancel();
                    breakingWood.remove(uuid);
                    return;
                }

                RayTraceResult r = p.rayTraceBlocks(5, FluidCollisionMode.NEVER);
                if (r != null && r.getHitBlock() != null && !r.getHitBlock().getLocation().equals(origin.getLocation())) {
                    task.cancel();
                    breakingWood.remove(uuid);
                    return;
                }

                p.playSound(p, "minesky.replacement.block.wood.hit", fakeSoundVolume, 0);

                n++;
            }
        }, () -> {}, 1, 4);
    }

    public static void playerTryingToBreak(Player p, ItemStack item, Block origin, CustomBlock cb) {
        CustomBlockProperties properties = cb.getProperties();
        HardnessResult result = calculateHardnessItem(p, cb, properties.getHardness());

        BlockPos bp = new BlockPos(origin.getX(), origin.getY(), origin.getZ());

        AtomicInteger n = new AtomicInteger();
        AtomicInteger soundN = new AtomicInteger();
        AtomicInteger breaktime = new AtomicInteger();

        final Location originLocation = origin.getLocation();
        ScheduledTask b = Bukkit.getRegionScheduler().runAtFixedRate(MineSkyCustom.getInstance(), originLocation, (task) -> {
            float f = ((float) n.get() / (float) result.getHardness()) * (float) 1;

            int stage = (int) (f * 10.0f);

            // Bukkit.broadcastMessage(stage+" | "+f);

            if(soundN.get() == 4)
                soundN.set(0);

            if(soundN.get() == 0) {
                p.playSound(originLocation, cb.getProperties().getSound()+".hit", 0.4f, 0);
            }

            final int breakT = breaktime.get();
            if(stage != breakT) {
                if(breakT <= 9) {
                    ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(0, bp, breakT);
                    for (Player bs : Bukkit.getOnlinePlayers()) {
                        ((CraftPlayer) bs).getHandle().connection.send(packet);
                    }
                }
                breaktime.incrementAndGet();
            }

            if(breakT == 12 || result.getHardness() <= 0/* || n == hardness*/ ) {
                breakCustomBlock(p, item,  origin, cb, true, result.isUsingRightTool());
                task.cancel();
                return;
            }

            RayTraceResult r = p.rayTraceBlocks(5, FluidCollisionMode.NEVER);
            if(r != null && r.getHitBlock() != null && !r.getHitBlock().getLocation().equals(origin.getLocation())) {
                cancelBreaking(p, origin);
                task.cancel();
                return;
            }

            n.getAndIncrement();
            soundN.getAndIncrement();
        }, 1, 1);

        BLOCKS.put(origin, b);
    }

    public static record BlockEntry(CustomBlock customBlock, Block block) {}

    private static @Nullable BlockEntry checkRelativeFromFake(Location origin, Block barrier, Block block) {
        if(block.getType() != Material.NOTE_BLOCK)
            return null;

        CustomBlock custom = getCustomBlock((NoteBlock) block.getBlockData());
        if(custom == null || custom.getOrientedValues() == null)
            return null;

        for(CustomBlock.OrientedValue value : custom.getOrientedValues().values()) {
            final List<Integer> fakes = value.fakeBlocks();
            if(fakes.size() >= 3 || origin.clone().add(fakes.get(0), fakes.get(1), fakes.get(2)).equals(block.getLocation())) {
                return new BlockEntry(custom, block);
            }
        }

        return null;
    }

    public static @Nullable BlockEntry findCustomBlockFromFake(Location origin, Block barrier) {
        if(barrier.getType() != Material.BARRIER)
            return null;

        BlockEntry block = checkRelativeFromFake(origin, barrier, barrier.getRelative(BlockFace.WEST));
        if(block == null)
            block = checkRelativeFromFake(origin, barrier, barrier.getRelative(BlockFace.EAST));
        if(block == null)
            block = checkRelativeFromFake(origin, barrier, barrier.getRelative(BlockFace.SOUTH));
        if(block == null)
            block = checkRelativeFromFake(origin, barrier, barrier.getRelative(BlockFace.NORTH));

        return block;
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
                    CustomItem it = MineSkyCustom.REGISTERED_BLOCKS.get(n).getItem();
                    sendAddBlockPacket(p, it.material() + "," + it.model() + "," + it.name(), 120);
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

    public static @Nullable CustomPlant getCustomPlant(Block block) {
        for(CustomPlant cb : MineSkyCustom.REGISTERED_PLANTS) {
            if(cb.isSame(block))
                return cb;
        }
        return null;
    }

    public static @Nullable CustomBlock getCustomBlock(Block block) {
        final Material material = block.getType();

        return (material == Material.NOTE_BLOCK ? getCustomBlock((NoteBlock) block.getBlockData()) : null);
    }

    public static @Nullable CustomBlock getCustomBlock(NoteBlock noteBlock) {
        for(CustomBlock cb : MineSkyCustom.REGISTERED_BLOCKS) {
            if(noteBlock.getNote().getId() == cb.getNote() && noteBlock.getInstrument() == InstrumentConverter.fromMinecraft(cb.getInstrument()))
                return cb; 
        }
        return null;
    }

    public static @Nullable CustomObject getCustomObjectFromItemStack(@Nullable ItemStack it) {
        if(it == null) return null;

        int cmd = 0;
        final Material material = it.getType();

        if(it.hasItemMeta()) {
            ItemMeta im = it.getItemMeta();
            if(im.hasCustomModelData())
                cmd = im.getCustomModelData();
        }

        for(CustomBlock cb : MineSkyCustom.REGISTERED_BLOCKS) {
            final CustomItem item = cb.getItem();
            if(!item.isMineSkyItem()) {
                if(item.model() == cmd && material == item.spigotMaterial())
                    return new CustomObject(cb);
            } else {
                final net.mineskyitems.entities.item.Item custom = net.mineskyitems.entities.item.ItemHandler.getItemById(item.mineskyItemId());
                if(custom != null && custom.buildStack().isSimilar(it))
                    return new CustomObject(cb);
            }
        }

        for(CustomPlant cp : MineSkyCustom.REGISTERED_PLANTS) {
            final CustomItem item = cp.getItem();
            if(!item.isMineSkyItem()) {
                if(item.model() == cmd && material == item.spigotMaterial())
                    return new CustomObject(cp);
            } else {
                final net.mineskyitems.entities.item.Item custom = net.mineskyitems.entities.item.ItemHandler.getItemById(item.mineskyItemId());
                if(custom != null && custom.buildStack().isSimilar(it))
                    return new CustomObject(cp);
            }
        }

        return null;
    }
}
