package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

/**
 * A Semaphore is created with an initial number of permits. Each acquire decrements the count of available permits.
 * Each release increments the number of available permits.
 * Note: Release calls do not need to come from the acquiring threads; anyone can call release to increase the number
 * of available permits. Also, releases can be called even before the acquires, to effectively increase the available
 * number of permits.
 */
public class BlockingQueueExample extends ConcurrentExample {

  private BlockingQueue<ConcurrentSprite> queue;

  private final JButton putButton = new JButton("put");
  private final JButton offerButton = new JButton("offer");
  private final JButton pollButton = new JButton("poll");
  private final JButton takeButton = new JButton("take");
  private int index;
  private boolean initialized = false;
  private static final int MIN_SNIPPET_POSITION = 340;
  private JTextField threadCountField = createThreadCountField();

  public String getTitle() {
    return "BlockingQueue";
  }

  public BlockingQueueExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.BLOCKING, MIN_SNIPPET_POSITION, false, slideNumber);
  }

  protected void initializeComponents() {
    reset();
    if (!initialized) {
      initializeButton(putButton, new Runnable() {
        public void run() {
          setAnimationCanvasVisible(true);
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            put();
          }
        }
      });
      initializeButton(offerButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                offer();
              }
            });
          }
        }
      });
      initializeButton(pollButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            poll();
          }
        }
      });

      initializeButton(takeButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            take();
          }
        }
      });
      initializeThreadCountField(threadCountField);
      initialized = true;
    }

  }

  private void put() {
    try {
      message1("Waiting for acquire...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      setState(1);
      ConcurrentSprite sprite = createAcquiringSprite(ConcurrentSprite.SpriteType.OVAL);
      queue.put(sprite);
      sprite.setAcquired();
      message1("Acquired", ConcurrentExampleConstants.MESSAGE_COLOR);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void poll() {
    setState(2);
    int index = this.index++;
    message2("Attempting removal " + index, ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    ConcurrentSprite sprite = queue.poll();
    if (sprite != null) {
      sprite.setReleased();
      message2("Removed index " + index, ConcurrentExampleConstants.MESSAGE_COLOR);
    }
    else {
      message2("Poll returned null", ConcurrentExampleConstants.MESSAGE_COLOR);
    }
  }

  private void take() {
    setState(4);
    int index = this.index++;
    message2("Attempting removal " + index, ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    ConcurrentSprite sprite = null;
    try {
      sprite = queue.take();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    if (sprite != null) {
      sprite.setReleased();
      message2("Removed index " + index, ConcurrentExampleConstants.MESSAGE_COLOR);
    }
  }

  private void offer() {
    try {
      message1("Waiting for acquire...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      setState(3);
      ConcurrentSprite sprite = createAttemptingSprite(ConcurrentSprite.SpriteType.OVAL);

      if (queue.offer(sprite, timeout, TimeUnit.MILLISECONDS)) {
        sprite.setAcquired();
        message1("Acquired", ConcurrentExampleConstants.MESSAGE_COLOR);
      } else {
        sprite.setRejected();
        message1("Rejected", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public String getDescriptionHtml() {
    StringBuffer sb = new StringBuffer();
    //    sb.append("<html>");
    //    sb.append("<table border=\"0\"><tr><td>");
    //    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_FONT_COLOR + "\">");
    //
    //    sb.append("A <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>CountDown</code></font> is instantiated with a specified count.<p><p>");
    //    sb.append("One or more threads call <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>CountDown.acquire()</code></font>, which blocks until the count is achieved.<p><p>");
    //    sb.append("Subsequent threads call <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>CountDown.release()</code></font>.");
    //    sb.append(" Once the specified count is released, ");
    //    sb.append("then any acquired threads unblock and proceed.");
    //
    //    sb.append("</td></tr></table>");
    //    sb.append("</html>");

    return sb.toString();
  }

  public void reset() {
    super.reset();
    resetThreadCountField(threadCountField);
    queue = new ArrayBlockingQueue<ConcurrentSprite>(4);
    index = 1;
    message1(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    setState(0);
  }

  protected String getSnippet() {

    String snippet;
    snippet = "<html><PRE>" +
            "<font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Constructor - pass in the upper bound</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>final</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> BlockingQueue queue = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> ArrayBlockingQueue<ConcurrentSprite>(4); \n" +
            " \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Threads attempting to put will block</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// until there is room in the buffer</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\">Thread putThread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> Thread() { \n" +
            "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> run() { \n" +
            "        </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>try</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> { \n" +
            "          queue.put(); \n" +
            "        } </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>catch</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">(InterruptedException e) { }\n" +
            //       "          Thread.currentThread().interrupt(); \n" +
            //       "        } \n" +
            //       "      } \n" +
            //       "    }); \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// offer is like put except that it</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// times out after the specified timeout period</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\">Thread offerThread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> Thread() { \n" +
            "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> run() { \n" +
            "        </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>try</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> { \n" +
            "          queue.offer(someObject, 1L, TimeUnit.SECONDS</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">); \n" +
            "        }</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>catch</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">(InterruptedException e) { }\n" +
            //       "          Thread.currentThread().interrupt(); \n" +
            //       "        } \n" +
            //       "      } \n" +
            //       "    });" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Threads attempting to poll will return </I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// null if there is nothing on the queue</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\">Thread pollThread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> Thread() { \n" +
            "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> run() { \n" +
            "        queue.poll(); \n" +
            "      } \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Threads attempting to take will block</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// until the there is something to take</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000080>\">Thread takeThread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000000>\"> Thread() { \n" +
            "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000000>\"> run() { \n" +
            "        queue.take(); \n" +
            "      } \n" +
            //       "    }); \n" +
            "</FONT></PRE></html>";

    return snippet;
  }
}
