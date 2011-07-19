package vgrazi.concurrent.samples.examples;

import jsr166y.Phaser;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

public class PhaserExample extends ConcurrentExample {

  private Phaser phaser;

  private final JButton arriveButton = new JButton("arrive()");
  private final JButton arriveAndAwaitButton = new JButton("arriveAndAwait()");
  private final JButton arriveAndDeregisterButton = new JButton("arriveAndDeregister()");
  private final JButton arriveAndAwaitAdvanceButton = new JButton("arriveAndAwaitAdvance()");
  private final JButton awaitAdvanceButton = new JButton("awaitAdvance(phase)");
  private final JButton awaitAdvanceWrongPhaseButton = new JButton("awaitAdvance(phase+1)");
  private final JButton registerButton = new JButton("register()");
  private final JButton bulkRegisterButton = new JButton("bulkRegister()");
  private boolean initialized;
  private static final int MIN_SNIPPET_POSITION = 400;
  private JTextField threadCountField = createThreadCountField();
  public PhaserExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.WORKING, MIN_SNIPPET_POSITION, false, slideNumber);
  }

  protected void initializeComponents() {
    if(!initialized) {
      initializeButton(arriveAndAwaitButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                arriveAndAwait();
              }
            });
          }
        }
      });
      initializeButton(arriveButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                arrive();
              }
            });
          }
        }
      });
      initializeButton(arriveAndDeregisterButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                arriveAndDeregister();
              }
            });
          }
        }
      });
      initializeButton(awaitAdvanceButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                awaitAdvanceThisPhase();
              }
            });
          }
        }
      });
      initializeButton(awaitAdvanceWrongPhaseButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                awaitAdvanceWrongPhase();
              }
            });
          }
        }
      });
      initializeButton(arriveAndDeregisterButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                arriveAndDeregister();
              }
            });
          }
        }
      });
      initializeButton(arriveAndAwaitAdvanceButton, new Runnable() {
        public void run() {
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            threadCountExecutor.execute(new Runnable() {
              public void run() {
                arriveAndAwaitAdvance();
              }
            });
          }
        }
      });
      initializeButton(registerButton, new Runnable() {
        public void run() {
          phaser.register();
          displayPhaseAndParties(phaser.getPhase());
        }
      });
      initializeButton(bulkRegisterButton, new Runnable() {
        public void run() {
          phaser.bulkRegister(getThreadCount());
          displayPhaseAndParties(phaser.getPhase());
        }
      });
      initializeThreadCountField(threadCountField);
      initialized = true;
    }
    reset();
  }

  private void arrive() {
    ConcurrentSprite sprite = null;
    setAnimationCanvasVisible(true);
    message1(String.format("Waiting for phaser (registered parties:%d)...", phaser.getRegisteredParties()), ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    setState(1);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    sprite = createAcquiringSprite();
    int phase = phaser.arrive();

    sprite.setReleased();
    setState((0));
  }

  private void arriveAndAwait() {
    ConcurrentSprite sprite = null;
    setAnimationCanvasVisible(true);
    message1(String.format("Waiting for phaser (registered parties:%d)...", phaser.getRegisteredParties()), ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
    setState(1);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    sprite = createAcquiringSprite();
    int phase = phaser.arriveAndAwaitAdvance();

    sprite.setReleased();
    setState((0));
  }

  private void arriveAndDeregister() {
    ConcurrentSprite sprite = null;
    setAnimationCanvasVisible(true);
    setState(1);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    sprite = createAcquiringSprite();
    int phase = phaser.arriveAndDeregister();
    displayPhaseAndParties(phase);

    sprite.setReleased();
    setState((0));
  }

  private void arriveAndAwaitAdvance() {
    ConcurrentSprite sprite = null;
    setAnimationCanvasVisible(true);
    setState(1);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    sprite = createAcquiringSprite();
    int phase = phaser.arriveAndAwaitAdvance();
    displayPhaseAndParties(phase);

    sprite.setReleased();
    setState((0));
  }

  private void awaitAdvanceThisPhase() {
    ConcurrentSprite sprite = null;
    setAnimationCanvasVisible(true);
    setState(1);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    sprite = createAcquiringSprite();
    sprite.setColor(ConcurrentExampleConstants.ATTEMPTING_COLOR);
    int phase = phaser.awaitAdvance(phaser.getPhase());
    displayPhaseAndParties(phase);

    sprite.setReleased();
    setState((0));
  }

  private void awaitAdvanceWrongPhase() {
    ConcurrentSprite sprite = null;
    setAnimationCanvasVisible(true);
    setState(1);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    sprite = createAcquiringSprite();
    sprite.setColor(ConcurrentExampleConstants.ATTEMPTING_COLOR);
    int phase = phaser.awaitAdvance(phaser.getPhase() + 1);
    displayPhaseAndParties(phase);

    sprite.setReleased();
    setState((0));
  }

  private void displayPhaseAndParties(int phase) {
    message1(String.format("Phase: %d Registered Parties:%d", phase, phaser.getRegisteredParties()), ConcurrentExampleConstants.MESSAGE_COLOR);
  }


  public String getDescriptionHtml() {
    StringBuffer sb = new StringBuffer();
    return sb.toString();

  }

  @Override
  public void reset() {
    super.reset();
    createPhaser();
    resetThreadCountField(threadCountField);    
    message1(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    setState(0);
  }

  private void createPhaser() {
    phaser = new Phaser(4) {
      @Override
      protected boolean onAdvance(int phase, int registeredParties) {
        String text = "Phase:" + phase + " Registered parties:" + registeredParties;
        System.out.println("PhaserExample.onAdvance: " + text);
        message1(text, ConcurrentExampleConstants.DEFAULT_BACKGROUND);
        return false;
      }
    };
  }
  protected String getSnippet() {
    final String snippet;
    snippet = "<html><PRE><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\">" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Contructor specifies number of parties, and an<br>    // optional Runnable that gets called when the</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// phaser is opened.</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>final</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> CyclicBarrier cyclicBarrier = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\">" +
       "<br>    <B>    new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> CyclicBarrier(4</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\">, </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> Runnable(){ \n" +
       "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> run(){ \n" +
       "        System.out.println(</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> Date() + </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#008000>\"><B>&quot; Runnable hit&quot;</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\">); \n" +
       "      } \n" +
//       "    }); \n" +
       " \n" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Each call to arriveAndAwait() blocks, until<br>    // the number specified in the constructor is reached.</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Then the Runnable is executed and all can pass. </I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\">" +
       "    <font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> \n" +
       "    Thread thread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> Thread(){ \n" +
       "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> run() { \n" +
       "        </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>try</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> { \n" +
       "          cyclicBarrier.arriveAndAwait(); \n" +
       "        } </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>catch</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">(BrokenBarrierException e) {} \n" +
//       "        } \n" +
//       "      } \n" +
//       "    });" +
       " \n" +
       "    <font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> \n" +
       "    Thread thread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> Thread(){ \n" +
       "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> run() { \n" +
       "        </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>try</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> { \n" +
       "          cyclicBarrier.arriveAndAwait(timeout, TimeUnit.MILLISECONDS); \n" +
       "        } </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>catch</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">(BrokenBarrierException e) {} \n" +
//       "        } \n" +
//       "      } \n" +
//       "    });" +
       " \n" +
            "    </FONT><font style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// reset() allows the phaser to be reused.</I></FONT><font style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><font style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Any waiting threads will throw a BrokenBarrierException</I></FONT><font style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    <font style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">cyclicBarrier.reset()</FONT><font style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">); \n" +
            "</FONT></PRE></html>";
    return snippet;
  }
}
