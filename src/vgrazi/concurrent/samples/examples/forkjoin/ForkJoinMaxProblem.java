package vgrazi.concurrent.samples.examples.forkjoin;

import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.canvases.ForkJoinCanvas;
import vgrazi.concurrent.samples.examples.ForkJoinConcurrentExample;
import vgrazi.concurrent.samples.sprites.ForkJoinSprite;
import vgrazi.concurrent.samples.sprites.ForkJoinThread;
import vgrazi.util.StopWatch;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;

public class ForkJoinMaxProblem {
  private int arraySize;
  private int threadCount;
  private static Random random = new Random();
  private ForkJoinConcurrentExample concurrentExample;
  private ForkJoinCanvas canvas;
  private Map<Thread, ForkJoinThread> threadMap = new ConcurrentHashMap<Thread, ForkJoinThread>();
  private Map<Thread, Integer> counterMap = new ConcurrentHashMap<Thread, Integer>();
  private ForkJoinPool pool;

  public ForkJoinMaxProblem(ForkJoinConcurrentExample concurrentExample, ForkJoinCanvas canvas, int arraySize, int threadCount) {
    this.concurrentExample = concurrentExample;
    this.canvas = canvas;
    this.arraySize = arraySize;
    this.threadCount = threadCount;
  }

  public static void main(String[] args) {
    final ForkJoinMaxProblem problem = new ForkJoinMaxProblem(null, null, 10, 2);
    problem.launch();
  }

  public void launch() {
    int[] array = initialize();
    findMax(array);
  }

  private int[] initialize() {
    int[] array = new int[arraySize];
    for(int i = 0; i < array.length; i++) {
      array[i] = random.nextInt(50);
    }
    return array;
  }

  private class Solver extends RecursiveAction {
    private int[] array;
    private int start;
    private int end;
    private int level;
    private int result;

    /**
     * end is one past the end
     *
     * @param array
     * @param start
     * @param end
     */
    private Solver(int[] array, int start, int end, int step) {
      this.array = array;
      this.start = start;
      this.end = end;
//      System.out.printf("Calculating from %d to %d%n", start, end);
      this.level = step;
    }

    @Override
    protected void compute() {
      final Thread thread = Thread.currentThread();

      // ForkJoinThreads wrap a standard thread, so that when we move a thread to a sprite,
      // we can remove the thread from the previous sprite that owned it
      ForkJoinThread forkJoinThread = threadMap.get(thread);
      if(forkJoinThread == null) {
        forkJoinThread = new ForkJoinThread(thread);
        threadMap.put(thread, forkJoinThread);
      }

      ForkJoinSprite sprite = new ForkJoinSprite(start, end, level);
      forkJoinThread.setCurrentSprite(sprite);
      canvas.addSprite(sprite);
      bumpThreadCount(thread);
//      System.out.printf("%s vgrazi.concurrent.samples.examples.forkjoin.ForkJoinMax$Solver.compute %s is computing (%d, %d)\tlength:%d\tlevel:%d%n", new Date(), thread, start, end, end-start, level);
      final int length = end - start;
      if(length == 1) {
        result = array[start];
        if(forkJoinThread.getIndex() == 0){
            concurrentExample.setState(2);
        }
//        sleep(.75f);

      }
      else {
        if(forkJoinThread.getIndex() == 0){
            concurrentExample.setState(3);
        }
        int mid = (start + end)/2;
        forkJoinThread.setCurrentSprite(null);
        Solver solver1 = new Solver(array, start, mid, level +1);
        Solver solver2 = new Solver(array, mid, end, level +1);
//        sleep(.375f);
        invokeAll(solver1, solver2);
          forkJoinThread.setCurrentSprite(sprite);
//        sleep(.375f);

        result = Math.max(solver1.result, solver2.result);
      }
        sleep(.75f);
      sprite.setComplete(result);
    }

    private void sleep(float seconds) {
      try {
        Thread.sleep((long) (seconds * 1000L));
      }
      catch(InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    private void bumpThreadCount(Thread thread) {
      Integer count = counterMap.get(thread);
      if(count == null) {
        counterMap.put(thread, 1);
      }
      else {
        counterMap.put(thread, count +1);
      }
    }
  }

  private void findMax(int[] array) {
    Solver solver = new Solver(array, 0, array.length, 0);

//      System.out.println("Thread:"+ threadCount);
    pool = new ForkJoinPool(threadCount);
    StopWatch stopwatch = new StopWatch();
      try{
          pool.invoke(solver);
          concurrentExample.setState(4);
      }
      catch(CancellationException e) {
          System.out.println("ForkJoinMaxProblem.findMax cancelled");
      }
      stopwatch.stop();
      String time = stopwatch.getDurationString();
      int result = solver.result;
//      System.out.printf("Fork&Join   Done. Result: %d  time:%s%n", result, time);
      concurrentExample.message1(String.format("Done - Result: %d    Execution time: %s%n", result, stopwatch.getDurationSecondsString()), ConcurrentExampleConstants.MESSAGE_COLOR);
//      displayThreadCounts();
//      int max = 0;
      // Check if the result was ok
//      stopwatch = new StopWatch();

//    stopwatch.start();
//    for(int i : array) {
//      if(i > max) {
//        max = i;
//      }
//    }
//    stopwatch.stop();
//    time = stopwatch.getDurationString();
//    System.out.printf("Synchronous Done. Result: %d  time:%s%n", max, time);
  }

//  private void displayThreadCounts() {
//    for(Map.Entry<Thread, Integer> entry : counterMap.entrySet()) {
//      System.out.println(entry);
//    }
//  }
  public void reset() {
    if(pool != null) {
      pool.shutdownNow();
    }
  }
}
