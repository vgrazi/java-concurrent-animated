package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.canvases.ForkJoinCanvas;
import vgrazi.concurrent.samples.examples.forkjoin.ForkJoinMaximumProblem;
import vgrazi.concurrent.samples.sprites.ForkJoinThread;

import javax.swing.*;
import java.awt.*;

public class ForkJoinConcurrentExample extends ConcurrentExample {
  private JButton button = new JButton("ForkJoinPool.invoke(ForkJoinTask)");
  private final JTextField threadCountField = createThreadCountField();
  private boolean initialized;
  private ForkJoinMaximumProblem problem;
  private boolean animating;

  public ForkJoinConcurrentExample(String label, Container frame, int slideShowIndex) {
    super(label, frame, ExampleType.WORKING, 490, false, slideShowIndex);
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


  protected void initializeComponents() {
    if (!initialized) {
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
        setAnimating(true);
        problem = new ForkJoinMaximumProblem(ForkJoinConcurrentExample.this, (ForkJoinCanvas) getCanvas(), 14, getThreadCount(threadCountField));
        problem.launch();
      }
    });
  }

  @Override
  protected String getSnippetText() {
    return  "<0 highlight>/** Challenge: Calculate the maximum\n" +
            "  * element of array[14] using Fork/Join*/<0 default><br/>" +
            " <0 keyword>public int <0 default>findMax(\n" +
            "    <0 keyword>int[] <0 default>array, <0 keyword>int <0 default>nthreads) {\n" +
            "   Solver solver = new Solver(array, 0,\n" +
            "                            array.length);\n" +
            "   ForkJoinPool pool \n" +
            "      = new ForkJoinPool(nthreads);\n" +
            "   pool.invoke(solver);\n" +
            "   <4 keyword>int <4 default>result = solver.result;\n" +
            "   return result;\n" +
            " }\n" +
            " <0 keyword>class Solver extends <0 default>RecursiveAction {\n" +
            "   private int start, end, result;\n" +
            "   private int array[]\n\n" +
            "   private Solver(int[] array, int start,\n" +
            "                     int end) {\n" +
            "     this.array = array;\n" +
            "     this.start = start;\n" +
            "     this.end = end;\n" +
            "   }\n" +
            "   @Override\n" +
            "   protected void compute() {\n" +
            "     <2 keyword>if(<2 default>end - start == <2 literal>1<2 default>) {\n" +
            "       result = array[start];\n" +
            "     }\n" +
            "     <3 keyword>else <3 default>{\n" +
            "       int mid = (start + end)/2;\n" +
            "       Solver solver1 = new Solver(array,\n" +
            "                            start, mid);\n" +
            "       Solver solver2 = new Solver(array,\n" +
            "                            mid, end);\n" +
            "       invokeAll(solver1, solver2);\n" +
            "       result = Math.max(solver1.result,\n" +
            "                         solver2.result);\n" +
            "     }\n" +
            "   }\n" +
            " }\n";
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
    setAnimating(true);
    if (problem != null) {
      problem.reset();
    }
    ForkJoinThread.reset();
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        setState(0);
        clearMessages();
      }
    });
  }

  /**
   * This returns the shift from the default snippet pane. Usually 0, but for F&J which needs more room, use a negative shift
   * @return the shift from the default snippet pane.
   */
  public int getVerticalOffsetShift() {
    return -30;
  }


  public void setAnimating(boolean b) {
    animating = b;
    System.out.printf("ForkJoinConcurrentExample.setAnimating %s%n", b);
    getCanvas().notifyAnimationThread();
  }

  public boolean isAnimating() {
    return animating;
  }
}
