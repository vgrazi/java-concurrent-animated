package vgrazi.concurrent.samples.sprites;

/**
 * This is the sprite used for rendering Fork and Join animations
 */
public class ForkJoinSprite extends ConcurrentSprite {

  private int start;
  private int end;
  private int level;
  private boolean complete;
  //-1 indicates not yet set
  private int levelIndex = -1;
  private ForkJoinThread forkJoinThread;
  private int solution;
  public ForkJoinSprite(int start, int end, int level) {

    // index not used
    super(-1);

    this.start = start;
    this.end = end;
    this.level = level;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public int getLevel() {
    return level;
  }

  public boolean isComplete() {
    return complete;
  }

  /**
   * Sets the complete indicator. Also, sets the forkJoinThread to null
   * synchronized to ensure happens before ordering: so that "solution" is visible before "complete"
   * @param solution
   */
  public synchronized void setComplete(int solution) {
    this.solution = solution;
    this.complete = true;
    if(forkJoinThread != null) {
      this.forkJoinThread.setCurrentSprite(null);
      this.forkJoinThread = null;
    }
  }

  public int getLevelIndex() {
    return levelIndex;
  }

  public void setLevelIndex(Integer levelIndex) {
    this.levelIndex = levelIndex;
  }

  public void removeThread() {
//    System.out.printf("ForkJoinSprite.removeThread %s from %s%n", forkJoinThread, this);
    this.forkJoinThread = null;
  }

  public ForkJoinThread getForkJoinThread() {
    return forkJoinThread;
  }

  public void setForkJoinThread(ForkJoinThread forkJoinThread) {
    this.forkJoinThread = forkJoinThread;
  }

  @Override
  public String toString() {
    return String.format("(%d,%d)", start, end);
  }

  public String getSolution() {
    return String.valueOf(solution);
  }
}
