package vgrazi.ui.fancymenu;

import vgrazi.util.UIUtils;

import java.awt.*;

public class ButtonMenuLayout extends FlowLayout {
  @Override
  /**
   * Lays out 2 components, a ButtonMenu and something else
   */
  public void layoutContainer(Container target) {

    int buttonMenuWidth = 0;
    int buttonMenuHeight = 0;
    ButtonMenu buttonMenu = null;

    Component other = null;

    Component[] components = target.getComponents();
    int frameHeight = target.getHeight();
    for (Component component : components) {
      if (component instanceof ButtonMenu) {
        buttonMenu = (ButtonMenu) component;
        buttonMenuWidth = buttonMenu.getPreferredSize().width;
        buttonMenuHeight = frameHeight;
      } else {
        other = component;
      }
    }
    // by now, the following should be set: other, buttonMenuWidth, buttonMenuHeight, buttonMenu
    if (buttonMenu != null && buttonMenu.isVisible()) {
      buttonMenu.setBounds(0, 0, buttonMenuWidth, buttonMenuHeight);
    }
    else {
      buttonMenuWidth = 0;
    }
    if (other != null) {
      Frame parentFrame = UIUtils.getParentFrame(target);
      System.out.println("ButtonMenuLayout.layoutContainer Frame bounds:" + parentFrame.getSize());
      int frameWidth = parentFrame.getWidth();
      other.setBounds(buttonMenuWidth + 1, 0, frameWidth - buttonMenuWidth - 1, frameHeight);
      System.out.printf("ButtonMenuLayout.layoutContainer set size of component to %s%n", other.getSize());
    }
  }
}
