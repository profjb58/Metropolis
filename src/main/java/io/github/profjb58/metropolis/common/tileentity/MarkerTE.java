package io.github.profjb58.metropolis.common.tileentity;

import io.github.profjb58.metropolis.Metropolis;
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

    boolean isHead, isTail, connected;
    UUID playerPlaced = null;
    String connectedFacing = null; // Values are "north", "east", "south", "west";
    int[] prevMarker = null;
    int[] headMarker = null;

    //  Temporary variable. Determines if the region can be connected and therefore generated.
    boolean generateRegion = false;

    public MarkerTE() {
        super(Reference.MARKER_TE);
    }

    public void init(UUID playerPlaced){
        if(playerPlaced == null){
            Metropolis.LOGGER.error("Invalid associated player UUID for tile entity placement of a Marker");
            return;
        } else {
            this.playerPlaced = playerPlaced;
        }

        MarkerTE marker = null;
        if (this.getBlockState().getBlock() == Reference.PRISMARINE_MARKER) {
            marker = (MarkerTE) findWithinRadius(32);
        } else if (this.getBlockState().getBlock() == Reference.QUARTZ_MARKER){
            marker = (MarkerTE) findWithinRadius(16);
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
        this.markDirty();
    }

    private void connectTo(MarkerTE prevMarkerTile) {
        isHead = false;
        isTail = true;
        connected = true;

        BlockPos prevBlockPos = prevMarkerTile.getPos();
        prevMarker = new int[]{prevBlockPos.getX(), prevBlockPos.getY(), prevBlockPos.getZ()};

        if(prevMarkerTile.isHead){
            headMarker = new int[]{prevBlockPos.getX(), prevBlockPos.getY(), prevBlockPos.getZ()};
        } else {
            headMarker = prevMarkerTile.headMarker;
        }

        //  Check direction of current marker.
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
        prevMarkerTile.isTail = false;
        prevMarkerTile.connected = true;

        //  Check if any connected head markers are found.
        if(generateRegion){
            boolean generated = generateRegion();
            // TODO - Check this dosen't send a message serverside. Should only send on the client.
            // TODO - Add link to explain the exception.
            if(!generated) Metropolis.LOGGER.info("Metropolis region failed to generate from marker at pos " +
                    "[" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "] visit [INSERT LINK HERE] for more information" );
        }

        this.markDirty();
        prevMarkerTile.markDirty();
    }

    private boolean generateRegion() {
        MarkerTE currentMarker = this;
        //  Check for a complete loop.
        while(currentMarker.prevMarker != null){
            TileEntity prevTile = world.getTileEntity(new BlockPos(currentMarker.prevMarker[0], currentMarker.prevMarker[1], currentMarker.prevMarker[2]));
            if(prevTile instanceof MarkerTE){
                MarkerTE prevMarkerTile = (MarkerTE) prevTile;
                if(prevMarkerTile != null && prevMarkerTile.playerPlaced == this.playerPlaced){
                    currentMarker = prevMarkerTile;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        if(currentMarker.isHead){
            isTail = false;
            currentMarker.connected = true;
            currentMarker.prevMarker = new int[]{pos.getX(), pos.getY(), pos.getZ()};
            currentMarker.isTail = true;
            Metropolis.LOGGER.debug("Metropolis Marker region generated!");
            //TODO - Generate an actual region.
            return true;
        }
        return false;
    }

    private TileEntity findWithinRadius(int radiusCheck){
        //  If Previous marker tile is a head then set to headMarker. Otherwise just transfer previous contents accross.

        //  Check in all 4 directions in a kind of spiral pattern.
        //  If a marker is found and on the same x or z plane as the head of the
        BlockPos headMarkerBlockPos = null;
        for(int xz = 1; xz <= radiusCheck; xz++) {
            for (int y = -radiusCheck; y <= radiusCheck; y++) {
                int x = xz; int z = xz;

                for(int i=0; i<=1; i++){ // Cycle twice, once for pos values and one for neg.
                    BlockPos xBlock = getTileTailPos(new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ()));
                    if (xBlock != null) {
                        MarkerTE xMarker = (MarkerTE) world.getTileEntity(xBlock);
                        if(xMarker.headMarker != null) {
                            headMarkerBlockPos = new BlockPos(xMarker.headMarker[0], xMarker.headMarker[1], xMarker.headMarker[2]);
                            if (headMarkerBlockPos.getX() == pos.getX() || headMarkerBlockPos.getZ() == pos.getZ()) generateRegion = true;
                        }
                        return world.getTileEntity(xBlock);
                    }
                    BlockPos zBlock = getTileTailPos(new BlockPos(pos.getX(), pos.getY() + y, pos.getZ() + z));
                    if (zBlock != null) {
                        MarkerTE zMarker = (MarkerTE) world.getTileEntity(zBlock);
                        if(zMarker.headMarker != null) {
                            headMarkerBlockPos = new BlockPos(zMarker.headMarker[0], zMarker.headMarker[1], zMarker.headMarker[2]);
                            if (headMarkerBlockPos.getX() == pos.getX() || headMarkerBlockPos.getZ() == pos.getZ()) generateRegion = true;
                        }
                        return world.getTileEntity(zBlock);
                    }
                    x = -x; z = -z;
                }
            }
        }
        return null;
    }


    private BlockPos getTileTailPos(BlockPos posToSearch){
        TileEntity tile = world.getTileEntity(posToSearch);
        if(tile != null && tile instanceof MarkerTE){
            MarkerTE markerTile = (MarkerTE) tile;
            if(this.getBlockState().getBlock() ==  tile.getBlockState().getBlock()){
                if(markerTile.playerPlaced == playerPlaced && markerTile.isTail){
                    if(markerTile.isHead && markerTile.connected){
                        //  Ignore connected head markers.
                        return null;
                    } else {
                        if(markerTile.connectedFacing == null){
                            return posToSearch;
                        } else {
                            if(posToSearch.getX() == pos.getX()){ // Fixed in the x plane.
                                if(posToSearch.getZ() - pos.getZ() > 0){
                                    // Check the new point dosen't go back on itself.
                                    if(markerTile.connectedFacing.equals("south")) return null;
                                } else {
                                    if(markerTile.connectedFacing.equals("north")) return null;
                                }
                                return posToSearch;
                            } else if(posToSearch.getZ() == pos.getZ()){ // Fixed in the z plane.
                                if(posToSearch.getX() - pos.getX() > 0){
                                    if(markerTile.connectedFacing.equals("east")) return null;
                                } else {
                                    if(markerTile.connectedFacing.equals("west")) return null;
                                }
                                return posToSearch;
                            }
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
        compound.putBoolean("is_head", this.isHead);
        compound.putBoolean("is_tail", this.isTail);
        compound.putBoolean("connected", this.connected);
        if(connectedFacing != null) compound.putString("connected_facing", this.connectedFacing);
        if(prevMarker != null) compound.putIntArray("prev_marker", this.prevMarker);
        if(headMarker != null) compound.putIntArray("head_marker", this.headMarker);
        if(playerPlaced != null) compound.putUniqueId("player_placed", this.playerPlaced);

        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound){
        super.read(compound);
        if(compound != null){
            this.isHead = compound.getBoolean("is_head");
            this.isTail = compound.getBoolean("is_tail");
            this.connected = compound.getBoolean("connected");
            if(compound.contains("connected_facing")) this.connectedFacing = compound.getString("connected_facing");
            if(compound.contains("prev_marker")) this.prevMarker = compound.getIntArray("prev_marker");
            if(compound.contains("head_marker")) this.headMarker = compound.getIntArray("head_marker");
            if(compound.contains("player_placed")) this.playerPlaced = compound.getUniqueId(MarkerEvents.UUID_NBT_TAG);

        }
    }
}
