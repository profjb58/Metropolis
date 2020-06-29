package io.github.profjb58.metropolis;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.ParseResults;
import io.github.profjb58.metropolis.config.Config;
import io.github.profjb58.metropolis.init.MItemGroup;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.DeOpCommand;
import net.minecraft.command.impl.OpCommand;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.ServerLifecycleEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.CallbackI;

import java.io.File;

@Mod(Metropolis.MOD_ID)
public class Metropolis
{
    public static File config;
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "metropolis";
    public static final ItemGroup M_BASE_ITEM_GROUP = new MItemGroup("base");

    public static Metropolis instance;

    public Metropolis() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        instance = this;

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register configs.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "metropolis-common.toml");
    }

    //  Stuff that happens after blocks are loaded.
    private void setup(final FMLCommonSetupEvent event) {
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(Reference.PRISMARINE_MARKER, RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(Reference.QUARTZ_MARKER, RenderType.getCutoutMipped());
    }


}
