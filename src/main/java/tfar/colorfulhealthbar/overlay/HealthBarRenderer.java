package tfar.colorfulhealthbar.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import tfar.colorfulhealthbar.ColorfulHealthBar;
import tfar.colorfulhealthbar.config.Configs;
import tfar.colorfulhealthbar.config.Configs.*;

import javax.annotation.Nullable;
import java.util.Random;

import static tfar.colorfulhealthbar.config.Configs.*;

/*
    Class handles the drawing of the health bar
 */

public class HealthBarRenderer implements IIngameOverlay {
  private static int updateCounter = 0;
  private int playerHealth = 0;
  private int lastPlayerHealth = 0;
  private long healthUpdateCounter = 0;
  private long lastSystemTime = 0;
  private final Random rand = new Random();
  private Icon[] healthIcons;
  private Icon[] absorbIcons;

  private static final ResourceLocation ICON_HEARTS = new ResourceLocation(ColorfulHealthBar.MODID, "textures/gui/health.png");
  private static final ResourceLocation ICON_ABSORPTION = new ResourceLocation(ColorfulHealthBar.MODID, "textures/gui/absorption.png");
  private static final ResourceLocation ICON_VANILLA = Gui.GUI_ICONS_LOCATION;

  private static final float PASS_ONE_ALPHA = 1.0F;
  private static final float PASS_TWO_ALPHA = 0.2647F;// 0.2645 - 0.2649 needs tweaking too much red, too little green/blue
  private static final float PASS_THREE_ALPHA = 0.769F;//exact
  private static final float PASS_FOUR_ALPHA = 0.63F;//< 0.66
  private static final float POTION_ALPHA = 0.85F;
  private static final float PASS_SIX_ALPHA = 0.20F;//< 0.66


  private boolean forceUpdateIcons = false;

  public void drawTexturedModalRect(PoseStack stack,int x, int y, int textureX, int textureY, int width, int height) {
    Minecraft.getInstance().gui.blit(stack,x, y, textureX, textureY, width, height);
  }

  public static final HealthBarRenderer RENDERER = new HealthBarRenderer();

  public HealthBarRenderer() {
  }

  public void forceUpdate() {
    forceUpdateIcons = true;
  }

