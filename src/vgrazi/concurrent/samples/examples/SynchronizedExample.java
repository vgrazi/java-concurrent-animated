package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;
import vgrazi.concurrent.samples.sprites.ThreadStateToColorMapper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SynchronizedExample extends ConcurrentExample {

  //  private final JButton launchWaitButton = new JButton("Launch wait thread");
  private final JButton launchLockButton = new JButton("synchronized");
  private final JButton lockWaitButton = new JButton("wait");
  private final JButton releaseLockButton = new JButton("exit synchronized");
  private final JButton notifyButton = new JButton("notify");
  private final JButton notifyAllButton = new JButton("notifyAll");
  private final JButton interruptButton = new JButton("interrupt");
  //  private final JLabel reportLabel = new JLabel();
  private final AtomicInteger threadCounter = new AtomicInteger(0);
  private List<ConcurrentSprite> sprites = new ArrayList<ConcurrentSprite>();


  /**
   * This is the real mutex,
   */
  private static final Object MAIN_MUTEX = new Object();
  /**
   * This is a utility mutex, used to keep the lock from exiting the synchronized block
   */
  private static final Object UTILITY_MUTEX = new Object();

  private boolean initialized = false;

  /**
   * This is interrogated by a waking locked thread.
   * Depending on the value, performs the following actions:<br/>
   * 0: wait<br/>
   * 1: notify<br/>
   * 2: notifyAll<br/>
   * 3: release lock<br/>
   */
  private int notificationState;

  /**
   * @param title               the title to display in the title bar
   * @param container           the container to contain the animation
   * @param exampleType         the type of animation
   * @param snippetMinimumWidth the horizontal position to start the snippet frame
   * @param fair                true
   * @param slideNumber         when configured as a slide show, this indicates the slide number. -1 for exclude from slide show - will still show in menu bar
   */
  public SynchronizedExample(String title, Container container, ExampleType exampleType, int snippetMinimumWidth, boolean fair, int slideNumber) {
    super(title, container, exampleType, snippetMinimumWidth, fair, slideNumber);
  }

  public SynchronizedExample(String label, Container frame, int slideNumber) {
    super(label, frame, ExampleType.BLOCKING, 552, true, slideNumber);
  }

  @Override
  public String getDescriptionHtml() {
    return "";
  }

  @Override
  protected String getSnippet() {
    return "";
  }

  @Override
  protected void initializeComponents() {
    if (!initialized) {
      initializeButton(launchLockButton, new Runnable() {
        public void run() {
          setAnimationCanvasVisible(true);
//          launchThreadStateColorThread();
          launchLockThread(0);
          setState(1);
        }
      });

      initializeButton(releaseLockButton, new Runnable() {
        @Override
        public void run() {
          synchronized (UTILITY_MUTEX) {
            notificationState = 3;
            UTILITY_MUTEX.notify();
          }
//          displayReport();
        }
      });

//      todo: interrupt should transition waiting threads to blocked state
      initializeButton(interruptButton, new Runnable() {
        @Override
        public void run() {
          if (!sprites.isEmpty()) {
            ConcurrentSprite sprite = sprites.get(0);
            sprite.getThread().interrupt();
            sprite.setThreadState(Thread.State.BLOCKED);
            sprite.setColor(ThreadStateToColorMapper.getColorForState(Thread.State.BLOCKED));
            resetSpriteThreadStates();
          }
        }
      });
      addButtonSpacer();
      initializeButton(lockWaitButton, new Runnable() {
        @Override
        public void run() {
          // get rid of any existing lock

          synchronized (UTILITY_MUTEX) {
            notificationState = 0;
            UTILITY_MUTEX.notify();
          }

        }
      });

      initializeButton(notifyButton, new Runnable() {
        @Override
        public void run() {
          // get rid of any existing lock

          synchronized (UTILITY_MUTEX) {
            notificationState = 1;
            UTILITY_MUTEX.notify();
          }

        }
      });

      initializeButton(notifyAllButton, new Runnable() {
        @Override
        public void run() {
          // get rid of any existing lock
          synchronized (UTILITY_MUTEX) {
            notificationState = 2;
            UTILITY_MUTEX.notify();
          }
        }
      });

      initialized = true;
    }
  }

  /**
   * @param notification 0 is nothing
   *                     1 is notify
   *                     2 is notifyAll
   */
  private void launchLockThread(final int notification) {

    final Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        final ConcurrentSprite sprite = createAcquiringSprite();
        sprites.add(sprite);
        sprite.setThreadState(Thread.State.BLOCKED);
        synchronized (MAIN_MUTEX) {
          sprite.setAcquired();
          sprite.setThreadState(Thread.State.RUNNABLE);
          // note: Calls to notify and notifyAll also end up here (notification == 1 and 2 respectively. Detect those.
          if (notification > 0) {
            if (notification == 1) {
              MAIN_MUTEX.notify();
            } else if (notification == 2) {
              MAIN_MUTEX.notifyAll();
            }
          }
          int state = 0;
          // forgive me Edsger Dijkstra, for my goto. This code was too hairy to think about it.
          unlockAll:
          try {
            while (true) {
              // this state is only to capture the wait state. When wait is called, we need to exit the utility lock
              // and grab the main lock.
              if (state == 1) {
                state = 0;
                try {

                  sprite.setThreadState(Thread.State.WAITING);
                  MAIN_MUTEX.wait();
                  sprite.setThreadState(Thread.State.RUNNABLE);

                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  rejectSprite(sprite);
                }
              }
              synchronized (UTILITY_MUTEX) {
                resetSpriteThreadStates();

                UTILITY_MUTEX.wait();

                // we received a notification. Check notificationState and act accordingly
                switch (notificationState) {
                  case 0:

                    state = 1;
                    break;
                  case 1:

                    MAIN_MUTEX.notify();
                    break;
                  case 2:

                    MAIN_MUTEX.notifyAll();
                    break;
                  case 3:

                    releaseSprite(sprite);
                    sprite.setColor(ThreadStateToColorMapper.getColorForState(Thread.State.TERMINATED));
                    sprites.remove(sprite);
                    // exit lock
                    break unlockAll;
                }
              }
            }
          } catch (InterruptedException e) {
            rejectSprite(sprite);
            Thread.currentThread().interrupt();
          }
        }

      }
    }, "Locked Thread " + threadCounter.incrementAndGet());
    thread.start();
  }

  private void releaseSprite(ConcurrentSprite sprite) {
    sprite.setReleased();
    sprite.setThreadState(Thread.State.TERMINATED);
    sprite.setColor(ThreadStateToColorMapper.getColorForState(Thread.State.TERMINATED));
  }

  private void rejectSprite(ConcurrentSprite sprite) {
    sprite.setRejected();
    sprite.setThreadState(Thread.State.TERMINATED);
    sprite.setColor(ThreadStateToColorMapper.getColorForState(Thread.State.TERMINATED));
  }

  /**
   * Sets the states of any blocked sprites to BLOCKED
   */
  private void resetSpriteThreadStates() {
    for (ConcurrentSprite sprite : sprites) {
      if (sprite.getThread().getState() == Thread.State.BLOCKED) {
        sprite.setThreadState(Thread.State.BLOCKED);
        sprite.setColor(ThreadStateToColorMapper.getColorForState(sprite));
      }
    }
  }

  @Override
  public void reset() {
    for (ConcurrentSprite sprite: sprites) {
      sprite.getThread().interrupt();
    }
    sprites.clear();
  }
}
