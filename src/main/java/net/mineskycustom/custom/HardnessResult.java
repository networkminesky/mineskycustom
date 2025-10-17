package net.mineskycustom.custom;

import org.bukkit.Bukkit;

public class HardnessResult {

    private final int hardness;
    private final boolean usingRightTool;

    public HardnessResult(int HardResult, boolean RightTool) {
        if(HardResult == 0)
            HardResult = 4;

        if(HardResult <= -5)
            HardResult = 0;

        if(HardResult <= -3)
            HardResult = 1;

        if(HardResult <= -2)
            HardResult = 2;

        if(HardResult < 0)
            HardResult = 3;

        this.usingRightTool = RightTool;
        this.hardness = HardResult;
    }

    public int getHardness() {
        return this.hardness;
    }

    public boolean isUsingRightTool() {
        return this.usingRightTool;
    }

}
