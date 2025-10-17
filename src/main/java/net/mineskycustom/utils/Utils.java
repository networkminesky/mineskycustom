package net.mineskycustom.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<BlockFace> blockfaces = Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN);

    public static String energyBarFormatter(double i, double max) {

        float f = ((float)i / (float)max) * (float)1;
        int stage = (int) (f * 10.0f);

        if(stage == 0) {
            return "௵";
        }
        if(stage == 1) {
            return "༗";
        }
        if(stage == 2) {
            return "௷";
        }
        if(stage == 3) {
            return "௸";
        }
        if(stage == 4) {
            return "௺";
        }
        if(stage == 5) {
            return "౿";
        }
        if(stage == 6) {
            return "൏";
        }
        if(stage == 7) {
            return "൹";
        }
        if(stage == 8) {
            return "༁";
        }
        if(stage == 9) {
            return "༂";
        }
        if(stage == 10) {
            return "༃";
        }

        return "௵";

    }

    public static String itemStackToBase64(ItemStack itemStack) {
        try {
            final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            final BukkitObjectOutputStream objectOutputStream = new BukkitObjectOutputStream(arrayOutputStream);
            objectOutputStream.writeObject(itemStack);
            return Base64Coder.encodeLines(arrayOutputStream.toByteArray());
        } catch (final Exception exception) {
            throw new RuntimeException("Error turning ItemStack into base64", exception);
        }
    }

    public static String formatBase64(String s) {
        s = s.replace("\n", "").replace("\r", "");
        s = s.replaceAll("[^\\x00-\\x7F]", "");

        s = s.trim();
        s = s.strip();

        return s;
    }

    public static ItemStack itemStackFromBase64(String base64) {
        try {
            final ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            final BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(arrayInputStream);
            return (ItemStack) objectInputStream.readObject();
        } catch (final Exception exception) {
            throw new RuntimeException("Error turning base64 into ItemStack", exception);
        }
    }

    public static double getPercent(double i, double max) {
        float f = ((float)i / (float)max) * (float)1;
        int stage = (int) (f * 10.0f);

        return stage * 10;
    }

}
