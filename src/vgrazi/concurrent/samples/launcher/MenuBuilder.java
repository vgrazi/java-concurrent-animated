package vgrazi.concurrent.samples.launcher;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import vgrazi.concurrent.samples.Alignment;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ImagePanelActionListener;
import vgrazi.concurrent.samples.examples.*;
import vgrazi.concurrent.samples.slides.ConcurrentSlideShow;
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

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if("ImageSlide".equals(qName)) {
      String htmlFile = attributes.getValue("text");
      if (htmlFile != null) {
        try {
          String html = IOUtils.readHtmlText(htmlFile);
          initializeImageSlide(attributes.getValue("image"), html, delta++, false, Alignment.CENTER);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      else {
        initializeImageSlide(attributes.getValue("image"), delta++, false, Alignment.CENTER);
      }
    }
    else if("MenuItem".equals(qName)) {
      label = attributes.getValue("label");
      examples = new ArrayList<Example>();
    }
    else if("Example".equals(qName)) {
//      <Example class="vgrazi.concurrent.samples.examples.BlockingQueueExample" paging="true" fair="false" mutexLabel="BlockingQueue"/>
      Example example = new Example(attributes.getValue("class"), attributes.getValue("paging"), attributes.getValue("fair"), attributes.getValue("mutexLabel"));
      examples.add(example);
    }
    else if("References".equals(qName)) {
      initializeReferencesMenuItem();
    }
    else if("Help".equals(qName)) {
      initializeHelpMenuItem();
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
    new ExecutorsExample(ExecutorsExample.FIXED_TYPE, container, delta++);
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
    Menu menu = new Menu("References");
    menuBar.add(menu);
    MenuItem menuItem = new MenuItem("References");
    menu.add(menuItem);
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        concurrentExampleLauncher.launchExamplePanel(null);
        container.repaint(50);
      }
    });
  }

  /**
   * Creates a Help About menu item
   */
  private void initializeHelpMenuItem() {
    Menu menu = new Menu("Help");
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
