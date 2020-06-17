package io.github.profjb58.metropolis.init;

import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.api.blocks.MBlocks;
import io.github.profjb58.metropolis.api.tileentities.MTileEntityTypes;
import io.github.profjb58.metropolis.common.block.Marker;
import io.github.profjb58.metropolis.common.block.Quarry;
import io.github.profjb58.metropolis.common.tileentity.MarkerTE;
import io.github.profjb58.metropolis.common.tileentity.QuarryTE;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryHandler {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event){
        event.getRegistry().registerAll(
                new Marker("quartz_marker", 11, new RedstoneParticleData(1.0f,1.0f,0.9f,1.0f)),
                new Marker("prismarine_marker", 15, new RedstoneParticleData(0.0f, 0.8f, 0.6f, 1.0f)),

                new Quarry("quarry")
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event){
        event.getRegistry().registerAll(
                new BlockItem(MBlocks.QUARTZ_MARKER, new Item.Properties().group(Metropolis.M_BASE_ITEM_GROUP)).setRegistryName(Metropolis.MOD_ID, "quartz_marker"),
                new BlockItem(MBlocks.PRISMARINE_MARKER, new Item.Properties().group(Metropolis.M_BASE_ITEM_GROUP)).setRegistryName(Metropolis.MOD_ID, "prismarine_marker"),
                new BlockItem(MBlocks.QUARRY, new Item.Properties().group(Metropolis.M_BASE_ITEM_GROUP)).setRegistryName(Metropolis.MOD_ID, "quarry")
        );
    }

    @SubscribeEvent
    public static void registerTE(RegistryEvent.Register<TileEntityType<?>> event){
        TileEntityType<MarkerTE> markerType = TileEntityType.Builder.create(() -> new MarkerTE() , MBlocks.PRISMARINE_MARKER, MBlocks.QUARTZ_MARKER).build(null);
        TileEntityType<QuarryTE> quarryType = TileEntityType.Builder.create(() -> new QuarryTE(), MBlocks.QUARRY).build(null);

        markerType.setRegistryName(Metropolis.MOD_ID, "marker_te");
        quarryType.setRegistryName(Metropolis.MOD_ID, "quarry_te");
        event.getRegistry().registerAll(
                markerType, quarryType
        );
    }
}
