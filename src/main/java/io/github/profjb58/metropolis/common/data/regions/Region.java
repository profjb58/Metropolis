package io.github.profjb58.metropolis.common.data.regions;

import com.google.common.collect.ArrayListMultimap;
import io.github.profjb58.metropolis.Metropolis;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.core.tools.Generate;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class Region {

    public static class Generator {

        public static String generateRegionID(UUID uuid, BlockPos markerHeadPos) {
            String uuidString = uuid.toString();
            String headPosString = markerHeadPos.toString();

            return uuidString + "_" + headPosString;
        }

//        private static ArrayList<Integer> spliceLine(Line2D line, TreeSet<Integer> allZColumnValues) {
//            ArrayList<Integer> splicedLines = new ArrayList<>();
//            if (line.getX1() == line.getX2()) { // Check the line is within the z plane.
//                for (Integer val : allZColumnValues) {
//
//
//                    if (val >= line.getY1() && val <= line.getY2()) { // Imagine y == z. Using 2D line function provided.
//                        splicedLines.add(val);
//                    }
//                }
//                return splicedLines;
//            } else {
//                return null;
//            }
//        }

        // TODO - Used for declaring which chunks a player has claimed.
        private static Stack<ChunkPos> findChunksWithinLine(Line2D line, World world) {
            Stack<ChunkPos> chunks = new Stack<>();
            int xChunkPos1 = (int) line.getX1() >> 4; int zChunkPos1 = (int) line.getY1() >> 4;
            int xChunkPos2 = (int) line.getX2() >> 4; int zChunkPos2 = (int) line.getY2() >> 4;

            ChunkPos chunkStart = new ChunkPos(xChunkPos1, zChunkPos1);
            ChunkPos chunkEnd = new ChunkPos(xChunkPos2, zChunkPos2);

            if (chunkStart.equals(chunkEnd)) {
                chunks.add(chunkStart);
            } else {
                int xChunkMax, xChunkMin, zChunkMax, zChunkMin;
                xChunkMax = Math.max(chunkStart.x, chunkEnd.x);
                xChunkMin = Math.max(chunkStart.x, chunkEnd.x);
                zChunkMax = Math.max(chunkStart.z, chunkEnd.z);
                zChunkMin = Math.max(chunkStart.z, chunkEnd.z);

                if (xChunkMax == xChunkMin) { // Z line
                    for (int zChunkPos = zChunkMin; zChunkPos <= zChunkMax; zChunkPos++) {
                        chunks.add(new ChunkPos(xChunkMin, zChunkPos));
                    }
                } else if (zChunkMin == zChunkMax) { // X line
                    for (int xChunkPos = xChunkMin; xChunkPos <= xChunkMax; xChunkPos++) {
                        chunks.add(new ChunkPos(xChunkPos, zChunkMin));
                    }
                }
            }
            return chunks;
        }

        /**
         * Generate rectilinear polygons within the interior of the marker region.
         * Used for defining a larger region.
         *
         * @param path - Marker path.
         * @return - List of coordinates to mark all the rectangular polygons contained within the given region.
         */
        public static LinkedList<int[]> generateRectPolygons(ArrayList<BlockPos> path, World world) {

            /* Rectangular regions. [Format = (xStart, xEnd, zStart, zEnd)].
                 e.g. a rectangle with coords = (-100, 50), (-100, 80), (50, 50), (50, 80)
                 has an array of [-100, 50, 50, 80]. */
            LinkedList<int[]> rectRegions = new LinkedList<>();
            Stack<ChunkPos> chunks = new Stack<>();

            // Total z lines within each chunk. Used to define rectangular regions.
//            HashMap<ChunkPos, ArrayList<Line2D>> zLines = new HashMap<>();
//            HashMap<ChunkPos, ArrayList<Line2D>> xLines = new HashMap<>();

            HashMap<Integer, HashMap<Integer, Line2D>> zLines = new HashMap<>();

            /* Ordered points for defining scalar amounts for each rectangle within the region.
               TreeSet automatically orders and prevents duplicate values. */
            TreeSet<Integer> xRowValues = new TreeSet<>();
            TreeSet<Integer> zColumnValues = new TreeSet<>();
            for (BlockPos blockPos : path) {
                xRowValues.add(blockPos.getX());
                zColumnValues.add(blockPos.getZ());
            }

            for (int i = 0; i < path.size(); i++) {
                int x1 = path.get(i).getX();
                int z1 = path.get(i).getZ();
                int x2; int z2;

                if ((i + 1) == path.size()) { // Last line. (The tail)
                    x2 = path.get(0).getX();
                    z2 = path.get(0).getZ();
                } else { // Normal line.
                    x2 = path.get(i + 1).getX();
                    z2 = path.get(i + 1).getZ();
                }
                int zMin = Math.min(z1, z2);
                int zMax = Math.max(z1, z2);

                Line2D line = new Line2D.Double(x1, zMin, x2, zMax);
                if(x1 == x2){ // If the line is in the z plane.

                    for(Integer zRow : zColumnValues){
                        if(zRow >= zMin && zRow <= zMax){
                            if(!zLines.containsKey(zRow)){
                                zLines.put(zRow, new HashMap<>());
                            }
                            HashMap<Integer, Line2D> lines = zLines.get(zRow);
                            lines.put(x1, line);
                        }
                    }
                }
            }
            Metropolis.LOGGER.debug("z-lines: " + zLines.toString());

            double x1, x2, z; // Rectangular region points.
            int[] rectPolygon = new int[4];

            Iterator<Integer> zIterator = zColumnValues.iterator();
            Integer prevZColumn, currentZColumn;
            if (zIterator.hasNext()) {
                prevZColumn = zIterator.next();
                x1 = xRowValues.first() - 0.1;
                x2 = xRowValues.last() + 0.1;

                while (zIterator.hasNext()) {
                    boolean intersected = false;
                    currentZColumn = zIterator.next();

                    double zDiff = (currentZColumn - prevZColumn);
                    z = prevZColumn + (Math.abs(zDiff)) / 2;
                    Line2D intersectingLine = new Line2D.Double(x1, z, x2, z);

                    rectPolygon[2] = prevZColumn;
                    rectPolygon[3] = currentZColumn;

                    //TODO - add a null check here.
                    HashMap<Integer, Line2D> lines = zLines.get(currentZColumn);
                    for (Integer xRow : xRowValues) {
                        if (lines.containsKey(xRow)) {
                            Line2D line = lines.get(xRow);
                            if (intersected && line.intersectsLine(intersectingLine)) {
                                rectPolygon[1] = xRow;
                                rectRegions.add(rectPolygon);

                                rectPolygon = new int[4];
                                rectPolygon[2] = prevZColumn;
                                rectPolygon[3] = currentZColumn;
                                intersected = false;
                            } else if (line.intersectsLine(intersectingLine)) {
                                rectPolygon[0] = xRow;
                                intersected = true;
                            }
                        }
                    }
                    prevZColumn = currentZColumn;
                }
            }
            return rectRegions;
        }
    }
}
