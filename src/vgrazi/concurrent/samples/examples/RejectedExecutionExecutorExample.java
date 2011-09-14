package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RejectedExecutionExecutorExample extends ExecutorsExample {

  /**
   * Each example must have a unique slide show index (or -1). Indexes must start with 0 and must be in sequence, no skipping
   *
   * @param label          the label to display at the top of the
   * @param frame          the launcher frame to display the example
   * @param slideShowIndex when configured as a slide show, this indicates the slide number. -1 for exclude from slide show - will still show in menu bar
   */
  public RejectedExecutionExecutorExample(String label, Container frame, int slideShowIndex) {
    super(label, frame, slideShowIndex);
  }

  @Override
  public void reset() {
    if (executor != null) {
      executor.shutdownNow();
    }
    initializeThreadPool();
    setState(0);
    resetThreadCountField(threadCountField);

    nextIndex = 1;
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    super.reset();
  }

  @Override
  protected void initializeThreadPool() {
    executor = new ThreadPoolExecutor(0, 4, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(4));
  }

  @Override
  protected void setDefaultState() {
    sleepTime = 2000;
    setState(0);
  }

  protected String getSnippet() {
    String snippet = "<html><PRE><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// FixedThreadPool Construction</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>final</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> Executor executor = </FONT>\n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\">     <B>new</B> <FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\">ThreadPoolExecutor(0, 4, 1, TimeUnit.MINUTES, \n" +
            "               </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\">     <B>new</B> <FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> LinkedBlockingQueue<Runnable>(4));\n" +
            " \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Use the Executor to launch some Runnable </I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> \n" +
            "    executor.execute(</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>new</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> Runnable(){ \n" +
            "        </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>public</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>void</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> run(){ \n" +
            "          </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + ">\"><I>// do work</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> \n" +
            "        }}); \n"+
          " \n" +
          "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// RejectedExecutionHandler handler = ThreadPoolExecutor.CallerRunsPolicy(); </I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state5:#000000>\"> \n" +
          "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// RejectedExecutionHandler handler = ThreadPoolExecutor.DiscardPolicy();</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state5:#000000>\"> \n" +
          "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// RejectedExecutionHandler handler = ThreadPoolExecutor.DiscardOldestPolicy(); </I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state5:#000000>\"> \n" +
          "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// RejectedExecutionHandler handler = ThreadPoolExecutor.AbortPolicy(); </I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state5:#000000>\"> \n" +
          "    ((ThreadPoolExecutor) executor).setRejectedExecutionHandler(handler);\n" +
          " \n";
    return snippet;
  }

}
