package io.github.profjb58.metropolis.client.render;

import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Renderer {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void renderLayers(final FMLClientSetupEvent event){
        //  CUTOUT-MIPPED
        RenderTypeLookup.setRenderLayer(Reference.PRISMARINE_MARKER, RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(Reference.QUARTZ_MARKER, RenderType.getCutoutMipped());
    }

}
