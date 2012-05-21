package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

public class CountDownLatchExample extends ConcurrentExample {

  private CountDownLatch countDownLatch;

  private final JButton awaitButton = new JButton("await");
  private final JButton countdownButton = new JButton("countDown");
  private final JButton attemptButton = new JButton("await(timeMS, TimeUnit)");

  private int index;
  private boolean initialized = false;
  private final JTextField threadCountField = createThreadCountField();

  public String getTitle() {
    return "CountDownLatch";
  }

  public CountDownLatchExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.WORKING, 520, false, slideNumber);
  }

  protected void initializeComponents() {
    reset();
    if(!initialized) {
      initializeButton(awaitButton, new Runnable() {
        public void run() {
          setAnimationCanvasVisible(true);
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                acquire();
              }
            });
          }
        }
      });
      initializeButton(countdownButton, new Runnable() {
        public void run() {
            release();
        }
      });
      addButtonSpacer();
      initializeButton(attemptButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                attempt();
              }
            });
          }
        }
      });
      initializeThreadCountField(threadCountField);
      Dimension size = new Dimension(144, awaitButton.getPreferredSize().height);
      awaitButton.setPreferredSize(size);
      countdownButton.setPreferredSize(size);
      initialized = true;
    }

  }

  private void attempt() {
    try {
      setState(3);
      message1("Attempting acquire..", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      ConcurrentSprite sprite = createAttemptingSprite();
      if(countDownLatch.await(timeout, TimeUnit.MILLISECONDS)) {
        message1("Acquire attempt succeeded", ConcurrentExampleConstants.MESSAGE_COLOR);
        sprite.setReleased();
      } else {
        message1("Acquire attempt failed", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
        sprite.setRejected();
      }
      setState(3);
    }
    catch(InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void release() {
    setState(2);
    int index = this.index++;
    message2("Attempting release " + index, ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    countDownLatch.countDown();
    createReleasingSprite();
    message2("Released index " + index, ConcurrentExampleConstants.MESSAGE_COLOR);
    setState(2);
  }

  private void acquire() {
    try {
      message1("Waiting for acquire...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      setState(1);
      ConcurrentSprite sprite = createAcquiringSprite();
      countDownLatch.await();
      sprite.setReleased();
      message1("Acquired", ConcurrentExampleConstants.MESSAGE_COLOR);
      setState(1);
    } catch(InterruptedException e) {
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

  @Override
  public void reset() {
    super.reset();
    countDownLatch = new CountDownLatch(4);
    index = 1;
    resetThreadCountField(threadCountField);
    message1(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    setState(0);
  }

  protected String getSnippet() {

    return "<html><PRE>\n" +
       "<font style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       " \n" +
       " </FONT><font style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Constructor - pass in the pass count</I></FONT><font style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       " </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>final</B></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> CountDownLatch countDownLatch = </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\">\n\n" +
       " </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\">        <B>new</B></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> CountDownLatch(4); \n" +
       " \n" +
       " </FONT><font style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Threads attempting to acquire</I></FONT><font style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       " </FONT><font style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// will block until the specified</I></FONT><font style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       " </FONT><font style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// number of releases is counted.</I></FONT><font style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       " </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\">Thread acquireThread = </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>new</B></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> Thread() { \n" +
       "   </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>public</B></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>void</B></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> run() { \n" +
       "     </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>try</B></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> { \n" +
       "       countDownLatch.await(); \n" +
       "     } </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>catch</B></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">(InterruptedException e) { }\n" +
//       "        } \n" +
//       "      } \n" +
//       "    }); \n" +
       " \n" +
       " </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\">Thread releaseThread = </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>new</B></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> Thread() { \n" +
       "   </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>public</B></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>void</B></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> run() { \n" +
       "     countDownLatch.countDown(); \n" +
       "   } \n" +
//       "    }); \n" +
       " \n" +
       " </FONT><font style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// timed await is like await except that</I></FONT><font style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       " </FONT><font style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// it times out after the specified </I></FONT><font style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       " </FONT><font style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// timeout period.</I></FONT><font style=\"font-family:monospaced;\" COLOR=\"#000000\">\n" +
       "   </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>try</B></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> {\n" +
       "     countDownLatch.await(1L, TimeUnit.DAYS)</FONT><font style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">);\n" +
       "   } </FONT><font style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>catch</B></FONT><font style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">(InterruptedException e) { }\n" +
//       "      } \n" +
       "</FONT></PRE></html>";
  }
}
