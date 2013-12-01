package vgrazi.concurrent.samples.examples;

import jsr166e.StampedLock;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.logging.Logger;

//import java.util.concurrent.locks.StampedLock;

/**
 * Notes:
 * ReadLock is obtained from long stamp = lock.readLock() and released from lock.unlockRead(stamp)
 * WriteLock is obtained from long stamp = lock.writeLock() and released from lock.unlockWrite(stamp)
 * Optimistic ReadLock is obtained from long stamp = lock.tryOptimisticRead(). Copy results to local variables, then call lock.validate(stamp)
 * If not valid, obtain a true read lock, then recopy the variables locally.
 * todo: BUG!! when optimistic twice, then writer, then release writer then validate twice, second validate blocks until read release is pressed
 * todo: verify thread state is indeed WAITING when acquiring a reader from an optimistic lock, if there is a writer
 */
public class StampedLockExample extends ConcurrentExample {
  private final static Logger logger = Logger.getLogger(StampedLockExample.class.getCanonicalName());
  private StampedLock lock;
  private final Object READ_LOCK_MUTEX = new Object();
  private final Object WRITE_LOCK_MUTEX = new Object();
  private final Object OPTIMISTIC_LOCK_MUTEX = new Object();
  private final Object OPTIMISTIC_LOCK_MUTEX1 = new Object();
  private volatile int readLockCount;
  private volatile int writeLockCount;
  private final JButton lockReadButton = new JButton("lock.readLock()");
  private final JButton lockWriteButton = new JButton("lock.writeLock()");
  private final JButton tryOptimisticReadButton = new JButton("lock.tryOptimisticRead()");
  private final JButton unlockReadButton = new JButton("lock.unlockRead()");
  private final JButton unlockWriteButton = new JButton("lock.unlockWrite()");
  private final JButton validateButton = new JButton("lock.validate()");
  private final Stack<Long> optimisticLockStamps = new Stack<Long>();
  private final Stack<ConcurrentSprite> optimisticLocks = new Stack<ConcurrentSprite>();

  private boolean initialized = false;
  private boolean writerOwned = false;
  private final JTextField threadCountField = createThreadCountField();

  public StampedLockExample(String label, Container frame, int slideNumber) {
    this(label, frame, false, slideNumber);
  }

  public StampedLockExample(String label, Container frame, boolean fair, int slideNumber) {
    super(label, frame, ExampleType.BLOCKING, 552, fair, slideNumber);
  }

