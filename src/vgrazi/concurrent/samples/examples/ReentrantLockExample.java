package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

public class ReentrantLockExample extends ConcurrentExample {

  private Lock lock;
  private final static Object MUTEX = new Object();
  private volatile int lockCount;
  private final JButton acquireButton = new JButton("lock");
  private final JButton unlockButton = new JButton("unlock");
  private final JButton attemptButton = new JButton("tryLock");
  private boolean initialized = false;
  private static final int MIN_SNIPPET_POSITION = 300;
  private final JTextField threadCountField = createThreadCountField();

  public String getTitle() {
    return "ReentrantLock";
  }

  protected String getSnippet() {
    String snippet;
    snippet = "<html><PRE><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\">" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Constructor</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\">final</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"><B> Lock lock = </B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> ReentrantLock(); \n" +
       " \n" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// Locking Thread - Once a Lock is acquired this<br>    // thread blocks until unlock is called.</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> \n" +
       "       lock.lock(); \n" +
       "<font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">\n" +
       "       // lock unblocks and work continues... \n" +
       " \n" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// Unlocking Thread - All waiting threads are<br>    // notified when lock is released. Then one is<br>    // selected at random to acquire the lock.</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> \n" +
       "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">\n" +
       " lock.unlock(); \n" +
       " \n" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// Try Lock Thread - All waiting threads are<br>    // notified when lock is released. Then one is<br>    // selected at random to acquire the lock.</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> \n" +
       "       </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\">try</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> { \n" +
       "         if(lock.tryLock(1L, TimeUnit.SECONDS</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">)){\n" +
       "           try {                                \n" +
       "             doSomething();                     \n" +
       "           } finally {                            \n" +
       "             lock.release();                    \n" +
       "           }                                 \n" +     
       "         }\n" +
       "<font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">\n" +
       "       // lock unblocks and work continues... \n" +
       "       "+
       "</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>} catch</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">(InterruptedException e) { \n" +
       "         Thread.currentThread().interrupt(); \n" +
       "       } \n" +
       "     }" +
       "</FONT></PRE></html>";
    return snippet;
  }

  public ReentrantLockExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.BLOCKING, MIN_SNIPPET_POSITION, false, slideNumber);
  }

  protected void initializeComponents() {
    if(!initialized) {
      initializeButton(acquireButton, new Runnable() {
        public void run() {
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

      initializeButton(unlockButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            if (lockCount > 0) {
              unlock();
              try {
                // sleep a little to give a chance for the locking thread to reach its target
                Thread.sleep(500);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            }
            else {
              message1("Un-held lock calling unlock", Color.red);
              message2("IllegalMonitorStateException thrown", Color.red);
              break;
            }
          }
        }
      });
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
      initialized = true;
    }
  }

  private void acquire() {
    setAnimationCanvasVisible(true);
    setState(1);
    message1("Waiting for acquire...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    ConcurrentSprite sprite = createAcquiringSprite();
    lock.lock();
    lockCount++;
    sprite.setAcquired();
    setAcquiredSprite(sprite);
    message1("Acquired", ConcurrentExampleConstants.MESSAGE_COLOR);
    waitForUnlockNotification();
  }

  private void attempt() {
    try {
      setState(3);
      message1("Attempting acquire..", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
      ConcurrentSprite sprite = createAttemptingSprite();

      if(lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
        lockCount++;
        message1("Acquire attempt succeeded", ConcurrentExampleConstants.MESSAGE_COLOR);
        sprite.setAcquired();
        setAcquiredSprite(sprite);
        waitForUnlockNotification();
      } else {
        message1("Acquire attempt failed", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
        sprite.setRejected();
      }
    }
    catch(InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Sits in a wait block until MUTEX.notify() is called by the locked thread.
   */
  private void waitForUnlockNotification() {
    synchronized(MUTEX) {
      try {
        MUTEX.wait();
        lock.unlock();
        lockCount--;
      } catch(InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private void unlock() {
    setState(2);
    message2("Waiting for unlock ", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    synchronized(this) {
      synchronized(MUTEX) {
        MUTEX.notify();
      }
      ConcurrentSprite acquiredSprite = this.acquiredSprite;
      if(acquiredSprite != null) {
        acquiredSprite.setReleased();
        setAcquiredSprite(null);
      }
      message2("Unlocked", ConcurrentExampleConstants.MESSAGE_COLOR);
    }
    setState(2);
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
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    resetThreadCountField(threadCountField);
    setState(0);
    super.reset();
  }
}
