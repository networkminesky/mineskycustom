package net.mineskycustom.handler;

import org.bukkit.Instrument;

public class InstrumentConverter {

    public static String fromSpigot(Instrument ins) {
        return switch(ins) {
            case PIANO -> "HARP";
            case BASS_DRUM -> "BASEDRUM";
            default -> ins.name();
        };
    }

    public static Instrument fromMinecraft(String ins) {
        return switch (ins.toLowerCase()) {
            case "harp" -> Instrument.PIANO;
            case "basedrum" -> Instrument.BASS_DRUM;
            default -> Instrument.valueOf(ins.toUpperCase());
        };
    }
}
