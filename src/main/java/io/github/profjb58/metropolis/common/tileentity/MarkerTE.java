package io.github.profjb58.metropolis.common.tileentity;

import io.github.profjb58.metropolis.api.tileentities.MTileEntityTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class MarkerTE extends TileEntity {

    public MarkerTE() {
        super(MTileEntityTypes.MARKER_TE);
    }

}
