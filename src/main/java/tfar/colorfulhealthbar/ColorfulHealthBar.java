package tfar.colorfulhealthbar;

import tfar.colorfulhealthbar.config.Configs;
import tfar.colorfulhealthbar.overlay.HealthBarRenderer;
import tfar.colorfulhealthbar.overlay.OverlayEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

@Mod(value = ColorfulHealthBar.MODID)
public class ColorfulHealthBar {
  public static final String MODID = "colorfulhealthbar";

  public ColorfulHealthBar() {
    DistExecutor.runWhenOn(Dist.CLIENT, () -> this::setup);
  }

  public void bakeConfigs(ModConfig.ModConfigEvent event) {
      Configs.bake();
  }

  public void setup(){
    MinecraftForge.EVENT_BUS.register(new OverlayEventHandler(new HealthBarRenderer(Minecraft.getInstance())));
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::bakeConfigs);
    ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, Configs.CLIENT_SPEC);
  }
}
