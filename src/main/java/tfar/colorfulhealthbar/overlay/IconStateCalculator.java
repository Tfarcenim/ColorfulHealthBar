package tfar.colorfulhealthbar.overlay;


import java.util.List;

/*
    Class manages the calculations required to determine the correct color(s) to use
 */
public class IconStateCalculator {
  private static void setIconColor(Icon icon, List<? extends String> colors, int scale, int value) {
    int currentScale = scale;
    int previousScale = scale - 1;

    //Force last color if we have run out of colors on the list
    if (currentScale > colors.size() - 1) {
      currentScale = colors.size() - 1;
    }
    if (previousScale > colors.size() - 1) {
      previousScale = colors.size() - 1;
    }

    //Previous scale is -1 between 0 and 20 points of health, so reset to 0 for sane value
    if (previousScale < 0) {
      previousScale = 0;
    }

    //Covers 2 (FULL) and 1 (HALF) - Primary Color
    if (value >= 1) {
      //Should be current tier color
      icon.primaryIconColor.setColorFromHex(colors.get(currentScale));
    }

    //Covers 1 (HALF) - Secondary Color
    if (value == 1) {
      //Should be previous tier color
      icon.secondaryIconColor.setColorFromHex(colors.get(previousScale));
    }

    if (value == 0) {
      //Should be previous tier color
      icon.primaryIconColor.setColorFromHex(colors.get(previousScale));
    }
  }

  public static Icon[] calculateIcons(int playerHealthValue, List<? extends String> colors) {
    Icon[] icons = new Icon[10];

    //Calculate which color scale to use
    int scale = playerHealthValue / 20;

    //Scale the value down for each position
    int counter = playerHealthValue - (scale * 20);

    //Handle exact wrap around situation
    if (scale > 0 && counter == 0) {
      //Show we are maxed out at previous scale
      scale -= 1;
      counter = 20;
    }

    for (int i = 0; i < 10; i++) {
      icons[i] = new Icon();
      setIconColor(icons[i], colors, scale, counter);
      if (counter >= 2) {
        //We have at least a full icon to show
        icons[i].iconType = Icon.Type.FULL;
        counter -= 2;
      } else if (counter == 1) {
        //We have a half icon to show
        icons[i].iconType = Icon.Type.HALF;
        counter -= 1;
      } else {
        if (scale > 0) {
          //If scale is greater than 1 we have wrapped so we should use a full heart
          icons[i].iconType = Icon.Type.FULL;
        } else {
          //Empty icon
          icons[i].iconType = Icon.Type.NONE;
        }
      }
    }

    return icons;
  }
}
