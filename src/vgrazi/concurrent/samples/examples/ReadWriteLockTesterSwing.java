package vgrazi.concurrent.samples.examples;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;


public class ReadWriteLockTesterSwing {

  private ReentrantReadWriteLock rwlock;
  private final JTextArea text = new JTextArea();
  private static final Object READ_MUTEX = new Object();
  private static final Object WRITE_MUTEX = new Object();
  private final JPanel buttons = new JPanel(new GridLayout(1, 6));
  // create a thread pool for launching
  private final ExecutorService pool = Executors.newCachedThreadPool();
  public static void main(String[] args) {
    new ReadWriteLockTesterSwing();
  }

  public ReadWriteLockTesterSwing() {
    JFrame frame = new JFrame("ReadWriteLock tester");
    frame.setBounds(100, 100, 1000, 600);
    JPanel panel = new JPanel(new BorderLayout());
    frame.getContentPane().add(BorderLayout.CENTER, panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    panel.add(BorderLayout.NORTH, buttons);
    panel.add(BorderLayout.CENTER, text);

    // start with a default rw lock
    createDefaultRWLock();

    // clears the text area
    createButton("clear", new Runnable() {
      public void run() {
        text.setText("");
      }
    });

    // user can toggle the RWLock - this creates default (unfair)
    createButton("construct default", new Runnable() {
        public void run() {
          createDefaultRWLock();
        }
    });

    // user can toggle the RWLock - this creates fair)
    createButton("construct fair", new Runnable() {
      public void run() {
        createFairRWLock();
      }
    });

    // acquire a read lock as soon as it is available
    createButton("readLock", new Runnable() {
      public void run() {
        rwlock.readLock().lock();
        text.append(">>>> read lock acquired" + status());
        synchronized (READ_MUTEX) {
          try {
            READ_MUTEX.wait();
            rwlock.readLock().unlock();
            text.append("<<<< read lock released" + status());
          } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
          }
        }
      }
    });

    // acquire a write lock as soon as it is available
    createButton("writeLock", new Runnable() {
          public void run() {
            rwlock.writeLock().lock();
            text.append(">>>> write lock acquired" + status());
            synchronized (WRITE_MUTEX) {
              try {
                WRITE_MUTEX.wait();
                rwlock.writeLock().unlock();
                text.append("<<<< write lock released" + status());
              } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
              }
            }
          }
    });

    // release a read lock if any
    createButton("readRelease", new Runnable() {
      public void run() {
            synchronized (READ_MUTEX) {
              READ_MUTEX.notify();
            }
          }
    });

    // release a write lock if any
    createButton("writeRelease", new Runnable() {
      public void run() {
            synchronized (WRITE_MUTEX) {
              WRITE_MUTEX.notify();
            }
          }
    });
    frame.setVisible(true);
  }

  private String status() {
    return " read locks:" + rwlock.getReadLockCount() + " writeLocks:" + rwlock.getWriteHoldCount() + " waiting:" + rwlock.getQueueLength() +"\n";
  }

  /**
   * Creates a button with the supplied label, and executes the runnable in a thread when the button is clicked
   * @param label what to display
   * @param runnable what to run
   */
  private void createButton(String label, final Runnable runnable) {
    JButton clearButton = new JButton(label);
    clearButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pool.execute(runnable);
      }
    });
    buttons.add(clearButton);
  }

  /**
   * Initializes the RW Lock to default (unfair)
   */
  private void createDefaultRWLock() {
    new Thread(new Runnable() {
      public void run() {
        rwlock = new ReentrantReadWriteLock(false);
        text.append("new ReentrantReadWriteLock(false)\n");
      }
    }).start();
  }

  /**
   * Initializes the RW Lock to fair
   */
  private void createFairRWLock() {
    new Thread(new Runnable() {
      public void run() {
        reset();
        rwlock = new ReentrantReadWriteLock(true);
        text.append("new ReentrantReadWriteLock(true)\n");
      }
    }).start();
  }

  /**
   * Releases any waiting locks
   */
  private void reset() {
    try {
      while (rwlock.getReadLockCount() > 0 || rwlock.getWriteHoldCount() > 0) {
        synchronized (READ_MUTEX) {
          READ_MUTEX.notifyAll();
        }
        synchronized (WRITE_MUTEX) {
          WRITE_MUTEX.notifyAll();
        }
        // wait a bit before returning to give threads a chance to wake up
        Thread.sleep(100);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}