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
    return "<html><style type=\"text/css\"> \n" +
        ".ln { color: rgb(0,0,0); font-weight: normal; font-style: normal; }\n" +
        ".s0 { }\n" +
        ".s1 { color: rgb(128,128,128); font-style: italic; }\n" +
        ".s2 { color: rgb(0,0,128); font-weight: bold; }\n" +
        ".s3 { color: rgb(0,0,255); }\n" +
        "</style> \n" +
        "</head>\n" +
        "<BODY BGCOLOR=\"#ffffff\">" +
        "<pre><span class=\"s1\">  /** \n" +
        "   * Calculate the array maximum using Fork &amp; Join \n" +
        "   */</span><span class=\"s0\"> \n" +
        "  </span><span class=\"s2\">public int </span><span class=\"s0\">findMax(</span><span class=\"s2\">int</span><span class=\"s0\">[] array, <span class=\"s2\">int</span><span class=\"s0\"> threadCount) { \n" +
        "    Solver solver = </span><span class=\"s2\">new </span><span class=\"s0\">Solver(array, </span><span class=\"s3\">0</span><span class=\"s0\">, array.length); \n" +
        "    ForkJoinPool pool = </span><span class=\"s2\">new </span><span class=\"s0\">ForkJoinPool(threadCount); \n" +
        "    pool.invoke(solver); \n" +
        "    </span><span class=\"s2\">int </span><span class=\"s0\">result = solver.result; \n" +
        "    </span><span class=\"s2\">return </span><span class=\"s0\">result; \n" +
        "  } \n" +
        "  </span><span class=\"s2\">class </span><span class=\"s0\">Solver </span><span class=\"s2\">extends </span><span class=\"s0\">RecursiveAction { \n" +
        "    </span><span class=\"s2\">private int </span><span class=\"s0\">start, end, result, array[]; \n" +
        "    </span><span class=\"s2\">private </span><span class=\"s0\">Solver(</span><span class=\"s2\">int</span><span class=\"s0\">[] array, </span><span class=\"s2\">int </span><span class=\"s0\">start, </span><span class=\"s2\">int </span><span class=\"s0\">end) { \n" +
        "      </span><span class=\"s2\">this</span><span class=\"s0\">.array = array; \n" +
        "      </span><span class=\"s2\">this</span><span class=\"s0\">.start = start; \n" +
        "      </span><span class=\"s2\">this</span><span class=\"s0\">.end = end; \n" +
        "    } \n" +
        "    @Override \n" +
        "    </span><span class=\"s2\">protected void </span><span class=\"s0\">compute() { \n" +
        "      </span><span class=\"s2\">if</span><span class=\"s0\">(end - start == </span><span class=\"s3\">1</span><span class=\"s0\">) { \n" +
        "        result = array[start]; \n" +
        "      } \n" +
        "      </span><span class=\"s2\">else </span><span class=\"s0\">{ \n" +
        "        </span><span class=\"s2\">int </span><span class=\"s0\">mid = (start + end)/</span><span class=\"s3\">2</span><span class=\"s0\">; \n" +
        "        Solver solver1 = </span><span class=\"s2\">new </span><span class=\"s0\">Solver(array, start, mid); \n" +
        "        Solver solver2 = </span><span class=\"s2\">new </span><span class=\"s0\">Solver(array, mid, end); \n" +
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
