package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;
import vgrazi.concurrent.samples.canvases.ConcurrentSpriteCanvas;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;
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
 * number of permits.<br>
 * Note that acquiring threads are not awarded their acquires in order of arrival, unless the Semaphore was
 * instantiated with the fair = true parameter.
 */
public class SemaphoreExample extends ConcurrentExample {

  private Semaphore semaphore;


  private final JButton timedtryAcquireButton = new JButton("tryAcquire(timeoutMS, TimeUnit.MILLISECONDS)");
  private final JButton acquireButton = new JButton("acquire");
  private final JButton releaseButton = new JButton("release");
  private final JButton immediatetryAcquireButton = new JButton("tryAcquire()");
  private boolean initialized = false;
  private String timeoutString = "";
  // todo: when you create more threads than permits, they don't always release
  private final List<ConcurrentSprite> acquiredSprites = new Vector<ConcurrentSprite>();
  private static final int MIN_SNIPPET_POSITION = 650;
  private JTextField threadCountField = createThreadCountField();

  public SemaphoreExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.BLOCKING, MIN_SNIPPET_POSITION, true, slideNumber);
  }

  public SemaphoreExample(String title, Container frame, boolean fair, int slideNumber) {
    super(title, frame, ExampleType.BLOCKING, MIN_SNIPPET_POSITION, fair, slideNumber);
    initializeFair(fair);
  }

  protected  String getSnippetText() {
      String snippet =
  "    <6 comment>// Constructor - pass in the number of permits\n" +
  "    <6 keyword>final <6 default>Semaphore semaphore = <6 keyword>new<6 default> Semaphore(<6 literal>4,<6 keyword> true<6 default>);\n" +
  "    <0 keyword>final <0 default>Semaphore semaphore = <0 keyword>new<0 default> Semaphore(<0 literal>4<6 default>);\n" +
  "\n" +
  "    <1 comment>// Threads attempting to acquire will block\n" +
  "    // until the specified number of releases are counted\n" +
  "    <1 keyword>try <1 default>{\n" +
  "      semaphore.acquire();\n" +
  "    }<1 keyword> catch <1 default>(InterruptedException e) { }\n" +
  "\n" +
  "    <2 default>semaphore.release();\n" +
  "\n" +
  "    <4 comment>// tryAcquire is like acquire, except that it\n" +
  "    // times out after an (optional) specified time.\n" +
  "    <4 keyword>try<4 default> {\n" +
  "    <4 keyword>  if<4 default>(semaphore.tryAcquire(<4 literal>5<4 default>, TimeUnit.SECONDS)) {\n" +
  "    <4 comment>    // Do something\n" +
  "    <4 default>  }\n" +
  "    } <4 keyword><4 keyword>catch<4 default> (InterruptedException e) { }\n" +
  "\n" +
  "    <3 comment>// If no time is specified, times out immediately\n" +
  "    <3 comment>//    if not acquired\n" +
  "    <3 keyword>if<3 default>(semaphore.tryAcquire()) {\n" +
  "    <3 comment>  // Do something\n" +
  "    <3 default>}\n";
    return snippet;
  }

  @Override
  public String getToolTipText() {
    return "<HTML>" +
            "<body>" +
            "Semaphore permits a specified fixed number<br>" +
            "of threads access.<br><br>" +
            "Once permits are exhausted, no other threads<br>" +
            "may acquire until holding threads release.<br><br>" +
            "Unfair mode selects waiting threads randomly when permits becomes available.<br>" +
            "Fair mode selects waiting threads in the order they arrived." +
            "</body>" +
            "</HTML>";
  }

  protected void initializeComponents() {
    reset();
    if (!initialized) {
        initializeButton(acquireButton, new Runnable() {
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
        initializeButton(releaseButton, new Runnable() {
          public void run() {
            int count = getThreadCount(threadCountField);
            for (int i = 0; i < count; i++) {
              threadCountExecutor.execute(new Runnable() {
                public void run() {
                  release();
                }
              });
            }
          }
        });
      initializeButton(immediatetryAcquireButton, new Runnable() {
        public void run() {
          timeoutString = "";
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                tryUntimedAcquire();                
              }
            });
          }
        }
      });
      addButtonSpacer();
        initializeButton(timedtryAcquireButton, new Runnable() {
          public void run() {
            timeoutString = "1L, TimeUnit.SECONDS";
            int count = getThreadCount(threadCountField);
            for (int i = 0; i < count; i++) {
              threadCountExecutor.execute(new Runnable() {
                public void run() {
                  tryTimedAcquire();
                }
              });
            }
          }
        });
      initializeThreadCountField(threadCountField);

      initialized = true;
    }
  }

  private void initializeFair(boolean fair) {
    reset();
    setState(6);
    semaphore = new Semaphore(4, fair);
  }

  /**
   * process release button action
   */
  private void release() {
    ConcurrentSprite sprite = null;
    synchronized (this) {
      if (!acquiredSprites.isEmpty()) {
        sprite = acquiredSprites.remove(0);
      }
    }
    if (sprite != null) {
      synchronized (sprite) {
        sprite.notify();
      }
    }
    semaphore.release();
    displayPermits();
    setState(2);
  }

  private void _release(ConcurrentSprite sprite, boolean setState) {
    sprite.setReleased();
    message1("Released ", ConcurrentExampleConstants.MESSAGE_COLOR);
    if (setState) {
      setState(2);
    }
  }

  private void acquire() {
    try {
      message1("Waiting for acquire...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      setState(1);
      ConcurrentSprite sprite = createAcquiringSprite();
      semaphore.acquire();
      displayPermits();
      synchronized (this) {
        acquiredSprites.add(sprite);
      }
      sprite.setAcquired();
      message1("Acquired", ConcurrentExampleConstants.MESSAGE_COLOR);
      // the sprite has been acquired by the permit. Wait now for a release message
      synchronized (sprite) {
        sprite.wait();
      }
      _release(sprite, false);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void tryTimedAcquire() {
    try {
      setState(4);
      message1("Trying acquire..", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      ConcurrentSprite sprite = createAttemptingSprite();
      if (semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
        displayPermits();
        message1("Acquire succeeded", ConcurrentExampleConstants.MESSAGE_COLOR);
        sprite.setAcquired();
        synchronized (this) {
          acquiredSprites.add(sprite);
        }
        // the sprite has been acquired by the permit. Wait now for a release message
        synchronized (sprite) {
          sprite.wait();
        }
        _release(sprite, false);
      } else {
        Thread.sleep(ConcurrentSpriteCanvas.getTimeToAcquireBorder());
        message1("Acquire failed", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
        sprite.setRejected();
        setState(4);
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void tryUntimedAcquire() {
    setState(3);
    timeoutString = "";
    message1("Trying acquire..", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    ConcurrentSprite sprite = createAttemptingSprite();
    try {
      if (semaphore.tryAcquire()) {
        displayPermits();
        message1("Acquire succeeded", ConcurrentExampleConstants.MESSAGE_COLOR);
        sprite.setAcquired();
        setState(3);
        synchronized (this) {
          acquiredSprites.add(sprite);
        }
        // the sprite has been acquired by the permit. Wait now for a release message
        synchronized (sprite) {
          sprite.wait();
        }
        _release(sprite, false);
      } else {
        Thread.sleep(ConcurrentSpriteCanvas.getTimeToAcquireBorder());
        message1("Acquire failed", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
        sprite.setRejected();
        setState(3);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public String getDescriptionHtml() {
    StringBuffer sb = new StringBuffer();
    return sb.toString();
  }

  private void displayPermits() {
    message2(String.format("Available permits:%d of %d", semaphore.availablePermits(), 4), Color.white);
  }
  @Override
  public void reset() {
    super.reset();
    semaphore = new Semaphore(4, isFair());
    for (ConcurrentSprite sprite : acquiredSprites) {
      synchronized (sprite) {
        sprite.notify();
      }
    }
    acquiredSprites.clear();
    resetThreadCountField(threadCountField);
    message1(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    displayPermits();
    setState(6);
  }

  @Override
  protected void setDefaultState() {
    if (isFair()) {
      setState(6);
    }
    else {
      setState(0);
    }    
  }
}
