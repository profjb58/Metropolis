package io.github.profjb58.metropolis.common.tileentity;

import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.common.event.MarkerEvents;
import io.github.profjb58.metropolis.config.Config;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.UUID;

public class MarkerTE extends TileEntity {
    private int RADIUS_CHECK = 16;

    private boolean isHead, isTail, connected;
    private UUID playerPlaced = null;
    private String connectedFacing = null; // Values are "north", "east", "south", "west". ;
    private BlockPos prevMarker = null; private BlockPos nextMarker = null; private BlockPos headMarker = null;

    //  Temporary variable. Determines if the region can be connected and therefore generated.
    boolean generateRegion = false;

    public MarkerTE() {
        super(Reference.MARKER_TE);
    }

    public void init(@Nonnull UUID playerPlaced){
        this.playerPlaced = playerPlaced;

        MarkerTE marker = null;
        if (this.getBlockState().getBlock() == Reference.PRISMARINE_MARKER) {
            RADIUS_CHECK = Config.COMMON.prismarine_marker_radius.get();
            marker = (MarkerTE) findWithinRadius(RADIUS_CHECK);
        } else if (this.getBlockState().getBlock() == Reference.QUARTZ_MARKER){
            RADIUS_CHECK = Config.COMMON.quartz_marker_radius.get();
            marker = (MarkerTE) findWithinRadius(RADIUS_CHECK);
        }

        if(marker == null){
            createHead();
            return;
        } else {
            connectTo(marker);
        }
    }

