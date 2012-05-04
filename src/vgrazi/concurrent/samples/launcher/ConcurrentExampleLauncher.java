package vgrazi.concurrent.samples.launcher;

import org.xml.sax.SAXException;
import vgrazi.concurrent.samples.Alignment;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ImagePanel;
import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.slides.ConcurrentSlideShow;
import vgrazi.ui.fancymenu.ButtonMenu;
import vgrazi.ui.fancymenu.ButtonMenuLayout;
import vgrazi.util.UIUtils;
import vgrazi.util.IOUtils;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;


/**
todo
�	I will try to expand the explanations on the descriptive slides, and also will add a mouseover to explain what is happening, especially on each executor, and explain fair and unfair<br><br>

Walking through the slides using page down is good, but seems to skip at least one of the functions in comparison to the drop-down menu. For example, using page down to navigate to 'sempahore' then again to 'Future', but can't get to 'semaphore (fair)' without using the menu. Also, explanation of what 'fair' does in comparison would be good.<br>
�	Each screen use the underlying components to control the animations. Semaphore fair and unfair for some reason have been acting the same in JDK1.6. This seems to be a JDK issue. I removed fair to avoid confusion. I put it back now. (Fair guarantees that waiting threads are released in the order they arrived. Unfair doesn�t)<br><br>

Scheduled executors are covered in the slide, but not in the animations?<br>
�	No reason, just didn�t get to that one<br><br>

Not sure there's enough information for 'condition' �<br>
�	This was hard to visualize, if someone can suggest a better approach please let me know. The way it works is that a lock is created (see the code snippet in the animation) and used to create one or more conditions. Then, threads that wish to be notified when any of those conditions occur will call await on the condition, and will sit there blocking, until another thread calls signal() or signalAll() on that condition. If signal() is called, one thread will be notified. If signalAll() is called, all threads will be notified.<br><br>

Countdownlatch, I'm not sure whether it's obvious from the slides that once the countdown has been reached, all further threads execute immediately.<br>
�	I can add some more explanation to the power point �Once the countdown has been completed, all further calls to await will pass through unblocked<br><br>
*/
public class ConcurrentExampleLauncher {
  private final JFrame frame = new JFrame();
  private final Container container = new JPanel(new BorderLayout());
  private JLabel backgroundImage;
  private ConcurrentExample examplePanel;
  private static MenuBuilder menuBuilder;