  protected void initializeComponents() {
    if (!initialized) {
      initializeLockReadButton();
      initializeUnlockReadButton();
      addButtonSpacer();
      initializeLockWriteButton();
      initializeUnlockWriteButton();
      addButtonSpacer();
      initializeTryOptimisticReadButton();
      initializeValidateButton();
      initializeThreadCountField(threadCountField);

      Dimension size = new Dimension(200, 20);
      lockWriteButton.setPreferredSize(size);
      unlockReadButton.setPreferredSize(size);
      lockReadButton.setPreferredSize(size);
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
    return sb.toString();
  }

  private void initializeLockReadButton() {
    initializeButton(lockReadButton, new Runnable() {
      public void run() {
        setAnimationCanvasVisible(true);
        setState(1);
        int count = getThreadCount(threadCountField);
        for (int i = 0; i < count; i++) {
          threadCountExecutor.execute(new Runnable() {
            public void run() {
              readAcquire();
            }
          });
        }
      }
    });
  }

  private void initializeTryOptimisticReadButton() {
    initializeButton(tryOptimisticReadButton, new Runnable() {
      public void run() {
        setAnimationCanvasVisible(true);
        setState(1);
        int count = getThreadCount(threadCountField);
        for (int i = 0; i < count; i++) {
          threadCountExecutor.execute(new Runnable() {
            public void run() {
              tryOptimisticRead();
            }
          });
        }
      }
    });
  }

  private void readAcquire() {
    message1("Waiting to acquire READ lock", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);

//    logger.info("Acquiring read lock " + readLock);
    // create the sprite before locking, otherwise the thread won't appear if another thread has the lock
    final ConcurrentSprite sprite = createAcquiringSprite();
    sprite.setThreadState(Thread.State.WAITING);
    long stamp = lock.readLock();
    sprite.setThreadState(Thread.State.RUNNABLE);
    readLockCount++;
    sprite.setAcquired();
    message1("Acquired read lock ", ConcurrentExampleConstants.MESSAGE_COLOR);
    synchronized (READ_LOCK_MUTEX) {
      try {
        READ_LOCK_MUTEX.wait();
        logger.info("read waking");
        lock.unlock(stamp);
        readLockCount--;
        sprite.setReleased();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private void tryOptimisticRead() {
    message1("trying optimistic read", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);

//    logger.info("Acquiring read lock " + readLock);
    // create the sprite before locking, otherwise the thread won't appear if another thread has the lock
    ConcurrentSprite sprite = createAcquiringSprite();
    sprite.setOptimisticRead(true);
    sprite.setThreadState(Thread.State.RUNNABLE);
    sprite.setAcquired();
    message1("Acquired optimistic read lock ", ConcurrentExampleConstants.MESSAGE_COLOR);
    synchronized (OPTIMISTIC_LOCK_MUTEX) {
      try {
        optimisticLockStamps.push(lock.tryOptimisticRead());
        optimisticLocks.add(sprite);
        OPTIMISTIC_LOCK_MUTEX.wait();
        sprite = optimisticLocks.pop();
        optimisticLockStamps.pop();
        sprite.setReleased();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private void initializeLockWriteButton() {
    initializeButton(lockWriteButton, new Runnable() {
      public void run() {
        setState(3);
        int count = getThreadCount(threadCountField);
        for (int i = 0; i < count; i++) {
          threadCountExecutor.execute(new Runnable() {
            public void run() {
              writeAcquire();
            }
          });
        }
      }
    });
  }

  private void writeAcquire() {
    message1("Waiting to acquire WRITE lock", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    final ConcurrentSprite sprite = createSpecialHeadSprite();
    sprite.setColor(Color.RED);
    sprite.setThreadState(Thread.State.WAITING);
    long stamp = lock.writeLock();
    sprite.setThreadState(Thread.State.RUNNABLE);
    writeLockCount++;
    sprite.setAcquired();
    message1("Acquired write lock ", ConcurrentExampleConstants.MESSAGE_COLOR);
    try {
      synchronized (WRITE_LOCK_MUTEX) {
        writerOwned = true;
        WRITE_LOCK_MUTEX.wait();
        writerOwned = false;
        lock.unlock(stamp);
        sprite.setReleased();
        synchronized (OPTIMISTIC_LOCK_MUTEX1) {
          OPTIMISTIC_LOCK_MUTEX1.notify();
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void readRelease() {
    message1("Waiting to release READ lock...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    message2("  ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    synchronized (READ_LOCK_MUTEX) {
      setState(4);
      READ_LOCK_MUTEX.notify();
    }
    message1(" ", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
  }

  private void validateOptimisticLock() {
//    message2("  ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    synchronized (OPTIMISTIC_LOCK_MUTEX) {
      if (lock.validate(optimisticLockStamps.get(optimisticLockStamps.size() - 1))) {
        message1("VALID", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
        OPTIMISTIC_LOCK_MUTEX.notify();
        setState(4);
      } else {
        message1("NOT VALID", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
        validateButton.setVisible(true);
        if (JOptionPane.showConfirmDialog(this, "No Optimistic locks to validate. Try pessimistic?") == JOptionPane.YES_OPTION) {
          // whatever called the optimistic lock, should now acquire a pessimistic lock
          ConcurrentSprite sprite = optimisticLocks.get(optimisticLocks.size() - 1);
          sprite.setOptimisticRead(false);
          sprite.setThreadState(Thread.State.WAITING);
          long stamp = lock.readLock();
          if (writerOwned) {
            synchronized (OPTIMISTIC_LOCK_MUTEX1) {
              try {
                OPTIMISTIC_LOCK_MUTEX1.wait();
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            }
          }
          lock.unlockRead(stamp);
          message1(" ", Color.ORANGE);
          optimisticLocks.pop();
          optimisticLockStamps.pop();
          sprite.setThreadState(Thread.State.RUNNABLE);
          sprite.setAcquired();
          readLockCount++;
          synchronized (READ_LOCK_MUTEX) {
            try {
              READ_LOCK_MUTEX.wait();
              sprite.setReleased();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
        }
      }
    }
  }

  private void writeRelease() {
    message1("Waiting to release Write lock...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    message2("  ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    synchronized (WRITE_LOCK_MUTEX) {
      setState(4);
      WRITE_LOCK_MUTEX.notify();
    }
    message1(" ", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
  }

  private void initializeUnlockReadButton() {
    initializeButton(unlockReadButton, new Runnable() {
      public void run() {
        setState(2);
        if (readLockCount > 0) {
          readRelease();
        } else {
          message1("Un-held lock calling unlock", Color.red);
          message2("IllegalMonitorStateException thrown", Color.red);
        }
      }
    });
  }

  private void initializeValidateButton() {
    initializeButton(validateButton, new Runnable() {
      public void run() {
        setState(2);
        if (!optimisticLocks.isEmpty()) {
          message1("Validating", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
          validateOptimisticLock();
        } else {
          message1("No Optimistic locks to validate", Color.red);
        }
      }
    });
  }

  private void initializeUnlockWriteButton() {
    initializeButton(unlockWriteButton, new Runnable() {
      public void run() {
        setState(2);
        if (writeLockCount > 0) {
          writeRelease();
        } else {
          message1("Un-held lock calling unlock", Color.red);
          message2("IllegalMonitorStateException thrown", Color.red);
        }
      }
    });
  }

  @Override
  public void reset() {
    resetExample();
    lock = new StampedLock();
    resetThreadCountField(threadCountField);
    setState(0);
  }

  private void resetExample() {
    synchronized (READ_LOCK_MUTEX) {
      READ_LOCK_MUTEX.notifyAll();
    }
    synchronized (WRITE_LOCK_MUTEX) {
      WRITE_LOCK_MUTEX.notifyAll();
    }
    writerOwned = false;
    synchronized (OPTIMISTIC_LOCK_MUTEX) {
      OPTIMISTIC_LOCK_MUTEX.notifyAll();
    }
    synchronized (OPTIMISTIC_LOCK_MUTEX1) {
      OPTIMISTIC_LOCK_MUTEX1.notifyAll();
    }
    optimisticLocks.clear();
    optimisticLockStamps.clear();
    super.reset();
    message1("  ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    message2("  ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
  }

  protected String getSnippet() {
    return "";
  }
}
