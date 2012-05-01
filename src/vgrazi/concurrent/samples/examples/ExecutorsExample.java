package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

public abstract class ExecutorsExample extends ConcurrentExample implements Pooled {
  public ExecutorService getExecutor() {
    return executor;
  }

  public void setExecutor(ExecutorService executor) {
    this.executor = executor;
  }

  private ExecutorService executor;
  int nextIndex;
  private final JButton executeButton = new JButton("execute");
  private final JButton prestartButton = new JButton("prestartAllCoreThreads");
  // todo: How do we demo setRejectedExecutionHandler() ???
  private final JButton setRejectedExecutionHandlerAbortButton = new JButton("setRejectedExecutionHandler(Abort)");
    private final JButton setRejectedExecutionHandlerCallerRunsButton = new JButton("setRejectedExecutionHandler(CallerRuns)");
  private final JButton setRejectedExecutionHandlerDiscardOldestButton = new JButton("setRejectedExecutionHandler(DiscardOldest)");
  private final JButton setRejectedExecutionHandlerDiscardButton = new JButton("setRejectedExecutionHandler(Discard)");
  protected boolean initialized = false;
  protected int sleepTime;
  final JTextField threadCountField = createThreadCountField();
  protected String currentSaturationHandler = "AbortPolicy";

  /**
   * Each example must have a unique slide show index (or -1). Indexes must start with 0 and must be in sequence, no skipping
   *
   * @param label          the label to display at the top of the
   * @param frame          the launcher frame to display the example
   * @param slideShowIndex when configured as a slide show, this indicates the slide number. -1 for exclude from slide show - will still show in menu bar
   */
  public ExecutorsExample(String label, Container frame, int slideShowIndex) {
    super(label, frame, ExampleType.POOLED, 340, false, slideShowIndex);
  }

  protected  abstract void initializeThreadPool();

  @Override
  protected abstract void setDefaultState();


  @Override
  public String getToolTipText() {
    return "<HTML>" +
            "<body>" +
            "" +
            "</body>" +
            "</HTML>";
  }

  public String getDescriptionHtml() {
    return "";
  }


  protected void initializeExecuteButton() {
    initializeButton(executeButton, new Runnable() {
      public void run() {
        final int threadCount = getThreadCount(threadCountField);
        for (int i = 0; i < threadCount; i++) {
          executeNewRunnable();
        }
      }
    });
  }

  private void executeNewRunnable() {
    scheduledExecutor.schedule(new Runnable() {
      @Override
      public void run() {

        final int index = nextIndex++;
        message1("Executing index " + index, ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
        final ConcurrentSprite sprite = createAcquiringSprite(ConcurrentSprite.SpriteType.RUNNABLE);
        try {
          Runnable runnable = new ExampleRunnable(sprite, index);

          executor.execute(runnable);
        } catch (RejectedExecutionException e) {
          message2("RejectedExecutionException ", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
          setRejected(sprite);
        }
      }
    }, 500, TimeUnit.MILLISECONDS);
  }

  protected void initializePrestartButton() {
    initializeButton(prestartButton, new Runnable() {
      public void run() {
        setState(4);
        int count = ((ThreadPoolExecutor) executor).prestartAllCoreThreads();
        getCanvas().notifyAnimationThread();
        message1(String.format("Prestarted %d thread%s", count, count == 1 ? "" : "s"), ConcurrentExampleConstants.MESSAGE_COLOR);
        message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
      }
    });
  }

  protected void initializeSaturationPolicyButtons() {
    initializeButton(setRejectedExecutionHandlerCallerRunsButton, new Runnable() {
      public void run() {
        ((ThreadPoolExecutor) executor).setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy(){
          @Override
          public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            final ConcurrentSprite sprite = ((ExampleRunnable) r).getSprite();
            sprite.setAcquired();
            scheduledExecutor.schedule(new Runnable() {
              @Override
              public void run() {
                sprite.setReleased();
              }
            }, 3, TimeUnit.SECONDS);
//            setAcquiredSprite(sprite);
            System.out.println("CallerRuns.rejectedExecution " + sprite);
            message2("CallerRuns invoked. Caller runs", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
          }
        });
        currentSaturationHandler = "CallerRunsPolicy";
        setState(5);
      }
    });
    initializeButton(setRejectedExecutionHandlerDiscardButton, new Runnable() {
      public void run() {
        ((ThreadPoolExecutor) executor).setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy() {
          @Override
          public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            setRejected(((ExampleRunnable) r).getSprite());
            message2("DiscardPolicy invoked. Discarding", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
          }
        });
        currentSaturationHandler = "DiscardPolicy";
        setState(5);
      }
    });
    initializeButton(setRejectedExecutionHandlerDiscardOldestButton, new Runnable() {
      public void run() {
        ((ThreadPoolExecutor) executor).setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy() {
          @Override
          public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            setRejected(((ExampleRunnable) r).getSprite());
            message2("DiscardOldestPolicy invoked. Discarding oldest", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
          }
        });
        currentSaturationHandler = "DiscardOldestPolicy";
        setState(5);
      }
    });
    initializeButton(setRejectedExecutionHandlerAbortButton, new Runnable() {
      public void run() {
        ((ThreadPoolExecutor) executor).setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy() {
          @Override
          public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            setRejected(((ExampleRunnable) r).getSprite());
            message2("AbortPolicy invoked. Aborting", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
          }
        });
        currentSaturationHandler = "AbortPolicy";
        setState(5);
      }
    });
  }

  private void setRejected(final ConcurrentSprite sprite) {
    scheduledExecutor.schedule(new Runnable() {
      @Override
      public void run() {
        sprite.setRejected();
      }
    }, 500, TimeUnit.MILLISECONDS);
  }

  public int getAvailableThreadCount() {
//      System.out.println(String.format("Active count: %d   Core Pool Size: %d   Pool Size:%d   Task count: %d", tpExecutor.getActiveCount(), tpExecutor.getCorePoolSize(), tpExecutor.getPoolSize(), tpExecutor.getTaskCount()));
    int count = 0;
    if (executor instanceof ThreadPoolExecutor) {
      ThreadPoolExecutor tpExecutor = (ThreadPoolExecutor) executor;
      count = tpExecutor.getPoolSize() - tpExecutor.getActiveCount();
    } else {
      count = 1;
    }
    return count;
  }


  class ExampleRunnable implements Runnable {
    ConcurrentSprite sprite;
    private int index;

    public ExampleRunnable(ConcurrentSprite sprite, int index) {
      this.sprite = sprite;
      this.index = index;
    }
    public void run() {
      setState(3);
      sprite.setAcquired();
      try {
        int sleepTime = ExecutorsExample.this.sleepTime + (int) (Math.random() * 1000);
        Thread.sleep(sleepTime);
        sprite.setReleased();
      } catch (InterruptedException e) {
        System.out.println("ExecutorsExample.run interrupted exception");
        Thread.currentThread().interrupt();
      }
    }

    public ConcurrentSprite getSprite() {
      return sprite;
    }

    @Override
    public String toString() {
      return "ExampleRunnable " + index;
    }
  }
}
