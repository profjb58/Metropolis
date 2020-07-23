package io.github.profjb58.metropolis;


import io.github.profjb58.metropolis.client.render.MarkerLineRenderer;
import io.github.profjb58.metropolis.client.render.tileentity.MarkerTERenderer;
import io.github.profjb58.metropolis.common.config.Config;
import io.github.profjb58.metropolis.common.init.MItemGroup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Metropolis.MOD_ID)
public class Metropolis
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "metropolis";

    public static final ItemGroup M_BASE_ITEM_GROUP = new MItemGroup("base");
    public static StringTextComponent metropolisTextHeader;
    public static boolean DEBUG_ENABLED = false;

    public static Metropolis instance;

    public Metropolis() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        instance = this;

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register configs.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "metropolis-common.toml");

        metropolisTextHeader = new StringTextComponent("[Metropolis]");
        metropolisTextHeader.getStyle().setColor(TextFormatting.AQUA);
    }

    //  Stuff that happens after blocks are loaded.
    private void setup(final FMLCommonSetupEvent event) {

    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(Reference.MARKER_TE, MarkerTERenderer::new);
        MarkerLineRenderer.initHeldItemsList();

        RenderTypeLookup.setRenderLayer(Reference.PRISMARINE_MARKER, RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(Reference.QUARTZ_MARKER, RenderType.getCutoutMipped());
    }


}
