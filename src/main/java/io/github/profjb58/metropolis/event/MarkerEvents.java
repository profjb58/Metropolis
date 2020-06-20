package io.github.profjb58.metropolis.event;

import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.common.block.Marker;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MarkerEvents {

    public static final String UUID_NBT_TAG = "player_placed";

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockEvent.BreakEvent event){
        PlayerEntity player = event.getPlayer();
        BlockState blockState = event.getState();
        BlockPos blockPos = event.getPos();
        World world = event.getPlayer().getEntityWorld();

        if(blockState.getBlock() instanceof Marker){

            TileEntity markerTE = world.getTileEntity(blockPos);
            if(markerTE != null && markerTE.getTileData().contains(UUID_NBT_TAG)){
                if(player.getUniqueID() != markerTE.getTileData().getUniqueId(UUID_NBT_TAG)){
                    if(event.isCancelable()) event.setCanceled(true);
                }
            }
        }
    }
}
