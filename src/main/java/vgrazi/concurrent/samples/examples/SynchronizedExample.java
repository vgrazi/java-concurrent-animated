package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.canvases.BasicCanvas;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;
import vgrazi.concurrent.samples.sprites.ThreadStateToColorMapper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//todo: Enable/Disable buttons based on state
public class SynchronizedExample extends ConcurrentExample {

  //  private final JButton launchWaitButton = new JButton("Launch wait thread");
  private final JButton launchLockButton = new JButton("synchronized");
  private final JButton lockWaitButton = new JButton("wait");
  private final JButton releaseLockButton = new JButton("exit synchronized");
  private final JButton notifyButton = new JButton("notify");
  private final JButton notifyAllButton = new JButton("notifyAll");
  private final JButton interruptRunningButton = new JButton("interrupt running");
  private final JButton interruptWaitingButton = new JButton("interrupt waiting");
  //  private final JLabel reportLabel = new JLabel();
  private final AtomicInteger threadCounter = new AtomicInteger(0);
  private ConcurrentSprite lockedThread;
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
    protected void createCanvas() {
        setCanvas(new BasicCanvas(this, getTitle()));
    }

    @Override
  public String getDescriptionHtml() {
    return "";
  }

  @Override
  protected String getSnippetText() {
    return  "  <1 keyword>synchronized <1 default>(object) {\n" +
            "    . . .\n" +
            "  \n" +
            "    // do some work\n" +
            "  \n" +
            "    . . .          \n" +
            "    <2 default>// releasing lock\n" +
            "  }\n" +
            "  \n" +
            "  <3 keyword>synchronized <3 default>(object) {\n" +
            "    <3 keyword>try {\n" +
            "      <3 default>object.wait();\n" +
            "    } <3 keyword>catch <3 default>(InterruptedException e) {\n" +
            "      Thread.currentThread().interrupt();\n" +
            "    }\n" +
            "  }\n" +
            "  \n" +
            "  <4 keyword>synchronized <4 default>(object) {\n" +
            "    object.notify();\n" +
            "  }\n" +
            "  \n" +
            "  <5 keyword>synchronized <5 default>(object) {\n" +
            "    object.notifyAll();\n" +
            "  }\n" +
            "  \n" +
            "  <6 default>thread.interrupt();";
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
            setState(2);
          }
        }
      });

//      todo: interrupt should transition waiting threads to blocked state
      initializeButton(interruptRunningButton, new Runnable() {
        @Override
        public void run() {
          ConcurrentSprite sprite = null;
          if (!sprites.isEmpty()) {
            if(lockedThread != null) {
              sprite = lockedThread;
              sprite.setRejected();
            }
            if (sprite != null) {
              sprite.getThread().interrupt();
              sprite.setColor(ThreadStateToColorMapper.getColorForState(Thread.State.BLOCKED));
              resetSpriteThreadStates();
              setState(6);
            }
          }
        }
      });

//      initializeButton(interruptWaitingButton, new Runnable() {
//        @Override
//        public void run() {
//          // find a sprite with state not blocked
//          for (int i = 0; i < sprites.size(); i++) {
//            ConcurrentSprite sprite = sprites.get(i);
//            if(sprite != lockedThread && sprite.getThread().getState() != Thread.State.BLOCKED) {
//              System.out.println("SynchronizedExample.run FOUND A WAITING SPRITE");
//              sprite.getThread().interrupt();
//              sprite.setThreadState(Thread.State.BLOCKED);
//              sprite.setColor(ThreadStateToColorMapper.getColorForState(Thread.State.BLOCKED));
//              setState(6);
//              resetSpriteThreadStates();
//              break;
//            }
//          }
//        }
//      });
      addButtonSpacer();
      initializeButton(lockWaitButton, new Runnable() {
        @Override
        public void run() {
          // get rid of any existing lock
          setState(3);
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
          setState(4);
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
          setState(5);
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
          lockedThread = sprite;
          if (!sprite.getThread().isInterrupted()) {
            sprite.setAcquired();
            sprite.setThreadState(Thread.State.RUNNABLE);
          }
          else {
            sprite.setRejected();
            sprite.setThreadState(Thread.State.TERMINATED);
          }
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
                  lockedThread = null;
                  sprite.setThreadState(Thread.State.WAITING);
                  MAIN_MUTEX.wait();
                  lockedThread = sprite;
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
    // use a for loop, not for each, to prevent concurrent modification exception
    for (int i = 0; i < sprites.size(); i++) {
      ConcurrentSprite sprite = sprites.get(i);
      if (sprite.getThread().getState() == Thread.State.BLOCKED) {
        sprite.setThreadState(Thread.State.BLOCKED);
        sprite.setColor(ThreadStateToColorMapper.getColorForState(sprite));
      }
    }
    setButtonState();
  }

  /**
   * Enables and disables buttons according to context
   */
  private void setButtonState() {
//    if(sprites.isEmpty()) {
//      // if there are no threads, disable everything except lock
//      if(sprites.isEmpty()) {
//        lockWaitButton.setEnabled(false);
//        releaseLockButton.setEnabled(false);
//        notifyButton.setEnabled(false);
//        notifyAllButton.setEnabled(false);
//      }
//      else if(lockedThread == null) {
//        // if there is no locked thread, disable notify and notifyAll
//        lockWaitButton.setEnabled(false);
//        releaseLockButton.setEnabled(false);
//        notifyButton.setEnabled(false);
//        notifyAllButton.setEnabled(false);
//      } else {
//        // otherwise, enable everything
//        lockWaitButton.setEnabled(true);
//        releaseLockButton.setEnabled(true);
//        notifyButton.setEnabled(true);
//        notifyAllButton.setEnabled(true);
//      }
//    }
  }

  @Override
  public void reset() {
    for (ConcurrentSprite sprite: sprites) {
      sprite.getThread().interrupt();
    }
    setState(0);
    sprites.clear();
    setButtonState();
  }
}
