package io.github.profjb58.metropolis.client.render;
import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.common.tileentity.MarkerTE;
import io.github.profjb58.metropolis.config.Config;
import io.github.profjb58.metropolis.util.PositionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MarkerRegionRenderer extends LineRenderer {
    private static HashMap<BlockPos, BlockPos> markerLines = new HashMap<>();

    private static BlockPos currentTailPos = null;
    private static String currentFacing = null;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void render(RenderWorldLastEvent event){
        if(currentTailPos == null || currentFacing == null) return;

        ClientPlayerEntity player = Minecraft.getInstance().player;
        Item heldItem = player != null ? player.getHeldItemMainhand().getItem() : null;

        TileEntity currentMarker = player.world.getTileEntity(currentTailPos);

        if(currentMarker != null && currentMarker instanceof MarkerTE){
            boolean withinSearchRadius = false;
            boolean renderItemHeld = false;

            if(heldItem == Reference.PRISMARINE_MARKER.asItem()){
                if(checkRadius(currentMarker.getPos(), player.getPosition(), Config.COMMON.prismarine_marker_radius.get())) withinSearchRadius = true;
            } else if (heldItem == Reference.QUARTZ_MARKER.asItem()){
                if(checkRadius(currentMarker.getPos(), player.getPosition(), Config.COMMON.quartz_marker_radius.get())) withinSearchRadius = true;
            }

            if(withinSearchRadius){
                if(PositionHelper.inForwardPlane(currentMarker.getPos(), player.getPosition(), currentFacing)){
                    drawTileToPlayer(currentMarker, player, event.getMatrixStack());
                }
            }
        }

        for (HashMap.Entry<BlockPos, BlockPos> entry : markerLines.entrySet()) {
            BlockPos startPos = entry.getKey();
            BlockPos endPos = entry.getValue();

            //TODO - Fixed rendering;
        }
    }

    private static boolean checkRadius(BlockPos currentPos, BlockPos prevPos, int radius){
        if(Math.abs(prevPos.getX() - currentPos.getX()) <= radius) return true;
        if(Math.abs(prevPos.getY() - currentPos.getY()) <= radius) return true;
        if(Math.abs(prevPos.getZ() - currentPos.getZ()) <= radius) return true;
        return false;
    }

    public static void addLineToDraw(BlockPos startPos, @Nullable BlockPos endPos, String connectedFacing, boolean isTail){
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

}
