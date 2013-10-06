package vgrazi.concurrent.samples.sprites;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class wraps a thread, keeping track of which sprite this thread is operating on
 * when the thread is added to a new sprite, it is removed from the prior if any
 */
public class ForkJoinThread {
  private final Thread thread;
  private ForkJoinSprite sprite;
    /**
     * This is the color of this thread, as displayed when this thread is working as a rotating oval
     */
  private Color threadColor;
  private int index;
  private static List<Thread> threads = new ArrayList<Thread>();
  public ForkJoinThread(Thread thread) {
    this.thread = thread;
    registerThread(thread);
  }

  /**
   * registers the thread to determine the index, so that it can be associated with a unique color
   * @param thread
   */
  private void registerThread(Thread thread) {
    synchronized (getClass()){
        index = threads.indexOf(thread);
        if(index < 0) {
            index = threads.size();
            threads.add(thread);
        }
        int colorIndex = index % ConcurrentExampleConstants.FORK_JOIN_THREAD_COLORS.length;
        threadColor = ConcurrentExampleConstants.FORK_JOIN_THREAD_COLORS[colorIndex];
    }
  }

  public synchronized void setCurrentSprite(ForkJoinSprite sprite) {
    if(this.sprite != null) {
      this.sprite.removeThread();
    }
    this.sprite = sprite;
    if(sprite != null) {
      sprite.setForkJoinThread(this);
    }
  }

  @Override
  public String toString() {
    int endIndex = 30;
    final String string = thread.toString();
    if(endIndex > string.length()) {
      endIndex = string.length();
    }
    return string.substring(0, endIndex);
  }

    public Color getThreadColor() {
        return threadColor;
    }
    public int getIndex () {
        return index;
    }
    public static void reset() {
        threads.clear();
    }
}
