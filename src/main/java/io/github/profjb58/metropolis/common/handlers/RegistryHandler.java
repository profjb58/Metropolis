package io.github.profjb58.metropolis.common.handlers;

import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import io.github.profjb58.metropolis.common.blocks.MarkerBlock;
import io.github.profjb58.metropolis.common.blocks.QuarryBlock;
import io.github.profjb58.metropolis.common.tileentity.Marker;
import io.github.profjb58.metropolis.common.tileentity.Quarry;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryHandler {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event){
        event.getRegistry().registerAll(
                new MarkerBlock("quartz_marker", 11, new RedstoneParticleData(1.0f,1.0f,0.9f,1.0f)),
                new MarkerBlock("prismarine_marker", 15, new RedstoneParticleData(0.0f, 0.8f, 0.6f, 1.0f)),

                new QuarryBlock("quarry")
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event){
        event.getRegistry().registerAll(
                new BlockItem(Reference.QUARTZ_MARKER, new Item.Properties().group(Metropolis.M_BASE_ITEM_GROUP)).setRegistryName(Metropolis.MOD_ID, "quartz_marker"),
                new BlockItem(Reference.PRISMARINE_MARKER, new Item.Properties().group(Metropolis.M_BASE_ITEM_GROUP)).setRegistryName(Metropolis.MOD_ID, "prismarine_marker"),
                new BlockItem(Reference.QUARRY, new Item.Properties().group(Metropolis.M_BASE_ITEM_GROUP)).setRegistryName(Metropolis.MOD_ID, "quarry")
        );
    }

    @SubscribeEvent
    public static void registerTE(RegistryEvent.Register<TileEntityType<?>> event){
        TileEntityType<Marker> markerType = TileEntityType.Builder.create(Marker::new, Reference.PRISMARINE_MARKER, Reference.QUARTZ_MARKER).build(null);
        TileEntityType<Quarry> quarryType = TileEntityType.Builder.create(Quarry::new, Reference.QUARRY).build(null);

        markerType.setRegistryName(Metropolis.MOD_ID, "marker_te");
        quarryType.setRegistryName(Metropolis.MOD_ID, "quarry_te");
        event.getRegistry().registerAll(
                markerType, quarryType
        );
    }
}
