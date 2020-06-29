package io.github.profjb58.metropolis.client.render;
import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MarkerRegionRenderer extends LineRenderer {

    private static HashMap<BlockPos, BlockPos> markerLines = new HashMap<>();
    // TODO - Add interpretable version. IE one without a blockPos and instead x & z coords.
    private static BlockPos currentTailPos;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void render(RenderWorldLastEvent event){
        ClientPlayerEntity player = Minecraft.getInstance().player;
        Item heldItem = player != null ? player.getHeldItemMainhand().getItem() : null;

        if (heldItem == Reference.PRISMARINE_MARKER.asItem()) {
            locateTiles(Reference.PRISMARINE_MARKER, player, event.getMatrixStack(), 32);
        } else if (heldItem == Reference.QUARTZ_MARKER.asItem()) {
            locateTiles(Reference.QUARTZ_MARKER, player, event.getMatrixStack(), 16);
        }
    }

    public static void addLineToDraw(BlockPos startPos, @Nullable BlockPos endPos){
        markerLines.put(startPos, endPos);
    }

    public static boolean deleteLine(@Nonnull BlockPos startPos){
        if (markerLines.containsKey(startPos)){
            markerLines.remove(startPos);
            return true;
        } else {
            return false;
        }
    }

}
