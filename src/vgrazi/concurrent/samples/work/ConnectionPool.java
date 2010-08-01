package vgrazi.concurrent.samples.work;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copyright (CENTER) 2005 - Victor J. Grazi
 * Date: Dec 26, 2005
 * Time: 3:07:29 PM
 */
public class ConnectionPool {
  private final Lock lock = new ReentrantLock();
  private final Condition hasConnections = lock.newCondition();
  private Condition belowMinimum = lock.newCondition();

  private final List<Connection> connections = new ArrayList<Connection>();
  private final int min;
  private final int refills;
  private boolean running;

  /**
   * Creates a Connection pool with a minimum of min connections.
   * When it goes below the min, the specified refills are created
   *
   * @param min
   */
  public ConnectionPool(int min, int refills) {
    output("ConnectionPool.run launching refill thread");
    this.min = min;
    this.refills = refills;
    running = true;
    launchRefillThread();
  }

  private void launchRefillThread() {
    Executors.defaultThreadFactory().newThread(new Runnable() {
      public void run() {

        try {
          while(running) {
            output("ConnectionPool.refill pool is full awaiting...");
            lock.lock();
            while(running && connections.size() >= min) {
              belowMinimum.await();
            }

            output("ConnectionPool.refill need new connections.");
            for(int i = 0; running && i < refills; i++) {
              Connection connection = createConnection();
              connections.add(connection);
              output("ConnectionPool.added");
              hasConnections.signalAll();
            }
            lock.tryLock(1, TimeUnit.SECONDS);
          }
        }
        catch(InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          lock.unlock();
        }
      }
    }).start();
  }

  public void stop() {
    try {
      lock.lock();
      running = false;
      belowMinimum.signal();
    } finally {
      lock.unlock();
    }
    // close all connections.
    for(Connection connection : connections) {
      connection.close();
    }
  }

  /**
   * Returns a Connection. If no connections available, method will block until a connection becomes available
   */
  public Connection getConnection() {
    Connection connection = null;
    lock.lock();
    try {
      while(connections.isEmpty()) {
        output("ConnectionPool.getConnection no connections. Awaiting connections...");
        hasConnections.await();
      }
      // Since this is the only place to grab a connection, and since we are still in the lock block where
      // the connections List was tested, we are guaranteed to find a connection here.
      if(running) {
        connection = connections.remove(0);
      }
      belowMinimum.signal();
    }
    catch(InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      lock.unlock();
    }
    output("ConnectionPool.getConnection received a connection");
    return connection;
  }

  /**
   * Returns a Connection to the pool
   *
   * @param connection
   */
  public void releaseConnection(Connection connection) {
    try {
      lock.lock();
      connections.add(connection);
      hasConnections.signalAll();
    } finally {
      lock.unlock();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    final ConnectionPool pool = new ConnectionPool(5, 3);
    Executors.defaultThreadFactory().newThread(new Runnable() {
      public void run() {
        try {
          Thread.sleep(5000);
          pool.output("Stopping...");
          pool.stop();
        }
        catch(InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }).start();
    pool.test();
  }

  private void test() throws InterruptedException {
    int count = 10;
    for(int i = 0; i < count; i++) {
      Thread.sleep(5 * 1000);
      output("Connection.main requesting connections");
      Connection connection = getConnection();
      output("Connection.main connection obtained: " + connection);
    }
  }

  private void output(String s) {
    System.out.println(s + ". connections:" + connections.size());
  }

  private Connection createConnection() {
    return new Connection();
  }

  /**
   * A Mock Connection object
   */
  class Connection {
    Date creation = new Date();

    public Connection() {
      long time = (long) (1000 * (1 + Math.random()));
      try {
        Thread.sleep(time);
      }
      catch(InterruptedException e) {
        Thread.currentThread().interrupt();
      }

    }

    public String toString() {
      return "Connection " + creation;
    }

    public void close() {
    }
  }
}
