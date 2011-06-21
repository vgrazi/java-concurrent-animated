package vgrazi.util;

public class StopWatch {
  long startTime;
  long endTime;
  public StopWatch() {
    this(true);
  }
  public StopWatch(boolean start) {
    if(start) {
      start();
    }
  }

  public void start() {
    startTime = System.currentTimeMillis();
  }

  public void stop() {
    endTime = System.currentTimeMillis();
  }

  public String getDurationString() {
    long duration = getDuration();
    return getDurationString(duration);
  }

  public static void main(String[] args) throws InterruptedException {
    StopWatch stopWatch = new StopWatch(false);
    String time = stopWatch.getDurationString(60*60000*2 + 60000 * 3 + 1000 * 5 + 123);
    System.out.printf("StopWatch.main %s%n", time);
    stopWatch.start();
    Thread.sleep(1000 * 5 + 123);
    stopWatch.stop();
    time = stopWatch.getDurationString();
    System.out.printf("StopWatch.main %s%n", time);

  }
  private String getDurationString(long duration) {
    long ms = fractionalPart(duration, 1000);
    long sec = fractionalPart(duration / 1000, 60);
    long min = fractionalPart(duration / 60000, 60);
    long hours = fractionalPart(duration /(60000 * 60), 60);
    return String.format("Hours:%d, Minutes:%d, Seconds:%d, MS:%d", hours, min, sec, ms);
  }

  private long fractionalPart(long duration, int divisor) {
    return duration - (duration /divisor) * divisor;
  }

  public long getDuration() {
    return endTime - startTime;
  }
}
