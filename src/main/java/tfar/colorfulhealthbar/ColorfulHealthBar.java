package tfar.colorfulhealthbar;

import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
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
      ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
              ()->new IExtensionPoint.DisplayTest(()->"anything. i don't care", // if i'm actually on the server, this string is sent but i'm a client only mod, so it won't be
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
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::bakeConfigs);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerBar);
    MinecraftForge.EVENT_BUS.addListener(this::disableVanillaBar);
    ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, Configs.CLIENT_SPEC);
  }

  public void registerBar(RegisterGuiOverlaysEvent e) {
    e.registerBelow(VanillaGuiOverlay.PLAYER_HEALTH.id(),MODID, HealthBarRenderer.RENDERER);
  }

  public void disableVanillaBar(RenderGuiOverlayEvent e) {
    if (e.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type()) {
      e.setCanceled(true);
    }
  }
}
