package vgrazi.concurrent.samples.research;

import jsr166e.StampedLock;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Notes:
 * ReadLock is obtained from long stamp = lock.readLock() and released from lock.unlockRead(stamp)
 * WriteLock is obtained from long stamp = lock.writeLock() and released from lock.unlockWrite(stamp)
 * Optimistic ReadLock is obtained from long stamp = lock.tryOptimisticRead(). Copy results to local variables, then call lock.validate(stamp)
 * If not valid, obtain a true read lock, then recopy the variables locally.
 *
 */
public class StampedLockResearch {
  private StampedLock lock = new StampedLock();
  private final AtomicInteger counter = new AtomicInteger();
  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final Random random = new Random();

  public static void main(String[] args) {
    new StampedLockResearch().launch();
  }

  private void launch() {
    if (true) {
      executor.execute(createReadCommand());
      executor.execute(createOptimisticReadCommand());
      executor.execute(createWriteCommand());
    } else {
      Point point = new Point();
      executor.execute(movePointCommand(point));
      executor.execute(readPointCommand(point));
    }
    executor.execute(exitCommand());
  }

  private Runnable exitCommand() {
    return new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(10000);
          System.exit(0);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    };
  }

  private Runnable createWriteCommand() {
    return new Runnable() {
      @Override
      public void run() {
        final String sequence = getThreadSequence();
        sleepRandom(0);
        System.out.println(sequence + "- Attempting Write lock");
        final long stamp = lock.writeLock();
        System.out.println(sequence + "> Write Lock obtained " + stamp);
        sleepRandom(2);
        lock.unlockWrite(stamp);
        System.out.println(sequence + "< Write Lock released " + stamp);
      }
    };
  }

  private Runnable createOptimisticReadCommand() {
    return new Runnable() {
      @Override
      public void run() {
        sleepRandom(0);
        final String sequence = getThreadSequence();
        System.out.println(sequence + "- Attempting Optimistic read lock");
        long stamp = lock.tryOptimisticRead();
        System.out.println(sequence + "> Optimistic Read Lock obtained " + stamp);
        sleepRandom(2);
        // now make local copies of the variables...
        // ... then validate. If not valid, obtain a true lock, and try again
        boolean valid = lock.validate(stamp);
        if (!valid) {
          System.out.println(sequence + "- INVALID Read Lock. Try again ");
          stamp = lock.readLock();
          System.out.println(sequence + "> Real read Lock obtained " + stamp);
          lock.unlockRead(stamp);
          System.out.println(sequence + "< Real read Lock released: " + stamp);
        }
        else {
          System.out.println(sequence + "< Read Lock validated: " + stamp);
        }
      }
    };
  }

  private Runnable createReadCommand() {
    return new Runnable() {
      @Override
      public void run() {
        sleepRandom(0);
        final String sequence = getThreadSequence();
        System.out.println(sequence + "- Attempting read lock");
        final long stamp = lock.readLock();
        System.out.println(sequence + "> Read Lock obtained " + stamp);
        sleepRandom(2);
        lock.unlockRead(stamp);
        System.out.println(sequence + "< Read Lock released " + stamp);
      }
    };
  }

  private Runnable movePointCommand(final Point point) {
    return new Runnable() {
      @Override
      public void run() {
        point.move(1, 1);
      }
    };
  }

  private Runnable readPointCommand(final Point point) {
    return new Runnable() {
      @Override
      public void run() {
        double v = point.distanceFromOrigin();
      }
    };
  }

  /**
   * Returns an indented sequence number
   *
   * @return an indented sequence number
   */
  private String getThreadSequence() {
    final int seq = counter.getAndIncrement();
    return "           ".substring(0, seq + 1) + seq;
  }

  /**
   * Sleep for a specified number of seconds plus a fraction of a second, so that the locks are randomly obtained
   *
   * @param init the initial number of seconds to sleep
   */
  private void sleepRandom(int init) {
    final float time = random.nextFloat();
    try {
      Thread.sleep((long) (init * 1000 + time * 1000));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  //======================================================================================================================
  class Point {
    private double x, y;
    private final StampedLock sl = new StampedLock();

    void move(double deltaX, double deltaY) { // an exclusively locked method
      String sequence = getThreadSequence();
      sleepRandom(2);
      System.out.println(sequence + "- Acquiring write lock");
      long stamp = sl.writeLock();
      System.out.println(sequence + "> write lock obtained");
      sleepRandom(2);
      try {
        x += deltaX;
        y += deltaY;
      } finally {
        sl.unlockWrite(stamp);
        System.out.println(sequence + "> write lock released");
      }
    }

    double distanceFromOrigin() { // A read-only method
      String sequence = getThreadSequence();
      sleepRandom(1);
      System.out.println(sequence + "- Acquiring optimistic read lock");
      long stamp = sl.tryOptimisticRead();
      System.out.println(sequence + "> optimistic read lock obtained");
      sleepRandom(1);
      // create a copy of variables
      double currentX = x, currentY = y;
      // now that we have a copy, let's validate the values. If not valid, obtain a true read lock, and re-obtain
      if (!sl.validate(stamp)) {
        System.out.println(sequence + "- optimistic read lock invalid. Acquiring real read lock");
        stamp = sl.readLock();
        System.out.println(sequence + "> optimistic read lock invalid. Real read lock acquired");
        try {
          currentX = x;
          currentY = y;
        } finally {
          sl.unlockRead(stamp);
          System.out.println(sequence + "> optimistic read lock obtained");
        }
      } else {
        System.out.println(sequence + "> optimistic read lock validated (released)");
      }
      return Math.sqrt(currentX * currentX + currentY * currentY);
    }

    void moveIfAtOrigin(double newX, double newY) { // upgrade
      // Could instead start with optimistic, not read mode
      long stamp = sl.readLock();
      try {
        while (x == 0.0 && y == 0.0) {
          long ws = sl.tryConvertToWriteLock(stamp);
          if (ws != 0L) {
            stamp = ws;
            x = newX;
            y = newY;
            break;
          } else {
            sl.unlockRead(stamp);
            stamp = sl.writeLock();
          }
        }
      } finally {
        sl.unlock(stamp);
      }
    }
  }
}
