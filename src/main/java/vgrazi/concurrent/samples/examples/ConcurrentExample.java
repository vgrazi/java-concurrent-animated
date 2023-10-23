package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.*;
import vgrazi.concurrent.samples.launcher.MenuBuilder;
import vgrazi.concurrent.samples.slides.ConcurrentSlideShow;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;
import vgrazi.concurrent.samples.canvases.ConcurrentSpriteCanvas;
import vgrazi.concurrent.samples.sprites.ConcurrentTextSprite;
import vgrazi.ui.fancymenu.ButtonMenu;
import vgrazi.util.StringUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public abstract class ConcurrentExample extends JPanel {

  private Container container;
  private Executor executor = Executors.newCachedThreadPool();
  protected static ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(1);
  protected final Executor threadCountExecutor = Executors.newCachedThreadPool();

  //  private final Insets INSETS = new Insets(5, 5, 5, 5);
  protected final long timeout = 3 * 1000;
  protected final JLabel message1Label = new MessageLabel("  ");
  protected final JLabel message2Label = new MessageLabel("  ");
  protected final JButton resetButton = new JButton("Reset");
  private JTextField threadCountField;

  private ConcurrentSpriteCanvas canvas;

  protected final Random random = new Random();
  private AtomicInteger acquiring = new AtomicInteger(0);

  private AtomicInteger releasing = new AtomicInteger(0);
  public static final String FONT_SIZE = "5";

  private final JLabel snippetLabel = new JLabel();

  private final JScrollPane snippetPane = new JScrollPane(snippetLabel);
  protected final KeyListener keyListener = new KeyAdapter() {
    public void keyReleased(KeyEvent e) {
      if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
        if (e.getKeyCode() == KeyEvent.VK_A) {
          setState(-1);
        } else if (e.getKeyCode() == KeyEvent.VK_C) {
          reset();
          canvas.resumeClock();
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
          canvas.togglePauseResume();
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
          reset();
          canvas.resumeClock();
        } else if (e.getKeyCode() == KeyEvent.VK_X) {
          message1("Canvas:" + toString(canvas.getBounds()) + " " + (canvas.isVisible() ? "Visible" : "Hidden"), Color.BLACK);
        }
      }
      if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN || e.getKeyCode() == KeyEvent.VK_DOWN) {
        nextSlide();
      } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_UP) {
        previousSlide();
      } else if (e.getKeyCode() == KeyEvent.VK_H && e.isControlDown()) {
        ButtonMenu buttonMenu = MenuBuilder.getButtonMenu();
        if (buttonMenu != null) {
          buttonMenu.setVisible(!buttonMenu.isVisible());
        }
      }
    }

    private String toString(Rectangle bounds) {
      return "(" + bounds.x + "," + bounds.y + "," + bounds.width + "," + bounds.height + ")";
    }
  };

  protected ConcurrentSprite acquiredSprite;
  private static final int RELEASING_DELTA = 10;
  private static final Font SNIPPET_FONT = new Font("SansSerif", Font.PLAIN, 18);
  private String title;
  private ExampleType exampleType;
  private int snippetMinimumWidth;
  private boolean fair;
  private final int slideNumber;
  private final ConcurrentLinkedQueue<JButton> buttons = new ConcurrentLinkedQueue<JButton>();
  private final static Logger logger = Logger.getLogger(ConcurrentExample.class.getName());
  private int state;
  private int menuIndex;
  /**
   * Used for calculating the vertical center line when dragged
   */
  private volatile int mouseDown;
  private volatile int offset;

  //  public ConcurrentExample() {


  //    this(ExampleType.BLOCKING);
  //  }
  /**
   * @param title              the title to display in the title bar
   * @param container          the container to contain the animation
   * @param exampleType        the type of animation
   * @param snippetMinimumWidth the horizontal position to start the snippet frame
   * @param fair               true
   * @param slideNumber        when configured as a slide show, this indicates the slide number. -1 for exclude from slide show - will still show in menu bar
   */
  public ConcurrentExample(String title, final Container container, ExampleType exampleType, int snippetMinimumWidth, boolean fair, int slideNumber) {
    this.title = title;
    this.exampleType = exampleType;
    this.snippetMinimumWidth = snippetMinimumWidth;
    this.fair = fair;
    this.slideNumber = slideNumber;
    createCanvas();
    this.container = container;
    setLayout(new ConcurrentExampleLayout());
    setBackgroundColors();
    message1Label.setFont(ConcurrentExampleConstants.LABEL_FONT);
    message1Label.setOpaque(false);
    message2Label.setFont(ConcurrentExampleConstants.LABEL_FONT);
    message2Label.setOpaque(false);

//    /*
    snippetLabel.setOpaque(true);
    snippetLabel.setFocusable(true);
    snippetLabel.setFocusTraversalKeysEnabled(true);
    snippetPane.setFocusable(true);
    snippetPane.setFocusTraversalKeysEnabled(true);
    snippetLabel.addKeyListener(keyListener);
    snippetLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        mouseDown = e.getX();
        System.out.println("ConcurrentExample.mousePressed " + mouseDown);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        snippetLabel.getTopLevelAncestor().setCursor(Cursor.getDefaultCursor());
      }
    });
    snippetLabel.addMouseMotionListener(new MouseAdapter() {
      @Override
      public void mouseDragged(final MouseEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            // the new offset is the new mouse location minus the click location
            offset += e.getX() - mouseDown;
//            System.out.println("ConcurrentExample.mouseDragged offset: " + offset + "snippet width:" + snippetPane.getWidth());
            mouseDown = e.getX();
            // validate is necessary to re-render the snippet scrollbars after dragging
            snippetLabel.validate();
            doLayout();
          }
        });
      }

      @Override
      public void mouseMoved(final MouseEvent e) {
//        System.out.println("ConcurrentExample.mouseMoved SETTING CURSOR" + e.getX() + "," + e.getY());
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            if (e.getX() < 60) {
              snippetLabel.getTopLevelAncestor().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            } else {
              snippetLabel.getTopLevelAncestor().setCursor(Cursor.getDefaultCursor());
            }
          }
        });
      }
    });
    snippetLabel.setFont(SNIPPET_FONT);
  }


  public ExampleType getExampleType() {
    return exampleType;
  }

  public boolean displayStateColors() {
    return true;
  }

  private void nextSlide() {
    getCanvas().pauseClock();
    ConcurrentSlideShow.nextSlide();
  }

  private void previousSlide() {
    getCanvas().pauseClock();
    ConcurrentSlideShow.previousSlide();
  }

  protected void createCanvas() {
    setCanvas(new ConcurrentSpriteCanvas(this, getTitle()));
  }

  public ConcurrentSpriteCanvas getCanvas() {
    return canvas;
  }

  protected void setCanvas(ConcurrentSpriteCanvas canvas) {
    this.canvas = canvas;
  }

  public boolean isFair() {
    return fair;
  }

  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
  }

  private void setBackgroundColors() {
    setBackground(ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    snippetLabel.setBackground(Color.white);
  }

  public void setAnimationCanvasVisible(boolean value) {
    canvas.setVisible(value);
  }

  protected synchronized ConcurrentSprite createSpecialHeadSprite() {
    return createAcquiringSprite(ConcurrentSprite.SpriteType.SPECIAL);
  }

  protected synchronized ConcurrentSprite createAcquiringSprite() {
    return createAcquiringSprite(ConcurrentSprite.SpriteType.WORKING);
  }

  protected synchronized ConcurrentSprite createTextSprite(String text) {
    final int index = getNextAcquiringIndex();
    ConcurrentSprite sprite = new ConcurrentTextSprite(text, index);
    sprite.setAcquiring();
    canvas.addSprite(sprite);
    return sprite;
  }

  protected synchronized ConcurrentSprite createAcquiringSprite(ConcurrentSprite.SpriteType type) {
    final int index = getNextAcquiringIndex();
    return createAcquiringSprite(index, type);
  }

  /**
   * A pulling sprite is a "getter" thread that pulls the result from a Future
   * It is drawn to the right of the mutex and waits for the associated sprite to be released
   */
  protected synchronized ConcurrentSprite createPullingSprite(ConcurrentSprite pullee) {
    return createPullingSprite(pullee, ConcurrentSprite.SpriteType.PULLER);
  }

  /**
   * A pulling sprite is a "getter" thread that pulls the result from a Future
   * It is drawn to the right of the mutex and waits for the associated sprite to be released
   * @param type
   * @return
   */
  protected synchronized ConcurrentSprite createPullingSprite(ConcurrentSprite pullee, ConcurrentSprite.SpriteType type) {
    return createPullingSprite(pullee.getIndex(), type);
  }

  public void shuffleSprites() {
    canvas.shuffleSprites();
  }

  public synchronized ConcurrentSprite createAcquiringSprite(int index, ConcurrentSprite.SpriteType type) {
    ConcurrentSprite sprite = new ConcurrentSprite(index);
    sprite.setType(type);
    sprite.setAcquiring();
    if (displayStateColors()) {
      sprite.setThreadState(Thread.State.RUNNABLE);
    }
    canvas.addSprite(sprite);
    return sprite;
  }

  /**
   * A pulling sprite is a "getter" thread that pulls the result from a Future
   * It is drawn to the right of the mutex and waits for the associated sprite to be released
   * @param index
   * @param type
   * @return
   */
  public synchronized ConcurrentSprite createPullingSprite(int index, ConcurrentSprite.SpriteType type) {
    ConcurrentSprite sprite = new ConcurrentSprite(index);
    sprite.setType(type);
    sprite.setPulling();
    canvas.addSprite(sprite);
    return sprite;
  }

  protected ConcurrentSprite createReleasingSprite() {
    ConcurrentSprite sprite = new ConcurrentSprite(getNextReleasingIndex());
    sprite.moveToAcquiringBorder();
    sprite.setColor(Color.ORANGE);
    sprite.setReleased();
    canvas.addSprite(sprite);
    return sprite;
  }

  /**
   * Returns The title to display in the animation canvas
   *
   * @return The title to display in the animation canvas
   */
  public String getTitle() {
    return title;
  }

  protected ConcurrentSprite createAttemptingSprite() {
    return createAttemptingSprite(ConcurrentSprite.SpriteType.WORKING);
  }

  protected ConcurrentSprite createAttemptingSprite(ConcurrentSprite.SpriteType type) {
    ConcurrentSprite sprite = new ConcurrentSprite(getNextAcquiringIndex());
    sprite.setType(type);
    sprite.setAttempting();
    canvas.addSprite(sprite);
    return sprite;
  }


  /**
   * Override in Example subclasses as a more convenient way of returning the snippet, instead of overriding getSnippet
   * the default getSnippet will provide the <html><head><style><pre> as well as the default classes.
   * In getSnippetText, return the string, with /n and proper indentation as desired to indicate line feed and spaces
   * then insert <d class> for each state, where d is the state and class is the class: default, keyword, literal, comment, unselected.
   * For example <3 literal> means display this as a literal (light blue) when the state==3
   * @return the snippet text
   */
  protected String getSnippetText() {
    return null;
  }

  /**
   * Returns a code snippet, with &lt;state1> etc delimiting areas that need to change to bold based on state
   *
   * @return a code snippet, with &lt;state1> etc delimiting areas that need to change to bold based on state
   */
  protected String getSnippet() {
    final String snippet;
    snippet="<html><head><style type=\"text/css\"> \n" +
            ".default { font-weight: bold}\n" +
            ".keyword { color: rgb(0,0,200); font-weight: bold; }\n" +
            ".highlight { color: rgb(0,0,0); background-color: yellow; font-weight: normal; }\n" +
            ".literal { color: rgb(0,0,255); font-weight: bold}\n" +
            ".comment { color: rgb(128,128,128);}\n" +
            ".unselected { color: rgb(128,128,128); }\n" +
            "</style> \n" +
            "</head>\n" +
            "<BODY BGCOLOR=\"#ffffff\">\n" +
            "<pre>\n" + getSnippetText() +
            "</pre></body>\n" +
            "</html>";
    return snippet;
  }

  public void reset() {
    acquiring = new AtomicInteger(0);
    releasing = new AtomicInteger(0);
    canvas.clearSprites();
    canvas.resumeClock();
    repaint();
  }

  public void pauseAnimationClock() {
    canvas.pauseClock();
  }

  protected int getNextAcquiringIndex() {
    if (canvas.getSpriteCount() == 0) {
      acquiring.set(0);
      releasing.set(0);
    }
    if (getExampleType() != ExampleType.ONE_USE) {
      return acquiring.incrementAndGet();
    }
    else {
      return 1;
    }
  }

  protected int getNextReleasingIndex() {
    return releasing.incrementAndGet() + RELEASING_DELTA;
  }

  /**
   * Called to initialize all of the components in the JFrame
   */
  protected abstract void initializeComponents();

  public abstract String getDescriptionHtml();

  /**
   * Called by the main method
   */
  public final void launchExample() {
    reset();
    canvas.resumeClock();
    initializeFrame();
  }

  /**
   * Creates a JFrame with the supplied title and dimensions, and centers it in the screen.
   * Calls subclass initializeComponents method,
   */
  protected void initializeFrame() {
    //    frame.setTitle(title);
    container.add(this);

    initializeComponents();
    initializeSnippet();
    initializeOutput();
    initializeAnimationCanvas();
    setDefaultState();
    repaint();
  }

  /**
   * Generally the default state is 0. Occasionally, for example when there are default and fair versions,
   * override this method to return the correct state pointing to the correct constructor depending on whether
   * the example is fair or unfair.
   */
  protected void setDefaultState() {
    setState(0);
  }

  private void initializeAnimationCanvas() {
    canvas.setVisible(false);
    canvas.setExampleType(exampleType);
    add(canvas);
  }

  /**
   * Formats the supplied button using the supplied GridBagConstrains, and assigns the supplied Runnable to
   * the supplied button in such a way that the Runnable is called in a new Thread, so that it does not tie up the UI
   *
   * @param button   The button to hit for this example
   * @param runnable the
   */
  protected void initializeButton(JButton button, final Runnable runnable) {
    button.addKeyListener(keyListener);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        executor.execute(runnable);
      }
    });
    add(button);
  }

  public Component add(JButton button) {
    buttons.add(button);
    return super.add(button);
  }

  protected Component addButtonSpacer() {
    SpacerButton spacerButton = new SpacerButton();
    buttons.add(spacerButton);
    return super.add(spacerButton);
  }

  protected final void initializeOutput() {
    add(message1Label);
    add(message2Label);
  }

  /**
   * Returns a default thread count field
   * @return a default thread count field
   */
  protected JTextField createThreadCountField() {
    threadCountField = new JTextField(3);
    threadCountField.setHorizontalAlignment(SwingConstants.CENTER);
    return threadCountField;
  }

  /**
   * The examples have the ability to create one or more thread count text fields.
   * To do so, declare a JTextField, then call initializeThreadCountField.
   * To get the value, call getThreadCount() supplying the field instance.
   * This method supplies a default label "Thread Count:"
   * @param threadCountField the field containing the thread count
   * @see #getThreadCount
   * @see #initializeThreadCountField(JTextField, String)
   */
  protected void initializeThreadCountField(JTextField threadCountField) {
    initializeThreadCountField(threadCountField, "Thread Count:");
  }
  /**
   * The examples have the ability to create one or more thread count text fields.
   * To do so, declare a JTextField, then call initializeThreadCountField.
   * To get the value, call getThreadCount() supplying the field instance.
   * @param threadCountField the field containing the thread count
   * @param labelText the label to display
   * @see #getThreadCount
   */
  protected void initializeThreadCountField(final JTextField threadCountField, String labelText) {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    final JLabel label = new JLabel(labelText);
    label.setForeground(Color.white);
    panel.add(label);
    final JSpinner spinner = new JSpinner();
    spinner.setEditor(threadCountField);
    final boolean[] inchange = new boolean[]{false};
    spinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        final int value = (Integer)spinner.getValue();
        if(value < 1) {
          spinner.setValue(1);
        }
        else {
          // don't change the threadCountField if the change if the notification originated from the threadCountField
          if (!inchange[0]) {
            threadCountField.setText(String.valueOf(value));
          }
        }
      }
    });
    threadCountField.getDocument().addDocumentListener(new DocumentListener() {

      public void insertUpdate(DocumentEvent e) {
        setSpinnerFromField();
      }

      public void removeUpdate(DocumentEvent e) {
        setSpinnerFromField();
      }

      public void changedUpdate(DocumentEvent e) {
        setSpinnerFromField();
      }

      private void setSpinnerFromField() {
        inchange[0] = true;
        spinner.setValue(getThreadCount(threadCountField));
        inchange[0] = false;
      }
    });
    final Dimension preferredSize = spinner.getPreferredSize();
    preferredSize.width = 45;
    spinner.setPreferredSize(preferredSize);
    panel.add(spinner);
    resetThreadCountField(threadCountField);
    spinner.setValue(getThreadCount(threadCountField));
    add(panel);
  }

  /**
   * Returns the value in the threadCountField as an int. If it can't be converted or is blank, returns 1
   * @return the value in the threadCountField as an int. If it can't be converted or is blank, returns 1
   * @param threadCountField the field containing the count value to convert
   */
  protected int getThreadCount(JTextField threadCountField) {
    int count = 1;
    String countText = threadCountField.getText();
    if(!StringUtils.isBlank(countText)) {
      try {
        count = Integer.parseInt(countText);
      } catch (NumberFormatException e) {
        logger.info("User entered incorrect value, using 1");
      }
    }
    return count;
  }

  protected int getThreadCount() {
      return getThreadCount(threadCountField);
  }

  /**
   * Resets the supplied thread count field to "1"
   * @param threadCountField the field to reset
   */
  protected void resetThreadCountField(JTextField threadCountField) {
    resetThreadCountField(threadCountField, 1);
  }

  /**
   * Resets the supplied thread count field to "1"
   * @param threadCountField the field to reset
   * @param value the value to set in the field
   */
  protected void resetThreadCountField(JTextField threadCountField, int value) {
    threadCountField.setText(String.valueOf(value));
  }

  public void message1(String text, Color foreground) {
    final JLabel messageLabel = message1Label;
    message(messageLabel, foreground, text);
  }

  public void message2(String text, Color foreground) {
    final JLabel messageLabel = message2Label;
    message(messageLabel, foreground, text);
  }

  private void message(final JLabel messageLabel, Color foreground, final String text) {
    messageLabel.setForeground(foreground);
    messageLabel.setText(text);
    if (foreground == ConcurrentExampleConstants.MESSAGE_COLOR) {
      executor.execute(new Runnable() {
        public void run() {
          try {
            Thread.sleep(1000);
            if (messageLabel.getText().equals(text) && messageLabel.getForeground() == ConcurrentExampleConstants.MESSAGE_COLOR) {
              messageLabel.setForeground(ConcurrentExampleConstants.MESSAGE_FLASH_COLOR);
            }
          }
          catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      });
    }
  }

  public void message1(Color foreground) {
    message1Label.setForeground(foreground);
    message1Label.setText(message1Label.getText());
  }

  public void message2(Color foreground) {
    message2Label.setForeground(foreground);
    message2Label.setText(message2Label.getText());
  }
    protected void clearMessages() {
        clearMessage1();
        clearMessage2();
    }
    protected void clearMessage2() {
        message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    }

    protected void clearMessage1() {
        message1(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    }



  //  protected void description(String text) {
  //    imagePanel.setText(text);
  //  }

  protected void initializeResetButton() {
    initializeButton(resetButton, new Runnable() {
      public void run() {
        reset();
        canvas.resumeClock();
      }
    });
  }

  protected int getState() {
    return state;
  }

  /**
   * Sets the state and redraws the snippet. State == -1 colors the entire snippet. State == 0 colors the constructor, etc
   *
   * @param state -1 colors the entire snippet. 0 colors the constructor, etc
   */
  public void setState(int state) {
    this.state = state;
    String snippet = getSnippet();
    snippet = applyState(state, snippet);
    getSnippetLabel().setText(snippet);

  }


    private static String applyState(int state, String snippet) {
//        System.out.println("ConcurrentExample.applyState " + state);
        if (snippet != null) {
          if (state == -1) {
            snippet = snippet.replaceAll("<state\\d:(#\\d\\d\\d\\d\\d\\d)>", "$1");
          } else {
            snippet = snippet.replaceAll("<state" + state + ":(#\\d\\d\\d\\d\\d\\d)>", "$1");
            snippet = snippet.replaceAll("<state\\d:(#\\d\\d\\d\\d\\d\\d)>", ConcurrentExampleConstants.HTML_DISABLED_COLOR);
          }
          // in order to change the size of the selected font, include a size css font style as follows: font-size:state2-size
          // the state number (in this example state2) corresponds to the state parameter
          if (state>=0) {
            snippet = snippet.replaceAll(String.format("state%d-size", state), "24pt");
            snippet = snippet.replaceAll(String.format("state[~%d]-size", state), "21pt");
          }

          // for newer html output, intelliJ is spitting out css. The default css class is .s9
          // Look for <state2:s1> if state == 2 convert that to s1 else s9
//          System.out.println(snippet);
          snippet = snippet.replaceAll("<state" + state + "\\:(\\w*)>", "$1");
          snippet = snippet.replaceAll("<state\\d:(s\\w+)>", "s9");
          snippet = snippet.replaceAll("<state\\d:(\\w+)>", "unselected");
//          "<format state=3, class=\"keyword\"/>int </format>"


          snippet = snippet.replaceAll(String.format("<%d\\s+(\\w+)>", state), String.format("</span><span class=\"%s\">", "$1"));
          snippet = snippet.replaceAll("<\\d+\\s*(\\w+)>", "</span><span class=\"unselected\">");

        }
        return snippet;
    }


  private JLabel getSnippetLabel() {
    return snippetLabel;
  }

  protected void initializeSnippet() {
    add(snippetPane);
  }

  public static void main(String[] args) {
    String test = "COLOR=\"<state0:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>";
    String s = test.replaceAll("<state0:(#\\d\\d\\d\\d\\d\\d)>", "$1");
    System.out.println(s);
  }

  public ConcurrentSpriteCanvas getAnimationCanvas() {
    return canvas;
  }

  protected synchronized void setAcquiredSprite(ConcurrentSprite sprite) {
    acquiredSprite = sprite;
  }

  /**
   * Sleeps for a Random amount of milliseconds, not more than the specified number
   *
   * @param maxms the maximum number of milliseconds
   * @throws InterruptedException if interrupted
   */
  public void sleepRandom(long maxms) throws InterruptedException {
    // add 1, since the upper bound is excluded
    long ms = random.nextInt((int) maxms);
    Thread.sleep(ms);
  }

  /**
   * Sleeps for a Random amount of seconds, not more than the specified number
   *
   * @param minMS the minimum number of milliseconds
   * @param maxMS the maximum number of milliseconds
   * @throws InterruptedException if interrupted
   */
  public void sleepRandom(long minMS, long maxMS) throws InterruptedException {
    // add 1, since the upper bound is excluded
    long ms = random.nextInt((int) ((maxMS - minMS) + minMS));
    Thread.sleep(ms);
  }


  /**
   * A call back method called by the ConcurrentSpriteCanvas when the supplied sprite has animated off the screen
   * and has been removed.
   *
   * @param sprite the sprite that has completed its animation
   */
  public void spriteRemoved(ConcurrentSprite sprite) {

  }

  public void setButtonsVisible(boolean value) {
    for (JButton button : buttons) {
      button.setVisible(value);
    }
  }

  public int getSlideNumber() {
    return slideNumber;
  }

  public Map<Integer, ActionListener> getSlideShowSlides() {
    return ConcurrentSlideShow.slideShowSlides;
  }

  public int getMenuIndex() {
    return menuIndex;
  }

  public void setMenuIndex(int menuIndex) {
    this.menuIndex = menuIndex;
  }

  /**
   * Gets the minimum snippet width, accounting for offset by dragging
   * @return the snippet width, accounting for offset by dragging
   */
  public int getSnippetMinimumWidth() {
    return snippetMinimumWidth;
  }

  /**
   * This returns the default vertical shift from the bottom button to the monolith. Usually 0, but for F&J which needs more room, use a negative shift
   * @return the shift from the default snippet pane.
   */
  public int getVerticalOffsetShift() {
    return 0;
  }

  public int getOffset() {
    return offset;
  }
}
