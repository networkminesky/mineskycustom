package net.mineskycustom.custom.codeditems;

import net.mineskycustom.MineSkyCustom;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class Stormlander extends ThrowableItem {

    private boolean inShockwave;
    private boolean isThundering;

    public Stormlander(Player thrower) {
        super(thrower, 1.5D, "Stormlander", 1);
        isThundering = thrower.getWorld().isThundering();

        multiplier = isThundering ? 2 : 1;

        maxTicks = 40 * multiplier;
        maxBackTicks = 40 * multiplier;
        damage = 5 * multiplier;
    }
    final int multiplier;
    final int maxTicks;
    final int maxBackTicks;
    final int damage;

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public int getItemModelData() {
        return 84;
    }

    @Override
    public int ticksBeforeComingBack() {
        return maxTicks;
    }
    @Override
    public int ticksToTimeOutAfterComingBack() {
        return maxBackTicks;
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_AXE;
    }

    @Override
    public void hitEntity(LivingEntity gotHit) {
        gotHit.damage(damage);

        ArmorStand as = getArmorStand();

        // dano e raio
        as.getWorld().playSound(as.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 0.4f, 1);
        as.getWorld().strikeLightningEffect(as.getLocation());

        // tacar as entidade pra longe
        Vector direction = as.getLocation().toVector().subtract(gotHit.getLocation().toVector()).normalize();
        gotHit.setVelocity(direction.multiply((-1 * multiplier)));
    }

    @Override
    public void tick(int tick, boolean comingBack) {
        ArmorStand armorStand = getArmorStand();

        armorStand.getWorld().playSound(armorStand.getLocation(), "minesky.mobs.dragon.flap", 1, 0.2f);

        armorStand.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, armorStand.getLocation().clone().add(0, 1, 0), 5, 0.5, 0.5, 0.5);

        if(comingBack)
            armorStand.setRightArmPose(armorStand.getRightArmPose().add(0.05, 0, 0));
        else
            armorStand.setRightArmPose(armorStand.getRightArmPose().add(0.03, 0, 0.035));

        if(isThundering)
            armorStand.getWorld().spawnParticle(Particle.SONIC_BOOM, armorStand.getLocation().clone().add(0, 1, 0), 1, 0.1, 0.1, 0.1);

        if(tick == ((ticksBeforeComingBack()-20))) {
            new BukkitRunnable() {
                int n = 0;
                @Override
                public void run() {
                    if(n >= 20) {
                        getArmorStand().setRightArmPose(getArmorStand().getRightArmPose().add(0.04, 0, 0.01));
                    } else
                        getArmorStand().setRightArmPose(getArmorStand().getRightArmPose().add(0.09, 0, 0.01));

                    if(n >= 33 || getBase() == null || getBase().isDead()) {
                        this.cancel();
                        return;
                    }

                    n++;
                }
            }.runTaskTimer(MineSkyCustom.getInstance(),0, 0);
        }
    }

    @Override
    protected void onItemThrown() {
        getArmorStand().getWorld().playSound(getArmorStand().getLocation(), Sound.ENTITY_RAVAGER_STUNNED, 1, 0);

        getArmorStand().setRightArmPose(new EulerAngle(5, 0, 0));
    }

    @Override
    public void onItemBack(int tick) {
        ShockWave.entityFaceOtherEntity(getBase(), getThrower());

        getArmorStand().getWorld().playSound(getArmorStand().getLocation(), Sound.ITEM_TRIDENT_RETURN, 2, 1);
    }

    @Override
    public void onGettingHitByAnotherItem(ThrowableItem item) {

    }

    @Override
    public void onHitAnotherItem(final ThrowableItem item) {
        if(!item.getItemName().equalsIgnoreCase("leviathan"))
            return;

        Stormlander stormlander = this;
        Leviathan leviathan = (Leviathan)item;

        leviathan.onGettingHitByAnotherItem(this);

        leviathan.stopItemTicking();
        stormlander.stopItemTicking();

        leviathan.getBase().setAI(false);
        stormlander.getBase().setAI(false);

        stormlander.getBase().getWorld().playSound(leviathan.getBase().getLocation(), Sound.BLOCK_ANVIL_LAND, 3, 0);
        stormlander.getBase().getWorld().playSound(leviathan.getBase().getLocation(), Sound.BLOCK_ANVIL_LAND, 3, 0);
        stormlander.getBase().getWorld().playSound(leviathan.getBase().getLocation(), Sound.BLOCK_ANVIL_LAND, 3, 0);

        ShockWave.entityFaceOtherEntity(leviathan.getArmorStand(), stormlander.getArmorStand());
        ShockWave.entityFaceOtherEntity(stormlander.getArmorStand(), leviathan.getArmorStand());

        final EulerAngle angle = new EulerAngle(-40, -10, -5);
        stormlander.getArmorStand().setRightArmPose(angle);
        leviathan.getArmorStand().setRightArmPose(angle);

        new BukkitRunnable() {
            int n = 0;
            @Override
            public void run() {

                if (n >= 190) {
                    leviathan.onItemBack(0);
                    stormlander.onItemBack(0);

                    ThrowableItemHandler.stopAndSafelyRemoveAnItem(stormlander);
                    ThrowableItemHandler.stopAndSafelyRemoveAnItem(leviathan);

                    this.cancel();
                    return;
                } else if (n == 160) {

                    stormlander.getBase().setAI(true);
                    leviathan.getBase().setAI(true);

                    stormlander.getBase().getWorld().spawnParticle(Particle.CLOUD, stormlander.getBase().getLocation(), 120, 0.2, 0.2, 0.2, 2);

                    leviathan.getBase().getWorld().playSound(stormlander.getBase().getLocation(), "minesky.effects.alert", 1, 1.2f);

                    Vector direction1 = stormlander.getArmorStand().getLocation().toVector().subtract(leviathan.getArmorStand().getLocation().toVector()).normalize();
                    Vector direction2 = leviathan.getArmorStand().getLocation().toVector().subtract(stormlander.getArmorStand().getLocation().toVector()).normalize();

                    leviathan.getBase().setVelocity(direction1.multiply((-3)).setY(1));
                    stormlander.getBase().setVelocity(direction2.multiply((-3)).setY(1));

                    n++;

                    return;
                }

                stormlander.getBase().getWorld().spawnParticle(Particle.FLASH, stormlander.getArmorStand().getLocation(), 1, 0.2, 0.2, 0.2);
                leviathan.getBase().getWorld().spawnParticle(Particle.FLASH, leviathan.getArmorStand().getLocation(), 1, 0.2, 0.2, 0.2);

                // A cada 5 ticks:
                if ((n % 5) == 0) {
                    leviathan.getBase().getWorld().playSound(leviathan.getArmorStand().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 0.9f);

                    leviathan.getArmorStand().getWorld().spawnParticle(Particle.ENCHANT, leviathan.getArmorStand().getLocation().add(0, 1.5, 0), 150, 0, 0, 0, 7);

                    stormlander.getBase().getWorld().strikeLightningEffect(stormlander.getArmorStand().getLocation());

                    for (Entity en : item.getBase().getWorld().getNearbyEntities(leviathan.getArmorStand().getLocation(), 4, 4, 4)) {
                        if (!(en instanceof LivingEntity living))
                            continue;
                        if (en.equals(stormlander.getBase())
                                || en.equals(leviathan.getBase()))
                            continue;
                        if (en.equals(stormlander.getArmorStand())
                                || en.equals(leviathan.getArmorStand()))
                            continue;

                        living.damage((((double) stormlander.getDamage() / 2) + ((double) leviathan.getDamage() / 2)));

                        living.getWorld().strikeLightningEffect(living.getLocation());

                        living.setFreezeTicks(30);

                        Vector direction = stormlander.getArmorStand().getLocation().toVector().subtract(living.getLocation().toVector());
                        living.setVelocity(direction.setY(1).multiply((-3)));

                    }
                }

                n++;
            }
        }.runTaskTimer(MineSkyCustom.getInstance(), 0, 0);

        // codigo pra quando bater no leviathan


    }

}
