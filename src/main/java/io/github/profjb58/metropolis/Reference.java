package io.github.profjb58.metropolis;

import io.github.profjb58.metropolis.common.blocks.MarkerBlock;
import io.github.profjb58.metropolis.common.blocks.QuarryBlock;
import io.github.profjb58.metropolis.common.tileentity.Marker;
import io.github.profjb58.metropolis.common.tileentity.Quarry;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Metropolis.MOD_ID)
public class Reference {
    // ====BLOCKS====
    public static final MarkerBlock QUARTZ_MARKER = null;
    public static final MarkerBlock PRISMARINE_MARKER = null;
    public static final QuarryBlock QUARRY = null;

    //  ====TILE ENTITY TYPES====
    public static final TileEntityType<Marker> MARKER_TE = null;
    public static final TileEntityType<Quarry> QUARRY_TE = null;
}
