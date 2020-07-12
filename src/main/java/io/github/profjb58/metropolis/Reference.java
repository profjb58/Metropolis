package io.github.profjb58.metropolis;

import io.github.profjb58.metropolis.common.block.Marker;
import io.github.profjb58.metropolis.common.block.Quarry;
import io.github.profjb58.metropolis.common.tileentity.MarkerTE;
import io.github.profjb58.metropolis.common.tileentity.QuarryTE;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Metropolis.MOD_ID)
public class Reference {
    // ====BLOCKS====
    public static final Marker QUARTZ_MARKER = null;
    public static final Marker PRISMARINE_MARKER = null;
    public static final Quarry QUARRY = null;

    //  ====TILE ENTITY TYPES====
    public static final TileEntityType<MarkerTE> MARKER_TE = null;
    public static final TileEntityType<QuarryTE> QUARRY_TE = null;
}
