package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import java.awt.*;

public class SynchronizedExample extends ConcurrentExample {
  /*
  LOCK is the actual lock being illustrated, represented by the monolith
  MUTEX helps control the animations. It is the lock held by the sprite, to prevent it from exiting the synchronized block.

 Lock
  •	synchronize on LOCK,
  •	wait on MUTEX

 Unlock
  •	Release MUTEX (next blocked thread will Lock)

 Wait
  •	There is a “Runnable” thread holding the lock, waiting on MUTEX.
  •	Call MUTEX.notify to transit the Runnable thread holding the lock to WAITING
  •	and LOCK.wait to allow the next thread in.

 Notify
  •	Notify LOCK to get the waiting thread to wake up.
  •	The notifying thread should set all waiting thread colors to BLOCKED then the running thread should change its color to RUNNABLE
  •	if there is another thread holding the lock
      o	transit to the blocked state
  •	else
      o	transit a waiting thread to runnable and let it exit the monolith

 NotifyAll
  •	Same as notify except call LOCK.notifyAll()
*/

  /**
   * LOCK is the actual lock being illustrated, represented by the monolith
   */
  private Object LOCK = new Object();

  /**
   * MUTEX helps control the animations. It is the lock held by the sprite, to prevent it from exiting the synchronized block.
   */
  private Object MUTEX = new Object();

  /**
   * A List of all Sprites holding the lock.
   */
  private List<ConcurrentSprite> lockedSpriteList = new ArrayList<ConcurrentSprite>();
  private List<ConcurrentSprite> waitingSpriteList = new ArrayList<ConcurrentSprite>();

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
  private final JButton interruptLockedButton = new JButton("interrupt (waiting)");
  private boolean initialized = false;

  public String getTitle() {
    return "synchronized";
  }

  @Override
  protected String getSnippetText() {
    return "";
  }

