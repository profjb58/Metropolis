package io.github.profjb58.metropolis.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class DirectionHelper {

    public static Direction getDirectionBetween(BlockPos currentPos, BlockPos prevPos){
        if(prevPos.getX() == currentPos.getX()){
            if(prevPos.getZ() - currentPos.getZ() > 0){
                return Direction.NORTH;
            } else {
                return Direction.SOUTH;
            }
        } else if(prevPos.getZ() == currentPos.getZ()) {
            if(prevPos.getX() - currentPos.getX() > 0){
                return Direction.WEST;
            } else {
                return Direction.EAST;
            }
        } else {
            return null;
        }
    }

    public static String convertToNBTString(Direction direction){
        if(direction == Direction.NORTH) {
            return "north";
        } else if(direction == Direction.SOUTH){
            return "south";
        } else if(direction == Direction.EAST){
            return "east";
        } else if(direction == Direction.WEST){
            return "west";
        } else {
            return null;
        }
    }

    public static Direction convertNBTStringToDirection(String directionString){
        switch (directionString) {
            case "north":
                return Direction.NORTH;
            case "south":
                return Direction.SOUTH;
            case "west":
                return Direction.WEST;
            case "east":
                return Direction.EAST;
        }
        return null;
    }
}