    public void removeMarker(){
        //  Un-connected Head
        if(isHead && !connected){
            return;
        }
        MarkerTE pmte = getMarkerFromPos(prevMarker);
        //  Tail
        if(isTail && !isHead){
            if(pmte != null){
                if(pmte.isHead){
                    pmte.createHead();
                    pmte.connected = false;
                    pmte.nextMarker = null;
                    pmte.connectedFacing = null;
                } else {
                    pmte.isTail = true;
                    pmte.nextMarker = null;
                }
                pmte.markDirty();
                return;
            } else {
                Metropolis.LOGGER.error("Failed to correctly remove tail marker at: [" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "] " +
                        "for user with UUID: " + playerPlaced.toString());
                return;
            }
        }
        MarkerTE nmte = getMarkerFromPos(nextMarker);

        if(pmte != null && nmte != null){
            //  Straight section (Non-corner)
            if(pmte.connectedFacing.equals(connectedFacing) && nmte.connectedFacing.equals(connectedFacing)) {
                Block prevBlock = pmte.getBlockState().getBlock() ; Block nextBlock = nmte.getBlockState().getBlock();
                if(prevBlock == getBlockState().getBlock() || nextBlock == getBlockState().getBlock()){
                    if (prevMarker.getZ() == nextMarker.getZ()) {
                        int dx = prevMarker.getX() - nextMarker.getX();
                        if (Math.abs(dx) <= RADIUS_CHECK) {
                            nmte.prevMarker = prevMarker;
                            pmte.nextMarker = nextMarker;
                            return;
                        }
                    } else if (prevMarker.getX() == nextMarker.getX()) {
                        int dz = prevMarker.getZ() - nextMarker.getZ();
                        if (Math.abs(dz) <= RADIUS_CHECK) {
                            nmte.prevMarker = prevMarker;
                            pmte.nextMarker = nextMarker;
                            return;
                        }
                    }
                }
            }
            //  Corner & Straight section > RADIUS_CHECK (Full destruction)
            if(pmte != null && !pmte.isHead){
                pmte.isTail = true;
                pmte.nextMarker = null;

                MarkerTE currentMarker = this;
                int MAX_LOOP_VALUE = 10000; int maxLoopCounter = 0; // Prevents infinite looping.

                //  Check for a complete loop.
                while(currentMarker.nextMarker != null && maxLoopCounter <= MAX_LOOP_VALUE){
                    MarkerTE nextMarkerTile = getMarkerFromPos(currentMarker.nextMarker);
                    if(nextMarkerTile.isHead){
                        nextMarkerTile.prevMarker = null;
                        nextMarkerTile.isTail = false;
                        break;
                    } else if(nextMarkerTile != null && nextMarkerTile.playerPlaced == this.playerPlaced){
                        world.playEvent(2001,pos,Block.getStateId(nextMarkerTile.getBlockState()));
                        world.removeBlock(currentMarker.nextMarker, false);
                        currentMarker = nextMarkerTile;
                    } else if(maxLoopCounter == MAX_LOOP_VALUE){
                       Metropolis.LOGGER.error("Unable to break loop when destroying all other required markers" +
                               "for corner marker placed at: [" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "]." +
                               "Please report this issue to: [INSERT LINK HERE] ");
                    }
                    maxLoopCounter++;
                }
            } else {
                Metropolis.LOGGER.error("Failed to correctly remove corner marker at: [" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "] " +
                        "for user with UUID: " + playerPlaced.toString());
                return;
            }
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

        prevMarker = prevMarkerTile.getPos();

        if(prevMarkerTile.isHead){
            headMarker = prevMarker;
        } else {
            headMarker = prevMarkerTile.headMarker;
        }

        //  Check direction of current marker.
        connectedFacing = checkDirection(prevMarker);
        if(prevMarkerTile.isHead) prevMarkerTile.connectedFacing = connectedFacing;

        prevMarkerTile.isTail = false;
        prevMarkerTile.connected = true;
        prevMarkerTile.nextMarker = pos;

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
            MarkerTE prevMarkerTile = getMarkerFromPos(currentMarker.prevMarker);
            if(prevMarkerTile != null && prevMarkerTile.playerPlaced == this.playerPlaced){
                currentMarker = prevMarkerTile;
            } else {
                return false;
            }
        }

        if(currentMarker.isHead){
            nextMarker = currentMarker.pos;
            isTail = false;
            currentMarker.connected = true;
            currentMarker.prevMarker = pos;
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
                    BlockPos xBlock = getMarkerTailPos(new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ()));
                    if (xBlock != null) {
                        MarkerTE xMarker = (MarkerTE) world.getTileEntity(xBlock);
                        if(xMarker.headMarker != null) {
                            if (xMarker.headMarker.getX() == pos.getX() || xMarker.headMarker.getZ() == pos.getZ()){
                                if(!checkDirection(xBlock).equals(getMarkerFromPos(xMarker.headMarker).connectedFacing)) generateRegion = true;
                            }
                        }
                        return world.getTileEntity(xBlock);
                    }
                    BlockPos zBlock = getMarkerTailPos(new BlockPos(pos.getX(), pos.getY() + y, pos.getZ() + z));
                    if (zBlock != null) {
                        MarkerTE zMarker = (MarkerTE) world.getTileEntity(zBlock);
                        if(zMarker.headMarker != null) {
                            if (zMarker.headMarker.getX() == pos.getX() || zMarker.headMarker.getZ() == pos.getZ()){
                                if(!checkDirection(zBlock).equals(getMarkerFromPos(zMarker.headMarker).connectedFacing)) generateRegion = true;
                            }
                        }
                        return world.getTileEntity(zBlock);
                    }
                    x = -x; z = -z;
                }
            }
        }
        return null;
    }


    private BlockPos getMarkerTailPos(BlockPos posToSearch){
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

    private String checkDirection(BlockPos prevMarker){
        if(pos.getZ() == prevMarker.getZ()){
            if(prevMarker.getX() - pos.getX() > 0){
                return "west";
            } else {
                return "east";
            }
        } else {
            if(prevMarker.getZ() - pos.getZ() > 0){
                return "north";
            } else {
                return "south";
            }
        }
    }

    private MarkerTE getMarkerFromPos(BlockPos pos){
        if(pos != null){
            TileEntity te = world.getTileEntity(pos);
            if(te != null && te instanceof MarkerTE) return (MarkerTE) te;
        }
        return null;
    }

    public UUID getPlayerPlaced(){
        return playerPlaced;
    }

    public String getConnectedFacing(){
        return connectedFacing;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if(compound == null) compound = new CompoundNBT();
        compound.putBoolean("is_head", this.isHead);
        compound.putBoolean("is_tail", this.isTail);
        compound.putBoolean("connected", this.connected);
        if(connectedFacing != null) compound.putString("connected_facing", this.connectedFacing);
        if(prevMarker != null) compound.putIntArray("prev_marker", new int[]{prevMarker.getX(), prevMarker.getY(), prevMarker.getZ()});
        if(headMarker != null) compound.putIntArray("head_marker", new int[]{headMarker.getX(), headMarker.getY(), headMarker.getZ()});
        if(nextMarker != null) compound.putIntArray("next_marker", new int[]{nextMarker.getX(), nextMarker.getY(), nextMarker.getZ()});
        if(playerPlaced != null) compound.putUniqueId(MarkerEvents.UUID_NBT_TAG, this.playerPlaced);
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
            if(compound.contains("prev_marker")){
                int[] posIntArray = compound.getIntArray("prev_marker");
                this.prevMarker = new BlockPos(posIntArray[0], posIntArray[1], posIntArray[2]);
            }
            if(compound.contains("head_marker")){
                int[] posIntArray = compound.getIntArray("head_marker");
                this.headMarker = new BlockPos(posIntArray[0], posIntArray[1], posIntArray[2]);
            }
            if(compound.contains("next_marker")){
                int[] posIntArray = compound.getIntArray("next_marker");
                this.nextMarker = new BlockPos(posIntArray[0], posIntArray[1], posIntArray[2]);
            }
            if(compound.hasUniqueId("player_placed")) this.playerPlaced = compound.getUniqueId(MarkerEvents.UUID_NBT_TAG);
        }
    }
}
