package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;
import vgrazi.concurrent.samples.sprites.ConcurrentSpriteCanvas;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

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
  private final List<ConcurrentSprite> acquiredSprites = new ArrayList<ConcurrentSprite>();
  private static final int MIN_SNIPPET_POSITION = 320;
  private JTextField threadCountField = createThreadCountField();

  public SemaphoreExample(String title, Container frame, boolean fair, int slideNumber) {
    super(title, frame, ExampleType.BLOCKING, MIN_SNIPPET_POSITION, fair, slideNumber);
    if(fair) {
      initializeFair();
    }
    else {
      initializeNonFair();
    }
  }

  protected String getSnippet() {

    String snippet;
    snippet = "<html><PRE>\n" +
            "<font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            " \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Constructor - pass in the permit count</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>final</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> Semaphore semaphore = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> Semaphore(4); \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state6:#000080>\"><B>final</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state6:#000000>\"> Semaphore semaphore = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state6:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state6:#000000>\"> Semaphore(4, true); \n" +
            " \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Threads attempting to acquire will block</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// until the specified number of releases are counted</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\">Thread acquireThread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> Thread(</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> Runnable() { \n" +
            "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> run() { \n" +
            "        </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>try</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> { \n" +
            "          semaphore.acquire(); \n" +
            "        } </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>catch</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">(InterruptedException e) { }\n" +
            //       "          Thread.currentThread().interrupt(); \n" +
            //       "        } \n" +
            //       "      } \n" +
            //       "    }); \n" +
            " \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\">Thread releaseThread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> Thread(</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> Runnable() { \n" +
            "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> run() { \n" +
            "        semaphore.release(); \n" +
            "      } \n" +
            //       "    }); \n" +
            " \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// tryAcquire is like acquire except that it</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// times out after the specified timeout period</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\">Thread tryAcquire = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> Thread(</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> Runnable() { \n" +
            "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> run() { \n" +
            "        </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>try</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> { \n" +
            "          semaphore.tryAcquire(" + timeoutString + "</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">); \n" +
            "        } </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>catch</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">(InterruptedException e) { }\n" +
            //       "          Thread.currentThread().interrupt(); \n" +
            //       "        } \n" +
            //       "      } \n" +
            //       "    });" +
            "</FONT></PRE></html>";

    return snippet;
  }

  @Override
  public String getToolTipText() {
    return "<HTML>" +
            "<body>" +
            "Semaphore permits a specified fixed number<br>" +
            "of threads access.<br><br>" +
            "Once permits are exhausted, no other threads<br>" +
            "may acquire until holding threads release." +
            "</body>" +
            "</HTML>";
  }

  protected void initializeComponents() {
    reset();
    if (!initialized) {
      initializeButton(timedtryAcquireButton, new Runnable() {
        public void run() {
          timeoutString = "1000L, TimeUnit.MILLISECONDS";
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
      initializeThreadCountField(threadCountField);

      initialized = true;
    }

  }

  private void initializeNonFair() {
    reset();
    semaphore = new Semaphore(4, false);
  }

  private void initializeFair() {
    reset();
    setState(6);
    semaphore = new Semaphore(4, true);
  }

  private void release() {
    int index;
    if (!acquiredSprites.isEmpty()) {
      ConcurrentSprite sprite;
      if (semaphore.isFair()) {
        index = 0;
      }
      else {
        index = getRandomIndex();
      }
      sprite = acquiredSprites.remove(index);
      synchronized (sprite) {
        sprite.notify();
      }
    }
  }

  private void _release(ConcurrentSprite sprite) {
    setState(2);
    message2("Attempting release ", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    semaphore.release();
    shuffleSprites();
    sprite.setReleased();
    message2("Released ", ConcurrentExampleConstants.MESSAGE_COLOR);
    setState(2);
  }

  private void acquire() {
    try {
      message1("Waiting for acquire...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      setState(1);
      ConcurrentSprite sprite = createAcquiringSprite();
      semaphore.acquire();
      acquiredSprites.add(sprite);
      sprite.setAcquired();
      message1("Acquired", ConcurrentExampleConstants.MESSAGE_COLOR);
      setState(1);
      synchronized (sprite) {
        sprite.wait();
      }
      _release(sprite);
      acquiredSprites.remove(sprite);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void tryTimedAcquire() {
    try {
      setState(3);
      message1("Trying acquire..", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      ConcurrentSprite sprite = createAttemptingSprite();
      if (semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
        message1("Acquire succeeded", ConcurrentExampleConstants.MESSAGE_COLOR);
        sprite.setAcquired();
        setState(3);
        acquiredSprites.add(sprite);
        synchronized (sprite) {
          sprite.wait();
        }
        _release(sprite);
        acquiredSprites.remove(sprite);
      } else {
        Thread.sleep(ConcurrentSpriteCanvas.getTimeToAcquireBorder());
        message1("Acquire failed", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
        sprite.setRejected();
        setState(3);
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
        message1("Acquire succeeded", ConcurrentExampleConstants.MESSAGE_COLOR);
        sprite.setAcquired();
        setState(3);
        acquiredSprites.add(sprite);
        synchronized (sprite) {
          //          acquiredSprites.add(sprite);
          sprite.wait();
        }
        _release(sprite);
        acquiredSprites.remove(sprite);
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

  protected void reset() {
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
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
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

  // todo: this is being applied to the holding sprites instead of the waiting sprites. Modify to wait instead for waiting sprites.
  private int getRandomIndex() {
//    int index = random.nextInt(acquiredSprites.size());
//    System.out.println("SemaphoreExample.getRandomIndex size, index -> " + acquiredSprites.size() + "," + index);
//    return index;
    return 0;
  }

}
