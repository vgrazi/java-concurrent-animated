package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import java.awt.*;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

public class SynchronizedExample extends ConcurrentExample {

  private final Object lock = new Object();
  private final Object MUTEX = new Object();
  private volatile int lockCount;
  private final List<ThreadSpriteHolder> lockedSpriteList = new ArrayList<ThreadSpriteHolder>();

  // when notifying, we can either be telling the thread to wait or to unlock. Before notifying, set the state
  private enum NotifyTracker {
    WAIT, UNLOCK
  }
  private volatile NotifyTracker notifyTracker;
  //  @ProtectedBy(this)
  private final JButton lockButton = new JButton("synchronized");
  private final JButton unlockButton = new JButton("(exit synchronized)");
  private final JButton waitButton = new JButton("wait");
  private final JButton notifyButton = new JButton("notify");
  private final JButton interruptLockedButton = new JButton("interrupt (locked)");
  private boolean initialized = false;
//  private final JTextField threadCountField = createThreadCountField();

  public String getTitle() {
    return "synchronized";
  }

  @Override
  protected String getSnippetText() {
    return "    // Constructor\n" +
            "    <0 keyword>final <0 default>Lock lock = <0 keyword>new <0 default>ReentrantLock();\n" +
            "    <1 default>lock.lock();\n" +
            "\n" +
            "    <4 keyword>try<4 default> {\n" +
            "      lock.lockInterruptibly();\n" +
            "    } <4 keyword>catch <4 default>(InterruptedException e) {...}\n" +
            "\n" +
            "    <2 default>lock.unlock();\n" +
            "\n" +
            "    <3 keyword>boolean <3 default>acquired = <3 literal>false<3 default>;\n" +
            "    <3 keyword>try<3 default> {\n" +
            "      acquired = lock.tryLock(<3 literal>1L<3 default>, TimeUnit.SECONDS);\n" +
            "      <3 keyword>if<3 default>(acquired) {\n" +
            "        doSomething();\n" +
            "      }\n" +
            "    } <3 keyword>catch<3 default> (InterruptedException e) {...\n" +
            "    } <3 keyword>finally {\n" +
            "      if <3 default>(acquired) {\n" +
            "        lock.unlock();\n" +
            "      }\n" +
            "    }\n" +
            "    <6 default>&lt;lockedThread>.interrupt();\n" +
            "    <5 default>&lt;blockedThread>.interrupt();" +
            "\n";
  }

  public SynchronizedExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.BLOCKING, 570, false, slideNumber);
  }

  protected void initializeComponents() {
    if(!initialized) {
      initializeButton(lockButton, new Runnable() {
        public void run() {
//          int count = getThreadCount(threadCountField);
//          for (int i = 0; i < count; i++)
          {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                lock();
              }
            });
          }
        }
      });
      initializeButton(unlockButton, new Runnable() {
        public void run() {
          notifyTracker = NotifyTracker.UNLOCK;
          unlockMethod();
        }
      });
      initializeButton(waitButton, new Runnable() {
        @Override
        public void run() {
          notifyTracker = NotifyTracker.WAIT;
          unlockMethod();
        }
      });
      initializeButton(notifyButton, new Runnable() {
        @Override
        public void run() {
          notifyTracker = NotifyTracker.WAIT;
          unlockMethod();
        }
      });
      addButtonSpacer();
      initializeButton(interruptLockedButton, new Runnable() {
        public void run() {
          setState(0);
          ThreadSpriteHolder lockedSprite = removeLastLocked();
          if(lockedSprite != null) {
            lockedSprite.thread.interrupt();
            setState(6);
          }
        }
      });


      Dimension size = new Dimension(150, lockButton.getPreferredSize().height);
      lockButton.setPreferredSize(size);
      unlockButton.setPreferredSize(size);
      interruptLockedButton.setPreferredSize(size);

//      initializeThreadCountField(threadCountField);
      initialized = true;
    }
  }

  private ThreadSpriteHolder removeLastLocked() {
    return lockedSpriteList.remove(lockedSpriteList.size() - 1);
  }

  private ThreadSpriteHolder getLastLocked() {
    if (!lockedSpriteList.isEmpty()) {
      return lockedSpriteList.get(lockedSpriteList.size() - 1);
    }
    else {
      return null;
    }
  }

  private void unlockMethod() {
    setState(2);
//    int count = getThreadCount(threadCountField);
//    for (int i = 0; i < count; i++)
    {
      if (notifyTracker == NotifyTracker.UNLOCK) {
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
  //        break;
        }
      }
      else {
        // WAIT
        ThreadSpriteHolder threadSpriteHolder = getLastLocked();
        threadSpriteHolder.getSprite().setThreadState(Thread.State.WAITING);
        synchronized (this) {
          synchronized (MUTEX) {
            MUTEX.notifyAll();
          }
        }
      }
    }
  }

  private void lock() {
    setAnimationCanvasVisible(true);
    setState(1);
    message1("Waiting for lock...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    ConcurrentSprite sprite = createAcquiringSprite();
    sprite.setThreadState(Thread.State.BLOCKED);
    synchronized (lock) {
      sprite.setThreadState(Thread.State.RUNNABLE);
      lockCount++;
      sprite.setAcquired();
      lockedSpriteList.add(new ThreadSpriteHolder(Thread.currentThread(), sprite));
      setAcquiredSprite(sprite);
      message1("Acquired", ConcurrentExampleConstants.MESSAGE_COLOR);
      waitForUnlockNotification(sprite);
    }
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
        if (notifyTracker == NotifyTracker.UNLOCK) {
          sprite.setRejected();
          message1("Interrupted", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
          Thread.currentThread().interrupt();
          setState(6);
        }
        else if (notifyTracker == NotifyTracker.WAIT) {
          synchronized (lock) {
            try {
              lock.wait();
            } catch (InterruptedException e1) {
              Thread.currentThread().interrupt();
            }
          }
        }
      }
      // todo: remove?
//      lockedSprite = null;
      lockCount--;
    }
  }


  private void unlock() {
//    setState(2);
    message2("Waiting for unlock ", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    synchronized(this) {
      synchronized(MUTEX) {
        MUTEX.notifyAll();
      }
      // remove the last sprite
      ThreadSpriteHolder acquiredSpriteHolder = getLastLocked();
      if (acquiredSpriteHolder != null) {
        ConcurrentSprite acquiredSprite = acquiredSpriteHolder.getSprite();
        if(acquiredSprite != null) {
          acquiredSprite.setReleased();
          setAcquiredSprite(null);
        }
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
//    lock = new ReentrantLock();
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
//    resetThreadCountField(threadCountField);
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
