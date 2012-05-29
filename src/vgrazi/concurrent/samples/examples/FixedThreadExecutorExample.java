package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FixedThreadExecutorExample extends ExecutorsExample {

  /**
   * Each example must have a unique slide show index (or -1). Indexes must start with 0 and must be in sequence, no skipping
   *
   * @param label          the label to display at the top of the
   * @param frame          the launcher frame to display the example
   * @param slideShowIndex when configured as a slide show, this indicates the slide number. -1 for exclude from slide show - will still show in menu bar
   */
  public FixedThreadExecutorExample(String label, Container frame, int slideShowIndex) {
    super(label, frame, slideShowIndex);
  }

  protected void initializeComponents() {
    if (!initialized) {
      initializeExecuteButton();
      initializePrestartButton();
      initializeThreadCountField(threadCountField);

      initialized = true;
    }
    reset();
  }

  @Override
  public void reset() {
    ExecutorService executor = getExecutor();
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
//    getAnimationCanvas().setLabelText("FixedThreadPooledExecutor");
    setExecutor(Executors.newFixedThreadPool(4));
  }

  @Override
  protected void setDefaultState() {
    sleepTime = 2000;
    setState(0);
  }

  @Override
  protected String getSnippetText() {
    return  "  <0 comment>// FixedThreadPool Construction\n" +
            "  <0 keyword>final <0 default>Executor executor =\n" +
            "          Executors.newFixedThreadPool(<0 literal>4<0 default>);\n" +
            "  <3 comment>// Use the Executor to execute a Runnable\n" +
            "  <3 default>executor.execute(<3 keyword>new <3 default>Runnable() {\n" +
            "    <3 keyword>@Override\n" +
            "    public void <3 default>run() {\n" +
            "      <3 comment>// Do work\n" +
            "    <3 default>}});\n" +
            "\n" +
            "  <4 comment>// Prestarting Core Threads\n" +
            "  <4 keyword>int <4 default>count = ((ThreadPoolExecutor)executor)\n" +
            "          .prestartAllCoreThreads();\n";
  }
}
