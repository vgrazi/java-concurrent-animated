package vgrazi.concurrent.samples.util;

import vgrazi.concurrent.samples.launcher.ConcurrentExampleLauncher;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * @user vgrazi.
 * Time: 12:59:27 AM
 */
public class UIUtils {
  public static void center(Component component) {
    int width = component.getWidth();
    int height = component.getHeight();
    Dimension screenSize = getScreenSize();
    int screenWidth = screenSize.width;
    int screenHeight = screenSize.height;
    int x = (screenWidth - width) / 2;
    int y = (screenHeight - height) / 2;
    component.setLocation(x, y);
  }

  public static void maximize(Component component) {
    Dimension screenSize = getScreenSize();
    int width = screenSize.width;
    int height = screenSize.height;
    component.setBounds(0, 0, width, height);
  }

  public static Frame getParentFrame(Component c) {
    while (! (c instanceof Frame) && c!= null) {
      c = c.getParent();
    }
    return ((Frame) c);
  }

  private static Dimension getScreenSize() {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int screenWidth = screenSize.width;
    if(screenWidth > 1024) {
      screenWidth = 1024;
    }
    int screenHeight = screenSize.height;
//    if(screenHeight > 768) {
//      screenHeight = 768;
//    }
    return new Dimension(screenWidth, screenHeight);
  }

  public static ImageIcon getImageIcon(String imageName) {
    URL url = ConcurrentExampleLauncher.class.getClassLoader().getResource(imageName);
//    logger.log(Level.INFO, "ConcurrentExampleLauncher.showTitlePane image: " + url);
    ImageIcon imageIcon = new ImageIcon(url);
    return imageIcon;
  }

  /**
   * If less than 0, returns 0. If greater than 1 returns 1, else returns the value
   * @param percent the percent value to coerce
   * @return the value between 0 and 1
   */
  public static float coerceZeroToOne(float percent) {
    if(percent <= 0) return 0; else if (percent > 1) return 1; else return percent;
  }

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
}
