package io.github.profjb58.metropolis.client.render;
import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.common.tileentity.MarkerTE;
import io.github.profjb58.metropolis.config.Config;
import io.github.profjb58.metropolis.util.DirectionHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.system.CallbackI;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import static io.github.profjb58.metropolis.common.tileentity.MarkerTE.getMarkerRadius;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MarkerRegionRenderer extends LineRenderer {

    // Key = Head Marker position. Values = list of all markers within that region.
    private static HashMap<BlockPos, LinkedList<BlockPos>> markerLines = new HashMap<>();
    private static BlockPos currentTailPos = null;
    private static Direction currentFacing = null;
    private static int checkRemovedCounter = 0;

    //  Rendering should always be a low priority.
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void render(RenderWorldLastEvent event){
        if(currentTailPos == null) return;

        ClientPlayerEntity player = Minecraft.getInstance().player;
        Item heldItem = player != null ? player.getHeldItemMainhand().getItem() : null;

        if(heldItem == Reference.PRISMARINE_MARKER.asItem() || heldItem == Reference.QUARTZ_MARKER.asItem()){
            TileEntity currentMarker = player.world.getTileEntity(currentTailPos);

            if(currentMarker != null && currentMarker instanceof MarkerTE){
                Block currentMarkerBlock = currentMarker.getBlockState().getBlock();
                if(checkWithinXZPlane(player.getPosition(), currentMarker.getPos(), MarkerTE.getMarkerRadius(currentMarkerBlock))){
                    if(currentFacing == null){ // Head Marker.
                        drawTileToPlayer(currentMarker, player, event.getMatrixStack());
                    } else {
                        //  Check when placing a marker we don't go back on ourselves.
                        Direction markerLinkDirection = DirectionHelper.getDirectionBetween(currentMarker.getPos(), player.getPosition());
                        if(currentFacing != markerLinkDirection){
                            drawTileToPlayer(currentMarker, player, event.getMatrixStack());
                        }
                    }
                }
            }

//            RenderPlayerEvent.
//
//            TileEntity prevMarker = null;
//            for (HashMap.Entry<BlockPos, LinkedList<BlockPos>> entry : markerLines.entrySet()) {
//                BlockPos headMarker = entry.getKey();
//                LinkedList<BlockPos> markers = entry.getValue();
//
//                prevMarker = null;
//                currentMarker = null;
//
//                if(markers.size() > 1){
//                    for(BlockPos marker: markers){
//                        prevMarker = player.world.getTileEntity(marker);
//
//                        if(currentMarker != null && prevMarker != null){
//                            drawTileToTile(prevMarker, currentMarker, player, event.getMatrixStack());
//                        }
//                        currentMarker = prevMarker;
//                    }
//                }
//            }
        }
    }

    public static void renderTest(TickEvent.RenderTickEvent event){

    }

    /**
     *  Check line drawn to the player from tail marker isn't diagonal within the x/z axis and is within the expected reachable 'radius'
     *  for the specified marker block type.
     **/
    private static boolean checkWithinXZPlane(BlockPos playerPos, BlockPos tailMarkerPos, int radius){
        if(playerPos.getX() == tailMarkerPos.getX()){
            if(Math.abs(tailMarkerPos.getZ() - playerPos.getZ()) <= radius){
                if(Math.abs(tailMarkerPos.getY() - playerPos.getY()) <= radius){
                    return true;
                }
            }
        }
        if(playerPos.getZ() == tailMarkerPos.getZ()){
            if(Math.abs(tailMarkerPos.getX() - playerPos.getX()) <= radius){
                if(Math.abs(tailMarkerPos.getY() - playerPos.getY()) <= radius){
                    return true;
                }
            }
        }
        return false;
    }

    public static void addMarkerToDraw(@Nullable BlockPos marker, BlockPos headMarker, Direction connectedFacing, boolean isTail){

        if(headMarker == null){
            LinkedList<BlockPos> markers = new LinkedList<>();
            markers.add(marker);
            markerLines.put(marker, markers);
        } else {
            if(markerLines.containsKey(headMarker)) {
                LinkedList<BlockPos> markers = markerLines.get(headMarker);

                if (!markers.contains(marker)) {
                    if (isTail) {
                        currentTailPos = marker;
                        currentFacing = currentFacing;
                    }
                    markers.add(marker);
                }

            }
        }
        printMarkerPath(headMarker);
    }

    // Only ever call when you know the block has definately been destroyed
    public static boolean removeMarker(@Nullable BlockPos marker){
        BlockPos headMarker = findHead(marker);
        if(headMarker != null){
            LinkedList<BlockPos> markers = markerLines.get(headMarker);
            if(markers != null){
                if(!markers.isEmpty()){
                    if(isTail(marker, headMarker)){
                        markers.removeLast();

                        if(!markers.isEmpty()){
                            currentTailPos = markers.getLast();
                            return true;
                        }
                        printMarkerPath(headMarker);
                    } else {
                        // Clear last few elements of the Marker Linked List.
                        markers.subList(markers.size() - markers.indexOf(marker), markers.size()).clear();
                        printMarkerPath(headMarker);
                    }

                    if(markers.isEmpty()){
                        markerLines.remove(headMarker);
                    }
                }
            }
        }
        return false;
    }

    private static BlockPos findHead(@Nullable BlockPos marker){
        for (HashMap.Entry<BlockPos, LinkedList<BlockPos>> entry : markerLines.entrySet()) {
            BlockPos headMarker = entry.getKey();
            LinkedList<BlockPos> markers = entry.getValue();

            if(markers.contains(marker)){
                return headMarker;
            }
        }
        return null;
    }

    private static boolean isTail(@Nullable BlockPos marker, BlockPos headMarker){
        LinkedList<BlockPos> markers = markerLines.get(headMarker);
        if(markers != null){
            BlockPos lastMarker = markers.getLast();
            if(lastMarker == marker){
                return true;
            }
        }
        return false;
    }

    public static void setCurrentTailPos(BlockPos blockPos){
        currentTailPos = blockPos;
    }

    public static void resetCurrentFacing(){
        currentFacing = null;
    }

    private static void printMarkerPath(BlockPos headMarker){
        if(headMarker != null){
            LinkedList<BlockPos> markers = markerLines.get(headMarker);

            Metropolis.LOGGER.debug("Marker Path for rendering starting at '" + headMarker.toString() + "'...");
            int index = 0;
            if(markers != null){
                for(BlockPos marker : markers){
                    Metropolis.LOGGER.debug(index + ") " + marker.toString());
                    index++;
                }
            }
        }
    }
}
