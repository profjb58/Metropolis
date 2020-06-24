package io.github.profjb58.metropolis.common.event;

import com.mojang.realmsclient.dto.Ops;
import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.common.block.Marker;
import io.github.profjb58.metropolis.common.tileentity.MarkerTE;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.OpList;
import net.minecraft.server.management.PlayerList;
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
        BlockState blockState = event.getState();
        BlockPos blockPos = event.getPos();
        World world = event.getPlayer().getEntityWorld();

        if(blockState.getBlock() instanceof Marker){
            TileEntity tile = world.getTileEntity(blockPos);
            if(tile instanceof MarkerTE){
                MarkerTE markerTE = (MarkerTE) tile;
                if(markerTE.playerPlaced != null){
                    boolean isOp = player.hasPermissionLevel(4) || player.hasPermissionLevel(3) ? true : false;
                    if(!isOp && !player.getUniqueID().toString().equals(markerTE.playerPlaced.toString())){
                        if(event.isCancelable()) event.setCanceled(true);
                    }
                }
            }
        }
    }
}
