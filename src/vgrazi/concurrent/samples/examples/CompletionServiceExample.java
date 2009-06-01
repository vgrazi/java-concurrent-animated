package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;

/**
 * A CompletionService is a utility for handling results of a series of asynchronous operations.
 * Callers register a Callable via its {@link CompletionService#submit} method.
 * The Callable call method is executed, and all such results are queued by the CompletionService in the order that they complete.
 * To obtain the queue of results, call CompletionService.take().get(). Note: the take() will block until a value is available.
 * take returns a {@link Future} instance. take().get() returns the value associated with that Future.
 * The CompletionService itself will never block unless there are no results available, which could happen if the executor is
 * tied up, for example, a pooled executor who's pool is deadlocked.
 * Copyright (CENTER) 2005 - Victor J. Grazi
 * User: Win XP user
 * Date: Oct 30, 2005
 * Time: 2:53:23 PM
 */
public class CompletionServiceExample extends ConcurrentExample {
  private boolean initialized;
//  private final JButton launchButton = new JButton("Launch");
  private final JButton submitButton = new JButton("submit");
  private final JButton takeButton = new JButton("take().get()");
  private CompletionService<Result> completionService;
  private int index;
  private int RESET_COUNT = 0;

  public CompletionServiceExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.BLOCKING, 390, false, slideNumber);
  }

  public String getTitle() {
    return "CompletionService";
  }

  protected void initializeComponents() {
    reset();
    if(!initialized) {
      initializeButton(submitButton, new Runnable() {
        public void run() {
          setState(1);
          submit();
        }
      });
      initializeButton(takeButton, new Runnable() {
        public void run() {
          setState(2);
          take();
        }
      });
      initialized = true;
    }
  }

  private void submit() {
    final ConcurrentSprite sprite = createAcquiringSprite(ConcurrentSprite.SpriteType.ARROW);
    final Result result = new Result(index++, sprite, RESET_COUNT);
    Callable<Result> callable = new Callable<Result>() {
      public Result call() throws Exception {
        setSpriteAcquired(result);
        long time = (long) (1000 + Math.random() * 2000);
        Thread.sleep(time);
        if (result.resetCount == RESET_COUNT) {
          message2("Calculation " + result.index + " complete", ConcurrentExampleConstants.MESSAGE_COLOR);
        }
        sprite.setActionCompleted();
        return result;
      }
    };
    solve(callable);
  }

  private void solve(Callable<Result> solver){
    completionService.submit(solver);
  }

  private void take() {
    try {
      long startTime = System.currentTimeMillis();
      // Now take results in the order they complete
      Future<Result> future = completionService.take();
      Result result = future.get();
      long getTime = System.currentTimeMillis();
      long endTime = System.currentTimeMillis();
      final long endDelta = endTime - startTime;
      final long getDelta = getTime - startTime;
      if(endDelta != getDelta) {
        System.out.println("get: " + getDelta + " end:" + endDelta);
      }
      setSpriteReleased(result);
      use(result);
    }
    catch(InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch(ExecutionException e) {
      e.printStackTrace();
    }
  }
  private void setSpriteAcquired(Result result) {
    ConcurrentSprite sprite = result.getSprite();
    sprite.setAcquired();
  }

  private void setSpriteReleased(Result result) {
    ConcurrentSprite sprite = result.getSprite();
    sprite.setReleased();
  }

  private void use(Result result) {
    message1("CompletionServiceExample.use: received result:" + result, ConcurrentExampleConstants.MESSAGE_COLOR);
  }

  public String getDescriptionHtml() {
    return "";
  }

  public void reset() {
    // Create a completionService, providing an Executor in the constructor
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    completionService = new ExecutorCompletionService<Result>(executorService);
    index = 0;
    RESET_COUNT++;
    message1("  ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    message2("  ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    setState(0);
    super.reset();
  }

  class Result {
    private final int index;
    private final ConcurrentSprite sprite;
    private int resetCount;

    public Result(int index, ConcurrentSprite sprite, int resetCount) {
      this.index = index;
      this.sprite = sprite;
      this.resetCount = resetCount;
    }

    public int getIndex() {
      return index;
    }

    public ConcurrentSprite getSprite() {
      return sprite;
    }

    public String toString() {
      return "Result:" + index;
    }

  }

  protected String getSnippet() {
    String snippet;
    snippet =
       "<html><pre>\n" +
          "<FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"> \n" +
          "    // Launch multiple Callables in a completion service.\n" +
          "    // Results are queued as they arrive. \n" +
          "    // To retrieve the results in sequence, \n" +
          "    // call completionService.take().get();" +
          " \n" +
          " \n" +
          "<FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>" +
          "    // Create a completionService, providing an Executor \n" +
          "    // in the constructor. \n" +
          "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"><B>final</B> CompletionService<Result> completionService = \n" +
          " <B>    new</B> ExecutorCompletionService<Result>(" +
          " \n        Executors.newFixedThreadPool(4));" +
          " \n" +
          " \n" +
          "<FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>" +
          "    // Submit callables to the completion service \n" +
          "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">completionService.submit(callable);</FONT>" +
          " \n" +
          " \n" +
          "<FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>" +
          "    // Now take results in the order they complete \n" +
          "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">Future<Result> future = completionService.take();</FONT>\n" +
          "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">Result result = future.get();</FONT>" +
          " \n" +
          " \n" +
          " \n" +
          " \n" +
          " \n" +
          " \n" +
          " \n" +
          " \n" +
          " \n" +
          "</PRE></html>";


    return snippet;
  }
}
