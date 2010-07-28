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
}
