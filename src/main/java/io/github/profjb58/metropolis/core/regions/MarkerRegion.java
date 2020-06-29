package io.github.profjb58.metropolis.core.regions;
import io.github.profjb58.metropolis.common.tileentity.MarkerTE;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;

public class MarkerRegion {

    // TODO - Proper generation code.
//    public static HashMap<BlockPos, BlockPos> generate(World world, @NotNull MarkerTE headMarker){
//        HashMap<BlockPos, BlockPos> regionLine = new HashMap<>();
//        MarkerTE currentMarker = headMarker;
//
//        while(currentMarker.getNextMarkerPos() != null){
//            MarkerTE nextMarker = getMarkerFromPos(currentMarker.getNextMarkerPos(), world);
//            if(nextMarker.isHead() || nextMarker == null || headMarker.getPlayerPlaced() != nextMarker.getPlayerPlaced()) break;
//
//            regionLine.put(currentMarker.getPos(), nextMarker.getPos());
//            currentMarker = nextMarker;
//        }
//        return regionLine;
//    }

    public static MarkerTE getMarkerFromPos(BlockPos pos, World world){
        if(pos != null){
            TileEntity te = world.getTileEntity(pos);
            if(te != null && te instanceof MarkerTE) return (MarkerTE) te;
        }
        return null;
    }
}
