package io.github.profjb58.metropolis.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class CustomRenderTypes extends RenderType {

    //  Dummy
    public CustomRenderTypes(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    private static final LineState THICK_LINE = new LineState(OptionalDouble.of(4f));
    private static final LineState THIN_LINE = new LineState(OptionalDouble.of(1.5f));

    //  TODO - May need a lightmap, cull, and depth test.
    public static final RenderType THICK_LINES = makeType("thick_lines",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.getBuilder().line(THICK_LINE)
                    .layer(PROJECTION_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .writeMask(COLOR_WRITE)
                    .texture(NO_TEXTURE)
                    .cull(CULL_DISABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    //.depthTest(DEPTH_ALWAYS)  Might change this later
                    .build(false));
}
