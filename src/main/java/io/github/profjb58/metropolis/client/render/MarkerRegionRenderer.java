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
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;

import static io.github.profjb58.metropolis.common.tileentity.MarkerTE.getMarkerRadius;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MarkerRegionRenderer extends LineRenderer {
    private static HashMap<BlockPos, BlockPos> markerLines = new HashMap<>();

    private static BlockPos currentTailPos = null;
    private static Direction currentFacing = null;

    //  Rendering always a low priority. If it fails
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void render(RenderWorldLastEvent event){
        if(currentTailPos == null) return;

        ClientPlayerEntity player = Minecraft.getInstance().player;
        Item heldItem = player != null ? player.getHeldItemMainhand().getItem() : null;

        TileEntity currentMarker = player.world.getTileEntity(currentTailPos);

        if(currentMarker != null && currentMarker instanceof MarkerTE){
            Block currentMarkerBlock = currentMarker.getBlockState().getBlock();
            boolean withinSearchRadius = false;

            if(heldItem == Reference.QUARTZ_MARKER.asItem() || heldItem == Reference.PRISMARINE_MARKER.asItem()){
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
        }

        for (HashMap.Entry<BlockPos, BlockPos> entry : markerLines.entrySet()) {
            BlockPos startPos = entry.getKey();
            BlockPos endPos = entry.getValue();

            //TODO - Fixed rendering;
        }
    }

    /**
     *  Check line drawn to the player from tail marker isn't diagonal within the x/z axis and is within the expected reachable 'radius'
     *  for the specified marker block type.
     **/
    private static boolean checkWithinXZPlane(BlockPos playerPos, BlockPos tailMarkerPos, int radius){
        if(playerPos.getX() == tailMarkerPos.getX() || playerPos.getZ() == tailMarkerPos.getZ()){
            if(Math.abs(tailMarkerPos.getX() - playerPos.getX()) <= radius || Math.abs(tailMarkerPos.getZ() - playerPos.getZ()) <= radius) {
                if (Math.abs(tailMarkerPos.getY() - playerPos.getY()) <= radius) return true;
            }
        }
        return false;
    }

    public static void addLineToDraw(BlockPos startPos, @Nullable BlockPos endPos, Direction connectedFacing, boolean isTail){
        markerLines.put(startPos, endPos);

        if(isTail) {
            currentTailPos = endPos;
            currentFacing = connectedFacing;
        }
    }

    public static boolean deleteLine(BlockPos startPos, @Nullable BlockPos endPos, boolean isTail){
        if (markerLines.containsKey(startPos)){
            markerLines.remove(startPos);
            if(isTail) currentTailPos = startPos;
            return true;
        } else {
            return false;
        }
    }

    public static void setCurrentTailPos(BlockPos blockPos){
        currentTailPos = blockPos;
    }

    public static void resetCurrentFacing(){
        currentFacing = null;
    }
}
