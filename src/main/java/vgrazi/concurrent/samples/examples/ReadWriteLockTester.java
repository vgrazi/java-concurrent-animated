package vgrazi.concurrent.samples.examples;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ReadWriteLockTester {

  final ExecutorService pool = Executors.newCachedThreadPool();
  ReentrantReadWriteLock rwlock;
  private final Object mutex = new Object();

  public static void main(String[] args) {
    new ReadWriteLockTester();
  }

  public ReadWriteLockTester() {
    boolean fair;
    rwlock = new ReentrantReadWriteLock(fair = true);
    try {
      System.out.println("ReadWriteLockTester.ReadWriteLockTester fair:" + rwlock.isFair());
      readLock();
      writeLock();

      readLock();
      writeLock();

      readLock();
      writeLock();

      if (true) {
        Thread.sleep(3000);

        wakeRead();
        wakeRead();
        wakeRead();
        Thread.sleep(3000);
        System.exit(1);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void wakeRead() {
    synchronized (mutex) {
//      System.out.println("ReadWriteLockTester.wakeRead");
      mutex.notify();
    }
  }

  private void readLock() {
    pool.execute(new Runnable() {
      public void run() {
        rwlock.readLock().lock();
        System.out.println("ConcurrentExampleLauncher.run " + this + " acquired ReadLock");
        synchronized (mutex) {
          try {
            mutex.wait();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
        System.out.println("ConcurrentExampleLauncher.run " + this + " releasing ReadLock");
        rwlock.readLock().unlock();
      }
    });
  }

  private void writeLock() {
    pool.execute(new Runnable() {
      public void run() {
        try {
          Thread.sleep(0);
          System.out.println("ReadWriteLockTester.run acquiring write lock");
          rwlock.writeLock().lock();
          System.out.println("ConcurrentExampleLauncher.run " + this + " acquired WriteLock");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
  }
}