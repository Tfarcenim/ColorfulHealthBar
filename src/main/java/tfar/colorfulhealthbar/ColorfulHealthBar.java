package tfar.colorfulhealthbar;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfar.colorfulhealthbar.config.Configs;
import tfar.colorfulhealthbar.overlay.HealthBarRenderer;

@Mod(value = ColorfulHealthBar.MODID)
public class ColorfulHealthBar {
  public static final String MODID = "colorfulhealthbar";

  public ColorfulHealthBar() {
    if (FMLEnvironment.dist.isClient()) {
      ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, ()->new IExtensionPoint.DisplayTest(   ()->"anything. i don't care", // if i'm actually on the server, this string is sent but i'm a client only mod, so it won't be
              (remoteversionstring,networkbool)->networkbool)); // i accept anything from the server, by returning true if it's asking about the server
      setup();
    } else {
      System.out.println("Why is this on the server?");
    }
  }

  public void bakeConfigs(ModConfigEvent event) {
      Configs.bake();
  }

  public void setup(){
    OverlayRegistry.registerOverlayBelow(ForgeIngameGui.PLAYER_HEALTH_ELEMENT,MODID, HealthBarRenderer.RENDERER);
    OverlayRegistry.enableOverlay(ForgeIngameGui.PLAYER_HEALTH_ELEMENT,false);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::bakeConfigs);
    ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, Configs.CLIENT_SPEC);
  }
}
