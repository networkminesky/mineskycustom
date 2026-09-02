package net.mineskycustom.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.mineskycustom.MineSkyCustom;
import net.mineskycustom.custom.CustomObject;
import net.mineskycustom.custom.blocks.CustomBlock;
import net.mineskycustom.custom.codeditems.ThrowableItemHandler;
import net.mineskycustom.custom.plants.CustomPlant;
import net.mineskycustom.custom.plants.CustomPlantProperties;
import net.mineskycustom.handler.ActionHandler;
import net.mineskycustom.handler.BlockHandler;
import net.mineskycustom.utils.Utils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class InteractEvents implements Listener {

    private static ArrayList<UUID> duplicateFixer = new ArrayList<>();
    private static ArrayList<UUID> forcePlace = new ArrayList<>();

    @EventHandler
    public void onNotePlay(NotePlayEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        BlockHandler.hasblocksregistered.remove(e.getPlayer().getUniqueId());
    }

    /*@EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block aboveBlock = event.getBlock().getLocation().add(0, 1, 0).getBlock();
        if (aboveBlock.getType() == Material.NOTE_BLOCK) {
            updateAndCheck(event.getBlock().getLocation());
            event.setCancelled(true);
            // aboveBlock.getState().update(true, true);
        }
        try {
            if (event.getBlock().getType() == Material.NOTE_BLOCK)
                event.setCancelled(true);
            if (event.getBlock().getType().toString().toLowerCase().contains("sign"))
                return;
            if (event.getBlock().getType() != Material.LECTERN) // (resolvendo bug com griefprevention)
                event.getBlock().getState().update(true, false);
        } catch(Exception ex) {}
    }*/
    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockPhysics(BlockPhysicsEvent event) {
        Block b = event.getBlock(),
                topBlock = b.getRelative(BlockFace.UP),       // Block (y + 1)
                bottomBlock = b.getRelative(BlockFace.DOWN);  // Block (y - 1)

        if(BlockHandler.canBlockBeCustom(b) || b.getType() == Material.TRIPWIRE_HOOK) {
            event.setCancelled(true);
        }

        /*if(b.getType() == Material.TRIPWIRE) {
            Tripwire wire = (Tripwire) b.getBlockData();
            wire.getAllowedFaces().forEach(face -> {
                Block novo = b.getRelative(face);
                Bukkit.broadcastMessage("checking: "+face);
                updateAndCheck(novo.getLocation());
            });
        }*/

        if (topBlock.getType() == Material.NOTE_BLOCK) {
            updateAndCheck(b.getLocation());
            if (Tag.DOORS.isTagged(b.getType()) && b.getBlockData() instanceof Door) {
                Door data = (Door) b.getBlockData();
                if (!data.getHalf().equals(Bisected.Half.TOP)) return;
                Door d = (Door) bottomBlock.getBlockData();
                d.setOpen(data.isOpen());
                bottomBlock.setBlockData(d);
                bottomBlock.getState().update(true, false);
            }
            event.setCancelled(true);
            if (!Tag.SIGNS.isTagged(b.getType()) && !b.getType().equals(Material.LECTERN) && !b.getType().toString().contains("BEE"))
                b.getState().update(true, false);
        }
    }

    @EventHandler
    public void onPower(BlockRedstoneEvent e) {
        if(BlockHandler.canBlockBeCustom(e.getBlock())) {
            e.setNewCurrent(e.getOldCurrent());
        }
    }

    public void updateAndCheck(Location loc) {
        Block b = loc.getBlock().getRelative(BlockFace.UP);
        if (b.getType() == Material.NOTE_BLOCK)
            b.getState().update(true, true);
        Block nextBlock = b.getRelative(BlockFace.UP);
        if (nextBlock.getType() == Material.NOTE_BLOCK)
            updateAndCheck(b.getLocation());
    }

    @EventHandler
    public void blockFrom(BlockFromToEvent e) {
        if(e.isCancelled())
            return;

        final Block to = e.getToBlock();
        if(to.getType() == Material.TRIPWIRE) {
            e.setCancelled(true);
            to.setType(Material.AIR);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        final Block b = e.getBlock();
        final Player p = e.getPlayer();

        if(e.isCancelled())
            return;

        if(b.getType() == Material.NOTE_BLOCK
                && p.getGameMode() == GameMode.CREATIVE) {
            final CustomBlock customBlock = BlockHandler.getCustomBlock((NoteBlock) b.getBlockData());
            if(customBlock != null) {
                BlockHandler.breakOrientedValues(customBlock, b, b.getLocation());
                return;
            }
        }

        if(b.getType() == Material.NOTE_BLOCK
                && e.isDropItems()
                && p.getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

        if(b.getType() == Material.BARRIER
        && p.getGameMode() == GameMode.CREATIVE /*duh*/) {
            BlockHandler.BlockEntry custom = BlockHandler.findCustomBlockFromFake(b.getLocation(), b);
            if(custom != null)
                BlockHandler.breakCustomBlock(p, e.getPlayer().getInventory().getItemInMainHand(), custom.block(), custom.customBlock(), true, false);
            return;
        }

        if(b.getType() == Material.TRIPWIRE) {
            e.setDropItems(false);

            for (CustomPlant rb : MineSkyCustom.REGISTERED_PLANTS) {
                if (rb.isSame(b)) {
                    BlockHandler.breakCustomPlant(e.getPlayer(), b, rb);
                    return;
                }
            }

            //e.setCancelled(true);
            //b.setType(Material.AIR, false);
            //Bukkit.broadcastMessage("cancelou");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent e) {
        if(e.isCancelled())
            return;

        Block b = e.getBlock();
        if(b.getType() == Material.LIGHTNING_ROD) {
            LightningRod r = (LightningRod) b.getBlockData();

            for (BlockFace f : Utils.blockfaces) {
                Block bc = b.getRelative(f);
                if (bc.getType() == Material.LIGHTNING_ROD && r.getFacing().getOppositeFace() != f) {
                    LightningRod rod = (LightningRod) bc.getBlockData();
                    Block be = bc.getRelative(rod.getFacing());
                    if (be.getLocation().equals(b.getLocation())) {
                        rod.setPowered(true);
                        bc.setBlockData(rod);
                    }
                }
            }
        }

        if(b.getType() == Material.TRIPWIRE) {

            Tripwire wire = (Tripwire) b.getBlockData();
            wire.getAllowedFaces().forEach(a -> {
                wire.setFace(a, false);
            });
            wire.setDisarmed(false);
            wire.setPowered(false);

            b.setBlockData(wire, false);

        }

        if(b.getType() == Material.TRIPWIRE_HOOK) {

            TripwireHook wire = (TripwireHook) b.getBlockData();
            wire.setAttached(false);

            b.setBlockData(wire);

        }
    }

    @EventHandler
    public void damage(EntityDamageEvent e) {
        if(e.getEntityType() != EntityType.PLAYER)
            return;

        if(e.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;

        Block b = e.getEntity().getLocation().getBlock().getRelative(BlockFace.DOWN);

        if(b.getType() == Material.NOTE_BLOCK) {
            if(b.getBlockData() instanceof NoteBlock nb) {
                if(nb.getInstrument() == Instrument.BANJO) {
                    int note = nb.getNote().getId();
                    final float calculated = (e.getEntity().getFallDistance());
                    if (note == 8 || note == 9) {
                        e.setCancelled(true);
                        e.getEntity().setFallDistance(calculated);
                        b.getWorld().playSound(b.getLocation(), "block.slime_block.fall", 1, 0f);
                        e.getEntity().setVelocity(new Vector(0, (calculated/10f), 0));
                    }
                }
            }
        }
    }

    public static boolean parryAllowed(Player p) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(p);
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(p.getLocation()));

        return set.testState(localPlayer, MineSkyCustom.PARRY_FLAG);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent e) {

        Entity damager = e.getDamager();

        if(e.isCancelled())
            return;

        // parry
        if(e.getEntity() instanceof Player damaged) {
            if(damaged.isBlocking() && MineSkyCustom.blockingTicks.getOrDefault(damaged, 999) <= 3
            && damaged.getLocation().distance(damager.getLocation()) <= 4.5
            && damaged.getCooldown(Material.SHIELD) == 0
            && parryAllowed(damaged)) {

                damaged.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 30, 5, false, false, false));

                if(damaged.getInventory().getItemInMainHand().getType() == Material.SHIELD)
                    damaged.swingMainHand();
                else
                    damaged.swingOffHand();

                Vector direction = damaged.getLocation().toVector().subtract(damager.getLocation().toVector()).normalize();

                Location l = damaged.getLocation();
                l.setPitch(0);

                direction.multiply(-0.9);

                direction.setY(0.4);

                damaged.setVelocity(l.getDirection().multiply(-0.5).setY(0.05));
                damager.setVelocity(direction);

                if(damager instanceof Damageable da) {
                    da.damage(e.getDamage()/2);
                }

                //AdvancementManager.awardAdvancement(damaged, new NamespacedKey("minesky", "parry"), "0");

                damaged.getWorld().playSound(damaged.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 2);
                damaged.getWorld().playSound(damaged.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1, 0.8f);

                damaged.getWorld().spawnParticle(Particle.END_ROD, damaged.getLocation().add(0, 1, 0), 10);

                damaged.setCooldown(Material.SHIELD, 20);

                e.setCancelled(true);
            }
        }

        if(!(e.getDamager() instanceof Player p))
            return;

        if(e.isCancelled())
            return;

        /*
        if(!p.getInventory().getItemInMainHand().getType().isAir()) {
            final ItemStack it = p.getInventory().getItemInMainHand();

            new BukkitRunnable() {
                @Override
                public void run() {
                    final net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(it);
                    final CompoundTag tag = nmsItem.getOrCreateTag();
                    if(tag == null) return;
                    if (tag.contains("mineskyvfx")) {
                        if (tag.getString("mineskyvfx_method").equals("damage")
                        || tag.getString("mineskyvfx_method").equals("damage_and_interaction")) {
                            String vfx = tag.getString("mineskyvfx");
                            VFXHandler.playVFXGroup(VFXHandler.getVFXGroupByID(vfx), p);
                        }
                    }
                }
            }.runTaskAsynchronously(MineSkyCustom.getInstance());
        }*/
    }

    @EventHandler
    public void onPick(PlayerPickBlockEvent e) {
        final Player p = e.getPlayer();
        final Block b = e.getBlock();

        ItemStack spigotItem = null;

        if(b.getType() == Material.NOTE_BLOCK) {
            final CustomBlock customBlock = BlockHandler.getCustomBlock((NoteBlock) b.getBlockData());
            if(customBlock != null) {
                spigotItem = customBlock.getItem().toSpigotItem();
            }
        } else if(b.getType() == Material.TRIPWIRE) {
            // Tripwire
            for(CustomPlant cp : MineSkyCustom.REGISTERED_PLANTS) {
                if(cp.isSame(b))
                    spigotItem = cp.getItem().toSpigotItem();
            }
        }

        if(spigotItem != null) {
            e.setCancelled(true);
            final PlayerInventory inventory = p.getInventory();

            if(inventory.containsAtLeast(spigotItem, 1)) {
                for(int i = 0; i < 9; i++) {
                    final ItemStack itemStack = inventory.getItem(i);
                    if(itemStack != null && itemStack.isSimilar(spigotItem)) {
                        p.getInventory().setHeldItemSlot(i);
                        return;
                    }
                }
            }

            if(p.getGameMode() != GameMode.CREATIVE)
                return;

            p.getInventory().setHeldItemSlot(e.getTargetSlot());
            p.getInventory().setItem(e.getTargetSlot(), spigotItem);
            return;
        }
    }

    /* REMOVE LEGACY
    @EventHandler
    public void onItem(InventoryCreativeEvent e) {
        Player p = (Player)e.getWhoClicked();
        ItemStack cursor = e.getCursor();

        if(e.getAction()
        == InventoryAction.PLACE_ALL
        && e.getClick() == ClickType.CREATIVE
        && p.getOpenInventory().getType() == InventoryType.CREATIVE
        && BlockHandler.canBlockBeCustom(cursor.getType())) {
            RayTraceResult result = p.rayTraceBlocks(4.9);
            if(result != null && result.getHitBlock() != null &&
                    BlockHandler.canBlockBeCustom(result.getHitBlock())) {

                final Block hitblock = result.getHitBlock();
                ItemStack spigotItem = null;

                if(hitblock.getType() == Material.NOTE_BLOCK) {
                    for(CustomBlock cb : MineSkyCustom.REGISTERED_BLOCKS) {
                        if(cb.isSame(hitblock))
                            spigotItem = cb.getItem().createMineSkyItem().toSpigotItem();
                    }

                } else {
                    // Tripwire
                    for(CustomPlant cp : MineSkyCustom.REGISTERED_PLANTS) {
                        if(cp.isSame(hitblock))
                            spigotItem = cp.getItem().createMineSkyItem().toSpigotItem();
                    }

                }

                if(spigotItem == null)
                    return;

                boolean alterouAlgo = false;

                for(int slot = 0; slot < 35; slot++) {
                    ItemStack item = p.getInventory().getItem(slot);
                    if(item != null) {
                        if(item.isSimilar(spigotItem)) {

                            e.setCancelled(true);

                            spigotItem.setAmount(item.getAmount());

                            if(slot <= 8) {
                                p.getInventory().setHeldItemSlot(slot);
                                return;
                            }


                            if(p.getInventory().getItemInMainHand().getType().isAir()) {

                                p.getInventory().setItem(slot, null);

                                p.getInventory().setItemInMainHand(spigotItem);

                            } else {}
                            return;
                        }
                    }
                }

                e.setCancelled(true);

                if(p.getInventory().getItemInMainHand().getType().isAir()) {
                    p.getInventory().setItemInMainHand(spigotItem);
                } else {
                    if(p.getInventory().firstEmpty() == -1) {
                        p.getInventory().setItemInMainHand(spigotItem);
                    } else {
                        if(p.getInventory().firstEmpty() <= 8) {
                            p.getInventory().addItem(spigotItem);
                            p.getInventory().setHeldItemSlot(e.getSlot());
                        } else {
                            final ItemStack actualItem = e.getCurrentItem();
                            p.getInventory().addItem(actualItem);
                            p.getInventory().setItemInMainHand(spigotItem);
                        }
                    }

                    p.getInventory().setItemInMainHand(spigotItem);
                }
            }
        }
    }*/

    @EventHandler
    public void onBreak(BlockExplodeEvent e) {
        if(e.getBlock().getType().equals(Material.NOTE_BLOCK)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBreak(EntityExplodeEvent e) {
        if(!e.getExplosionResult().name().contains("DESTROY"))
            return;

        if(e.isCancelled())
            return;

        e.blockList().removeIf((b) -> {
            boolean is = b.getType() == Material.NOTE_BLOCK || b.getType() == Material.TRIPWIRE;
            if(b.getType() == Material.NOTE_BLOCK) {
                CustomBlock custom = BlockHandler.getCustomBlock(b);
                if(custom == null) return false;

                b.setType(Material.AIR, true);
                BlockHandler.breakCustomBlock(null, null, b, custom, true, true);
            }
            if(b.getType() == Material.TRIPWIRE) {
                CustomPlant custom = BlockHandler.getCustomPlant(b);
                if(custom == null) return false;

                b.setType(Material.AIR, true);
                BlockHandler.breakCustomPlant(null, b, custom);
            }
            return is;
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPiston(BlockPistonExtendEvent e) {
        if(e.isCancelled())
            return;

        for(Block block : e.getBlocks()) {
            if(block.getType() == Material.TRIPWIRE) {
                CustomPlant plant = BlockHandler.getCustomPlant(block);
                if(plant != null) {
                    BlockHandler.breakCustomPlant(null, block, plant);
                }
            }

            if(block.getType() == Material.NOTE_BLOCK) {
                CustomBlock custom = BlockHandler.getCustomBlock(block);
                if(custom != null && custom.getOrientedValues() != null) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    /*@EventHandler
    public void onPiston(BlockPistonRetractEvent e) {
        if(e.isCancelled())
            return;

        for(Block block : e.getBlocks()) {
            if(block.getType() == Material.NOTE_BLOCK) {
                e.setCancelled(true);
                return;
            }
        }
    }*/

    @EventHandler
    public void onElytraBoost(PlayerElytraBoostEvent event) {
        Player player = event.getPlayer();

        if(player.getCooldown(Material.FIREWORK_ROCKET) > 0) {
            event.setCancelled(true);
            return;
        }

        player.getServer().getScheduler().runTask(MineSkyCustom.getInstance(), () -> {
            if (player.isGliding()) {
                Vector currentVelocity = player.getVelocity();
                Vector nerfedVelocity = currentVelocity.multiply(0.5);

                player.setCooldown(Material.FIREWORK_ROCKET, 60);
                player.setVelocity(nerfedVelocity);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        // VFX GROUP PLAYER
        /*
        new BukkitRunnable() {
            @Override
            public void run() {
                if(e.hasItem()
                        && e.getHand() == EquipmentSlot.HAND) {

                    ItemStack it = e.getItem();
                    net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(it);
                    CompoundTag tag = nmsItem.getOrCreateTag();

                    // mmoitems hook
                    if(tag != null && tag.contains("MMOITEMS_ITEM_TYPE")
                    && e.getAction().name().contains("LEFT")) {
                        String mmoItemsType = tag.getString("MMOITEMS_ITEM_TYPE");
                        if(mmoItemsType.equals("SWORD") || mmoItemsType.equals("KATANA")) {
                            VFXHandler.playVFXGroup(VFXHandler.getVFXGroupByID("ESPADA_INICIAL"), e.getPlayer().getLocation());
                            return;
                        }

                        if(mmoItemsType.equals("SPEAR")) {
                            VFXHandler.playVFXGroup(VFXHandler.getVFXGroupByID("LANCA_INICIAL"), e.getPlayer().getLocation());
                            return;
                        }
                    }

                    if(tag != null && tag.contains("mineskyvfx")) {
                        if(tag.getString("mineskyvfx_method").equals("interaction")
                        || (tag.getString("mineskyvfx_method").equals("damage_and_interaction")
                              && e.getAction().name().contains("LEFT_"))) {
                            String vfx = tag.getString("mineskyvfx");
                            VFXHandler.playVFXGroup(VFXHandler.getVFXGroupByID(vfx), e.getPlayer().getLocation());
                            return;
                        }

                        if(tag.getString("mineskyvfx_method").equals("interaction_leftclick") &&
                                e.getAction().name().contains("LEFT")) {
                            String vfx = tag.getString("mineskyvfx");
                            VFXHandler.playVFXGroup(VFXHandler.getVFXGroupByID(vfx), e.getPlayer().getLocation());
                            return;
                        }

                        if(tag.getString("mineskyvfx_method").equals("interaction_rightclick") &&
                                e.getAction().name().contains("RIGHT")) {
                            String vfx = tag.getString("mineskyvfx");
                            VFXHandler.playVFXGroup(VFXHandler.getVFXGroupByID(vfx), e.getPlayer().getLocation());
                            return;
                        }
                    }
                }
            }
            // trocando pra main thread pq ta bugando
        }.runTask(MineSkyCustom.getInstance());
        */

        final @Nullable ItemStack item = e.getItem();
        final Action action = e.getAction();
        final Player p = e.getPlayer();

        if (!e.hasBlock()) return;

        final Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null) return;

        final EquipmentSlot hand = e.getHand();
        final boolean canBeCustom = BlockHandler.canBlockBeCustom(clickedBlock);

        if (canBeCustom) {
            if (hand == EquipmentSlot.OFF_HAND || action == Action.PHYSICAL) {
                e.setCancelled(true);
                return;
            }
        }

        if (hand != EquipmentSlot.HAND) return;

        if (action == Action.RIGHT_CLICK_BLOCK) {
            // Action handler
            if(clickedBlock.getType() == Material.NOTE_BLOCK) {
                final CustomBlock customBlock = BlockHandler.getCustomBlock((NoteBlock) clickedBlock.getBlockData());

                if (customBlock != null && !customBlock.getProperties().getAction().isEmpty()) {
                    if(item == null || (!p.isSneaking())) {
                        if(duplicateFixer(p))
                            return;

                        e.setCancelled(true);
                        ActionHandler.executeAction(p, customBlock.getProperties().getAction(), clickedBlock, customBlock);
                        return;
                    }
                }
            }

            // handle interactions in fake-blocks
            if(clickedBlock.getType() == Material.BARRIER) {
                if(p.isSneaking()) return;

                BlockHandler.BlockEntry entry = BlockHandler.findCustomBlockFromFake(clickedBlock.getLocation(), clickedBlock);
                if(entry != null) {
                    if(!entry.customBlock().getProperties().getAction().isEmpty()) {
                        e.setCancelled(true);
                        BlockHandler.armSwingAnimation(p); // force arm swing because it's not a note_block
                        ActionHandler.executeAction(p, entry.customBlock().getProperties().getAction(), clickedBlock, entry.customBlock());
                        return;
                    }
                }
            }

            if (item != null) {
                if(BlockHandler.getCustomObjectFromItemStack(item) != null && duplicateFixer(p)) {
                    e.setCancelled(true);
                    return;
                }

                final Block placeBl = clickedBlock.getRelative(e.getBlockFace());

                if (clickedBlock.getType() == Material.NOTE_BLOCK) {
                    if (!item.getType().isBlock()) {
                        e.setCancelled(true);
                    }

                    if (item.getType().isBlock()) {
                        boolean hasNearbyEntities = !p.getWorld().getNearbyEntities(
                                placeBl.getLocation().add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5
                        ).isEmpty();

                        if(!p.isSneaking()
                        && !hasNearbyEntities
                        && placeBl.getType().isAir()) {
                            p.setSneaking(true);
                            p.getScheduler().runDelayed(MineSkyCustom.getInstance(), (task) -> p.setSneaking(false), null, 2);

                            clickedBlock.getWorld().playSound(
                                    clickedBlock.getLocation(),
                                    item.getType().createBlockData().getSoundGroup().getPlaceSound(),
                                    1.0F,
                                    0.8F
                            );
                        }
                    } else {
                        e.setCancelled(true);
                    }
                }

                if (clickedBlock.getType() != Material.NOTE_BLOCK && clickedBlock.getType().getHardness() != 0.0F) {
                    p.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(1.0);
                }

                if(Utils.isInteractable(clickedBlock.getType())
                        && !p.isSneaking())
                    return;

                final CustomObject object = BlockHandler.getCustomObjectFromItemStack(item);
                if (object == null) return;

                final Material pm = placeBl.getType();
                final boolean canPlace = pm.isAir() || (!pm.isSolid() && !pm.isBlock() && !pm.isOccluding() && !Utils.isInteractable(pm));
                if (!canPlace) return;

                if (object.isCustomBlock()) {
                    boolean hasLivingEntity = false;
                    for (Entity en : placeBl.getWorld().getNearbyEntities(placeBl.getLocation().add(0.5, 0.0, 0.5), 0.5, 1.0, 0.5)) {
                        if (en instanceof LivingEntity) {
                            hasLivingEntity = true;
                            break;
                        }
                    }

                    if (hasLivingEntity) return;

                    if(clickedBlock.getType() != Material.NOTE_BLOCK) {
                        BlockHandler.armSwingAnimation(p);
                    }

                    e.setCancelled(true);
                    BlockHandler.placeCustomBlock(p, (CustomBlock) object.getObject(), clickedBlock, placeBl, item, hand);
                    return;
                } else {
                    final Block below = placeBl.getRelative(BlockFace.DOWN);
                    final CustomPlant cp = (CustomPlant) object.getObject();
                    final CustomPlantProperties cpp = cp.getPlantProperties();

                    Tag<Material> tag = Tag.DIRT;
                    if (!cpp.getTag().isEmpty()) {
                        tag = Bukkit.getTag("blocks", NamespacedKey.minecraft(cpp.getTag().toLowerCase()), Material.class);
                    }

                    if (tag == null) return;

                    if (!p.isSneaking() && !p.hasPermission("mineskycustom.bypass.light-requirement")) {
                        if (cpp.hasMinLight() || cpp.hasMaxLight()) {
                            final int lightLevel = getNMSLightLevel(placeBl);
                            if (cpp.hasMinLight() && cpp.getMinLight() < lightLevel) return;
                            if (cpp.hasMaxLight() && cpp.getMaxLight() > lightLevel) return;
                        }
                    }

                    if (cpp.canPlaceOnAnyBlock()) {
                        if (!below.getType().isSolid()) return;
                    } else if (!tag.isTagged(below.getType())) {
                        return;
                    }

                    e.setCancelled(true);
                    BlockHandler.placeCustomPlant(p, (CustomPlant) object.getObject(), clickedBlock, placeBl, item, hand);
                    return;
                }
            } else if (canBeCustom) {
                e.setCancelled(true);
            }
        }
    }

    public static boolean duplicateFixer(Player player) {
        boolean contains = duplicateFixer.contains(player.getUniqueId());

        if(!contains) {
            duplicateFixer.add(player.getUniqueId());
            player.getScheduler().runDelayed(MineSkyCustom.getInstance(), (task) -> {
                duplicateFixer.remove(player.getUniqueId());
            }, null, 1);
        }
        return contains;
    }

    public static int getNMSLightLevel(Block b) {
        ServerLevel w = ((CraftWorld) b.getWorld()).getHandle();
        BlockPos pos = new BlockPos(b.getX(), b.getY(), b.getZ());
        return w.getRawBrightness(pos, 0);
    }

    public static final SoundGroup WOOD = Material.OAK_LOG.createBlockData().getSoundGroup();

    // ProtocolLib
    public static void registerDigEvent() {
        MineSkyCustom.protocolManager.addPacketListener(
                new PacketAdapter(MineSkyCustom.getInstance(), ListenerPriority.NORMAL,
                        PacketType.Play.Client.BLOCK_DIG) {
                    @Override
                    public void onPacketReceiving(PacketEvent e) {
                        BlockPosition l = e.getPacket().getBlockPositionModifier().read(0);
                        final Player p = e.getPlayer();
                        final UUID uuid = p.getUniqueId();

                        final Location bdL = new Location(p.getWorld(), l.getX(), l.getY(), l.getZ());
                        Bukkit.getRegionScheduler().run(MineSkyCustom.getInstance(), bdL, (task) -> {
                            Block bd = bdL.getBlock();
                            if(bd.isEmpty() || !p.isOnline() || p.getGameMode() != GameMode.SURVIVAL)
                                return;
                            EnumWrappers.PlayerDigType type = e.getPacket().getPlayerDigTypes().read(0);
                            //BlockPos bp = new BlockPos(bd.getX(), bd.getY(), bd.getZ());

                            switch (type) {
                                case STOP_DESTROY_BLOCK:
                                case ABORT_DESTROY_BLOCK: {
                                    BlockHandler.breakingWood.remove(uuid);
                                    BlockHandler.cancelBreaking(p, bd);
                                    break;
                                }
                                case START_DESTROY_BLOCK: {
                                    // Bukkit.broadcastMessage("START DESTROY");
                                    if (bd.getType() != Material.NOTE_BLOCK) {
                                        p.getScheduler().run(MineSkyCustom.getInstance(), (playerTask) -> {
                                            //p.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                                            p.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(1);
                                        }, null);

                                        if(bd.getBlockSoundGroup().equals(WOOD)) {
                                            BlockHandler.playerBreakingWood(p, bd);
                                            return;
                                        }

                                        return;
                                    }

                                    NoteBlock noteBlock = (NoteBlock)bd.getBlockData();
                                    CustomBlock customBlock = BlockHandler.getCustomBlock(noteBlock);

                                    if(customBlock != null) {
                                        p.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0);
                                        BlockHandler.playerTryingToBreak(p, e.getPlayer().getInventory().getItemInMainHand(), bd, customBlock);
                                        return;
                                    }

                                    break;
                                }
                                default:
                                    break;
                            }
                        });
                    }
                });
    }

}
