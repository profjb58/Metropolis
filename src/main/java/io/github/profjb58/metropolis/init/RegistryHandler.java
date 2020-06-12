package io.github.profjb58.metropolis.init;

import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.api.blocks.MBlocks;
import io.github.profjb58.metropolis.common.block.Marker;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryHandler {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event){
        event.getRegistry().registerAll(
                new Marker("quartz_marker", 11, new RedstoneParticleData(1.0f,1.0f,0.9f,1.0f)),
                new Marker("prismarine_marker", 15, new RedstoneParticleData(0.0f, 0.8f, 0.6f, 1.0f))
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event){
        event.getRegistry().registerAll(
                new BlockItem(MBlocks.QUARTZ_MARKER, new Item.Properties().group(Metropolis.M_BASE_ITEM_GROUP)).setRegistryName(Metropolis.MOD_ID, "quartz_marker"),
                new BlockItem(MBlocks.PRISMARINE_MARKER, new Item.Properties().group(Metropolis.M_BASE_ITEM_GROUP)).setRegistryName(Metropolis.MOD_ID, "prismarine_marker")
        );
    }
}
