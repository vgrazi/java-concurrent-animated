package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

public class ReentrantLockExample extends ConcurrentExample {

  private Lock lock;
  private final Object MUTEX = new Object();
//  @ProtectedBy(this)
  private final List<ThreadSpriteHolder> interruptibleSprites = Collections.synchronizedList(new ArrayList<ThreadSpriteHolder>());
  private volatile int lockCount;
  private final JButton lockButton = new JButton("lock");
  private final JButton unlockButton = new JButton("unlock");
  private final JButton interruptWaitingButton = new JButton("interrupt (waiting)");
  private final JButton interruptLockedButton = new JButton("interrupt (locked)");
  private final JButton tryButton = new JButton("tryLock");
  private final JButton lockInterruptiblyButton = new JButton("lockInterruptibly");
  private boolean initialized = false;
  private static final int MIN_SNIPPET_POSITION = 450;
  private final JTextField threadCountField = createThreadCountField();
  private ThreadSpriteHolder lockedSprite;

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
       "       lock.lock(); \n    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000000>\">" +
        "   try {                                \n" +
        "         lock.lockInterruptibly();                     \n" +
        "       } catch(InterruptedException {...}                            \n" +
       "<font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">\n" +
       "       // lock unblocks and work continues... \n" +
       " \n" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// Unlocking Thread - All waiting threads are<br>    // notified when lock is unlockd. Then one is<br>    // selected at random to acquire the lock.</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> \n" +
       "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">\n" +
       " lock.unlock(); \n" +
       " \n" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// Try Lock Thread - All waiting threads are<br>    // notified when lock is unlockd. Then one is<br>    // selected at random to acquire the lock.</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> \n" +
       "       </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\">try</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> { \n" +
       "         if(lock.tryLock(1L, TimeUnit.SECONDS</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">)){\n" +
       "           try {                                \n" +
       "             doSomething();                     \n" +
       "           } finally {                            \n" +
       "             lock.unlock();                    \n" +
       "           }                                 \n" +     
       "         }\n" +
       "<font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">\n" +
       "       // lock unblocks and work continues... \n" +
       "       "+
       "</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>} catch</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">(InterruptedException e) {..} \n" +
       "</FONT></PRE></html>";
    return snippet;
  }

  public ReentrantLockExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.BLOCKING, MIN_SNIPPET_POSITION, false, slideNumber);
  }

  protected void initializeComponents() {
    if(!initialized) {
      initializeButton(lockButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                lock();
              }
            });
          }
        }
      });
      initializeButton(lockInterruptiblyButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                lockInterruptibly();
              }
            });
          }
        }
      });
      initializeButton(tryButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                tryLock();
              }
            });
          }
        }
      });

      initializeButton(unlockButton, new Runnable() {
        public void run() {
          unlockMethod();
        }
      });

      initializeButton(interruptLockedButton, new Runnable() {
        public void run() {
          setState(0);
          ThreadSpriteHolder lockedSprite = ReentrantLockExample.this.lockedSprite;
          if(lockedSprite != null) {
            lockedSprite.thread.interrupt();
          }
        }
      });

      initializeButton(interruptWaitingButton, new Runnable() {
        public void run() {
          setState(0);
          if(!interruptibleSprites.isEmpty()) {
            interrupt();
          }
        }
      });

      initializeThreadCountField(threadCountField);
      initialized = true;
    }
  }

  private void interrupt() {
    ThreadSpriteHolder holder = interruptibleSprites.remove(0);
    System.out.printf("ReentrantLockExample.run interrupting %s%n", holder.thread);
    holder.thread.interrupt();
    holder.sprite.setRejected();
    message1("Interrupted", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    Thread.currentThread().interrupt();
  }

  private void unlockMethod() {
    setState(2);
    int count = getThreadCount(threadCountField);
    for (int i = 0; i < count; i++) {
      if (lockCount > 0) {
        unlock();
        try {
          // sleep a little to give a chance for the locking thread to reach its target
          Thread.sleep(500);
        } catch (InterruptedException e) {
          message1(e.getMessage(), ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
          System.out.println("ReentrantLockExample.unlockMethod interrupted");
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

  private void lock() {
    setAnimationCanvasVisible(true);
    setState(1);
    message1("Waiting for lock...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    ConcurrentSprite sprite = createAcquiringSprite();
    lock.lock();
    lockCount++;
    sprite.setAcquired();
    lockedSprite = new ThreadSpriteHolder(Thread.currentThread(), sprite);
    setAcquiredSprite(sprite);
    message1("Acquired", ConcurrentExampleConstants.MESSAGE_COLOR);
    waitForUnlockNotification(sprite);
  }

  private void lockInterruptibly() {
    setAnimationCanvasVisible(true);
    setState(4);
    message1("Waiting for lock...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    ConcurrentSprite sprite = createAcquiringSprite();
    sprite.setColor(ConcurrentExampleConstants.ACQUIRING_INTERRUPTIBLY_COLOR);
    final ThreadSpriteHolder threadSpriteHolder = new ThreadSpriteHolder(Thread.currentThread(), sprite);
    interruptibleSprites.add(threadSpriteHolder);
    try {
      lock.lockInterruptibly();
      lockedSprite = new ThreadSpriteHolder(Thread.currentThread(), sprite);
      interruptibleSprites.remove(threadSpriteHolder);
      System.out.printf("ReentrantLockExample.lockInterruptibly %s  locked interruptibly%n", Thread.currentThread());
      lockCount++;
      sprite.setAcquired();
      setAcquiredSprite(sprite);
      message1("Acquired", ConcurrentExampleConstants.MESSAGE_COLOR);
      waitForUnlockNotification(threadSpriteHolder);
    }
    catch(InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void tryLock() {
    try {
      setState(3);
      message1("Attempting acquire..", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
      ConcurrentSprite sprite = createAttemptingSprite();

      if(lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
        lockedSprite = new ThreadSpriteHolder(Thread.currentThread(), sprite);
        lockCount++;
        message1("Acquire tryLock succeeded", ConcurrentExampleConstants.MESSAGE_COLOR);
        sprite.setAcquired();
        setAcquiredSprite(sprite);
        waitForUnlockNotification(sprite);
      } else {
        message1("Acquire tryLock failed", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
        sprite.setRejected();
      }
    }
    catch(InterruptedException e) {
      System.out.println("ReentrantLockExample.tryLock interrupted");
      Thread.currentThread().interrupt();
    }
  }

  private void waitForUnlockNotification(ThreadSpriteHolder threadSpriteHolder) {
    waitForUnlockNotification(threadSpriteHolder.sprite);
    interruptibleSprites.remove(threadSpriteHolder);
  }

  /**
   * Sits in a wait block until MUTEX.notify() is called by the locked thread.
   * @param sprite the sprite that has acquired, waiting for unlock or interrupt
   */
  private void waitForUnlockNotification(ConcurrentSprite sprite) {
    synchronized(MUTEX) {
      try {
        MUTEX.wait();
      }
      catch(InterruptedException e) {
        System.out.println("ReentrantLockExample.waitForUnlockNotification interrupted");
        sprite.setRejected();
        message1("Interrupted", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
        Thread.currentThread().interrupt();
      }
      lockedSprite = null;
      lockCount--;
      lock.unlock();
    }
  }

  private void unlock() {
//    setState(2);
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
//    setState(2);
  }

  public String getDescriptionHtml() {
//    StringBuffer sb = new StringBuffer();
//    sb.append("<html>");
//    sb.append("<table border=\"0\"><tr valign='top'><td valign='top'>");
//    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_FONT_COLOR + "\">");
//
//    sb.append("A <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Mutex</code></font> is a Sync implementation that is similar to a Java synchronized lock, except that ");
//    sb.append("unlike a lock, its life survives beyond the end of the block. ");
//    sb.append("A <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Mutex</code></font> is not reentrant.<p><p>");
//    sb.append("Once a Mutex is acquired, subsequent calls to <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Mutex.lock()</code></font> will block ");
//    sb.append("until <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Mutex.release()</code></font> has been called.<p><p>");
//    sb.append("Per contract with the Sync interface, the method <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Mutex.tryLock(ms)</code></font> will tryLock ");
//    sb.append("an lock and return true if sucessful or false otherwise");
//    sb.append("</td></tr>");
//    sb.append("<tr><td>&nbsp;</td></tr>");
//    sb.append("<tr><td>&nbsp;</td></tr>");
//    sb.append("<tr><td>&nbsp;</td></tr>");
//    sb.append("<tr><td>&nbsp;</td></tr>");
//    sb.append("</table></html>");
//    return sb.toString();
      return "";
  }

  @Override
  public void reset() {
    lock = new ReentrantLock();
    interruptibleSprites.clear();
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    resetThreadCountField(threadCountField);
    setState(0);
    super.reset();
  }
  class ThreadSpriteHolder {
    private Thread thread;
    private ConcurrentSprite sprite;

    private ThreadSpriteHolder(Thread thread, ConcurrentSprite sprite) {
      this.thread = thread;
      this.sprite = sprite;
    }

    public ConcurrentSprite getSprite() {
      return sprite;
    }
  }
}
