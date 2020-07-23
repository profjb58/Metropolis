package io.github.profjb58.metropolis.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.client.render.CustomRenderTypes;
import io.github.profjb58.metropolis.client.render.LineRenderer;
import io.github.profjb58.metropolis.common.tileentity.Marker;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;

public class MarkerTERenderer extends TileEntityRenderer<Marker> {

    // Line Colours
    private static final float[] PRI_MARKER_COLOUR = {1.0f, 1.0f, 0.9f, 1.0f};
    private static final float[] QUARTZ_MARKER_COLOUR = {0.0f, 0.8f, 0.6f, 1.0f};

    public MarkerTERenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(Marker tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {

        IVertexBuilder lineBuilder = buffer.getBuffer(CustomRenderTypes.THICK_LINES);

        BlockPos pos = tileEntityIn.getPos();
        BlockPos nextPos = tileEntityIn.getNextMarkerPos();
        BlockPos prevPos = tileEntityIn.getPrevMarkerPos();
        boolean isTail = tileEntityIn.isTail();
        boolean isHead = tileEntityIn.isHead();

        float[] lineColour = getLineColour(tileEntityIn.getBlockState().getBlock());
        if(nextPos != null){
            float zDiff = (nextPos.getZ() + 1.0f) - (pos.getZ() + 1.0f);
            float xDiff = (nextPos.getX() + 1.0f) - (pos.getX() + 1.0f);
            float yDiff = (nextPos.getY() + 1.2f) - (pos.getY() + 1.2f);
            drawLine(lineBuilder, LineRenderer.generateProjectedMatrix(matrixStackIn) ,pos.getX() + 1.0f,pos.getY() + 1.2f,pos.getZ() + 1.0f,
                    (nextPos.getX() + 1.0f) + xDiff,
                    (nextPos.getY() + 1.2f) + yDiff,
                    (nextPos.getZ() + 1.0f) + zDiff, lineColour);
            matrixStackIn.pop();
        }
        if(prevPos != null) {
            float zDiff = (prevPos.getZ() + 1.0f) - (pos.getZ() + 1.0f);
            float xDiff = (prevPos.getX() + 1.0f) - (pos.getX() + 1.0f);
            float yDiff = (prevPos.getY() + 1.2f) - (pos.getY() + 1.2f);
            drawLine(lineBuilder, LineRenderer.generateProjectedMatrix(matrixStackIn), pos.getX() + 1.0f, pos.getY() + 1.2f, pos.getZ() + 1.0f,
                    (prevPos.getX() + 1.0f) + xDiff,
                    (prevPos.getY() + 1.2f) + yDiff,
                    (prevPos.getZ() + 1.0f) + zDiff, lineColour);
            matrixStackIn.pop();
        }
    }

    private static float[] getLineColour(Block block){
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

    @Override
    public boolean isGlobalRenderer(Marker te) {
        return true;
    }
}
