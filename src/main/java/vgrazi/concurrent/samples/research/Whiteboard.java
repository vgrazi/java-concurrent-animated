package vgrazi.concurrent.samples.research;

import java.util.concurrent.TimeUnit;

/**
 * Copyright (CENTER) 2005 - Victor J. Grazi
 * User: Win XP user
 * Date: Dec 24, 2005
 * Time: 11:46:35 PM
 */
public class Whiteboard {
  public static void main(String[] args) {
    new Whiteboard().timeUnitWork();
  }

  public void timeUnitWork() {
    TimeUnit milliseconds = TimeUnit.MILLISECONDS;
    TimeUnit seconds = TimeUnit.SECONDS;
    int amount = 1;
    TimeUnit target = seconds;
    long ms = milliseconds.convert(amount, target);
    System.out.println("FutureExample.timeUnitWork " + amount + " " + target + " = " + ms + " " + milliseconds);

    TimeUnit timeUnit = TimeUnit.valueOf("MILLISECONDS");
    System.out.println("TimeUnitWork.timeUnitWork MS:" + timeUnit);
  }

  public void waitNotifyWork() {
    Object MUTEX = new Object();

    // SETTING UP THE WAIT THREAD
    // check the state in a while block (a notification does not necessarily imply that the state is favorable)
    // the state is not favorable. wait until notification is received
    try {
      synchronized(MUTEX) {
        while(!checkSomeState()) {
          // state is not favorable - wait for notification
          MUTEX.wait();
          // notification has been received, recheck the state
        }
      }
      // loop exited without exception, state is favorable.
      // continue processing
    }
    catch(InterruptedException e) {
      // Interruption is trapped outside of the loop, so that interrupt will cause loop to exit
      Thread.currentThread().interrupt();
    }

    // SETTING UP THE NOTIFICATION THREAD
    // do some work
    // state has been changed
    // notify all waiting threads to wake up
    synchronized(MUTEX) {
      MUTEX.notifyAll();
    }

  }

  private boolean checkSomeState() {
    return false;
  }
}
