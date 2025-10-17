package net.mineskycustom.custom.codeditems;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;

public abstract class ThrowableItem {

    private final Player thrower;
    private final double yoffset;
    private final float speed;
    private final String itemName;
    private final ArmorStand armorStand;
    private final Sheep base;
    private List<Entity> alreadyHitEntities;

    private boolean itemTickingStopped = false;

    private final ItemStack itemStack;

    public ThrowableItem(Player thrower, double yoffset, String itemName, float speed) {
        this.thrower = thrower;
        this.yoffset = yoffset;
        this.itemName = itemName;

        itemStack = thrower.getInventory().getItemInMainHand();

        ArmorStand stand = thrower.getWorld().spawn(thrower.getLocation(), ArmorStand.class, as -> {
            as.setVisible(false);
            //as.setArms(true);
            as.setInvulnerable(true);
            as.getEquipment().setItemInMainHand(itemStack);
            as.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            as.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            as.setRightArmPose(new EulerAngle(-15, 0, 0));
        });
        Sheep sheep = thrower.getWorld().spawn(thrower.getLocation(), Sheep.class, a -> {
            a.setInvulnerable(true);
            a.setSilent(true);
            a.setBaby();
            a.setPersistent(true);
            a.setInvisible(true);
            a.setGravity(false);
            a.addPassenger(stand);
        });

        this.speed = speed;
        this.base = sheep;
        this.alreadyHitEntities = new ArrayList<>();
        this.armorStand = stand;
    }

    public float getItemSpeed() {
        return speed;
    }

    public void registerHitEntity(Entity en) {
        getAlreadyHitEntities().add(en);
    }
    public void clearHitEntities() {
        alreadyHitEntities.clear();
    }

    public List<Entity> getAlreadyHitEntities() {
        return alreadyHitEntities;
    }

    public double getYOffset() {
        return yoffset;
    }

    public Player getThrower() {
        return thrower;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public Sheep getBase() {
        return base;
    }

    public String getItemName() {
        return itemName;
    }

    public void stopItemTicking() {
        itemTickingStopped = true;
    }
    public void resumeItemTicking() {
        itemTickingStopped = false;
    }

    public boolean isTicking() {
        return !itemTickingStopped;
    }

    public abstract int ticksBeforeComingBack();
    public abstract int ticksToTimeOutAfterComingBack();

    public abstract int getItemModelData();

    public abstract Material getMaterial();

    public abstract int getDamage();

    public abstract void hitEntity(LivingEntity whoGotHit);

    public abstract void tick(int tick, boolean comingBack);

    // pode ser anulado
    protected abstract void onItemThrown();

    public abstract void onItemBack(int tick);

    public abstract void onHitAnotherItem(ThrowableItem theOtherItem);

    public abstract void onGettingHitByAnotherItem(ThrowableItem theOtherItem);
}

