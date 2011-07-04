package vgrazi.concurrent.samples.sprites;

/**
 * This class wraps a thread, keeping track of which sprite this thread is operating on
 * when the thread is added to a new sprite, it is removed from the prior if any
 */
public class ForkJoinThread {
  private final Thread thread;
  private ForkJoinSprite sprite;

  public ForkJoinThread(Thread thread) {
    this.thread = thread;
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
}
