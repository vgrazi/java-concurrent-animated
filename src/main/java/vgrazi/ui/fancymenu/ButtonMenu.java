package vgrazi.ui.fancymenu;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.util.UIUtils;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Instantiate this class with a set of labels. It will create a menu of toggle buttons, with the specified hgap and vgap.
 * The button at the selected index will be selected, and
 */
public class ButtonMenu extends JPanel {
  private final KeyListener KEY_PRESS_LISTENER = new KeyAdapter() {
    @Override
    public void keyReleased(KeyEvent e) {
//      System.out.println("ButtonMenu.keyReleased " + e.getKeyCode());
      if (e.getKeyCode() == KeyEvent.VK_UP) {
        int selectedIndex = getSelectedIndex() - 1;
        if (selectedIndex < 0) {
          selectedIndex = getMenuSize() - 1;
        }
        setSelected(selectedIndex);
      } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
        int selectedIndex = getSelectedIndex() + 1;
        if (selectedIndex >= getMenuSize()) {

          selectedIndex = 0;
        }
        setSelected(selectedIndex);
      }
    }
  };


  private JToggleButton[] buttons;
  private String[] labels;

  int selectedIndex;
  private int splashIndex;
  private int referenceIndex;

  public static void main(String[] args) {
    JFrame frame = new JFrame("TEST Menu");
    Container contentPane = frame.getContentPane();
//    BoxLayout layout = new BoxLayout(contentPane, BoxLayout.X_AXIS);
    ButtonMenuLayout layout = new ButtonMenuLayout();
    contentPane.setLayout(layout);
    final ButtonMenu menu = new ButtonMenu(2, 5, 5, "Red", "Orange", "Yellow", "this is a test of the width functionality",
            "<html><body>this is a test of the height functionality</body></html>", "Green", "Blue", "Indigo", "Violet");
    contentPane.add(menu);
    contentPane.add(new JPanel());

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(500, 800);
    UIUtils.center(frame);
    frame.setVisible(true);
    System.out.printf("ButtonMenu.ButtonMenu getting preferredSize:%s%n", menu.getPreferredSize());
    for (int i = 0; i < menu.getMenuSize(); i++) {
      final int finalI = i;
      menu.setActionListener(i, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          System.out.println("ButtonMenu.actionPerformed pressed button " + finalI + " " + menu.getLabel(finalI));
        }
      });
    }

    menu.setSelected(4);
    int selectedIndex = menu.getSelectedIndex();
    assert selectedIndex == 4;
  }

  /**
   * Creates a fancy menu, containing buttons with all of the labels. Selects the suppliedIndex
   *
   * @param selectedIndex the index of the button to be selected on start
   * @param hgap          the number of pixels from the left to the buttons.
   * @param vgap          the number of additional pixels (about 5 are already supplied by the layout manager)
   * @param labels        the labels for all of the buttons
   */
  public ButtonMenu(int selectedIndex, int hgap, int vgap, String... labels) {
    // we need to control the layout
    super(null);

    // need access to the buttons by index
    buttons = new JToggleButton[labels.length];

    this.labels = labels;

    // to make a horizontal menu, change the grid layout to be 1 long and length wide
    JPanel panel = new JPanel(new GridLayout(labels.length, 1), true);
    panel.setOpaque(false);
    ButtonGroup buttonGroup = new ButtonGroup();

    // all buttons will have the same width and height, equal to the maximum preferred width and height among all of the buttons
    int preferredWidth = -1;
    int preferredHeight = -1;
    for (int i = 0, labelsLength = labels.length; i < labelsLength; i++) {
      String label = labels[i];
      JToggleButton button = new JToggleButton(label);
      button.setHorizontalAlignment(SwingConstants.LEFT);
      if (button.getPreferredSize().width > preferredWidth) {
        preferredWidth = button.getPreferredSize().width;
      }
      if (button.getPreferredSize().height > preferredHeight) {
        preferredHeight = button.getPreferredSize().height;
      }
      panel.add(button);
      buttons[i] = button;
      buttonGroup.add(button);
      final int finalI = i;
      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          ButtonMenu.this.selectedIndex = finalI;
          System.out.printf("ButtonMenu.actionPerformed setting selected index to %d%n", finalI);
        }
      });
      button.addKeyListener(KEY_PRESS_LISTENER);
    }
    // make sure to initialize this.selectedIndex, so that when we start adding listeners, the one corresponding
    // to the selected index can be fired.
    this.selectedIndex = selectedIndex;
    buttons[selectedIndex].setSelected(true);

    // leave room for all of the gaps, plus one at top and one at bottom
    int totalHeight = preferredHeight * labels.length + vgap * (labels.length + 1);
    panel.setBounds(hgap, vgap, preferredWidth, totalHeight);
    add(panel);
    Dimension preferredSize = new Dimension(preferredWidth + hgap * 2, totalHeight);
    setPreferredSize(preferredSize);
    System.out.printf("ButtonMenu.ButtonMenu setting preferredSize:%s%n", preferredSize);
    setBackground(ConcurrentExampleConstants.DEFAULT_BACKGROUND);
  }

  /**
   * Sets all of the ActionListeners
   * @param actionListeners
   */
  public void setActionListeners(List<ActionListener> actionListeners) {
    for (int i = 0; i < actionListeners.size(); i++) {
      setActionListener(i, actionListeners.get(i));
    }
  }

  /**
   * Sets the supplied action listener to the button at the specified index
   * @param index the index of the button to add the ActionListener
   * @param actionListener the ActionListener to assign to the ith button
   */
  public void setActionListener(int i, ActionListener actionListener) {
    buttons[i].addActionListener(actionListener);
    if (i == selectedIndex) {
      actionListener.actionPerformed(null);
      System.out.println("ButtonMenu.setActionListener adding selected actionListener " + selectedIndex);
    }
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public String getLabel(int i) {
    return buttons[i].getText();
  }

  public int getMenuSize() {
    return labels.length;
  }

  /**
   * Selects the specified button and fires the corresponding listener
   *
   * @param i the index of the button to select
   */
  public void setSelected(int i, boolean notifyListeners) {
    buttons[i].requestFocus();
    buttons[i].setSelected(true);
    if (notifyListeners) {
      ActionListener[] actionListeners = buttons[i].getActionListeners();
      for (ActionListener actionListener : actionListeners) {
        actionListener.actionPerformed(null);
      }
    }
    selectedIndex =i;
//    System.out.println("Selected index set to " + selectedIndex);
  }

  public void setSelected(int i) {
    setSelected(i, true);
  }

  public void setSplashIndex(int splashIndex) {
    this.splashIndex = splashIndex;
  }

  public void setReferenceIndex(int referenceIndex) {
    this.referenceIndex = referenceIndex;
  }

  public void setSplashSelected() {
    setSelected(splashIndex);
  }

  public void setReferencesSelected() {
    setSelected(referenceIndex);
  }
}
