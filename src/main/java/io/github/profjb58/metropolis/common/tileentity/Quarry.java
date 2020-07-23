package io.github.profjb58.metropolis.common.tileentity;

import io.github.profjb58.metropolis.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class Quarry extends TileEntity implements ITickableTileEntity {

    boolean initialized = false;
    public int x,y,z, tick;

    public Quarry() {
        super(Reference.QUARRY_TE);
    }

    @Override
    public void tick() {
        if(!initialized) init();
        tick++;
        if(tick == 20){
            tick = 0;
            if(y > 2) execute();
        }
    }

    private void execute() {
        int index = 0;
        Block[] blocksRemoved = new Block[9];
        for(int x = 0; x < 3; x++) {
            for(int z = 0; z < 3; z++){
                BlockPos posToBreak = new BlockPos(this.x + x, this.y, this.z + z);
                blocksRemoved[index] = this.world.getBlockState(posToBreak).getBlock();
                destroyBlock(posToBreak, true, null);
                index++;
            }
        }
        this.y--;
    }

    private boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity){
        BlockState blockstate = world.getBlockState(pos);
        if(blockstate.isAir(world, pos)) return false;
        else{
            IFluidState fluid = world.getFluidState(pos);
            world.playEvent(2001,pos,Block.getStateId(blockstate));
            /*if(dropBlock){
                TileEntity tileentity = blockstate.hasTileEntity() ? world.getTileEntity(pos) : null;
                Block.spawnDrops(blockstate, world, this.pos.add(0,1.5,0), tileentity, entity, ItemStack.EMPTY);
            }*/
            return world.removeBlock(pos, false);
        }
    }

    private void init(){
        initialized = true;
        x = this.pos.getX() - 1;
        y = this.pos.getY() - 2;
        z = this.pos.getZ() - 1;
        tick = 0;
    }

    /*
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put("initvalues", NBTHelper.toNBT(this));
        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound){
        super.read(compound);
        CompoundNBT initvalues = compound.getCompound("initvalues");
        if(initvalues != null){
            this.x = initvalues.getInt("x");
            this.y = initvalues.getInt("y");
            this.z = initvalues.getInt("z");
            this.tick = 0;
            initialized = true;
            return;
        }
        init();
    }*/
}
