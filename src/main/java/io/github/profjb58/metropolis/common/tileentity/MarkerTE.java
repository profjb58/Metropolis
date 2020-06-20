package io.github.profjb58.metropolis.common.tileentity;

import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.event.MarkerEvents;
import io.github.profjb58.metropolis.util.NBTHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class MarkerTE extends TileEntity {

    boolean initialized = false;
    boolean isHead, isTail, connected;
    UUID playerPlaced;
    String connectedFacing = "none"; // Values are "north", "east", "south", "west", "none";
    int[] prevMarker;

    public MarkerTE() {
        super(Reference.MARKER_TE);
    }

    public void init(UUID playerPlaced){
        this.playerPlaced = playerPlaced;

        TileEntity marker = null;
        if (this.getBlockState().getBlock() == Reference.PRISMARINE_MARKER) {
            marker = findWithinRadius(32);
        } else if (this.getBlockState().getBlock() == Reference.QUARTZ_MARKER){
            marker = findWithinRadius(16);
        }

        if(marker == null){
            createHead();
            return;
        } else {
            connectTo(marker);
        }
    }

    private void createHead(){
        isHead = true;
        isTail = true;
        connected = false;
        connectedFacing = null;
        prevMarker = null;
        initialized = true;
        this.markDirty();
    }

    private void connectTo(TileEntity prevTile){
        isHead = false;
        isTail = true;
        connected = true;

        BlockPos prevBlockPos = prevTile.getPos();
        CompoundNBT prevNBT = prevTile.getTileData();
        prevMarker = new int[]{prevBlockPos.getX(), prevBlockPos.getY(), prevBlockPos.getZ()};

        if(pos.getZ() == prevBlockPos.getZ()){
            if(prevBlockPos.getX() - pos.getX() > 0){
                connectedFacing = "west";
            } else {
                connectedFacing = "east";
            }
        } else {
            if(prevBlockPos.getZ() - pos.getZ() > 0){
                connectedFacing = "north";
            } else {
                connectedFacing = "south";
            }
        }
        prevNBT.putBoolean("is_tail", false);
        prevNBT.putBoolean("connected", true);

        initialized = true;
        this.markDirty();
        prevTile.markDirty();
    }

    private TileEntity findWithinRadius(int radiusCheck){

        for(int xz = 1; xz <= radiusCheck; xz++) {
            for (int y = -radiusCheck; y <= radiusCheck; y++) {
                //
                BlockPos blockEast = getTilePos(new BlockPos(pos.getX() + xz, pos.getY() + y, pos.getZ()));
                if (blockEast != null) return world.getTileEntity(blockEast);

                BlockPos blockWest = getTilePos(new BlockPos(pos.getX() - xz, pos.getY() + y, pos.getZ()));
                if (blockWest != null) return world.getTileEntity(blockWest);

                BlockPos blockNorth = getTilePos(new BlockPos(pos.getX(), pos.getY() + y, pos.getZ() + xz));
                if (blockNorth != null) return world.getTileEntity(blockNorth);

                BlockPos blockSouth = getTilePos(new BlockPos(pos.getX(), pos.getY() + y, pos.getZ() - xz));
                if (blockSouth != null) return world.getTileEntity(blockSouth);
            }
        }
        return null;
    }


    private BlockPos getTilePos(BlockPos posToSearch){
        TileEntity tile = world.getTileEntity(posToSearch);
        if(tile != null && tile instanceof MarkerTE){
            if(this.getBlockState().getBlock() ==  tile.getBlockState().getBlock()){
                CompoundNBT tileNBT = tile.getTileData();

                //  Might need a .equals here.
                if(tileNBT.getUniqueId("uuid") == playerPlaced && tileNBT.getBoolean("is_tail")){
                    if(tileNBT.getBoolean("is_head") && !tileNBT.getBoolean("connected")){
                        return posToSearch;
                    } else {
                        if(posToSearch.getX() == pos.getX()){ // Fixed in the x plane.
                            if(posToSearch.getZ() - pos.getZ() > 0){
                                // Check the new point dosen't go back on itself.
                                if(tileNBT.get("connected_facing").equals("south")) return null;
                            } else {
                                if(tileNBT.get("connected_facing").equals("north")) return null;
                            }
                            return posToSearch;
                        } else if(posToSearch.getZ() == pos.getZ()){ // Fixed in the z plane.
                            if(posToSearch.getX() - pos.getX() > 0){
                                if(tileNBT.get("connected_facing").equals("east")) return null;
                            } else {
                                if(tileNBT.get("connected_facing").equals("west")) return null;
                            }
                            return posToSearch;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if(compound == null) compound = new CompoundNBT();
        compound.putBoolean("initialized", this.initialized);
        compound.putBoolean("is_head", this.isHead);
        compound.putBoolean("is_tail", this.isTail);
        compound.putBoolean("connected", this.connected);
        //compound.putString("connected_facing", this.connectedFacing);
        compound.putIntArray("prev_marker", this.prevMarker);
        compound.putUniqueId("player_placed", this.playerPlaced);

        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound){
        super.read(compound);
        if(compound != null){
            this.initialized = compound.getBoolean("initialized");
            this.isHead = compound.getBoolean("is_head");
            this.isTail = compound.getBoolean("is_tail");
            this.connected = compound.getBoolean("connected");
            //this.connectedFacing = compound.getString("connected_facing");
            this.prevMarker = compound.getIntArray("prev_marker");
            this.playerPlaced = compound.getUniqueId(MarkerEvents.UUID_NBT_TAG);
        }
    }
}
