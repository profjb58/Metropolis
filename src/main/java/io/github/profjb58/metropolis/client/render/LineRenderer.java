package io.github.profjb58.metropolis.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.sun.media.jfxmedia.logging.Logger;
import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.common.tileentity.MarkerTE;
import io.github.profjb58.metropolis.common.tileentity.QuarryTE;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


public class LineRenderer {

    // Line Colours
    private static final float[] PRI_COLOUR = {1.0f, 1.0f, 0.9f, 1.0f};
    private static final float[] QUARTZ_COLOUR = {0.0f, 0.8f, 0.6f, 1.0f};

    private static final int updateFrequency = 5;
    private static int updateCounter = 0;

    static void drawTileToPlayer(BlockPos tilePos, ClientPlayerEntity player, MatrixStack matrixStack, int radius){
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder = buffer.getBuffer(CustomRenderTypes.MARKER_LINES);

        BlockPos playerPos = player.getPosition();
        int px = playerPos.getX();
        int py = playerPos.getY();
        int pz = playerPos.getZ();
        World world = player.getEntityWorld();

        //  Begin pushing to the matrix stack.
        matrixStack.push();

        //  Get actual position of player and translate back to the actual location. E.g. blockpos is discrete integers. projected view isn't.
        Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        Matrix4f positionMatrix = matrixStack.getLast().getMatrix();

    }

    static void locateTiles(Block tileBlock, ClientPlayerEntity player, MatrixStack matrixStack, int radius){
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder = buffer.getBuffer(CustomRenderTypes.MARKER_LINES);

        BlockPos playerPos = player.getPosition();
        int px = playerPos.getX();
        int py = playerPos.getY();
        int pz = playerPos.getZ();
        World world = player.getEntityWorld();

        //  Begin pushing to the matrix stack.
        matrixStack.push();

        //  Get actual position of player and translate back to the actual location. E.g. blockpos is discrete integers. projected view isn't.
        Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        Matrix4f positionMatrix = matrixStack.getLast().getMatrix();

        BlockPos.Mutable pos = new BlockPos.Mutable(); // Not a new object, can't store it.
        for(int dx = -radius; dx <= radius; dx++){
            for(int dy = -radius; dy <= radius; dy++){
                for(int dz = -radius; dz <= radius; dz++){
                    pos.setPos(px + dx, py + dy, pz + dz);
                    if(world.getTileEntity(pos) != null){
                        if(world.getTileEntity(pos) instanceof MarkerTE && tileBlock == Reference.PRISMARINE_MARKER){
                            drawLine(builder, positionMatrix,pos.getX() + 0.5f,pos.getY() + 0.6f,pos.getZ() + 0.5f, px + 0.5f, py + 0.5f, pz + 0.5f, QUARTZ_COLOUR);
                        } else if (world.getTileEntity(pos) instanceof MarkerTE && tileBlock == Reference.QUARTZ_MARKER){
                            drawLine(builder, positionMatrix,pos.getX() + 0.5f,pos.getY() + 0.6f,pos.getZ() + 0.5f, px + 0.5f, py + 0.5f, pz + 0.5f, PRI_COLOUR);
                        } else if (world.getTileEntity(pos) instanceof QuarryTE){
                            //TODO - Quarry.
                        }
                    }
                }
            }
        }
        matrixStack.pop();
        buffer.finish(CustomRenderTypes.MARKER_LINES);

    }

    private static void drawLine(IVertexBuilder builder, Matrix4f posMatrix, float x1, float y1, float z1, float x2, float y2, float z2, float[] colour){
        builder.pos(posMatrix, x1, y1, z1)
                .color(colour[0], colour[1], colour[2], colour[3])
                .endVertex();
        builder.pos(posMatrix, x2, y2, z2)
                .color(colour[0], colour[1], colour[2], colour[3])
                .endVertex();
    }
}
