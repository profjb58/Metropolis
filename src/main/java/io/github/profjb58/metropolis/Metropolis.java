package io.github.profjb58.metropolis;


import io.github.profjb58.metropolis.api.blocks.MBlocks;
import io.github.profjb58.metropolis.init.MItemGroup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Metropolis.MOD_ID)
public class Metropolis
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "metropolis";
    public static final ItemGroup M_BASE_ITEM_GROUP = new MItemGroup("base");
    public static Metropolis instance;

    public Metropolis() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        instance = this;

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    //  Stuff that happens after blocks are loaded.
    private void setup(final FMLCommonSetupEvent event) {
    }

    private void clientSetup(final FMLClientSetupEvent event) { }
}
