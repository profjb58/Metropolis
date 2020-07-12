package io.github.profjb58.metropolis.common.block;

import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.client.render.MarkerRegionRenderer;
import io.github.profjb58.metropolis.common.tileentity.MarkerTE;
import io.github.profjb58.metropolis.common.tileentity.QuarryTE;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.RedstoneParticle;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;


public class Marker extends TorchBlock {

    private RedstoneParticleData particle;

    public Marker(String name, int lightValue, RedstoneParticleData particle) {
        super(Properties.create(Material.WOOD)
                .sound(SoundType.WOOD)
                .hardnessAndResistance(0f)
                .lightValue(lightValue));
        setRegistryName(Metropolis.MOD_ID, name);
        this.particle = particle;
    }

    @Override
    public boolean hasTileEntity(BlockState state){
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world){
        return Reference.MARKER_TE.create();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if(!worldIn.isRemote){
            if(placer != null && placer instanceof PlayerEntity){
                PlayerEntity player = (PlayerEntity) placer;
                UUID uuid = player.getUniqueID();

                TileEntity tile = worldIn.getTileEntity(pos);
                if(tile != null && tile instanceof MarkerTE){
                    MarkerTE mte = (MarkerTE) tile;

                    mte.init(uuid);
                }
            }
        }
    }

    @Override
    public void onPlayerDestroy(IWorld worldIn, BlockPos pos, BlockState state) {
        super.onPlayerDestroy(worldIn, pos, state);

//        if(!worldIn.isRemote()){
//            World world = worldIn.getWorld();
//            world.notifyBlockUpdate(pos, state, state, 2);
//        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        double d0 = (double)pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
        double d1 = (double)pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D;
        double d2 = (double)pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
        worldIn.addParticle(particle, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }
}
