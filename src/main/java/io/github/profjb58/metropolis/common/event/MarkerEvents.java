package io.github.profjb58.metropolis.common.event;

import com.mojang.realmsclient.dto.Ops;
import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.common.block.Marker;
import io.github.profjb58.metropolis.common.tileentity.MarkerTE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.OpList;
import net.minecraft.server.management.PlayerList;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.loading.FMLCommonLaunchHandler;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MarkerEvents {
    public static final String UUID_NBT_TAG = "player_placed";

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockBreak(BlockEvent.BreakEvent event){
        PlayerEntity player = event.getPlayer();
        World world = event.getPlayer().getEntityWorld();

        BlockPos breakPos = event.getPos();
        BlockState blockState = event.getState();
        BlockState blockStateAbove = world.getBlockState(breakPos.add(0,1,0));

        TileEntity tile;
        if(blockState.getBlock() instanceof Marker){
            tile = world.getTileEntity(breakPos);
        } else if(blockStateAbove.getBlock() instanceof Marker){
            //TODO - Need to stop markers breaking on a FallingBlock falling.
            // Falling block inherits 'Block'. Hence can check for an instanceof.

            tile = world.getTileEntity(breakPos.add(0,1,0));
        } else {
            return;
        }

        if(tile instanceof MarkerTE){
            MarkerTE markerTE = (MarkerTE) tile;
            if(markerTE.getPlayerPlaced() != null){
                boolean isOp = player.hasPermissionLevel(4) || player.hasPermissionLevel(3) ? true : false;
                if(!isOp && !player.getUniqueID().equals(markerTE.getPlayerPlaced())){
                    if(event.isCancelable()) event.setCanceled(true);
                } else {
                    markerTE.removeMarker();
                }
            }
        }
    }
}
