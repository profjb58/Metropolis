package io.github.profjb58.metropolis.common.tileentity;
import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.client.render.MarkerRegionRenderer;
import io.github.profjb58.metropolis.common.event.MarkerEvents;
import io.github.profjb58.metropolis.config.Config;
import io.github.profjb58.metropolis.util.DirectionHelper;
import io.github.profjb58.metropolis.util.RegionHelper;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class MarkerTE extends TileEntity {

    private boolean isHead, isTail;
    private UUID playerPlaced = null;
    private Direction connectedFacing = null;
    private BlockPos prevMarker = null; private BlockPos nextMarker = null; private BlockPos headMarker = null;

    //  Temporary variable. Determines if the region can be connected and therefore generated.
    boolean generateRegion = false;

    public MarkerTE() {
        super(Reference.MARKER_TE);
    }

    public void init(@Nonnull UUID playerPlaced){
        this.playerPlaced = playerPlaced;
        MarkerTE marker = findWithinRadius(getMarkerRadius(this.getBlockState().getBlock()));

        if(marker == null){
            createHead(this);
        } else {
            connectTo(marker);
        }
    }

    /**
     * Gets radius to search for different type of Marker Tile Entities. Keep properly updated.
     **/
    public static int getMarkerRadius(Block markerBlock){
        if(markerBlock == Reference.PRISMARINE_MARKER){
            return Config.COMMON.prismarine_marker_radius.get();
        } else if(markerBlock == Reference.QUARTZ_MARKER){
            return Config.COMMON.quartz_marker_radius.get();
        } else {
            return 0;
        }
    }

    private void reset(){
        isHead = false;
        isTail = false;
        headMarker = null;
        prevMarker = null;
        nextMarker = null;
        connectedFacing = null;
        markDirty();
    }

    private void createHead(MarkerTE mte){
        mte.isHead = true;
        mte.isTail = true;
        mte.prevMarker = null;
        mte.nextMarker = null;
        mte.connectedFacing = null;
        mte.markDirty();
    }

    private void connectTo(MarkerTE prevMarkerTile) {

        isHead = false;
        isTail = true;
        prevMarker = prevMarkerTile.getPos();
        prevMarkerTile.isTail = false;
        prevMarkerTile.nextMarker = pos;

        if(prevMarkerTile.isHead){
            headMarker = prevMarker;
        } else {
            headMarker = prevMarkerTile.headMarker;
        }

        //  Check direction of current marker.
        connectedFacing = DirectionHelper.getDirectionBetween(pos, prevMarker);
        if(prevMarkerTile.isHead) {
            prevMarkerTile.connectedFacing = connectedFacing;
        }

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

    private MarkerTE getTailMarker(@Nonnull BlockPos posToSearch){
        TileEntity tile = world.getTileEntity(posToSearch);
        if(tile != null && tile instanceof MarkerTE){
            MarkerTE markerTile = (MarkerTE) tile;
            if(markerTile.playerPlaced.equals(playerPlaced) && markerTile.isTail){
                if(markerTile.isHead && markerTile.connectedFacing != null){
                    //  Ignore connected head markers.
                    return null;
                } else {
                    if(markerTile.connectedFacing == null){
                        return markerTile;
                    } else {
                        //  Check when placing a marker we don't go back on ourselves.
                        Direction markerLinkDirection = DirectionHelper.getDirectionBetween(pos, posToSearch);
                        if(markerTile.connectedFacing == markerLinkDirection.getOpposite()){
                            return null;
                        } else {
                            return markerTile;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void checkGenerateRegion(@Nonnull MarkerTE mte){
        if(mte.headMarker != null) {
            MarkerTE headMarker = getMarkerFromPos(mte.headMarker, world);
            if(headMarker.connectedFacing != null && headMarker != null){

                //  If head marker in the same plane as the marker to be linked 'mte' try to generate region.
                if (mte.headMarker.getX() == pos.getX() || mte.headMarker.getZ() == pos.getZ()){
                    Direction connectingDirectionTail = DirectionHelper.getDirectionBetween(pos, mte.getPos());
                    Direction headDirection = headMarker.connectedFacing;
                    Direction connectingDirectionHead = DirectionHelper.getDirectionBetween(pos, headMarker.getPos());

                    if(headDirection != null){
                        if(connectingDirectionTail == headDirection){
                            if(!(connectingDirectionHead == headDirection)) generateRegion = true;
                        } else {
                            generateRegion = true;
                        }
                    }
                }
            }
        }
    }

    public void removeMarker(){
        //  Un-connected Head
        if(isHead && connectedFacing == null){
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
            Block thisBlock = getBlockState().getBlock();

            //  Straight section (Non-corner)
            if(pmte.connectedFacing == connectedFacing && nmte.connectedFacing == connectedFacing && !isHead) {
                Block prevBlock = pmte.getBlockState().getBlock();
                Block nextBlock = nmte.getBlockState().getBlock();

                if(prevBlock == thisBlock || nextBlock == thisBlock){
                    if (prevMarker.getZ() == nextMarker.getZ()) {
                        int dx = prevMarker.getX() - nextMarker.getX();
                        if (Math.abs(dx) <= getMarkerRadius(thisBlock)) {
                            nmte.prevMarker = prevMarker;
                            pmte.nextMarker = nextMarker;
                        }
                    } else if (prevMarker.getX() == nextMarker.getX()) {
                        int dz = prevMarker.getZ() - nextMarker.getZ();
                        if (Math.abs(dz) <= getMarkerRadius(thisBlock)) {
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
                    if(nextMarkerTile == null || !nextMarkerTile.playerPlaced.equals(this.playerPlaced)){
                        break;
                    } else if(nextMarkerTile.isHead){
                        nextMarkerTile.prevMarker = null;
                        nextMarkerTile.markDirty();
                        break;
                    }
                    world.removeBlock(currentMarker.nextMarker, false);
                    currentMarker = nextMarkerTile;
                }

                if(pmte.isHead) createHead(pmte); // If previous block was a head then reset it
            }
            nmte.markDirty();
            pmte.markDirty();
        } else {
            Metropolis.LOGGER.debug("Failed to correctly delete marker at: [" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "] " +
                    "for user with UUID: " + playerPlaced.toString());
            return;
        }
    }

    private boolean generateRegion() {
        MarkerTE currentMarker = getMarkerFromPos(headMarker, world);
        MarkerTE headMarkerRef = getMarkerFromPos(headMarker, world);
        if(currentMarker != null) {
            while (currentMarker.nextMarker != null) {
                MarkerTE nextMarkerTile = getMarkerFromPos(currentMarker.nextMarker, world);

                if (nextMarkerTile == null || !nextMarkerTile.playerPlaced.equals(this.playerPlaced)) {
                    return false;
                } else if (nextMarkerTile.isTail) {
                    nextMarker = headMarker;
                    isTail = false;

                    // Adjust head
                    headMarkerRef.prevMarker = pos;
                    headMarkerRef.isTail = true;
                    headMarkerRef.markDirty();
                    Metropolis.LOGGER.debug("Metropolis Marker region generated!");
                    return true;
                }
                currentMarker = nextMarkerTile;
            }
        }
        return false;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        notifyClientUpdate();
    }

    private void notifyClientUpdate(){
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }


    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        if (playerPlaced != null) compound.putUniqueId(MarkerEvents.UUID_NBT_TAG, this.playerPlaced);

        compound.putBoolean("is_tail", this.isTail);
        compound.putBoolean("is_head", this.isHead);

        if (connectedFacing != null) compound.putString("connected_facing", DirectionHelper.convertToNBTString(this.connectedFacing));
        if (prevMarker != null)
            compound.putIntArray("prev_marker", new int[]{prevMarker.getX(), prevMarker.getY(), prevMarker.getZ()});
        if (nextMarker != null)
            compound.putIntArray("next_marker", new int[]{nextMarker.getX(), nextMarker.getY(), nextMarker.getZ()});
        if (headMarker != null)
            compound.putIntArray("head_marker", new int[]{headMarker.getX(), headMarker.getY(), headMarker.getZ()});

        return compound;
    }


    @Override
    public void read(CompoundNBT compound){
        super.read(compound);

        if(compound.hasUniqueId("player_placed")) this.playerPlaced = compound.getUniqueId(MarkerEvents.UUID_NBT_TAG);

        this.isHead = compound.getBoolean("is_head");
        this.isTail = compound.getBoolean("is_tail");

        if(compound.contains("connected_facing")){
            this.connectedFacing = DirectionHelper.convertNBTStringToDirection(compound.getString("connected_facing"));
        }
        if(compound.contains("prev_marker")){
            int[] posIntArray = compound.getIntArray("prev_marker");
            this.prevMarker = new BlockPos(posIntArray[0], posIntArray[1], posIntArray[2]);
        }

        if(compound.contains("next_marker")){
            int[] posIntArray = compound.getIntArray("next_marker");
            this.nextMarker = new BlockPos(posIntArray[0], posIntArray[1], posIntArray[2]);
        }

        if(compound.contains("head_marker")){
            int[] posIntArray = compound.getIntArray("head_marker");
            this.headMarker = new BlockPos(posIntArray[0], posIntArray[1], posIntArray[2]);
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

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT compound = new CompoundNBT();
        this.write(compound);

        return compound;
    }

    @Override
    public void handleUpdateTag(CompoundNBT compound) {
        this.read(compound);
        BlockPos currentMarker = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));

        if(prevMarker == null && headMarker == null && isTail){ // Is the head.
            MarkerRegionRenderer.setCurrentTailPos(currentMarker);
            MarkerRegionRenderer.resetCurrentFacing();
        }
        MarkerRegionRenderer.addMarkerToDraw(currentMarker, headMarker, connectedFacing, isTail);
    }

    public static MarkerTE getMarkerFromPos(BlockPos pos, World world){
        if(pos != null){
            TileEntity te = world.getTileEntity(pos);
            if(te != null && te instanceof MarkerTE) return (MarkerTE) te;
        }
        return null;
    }

    public UUID getPlayerPlaced(){
        return playerPlaced;
    }

    public Direction getConnectedFacing(){
        return connectedFacing;
    }

    public boolean isHead() { return isHead; }

    public boolean isTail() { return isTail; }

    public BlockPos getNextMarkerPos() { return nextMarker; }

    public BlockPos getPrevMarkerPos() { return prevMarker; }

}
