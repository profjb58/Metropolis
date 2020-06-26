package io.github.profjb58.metropolis.config;

import io.github.profjb58.metropolis.Metropolis;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Reference;

@Mod.EventBusSubscriber(modid = Metropolis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    public Config(){
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "metropolis-common.toml");
    }

    public static class Common {
        public final IntValue quartz_marker_radius;
        public final IntValue prismarine_marker_radius;

        public Common(ForgeConfigSpec.Builder builder){
            builder.comment("Metropolis Mod Configuration").push("Metropolis");

            builder.comment("Markers").push("Markers");

            quartz_marker_radius = builder
                    .comment("Quartz marker radius")
                    .defineInRange("quartz_marker_radius", 16, 4, 128);

            prismarine_marker_radius = builder
                    .comment("Prismarine marker radius")
                    .defineInRange("prismarine_marker_radius", 32, 4, 128);
            builder.pop(2);
        }
    }

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;
    static {
        final Pair<Common, ForgeConfigSpec> specPair = new  ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading event){

    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading event){

    }
}
