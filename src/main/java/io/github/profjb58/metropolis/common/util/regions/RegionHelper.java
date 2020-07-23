package io.github.profjb58.metropolis.common.util.regions;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class RegionHelper {

    // Debug method to print the marker region path.
    public static void printPathForPlayer(PlayerEntity player, ArrayList<BlockPos> path){
        StringTextComponent headMessage = new StringTextComponent("Head:" + " x:" + path.get(0).getX() + " y:" + path.get(0).getY() + " z:" + path.get(0).getZ());
        headMessage.getStyle().setColor(TextFormatting.GREEN);

        StringTextComponent tailMessage = new StringTextComponent("Tail:" + " x:" + path.get(path.size() -1 ).getX() + " y:" + path.get(path.size() -1).getY() + " z:" + path.get(path.size() -1).getZ());
        tailMessage.getStyle().setColor(TextFormatting.RED);

        player.sendMessage(headMessage);
        player.sendMessage(tailMessage);
        player.sendMessage(new StringTextComponent(""));

        player.sendMessage(new StringTextComponent("Region path: "));
        int posCounter = 1;
        for(BlockPos blockPos : path){
            String message = posCounter + ") " + " x:" + blockPos.getX() + " y:" + blockPos.getY() + " z:" + blockPos.getZ();
            player.sendMessage(new StringTextComponent(message));
            posCounter++;
        }
    }

    public static void printPolygonsForPlayer(PlayerEntity player, LinkedList<int[]> rectPolygons){
        player.sendMessage(new StringTextComponent("Rectilinear Polygons contained within the region: "));
        player.sendMessage(new StringTextComponent("  Format = [x1, x2, z1, z2]"));

        int posCounter = 1;
        for(int[] rect : rectPolygons){
            player.sendMessage(new StringTextComponent(posCounter + ") " + Arrays.toString(rect)));
            posCounter++;
        }
    }
}
