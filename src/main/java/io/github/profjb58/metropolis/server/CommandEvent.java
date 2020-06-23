package io.github.profjb58.metropolis.server;

import com.mojang.brigadier.ParseResults;
import io.github.profjb58.metropolis.Metropolis;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, value = Dist.DEDICATED_SERVER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandEvent {
    public static String[] OP_LIST = null;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void dedicatedServerCommand(final net.minecraftforge.event.CommandEvent event){
        if(FMLEnvironment.dist == Dist.DEDICATED_SERVER){
            ParseResults<CommandSource> parseResults = event.getParseResults();
            String cRaw = parseResults.getReader().getRead();
            String c = cRaw.replace("/", "");

            if(c.startsWith("op") || c.startsWith("deop") || c.startsWith("reload")){
                Metropolis.LOGGER.debug("Metropolis permissions updated");
                MinecraftServer ds = parseResults.getContext().getSource().getServer();
                OP_LIST = ds.getPlayerList().getOppedPlayerNames();
            }
        }
    }
}
