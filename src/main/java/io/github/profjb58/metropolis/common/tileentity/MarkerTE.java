package io.github.profjb58.metropolis.common.tileentity;
import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.client.render.MarkerRegionRenderer;
import io.github.profjb58.metropolis.common.event.MarkerEvents;
import io.github.profjb58.metropolis.config.Config;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;

import static io.github.profjb58.metropolis.core.regions.MarkerRegion.getMarkerFromPos;

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
        MarkerTE pmte = getMarkerFromPos(prevMarker, world);
        MarkerTE nmte = getMarkerFromPos(nextMarker, world);

        //  Tail
        if(isTail && !isHead){
            if(pmte != null){
                if(pmte.isHead) {
                    createHead(pmte);
                } else {
                    pmte.isTail = true;
                    pmte.nextMarker = null;
                    if(nmte != null && nmte.isHead){
                        nmte.prevMarker = null;
                        nmte.isTail = false;
                        nmte.markDirty();
                    }
                }
                pmte.markDirty();
            } else {
                Metropolis.LOGGER.error("Failed to correctly remove tail marker at: [" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "] " +
                        "for user with UUID: " + playerPlaced.toString());

            }
        } else if(pmte != null && nmte != null){

            //  Straight section (Non-corner)
            if(pmte.connectedFacing.equals(connectedFacing) && nmte.connectedFacing.equals(connectedFacing) && !isHead) {
                Block prevBlock = pmte.getBlockState().getBlock() ; Block nextBlock = nmte.getBlockState().getBlock();
                if(prevBlock == getBlockState().getBlock() || nextBlock == getBlockState().getBlock()){
                    if (prevMarker.getZ() == nextMarker.getZ()) {
                        int dx = prevMarker.getX() - nextMarker.getX();
                        if (Math.abs(dx) <= RADIUS_CHECK) {
                            nmte.prevMarker = prevMarker;
                            pmte.nextMarker = nextMarker;
                        }
                    } else if (prevMarker.getX() == nextMarker.getX()) {
                        int dz = prevMarker.getZ() - nextMarker.getZ();
                        if (Math.abs(dz) <= RADIUS_CHECK) {
                            nmte.prevMarker = prevMarker;
                            pmte.nextMarker = nextMarker;
                        }
                    }
                }
            } else {
                //  Corner & Straight section > RADIUS_CHECK (Full destruction)
                pmte.isTail = true;
                pmte.nextMarker = null;

                MarkerTE currentMarker = this;
                //  Check for a complete loop.
                while(currentMarker.nextMarker != null) {
                    MarkerTE nextMarkerTile = getMarkerFromPos(currentMarker.nextMarker, world);
                    if(nextMarkerTile == null || nextMarkerTile.isHead || nextMarkerTile.playerPlaced != this.playerPlaced) break;
                    world.removeBlock(currentMarker.nextMarker, false);
                    currentMarker = nextMarkerTile;
                }

                if(pmte.isHead) createHead(pmte); // If previous block was a head then reset it.
            }
            nmte.markDirty();
            pmte.markDirty();
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
        MarkerTE currentMarker = getMarkerFromPos(headMarker, world);
        MarkerTE headMarkerRef = getMarkerFromPos(headMarker, world);
        if(currentMarker != null) {
            while (currentMarker.nextMarker != null) {
                MarkerTE nextMarkerTile = getMarkerFromPos(currentMarker.nextMarker, world);

                if (nextMarkerTile == null || nextMarkerTile.playerPlaced != this.playerPlaced) {
                    return false;
                } else if (nextMarkerTile.isTail) {
                    nextMarker = headMarker;
                    isTail = false;

                    // Adjust head
                    headMarkerRef.connected = true;
                    headMarkerRef.prevMarker = pos;
                    headMarkerRef.isTail = true;
                    Metropolis.LOGGER.debug("Metropolis Marker region generated!");
                    return true;
                }
                currentMarker = nextMarkerTile;
            }
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
            MarkerTE headMarker = getMarkerFromPos(mte.headMarker, world);
            if(headMarker.connected && headMarker != null){
                if (mte.headMarker.getX() == pos.getX() || mte.headMarker.getZ() == pos.getZ()){
                    String direction = checkDirection(mte.getPos());
                    String headDirection = headMarker.connectedFacing;
                    String connectingDirection = checkDirection(headMarker.getPos());

                    if(headDirection != null){
                        if(direction.equals(headDirection)){
                            if(!connectingDirection.equals(headDirection)) generateRegion = true;
                        } else {
                            generateRegion = true;
                        }
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

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if(compound == null) compound = new CompoundNBT();
        if(!world.isRemote){ // Only write data from within 'write' class if on the server.
            compound.putBoolean("is_head", this.isHead);
            compound.putBoolean("is_tail", this.isTail);
            compound.putBoolean("connected", this.connected);
            compound.putInt("radius_check", this.RADIUS_CHECK);
            if (connectedFacing != null) compound.putString("connected_facing", this.connectedFacing);
            if (prevMarker != null)
                compound.putIntArray("prev_marker", new int[]{prevMarker.getX(), prevMarker.getY(), prevMarker.getZ()});
            if (headMarker != null)
                compound.putIntArray("head_marker", new int[]{headMarker.getX(), headMarker.getY(), headMarker.getZ()});
            if (nextMarker != null)
                compound.putIntArray("next_marker", new int[]{nextMarker.getX(), nextMarker.getY(), nextMarker.getZ()});
            if (playerPlaced != null) compound.putUniqueId(MarkerEvents.UUID_NBT_TAG, this.playerPlaced);
        }
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
        return new SUpdateTileEntityPacket(getPos(), -1, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        CompoundNBT compound = pkt.getNbtCompound();
        handleUpdateTag(compound);
    }

    //  Chunk Loading
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT compound = new CompoundNBT();
        if(prevMarker != null) compound.putIntArray("prev_marker", new int[]{prevMarker.getX(), prevMarker.getY(), prevMarker.getZ()});
        return this.write(compound);
    }

    @Override
    public void handleUpdateTag(CompoundNBT compound) {
        super.handleUpdateTag(compound);

        BlockPos prevMarker = null;
        BlockPos currentMarker = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));

        if(compound.contains("prev_marker")){
            int[] posIntArray = compound.getIntArray("prev_marker");
            prevMarker = new BlockPos(posIntArray[0], posIntArray[1], posIntArray[2]);
        }
        MarkerRegionRenderer.addLineToDraw(currentMarker, prevMarker);
    }

    public UUID getPlayerPlaced(){
        return playerPlaced;
    }

    public String getConnectedFacing(){
        return connectedFacing;
    }

    public boolean isHead() { return isHead; }
    public boolean isTail() { return isTail; }
    public BlockPos getNextMarkerPos() { return nextMarker; }

}
