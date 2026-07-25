package net.mineskycustom.hooks;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.mineskycustom.MineSkyCustom;
import net.mineskycustom.custom.blocks.CustomBlock;
import net.mineskycustom.custom.plants.CustomPlant;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class WorldEditHook {

    public static void register() {
        WorldEdit.getInstance().getBlockFactory().register(new CustomBlockInputParser());
        MineSkyCustom.l.info("Registered custom block parser");
    }

    public static class CustomBlockInputParser extends InputParser<BaseBlock> {
        public CustomBlockInputParser() {
            super(WorldEdit.getInstance());
        }

        @Override
        public Stream<String> getSuggestions(String input, ParserContext context) {
            String lowerInput = input.toLowerCase();

            if (!"minesky:".startsWith(lowerInput) && !lowerInput.startsWith("minesky:")) {
                return Stream.empty();
            }

            Stream<String> plantsStream = MineSkyCustom.REGISTERED_PLANTS.stream().map(key -> "minesky:" + key.getId());
            Stream<String> blocksStream = MineSkyCustom.REGISTERED_BLOCKS.stream().map(key -> "minesky:" + key.getId());

            return Stream.concat(plantsStream, blocksStream)
                    .filter(id -> id.toLowerCase().startsWith(lowerInput));
        }

        @Override
        public BaseBlock parseFromInput(String input, ParserContext context) {
            String lowerInput = input.toLowerCase();

            if (!lowerInput.startsWith("minesky:")) {
                return null;
            }

            final String blockKey = lowerInput.substring("minesky:".length());

            for(CustomBlock block : MineSkyCustom.REGISTERED_BLOCKS) {
                if(!(block.getId().equalsIgnoreCase(blockKey)))
                    continue;

                BlockType noteBlockType = Objects.requireNonNull(BlockTypes.NOTE_BLOCK);
                BlockState noteBlockState = noteBlockType.getDefaultState();

                Property<String> instrumentProp = (Property<String>) noteBlockType.getPropertyMap().get("instrument");
                Property<Integer> noteProp = (Property<Integer>) noteBlockType.getPropertyMap().get("note");

                if (instrumentProp != null) {
                    noteBlockState = noteBlockState.with(instrumentProp, block.getInstrument());
                }
                if (noteProp != null) {
                    noteBlockState = noteBlockState.with(noteProp, block.getNote());
                }

                return noteBlockState.toBaseBlock();
            }

            for(CustomPlant plant : MineSkyCustom.REGISTERED_PLANTS) {
                if(!(plant.getId().equalsIgnoreCase(blockKey)))
                    continue;

                BlockType tripwireType = Objects.requireNonNull(BlockTypes.TRIPWIRE);
                BlockState tripwireState = tripwireType.getDefaultState();

                Property<Boolean> attachedProp = (Property<Boolean>) tripwireType.getPropertyMap().get("attached");
                Property<Boolean> disarmedProp = (Property<Boolean>) tripwireType.getPropertyMap().get("disarmed");

                Property<Boolean> east = (Property<Boolean>) tripwireType.getPropertyMap().get("east");
                Property<Boolean> north = (Property<Boolean>) tripwireType.getPropertyMap().get("north");
                Property<Boolean> powered = (Property<Boolean>) tripwireType.getPropertyMap().get("powered");
                Property<Boolean> south = (Property<Boolean>) tripwireType.getPropertyMap().get("south");
                Property<Boolean> west = (Property<Boolean>) tripwireType.getPropertyMap().get("west");

                if (attachedProp != null) {
                    tripwireState = tripwireState.with(attachedProp, plant.isAttached());
                }
                if (disarmedProp != null) {
                    tripwireState = tripwireState.with(disarmedProp, plant.isDisarmed());
                }

                if (east != null) {
                    tripwireState = tripwireState.with(east, plant.isEast());
                }
                if (north != null) {
                    tripwireState = tripwireState.with(north, plant.isNorth());
                }
                if (powered != null) {
                    tripwireState = tripwireState.with(powered, plant.isPowered());
                }
                if (south != null) {
                    tripwireState = tripwireState.with(south, plant.isSouth());
                }
                if (west != null) {
                    tripwireState = tripwireState.with(west, plant.isWest());
                }

                return tripwireState.toBaseBlock();
            }

            return null;
        }
    }
}
