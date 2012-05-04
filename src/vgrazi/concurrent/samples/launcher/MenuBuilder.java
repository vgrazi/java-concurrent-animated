package vgrazi.concurrent.samples.launcher;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import vgrazi.concurrent.samples.Alignment;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ImagePanelActionListener;
import vgrazi.concurrent.samples.examples.*;
import vgrazi.concurrent.samples.slides.ConcurrentSlideShow;
import vgrazi.ui.fancymenu.ButtonMenu;
import vgrazi.util.IOUtils;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Reads the plan.xml file and uses it to construct the menus and slides
 */
public class MenuBuilder extends DefaultHandler {
  private static int delta = 0;
  private ConcurrentExampleLauncher concurrentExampleLauncher;
  private Container container;
  private MenuBar menuBar;
  private JFrame frame;
  private final TreeMap<Integer, ActionListener> slideShowSlides = new TreeMap<Integer, ActionListener>();
  private final static Logger logger = Logger.getLogger(MenuBuilder.class.getCanonicalName());
  private String label;
  private List<Example> examples;
  private final Class<?>[] constructorTypes1 = new Class<?>[]{String.class, Container.class, boolean.class, int.class};
  private final Class<?>[] constructorTypes2 = new Class<?>[]{String.class, Container.class, int.class};
  private final List<String> menuItems = new ArrayList<String>();
  private final List<ActionListener> actionListeners = new ArrayList<ActionListener>();
  private static ButtonMenu buttonMenu;

  public MenuBuilder(ConcurrentExampleLauncher concurrentExampleLauncher, Container container, MenuBar menuBar, JFrame frame) {
    this.concurrentExampleLauncher = concurrentExampleLauncher;
    this.container = container;
    this.menuBar = menuBar;
    this.frame = frame;
  }

