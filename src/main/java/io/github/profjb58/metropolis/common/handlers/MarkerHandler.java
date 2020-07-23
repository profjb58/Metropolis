package io.github.profjb58.metropolis.common.handlers;

import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.common.blocks.MarkerBlock;
import io.github.profjb58.metropolis.common.tileentity.Marker;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MarkerHandler {
    public static final String UUID_NBT_TAG = "player_placed";

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockBreak(BlockEvent.BreakEvent event){
        PlayerEntity player = event.getPlayer();
        World world = event.getPlayer().getEntityWorld();

        BlockPos breakPos = event.getPos();
        BlockState blockState = event.getState();
        BlockState blockStateAbove = world.getBlockState(breakPos.add(0,1,0));

        TileEntity tile;
        if(blockState.getBlock() instanceof MarkerBlock){
            tile = world.getTileEntity(breakPos);
        } else if(blockStateAbove.getBlock() instanceof MarkerBlock){
            //TODO - Need to stop markers breaking on a FallingBlock falling.
            // Falling block inherits 'Block'. Hence can check for an instanceof.

            tile = world.getTileEntity(breakPos.add(0,1,0));
        } else {
            return;
        }

        if(tile instanceof Marker){
            Marker marker = (Marker) tile;
            if(marker.getPlayerPlaced() != null){
                boolean isOp = player.hasPermissionLevel(4) || player.hasPermissionLevel(3) ? true : false;
                if(!isOp && !player.getUniqueID().equals(marker.getPlayerPlaced())){
                    if(event.isCancelable()) event.setCanceled(true);
                } else {
                    marker.removeMarker();
                }
            }
        }
    }

    static boolean PLACE_WARNING_SHOWN = false;
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlaceEvent(BlockEvent.EntityPlaceEvent event){

        if(event.getEntity() != null && event.getEntity() instanceof PlayerEntity){
            PlayerEntity player = (PlayerEntity) event.getEntity();
            UUID uuid = player.getUniqueID();

            TileEntity tile = player.getEntityWorld().getTileEntity(event.getPos());
            if(tile != null && tile instanceof Marker){
                Marker mte = (Marker) tile;
                BlockPos mtePos = event.getPos();

                BlockPos blockBelowPos = new BlockPos(mtePos.getX(), mtePos.getY() - 1, mtePos.getZ());
                BlockState blockBelowState = player.getEntityWorld().getBlockState(blockBelowPos);
                Block blockBelow = blockBelowState.getBlock();

                if(blockBelow instanceof SlimeBlock || blockBelow instanceof FallingBlock){
                    ItemStack markerItemStack = new ItemStack(event.getPlacedBlock().getBlock().asItem(), 1);
                    if(event.isCancelable()){
                        event.setCanceled(true);
                        player.inventory.addItemStackToInventory(markerItemStack);
                        if(!PLACE_WARNING_SHOWN){
                            player.sendMessage(Metropolis.metropolisTextHeader);
                            player.sendMessage(new StringTextComponent("*poof* Looks like your marker needs a solid foundation"));
                            PLACE_WARNING_SHOWN = true;
                        }
                    }
                } else {
                    mte.init(player);
                }
            }
        } else {
            if(event.isCancelable()) event.setCanceled(true);
        }
    }
}
