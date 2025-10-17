package net.mineskycustom.custom.codeditems;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.UUID;

public class ShockWave {

    public static void entityFaceOtherEntity(Entity facer, Entity faced) {

        try {
            Vector v = faced.getLocation().subtract(facer.getLocation()).toVector().normalize();
            Location l = facer.getLocation().setDirection(v.multiply(1));
            facer.teleport(l);
        } catch(IllegalArgumentException ignored) {}
    }

    public static ArrayList<Entity> comeBack = new ArrayList<>();

    public static void startShockwave() {



    }

}
