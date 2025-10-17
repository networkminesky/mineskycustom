package net.mineskycustom.events;

import net.mineskycustom.handler.BlockHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;

public class MMODCreativeTab implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String s, Player p, byte[] b) {

        String channel = s.toLowerCase().trim();

        if(!channel.equals("mineskymod:main"))
            return;

        String brand = new String(b, StandardCharsets.UTF_8).toLowerCase();

        if(brand.contains("open-creative-tab"))
            BlockHandler.loadBlocksToMineSkyMod(p);

        if(brand.contains("load-blocks"))
            BlockHandler.loadBlocksToMineSkyMod(p);

    }

}
