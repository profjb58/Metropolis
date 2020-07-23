package io.github.profjb58.metropolis.common.tileentity;
import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.client.render.MarkerLineRenderer;
import io.github.profjb58.metropolis.common.data.regions.Region;
import io.github.profjb58.metropolis.common.handlers.MarkerHandler;
import io.github.profjb58.metropolis.common.config.Config;
import io.github.profjb58.metropolis.common.util.DirectionHelper;
import io.github.profjb58.metropolis.common.util.regions.RegionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.UUID;

public class Marker extends TileEntity {

    private boolean isHead, isTail;
    private UUID playerPlaced = null;
    private Direction connectedFacing = null;
    private BlockPos prevMarker = null; private BlockPos nextMarker = null; private BlockPos headMarker = null;

    //  Temporary variables not saved to nbt. Determines if the region can be connected and therefore generated.
    boolean generateRegion = false;

    public Marker() {
        super(Reference.MARKER_TE);
    }

    //  Initialize marker either creating the 'head' of the region or connecting to a tail.
    public void init(PlayerEntity playerPlaced){
        this.playerPlaced = playerPlaced.getUniqueID();
        PlayerEntity player = playerPlaced;

        Marker marker = findWithinRadius(getMarkerRadius(this.getBlockState().getBlock()));

        if(marker == null){ // No tail markers found.
            createHead(this);
        } else {
            connectTo(marker);
        }

        if(generateRegion) {
            ArrayList<BlockPos> path = generateRegion();

            if(path != null){
                player.sendMessage(Metropolis.metropolisTextHeader);
                player.sendMessage(new StringTextComponent("Region Generated!"));

                LinkedList<int[]> rectRegions = Region.Generator.generateRectPolygons(path, world);

                if(Config.COMMON.debug_enabled.get()) {
                    RegionHelper.printPathForPlayer(player, path);
                    RegionHelper.printPolygonsForPlayer(player, rectRegions);

                    for(int[] rectPolygon : rectRegions) {
                        for (int y = 100; y >= 10; y--) {
                            for (int x = rectPolygon[0]; x < rectPolygon[1]; x++) {
                                for (int z = rectPolygon[2]; z < rectPolygon[3]; z++) {
                                    BlockPos posToPlace = new BlockPos(x, y, z);
                                    world.setBlockState(posToPlace, Blocks.GLASS.getDefaultState());
                                }
                            }
                        }
                    }

                }
            } else {
                Metropolis.LOGGER.info("Metropolis region failed to generate from marker at pos " +
                        "[" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "] visit [INSERT LINK HERE] for more information");
            }
        }
    }

