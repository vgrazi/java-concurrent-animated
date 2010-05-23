package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.ImagePanel;
import vgrazi.concurrent.samples.MessageLabel;
import vgrazi.concurrent.samples.slides.ConcurrentSlideShow;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;
import vgrazi.concurrent.samples.sprites.ConcurrentSpriteCanvas;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public abstract class ConcurrentExample extends JPanel {

  private Container container;
  //  private final Insets INSETS = new Insets(5, 5, 5, 5);
  protected final long timeout = 3 * 1000;
  protected final JLabel message1Label = new MessageLabel(" ");
  protected final JLabel message2Label = new MessageLabel(" ");
  protected final JPanel imagePanel = new ImagePanel(this);
  protected final JButton resetButton = new JButton("Reset");
  private final ConcurrentSpriteCanvas canvas;
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
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
          canvas.togglePauseResume();
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
          reset();
        } else if (e.getKeyCode() == KeyEvent.VK_X) {
          message1("Canvas:" + toString(canvas.getBounds()) + " " + (canvas.isVisible() ? "Visible" : "Hidden"), Color.BLACK);
          message2("imagePanel:" + toString(imagePanel.getBounds()) + " " + (canvas.isVisible() ? "Visible" : "Hidden"), Color.BLACK);
        }
      }
      if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
        ConcurrentSlideShow.nextSlide();
      } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
        ConcurrentSlideShow.previousSlide();
      }
    }

    private String toString(Rectangle bounds) {
      return "(" + bounds.x + "," + bounds.y + "," + bounds.width + "," + bounds.height + ")";
    }
  };
  protected ConcurrentSprite acquiredSprite;
  private static final int RELEASING_DELTA = 10;
  private static final Font SNIPPET_FONT = new Font("Arial", Font.PLAIN, 12);
  private String title;
  private ExampleType exampleType;
  private boolean fair;
  private final int slideNumber;
  private final ConcurrentLinkedQueue<JButton> buttons = new ConcurrentLinkedQueue<JButton>();
  protected final Executor threadCountExecutor = Executors.newCachedThreadPool();
  private final static Logger logger = Logger.getLogger(ConcurrentExample.class.getName());


  //  public ConcurrentExample() {
  //    this(ExampleType.BLOCKING);
  //  }

  /**
   * @param title              the title to display in the title bar
   * @param container          the container to contain the animation
   * @param exampleType        the type of animation
   * @param minSnippetPosition the horizontal position to start the snippet frame
   * @param fair               true
   * @param slideNumber        when configured as a slide show, this indicates the slide number. -1 for exclude from slide show - will still show in menu bar
   */
  public ConcurrentExample(String title, Container container, ExampleType exampleType, int minSnippetPosition, boolean fair, int slideNumber) {
    this.title = title;
    this.exampleType = exampleType;
    this.fair = fair;
    this.slideNumber = slideNumber;
    canvas = new ConcurrentSpriteCanvas(this, getTitle());
    setContainer(container);
    setLayout(new ConcurrentExampleLayout(minSnippetPosition));
    setBackgroundColors();
    message1Label.setFont(ConcurrentExampleConstants.LABEL_FONT);
    message1Label.setOpaque(false);
    message2Label.setFont(ConcurrentExampleConstants.LABEL_FONT);
    message2Label.setOpaque(false);
    //    imagePanel.setBackground(Color.yellow);
    imagePanel.setBorder(BorderFactory.createEtchedBorder());

    snippetLabel.setOpaque(true);
    snippetLabel.setFocusable(true);
    snippetLabel.setFocusTraversalKeysEnabled(true);
    snippetPane.setFocusable(true);
    snippetPane.setFocusTraversalKeysEnabled(true);
    snippetLabel.addKeyListener(keyListener);
    snippetLabel.addMouseListener(new MouseAdapter() {
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
//    snippetLabel.setToolTipText(getToolTipText());
    snippetLabel.setFont(SNIPPET_FONT);
    imagePanel.setOpaque(true);
  }

  public boolean isFair() {
    return fair;
  }

  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
  }

  private void setBackgroundColors() {
    setBackground(ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    imagePanel.setBackground(ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    snippetLabel.setBackground(Color.white);
  }

  public void setAnimationCanvasVisible(boolean value) {
    canvas.setVisible(value);
    imagePanel.setVisible(!value);
  }

  private void setContainer(Container container) {
    this.container = container;
  }

  protected synchronized ConcurrentSprite createAcquiringSprite() {
    return createAcquiringSprite(ConcurrentSprite.SpriteType.ARROW);
  }

  protected synchronized ConcurrentSprite createAcquiringSprite(ConcurrentSprite.SpriteType type) {
    final int index = getNextAcquiringIndex();
    return createAcquiringSprite(index, type);
  }

  public void shuffleSprites() {
    canvas.shuffleSprites();
  }

  public synchronized ConcurrentSprite createAcquiringSprite(int index, ConcurrentSprite.SpriteType type) {
    ConcurrentSprite sprite = new ConcurrentSprite(index);
    sprite.setType(type);
    sprite.setAcquiring();
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
    return createAttemptingSprite(ConcurrentSprite.SpriteType.ARROW);
  }

  protected ConcurrentSprite createAttemptingSprite(ConcurrentSprite.SpriteType type) {
    ConcurrentSprite sprite = new ConcurrentSprite(getNextAcquiringIndex());
    sprite.setType(type);
    sprite.setAttempting();
    canvas.addSprite(sprite);
    return sprite;
  }


  /**
   * Returns a code snippet, with &lt;state1> etc delimiting areas that need to change to bold based on state
   *
   * @return a code snippet, with &lt;state1> etc delimiting areas that need to change to bold based on state
   */
  protected abstract String getSnippet();

  /**
   * Bumps the vertical location of a {@link ExampleType#ONE_USE} mutex. Ignore by all other ExampleTypes
   */
  public void bumpMutexVerticalIndex() {
    canvas.bumpVerticalMutexIndex();
  }

  /**
   * Resets the vertical location of a {@link ExampleType#ONE_USE} mutex. Ignore by all other ExampleTypes
   */
  public void resetMutexVerticalIndex() {
    canvas.resetMutexVerticalIndex();
  }

  protected void reset() {
    acquiring = new AtomicInteger(0);
    releasing = new AtomicInteger(0);
    canvas.clearSprites();
    if (!canvas.isVisible()) {
      imagePanel.setVisible(true);
    }
    canvas.resumeClock();
    //    canvas.setVisible(false);
  }

  protected int getNextAcquiringIndex() {
    if (canvas.getSpriteCount() == 0) {
      acquiring.set(0);
      releasing.set(0);
    }
    return acquiring.incrementAndGet();
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

    //    description(getDescriptionHtml());
    validateTree();
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
        Thread thread = new Thread(runnable);
        thread.start();
      }
    });
    add(button);
  }

  public Component add(JButton button) {
    buttons.add(button);
    return super.add(button);
  }

  protected void addButtonSpacer() {
    JComponent label = new JLabel("                   ");
    label.setBackground(ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    label.setOpaque(true);
    add(label);
  }

  protected final void initializeOutput() {
    add(message1Label);
    add(message2Label);
    add(imagePanel);
  }

  /**
   * Returns a default thread count field
   * @return a default thread count field
   */
  protected JTextField createThreadCountField() {
    final JTextField threadCountField = new JTextField(3);
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

  protected void message1(String text, Color foreground) {
    final JLabel messageLabel = message1Label;
    message(messageLabel, foreground, text);
  }

  protected void message2(String text, Color foreground) {
    final JLabel messageLabel = message2Label;
    message(messageLabel, foreground, text);
  }

  private void message(final JLabel messageLabel, Color foreground, final String text) {
    messageLabel.setForeground(foreground);
    messageLabel.setText(text);
    if (foreground == ConcurrentExampleConstants.MESSAGE_COLOR) {
      Thread thread = new Thread(new Runnable() {
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
      thread.start();
    }
  }

  protected void message1(Color foreground) {
    message1Label.setForeground(foreground);
    message1Label.setText(message1Label.getText());
  }

  protected void message2(Color foreground) {
    message2Label.setForeground(foreground);
    message2Label.setText(message2Label.getText());
  }

  //  protected void description(String text) {
  //    imagePanel.setText(text);
  //  }

  protected void initializeResetButton() {
    initializeButton(resetButton, new Runnable() {
      public void run() {
        reset();
      }
    });
  }

  /**
   * Sets the state and redraws the snippet. State == -1 colors the entire snippet. State == 0 colors the constructor, etc
   *
   * @param state -1 colors the entire snippet. 0 colors the constructor, etc
   */
  protected void setState(int state) {
    String snippet = getSnippet();
    if (snippet != null) {
      if (state == -1) {
        snippet = snippet.replaceAll("<state\\d:(#\\d\\d\\d\\d\\d\\d)>", "$1");
      } else {
        snippet = snippet.replaceAll("<state" + state + ":(#\\d\\d\\d\\d\\d\\d)>", "$1");
        snippet = snippet.replaceAll("<state\\d:(#\\d\\d\\d\\d\\d\\d)>", ConcurrentExampleConstants.HTML_DISABLED_COLOR);
      }
      getSnippetLabel().setText(snippet);
    }
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

  public JPanel getImagePanel() {
    return imagePanel;
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
}
