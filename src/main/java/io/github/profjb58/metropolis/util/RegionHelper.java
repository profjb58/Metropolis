package io.github.profjb58.metropolis.util;

import net.minecraft.util.math.BlockPos;

import java.util.Random;
import java.util.UUID;

public class RegionHelper {

    public static String generateRegionIdentifier(UUID uuid, BlockPos markerHeadPos){
        String uuidString = uuid.toString();
        String headPosString = markerHeadPos.toString();

        return uuidString + "_" + headPosString;
    }
}
