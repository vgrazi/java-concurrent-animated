package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

/**
 * Lock/Condition parallel synchronized/wait/notify. In fact, if there is only one condition, it makes sense to use the low level sematics.
 * However once more than one condition is being tested, it makes sense to consider using a Condition.
 * Otherwise, you might have one lock object requiring many notifies.
 * Consider naming each Condition variable after the condition it is testing, for example below, notFullCondition
 * is tested before adding elements to the queue.
 */
public class ConditionExample extends ConcurrentExample {

  private ReentrantLock lock;
  private Condition condition;

  private final JButton awaitButton = new JButton("await");
  private final JButton signalButton = new JButton("signal");
  private final JButton signalAllButton = new JButton("signalAll");

  private boolean initialized = false;
  private static final int MIN_SNIPPET_POSITION = 300;

  public ConditionExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.WORKING, MIN_SNIPPET_POSITION, false, slideNumber);
    reset();
  }

  protected void initializeComponents() {
    if(!initialized) {
      initializeButton(awaitButton, new Runnable() {
        public void run() {
          try {
            setState(1);
            ConcurrentSprite sprite = createAcquiringSprite();
            lock.lock();
            condition.await();
//            setState(2);
            sprite.setReleased();
          }
          catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          finally {
            lock.unlock();
          }
        }
      });
      initializeButton(signalButton, new Runnable() {
        public void run() {
          createReleasingSprite();
          setState(2);
          lock.lock();
          condition.signal();
          lock.unlock();
        }
      });
      initializeButton(signalAllButton, new Runnable() {
        public void run() {
          createReleasingSprite();
          setState(3);
          lock.lock();
          condition.signalAll();
          lock.unlock();
        }
      });
      initialized = true;
    }
  }



  public String getDescriptionHtml() {
    StringBuffer sb = new StringBuffer();
//    sb.append("<html>");
//    sb.append("<table border=\"0\"><tr valign='top'><td valign='top'>");
//    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_FONT_COLOR + "\">");
//
//    sb.append("A <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Mutex</code></font> is a Sync implementation that is similar to a Java synchronized lock, except that ");
//    sb.append("unlike a lock, its life survives beyond the end of the block. ");
//    sb.append("A <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Mutex</code></font> is not reentrant.<p><p>");
//    sb.append("Once a Mutex is acquired, subsequent calls to <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Mutex.acquire()</code></font> will block ");
//    sb.append("until <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Mutex.release()</code></font> has been called.<p><p>");
//    sb.append("Per contract with the Sync interface, the method <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Mutex.attempt(ms)</code></font> will attempt ");
//    sb.append("an acquire and return true if sucessful or false otherwise");
//    sb.append("</td></tr>");
//    sb.append("<tr><td>&nbsp;</td></tr>");
//    sb.append("<tr><td>&nbsp;</td></tr>");
//    sb.append("<tr><td>&nbsp;</td></tr>");
//    sb.append("<tr><td>&nbsp;</td></tr>");
//    sb.append("</table></html>");

    return sb.toString();

  }

  protected void reset() {
    lock = new ReentrantLock();
    condition = lock.newCondition();
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    setState(0);
    super.reset();
  }

  protected String getSnippet() {

    return "<html><PRE><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\">" +
//       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Constructor</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"><B>Lock lock = </B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> ReentrantLock(); \n" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"><B>Condition condition = </B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> lock.newCondition(); \n" +
       "\n" +
//       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\">\n" +
//       "</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> \n" +
        "" +
            //       "    </FONT>" +
       "<font 'style=\"font-family:monospaced;\" COLOR=\"<state1:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\">" +
       "</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> \n" +
       "    lock.lock();\n" +
       "    try {\n" +
       "      condition.await(); \n" +
       "    } catch(InterruptedException e) {}\n" +
       "</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> \n" +
       "    finally {\n" +
       "      lock.unlock();\n" +
       "    } \n" +
       "</FONT>" +
            "\n" +
       "<font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> \n" +
       "    lock.lock();\n" +
       "    condition.signal();\n" +
       "    lock.unlock();\n" +
       "</FONT>" +
       "\n" +
            "<font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> \n" +
       "    lock.lock();\n" +
       "    condition.signalAll();\n" +
       "    lock.unlock();\n" +
       "</FONT>" +
       "</PRE></html>";
  }
}