  public SynchronizedExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.BLOCKING, 570, false, slideNumber);
  }

  protected void initializeComponents() {
    if (!initialized) {
      initializeButton(lockButton, new Runnable() {
        public void run() {
          threadCountExecutor.execute(new Runnable() {
            public void run() {
              lock();
            }
          });
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
          waitMethod();
        }
      });
      addButtonSpacer();
      initializeButton(notifyButton, new Runnable() {
        @Override
        public void run() {
          notifyMethod();
          // note: When the notify button is pressed, then nothing happens until the locked sprite releases. At that point, one waiting sprite will revive. No blocked will enter
        }
      });
      initializeButton(interruptLockedButton, new Runnable() {
        public void run() {
          setState(0);
          if (!waitingSpriteList.isEmpty()) {
            ConcurrentSprite lockedSprite = waitingSpriteList.get(0);
            if (lockedSprite != null) {
              System.out.println("SynchronizedExample.run todo: lockedSprite.thread.interrupt();");
              setState(6);
            }
          }
        }
      });

      Dimension size = new Dimension(150, lockButton.getPreferredSize().height);
      lockButton.setPreferredSize(size);
      unlockButton.setPreferredSize(size);
      interruptLockedButton.setPreferredSize(size);
      initialized = true;
    }
  }

  private void unlockMethod() {
    setState(2);
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

  private void notifyMethod() {
    synchronized (LOCK) {
      for (ConcurrentSprite sprite : waitingSpriteList) {
        sprite.setThreadState(Thread.State.BLOCKED);
      }
      LOCK.notify();
    }
    // check if all threads are waiting, else we know one is running and holding the lock
    System.out.printf("SynchronizedExample.notifyMethod waiting list:%d locked list:%d%n", waitingSpriteList.size(), lockedSpriteList.size());
    // all threads holding the lock are waiting
    // transit a waiting thread to runnable, and let it exit
    if (!waitingSpriteList.isEmpty()) {

      System.out.println("SynchronizedExample.notifyMethod. ");
      if (lockedSpriteList.size() - waitingSpriteList.size() <2) {
        ConcurrentSprite sprite = waitingSpriteList.remove(0);
        //        lockedSpriteList.remove(sprite);
        synchronized (LOCK) {
          sprite.setThreadState(Thread.State.RUNNABLE);
          synchronized (MUTEX){
            try {
              MUTEX.wait();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
        }

        //        sprite.setReleased();
      }
      setNotifyButtonState();
    }
//    unlock();
    try {
      // sleep a little to give a chance for the locking thread to reach its target
      Thread.sleep(500);
    } catch (InterruptedException e) {
      message1(e.getMessage(), ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      System.out.println("ReentrantLockExample.unlockMethod interrupted");
      Thread.currentThread().interrupt();
    }
  }

  /**
   * The notify button can only be pressed if there is a thread holding the lock. Else disable
   */
  private void setNotifyButtonState() {
    notifyButton.setEnabled(lockedSpriteList.size() > waitingSpriteList.size());
  }

  private void waitMethod() {
    setState(2);
    // WAIT
    ConcurrentSprite sprite = getLockHolder();
    if (sprite != null) {
      sprite.setThreadState(Thread.State.WAITING);
      waitingSpriteList.add(sprite);
      setNotifyButtonState();
      synchronized (MUTEX) {
        MUTEX.notifyAll();
      }
      synchronized (LOCK) {
        LOCK.notifyAll();
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
    synchronized (LOCK) {
      sprite.setThreadState(Thread.State.RUNNABLE);
      sprite.setAcquired();
      lockedSpriteList.add(sprite);
      setNotifyButtonState();
      setAcquiredSprite(sprite);
      message1("Acquired", ConcurrentExampleConstants.MESSAGE_COLOR);
      waitForUnlockNotification(sprite);
    }
  }

  /**
   * Sits in a wait block until MUTEX.notify() is called by the locked thread.
   *
   * @param sprite the sprite that has acquired, waiting for unlock or interrupt
   */
  private void waitForUnlockNotification(ConcurrentSprite sprite) {
    try {
      synchronized (MUTEX) {
        MUTEX.wait();
        // notify the next sprite to transition the next RUNNABLE thread holding the lock to WAITING
        // todo: seems wrong!!!! MUTEX just received a notify, and we are sending another?
        MUTEX.notify();
      }

      // at this point, the user pressed Notify button, MUTEX was notified,
      // todo: Change state of one thread to BLOCKED
      synchronized (LOCK) {
        LOCK.notify();
      }
    } catch (InterruptedException e) {
      System.out.println("SynchronizedExample.waitForUnlockNotification interrupted");
      if (notifyTracker == NotifyTracker.UNLOCK) {
        sprite.setRejected();
        message1("Interrupted", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
        Thread.currentThread().interrupt();
        setState(6);
        waitingSpriteList.add(sprite);
        setNotifyButtonState();
      } else if (notifyTracker == NotifyTracker.WAIT) {
        synchronized (LOCK) {
          try {
            LOCK.wait();
            waitingSpriteList.add(sprite);
            setNotifyButtonState();
          } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
          }
          finally {
            waitingSpriteList.remove(sprite);
          }
        }
      }
    }
  }

  private void unlock() {
    //    setState(2);
    message2("Waiting for unlock ", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    synchronized (MUTEX) {
      MUTEX.notify();
    }
    // remove the last sprite
    ConcurrentSprite acquiredSprite = getLockHolder();
    if (acquiredSprite != null) {
      acquiredSprite.setReleased();
      lockedSpriteList.remove(acquiredSprite);
      setAcquiredSprite(null);
      setNotifyButtonState();
    }
    message2("Unlocked", ConcurrentExampleConstants.MESSAGE_COLOR);
//    setState(2);
  }

  private ConcurrentSprite getLockHolder() {
    System.out.printf("SynchronizedExample.getLastLocked locked:%d waiting:%d%n", lockedSpriteList.size(), waitingSpriteList.size());
    List<ConcurrentSprite> lockedSpriteListClone = new ArrayList<ConcurrentSprite>(lockedSpriteList);
    lockedSpriteListClone.removeAll(waitingSpriteList);
    ConcurrentSprite rval;
    if (!lockedSpriteListClone.isEmpty()) {
      rval = lockedSpriteListClone.get(0);
    } else {
      rval = null;
    }
    System.out.printf("SynchronizedExample.getLastLocked returning %s%n", rval);
    return rval;
  }

  public String getDescriptionHtml() {
    return "";
  }

  @Override
  public void reset() {
    LOCK = new Object();
    MUTEX = new Object();
    lockedSpriteList = new ArrayList<ConcurrentSprite>();
    waitingSpriteList = new ArrayList<ConcurrentSprite>();
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    setNotifyButtonState();
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
