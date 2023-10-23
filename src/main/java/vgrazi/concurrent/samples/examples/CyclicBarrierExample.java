package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.canvases.BasicCanvas;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

public class CyclicBarrierExample extends ConcurrentExample {

  private CyclicBarrier barrier;

  private final JButton awaitButton = new JButton("await()");
  private final JButton attemptButton = new JButton("await(timeout)");
  private final JButton createButton = new JButton("barrier.reset()");
  private boolean initialized;
//  private JTextField threadCountField = createThreadCountField();
  public CyclicBarrierExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.WORKING, 548, false, slideNumber);
  }

  protected void createCanvas() {
    setCanvas(new BasicCanvas(this, getTitle()));
  }

  protected void initializeComponents() {
    if (!initialized) {
      initializeButton(awaitButton, new Runnable() {
        public void run() {
          int count = 1;//getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                await();
              }
            });
          }
        }
      });
      initializeButton(attemptButton, new Runnable() {
        public void run() {
          int count = 1;//getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                attempt();
              }
            });
          }
        }
      });
      initializeButton(createButton, new Runnable() {
        public void run() {
          reset();
          setState(3);
        }
      });
      initialized = true;
    }
    reset();
  }

  private void await() {
    ConcurrentSprite sprite = null;
    try {
      setAnimationCanvasVisible(true);
      message1("Waiting for barrier... ", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      message2("Parties outstanding " + (barrier.getParties() - barrier.getNumberWaiting() - 1), ConcurrentExampleConstants.MESSAGE_COLOR);
      setState(1);
      sprite = createAcquiringSprite();
      sprite.setThreadState(Thread.State.WAITING);
      barrier.await();
      sprite.setThreadState(Thread.State.RUNNABLE);
      sprite.setReleased();
      setState((0));
      message1("barrier complete", ConcurrentExampleConstants.MESSAGE_COLOR);
    } catch (BrokenBarrierException e) {
      System.out.println("CyclicBarrierExample.attempt " + e);
      message1("BrokenBarrierException.", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
      message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
      sprite.setThreadState(Thread.State.RUNNABLE);
      sprite.setRejected();
      resetBarrier();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void attempt() {
    ConcurrentSprite sprite = null;
    try {
      setAnimationCanvasVisible(true);
      message1("Waiting for barrier...", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
      message2("Parties outstanding " + (barrier.getParties() - barrier.getNumberWaiting() - 1), ConcurrentExampleConstants.MESSAGE_COLOR);
      setState(2);
      sprite = createAttemptingSprite();
      sprite.setThreadState(Thread.State.TIMED_WAITING);

      barrier.await(timeout, TimeUnit.MILLISECONDS);
      sprite.setReleased();
      sprite.setThreadState(Thread.State.RUNNABLE);
      message1("barrier complete", ConcurrentExampleConstants.MESSAGE_COLOR);
    } catch (BrokenBarrierException e) {
      System.out.println("CyclicBarrierExample.attempt " + e);
      message1("BrokenBarrierException.", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
      message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
      sprite.setRejected();
      sprite.setThreadState(Thread.State.RUNNABLE);
      resetBarrier();
    } catch (TimeoutException e) {
      sprite.setRejected();
      sprite.setThreadState(Thread.State.RUNNABLE);
      message1("TimeoutException.", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
      message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
      System.out.println("CyclicBarrierExample.attempt " + e);
      resetBarrier();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void resetBarrier() {
    // todo: note: there seems to be a bug in barrier.reset() - it occasionally allows the next attempting thread to release
//    createBarrier();
//    barrier.reset();
  }

  public String getDescriptionHtml() {
    StringBuffer sb = new StringBuffer();
//    sb.append("<html>");
//    sb.append("<table border=\"0\"><tr><td>");
//    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_FONT_COLOR + "\">");
//
//    sb.append("A <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>CyclicBarrier</code></font> is instantiated with a specified number of parties, ");
//    sb.append("and an optional <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Runnable</code></font> that gets called once when a Barrier is passed.<p><p>");
//    sb.append("Threads call <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Barrier.await()</code></font>, which blocks until all parties have signaled.<p><p>");
//    sb.append("Once a CyclicBarrier has been released, subsequent calls to <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>Barrier.barrier()</code></font> start the process again.<p><p>");
//
//    sb.append("</td></tr></table>");
//    sb.append("</html>");

    return sb.toString();

  }

  @Override
  public void reset() {
    super.reset();
    createBarrier();
//    resetThreadCountField(threadCountField);
    message1(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
//    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    message2("Parties outstanding " + barrier.getParties(), ConcurrentExampleConstants.MESSAGE_COLOR);
    setState(0);
  }

  private void createBarrier() {
    if (barrier != null) {
      barrier.reset();
    } else {
      barrier = new CyclicBarrier(4, new Runnable() {
        public void run() {
          setState(2);
          message2("Runnable called", ConcurrentExampleConstants.MESSAGE_COLOR);
        }
      });
    }
  }

  protected String getSnippet() {
    final String snippet;
    snippet = "<html><PRE><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\">" +
        " </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Contructor specifies # of parties, and an<br>" +
        " // optional Runnable that gets called when the</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
        " </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// barrier is opened.</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
        " </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>final</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> CyclicBarrier cyclicBarrier = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\">" +
        "<br>    <B>    new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> CyclicBarrier(4</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\">, </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\">Runnable(){<br/>\n" +
        " </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> run(){ \n" +
        "   System.out.println(</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#008000>\"><B>&quot; Runnable hit&quot;</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\">); \n" +
        " } \n" +
//       "    }); \n" +
        " \n" +
        " </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>" +
        "// Each call to await blocks, until the number<br>" +
        " // specified in the constructor is reached.</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
        " </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>" +
        "// Then the Runnable executes and all can pass. </I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\">" +
        " <font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> \n" +
        " Thread thread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> Thread(){ \n" +
        " </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> run() { \n" +
        "   </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>try</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> { \n" +
        "     cyclicBarrier.await(); \n" +
        "   } </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>catch</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">(BrokenBarrierException e) {} \n" +
//       "        } \n" +
//       "      } \n" +
//       "    });" +
        " \n" +
        " <font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> \n" +
        " Thread thread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> Thread(){ \n" +
        "   </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> run() { \n" +
        "   </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>try</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> { \n" +
        "     cyclicBarrier.await(timeout,\n" +
        "                          TimeUnit.SECONDS); \n" +
        "   } </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>catch</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">(BrokenBarrierException e) {} \n" +
//       "        } \n" +
//       "      } \n" +
//       "    });" +
        " \n" +
        " </FONT><font style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// reset() allows the barrier to be reused.</I></FONT><font style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
        " </FONT><font style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Any waiting threads will throw \n" +
        " // a BrokenBarrierException</I></FONT><font style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
        " <font style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">cyclicBarrier.reset()</FONT><font style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">); \n" +
        "</FONT></PRE></html>";
    return snippet;
  }
}
