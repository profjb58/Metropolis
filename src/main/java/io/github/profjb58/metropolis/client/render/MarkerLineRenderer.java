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
public class MarkerLineRenderer extends LineRenderer {

    private static Item[] markerHeldItems;
    private static BlockPos currentTailPos = null;
    private static Block currentTailBlock = null;
    private static Direction currentFacing = null;

    public static void initHeldItemsList(){
        markerHeldItems = new Item[]{
                           Reference.PRISMARINE_MARKER.asItem(),
                           Reference.QUARTZ_MARKER.asItem()
        };
    }

    //  Rendering should always be a low priority.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void render(RenderWorldLastEvent event){
        if(currentTailPos == null) return;

        ClientPlayerEntity player = Minecraft.getInstance().player;
        Item heldItem = player != null ? player.getHeldItemMainhand().getItem() : null;

        boolean holdingMarker = false;
        for (Item markerItem : markerHeldItems) {
            if (heldItem == markerItem) {
                holdingMarker = true;
            }
        }

        if (holdingMarker) {
            TileEntity currentMarker = player.world.getTileEntity(currentTailPos);
            if (currentMarker != null && currentMarker instanceof MarkerTE) {
                Block currentMarkerBlock = currentMarker.getBlockState().getBlock();
                if (checkWithinXZPlane(player.getPosition(), currentMarker.getPos(), MarkerTE.getMarkerRadius(currentMarkerBlock))) {
                    if (currentFacing == null) { // Head Marker.
                        drawTileToPlayer(currentMarker, player, event.getMatrixStack(), 0.7f);
                    } else {
                        //  Check when placing a marker we don't go back on ourselves.
                        Direction markerLinkDirection = DirectionHelper.getDirectionBetween(currentMarker.getPos(), player.getPosition());
                        if (currentFacing != markerLinkDirection) {
                            drawTileToPlayer(currentMarker, player, event.getMatrixStack(), 0.7f);
                        }
                    }
                }
            }
        }
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

    public static void setCurrentTailPos(BlockPos blockPos){
        currentTailPos = blockPos;
    }

    public static void resetCurrentFacing(){
        currentFacing = null;
    }

}
