package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;

import java.awt.*;
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
    if (getExecutor() != null) {
      getExecutor().shutdownNow();
    }
    initializeThreadPool();
    setState(1);
    resetThreadCountField(threadCountField, 10);
    currentSaturationHandler = "AbortPolicy";
    nextIndex = 1;
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    super.reset();
  }

  protected void initializeComponents() {
    if (!initialized) {
      initializeExecuteButton();
      initializeThreadCountField(threadCountField);
      initializeSaturationPolicyButtons();

      initialized = true;
    }
    reset();
  }

  @Override
  protected void initializeThreadPool() {
    setExecutor(new ThreadPoolExecutor(0, 4, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(4)));
  }

  @Override
  protected void setDefaultState() {
    sleepTime = 2000;
    setState(1);
  }

  public String getSnippetText() {
    String snippet = "" +
            "<1 highlight>  Runnables are kept in a Queue until they can be handled.\n" +
            "  (These are independent of the running threads, \n" +
            "    which are removed from the queue to execute.)\n\n" +
            "  By default, the Executors factory methods use unbounded Queues.\n" +
            "  However you can create your own ThreadPoolExecutor and pass in a \n" +
            "  bounded Queue.\n\n" +
            "  If that Queue becomes saturated (filled to capacity with Runnables),\n" +
            "   then a runtime RejectedExecutionException will be thrown.\n\n" +
            "  To prevent that exception, pass in a saturation policy, as shown.\n" +
            "  Instantiate a saturation policy, and if desired, supply a \n" +
            "  rejectedExecution method for more fine grained handling.<0 default>\n\n\n" +
            "" +
            "  <1 keyword>final <1 default>Executor executor = \n" +
            "     <1 keyword>new <1 default>ThreadPoolExecutor(0, 4, 1, TimeUnit.MINUTES, \n" +
            "               <1 keyword>new <1 default>LinkedBlockingQueue(4));\n" +
            "\n\n" +
            "  <3 comment>// Use the Executor to launch some Runnable\n" +
            "  <3 default>executor.execute(<3 keyword>new <3 default>Runnable(){\n" +
            "      <3 keyword>public void <3 default>run() {\n" +
            "           <3 comment>// do work\n" +
            "      <3 default>}});\n" +
            "\n\n" +
            "  <5 default>RejectedExecutionHandler handler =\n" +
            "      <w keyword>new <w default>ThreadPoolExecutor.CallerRunsPolicy() {\n" +
            "         public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {\n" +
            "             <0 comment>// optionally - do something with the rejected Runnable\n" +
            "         <w default>}}\n" +
            "       <x keyword>new <x default>ThreadPoolExecutor.DiscardPolicy();\n" +
            "       <y keyword>new <y default>ThreadPoolExecutor.DiscardOldestPolicy();\n" +
            "       <z keyword>new <z default>ThreadPoolExecutor.AbortPolicy(); <z comment>//Default policy, throws \n" +
            "          //RejectedExecutionException\n" +
            "  <5 default>((ThreadPoolExecutor) executor).setRejectedExecutionHandler(handler);\n";
    if("CallerRunsPolicy".equals(currentSaturationHandler)) {
      snippet = snippet.replaceAll("<w", "<" + getState());
    }
    else if("DiscardPolicy".equals(currentSaturationHandler)) {
      snippet = snippet.replaceAll("<x", "<" + getState());
    }
    else if("DiscardOldestPolicy".equals(currentSaturationHandler)) {
      snippet = snippet.replaceAll("<y", "<" + getState());
    }
    else if("AbortPolicy".equals(currentSaturationHandler)) {
      snippet = snippet.replaceAll("<z", "<" + getState());
    }
    snippet = snippet.replaceAll("<[w-z]", "<0");
    return snippet;
  }
}
