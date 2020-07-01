package io.github.profjb58.metropolis.util;

import net.minecraft.util.math.BlockPos;

public class PositionHelper {

    public static boolean inForwardPlane(BlockPos pos, BlockPos posComparable, String facing, boolean checkX, boolean checkZ){
        if(checkZ){
            if(pos.getX() == posComparable.getX()){
                if(posComparable.getZ() - pos.getZ() > 0){
                    if(facing.equals("south")) return false;
                } else {
                    if(facing.equals("north")) return false;
                }
                return true;
            }
        }
        if(checkX){
            if(pos.getZ() == posComparable.getZ()){
                if(posComparable.getX() - pos.getX() > 0){
                    if(facing.equals("east")) return false;
                } else {
                    if(facing.equals("west")) return false;
                }
                return true;
            }
        }
        return false;
    }

    public static boolean inForwardPlane(BlockPos pos, BlockPos posComparable, String facing){
        return inForwardPlane(pos, posComparable, facing, true, true);
    }

    public static class StringDirection {

        public static String check(BlockPos currentPos, BlockPos prevPos){
            if(currentPos.getZ() == prevPos.getZ()){
                if(prevPos.getX() - currentPos.getX() > 0){
                    return "west";
                } else {
                    return "east";
                }
            } else {
                if(prevPos.getZ() - currentPos.getZ() > 0){
                    return "north";
                } else {
                    return "south";
                }
            }
        }

        public static boolean isOpposite(String direction, String directionComparable){
            switch(direction){
                case "south":
                    if(directionComparable.equals("north")) return true;
                case "north":
                    if(directionComparable.equals("south")) return true;
                case "west":
                    if(directionComparable.equals("east")) return true;
                case "east":
                    if(directionComparable.equals("west")) return true;
            }
            return false;
        }
    }
}
