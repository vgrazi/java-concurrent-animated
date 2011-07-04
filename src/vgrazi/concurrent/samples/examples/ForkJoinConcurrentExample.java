package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.canvases.ForkJoinCanvas;
import vgrazi.concurrent.samples.examples.forkjoin.ForkJoinMaxProblem;

import javax.swing.*;
import java.awt.*;

public class ForkJoinConcurrentExample extends ConcurrentExample {
  JButton button = new JButton("ForkJoinPool.invoke(ForkJoinTask)");
  private final JTextField threadCountField = createThreadCountField();
  private boolean initialized;
  private ForkJoinMaxProblem problem;

  public ForkJoinConcurrentExample(String label, Container frame, int slideShowIndex) {
    super(label, frame, ExampleType.WORKING, 650, false, slideShowIndex);
  }

  protected void initializeComponents() {
    if(!initialized) {
      initializeExecuteButton();
      initializeThreadCountField(threadCountField);

      initialized = true;
    }
    resetThreadCountField(threadCountField, 3);
    reset();
  }

  private void initializeExecuteButton() {
    initializeButton(button, new Runnable() {
      public void run() {
        reset();
        problem = new ForkJoinMaxProblem(ForkJoinConcurrentExample.this, (ForkJoinCanvas) getCanvas(), 14, getThreadCount(threadCountField));
        problem.launch();
      }
    });
  }

  @Override
  protected String getSnippet() {
    return "";
  }

  @Override
  public String getDescriptionHtml() {
    return "";
  }

  protected void createCanvas() {
    setCanvas(new ForkJoinCanvas(this, getTitle()));
  }


  @Override
  public void reset() {
    super.reset();
    if(problem != null) {
      problem.reset();
    }
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    ((ForkJoinCanvas) getCanvas()).reset();
  }

  /**
   * @param title              the title to display in the title bar
   * @param container          the container to contain the animation
   * @param exampleType        the type of animation
   * @param minSnippetPosition the horizontal position to start the snippet frame
   * @param fair               true
   * @param slideNumber        when configured as a slide show, this indicates the slide number. -1 for exclude from slide show - will still show in menu bar
   */
  public ForkJoinConcurrentExample(String title, Container container, ExampleType exampleType, int minSnippetPosition, boolean fair, int slideNumber) {
    super(title, container, exampleType, minSnippetPosition, fair, slideNumber);
  }
}
