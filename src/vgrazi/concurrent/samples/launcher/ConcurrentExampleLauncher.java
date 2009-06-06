package vgrazi.concurrent.samples.launcher;

import vgrazi.concurrent.samples.Alignment;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ImagePanel;
import vgrazi.concurrent.samples.ImagePanelActionListener;
import vgrazi.concurrent.samples.examples.*;
import vgrazi.concurrent.samples.slides.ConcurrentSlideShow;
import vgrazi.util.logging.Logger;
import vgrazi.util.IOUtils;
import vgrazi.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

//import org.apache.log4j.Logger;

public class ConcurrentExampleLauncher {
  private final static Logger logger = Logger.getLogger(ConcurrentExampleLauncher.class.getCanonicalName());
  private final JFrame frame = new JFrame();
  private final Container container = frame.getContentPane();
  private JLabel backgroundImage;
  private ConcurrentExample examplePanel;
  private final MenuBar menuBar = new MenuBar();
  private final TreeMap<Integer, ActionListener> slideShowSlides = new TreeMap<Integer, ActionListener>();

  private static int left;
  private static int top;
  private static int width;
  private static int height;
  private static final String REFERENCES = "References";
  private static final String HELP = "Help";
  private static ConcurrentExampleLauncher instance;
  private ImagePanel imagePanel;
  private static int delta = 0;
  private static final KeyAdapter keyListener = new KeyAdapter() {
    @Override
    public void keyReleased(KeyEvent e) {
      logger.debug("ConcurrentExampleLauncher.keyPressed " + e);
      if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
        ConcurrentSlideShow.nextSlide();
      } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
        ConcurrentSlideShow.previousSlide();
      }
    }
  };
  private static String SPLASH_LABEL;
  private static String REFERENCES_LABEL;

  public static void main(String[] args) throws IOException {
    if (args.length >= 4) {
      left = Integer.parseInt(args[0]);
      top = Integer.parseInt(args[1]);
      width = Integer.parseInt(args[2]);
      height = Integer.parseInt(args[3]);
    }
    instance = new ConcurrentExampleLauncher();
  }

  private void initializeMenuItems() {
    warmup();
    initializeImageSlide("images/concurrent.gif", SPLASH_LABEL, delta++, false, Alignment.CENTER);
    initializeImageSlide("images/concurrentPackage.jpg", delta++, false, Alignment.CENTER);
    initializeImageSlide("images/executors.jpg", delta++, false, Alignment.CENTER);
    initializeMenuItem("Executors",
            new ExecutorsExample(ExecutorsExample.FIXED_TYPE, container, delta++),
            new ExecutorsExample(ExecutorsExample.SINGLE_TYPE, container, delta++),
            new ExecutorsExample(ExecutorsExample.CACHED_TYPE, container, delta++));
    initializeMenuItem("Semaphore",
            new SemaphoreExample("Semaphore", container, false, delta++),
            new SemaphoreExample("Semaphore (fair)", container, true, -1));
    initializeImageSlide("images/future.jpg", delta++, false, Alignment.CENTER);
    initializeMenuItem("Future", new FutureExample("Future", container, delta++));
    initializeImageSlide("images/reentrantLock.jpg", delta++, false, Alignment.CENTER);
    initializeMenuItem("ReentrantLock", new ReentrantLockExample("ReentrantLock", container, delta++));
    initializeImageSlide("images/condition.jpg", delta++, false, Alignment.CENTER);
    initializeMenuItem("Condition", new ConditionExample("Condition", container, delta++));
    initializeImageSlide("images/reentrantRWLock.jpg", delta++, false, Alignment.CENTER);
    initializeMenuItem("ReadWriteLock",
            new ReadWriteLockExample("ReadWriteLock (default)", container, false, delta++),
            new ReadWriteLockExample("ReadWriteLock (fair)",    container, false, -1));
    initializeImageSlide("images/blockingQueue.jpg", delta++, false, Alignment.CENTER);
    initializeMenuItem("BlockingQueue", new BlockingQueueExample("BlockingQueue", container, delta++));
    initializeImageSlide("images/cyclicBarrier.jpg", delta++, false, Alignment.CENTER);
    initializeMenuItem("CyclicBarrier", new CyclicBarrierExample("CyclicBarrier", container, delta++));
    initializeImageSlide("images/countdownLatch.jpg", delta++, false, Alignment.CENTER);
    initializeMenuItem("CountDownLatch", new CountDownLatchExample("CountDownLatch", container, delta++));
    initializeImageSlide("images/AtomicInteger.jpg", delta++, false, Alignment.CENTER);
    initializeMenuItem("AtomicInteger", new AtomicIntegerExample("Atomic Integer", container, delta++));
    initializeImageSlide("images/completionService.jpg", delta++, false, Alignment.CENTER);
    initializeMenuItem("CompletionService", new CompletionServiceExample("CompletionService", container, delta++));
    //    initializeButton("AtomicInteger", new AtomicIntegerExampleOrig(container, buttonPanel, -1), buttonPanel);
    initializeImageSlide("images/concurrent.gif", REFERENCES_LABEL, delta++, false, Alignment.CENTER);
    initializeReferencesMenuItem();
    initializeHelpMenuItem();
    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
      public void run() {
        container.repaint();
      }
    }, 0, 1000, TimeUnit.MILLISECONDS);
  }

  /**
   * It seems to take too much time to launch the first Example. Therefore let's warm one up here.
   */
  private void warmup() {
    new ExecutorsExample(ExecutorsExample.FIXED_TYPE, container, delta++);
  }

  private ConcurrentExampleLauncher() throws IOException {
    SPLASH_LABEL = IOUtils.readHtmlText(ConcurrentExampleConstants.INSTRUCTIONS_FILE);
    REFERENCES_LABEL = IOUtils.readHtmlText(ConcurrentExampleConstants.REFERENCES_FILE);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setMenuBar(menuBar);
    initializeMenuItems();
    UIUtils.center(frame, .9f, .9f);
    ((JComponent) container).setOpaque(true);
    setBackgroundColors();
    showSplash();
    frame.setVisible(true);
  }


  private void setBackgroundColors() {
    container.setBackground(ConcurrentExampleConstants.DEFAULT_BACKGROUND);

  }

  JPanel splashPanel = new JPanel();

  JPanel referencesPanel = new JPanel();

  private void showSplash() {
    final JLabel label = new JLabel(SPLASH_LABEL);
    splashPanel.add(label);
    showTitlePane(label);
  }

  private void showReferences() {
    final JLabel label = new JLabel(REFERENCES_LABEL);
    referencesPanel.add(label);
    showTitlePane(label);
  }

  private void showTitlePane(JLabel label) {
    showTitlePane(label, "Visualizing the Java Concurrent API", ConcurrentExampleConstants.LOGO, false, Alignment.CENTER);
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

      container.add(backgroundImage);

      frame.getGlassPane().setVisible(true);
      if (label != null) {
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
    URL url = ConcurrentExampleLauncher.class.getClassLoader().getResource(imageName);
    logger.debug("ConcurrentExampleLauncher.showTitlePane image: " + url);
    ImageIcon imageIcon = new ImageIcon(url);
    if (resizeImage) {
      Dimension size = getImageSize(imageIcon);
      imageIcon = new ImageIcon(imageIcon.getImage().getScaledInstance(size.width, size.height, 0));
    }
    return imageIcon;
  }

  /**
   * Returns the preferred image size, which is the largest size that will fit into the existing screen, retaining the image proportions
   * @param imageIcon contains the image to render
   * @return the preferred image size, which is the largest size that will fit into the existing screen, retaining the image proportions
   */
  private Dimension getImageSize(ImageIcon imageIcon) {
    return frame.getSize();
  }

  public void launchExamplePanel(ConcurrentExample examplePanel) {
    clearFrame();
    this.examplePanel = examplePanel;
    this.imagePanel = null;
    frame.setVisible(true);
    if (examplePanel != null) {
      container.add(examplePanel);
      examplePanel.launchExample();
      if(examplePanel.getSlideNumber() != -1) {
        ConcurrentSlideShow.setSlideShowIndex(examplePanel.getSlideNumber());
      }
      examplePanel.setAnimationCanvasVisible(true);
    } else {
      showSplashes();
    }
    //            container.repaint(1000);    
  }

  public void launchImagePanel(ImagePanel imagePanel) {
    clearFrame();
    this.examplePanel = null;
    this.imagePanel = imagePanel;
    imagePanel.setVisible(true);
//    imagePanel.setOpaque(true);
//    frame.setVisible(true);
    container.add(imagePanel);
    container.validate();
    container.doLayout();
    //            container.repaint(1000);
  }

  boolean flip = false;

  private void showSplashes() {
    if (flip) {
      showSplash();
    } else {
      showReferences();
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
    if (((Container) frame.getGlassPane()).getComponentCount() > 0) {
      ((Container) frame.getGlassPane()).remove(0);
    }
  }

  /**
   * Creates a menu in the menu bar with the specified label. Under that menu, creates a menu item for each supplied
   * example, using the example title as the menu item label
   * @param menuLabel the label for the menu
   * @param examplePanels the examples to add to the menu
   */
  private void initializeMenuItem(final String menuLabel, final ConcurrentExample... examplePanels) {
    Menu menu = new Menu(menuLabel + " ");
    menuBar.add(menu);
    if (examplePanels != null && examplePanels.length > 0) {
      for (final ConcurrentExample examplePanel : examplePanels) {
        // give the example access to the slide show slides (even if it is not involved in the slide show, so that
        // the user can digress to a menu item and then continue the slide show where it left off.
        ConcurrentSlideShow.setSlideShowSlides(slideShowSlides);
        ActionListener actionListener = new ExampleActionListener(examplePanel);
        if (examplePanel.getSlideNumber() != -1) {
          slideShowSlides.put(examplePanel.getSlideNumber(), actionListener);
        }
        MenuItem menuItem = new MenuItem(examplePanel.getTitle());
        menuItem.addActionListener(actionListener);
        menu.add(menuItem);
      }
    }
  }

  private void initializeReferencesMenuItem() {
    Menu menu = new Menu(REFERENCES);
    menuBar.add(menu);
    MenuItem menuItem = new MenuItem(REFERENCES);
    menu.add(menuItem);
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        launchExamplePanel(null);
        container.repaint(50);
      }
    });
  }

  /**
   * Creates a Help About menu item
   */
  private void initializeHelpMenuItem() {
    Menu menu = new Menu(HELP);
    menuBar.add(menu);
    MenuItem menuItem = new MenuItem("About");
    menu.add(menuItem);
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        logger.info(System.getProperties().toString());
        infoDialog();
        container.repaint(50);
      }
    });
  }

  private void initializeImageSlide(String imageName, int slideNumber, boolean resizeImage, Alignment alignment) {
    initializeImageSlide(imageName, null, slideNumber, resizeImage, alignment);
  }

  private void initializeImageSlide(String imageName, String htmlText, int slideNumber, boolean resizeImage, Alignment alignment) {
    if (slideNumber != -1) {
      ActionListener actionListener = new ImagePanelActionListener(imageName, htmlText, resizeImage, alignment);
      slideShowSlides.put(slideNumber, actionListener);
    }
  }

  private void infoDialog() {
    String message =
            new StringBuilder().
                    append("Java VM version:").append(System.getProperty("java.vm.version")).append('\n').
                    append(System.getProperty("sun.os.patch.level")).append('\n').
                    append("Java version:").append(System.getProperty("java.version")).append('\n').
                    append("Runtime version:").append(System.getProperty("java.runtime.version")).append('\n').
                    append("CPU:").append(System.getProperty("sun.cpu.isalist")).append('\n').

                    toString();
    JOptionPane.showInternalMessageDialog(frame.getContentPane(), message, "System info", JOptionPane.INFORMATION_MESSAGE);
  }

  public static ConcurrentExampleLauncher getInstance() {
    return instance;
  }
}