  public void initializeMenuItems() throws SAXException, ParserConfigurationException, IOException {
    warmup();

    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(ConcurrentExampleConstants.PLAN_FILE);
    parser.parse(in, this);
    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
      public void run() {
        container.repaint();
      }
    }, 0, 1000, TimeUnit.MILLISECONDS);
  }

  private boolean imageSlide;
  private int menuIndex = -1;

  @Override
  public void startElement(final String uri, String localName, String qName, final Attributes attributes) throws SAXException {
    if("ImageSlide".equals(qName)) {
      menuIndex++;
      imageSlide = true;
      String htmlFile = attributes.getValue("text");
      if (htmlFile != null) {
        try {
          String html = IOUtils.readHtmlText(htmlFile);
          initializeImageSlide(attributes.getValue("image"), html, delta++, false, Alignment.CENTER, menuIndex);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      else {
        initializeImageSlide(attributes.getValue("image"), delta++, false, Alignment.CENTER, menuIndex);
      }
    }
    else if("MenuItem".equals(qName)) {
      if (!imageSlide) {
        menuIndex++;
      }
      else {
        imageSlide = false;
      }
      // don't set imageSlide = false, an imageSlide followed by a MenuItem is still an ImageSlide
      label = attributes.getValue("label");
      examples = new ArrayList<Example>();
    }
    else if("Example".equals(qName)) {
      imageSlide = false;
//      <Example class="vgrazi.concurrent.samples.examples.BlockingQueueExample" paging="true" fair="false" mutexLabel="BlockingQueue"/>
      Example example = new Example(attributes.getValue("class"), attributes.getValue("paging"), attributes.getValue("fair"), attributes.getValue("mutexLabel"));
      examples.add(example);
    }
//    else if("References".equals(qName)) {
//      imageSlide = false;
//      initializeReferencesMenuItem();
//    }
    else if("Help".equals(qName)) {
      imageSlide = false;
      initializeHelpMenuItem();
    }
    else if("Hyperlink".equals(qName)) {
      if (Desktop.isDesktopSupported()) {
        menuIndex++;
        imageSlide = false;
        String menuLabel = attributes.getValue("label");
        final String url = attributes.getValue("uri");
        Menu menu = new Menu(menuLabel);
        menuBar.add(menu);
        menuItems.add(menuLabel);
        MenuItem menuItem = new MenuItem(menuLabel);
        menu.add(menuItem);
        ActionListener actionListener = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            openURL(url);
          }
        };
        menuItem.addActionListener(actionListener);
        actionListeners.add(actionListener);
      }
    }
  }

  private void openURL(String uri) {
    try {
      Desktop.getDesktop().browse(new URI(uri));
    } catch (IOException e) {
      /* TODO: error handling */
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (URISyntaxException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if("MenuItem".equals(qName)) {
      ConcurrentExample[] concurrentExamples = new ConcurrentExample[this.examples.size()];
      Example example = null;
        try {
          for (int i = 0, examplesSize = examples.size(); i < examplesSize; i++) {
            example = examples.get(i);
            Class exampleClass = Class.forName(example.className);
            try {
              Constructor constructor = exampleClass.getConstructor(constructorTypes1);
              concurrentExamples[i] = (ConcurrentExample) constructor.newInstance(example.mutexLabel, container, example.fair, (example.paging ? delta++ : -1));
            } catch (NoSuchMethodException e) {
              Constructor constructor = exampleClass.getConstructor(constructorTypes2);
              concurrentExamples[i] = (ConcurrentExample) constructor.newInstance(example.mutexLabel, container, (example.paging ? delta++ : -1));
            }
            concurrentExamples[i].setMenuIndex(menuIndex);
          }
        } catch (NoSuchMethodException e) {
          throw new SAXException("No constructor for " + example.className);
        } catch (ClassNotFoundException e) {
          throw new SAXException("No class found " + example.className);
        } catch (InvocationTargetException e) {
          throw new SAXException("Invocation target exception trying to construct " + example.className);
        } catch (InstantiationException e) {
          throw new SAXException("Invocation target exception trying to instantiate " + example.className);
        } catch (IllegalAccessException e) {
          throw new SAXException("IllegalAccess exception trying to instantiate " + example.className);
        }
      initializeMenuItem(label, concurrentExamples);
    }
  }

  /**
   * It seems to take too much time to launch the first Example. Therefore let's warm one up here.
   */
  private void warmup() {
    new FutureExample("", container, delta++);
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
    menuItems.add(menuLabel);
    if (examplePanels != null && examplePanels.length > 0) {
      for (int i = 0, examplePanelsLength = examplePanels.length; i < examplePanelsLength; i++) {
        ConcurrentExample examplePanel = examplePanels[i];
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
        if (i == 0) {
          actionListeners.add(actionListener);
        }
      }
    }
  }

  private void initializeReferencesMenuItem() {
    String menuLabel = "References";
    Menu menu = new Menu(menuLabel);
    menuBar.add(menu);
    menuItems.add(menuLabel);
    MenuItem menuItem = new MenuItem(menuLabel);
    menu.add(menuItem);
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        concurrentExampleLauncher.showExamplePanel(null);
        container.repaint(50);
      }
    };
    menuItem.addActionListener(actionListener);
    actionListeners.add(actionListener);
  }

  /**
   * Creates a Help About menu item
   */
  private void initializeHelpMenuItem() {
    String menuLabel = "Help";
    Menu menu = new Menu(menuLabel);
    menuBar.add(menu);
    menuItems.add(menuLabel);
    MenuItem menuItem = new MenuItem("About");
    menu.add(menuItem);
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        logger.info(System.getProperties().toString());
        infoDialog();
        container.repaint(50);
      }
    };
    menuItem.addActionListener(actionListener);
    actionListeners.add(actionListener);
  }

  private void initializeImageSlide(String imageName, int slideNumber, boolean resizeImage, Alignment alignment, int menuIndex) {
    initializeImageSlide(imageName, null, slideNumber, resizeImage, alignment, menuIndex);
  }

  private void initializeImageSlide(String imageName, String htmlText, int slideNumber, boolean resizeImage, Alignment alignment, int menuIndex) {
    if (slideNumber != -1) {
      ActionListener actionListener = new ImagePanelActionListener(imageName, htmlText, resizeImage, alignment, menuIndex, this);
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

  public ButtonMenu initializeButtonMenu() {
    if (buttonMenu == null) {
      String[] strings = new String[menuItems.size()];
      menuItems.toArray(strings);
      buttonMenu = new ButtonMenu(0, 1, 0, strings);
      buttonMenu.setActionListeners(actionListeners);
    }
    return buttonMenu;
  }

  public static ButtonMenu getButtonMenu() {
    return buttonMenu;
  }

  /**
   * A wrapper class for the XML Example specification.eg<br>
   * &lt;Example class="vgrazi.concurrent.samples.examples.BlockingQueueExample" paging="true" fair="false" mutexLabel="BlockingQueue"/>
   */
  class Example {
    String className;
    boolean paging;
    boolean fair;
    String mutexLabel;

    public Example(String className, String paging, String fair, String mutexLabel) {
      this.className = className;
      this.paging = Boolean.valueOf(paging);
      this.fair = Boolean.valueOf(fair);
      this.mutexLabel = mutexLabel;
    }
  }
}