    /**
     * Gets radius to search for different type of Marker Tile Entities.
     * On adding a new marker keep updated.
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

    private void createHead(Marker mte){
        mte.isHead = true;
        mte.isTail = true;
        mte.prevMarker = null;
        mte.nextMarker = null;
        mte.connectedFacing = null;
        mte.markDirty();
    }

    private void connectTo(Marker prevMarkerTile) {

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

//        //  Check if any connected head markers are found.
//        if(generateRegion){
//            boolean generated = generateRegion();
//            // TODO - Check this dosen't send a message serverside. Should only send on the client.
//            // TODO - Add link to explain the exception.
//            if(!generated) Metropolis.LOGGER.info("Metropolis region failed to generate from marker at pos " +
//                    "[" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "] visit [INSERT LINK HERE] for more information" );
//        }

        this.markDirty();
        prevMarkerTile.markDirty();
    }

    private Marker findWithinRadius(int radiusCheck){
        //  If Previous marker tile is a head then set to headMarker. Otherwise just transfer previous contents accross.

        //  Check in all 4 directions in a kind of spiral pattern.
        //  If a marker is found and on the same x or z plane as the head of the
        for(int xz = 1; xz <= radiusCheck; xz++) {
            for (int y = -radiusCheck; y <= radiusCheck; y++) {
                int x = xz; int z = xz;

                for(int i=0; i<=1; i++){ // Cycle twice, once for pos values and one for neg.
                    Marker xMarker = getTailMarker(new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ()));
                    if(xMarker != null){
                        checkGenerateRegion(xMarker);
                        return xMarker;
                    }
                    Marker zMarker = getTailMarker(new BlockPos(pos.getX(), pos.getY() + y, pos.getZ() + z));
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

    private Marker getTailMarker(@Nonnull BlockPos posToSearch){
        TileEntity tile = world.getTileEntity(posToSearch);
        if(tile != null && tile instanceof Marker){
            Marker markerTile = (Marker) tile;
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

    private void checkGenerateRegion(@Nonnull Marker mte){
        if(mte.headMarker != null) {
            Marker headMarker = getMarkerFromPos(mte.headMarker, world);
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
        Marker pmte = getMarkerFromPos(prevMarker, world);
        Marker nmte = getMarkerFromPos(nextMarker, world);

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
                Metropolis.LOGGER.debug("Failed to correctly remove tail marker at: [" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "] " +
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

                Marker currentMarker = this;
                //  Check for a complete loop.
                while(currentMarker.nextMarker != null) {
                    Marker nextMarkerTile = getMarkerFromPos(currentMarker.nextMarker, world);
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
            pmte.markDirty();
            nmte.markDirty();
        } else {
            Metropolis.LOGGER.debug("Failed to correctly delete marker at: [" + "x:" + pos.getX() + " y:" + pos.getY() + " z:" + pos.getZ() + "] " +
                    "for user with UUID: " + playerPlaced.toString());
            return;
        }
    }

    /**
     * Check if a region can be generated by looping forwards from the head marker to see if there is a link.
     * @return - path for completed region if a region can be generated. (Only includes corner segments)
     */
    private ArrayList<BlockPos> generateRegion() {
        ArrayList<BlockPos> path = new ArrayList<>();

        Marker currentMarker = getMarkerFromPos(headMarker, world);
        Marker headMarkerRef = getMarkerFromPos(headMarker, world);
        if(currentMarker != null) {
            while (currentMarker.nextMarker != null) {
                Marker nextMarkerTile = getMarkerFromPos(currentMarker.nextMarker, world);
                BlockPos prevPos = currentMarker.getPrevMarkerPos();
                BlockPos nextPos = nextMarkerTile.getPos();

                if (nextMarkerTile == null || !nextMarkerTile.playerPlaced.equals(this.playerPlaced)) {
                    return null;
                } else if(prevPos != null && (prevPos.getX() == nextPos.getX() || prevPos.getZ() == nextPos.getZ())){ // Straight section.
                    currentMarker = nextMarkerTile;
                } else if (nextMarkerTile.isTail) {
                    nextMarker = headMarker;
                    isTail = false;

                    // Adjust head
                    headMarkerRef.prevMarker = pos;
                    headMarkerRef.isTail = true;
                    headMarkerRef.markDirty();

                    // Add the final tail marker and head to the path list.
                    path.add(currentMarker.pos);
                    path.add(nextMarkerTile.pos);
                    return path;
                } else {
                    path.add(currentMarker.pos);
                    currentMarker = nextMarkerTile;
                }
            }
        }
        return null;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
         super.write(compound);

        if (playerPlaced != null) compound.putUniqueId(MarkerHandler.UUID_NBT_TAG, this.playerPlaced);

        compound.putBoolean("is_tail", this.isTail);
        compound.putBoolean("is_head", this.isHead);

        if (connectedFacing != null) compound.putString("connected_facing", DirectionHelper.convertToNBTString(this.connectedFacing));

        if (prevMarker != null) compound.putIntArray("prev_marker", new int[]{prevMarker.getX(), prevMarker.getY(), prevMarker.getZ()});
        else compound.putIntArray("prev_marker", new int[]{}); // Integer array cannot be null. Hence make empty array.

        if (nextMarker != null) compound.putIntArray("next_marker", new int[]{nextMarker.getX(), nextMarker.getY(), nextMarker.getZ()});
        else compound.putIntArray("next_marker", new int[]{});

        if (headMarker != null) compound.putIntArray("head_marker", new int[]{headMarker.getX(), headMarker.getY(), headMarker.getZ()});
        else compound.putIntArray("head_marker", new int[]{});

        return compound;
    }


    @Override
    public void read(CompoundNBT compound){
        super.read(compound);

        if(compound.hasUniqueId("player_placed")) this.playerPlaced = compound.getUniqueId(MarkerHandler.UUID_NBT_TAG);

        this.isHead = compound.getBoolean("is_head");
        this.isTail = compound.getBoolean("is_tail");

        if(compound.contains("connected_facing")){
            this.connectedFacing = DirectionHelper.convertNBTStringToDirection(compound.getString("connected_facing"));
        }

        if(compound.getIntArray("prev_marker").length == 0){ // Check if the integer array is empty.
            this.prevMarker = null;
        } else {
            int[] posIntArray = compound.getIntArray("prev_marker");
            this.prevMarker = new BlockPos(posIntArray[0], posIntArray[1], posIntArray[2]);
        }

        if(compound.getIntArray("next_marker").length == 0){
            this.nextMarker = null;
        } else {
            int[] posIntArray = compound.getIntArray("next_marker");
            this.nextMarker = new BlockPos(posIntArray[0], posIntArray[1], posIntArray[2]);
        }

        if(compound.getIntArray("head_marker").length == 0){
            headMarker = null;
        } else {
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

        if(isTail){
            MarkerLineRenderer.setCurrentTailPos(currentMarker);
            MarkerLineRenderer.resetCurrentFacing();
        }
    }

    public static Marker getMarkerFromPos(BlockPos pos, World world){
        if(pos != null){
            TileEntity te = world.getTileEntity(pos);
            if(te != null && te instanceof Marker) return (Marker) te;
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
