package io.github.profjb58.metropolis.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.common.util.DirectionHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LineRenderer {
    // Line Colours
    private static final float[] PRI_MARKER_COLOUR = {1.0f, 1.0f, 0.9f, 1.0f};
    private static final float[] QUARTZ_MARKER_COLOUR = {0.0f, 0.8f, 0.6f, 1.0f};

    static void drawTileToPlayer(TileEntity tile, ClientPlayerEntity player, MatrixStack matrixStack, float height){
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder lineBuilder = buffer.getBuffer(CustomRenderTypes.THICK_LINES);

        BlockPos playerPos = player.getPosition();
        BlockPos tilePos = tile.getPos();

        Direction facing = DirectionHelper.getDirectionBetween(playerPos, tilePos);

        int px = playerPos.getX();
        int py = playerPos.getY();
        int pz = playerPos.getZ();

        if(facing == Direction.NORTH){
            pz = pz + 1;
        } else if (facing == Direction.SOUTH){
            pz = pz -1;
        } else if (facing == Direction.EAST){
            px = px + 1;
        } else if (facing == Direction.WEST){
            px = px - 1;
        }

        float[] lineColour = getLineColour(tile.getBlockState().getBlock());
        drawLine(lineBuilder, generateProjectedMatrix(matrixStack),tilePos.getX() + 0.5f,tilePos.getY() + 0.5f,tilePos.getZ() + 0.5f, px + 0.5f, py + height, pz + 0.5f, lineColour);

        matrixStack.pop();
        buffer.finish(CustomRenderTypes.THICK_LINES);
    }

    public static Matrix4f generateProjectedMatrix(MatrixStack matrixStack){
        //  Begin pushing to the matrix stack.
        matrixStack.push();

        //  Get actual position of player and translate back to the actual location. E.g. blockpos is discrete integers. projected view isn't.
        Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        return matrixStack.getLast().getMatrix();
    }

    static float[] getLineColour(Block block){
        if(block.getBlock() == Reference.QUARTZ_MARKER){
            return PRI_MARKER_COLOUR;
        } else if(block.getBlock() == Reference.PRISMARINE_MARKER){
            return QUARTZ_MARKER_COLOUR;
        } else {
            return null;
        }
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
