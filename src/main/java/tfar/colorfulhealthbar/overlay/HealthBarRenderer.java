package tfar.colorfulhealthbar.overlay;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import tfar.colorfulhealthbar.ColorfulHealthBar;
import tfar.colorfulhealthbar.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.gui.ForgeIngameGui;

import javax.annotation.Nullable;
import java.util.Random;

import static tfar.colorfulhealthbar.config.Configs.*;

/*
    Class handles the drawing of the health bar
 */

public class HealthBarRenderer {
  private Minecraft mc;

  private int updateCounter = 0;
  private int playerHealth = 0;
  private int lastPlayerHealth = 0;
  private long healthUpdateCounter = 0;
  private long lastSystemTime = 0;
  private Random rand = new Random();
  private Icon[] healthIcons;
  private Icon[] absorbIcons;

  private static final ResourceLocation ICON_HEARTS = new ResourceLocation(ColorfulHealthBar.MODID, "textures/gui/health.png");
  private static final ResourceLocation ICON_ABSORPTION = new ResourceLocation(ColorfulHealthBar.MODID, "textures/gui/absorption.png");
  private static final ResourceLocation ICON_VANILLA = IngameGui.GUI_ICONS_LOCATION;

  private static final float PASS_ONE_ALPHA = 1.0F;
  private static final float PASS_TWO_ALPHA = 0.2647F;// 0.2645 - 0.2649 needs tweaking too much red, too little green/blue
  private static final float PASS_THREE_ALPHA = 0.769F;//exact
  private static final float PASS_FOUR_ALPHA = 0.63F;//< 0.66
  private static final float POTION_ALPHA = 0.85F;
  private static final float PASS_SIX_ALPHA = 0.20F;//< 0.66


  private boolean forceUpdateIcons = false;

  public void drawTexturedModalRect(MatrixStack stack,int x, int y, int textureX, int textureY, int width, int height) {
    Minecraft.getInstance().ingameGUI.blit(stack,x, y, textureX, textureY, width, height);
  }

  public HealthBarRenderer(Minecraft mc) {
    this.mc = mc;
  }

  public void forceUpdate() {
    forceUpdateIcons = true;
  }

