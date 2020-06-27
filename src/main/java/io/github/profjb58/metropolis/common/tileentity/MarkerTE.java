package io.github.profjb58.metropolis.common.tileentity;

import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.common.event.MarkerEvents;
import io.github.profjb58.metropolis.config.Config;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
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
            marker = findWithinRadius(RADIUS_CHECK);
        } else if (this.getBlockState().getBlock() == Reference.QUARTZ_MARKER) {
            RADIUS_CHECK = Config.COMMON.quartz_marker_radius.get();
            marker = findWithinRadius(RADIUS_CHECK);
        }
        if(marker == null){
            createHead(this);
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
        MarkerTE nmte = getMarkerFromPos(nextMarker);
        //  Tail
        if(isTail && !isHead){
            if(pmte != null){
                if(pmte.isHead) {
                    createHead(pmte);
                } else {
                    pmte.isTail = true;
                    pmte.nextMarker = null;
                    pmte.markDirty();
                    if(nmte.isHead){
                        nmte.prevMarker = null;
                    }
                }
                return;
            } else {
                Metropolis.LOGGER.error("Failed to correctly remove tail marker at: [" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "] " +
                        "for user with UUID: " + playerPlaced.toString());
                return;
            }
        }

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
            if(pmte.isHead) createHead(pmte);
            else{ pmte.isTail = true; pmte.nextMarker = null; }

            MarkerTE currentMarker = this;
            // Prevents infinite looping. Should only occour if a 'head' marker or marker of value 'null' is not found.
            // Therefore only happens upon NBT error.
            int MAX_LOOP_VALUE = 10000; int maxLoopCounter = 0;

            //  Check for a complete loop.
            while(currentMarker.nextMarker != null && maxLoopCounter <= MAX_LOOP_VALUE) {
                MarkerTE nextMarkerTile = getMarkerFromPos(currentMarker.nextMarker);
                if (nextMarkerTile.isHead) break;
                else if (maxLoopCounter == MAX_LOOP_VALUE) {
                    Metropolis.LOGGER.error("Unable to break loop when destroying all other required markers" +
                            "for corner marker placed at: [" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "]." +
                            "Please report this issue to: [INSERT LINK HERE] ");
                    break;
                } else if (nextMarkerTile != null && nextMarkerTile.playerPlaced == this.playerPlaced) {
                    world.playEvent(2001, pos, Block.getStateId(nextMarkerTile.getBlockState()));
                    world.removeBlock(currentMarker.nextMarker, false);
                    currentMarker = nextMarkerTile;
                }
                maxLoopCounter++;
            }
        } else {
            Metropolis.LOGGER.error("Failed to correctly delete marker at: [" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "] " +
                    "for user with UUID: " + playerPlaced.toString());
            return;
        }
    }

    private void createHead(MarkerTE mte){
        mte.isHead = true;
        mte.isTail = true;
        mte.connected = false;
        mte.prevMarker = null;
        mte.nextMarker = null;
        mte.connectedFacing = null;
        mte.markDirty();
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
            currentMarker.connected = true;
            currentMarker.prevMarker = pos;
            Metropolis.LOGGER.debug("Metropolis Marker region generated!");
            //TODO - Generate an actual region.
            return true;
        }
        return false;
    }

    private MarkerTE findWithinRadius(int radiusCheck){
        //  If Previous marker tile is a head then set to headMarker. Otherwise just transfer previous contents accross.

        //  Check in all 4 directions in a kind of spiral pattern.
        //  If a marker is found and on the same x or z plane as the head of the
        for(int xz = 1; xz <= radiusCheck; xz++) {
            for (int y = -radiusCheck; y <= radiusCheck; y++) {
                int x = xz; int z = xz;

                for(int i=0; i<=1; i++){ // Cycle twice, once for pos values and one for neg.
                    MarkerTE xMarker = getTailMarker(new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ()));
                    if(xMarker != null){
                        checkGenerateRegion(xMarker);
                        return xMarker;
                    }
                    MarkerTE zMarker = getTailMarker(new BlockPos(pos.getX(), pos.getY() + y, pos.getZ() + z));
                    if (zMarker != null) {
                        checkGenerateRegion(zMarker);
                        return zMarker;
                    }
                    x = -x; z = -z;
                }
            }
        }
        return null;
    }

    private void checkGenerateRegion(@Nonnull MarkerTE mte){
        if(mte.headMarker != null) {
            MarkerTE headMarker = getMarkerFromPos(mte.headMarker);
            if(headMarker.connected){
                if (mte.headMarker.getX() == pos.getX() || mte.headMarker.getZ() == pos.getZ()){
                    String direction = checkDirection(mte.getPos());
                    String headDirection = headMarker.connectedFacing;

                    if(headDirection != null){
                        if(!direction.equals(headDirection)) generateRegion = true;
                    }
                }
            }
        }
    }

    private MarkerTE getTailMarker(@Nonnull BlockPos posToSearch){
        TileEntity tile = world.getTileEntity(posToSearch);
        if(tile != null && tile instanceof MarkerTE){
            MarkerTE markerTile = (MarkerTE) tile;
            if(markerTile.playerPlaced == playerPlaced && markerTile.isTail){
                if(markerTile.isHead && markerTile.connected){
                    //  Ignore connected head markers.
                    return null;
                } else {
                    if(markerTile.connectedFacing == null){
                        return markerTile;
                    } else {
                        if(posToSearch.getX() == pos.getX()){ // Fixed in the x plane.
                            if(posToSearch.getZ() - pos.getZ() > 0){
                                // Check the new point dosen't go back on itself.
                                if(markerTile.connectedFacing.equals("south")) return null;
                            } else {
                                if(markerTile.connectedFacing.equals("north")) return null;
                            }
                            return markerTile;
                        } else if(posToSearch.getZ() == pos.getZ()){ // Fixed in the z plane.
                            if(posToSearch.getX() - pos.getX() > 0){
                                if(markerTile.connectedFacing.equals("east")) return null;
                            } else {
                                if(markerTile.connectedFacing.equals("west")) return null;
                            }
                            return markerTile;
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
        compound.putInt("radius_check", this.RADIUS_CHECK);
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
            this.RADIUS_CHECK = compound.getInt("radius_check");
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

    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        CompoundNBT nbtTag = new CompoundNBT();

        //Write your data into the nbtTag
        return new SUpdateTileEntityPacket(getPos(), -1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        CompoundNBT tag = pkt.getNbtCompound();
        //Handle your Data
    }
}
