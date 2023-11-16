package tfar.colorfulhealthbar.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class Configs {
    public static final HealthConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<HealthConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(HealthConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public enum InfoLevel{NONE,BARS,ALL}

    static class HealthConfig {
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> healthColorValues;
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> absorptionColorValues;
        public static ForgeConfigSpec.EnumValue<InfoLevel> infoLevel;
        public static ForgeConfigSpec.DoubleValue textScale;
        public static ForgeConfigSpec.BooleanValue shouldLoop;

        HealthConfig(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            healthColorValues = builder
                    .comment("Colors must be specified in #RRGGBB format")
                    .translation("text.colorfulhealthbar.config.health")
                    .defineList("health color values", Lists.newArrayList("#FF1313", "#EE8100", "#E5CE00","#00DA00","#0C9DF1","#B486FF", "#EC8AFB","#FBD78B","#03EFEC","#B7E7FD","#EDEDED"), o -> o instanceof String);
            absorptionColorValues = builder
                    .comment("Colors must be specified in #RRGGBB format")
                    .translation("text.colorfulhealthbar.config.absorption")
                    .defineList("absorption color values", Lists.newArrayList("#2020FF","#FF1313","#13FF13","#FFFF13","#7713FF","#FF7713"), o -> o instanceof String);
            infoLevel = builder
                    .comment("Description level of text, all shows total health and absorption bars, bars shows number of bars, none doesn't show text")
                    .defineEnum("show bars",InfoLevel.ALL);
            textScale = builder
                    .comment("Size of index number")
                    .defineInRange("text size", .75,0,1);
            shouldLoop = builder.comment("Whether the heart colors should loop if the cap is reached")
                    .translation("text.colorfulhealthbar.config.should_loop")
                    .define("should loop",false);
            builder.pop();
        }
    }
    public static List<? extends String> healthColorValues = new ArrayList<>();
    public static List<? extends String> absorptionColorValues = new ArrayList<>();
    public static InfoLevel infoLevel = InfoLevel.ALL;
    public static double textScale = .75;
    public static boolean shouldloop;
    public static void bake(){
        healthColorValues = HealthConfig.healthColorValues.get();
        absorptionColorValues = HealthConfig.absorptionColorValues.get();
        infoLevel = HealthConfig.infoLevel.get();
        textScale = HealthConfig.textScale.get();
        shouldloop = HealthConfig.shouldLoop.get();
    }
}
