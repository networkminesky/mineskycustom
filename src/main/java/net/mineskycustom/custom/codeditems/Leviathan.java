package net.mineskycustom.custom.codeditems;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Leviathan extends ThrowableItem {

    private boolean inShockwave;
    private boolean isIcy;

    final BlockData data = Material.ICE.createBlockData();

    PotionEffect slowness;

    public static boolean isBiomeIcy(Biome b) {
        String name = b.getKey().getKey().toUpperCase();
        return name.contains("ICE") || name.contains("SNOW") || name.contains("COLD");
    }

    public Leviathan(Player thrower) {
        super(thrower, 1.5D, "Leviathan", 1.2f);
        isIcy = isBiomeIcy(thrower.getLocation().getBlock().getBiome());

        multiplier = isIcy ? 2 : 1;

        maxTicks = 40 * multiplier;
        maxBackTicks = 40 * multiplier;
        damage = 5 * multiplier;

        slowness = new PotionEffect(PotionEffectType.SLOWNESS, 30 * multiplier, multiplier, true, true, true);
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
        return 80;
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
        as.getWorld().playSound(as.getLocation(), "minesky.mobs.crab.hurt", 0.4f, 1);

        if(slowness != null)
            gotHit.addPotionEffect(slowness);

        gotHit.setFreezeTicks(65);

        // tacar as entidade pra longe
        Vector direction = as.getLocation().toVector().subtract(gotHit.getLocation().toVector()).normalize();
        gotHit.setVelocity(direction.multiply((-1.5 * multiplier)));
    }

    @Override
    public void tick(int tick, boolean comingBack) {
        ArmorStand armorStand = getArmorStand();

        armorStand.getWorld().playSound(armorStand.getLocation(), "minesky.mobs.dragon.flap", 1, 0.6f);

        armorStand.getWorld().spawnParticle(Particle.BLOCK, armorStand.getLocation().clone().add(0, 1, 0), 5, 0.3, 0.3, 0.3, data);

        if(comingBack)
            armorStand.setRightArmPose(armorStand.getRightArmPose().add(0.3, 0, -0.035));
        else
            armorStand.setRightArmPose(armorStand.getRightArmPose().add(0.3, 0, 0.04));

        if(isIcy)
            armorStand.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, armorStand.getLocation().clone().add(0, 1, 0), 4, 0.3, 0.3, 0.3);
    }

    @Override
    protected void onItemThrown() {
        getArmorStand().getWorld().playSound(getArmorStand().getLocation(), Sound.ENTITY_RAVAGER_STUNNED, 1, 0.8f);
    }

    @Override
    public void onItemBack(int tick) {
        ShockWave.entityFaceOtherEntity(getBase(), getThrower());

        getArmorStand().getWorld().playSound(getArmorStand().getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1, 1.2f);
    }

    @Override
    public void onGettingHitByAnotherItem(ThrowableItem item) {
        if(!item.getItemName().equalsIgnoreCase("stormlander"))
            return;

        item.stopItemTicking();
    }

    @Override
    public void onHitAnotherItem(ThrowableItem item) {

        /*if(!item.getItemName().equalsIgnoreCase("stormlander"))
            return;

        Bukkit.broadcastMessage("leviathan: acertou storm");

        item.stopItemTicking();
        this.stopItemTicking();*/

        // codigo pra quando bater no stormlander


    }

}

