package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

public class ExecutorsExample extends ConcurrentExample implements Pooled {
  private ExecutorService executor;
  private int nextIndex;
  private final JButton executeButton = new JButton("execute");
  private boolean initialized = false;
  private int sleepTime;
  public static final String FIXED_TYPE =  "FixedThreadPool";
  public static final String SINGLE_TYPE = "SingleThreadExecutor";
  public static final String CACHED_TYPE = "CachedThreadPool";
  private final JTextField threadCountField = createThreadCountField();


  /**
   * Each example must have a unique slide show index (or -1). Indexes must start with 0 and must be in sequence, no skipping
   * @param label the label to display at the top of the
   * @param frame the launcher frame to display the example
   * @param slideShowIndex when configured as a slide show, this indicates the slide number. -1 for exclude from slide show - will still show in menu bar
   */
  public ExecutorsExample(String label, Container frame, int slideShowIndex) {
    super(label, frame, ExampleType.POOLED, 390, false, slideShowIndex);
  }

  @Override
  protected void setDefaultState() {
    if(getTitle().equals(FIXED_TYPE)) {
      sleepTime = 2000;
      setState(0);
    }
    else if(getTitle().equals(SINGLE_TYPE)) {
      sleepTime = 1000;
      setState(1);
    }
    else if(getTitle().equals(CACHED_TYPE)) {
      sleepTime = 1000;
      setState(2);
    }
  }

  protected String getSnippet() {
    return "<html><PRE><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// FixedThreadPool Construction</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>final</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> Executor executor = </FONT>\n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\">     Executors.newFixedThreadPool(4);\n" +
            " \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// SingleThreadExecutor Construction</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>final</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> Executor executor = </FONT>\n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">     Executors.newSingleThreadExecutor(); \n" +
            " \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// CachedThreadPool Construction</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>final</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> Executor executor = </FONT>\n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">     Executors.newCachedThreadPool(); \n" +
            " \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Use the Executor to launch some Runnable </I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> \n" +
            "      executor.execute(</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>new</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> Runnable(){ \n" +
            "        </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>public</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>void</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> run(){ \n" +
            "          </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// do work</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> \n" +
            "        } \n" +
//            "      }); \n" +
//            "    } \n" +
            "    </FONT></PRE></html";
  }

  @Override
  public String getToolTipText() {
    if(getTitle().equals(FIXED_TYPE)) {
      return "<HTML>" +
              "<body>" +
              "FixedThreadPoolExecutor maintains a constant<br>" +
              "sized pool of threads.<br><br>" +
              "When all of those threads are executing," +
              "<br>additional clients must wait<br>" +
              "until threads are released.<br>" +
              "</body>" +
              "</HTML>";
    }
    else if(getTitle().equals(SINGLE_TYPE)){
      return "<HTML>" +
              "<body>" +
              "SingleThreadPoolExecutor is a fixed thread pool<br>" +
              "executor with just one thread.<br><br>" +
              "While that thread is executing,<br>" +
              "additional clients must wait until <br>" +
              "the thread is released.<br>" +
              "</body>" +
              "</HTML>";
    }
    else if(getTitle().equals(CACHED_TYPE)) {
      return "<HTML>" +
              "<body>" +
              "CachedThreadPoolExecutor attempts to reuse<br>" +
              "threads once they are created.<br><br>" +
              "If there are no pooled threads available,<br>" +
              "new threads will be created as requests come in.<br><br>" +
              "Threads timeout and leave the pool after 60 seconds,<br>" +
              "to conserve resources.<br>" +
              "</body>" +
              "</HTML>";
    }
    return "<HTML>" +
            "<body>" +
            "" +
            "</body>" +
            "</HTML>";
  }

  protected void initializeComponents() {
    if (!initialized) {
      initializeExecuteButton();
      initializeThreadCountField(threadCountField);
      initialized = true;
    }
    reset();
  }

