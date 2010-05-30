package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;
import vgrazi.concurrent.samples.sprites.ConcurrentTextSprite;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */
public class LegendExample extends ConcurrentExample {
  private final static Logger logger = Logger.getLogger(LegendExample.class.getCanonicalName());
  private ReadWriteLock lock;
  private final Object MUTEX = new Object();
  int position = 0;
  private final JButton startButton = new JButton("Start Demo");

  private boolean initialized = false;
  private static int minSnippetPosition = 390;

  public LegendExample(String label, Container frame, int slideNumber) {
    super(label, frame, ExampleType.BLOCKING, minSnippetPosition, false, slideNumber);
  }

  protected void initializeComponents() {
    if (!initialized) {
      initializeStartButton();
      initialized = true;
    }
  }

  @Override
  protected void setDefaultState() {
  }

  public String getDescriptionHtml() {
    return "";
  }

  private void initializeStartButton() {
    initializeButton(startButton, new Runnable() {
      public void run() {
        setAnimationCanvasVisible(true);
        startDemo();
        setState(1);
      }
    });
  }

  ConcurrentSprite[] sprites = new ConcurrentSprite[100];
  private void startDemo() {
    switch (position) {
      case 0:
        // todo: add justification - left, right, center.
        sprites[0] = createTextSprite("This is");
        sprites[6] = createTextSprite("a block");
        sprites[7] = createTextSprite("of some");
        sprites[8] = createTextSprite("sort.");
        sprites[0].setAcquired();
        sprites[6].setAcquired();
        sprites[7].setAcquired();
        sprites[8].setAcquired();
        position++;
        break;
      case 1:
        sprites[1] = createTextSprite("Threads are");
        sprites[2] = createTextSprite("represented");
        sprites[3] = createTextSprite("as arrows");
        sprites[4] = createAcquiringSprite();
        position++;
        break;
      case 2:
        sprites[4].setAcquired();
        sprites[5] = createTextSprite("Acquired");
        sprites[5].setAcquired();
        sprites[9] = createTextSprite("the lock");
        sprites[9].setAcquired();
        position++;
        break;
      case 3:
        ((ConcurrentTextSprite) sprites[5]).setText("Released");
        ((ConcurrentTextSprite) sprites[9]).setText("");
        sprites[5].setReleased();
        sprites[9].setReleased();
        sprites[4].setReleased();
        position++;
        break;
      case 4:
        resetExample();
        sprites[11] = createTextSprite("A Runnable");
        sprites[12] = createAcquiringSprite(ConcurrentSprite.SpriteType.RUNNABLE);
        position++;
        break;
      case 5:
        sprites[11] = createTextSprite("Working");
        sprites[11].setAcquired();
        sprites[12].setAcquired();
        position++;
        break;
      case 6:
        ((ConcurrentTextSprite) sprites[11]).setText("Released");
        sprites[11].setReleased();
        sprites[12].setReleased();
        position++;
        break;
      case 7:
        resetExample();
        sprites[15] = createTextSprite("Any object");
        sprites[16] = createAcquiringSprite();
        sprites[16].setType(ConcurrentSprite.SpriteType.OVAL);
        position++;
        break;
      case 8:
        sprites[17] = createTextSprite("Queued");
        sprites[16].setAcquired();
        position++;
        break;
      case 9:
        ((ConcurrentTextSprite) sprites[17]).setText("Released");
        sprites[16].setReleased();
        sprites[17].setReleased();
        position++;
        break;
    }
  }

  protected void reset() {
    resetExample();
//    lock = new ReentrantReadWriteLock(isFair());
    position = 0;
  }

  private void resetExample() {
    synchronized (MUTEX) {
      MUTEX.notifyAll();
    }
    synchronized (MUTEX) {
      MUTEX.notifyAll();
    }
    super.reset();
    message1("  ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    message2("  ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
  }

  protected String getSnippet() {
    String snippet;
    snippet = "<html>" +
            "<PRE> " +
            "    <FONT style=\"font-family:monospaced;\" COLOR=\"#606060\"><I>// Construct the ReadWriteLock</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>final</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> ReadWriteLock lock =  \n" +
            "        </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>new</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> ReentrantReadWriteLock(); \n" +
            "        </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state6:#000080>\"><B>new</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state6:#000000>\"> ReentrantReadWriteLock(true); \n" +
            //       "        </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>new</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> WriterPreferenceReadWriteLock(); \n" +
            //       "        </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>new</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> ReaderPreferenceReadWriteLock(); \n" +
            //       "        </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>new</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> FIFOReadWriteLock(); \n" +
            "     \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// Acquire the read lock</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>try</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> { \n" +
            "      lock.readLock().lock(); \n" +
            "      </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// or</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> \n" +
            "      lock.readLock().tryLock(</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000099>\">1000L, TimeUnit.MILLISECONDS</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">); \n" +
            "    }" +
            " </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>catch</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">(InterruptedException e) { }\n" +
            //            "      Thread.currentThread().interrupt(); \n" +
            //            "    } \n" +
            " \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// Release the read lock</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> \n" +
            "    lock.readLock().unlock(); \n" +
            " \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// Acquire the write lock</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>try</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> { \n" +
            "      lock.writeLock().lock(); \n" +
            "      </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// or</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> \n" +
            "      lock.writeLock().tryLock(</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000099>\">1000L, TimeUnit.MILLISECONDS</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">); \n" +
            "    }" +
            " </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>catch</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">(InterruptedException e) { }\n" +
            //            "    } \n" +
            " \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state4:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// Release the lock</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state4:#000000>\"> \n" +
            "    lock.writeLock().unlock(); \n" +
            "    // or \n" +
            "    lock.readLock().unlock(); \n" +
            " \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state5:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// Downgrade the write lock</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state5:#000080>\"><B>try</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state5:#000000>\"> { \n" +
            "      lock.readLock().lock(); \n" +
            "      lock.writeLock().unlock();\n" +
            "    }" +
            " </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state5:#000080>\"><B>catch</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state5:#000000>\">(InterruptedException e) {} \n" +
            //            "    } \n" +
            " \n" +
            "</FONT>" +
            "</PRE></html>";
    return snippet;
  }
}