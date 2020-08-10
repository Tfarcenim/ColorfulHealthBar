package tfar.colorfulhealthbar.overlay;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/*
    Class which handles the render event and hides the vanilla armor bar
 */
public class OverlayEventHandler
{
    public OverlayEventHandler(HealthBarRenderer healthBarRenderer)
    {
        this.healthBarRenderer = healthBarRenderer;
    }

    private HealthBarRenderer healthBarRenderer;

    @SubscribeEvent(receiveCanceled = true)
    public void onRenderGameOverlayEventPre(RenderGameOverlayEvent.Pre event)
    {
        Minecraft mc = Minecraft.getInstance();
        int scaledWidth = mc.getMainWindow().getScaledWidth();
        int scaledHeight = mc.getMainWindow().getScaledHeight();

        /* Don't render the vanilla health bar */
        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH) {
            healthBarRenderer.renderHealthBar(event.getMatrixStack(),scaledWidth, scaledHeight);
            event.setCanceled(true);
        }
    }
}