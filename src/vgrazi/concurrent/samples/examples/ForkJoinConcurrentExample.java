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
    super(label, frame, ExampleType.WORKING, 660, false, slideShowIndex);
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
  protected String getSnippet() {
    return "<html><head><style type=\"text/css\"> \n" +
            ".ln { color: rgb(0,0,0); font-weight: normal; font-style: normal; }\n" +
            ".s0 { }\n" +
            ".s1 { color: rgb(128,128,128); font-style: italic; }\n" +
            ".s2 { color: rgb(0,0,128); font-weight: bold; }\n" +
            ".s3 { color: rgb(0,0,255); }\n" +
            ".s9 { color: rgb(128,128,128); }\n" +
            "</style> \n" +
            "</head>\n" +
            "<BODY BGCOLOR=\"#ffffff\">" +
            "<pre><span class=\"s1\">  /** \n" +
            "   * Calculate the array[14] maximum using Fork &amp; Join \n" +
            "   */</span><span class=\"<state1:s0>\"> \n" +
            "  </span><span class=\"<state0:s2>\">public int </span><span class=\"<state0:s0>\">findMax(</span><span class=\"<state0:s2>\">int</span>" +
            "<span class=\"<state0:s0>\">[] array, <span class=\"<state0:s2>\">int</span><span class=\"<state0:s0>\"> threadCount) { \n" +
            "    Solver solver = </span><span class=\"<state0:s2>\">new </span><span class=\"<state0:s0>\">Solver(array, </span><span class=\"<state0:s3>\">0</span><span class=\"<state0:s0>\">, array.length); \n" +
            "    ForkJoinPool pool = </span><span class=\"<state0:s2>\">new </span><span class=\"<state0:s0>\">ForkJoinPool(threadCount); \n" +
            "    pool.invoke(solver); \n" +
            "    </span><span class=\"<state4:s2>\">int </span><span class=\"<state4:s0>\">result = solver.result; \n" +
            "    </span><span class=\"<state4:s2>\">return </span><span class=\"<state4:s0>\">result; \n" +
            "  } \n" +
            "  </span><span class=\"<state0:s2>\">class </span><span class=\"<state0:s0>\">Solver </span><span class=\"<state0:s2>\">extends </span><span class=\"<state0:s0>\">RecursiveAction { \n" +
            "    </span><span class=\"<state0:s2>\">private int </span><span class=\"<state0:s0>\">start, end, result, array[]; \n" +
            "    </span><span class=\"<state0:s2>\">private </span><span class=\"<state0:s0>\">Solver(</span><span class=\"<state0:s2>\">int</span><span class=\"<state0:s0>\">[] array, </span>" +
            "<span class=\"<state0:s2>\">int </span><span class=\"<state0:s0>\">start, </span><span class=\"<state0:s2>\">int </span><span class=\"<state0:s0>\">end) { \n" +
            "      </span><span class=\"<state0:s2>\">this</span><span class=\"<state0:s0>\">.array = array; \n" +
            "      </span><span class=\"<state0:s2>\">this</span><span class=\"<state0:s0>\">.start = start; \n" +
            "      </span><span class=\"<state0:s2>\">this</span><span class=\"<state0:s0>\">.end = end; \n" +
            "    } \n" +
            "    @Override \n" +
            "    </span><span class=\"<state0:s2>\">protected void </span><span class=\"<state0:s0>\">compute() { \n" +
            "      </span><span class=\"<state2:s2>\">if</span><span class=\"<state2:s0>\">(end - start == </span><span class=\"<state2:s3>\">1</span><span class=\"<state2:s0>\">) { \n" +
            "        result = array[start]; \n" +
            "      } \n" +
            "      </span><span class=\"<state3:s2>\">else </span><span class=\"<state0:s0>\">{ \n" +
            "        </span><span class=\"<state3:s2>\">int </span><span class=\"<state3:s0>\">mid = (start + end)/</span><span class=\"<state3:s3>\">2</span><span class=\"<state3:s0>\">; \n" +
            "        Solver solver1 = </span><span class=\"<state3:s2>\">new </span><span class=\"<state3:s0>\">Solver(array, start, mid); \n" +
            "        Solver solver2 = </span><span class=\"<state3:s2>\">new </span><span class=\"<state3:s0>\">Solver(array, mid, end); \n" +
            "        invokeAll(solver1, solver2); \n" +
            "        result = Math.max(solver1.result, solver2.result); \n" +
            "      } \n" +
            "    } \n" +
            "  } \n" +
            "</span></pre>\n" +
            "</body>\n" +
            "</html>";
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

  public void setAnimating(boolean b) {
    animating = b;
    System.out.printf("ForkJoinConcurrentExample.setAnimating %s%n", b);
    getCanvas().notifyAnimationThread();
  }

  public boolean isAnimating() {
    return animating;
  }
}