  public String getDescriptionHtml() {
    StringBuffer sb = new StringBuffer();
    //    sb.append("<html>");
    //    sb.append("<table border=\"0\">");
    //    sb.append("<tr><td>");
    //    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_FONT_COLOR + "\">");
    //
    //    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>QueuedExecutor.execute(Runnable)</code></font> accepts a Runnable parameter and returns immediately. ");
    //    sb.append("It then executes that Runnable ");
    //    sb.append("in its own thread.<p><p>");
    //    sb.append("However the threads are queued - one does not begin until the previous one completes.<p><p>");
    //
    //    sb.append("</td></tr>");
    //    sb.append("<tr><td>");
    //    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_FONT_COLOR + "\">");
    //    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>ThreadedExecutor.execute(Runnable)</code></font> accepts a Runnable parameter and returns immediately. ");
    //    sb.append("It executes each Runnable concurrently ");
    //    sb.append("in its own thread.<p><p>");
    //    sb.append("</td></tr>");
    //    sb.append("<tr><td>");
    //    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_FONT_COLOR + "\">");
    //
    //    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>PooledExecutor.execute(Runnable)</code></font> accepts a Runnable parameter and returns immediately. ");
    //    sb.append("It executes each Runnable ");
    //    sb.append("in its own thread from a pool of available threads. When no threads are available in the pool, threads are queued in wait for the next available thread.<p><p>");
    //
    //    sb.append("</td></tr>");
    //    sb.append("</table>");
    //    sb.append("</html>");
    //
    return sb.toString();
  }

  private void initializeFixedThreadPool() {
//    getAnimationCanvas().setLabelText("FixedThreadPooledExecutor");
    executor = Executors.newFixedThreadPool(4);
  }
  private void initializeSingleThreadPool() {
    // todo: replace fixed with single
//    executor = Executors.newSingleThreadExecutor();
    executor = Executors.newFixedThreadPool(1);
  }
  private void initializeCachedThreadPool() {
    executor = Executors.newCachedThreadPool();
  }

  private void initializeExecuteButton() {
    initializeButton(executeButton, new Runnable() {
      public void run() {
        final int threadCount = getThreadCount(threadCountField);
        for (int i = 0; i < threadCount; i++) {
          final int index = nextIndex++;
          message1("Executing index " + index, ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
          final ConcurrentSprite sprite = createAcquiringSprite(ConcurrentSprite.SpriteType.RUNNABLE);
          executor.execute(new Runnable() {

            public void run() {
              setState(3);
              sprite.setAcquired();
              try {
                int sleepTime = ExecutorsExample.this.sleepTime + (int) (Math.random() * 500);
                Thread.sleep(sleepTime);
                sprite.setReleased();
              }
              catch (InterruptedException e) {
                sprite.setReleased();
                Thread.currentThread().interrupt();
              }
              message2("Completed executing index " + index, ConcurrentExampleConstants.MESSAGE_COLOR);
            }
          });
        }
      }
    });
  }

  @Override
  public void reset() {
//    if (executor != null) {
//      executor.shutdown();
//    }
    if(getTitle().equals(FIXED_TYPE)) {
      initializeFixedThreadPool();
    }
    else if(getTitle().equals(SINGLE_TYPE)) {
      initializeSingleThreadPool();
    }
    else if(getTitle().equals(CACHED_TYPE)) {
      initializeCachedThreadPool();
    }
    resetThreadCountField(threadCountField);

    nextIndex = 1;
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    setState(0);
    super.reset();
  }

  public int getAvailableThreadCount() {
//      System.out.println(String.format("Active count: %d   Core Pool Size: %d   Pool Size:%d   Task count: %d", tpExecutor.getActiveCount(), tpExecutor.getCorePoolSize(), tpExecutor.getPoolSize(), tpExecutor.getTaskCount()));
      ThreadPoolExecutor tpExecutor = (ThreadPoolExecutor) executor;
      return tpExecutor.getPoolSize() - tpExecutor.getActiveCount();
  }
}
