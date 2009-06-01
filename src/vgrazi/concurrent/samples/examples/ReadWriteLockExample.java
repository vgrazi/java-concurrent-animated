package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;
import vgrazi.util.logging.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */
// todo: if a writer has the lock, and there are readers and writers waiting, downgrade (in JDK1.5) should release the readers. 
public class ReadWriteLockExample extends ConcurrentExample {
  private final static Logger logger = Logger.getLogger(ReadWriteLockExample.class.getCanonicalName());
  private ReadWriteLock lock;
  private final Object MUTEX = new Object();

  private final JButton readAcquireButton = new JButton("   lock.readLock().lock()    ");
  private final JButton readReleaseButton = new JButton("    unlock()   ");
  private final JButton writeAcquireButton = new JButton("   lock.writeLock().lock()   ");
  private final JButton writeDowngradeButton = new JButton("(Downgrade to read)");

  private boolean initialized = false;
  private static int minSnippetPosition = 390;
  private boolean downgrade = false;
  private boolean writerOwned = false;

  public ReadWriteLockExample(String label, Container frame, boolean fair, int slideNumber) {
    //    super(frame, buttonFrame, ExampleType.WORKING);
    super(label, frame, ExampleType.BLOCKING, minSnippetPosition, fair, slideNumber);
  }

  protected void initializeComponents() {
    if (!initialized) {
      initializeReadAcquireButton();
      initializeWriteAcquireButton();
      addButtonSpacer();
      initializeReadReleaseButton();
      initializeWriteDowngradeToReadButton();
      initialized = true;
    }
  }

  @Override
  protected void setDefaultState() {
    if (isFair()) {
      setState(6);
    } else {
      setState(0);
    }
  }

  public String getDescriptionHtml() {
    StringBuffer sb = new StringBuffer();
    //    sb.append("<html>");
    //    sb.append("<table border=\"0\"><tr><td>");
    //    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_FONT_COLOR + "\">");
    //
    //    sb.append("A <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>ReadWriteLock</code></font> synchronizes accesses to a resource ");
    //    sb.append("that allows many readers, but needs to lock out readers while writers are updating.<p><p>");
    //    sb.append("There are several varieties of ReadWriteLock:<ul>");
    //    sb.append("<li><font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>WriterPreferencReadWriteLock</code></font></li>");
    //    sb.append("<li><font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>ReaderPreferencReadWriteLock</code></font></li>");
    //    sb.append("<li><font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>ReentrantWriterPreferencReadWriteLock</code></font></li>");
    //    sb.append("<li><font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>FIFOReadWriteLock</code></font></li>");
    //    sb.append("</ul>");
    //    sb.append("To grab the read lock, call <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>ReadWriteLock.readLock().acquire()</code></font><p><p>");
    //    sb.append("To release the lock, call <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>ReadWriteLock.readLock().release()</code></font><p><p>");
    //    sb.append("To grab the write lock, call <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>ReadWriteLock.writeLock().acquire()</code></font><p><p>");
    //    sb.append("To release the write lock, call <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>ReadWriteLock.writeLock().release()</code></font><p><p>");
    //    sb.append("Many threads can access the read lock as long as no thread has the write lock. Only one thread can have the write lock. Calls to acquire either lock will block until the lock is available.");
    //
    //    sb.append("</td></tr></table>");
    //    sb.append("</html>");
    //
    return sb.toString();
  }

  private void initializeReadAcquireButton() {
    initializeButton(readAcquireButton, new Runnable() {
      public void run() {
        setAnimationCanvasVisible(true);
        setState(1);
        readAcquire();
      }
    });
  }

  private void readAcquire() {
    message1(new Date() + " Waiting to acquire READ lock", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    final ConcurrentSprite sprite = createAcquiringSprite();

    Lock readLock = lock.readLock();
    logger.info("Acquiring read lock " + readLock);
    readLock.lock();
    writerOwned = false;
    sprite.setAcquired();
    message1(new Date() + " Acquired read lock ", ConcurrentExampleConstants.MESSAGE_COLOR);
    synchronized (MUTEX) {
      try {
        MUTEX.wait();
        logger.info("read waking");
        readLock.unlock();
        sprite.setReleased();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private void initializeWriteAcquireButton() {
    initializeButton(writeAcquireButton, new Runnable() {
      public void run() {
        setState(3);
        writeAcquire();
      }
    });
  }

  private void writeAcquire() {
    message2(new Date() + " Waiting to acquire WRITE lock", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    final ConcurrentSprite sprite = createAcquiringSprite();
    Lock writeLock = lock.writeLock();
    sprite.setColor(Color.RED);
    writeLock.lock();
    sprite.setAcquired();
    message2(new Date() + " Acquired write lock ", ConcurrentExampleConstants.MESSAGE_COLOR);
    try {
      synchronized (MUTEX) {
        writerOwned = true;
        MUTEX.wait();
        if (downgrade) {
          // Grab a read lock - since this is reentrant and we own the write lock, this will pass.
          lock.readLock().lock();

          // We have the read lock, release the write lock
          writeLock.unlock();

          // housekeeping to reset the animation as a read lock
          downgrade = writerOwned = false;
          
          // convert our color to a read lock
          sprite.setColor(ConcurrentExampleConstants.ACQUIRING_COLOR);

          // now that we are a read lock, all read locks can enter. Notify to try again.
          MUTEX.notify();

          // now we wait, along with the other readers.
          MUTEX.wait();

          // we are no longer waiting, release our lock
          lock.readLock().unlock();
        } else {
          writeLock.unlock();
        }
        sprite.setReleased();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void writeDowngradeToRead() {
    if (writerOwned) {
      setState(5);
      message2(new Date() + " Waiting to Downgrade WRITE lock...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      downgrade = true;
      synchronized (MUTEX) {
        MUTEX.notify();
      }
    }
  }

  private void readRelease() {
    message1(new Date() + " Waiting to release READ lock...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    synchronized (MUTEX) {
      setState(4);
      downgrade = false;
      MUTEX.notify();
    }
    message1(".", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
  }

  private void initializeReadReleaseButton() {
    initializeButton(readReleaseButton, new Runnable() {
      public void run() {
        setState(2);
        readRelease();
      }
    });
  }

  private void initializeWriteDowngradeToReadButton() {
    initializeButton(writeDowngradeButton, new Runnable() {
      public void run() {
        writeDowngradeToRead();
      }
    });
  }

  protected void reset() {
    resetExample();
    lock = new ReentrantReadWriteLock(isFair());
    setState(0);
  }

  private void resetExample() {
    synchronized (MUTEX) {
      downgrade = false;
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