  public void renderHealthBar(MatrixStack stack,int screenWidth, int screenHeight) {
    //Push to avoid lasting changes
    RenderSystem.pushMatrix();
    RenderSystem.enableBlend();

    updateCounter = mc.ingameGUI.getTicks();//get update counter

    PlayerEntity player = (PlayerEntity) mc.getRenderViewEntity();
    int health = MathHelper.ceil(player.getHealth());
    boolean highlight = healthUpdateCounter > (long) updateCounter && (healthUpdateCounter - (long) updateCounter) / 3L % 2L == 1L;

    if (health < playerHealth && player.hurtResistantTime > 0) {
      lastSystemTime = System.currentTimeMillis();
      healthUpdateCounter = (long) (updateCounter + 20);
    } else if (health > playerHealth && player.hurtResistantTime > 0) {
      lastSystemTime = System.currentTimeMillis();
      healthUpdateCounter = (long) (updateCounter + 10);
    }

    if (System.currentTimeMillis() - lastSystemTime > 1000L) {
      playerHealth = health;
      lastPlayerHealth = health;
      lastSystemTime = System.currentTimeMillis();
    }
    int absorb = MathHelper.ceil(player.getAbsorptionAmount());
    if (health != playerHealth || absorbIcons == null || healthIcons == null || forceUpdateIcons) {
      healthIcons = IconStateCalculator.calculateIcons(health, Configs.healthColorValues);
      absorbIcons = IconStateCalculator.calculateIcons(absorb, Configs.absorptionColorValues);
      forceUpdateIcons = false;
    }

    playerHealth = health;
    int j = lastPlayerHealth;
    rand.setSeed((long) (updateCounter * 312871));
    ModifiableAttributeInstance maxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
    int xStart = screenWidth / 2 - 91;
    int yStart = screenHeight - 39;
    double maxHealth = maxHealthAttribute.getValue();
    int numberOfHealthBars = MathHelper.ceil((maxHealth + absorb) / 20.0F);
    int i2 = Math.max(10 - (numberOfHealthBars - 2), 3);
    int regen = -1;

    if (player.isPotionActive(Effects.REGENERATION))
      regen = updateCounter % MathHelper.ceil(maxHealth + 5.0F);

    mc.getProfiler().startSection("health");

    for (int i = 9; i >= 0; --i) {
      healthIcons = IconStateCalculator.calculateIcons(health, Configs.healthColorValues
      );
      Icon icon = healthIcons[i];
      IconColor firstHalfColor = icon.primaryIconColor;
      IconColor secondHalfColor = icon.secondaryIconColor;

      int k5 = 16;

      if (player.isPotionActive(Effects.POISON)) k5 += 36;
      else if (player.isPotionActive(Effects.WITHER)) k5 += 72;

      int i4 = (highlight) ? 1 : 0;

      int j4 = MathHelper.ceil((float) (i + 1) / 10.0F) - 1;
      int xPosition = xStart + i % 10 * 8;
      int yPosition = yStart - j4 * i2;

      if (health <= 4) yPosition += rand.nextInt(2);

      if (absorb <= 0 && i == regen) yPosition -= 2;

      int i5 = (player.world.getWorldInfo().isHardcore()) ? 5 : 0;

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
        mc.getTextureManager().bindTexture(ICON_HEARTS);

        //Draw tinted white heart
        RenderSystem.color4f(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_ONE_ALPHA);
        drawTexturedModalRect(stack, xPosition, yPosition, 0, 0, 9, 9);

        //Second pass dark highlights
        RenderSystem.color4f(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_TWO_ALPHA);
        drawTexturedModalRect(stack, xPosition, yPosition, 0, 9, 9, 9);

        if (i5 == 5) {
          RenderSystem.color4f(1, 1, 1, PASS_FOUR_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 0, 18, 9, 9);
        } else {
          RenderSystem.color4f(1.0F, 1.0F, 1.0F, PASS_THREE_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 27, 0, 9, 9);
        }

        //Reset back to normal settings
        mc.getTextureManager().bindTexture(ICON_VANILLA);
        if (k5 != 16) potionEffects(stack, xPosition, yPosition, k5, i, health);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      }

      //if (i * 2 + 1 == health)
      if (icon.iconType == Icon.Type.HALF) {
        //Draw Half Heart

        if (health > 20) {
          //We have wrapped, Draw both parts of the heart seperately

          //Bind our custom texture
          mc.getTextureManager().bindTexture(ICON_HEARTS);

          //Draw first half of tinted white heart
          RenderSystem.color4f(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_ONE_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 9, 0, 9, 9);

          //Second pass dark highlights
          RenderSystem.color4f(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_TWO_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 9, 9, 9, 9);

          if (i5 == 5) {
            RenderSystem.color4f(1, 1, 1, PASS_FOUR_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition, 0, 18, 9, 9);
          } else {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, PASS_THREE_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition, 27, 0, 9, 9);
          }

          //Draw second half of tinted white heart
          RenderSystem.color4f(secondHalfColor.Red, secondHalfColor.Green, secondHalfColor.Blue, PASS_ONE_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 18, 0, 9, 9);

          //Second pass dark highlights
          RenderSystem.color4f(secondHalfColor.Red, secondHalfColor.Green, secondHalfColor.Blue, PASS_TWO_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 18, 9, 9, 9);

          if (i5 == 5) {
            RenderSystem.color4f(1, 1, 1, PASS_FOUR_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition, 0, 18, 9, 9);
          } else {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, PASS_THREE_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition, 27, 0, 9, 9);
          }
        } else {
          //Draw only first half of heart

          //Bind our custom texture
          mc.getTextureManager().bindTexture(ICON_HEARTS);

          //Draw tinted white heart
          RenderSystem.color4f(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_ONE_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 9, 0, 9, 9);

          //Second pass dark highlights
          RenderSystem.color4f(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_TWO_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition, 9, 9, 9, 9);

          if (i5 == 5) {
            RenderSystem.color4f(1, 1, 1, PASS_FOUR_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition, 9, 18, 9, 9);
          } else {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, PASS_THREE_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition, 27, 0, 9, 9);
          }
        }

        //Reset back to normal settings
        mc.getTextureManager().bindTexture(ICON_VANILLA);
        if (k5 != 16) potionEffects(stack, xPosition, yPosition, k5, i, health);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

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

        if (player.isPotionActive(Effects.POISON)) k5 += 36;
        else if (player.isPotionActive(Effects.WITHER)) k5 += 72;

        int i4 = (highlight) ? 1 : 0;

        int j4 = MathHelper.ceil((float) (i + 1) / 10.0F) - 1;
        int xPosition = xStart + i % 10 * 8;
        int yPosition = yStart - j4 * i2;

        //if (health <= 4) yPosition += .rand.nextInt(2);

        int i5 = (player.world.getWorldInfo().isHardcore()) ? 5 : 0;

        //Heart background
        //drawTexturedModalRect(xPosition, yPosition-offset, 16 + i4 * 9, 9 * i5, 9, 9);

        //if (i * 2 + 1 < absorb)
        if (i * 2 + 1 < absorb) {
          //Draw full heart

          //Bind our custom texture
          mc.getTextureManager().bindTexture(ICON_ABSORPTION);

          //Draw tinted white absorption heart
          RenderSystem.color4f(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_ONE_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition - offset, 0, 0, 9, 9);

          //Second pass dark highlights
          RenderSystem.color4f(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_TWO_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition - offset, 0, 9, 9, 9);

          //Third pass dot highlight
          RenderSystem.color4f(1.0F, 1.0F, 1.0F, PASS_SIX_ALPHA);
          drawTexturedModalRect(stack, xPosition, yPosition - offset, 27, 0, 9, 9);

          //Reset back to normal settings
          RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
          mc.getTextureManager().bindTexture(ICON_VANILLA);
        }
        //if (i * 2 + 1 == absorb)
        if (i * 2 + 1 == absorbCap) {
          //Draw Half Heart
          if (absorb > 20) {
            //We have wrapped, Draw both parts of the heart separately
            //Bind our custom texture
            mc.getTextureManager().bindTexture(ICON_ABSORPTION);

            //Draw first half of tinted white heart
            RenderSystem.color4f(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_ONE_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 9, 0, 9, 9);

            //Second pass dark highlights
            RenderSystem.color4f(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_TWO_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 9, 9, 9, 9);

            //Third pass dot highlight
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, PASS_SIX_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 27, 0, 9, 9);

            //Draw second half of tinted white heart
            RenderSystem.color4f(secondHalfColor.Red, secondHalfColor.Green, secondHalfColor.Blue, PASS_ONE_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 18, 0, 9, 9);

            //Second pass dark highlights
            RenderSystem.color4f(secondHalfColor.Red, secondHalfColor.Green, secondHalfColor.Blue, PASS_TWO_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 18, 9, 9, 9);
          } else {
            //Draw only first half of heart

            //Bind our custom texture
            mc.getTextureManager().bindTexture(ICON_ABSORPTION);

            //Draw tinted white heart
            RenderSystem.color4f(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_ONE_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 9, 0, 9, 9);

            //Second pass dark highlights
            RenderSystem.color4f(firstHalfColor.Red, firstHalfColor.Green, firstHalfColor.Blue, PASS_TWO_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 9, 9, 9, 9);

            //third pass dot highlight
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, PASS_SIX_ALPHA);
            drawTexturedModalRect(stack, xPosition, yPosition - offset, 27, 0, 9, 9);
          }

          //Reset back to normal settings
          RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

          mc.getTextureManager().bindTexture(ICON_VANILLA);
        }
      }
    }
    ForgeIngameGui.left_height += 10;
    if (absorb > 0) {
      ForgeIngameGui.left_height += 10;
    }


    RenderSystem.disableBlend();

    //Revert our state back
    //Revert our state back
    RenderSystem.scaled(textScale, textScale, 1);
    int index = (int) Math.max(Math.ceil(health / 20f), 1);
    String[] info = getInfo(health, (int) maxHealth, absorb);
    if (info != null) {
      int textOffset = mc.fontRenderer.getStringWidth(info[0]);
      drawStringOnHUD(stack, info[0], xStart - textOffset + 3, yStart, Integer.decode(healthColorValues.get(Math.min(index - 1, healthColorValues.size() - 1))), textScale);
      if (absorb > 0) {
        textOffset = mc.fontRenderer.getStringWidth(info[1]);
        drawStringOnHUD(stack, info[1], xStart - textOffset - 1, yStart - 10, Integer.decode(absorptionColorValues.get(Math.min((int) Math.ceil(absorb / 20d) - 1, absorptionColorValues.size() - 1))), textScale);
      }
    }
    RenderSystem.color4f(1, 1, 1, 1);
    RenderSystem.scaled(1, 1, 1);
    mc.getTextureManager().bindTexture(ICON_VANILLA);
    if (absorb > 0) {
      ForgeIngameGui.left_height += 10;
    }
    RenderSystem.popMatrix();
    mc.getProfiler().endSection();
  }

  public void potionEffects(MatrixStack stack,int x, int y, int k5, int i, int health) {
    if (k5 == 52) {
      if (i * 2 + 1 != health || health >= 20) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, POTION_ALPHA);
        drawTexturedModalRect(stack, x, y, 88, 0, 9, 9);
      } else {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, POTION_ALPHA);
        drawTexturedModalRect(stack, x, y, 97, 0, 9, 9);
      }
    }
    if (k5 == 88) {
      if (i * 2 + 1 != health || health >= 20) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, POTION_ALPHA);
        drawTexturedModalRect(stack, x, y, 124, 0, 9, 9);
      } else {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, POTION_ALPHA);
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
        return new String[]{health/2 + "/" + maxHealth/2, absorption/2 + ""};
    }
  }

  public void drawStringOnHUD(MatrixStack stack,String string, int xOffset, int yOffset, int color, double scale) {
    if (infoLevel == InfoLevel.NONE) return;
    yOffset += 9 * (1 - scale);
    xOffset += 9 * (1 - scale);
    mc.fontRenderer.drawStringWithShadow(stack,string, xOffset / (float) scale, yOffset / (float) scale, color);
  }
}