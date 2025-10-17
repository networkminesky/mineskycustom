package net.mineskycustom.hooks;

import com.sk89q.worldguard.protection.flags.StringFlag;
import net.mineskycustom.MineSkyCustom;
import net.mineskycustom.custom.blocks.CustomBlock;
import org.bukkit.Instrument;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.block.custom.CustomBlockData;
import org.geysermc.geyser.api.block.custom.component.*;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent;
import org.geysermc.geyser.api.util.CreativeCategory;

import java.util.Arrays;
import java.util.stream.IntStream;

public class GeyserHook {

    public static void registerBlocks() {
        MineSkyCustom.l.info("[GEYSER] Registering "+MineSkyCustom.REGISTERED_BLOCKS.size()+" blocks!");
        GeyserApi.api().eventBus().subscribe(MineSkyCustom.getInstance(), GeyserDefineCustomBlocksEvent.class, GeyserHook::onDefineCustomBlocks);
    }

    @Subscribe
    public static void onDefineCustomBlocks(GeyserDefineCustomBlocksEvent event) {
        MineSkyCustom.l.info("[GEYSER] Geyser Define Custom Blocks Event has been fired!");

        int n = 0;
        for (CustomBlock customBlock : MineSkyCustom.REGISTERED_BLOCKS) {
            CustomBlockComponents components = CustomBlockComponents.builder()
                    .collisionBox(BoxComponent.fullBox())
                    .selectionBox(BoxComponent.fullBox())
                    .friction(1F)
                    .geometry(GeometryComponent.builder()
                            .identifier("geometry.mineskycustom."+customBlock.getId())
                            .build())
                    .lightEmission(0)
                    .placeAir(true)
                    .lightDampening(0)
                    .materialInstance("*", MaterialInstance.builder()
                            .texture(String.valueOf(530+n))
                            .renderMethod("alpha_test")
                            .faceDimming(false)
                            .ambientOcclusion(true)
                            .build())
                    .build();

            CustomBlockData blockData = CustomBlockData.builder()
                    .name(customBlock.getId())

                    //.stringProperty("instrument", Arrays.stream(Instrument.values()).map(Enum::toString).map(String::toLowerCase).toList())
                    //.intProperty("note", IntStream.range(0, 25).boxed().toList())
                    .creativeCategory(CreativeCategory.CONSTRUCTION)
                    .components(components)
                    //.permutations(createPermutations())
                    .build();

            event.register(blockData);
            //event.registerItemOverride("minecraft:note_block", blockData);

            try {
                String javaIdentifier = "minecraft:note_block[instrument=" + customBlock.getInstrument() + ",note=" + customBlock.getNote() + ",powered=false]";
                MineSkyCustom.l.info("[GEYSER] Tentando registrar: "+javaIdentifier);
                event.registerOverride(javaIdentifier, blockData.blockStateBuilder()
                        //.stringProperty("instrument", customBlock.getInstrument())
                        //.intProperty("note", customBlock.getNote())
                        .build());
            } catch (Exception ex) {
                MineSkyCustom.l.info("[GEYSER] Erro: "+ex.getMessage());
                ex.fillInStackTrace();
            }

            MineSkyCustom.l.info("[GEYSER] Registered block "+customBlock.getId()+"! Texture: "+(530+n));
            n++;
        }
    }

}
