package vgrazi.concurrent.samples.examples;

import jsr166y.Phaser;
import jsr166y.ThreadLocalRandom;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.canvases.ConcurrentSpriteCanvas;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * @author Heinz Kabutz
 */
public class BlinkerExample extends ConcurrentExample {
  private final JPanel blinkerPanel = new ConcurrentSpriteCanvas(this, "Phaser BlinkerExample");

  private static final boolean USE_THREAD_LOCAL_RANDOM = false;
  private static final int BUTTON_COUNT = 21;
  private static final int BLINK_COUNT = 3;
  private Phaser phaser;
  private final Random rand = USE_THREAD_LOCAL_RANDOM ? ThreadLocalRandom.current() : new Random();
  /**
   * Keeps track of the phase # that was displayed in the most recent message
   */
  private int lastMessageIndex = -1;


  private JComponent[] buttonArray = new JComponent[BUTTON_COUNT];
  private boolean initialized;


  /**
   * @param title        the title to display in the title bar
   * @param container    the container to contain the animation
   * @param exampleType  the type of animation
   * @param snippetWidth the horizontal position to start the snippet frame
   * @param fair         true
   * @param slideNumber  when configured as a slide show, this indicates the slide number. -1 for exclude from slide show - will still show in menu bar
   */
  public BlinkerExample(String title, Container container, ExampleType exampleType, int snippetWidth, boolean fair, int slideNumber) {
    this(title, container, fair, slideNumber);
  }

  public BlinkerExample(String title, Container container, boolean fair, int slideNumber) {
    this(title, container, slideNumber);
  }

  public BlinkerExample(String title, Container container, int slideNumber) {
    super(null, container, ExampleType.WORKING, 550, false, slideNumber);
    addButtons(BUTTON_COUNT);
    blinkerPanel.setLayout(new GridLayout(0, 3));
    add(blinkerPanel);
    reset();
  }

  private void addButtons(int buttons) {
    for (int i = 0; i < buttons; i++) {
      final JComponent comp = new JButton("<html><body align='center'>Button <br/>" + i + "</body></html>");
      comp.setOpaque(true);
      final Color defaultColor = comp.getBackground();
      changeColor(comp, defaultColor);
      blinkerPanel.add(comp);
      buttonArray[i] = comp;
      if(i == buttons -1) {
        // use the last button for sizing (its the largest)
        Dimension preferredSize = comp.getPreferredSize();
        int buttonWidth = preferredSize.width;
        int buttonHeight = preferredSize.height;
        blinkerPanel.setBounds(20, 200, 3 * (buttonWidth), 6 * (buttonHeight + 10));
      }
    }
  }

  private void start() {
    resetPhaser();
    message1("", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2("", ConcurrentExampleConstants.MESSAGE_COLOR);
    setState(1);
    for (int i = 0, buttonArrayLength = buttonArray.length; i < buttonArrayLength; i++) {
      final JComponent comp = buttonArray[i];
      final int finalI = i;
      Thread thread = new Thread() {
        public void run() {
          try {
            do {
              setStateSometimes(finalI, 3);
              final Color defaultColor = comp.getBackground();
              Color newColor = new Color(rand.nextInt());
              changeColor(comp, newColor);
              Thread.sleep(500 + rand.nextInt(3000));
              setStateSometimes(finalI, 4);
              changeColor(comp, defaultColor);
              if (!phaser.isTerminated()) {
                Toolkit.getDefaultToolkit().beep();
              }
              if (!phaser.isTerminated()) {
                Thread.sleep(2000);
                phaser.arriveAndAwaitAdvance();
              }
              setStateSometimes(finalI, 1);
            } while (!phaser.isTerminated());
            setState(0);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      };
      thread.start();
    }
  }

  private void setStateSometimes(int buttonIndex, int state) {
    if (buttonIndex % 5 == 0) {
      setState(state);
    }
    displayPhaseMessage();
  }

  private void displayPhaseMessage() {
    int phase = phaser.getPhase();
    if (lastMessageIndex != phase) {
      lastMessageIndex = phase;
      message1("Phase:" + phase, ConcurrentExampleConstants.MESSAGE_COLOR);
      if (phase < 0) {
        message2(" (Negative value == Phaser terminated)", ConcurrentExampleConstants.MESSAGE_COLOR);
      }
    }
  }

  private void changeColor(final JComponent comp, final Color color) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        comp.setBackground(color);
        blinkerPanel.invalidate();
        blinkerPanel.repaint();
      }
    });
  }

  @Override
  protected void initializeComponents() {
    if (!initialized) {
      initializeButton(new JButton("Start"), new Runnable() {
        @Override
        public void run() {
          start();
        }
      });
      initialized = true;
    }
  }

  @Override
  public String getDescriptionHtml() {
    return "";
  }

  @Override
  protected String getSnippetText() {
    return  " <0 highlight>///// COURTESY DR. HEINZ KABUTZ -\n" +
            "      CONCURRENCY SPECIALIST COURSE /////<0 default>\n<br/>" +
            " <0 keyword>private <0 default>Phaser phaser = <0 keyword>new <0 default>Phaser(21) {\n" +
            " <2 keyword>   protected boolean <2 default>onAdvance(<2 keyword>int<2 default> phase,\n" +
            "                      <2 keyword>int <2 default>registeredParties) {\n" +
            "     <2 keyword>return <2 default>phase >= BLINK_COUNT - 1\n" +
            "                      || registeredParties == 0;\n" +
            "   }\n" +
            " <0 default>};<br/>" +
            "<1 keyword>public void <1 default>start() {\n" +
            " Random rand = new Random();\n" +
            " for (final JComponent comp: buttonArray) {\n" +
            "  Thread thread = new Thread() {\n" +
            "   public void run() {\n" +
            "    <3 keyword>try <3 default>{\n" +
            "     <4 keyword>do <3 default>{\n" +
            "      <3 default>Color defaultColor = comp.getBackground();\n" +
            "      Color newColor = <3 keyword>new <3 default>Color(rand.nextInt());\n" +
            "      changeColor(comp, newColor);\n" +
            "      Thread.sleep(500 + rand.nextInt(3000));\n" +
            "      <4 default>changeColor(comp, defaultColor);\n" +
            "      Toolkit.getDefaultToolkit().beep();\n" +
            "      Thread.sleep(2000);\n" +
            "      phaser.arriveAndAwaitAdvance();\n" +
            "     } <4 keyword>while<4 default> (!phaser.isTerminated());\n" +
            "    <3 default>} <3 keyword>catch<3 default> (InterruptedException e) {\n" +
            "     Thread.currentThread().interrupt();\n" +
            "    }\n" +
            "   }\n" +
            "  };\n" +
            "  <1 default>thread.start();\n" +
            " }\n" +
            "}";
  }

  @Override
  public void reset() {
    super.reset();
    setState(0);
    message1("", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2("", ConcurrentExampleConstants.MESSAGE_COLOR);
    if (phaser != null && !phaser.isTerminated()) {
      phaser.forceTermination();
    }
    resetPhaser();
  }

  private void resetPhaser() {
    setState(0);
    phaser = new Phaser(BUTTON_COUNT) {
      protected boolean onAdvance(int phase, int registeredParties) {
        setState(2);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        return phase >= BLINK_COUNT - 1 || registeredParties == 0;
      }
    };
  }
}