  private static volatile ConcurrentExampleLauncher instance;
  private ImagePanel imagePanel;
  private static final KeyAdapter keyListener = new KeyAdapter() {
    @Override
    public void keyReleased(KeyEvent e) {
//      logger.log(Level.INFO, "ConcurrentExampleLauncher.keyPressed " + e);
      if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN || e.getKeyCode() == KeyEvent.VK_DOWN) {
        ConcurrentSlideShow.nextSlide();
      } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_UP) {
        ConcurrentSlideShow.previousSlide();
      } else if(e.getKeyCode() == KeyEvent.VK_H && e.isControlDown()) {
        ButtonMenu buttonMenu = MenuBuilder.getButtonMenu();
        if(buttonMenu != null) {
          buttonMenu.setVisible(!buttonMenu.isVisible());
        }
      }
    }
  };
  private static String SPLASH_LABEL;
  private static String REFERENCES_LABEL;

  public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
    instance = new ConcurrentExampleLauncher();
    System.out.printf("ConcurrentExampleLauncher.main set instance to %s%n", instance);
  }

  private ConcurrentExampleLauncher() throws IOException, SAXException, ParserConfigurationException {
    // todo: remove this escape!!!
    instance = this;
    SPLASH_LABEL = IOUtils.readHtmlText(ConcurrentExampleConstants.INSTRUCTIONS_FILE);
    REFERENCES_LABEL = IOUtils.readHtmlText(ConcurrentExampleConstants.REFERENCES_FILE);
    ToolTipManager ttm = ToolTipManager.sharedInstance();
    ttm.setDismissDelay(30 * 60 * 1000);
    ttm.setInitialDelay(500);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    MenuBar menuBar = new MenuBar();

    configureMSWindowsSpecificStuff(menuBar);
    menuBuilder = new MenuBuilder(this, container, menuBar, frame);
    menuBuilder.initializeMenuItems();
    UIUtils.center(frame, .9f, .9f);
    ((JComponent) container).setOpaque(true);
    Container contentPane = frame.getContentPane();
    contentPane.setLayout(new ButtonMenuLayout());

    menuBuilder.initializeButtonMenu();
    ButtonMenu menu = MenuBuilder.getButtonMenu();
    contentPane.add(menu);
    contentPane.add(container);
    setBackgroundColors();
    contentPane.setBackground(ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    showSplash();
    frame.setVisible(true);
  }

  /**
   * If this is MS Windows, adds a menu bar
   * @param menuBar
   */
  private void configureMSWindowsSpecificStuff(MenuBar menuBar) {
    if (File.pathSeparatorChar == ';') {
      // Windows
      frame.setMenuBar(menuBar);
    }
  }


  private void setBackgroundColors() {
    container.setBackground(ConcurrentExampleConstants.DEFAULT_BACKGROUND);

  }

  private final JPanel referencesPanel = new JPanel();

  private void showSplash() {
    final JLabel label = new JLabel(SPLASH_LABEL);
    showTitlePane(label);
  }

  private void showReferences() {
    final JLabel label = new JLabel(REFERENCES_LABEL);
    referencesPanel.add(label);
    showTitlePane(label);
  }

  private void showTitlePane(JLabel label) {
    showTitlePane(label, ConcurrentExampleConstants.FRAME_TITLE, ConcurrentExampleConstants.LOGO, false, Alignment.CENTER);
  }

  public void showTitlePane(JLabel label, String frameTitle, String imageName, boolean resizeImage, Alignment alignment) {
    try {
      clearFrame();
      frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      if (frameTitle != null) {
        frame.setTitle(frameTitle);
      }
      ImageIcon imageIcon = getImageIcon(imageName, resizeImage);
      clearFrame();
      backgroundImage = new JLabel(imageIcon);
      backgroundImage.setHorizontalAlignment(alignment.getHorizontal());
      backgroundImage.setVerticalAlignment(alignment.getVertical());
      backgroundImage.setBackground(Color.white);
      backgroundImage.setOpaque(true);
      backgroundImage.setFocusable(true);


      backgroundImage.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if ((e.getModifiersEx() & MouseEvent.ADJUSTMENT_EVENT_MASK) != 0) {
            ConcurrentSlideShow.previousSlide();
          }
          else {
            ConcurrentSlideShow.nextSlide();
          }
        }
      });

      container.setLayout(null);
      backgroundImage.setBounds(-400, -600, 2000, 2000);
      container.add(backgroundImage);

      ((Container) frame.getGlassPane()).setLayout(null);
      frame.getGlassPane().setVisible(true);
      if (label != null) {
        label.setBounds(200, -600, 2000, 2000);
        ((Container) frame.getGlassPane()).add(label);
      }
      frame.getGlassPane().validate();

      backgroundImage.addKeyListener(keyListener);
      backgroundImage.requestFocus();
      container.validate();
    } finally {
      frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }

  private ImageIcon getImageIcon(String imageName, boolean resizeImage) {
    ImageIcon imageIcon = vgrazi.util.UIUtils.getImageIcon(imageName);
    if (resizeImage) {
      Dimension size = getImageSize();
      imageIcon = new ImageIcon(imageIcon.getImage().getScaledInstance(size.width, size.height, 0));
    }
    return imageIcon;
  }

  /**
   * Returns the preferred image size, which is the largest size that will fit into the existing screen, retaining the image proportions
   * @return the preferred image size, which is the largest size that will fit into the existing screen, retaining the image proportions
   */
  private Dimension getImageSize() {
    return frame.getSize();
  }

  public void showExamplePanel(ConcurrentExample examplePanel) {
    clearFrame();
    this.examplePanel = examplePanel;
    this.imagePanel = null;
    frame.setVisible(true);
    if (examplePanel != null) {
      container.setLayout(new BorderLayout());
      container.add(examplePanel);
      examplePanel.launchExample();
      if(examplePanel.getSlideNumber() != -1) {
        ConcurrentSlideShow.setSlideShowIndex(examplePanel.getSlideNumber());
      }
      int menuIndex = examplePanel.getMenuIndex();
//      System.out.printf("ConcurrentExampleLauncher.launchExamplePanel setting selected index to %d%n", menuIndex);
      MenuBuilder.getButtonMenu().setSelected(menuIndex, false);
      examplePanel.setAnimationCanvasVisible(true);
    } else {
      showSplashes();
    }
  }


  boolean flip = false;

  private void showSplashes() {
    if (flip) {
      showSplash();
      MenuBuilder.getButtonMenu().setSplashSelected();
    } else {
      showReferences();
      MenuBuilder.getButtonMenu().setReferencesSelected();
    }
    flip = !flip;
  }

  private void clearFrame() {
    if (backgroundImage != null) {
      container.remove(backgroundImage);
      backgroundImage = null;
    }
    if (examplePanel != null) {
      container.remove(examplePanel);
      backgroundImage = null;
    }
    if (imagePanel != null) {
      container.remove(imagePanel);
      imagePanel = null;
    }
    int componentCount = ((Container) frame.getGlassPane()).getComponentCount();
    if (componentCount > 0) {
      System.out.printf("ConcurrentExampleLauncher.clearFrame contains %d elements. Removing first %s%n", componentCount, ((Container) frame.getGlassPane()).getComponents()[0]);
      ((Container) frame.getGlassPane()).remove(0);
    }
  }

  public static ConcurrentExampleLauncher getInstance() {
    return instance;
  }
}
