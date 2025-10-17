package net.mineskycustom.custom.codeditems;

import net.mineskycustom.MineSkyCustom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ThrowableItemHandler {

    public static ArrayList<ThrowableItem> requestForRemoval = new ArrayList<>();

    public static HashMap<UUID, ThrowableItem> bases = new HashMap<>();
    //                    ARMO| BASE
    public static HashMap<UUID, UUID> armorstandBases = new HashMap<>();

    public static void runTestsInteraction(Player thrower, final ItemStack itemStack, int modelData) {
        for (Map.Entry<Integer, Class<? extends ThrowableItem>> entry : ThrowableItemRegistry.registeredItems.entrySet()) {
            int id = entry.getKey();
            Class<? extends ThrowableItem> itemClass = entry.getValue();

            if (id == modelData) {
                ThrowableItem newItem = createItemInstance(itemClass, thrower);

                executeTheItemCode(itemStack, newItem);

                return; // ou continue, dependendo do que você precisa fazer
            }
        }
    }

    public static void stopAndSafelyRemoveAnItem(ThrowableItem item) {
        requestForRemoval.add(item);
    }

    private static void itemDestroy(ThrowableItem it) {
        bases.remove(it.getBase().getUniqueId());
        armorstandBases.remove(it.getArmorStand().getUniqueId());
    }

    public static void executeTheItemCode(final ItemStack it, ThrowableItem item) {
        bases.put(item.getBase().getUniqueId(), item);
        armorstandBases.put(item.getArmorStand().getUniqueId(), item.getBase().getUniqueId());

        final int tickBeforeComingBack = item.ticksBeforeComingBack();

        item.onItemThrown();

        Player p = item.getThrower();

        p.getInventory().setItemInMainHand(null);

        PotionEffect resistance = new PotionEffect(PotionEffectType.RESISTANCE, 99999, 200, false, false, false);
        item.getBase().addPotionEffect(resistance);

        final float speed = item.getItemSpeed();

        new BukkitRunnable() {
            int tick = 0;
            int tickBacking = 0;
            @Override
            public void run() {
                if(p.isDead() || !p.isOnline() || tickBacking >= item.ticksToTimeOutAfterComingBack() || requestForRemoval.contains(item)) {
                    if(p.isOnline())
                        p.getInventory().addItem(it);

                    this.cancel();
                    itemDestroy(item);

                    item.getArmorStand().remove();
                    item.getBase().remove();

                    return;
                }

                ArmorStand as = item.getArmorStand();
                Sheep base = item.getBase();

                if(!item.isTicking()) {
                    return;
                }

                if(tick == tickBeforeComingBack) {
                    item.onItemBack(tick);
                    item.clearHitEntities();
                }

                if(tick >= item.ticksBeforeComingBack()) {
                    Location playerLoc = p.getLocation();
                    Location armorLoc = base.getLocation();

                    base.setVelocity(playerLoc.subtract(armorLoc).toVector().multiply(0.25));

                    tickBacking++;
                } else {
                    Location playerLoc = p.getLocation().clone();
                    playerLoc.setPitch((playerLoc.getPitch() > 0 ? playerLoc.getPitch()*0.8f : playerLoc.getPitch()*1.1f));

                    base.setVelocity(playerLoc.getDirection().multiply(speed));
                }

                item.tick(tick, tick >= tickBeforeComingBack);

                Location l = item.getArmorStand().getLocation().clone().add(0, item.getYOffset(), 0);

                for(Entity en : item.getBase().getWorld().getNearbyEntities(l, 0.7, 0.7, 0.7)) {
                    if (en instanceof LivingEntity living) {
                        if (en.equals(item.getBase()) || en.equals(item.getArmorStand()))
                            continue;
                        if(item.getAlreadyHitEntities().contains(en))
                            continue;

                        // sistema de item com item
                        if (bases.containsKey(en.getUniqueId())) {
                            item.onHitAnotherItem(bases.get(en.getUniqueId()));
                        } else if(armorstandBases.containsKey(en.getUniqueId())) {
                            UUID u = armorstandBases.get(en.getUniqueId());
                            if(bases.containsKey(u))
                                item.onHitAnotherItem(bases.get(u));
                        }

                        if (en.equals(item.getThrower())) {
                            // coming back?
                            if (tick >= item.ticksBeforeComingBack()) {
                                if (p.isOnline())
                                    p.getInventory().addItem(it);

                                this.cancel();
                                itemDestroy(item);

                                item.getArmorStand().remove();
                                item.getBase().remove();

                                return;
                            } else continue;
                        }

                        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(item.getThrower(), en, EntityDamageEvent.DamageCause.ENTITY_ATTACK, item.getDamage());
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled())
                            continue;

                        item.hitEntity(living);
                        item.registerHitEntity(en);
                    }
                }

                tick++;
            }
        }.runTaskTimer(MineSkyCustom.getInstance(), 0, 0);
    }

    private static ThrowableItem createItemInstance(Class<? extends ThrowableItem> itemClass, Player thrower) {
        try {
            return itemClass.getDeclaredConstructor(Player.class).newInstance(thrower);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
