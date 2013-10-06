package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.canvases.BasicCanvas;
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
 * Lock/Condition parallel synchronized/wait/notify. In fact, if there is only one condition, it makes sense to use the low level semantics.
 * However once more than one condition is being tested, it makes sense to consider using a Condition.
 * Otherwise, you might have one lock object requiring many notifies.
 * Consider naming each Condition variable after the condition it is testing, for example below, notFullCondition
 * is tested before adding elements to the queue.
 */
public class ConditionExample extends ConcurrentExample {

  private ReentrantLock lock;
  private Condition condition1;
  private Condition condition2;
  private Condition condition3;
  private final static Color CONDITION1_COLOR = Color.YELLOW;
  private final static Color CONDITION2_COLOR = Color.CYAN;
  private final static Color CONDITION3_COLOR = Color.GREEN;
  private static final Color CONDITION1_SIGNAL_COLOR = CONDITION1_COLOR.brighter();
  private static final Color CONDITION2_SIGNAL_COLOR = CONDITION2_COLOR.brighter();
  private static final Color CONDITION3_SIGNAL_COLOR = CONDITION3_COLOR.brighter().brighter();

  /**
   * Used for the snippet rendering, keeps track of which condition was last signalled - 1, 2, or 3
   */
  private int conditionIndex = 1;

  private final JButton await1Button = new JButton("await [for condition 1]");
  private final JButton await2Button = new JButton("await [for condition 2]");
  private final JButton await3Button = new JButton("await [for condition 3]");
  private final JButton signal1Button = new JButton("signal [condition 1]");
  private final JButton signal2Button = new JButton("signal [condition 2]");
  private final JButton signal3Button = new JButton("signal [condition 3]");
  private final JButton signal1AllButton = new JButton("signalAll [condition 1]");
  private final JButton signal2AllButton = new JButton("signalAll [condition 2]");
  private final JButton signal3AllButton = new JButton("signalAll [condition 3]");

  private boolean initialized = false;

  public ConditionExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.WORKING, 555, false, slideNumber);
    reset();
  }
    protected void createCanvas() {
        setCanvas(new BasicCanvas(this, getTitle()));
    }


    protected void initializeComponents() {
    if(!initialized) {
      initializeButton(await1Button, new Runnable() {
        public void run() {
          try {
            conditionIndex = 1;
            setState(1);
            ConcurrentSprite sprite = createAcquiringSprite();
            sprite.setColor(CONDITION1_COLOR);
            lock.lock();
            condition1.await();
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
      initializeButton(await2Button, new Runnable() {
        public void run() {
          try {
            conditionIndex = 2;
            setState(1);
            ConcurrentSprite sprite = createAcquiringSprite();
            sprite.setColor(CONDITION2_COLOR);
            lock.lock();
            condition2.await();
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
      initializeButton(await3Button, new Runnable() {
        public void run() {
          try {
            conditionIndex = 3;
            setState(1);
            ConcurrentSprite sprite = createAcquiringSprite();
            sprite.setColor(CONDITION3_COLOR);
            lock.lock();
            condition3.await();
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
      addButtonSpacer();
      initializeButton(signal1Button, new Runnable() {
        public void run() {
          ConcurrentSprite sprite = createReleasingSprite();
          sprite.setColor(CONDITION1_SIGNAL_COLOR);
          conditionIndex = 1;
          setState(2);
          lock.lock();
          condition1.signal();
          lock.unlock();
        }
      });
      initializeButton(signal2Button, new Runnable() {
        public void run() {
          conditionIndex = 2;
          ConcurrentSprite sprite = createReleasingSprite();
          sprite.setColor(CONDITION2_SIGNAL_COLOR);
          setState(2);
          lock.lock();
          condition2.signal();
          lock.unlock();
        }
      });
      initializeButton(signal3Button, new Runnable() {
        public void run() {
          conditionIndex = 3;
          ConcurrentSprite sprite = createReleasingSprite();
          sprite.setColor(CONDITION3_SIGNAL_COLOR);
          setState(2);
          lock.lock();
          condition3.signal();
          lock.unlock();
        }
      });
      addButtonSpacer();
      initializeButton(signal1AllButton, new Runnable() {
        public void run() {
          conditionIndex = 1;
          ConcurrentSprite sprite = createReleasingSprite();
          sprite.setColor(CONDITION1_SIGNAL_COLOR);
          setState(3);
          lock.lock();
          condition1.signalAll();
          lock.unlock();
        }
      });
      initializeButton(signal2AllButton, new Runnable() {
        public void run() {
          conditionIndex = 2;
          ConcurrentSprite sprite = createReleasingSprite();
          sprite.setColor(CONDITION2_SIGNAL_COLOR);
          setState(3);
          lock.lock();
          condition2.signalAll();
          lock.unlock();
        }
      });
      initializeButton(signal3AllButton, new Runnable() {
        public void run() {
          conditionIndex = 3;
          ConcurrentSprite sprite = createReleasingSprite();
          sprite.setColor(CONDITION3_SIGNAL_COLOR);
          setState(3);
          lock.lock();
          condition3.signalAll();
          lock.unlock();
        }
      });
      Dimension size = new Dimension(180, await1Button.getPreferredSize().height);
      await1Button.setPreferredSize(size);
      await2Button.setPreferredSize(size);
      await3Button.setPreferredSize(size);
      signal1Button.setPreferredSize(size);
      signal2Button.setPreferredSize(size);
      signal3Button.setPreferredSize(size);
      signal1AllButton.setPreferredSize(size);
      signal2AllButton.setPreferredSize(size);
      signal3AllButton.setPreferredSize(size);
      initialized = true;
    }
  }

  @Override
  public boolean displayStateColors() {
    return false;
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

  @Override
  public void reset() {
    lock = new ReentrantLock();
    conditionIndex = 1;
    condition1 = lock.newCondition();
    condition2 = lock.newCondition();
    condition3 = lock.newCondition();
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    setState(0);
    super.reset();
  }

  protected String getSnippet() {
    return getBaseSnippet().replaceAll("\\[i\\]", String.valueOf(conditionIndex));
  }
  protected String getBaseSnippet() {

    return "<html><PRE><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\">" +
       " </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"><B>Lock lock = </B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> ReentrantLock(); \n" +
       " </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"><B>Condition condition1 = </B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> lock.newCondition(); \n" +
       " </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"><B>Condition condition2 = </B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> lock.newCondition(); \n" +
       " </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"><B>Condition condition3 = </B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> lock.newCondition(); \n" +
       "\n" +
//       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\">\n" +
//       "</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> \n" +
        "" +
            //       "    </FONT>" +
       "<font 'style=\"font-family:monospaced;\" COLOR=\"<state1:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\">" +
       "</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> \n" +
       " lock.lock();\n" +
       " try {\n" +
       "   condition[i].await(); \n" +
       " } catch(InterruptedException e) {...}\n" +
       "</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> \n" +
       " finally {\n" +
       "   lock.unlock();\n" +
       " } \n" +
       "</FONT>" +
            "\n" +
       "<font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> \n" +
       " lock.lock();\n" +
       " condition[i].signal();\n" +
       " lock.unlock();\n" +
       "</FONT>" +
       "\n" +
            "<font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> \n" +
       " lock.lock();\n" +
       " condition[i].signalAll();\n" +
       " lock.unlock();\n" +
       "</FONT>" +
       "</PRE></html>";
  }
}
