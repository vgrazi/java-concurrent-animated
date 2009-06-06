package vgrazi.util;

import java.awt.*;

/**
 * Created by Victor Grazi.
 * Date: Jun 5, 2009 - 7:36:55 PM
 */
public class UIUtils {
  /**
   * Sets the size as a percentage of the screen width and height, then centers it
   * @param frame the frame to center
   * @param widthPercent the percent of the screen width to size it
   * @param heightPercent the percent of the screen height to size it
   */
  public static void center(Frame frame, float widthPercent, float heightPercent) {
    widthPercent = coerceZeroToOne(widthPercent);
    heightPercent = coerceZeroToOne(heightPercent);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int width = (int) (widthPercent * screenSize.width);
    int height = (int) (heightPercent * screenSize.height);
    int x = (screenSize.width - width)/2;
    int y = (screenSize.height - height)/2;
    frame.setBounds(x, y, width, height);
  }

  /**
   * If less than 0, returns 0. If greater than 1 returns 1, else returns the value
   * @param percent the percent value to coerce
   * @return the value between 0 and 1
   */
  private static float coerceZeroToOne(float percent) {
    if(percent <= 0) return 0; else if (percent > 1) return 1; else return percent;
  }
}
