package net.mineskycustom.handler;

import org.bukkit.Instrument;

public class InstrumentConverter {

    public static String fromSpigot(Instrument ins) {
        switch(ins) {
            case PIANO: {
                return "HARP";
            }
            case BASS_DRUM: {
                return "BASEDRUM";
            }
            default:{ return ins+""; }
        }
    }

    public static Instrument fromMinecraft(String ins) {
        switch(ins) {
            case "HARP": {
                return Instrument.PIANO;
            }
            case "BASEDRUM": {
                return Instrument.BASS_DRUM;
            }
            default:{ return Instrument.valueOf(ins.toUpperCase()); }
        }
    }
}