  public void render(ForgeIngameGui gui, PoseStack stack, float f, int screenWidth, int screenHeight) {
    //Push to avoid lasting changes
    stack.pushPose();
    RenderSystem.enableBlend();

    updateCounter = gui.getGuiTicks();//get update counter

    Player player = (Player) Minecraft.getInstance().getCameraEntity();
    int health = Mth.ceil(player.getHealth());
    boolean highlight = healthUpdateCounter > (long) updateCounter && (healthUpdateCounter - (long) updateCounter) / 3L % 2L == 1L;

    if (health < playerHealth && player.invulnerableTime > 0) {
      lastSystemTime = System.currentTimeMillis();
      healthUpdateCounter = updateCounter + 20;
    } else if (health > playerHealth && player.invulnerableTime > 0) {
      lastSystemTime = System.currentTimeMillis();
      healthUpdateCounter = updateCounter + 10;
    }

    if (System.currentTimeMillis() - lastSystemTime > 1000L) {
      playerHealth = health;
      lastPlayerHealth = health;
      lastSystemTime = System.currentTimeMillis();
    }
    int absorb = Mth.ceil(player.getAbsorptionAmount());
    if (health != playerHealth || absorbIcons == null || healthIcons == null || forceUpdateIcons) {
      healthIcons = IconStateCalculator.calculateIcons(health, Configs.healthColorValues);
      absorbIcons = IconStateCalculator.calculateIcons(absorb, Configs.absorptionColorValues);
      forceUpdateIcons = false;
    }

    playerHealth = health;
    int j = lastPlayerHealth;
    rand.setSeed(updateCounter * 312871L);
    AttributeInstance maxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
    int xStart = screenWidth / 2 - 91;
    int yStart = screenHeight - 39;
    double maxHealth = maxHealthAttribute.getValue();
    int numberOfHealthBars = Mth.ceil((maxHealth + absorb) / 20.0F);
    int i2 = Math.max(10 - (numberOfHealthBars - 2), 3);
    int regen = -1;

    if (player.hasEffect(MobEffects.REGENERATION))
      regen = updateCounter % Mth.ceil(maxHealth + 5.0F);

    Minecraft.getInstance().getProfiler().push("health");

    for (int i = 9; i >= 0; --i) {
      healthIcons = IconStateCalculator.calculateIcons(health, Configs.healthColorValues
      );
      Icon icon = healthIcons[i];
      IconColor firstHalfColor = icon.primaryIconColor;
      IconColor secondHalfColor = icon.secondaryIconColor;

      int k5 = 16;

      if (player.hasEffect(MobEffects.POISON)) k5 += 36;
      else if (player.hasEffect(MobEffects.WITHER)) k5 += 72;

      int i4 = (highlight) ? 1 : 0;

      int j4 = Mth.ceil((float) (i + 1) / 10.0F) - 1;
      int xPosition = xStart + i % 10 * 8;
      int yPosition = yStart - j4 * i2;

      if (health <= 4) yPosition += rand.nextInt(2);

      if (absorb <= 0 && i == regen) yPosition -= 2;

      int i5 = (player.level.getLevelData().isHardcore()) ? 5 : 0;

      //Heart background
      drawTexturedModalRect(stack, xPosition, yPosition, 16 + i4 * 9, 9 * i5, 9, 9);

      if (highlight) {
        if (i * 2 + 1 < j) {
          //Draw full highlighted heart
          drawTexturedModalRect(stack, xPosition, yPosition, k5 + 54, 9 * i5, 9, 9);
        }

        if (i * 2 + 1 == j) {
          //Draw half highlighted heart
          drawTexturedModalRect(stack, xPosition, yPosition, k5 + 63, 9 * i5, 9, 9);
        }
      }

      //if (i * 2 + 1 < health)
      if (icon.iconType == Icon.Type.FULL) {
        //Draw full heart

        //Bind our custom texture
        bind(ICON_HEARTS);

        //Draw tinted white heart
        setColor(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_ONE_ALPHA);
        drawTexturedModalRect(stack, xPosition, yPosition, 0, 0, 9, 9);

        //Second pass dark highlights
        setColor(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_TWO_ALPHA);
        drawTexturedModalRect(stack, xPosition, yPosition, 0, 9, 9, 9);

        if (i5 == 5) {
          setColor(1, 1, 1, PASS_FOUR_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 0, 18, 9, 9);
        } else {
          setColor(1.0F, 1.0F, 1.0F, PASS_THREE_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 27, 0, 9, 9);
        }

        //Reset back to normal settings
        bind(ICON_VANILLA);
        if (k5 != 16) potionEffects(stack, xPosition, yPosition, k5, i, health);
        setColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      //if (i * 2 + 1 == health)
      if (icon.iconType == Icon.Type.HALF) {
        //Draw Half Heart

        if (health > 20) {
          //We have wrapped, Draw both parts of the heart seperately

          //Bind our custom texture
          bind(ICON_HEARTS);

          //Draw first half of tinted white heart
          setColor(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_ONE_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 9, 0, 9, 9);

          //Second pass dark highlights
          setColor(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_TWO_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 9, 9, 9, 9);

          if (i5 == 5) {
            setColor(1, 1, 1, PASS_FOUR_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition, 0, 18, 9, 9);
          } else {
            setColor(1.0F, 1.0F, 1.0F, PASS_THREE_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition, 27, 0, 9, 9);
          }

          //Draw second half of tinted white heart
          setColor(secondHalfColor.Red, secondHalfColor.Green, secondHalfColor.Blue, PASS_ONE_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 18, 0, 9, 9);

          //Second pass dark highlights
          setColor(secondHalfColor.Red, secondHalfColor.Green, secondHalfColor.Blue, PASS_TWO_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 18, 9, 9, 9);

          if (i5 == 5) {
            setColor(1, 1, 1, PASS_FOUR_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition, 0, 18, 9, 9);
          } else {
            setColor(1.0F, 1.0F, 1.0F, PASS_THREE_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition, 27, 0, 9, 9);
          }
        } else {
          //Draw only first half of heart

          //Bind our custom texture
          bind(ICON_HEARTS);

          //Draw tinted white heart
          setColor(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_ONE_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 9, 0, 9, 9);

          //Second pass dark highlights
          setColor(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_TWO_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 9, 9, 9, 9);

          if (i5 == 5) {
            setColor(1, 1, 1, PASS_FOUR_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition, 9, 18, 9, 9);
          } else {
            setColor(1.0F, 1.0F, 1.0F, PASS_THREE_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition, 27, 0, 9, 9);
          }
        }

        //Reset back to normal settings
        bind(ICON_VANILLA);
        if (k5 != 16) potionEffects(stack, xPosition, yPosition, k5, i, health);
        setColor(1.0F, 1.0F, 1.0F, 1.0F);

      }
    }
    if (absorb > 0) {
      for (int i = 9; i >= 0; i--) {
        if ((absorb / 2) < i) continue;
        int absorbCap = absorb % 20;
        int offset = 10;
        Icon icon2 = absorbIcons[i];
        absorbIcons = IconStateCalculator.calculateIcons(absorb, Configs.absorptionColorValues);
        IconColor firstHalfColor = icon2.primaryIconColor;
        IconColor secondHalfColor = icon2.secondaryIconColor;

        int k5 = 16;

        if (player.hasEffect(MobEffects.POISON)) k5 += 36;
        else if (player.hasEffect(MobEffects.WITHER)) k5 += 72;

        int i4 = (highlight) ? 1 : 0;

        int j4 = Mth.ceil((float) (i + 1) / 10.0F) - 1;
        int xPosition = xStart + i % 10 * 8;
        int yPosition = yStart - j4 * i2;

        //if (health <= 4) yPosition += .rand.nextInt(2);

        int i5 = (player.level.getLevelData().isHardcore()) ? 5 : 0;

        //Heart background
        //drawTexturedModalRect(xPosition, yPosition-offset, 16 + i4 * 9, 9 * i5, 9, 9);

        //if (i * 2 + 1 < absorb)
        if (i * 2 + 1 < absorb) {
          //Draw full heart

          //Bind our custom texture
          bind(ICON_ABSORPTION);

          //Draw tinted white absorption heart
          setColor(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_ONE_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition - offset, 0, 0, 9, 9);

          //Second pass dark highlights
          setColor(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_TWO_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition - offset, 0, 9, 9, 9);

          //Third pass dot highlight
          setColor(1.0F, 1.0F, 1.0F, PASS_SIX_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition - offset, 27, 0, 9, 9);

          //Reset back to normal settings
          setColor(1.0F, 1.0F, 1.0F, 1.0F);
          bind(ICON_VANILLA);
        }
        //if (i * 2 + 1 == absorb)
        if (i * 2 + 1 == absorbCap) {
          //Draw Half Heart
          if (absorb > 20) {
            //We have wrapped, Draw both parts of the heart separately
            //Bind our custom texture
            bind(ICON_ABSORPTION);

            //Draw first half of tinted white heart
            setColor(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_ONE_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 9, 0, 9, 9);

            //Second pass dark highlights
            setColor(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_TWO_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 9, 9, 9, 9);

            //Third pass dot highlight
            setColor(1.0F, 1.0F, 1.0F, PASS_SIX_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 27, 0, 9, 9);

            //Draw second half of tinted white heart
            setColor(secondHalfColor.Red, secondHalfColor.Green, secondHalfColor.Blue, PASS_ONE_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 18, 0, 9, 9);

            //Second pass dark highlights
            setColor(secondHalfColor.Red, secondHalfColor.Green, secondHalfColor.Blue, PASS_TWO_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 18, 9, 9, 9);
          } else {
            //Draw only first half of heart

            //Bind our custom texture
            bind(ICON_ABSORPTION);

            //Draw tinted white heart
            setColor(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_ONE_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 9, 0, 9, 9);

            //Second pass dark highlights
            setColor(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_TWO_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 9, 9, 9, 9);

            //third pass dot highlight
            setColor(1.0F, 1.0F, 1.0F, PASS_SIX_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 27, 0, 9, 9);
          }

          //Reset back to normal settings
          setColor(1.0F, 1.0F, 1.0F, 1.0F);

          bind(ICON_VANILLA);
        }
      }
    }

    gui.left_height += 10;
    if (absorb > 0) {
      gui.left_height += 10;
    }

    RenderSystem.disableBlend();

    //Revert our state back
    //Revert our state back
    stack.scale((float) textScale, (float) textScale, 1);
    int index = (int) Math.max(Math.ceil(health / 20f), 1);
    String[] info = getInfo(health, (int) maxHealth, absorb);
    if (info != null) {
      int textOffset = gui.getFont().width(info[0]);
      drawStringOnHUD(gui,stack, info[0], xStart - textOffset + 3, yStart, Integer.decode(healthColorValues.get(Math.min(index - 1, healthColorValues.size() - 1))), textScale);
      if (absorb > 0) {
        textOffset = gui.getFont().width(info[1]);
        drawStringOnHUD(gui,stack, info[1], xStart - textOffset - 1, yStart - 10, Integer.decode(absorptionColorValues.get(Math.min((int) Math.ceil(absorb / 20d) - 1, absorptionColorValues.size() - 1))), textScale);
      }
    }
    setColor(1, 1, 1, 1);
    stack.scale(1, 1, 1);
    bind(ICON_VANILLA);
    if (absorb > 0) {
      gui.left_height += 10;
    }
    stack.popPose();
    Minecraft.getInstance().getProfiler().pop();
  }

  private static void setColor(float r,float g,float b,float a) {
    RenderSystem.setShaderColor(r, g, b, a);
  }
  
  private static void bind(ResourceLocation texture) {
    RenderSystem.setShaderTexture(0,texture);
  }

  public void potionEffects(PoseStack stack,int x, int y, int k5, int i, int health) {
    if (k5 == 52) {
      if (i * 2 + 1 != health || health >= 20) {
        setColor(1.0F, 1.0F, 1.0F, POTION_ALPHA);
        drawTexturedModalRect(stack, x, y, 88, 0, 9, 9);
      } else {
        setColor(1.0F, 1.0F, 1.0F, POTION_ALPHA);
        drawTexturedModalRect(stack, x, y, 97, 0, 9, 9);
      }
    }
    if (k5 == 88) {
      if (i * 2 + 1 != health || health >= 20) {
        setColor(1.0F, 1.0F, 1.0F, POTION_ALPHA);
        drawTexturedModalRect(stack, x, y, 124, 0, 9, 9);
      } else {
        setColor(1.0F, 1.0F, 1.0F, POTION_ALPHA);
        drawTexturedModalRect(stack, x, y, 133, 0, 9, 9);
      }
    }
  }

  @Nullable
  public String[] getInfo(int health, int maxHealth, int absorption) {
    switch (infoLevel) {
      case NONE:
        return null;
      case BARS:
        return new String[]{(int) Math.max(Math.ceil(health / 20d), 1) + "x", (int) Math.max(Math.ceil(absorption / 20d), 1) + "x"};
      case ALL:
      default:
        return new String[]{health/2 + "/" + maxHealth/2, String.valueOf(absorption / 2)};
    }
  }

  public void drawStringOnHUD(ForgeIngameGui gui, PoseStack stack,String string, int xOffset, int yOffset, int color, double scale) {
    if (infoLevel == InfoLevel.NONE) return;
    yOffset += 9 * (1 - scale);
    xOffset += 9 * (1 - scale);
    gui.getFont().drawShadow(stack,string, xOffset / (float) scale, yOffset / (float) scale, color);
  }
}