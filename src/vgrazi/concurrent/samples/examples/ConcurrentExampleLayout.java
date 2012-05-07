package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.SpacerButton;
import vgrazi.concurrent.samples.canvases.ConcurrentSpriteCanvas;
import vgrazi.concurrent.samples.MessageLabel;
import vgrazi.concurrent.samples.canvases.ForkJoinCanvas;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by IntelliJ IDEA.
 * User: vgrazi
 * Date: Sep 6, 2005
 * Time: 2:30:51 PM
 */
public class ConcurrentExampleLayout extends FlowLayout {
  private static final int MAX_OTHER_WIDTH = 650;
  private final int SNIPPET_WIDTH;
  private static final int INSET = 5;

  public ConcurrentExampleLayout(int snippetWidth) {
    SNIPPET_WIDTH = snippetWidth;
  }

  @Override
  public void layoutContainer(Container target) {
    //    super.layoutContainer(target);
    //    System.out.println("ConcurrentExampleLayout.layoutContainer size:" + target.getSize());
    Component[] components = target.getComponents();
    JScrollPane snippetPane = null;
    List<JButton> buttons = new ArrayList<JButton>();
        List<Component> others = new ArrayList<Component>();
    List<MessageLabel> messageLabels = new ArrayList<MessageLabel>();
    ConcurrentSpriteCanvas canvas = null;

    // first, let's accumulate the components that we have. Then we can lay them out
    for (Component component : components) {
      //      component.setBackground(Color.yellow);
      if (component instanceof JButton) {
        buttons.add((JButton) component);
      } else if (component instanceof MessageLabel) {
        messageLabels.add((MessageLabel) component);
      } else if (component instanceof JScrollPane) {
        snippetPane = (JScrollPane) component;
      } else if (component instanceof ConcurrentSpriteCanvas) {
        canvas = (ConcurrentSpriteCanvas) component;
      } else {
        others.add(component);
      }
    }


    int xPos = INSET;
    int yPos = 10;
    int otherWidth;

    // layout buttons
    JButton firstButton = null;
    JButton focusButton = null;
    int height = 0;
    for (int i = 0; i < buttons.size(); i++) {
      // if none of the buttons have focus, give focus to the first
      JButton button = buttons.get(i);
      if(button instanceof SpacerButton) {
        xPos = INSET;
        yPos += INSET + height + 5;
        continue;
      }
      if (i == 0) {
        firstButton = button;
      }
      if (button.hasFocus()) {
        focusButton = button;
      }
      Dimension componentSize = button.getPreferredSize();
      height = componentSize.height;
      final int width = componentSize.width;

      if (xPos + width > SNIPPET_WIDTH) {
        xPos = INSET;
        yPos += INSET + height + 5;
      }
      button.setBounds(xPos, yPos, width, height);
      xPos += INSET + width;

      //      System.out.println("ConcurrentExampleLayout.layoutContainer position button " + button.getText() + " at " + button.getBounds());
    }

    yPos += height + INSET;


    if (focusButton == null && firstButton != null) {
      firstButton.requestFocus();
    }

    int xPosOfSnippet = 0;

////// layout snippet
    if (snippetPane != null) {
      xPosOfSnippet = target.getSize().width - SNIPPET_WIDTH;
      snippetPane.setBounds(xPosOfSnippet, INSET, target.getSize().width - xPosOfSnippet - INSET, target.getSize().height - INSET * 2);

//      if (xPosOfSnippet < SNIPPET_WIDTH) {
//        xPosOfSnippet = SNIPPET_WIDTH;
//      }
//      xPosOfSnippet = xPos;
//      if (xPosOfSnippet < SNIPPET_WIDTH) {
//        xPosOfSnippet = SNIPPET_WIDTH;
//      }
//      snippetPane.setBounds(xPosOfSnippet, INSET, target.getSize().width - xPosOfSnippet - INSET, target.getSize().height - INSET * 2);
    }

    yPos += INSET * 2;

    // layout others
    xPos = INSET;
    for (Component component : others) {
      final Dimension preferredSize = component.getPreferredSize();
      final int width = preferredSize.width;
      component.setBounds(xPos, yPos, width, preferredSize.height);
      xPos += width + INSET;
    }
    yPos += height + INSET;

    // layout message labels (should be 2 of them)
    final int defaultWidth = xPosOfSnippet - INSET;
    for (Object messageLabel : messageLabels) {
      MessageLabel label = (MessageLabel) messageLabel;
      //      label.setBackground(Color.GRAY);
      //      label.setOpaque(true);
      Dimension componentSize = label.getPreferredSize();
      height = componentSize.height;
      //      System.out.println("ConcurrentExampleLayout.layoutContainer setting messageLabel y,h " + yPos + "," + height);
      label.setBounds(INSET, yPos, defaultWidth, height);
      yPos += height + INSET;
    }
    if (xPosOfSnippet > 0) {
      otherWidth = xPosOfSnippet - INSET * 2;
    } else {
      otherWidth = target.getSize().width - INSET * 2;
    }
    if (otherWidth > MAX_OTHER_WIDTH) {
      otherWidth = MAX_OTHER_WIDTH;
    }

////// layout ConcurrentExample
    // fork join canvas needs a bit more vertical space, so we don't use message2. Move up a few pixels
    if(canvas instanceof ForkJoinCanvas) {
      canvas.setBounds(INSET, yPos - 30, otherWidth, target.getSize().height - yPos);
    }
    else if (canvas != null) {
      canvas.setBounds(INSET, yPos, otherWidth, target.getSize().height - yPos);
    }
    if (snippetPane != null) {
      snippetPane.setVisible(true);
    }
    setVisible(messageLabels, true);
  }

  private void setVisible(List<MessageLabel> messageLabels, boolean b) {
    for (MessageLabel messageLabel : messageLabels) {
      messageLabel.setVisible(b);
    }
  }
}
