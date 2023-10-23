package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.*;
import vgrazi.concurrent.samples.canvases.*;
import vgrazi.concurrent.samples.sprites.*;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

public class PhaserExample extends ConcurrentExample {

  private Phaser phaser;

  private final JButton arriveButton = new JButton("arrive()");
  private final JButton arriveAndDeregisterButton = new JButton("arriveAndDeregister()");
  private final JButton arriveAndAwaitAdvanceButton = new JButton("arriveAndAwaitAdvance()");
  private final JButton awaitAdvanceButton = new JButton("awaitAdvance()");
  //  private final JButton awaitAdvanceWrongPhaseButton = new JButton("awaitAdvance(other_phase)");
  private final JButton registerButton = new JButton("register()");
  private final JButton bulkRegisterButton = new JButton("bulkRegister()");
  private boolean initialized;
  private JTextField threadCountField = createThreadCountField();

  public PhaserExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.WORKING, 550, false, slideNumber);
  }

  protected void createCanvas() {
    setCanvas(new BasicCanvas(this, getTitle()));
  }

  protected void initializeComponents() {
    if (!initialized) {
      initializeButton(registerButton, new Runnable() {
        public void run() {
          setState(5);
          phaser.register();
          displayPhaseAndParties();
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
      addButtonSpacer();

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

//      Dimension size = new Dimension(220, arriveAndAwaitAdvanceButton.getPreferredSize().height);
//      arriveAndAwaitAdvanceButton.setPreferredSize(size);
//      awaitAdvanceButton.setPreferredSize(size);
//      awaitAdvanceWrongPhaseButton.setPreferredSize(size);
//      arriveButton.setPreferredSize(size);

//      Dimension smallerSize = new Dimension(129, arriveAndAwaitAdvanceButton.getPreferredSize().height);
//      arriveAndDeregisterButton.setPreferredSize(smallerSize);
//      registerButton.setPreferredSize(smallerSize);
//      bulkRegisterButton.setPreferredSize(smallerSize);


      initialized = true;
    }
    reset();
  }

  private void arrive() {
    methodSetup(Thread.State.RUNNABLE, 1, new Callable<Integer>() {
      public Integer call() throws Exception {
        return phaser.arrive();
      }
    });
  }

  private void arriveAndDeregister() {
    methodSetup(null, 2, new Callable<Integer>() {
      public Integer call() throws Exception {
        return phaser.arriveAndDeregister();
      }
    });
  }

  private void arriveAndAwaitAdvance() {
    methodSetup(Thread.State.WAITING, 3, new Callable<Integer>() {
      public Integer call() throws Exception {
        return phaser.arriveAndAwaitAdvance();
      }
    });
  }

  private void awaitAdvanceThisPhase() {
    methodSetup(Thread.State.WAITING, 4, new Callable<Integer>() {
      public Integer call() throws Exception {
        return phaser.awaitAdvance(phaser.getPhase());
      }
    });
  }

  private void awaitAdvanceWrongPhase() {
    methodSetup(Thread.State.RUNNABLE, 4, new Callable<Integer>() {
      public Integer call() throws Exception {
        return phaser.awaitAdvance(phaser.getPhase() + 1);
      }
    });
  }

  private void methodSetup(Thread.State threadState, int state, Callable<Integer> phaserMethod) {
    ConcurrentSprite sprite = createAcquiringSprite();
    try {
      setState(state);
      message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
      if (threadState != null) {
        sprite.setThreadState(threadState);
      }
      displayPhaseAndParties();
      int phase = phaserMethod.call();
      sprite.setThreadState(Thread.State.RUNNABLE);
    } catch (Exception e) {
      message1(e.getMessage(), ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
      sprite.setRejected();
    }

    sprite.setReleased();
  }

  /**
   * Displays Phase, # registered parties, # arrived parties, and # unarrived parties.
   * Note: This can be called before an awaiting call. It is scheduled for a few ms later so that the await can finish before this is called
   */
  private void displayPhaseAndParties() {
    scheduledExecutor.schedule(new Runnable() {
      public void run() {
        showPhaseAndParties();

      }
    }, 500, TimeUnit.MILLISECONDS);
  }

  private void showPhaseAndParties(Color color) {
    message1(String.format("Phase: %d  Registered: %d  Arrived: %d  Unarrived: %d", phaser.getPhase(), phaser.getRegisteredParties(), phaser.getArrivedParties(), phaser.getUnarrivedParties()), color);
  }
  private void showPhaseAndParties() {
    showPhaseAndParties(ConcurrentExampleConstants.MESSAGE_COLOR);
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
    showPhaseAndParties();
    setState(0);
  }

  private void createPhaser() {
    phaser = new Phaser(4) {
      @Override
      protected boolean onAdvance(int phase, int registeredParties) {
        showPhaseAndParties();
        showPhaseAndParties(ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
        setState(7);
        return false;
      }
    };
  }

  protected String getSnippetText() {
    return "";
    }

  protected String getSnippetTextOff() {
    return
        "<0 default>\n" +
            " Phaser phaser = <0 keyword>new <0 default>Phaser(<0 literal>4<0 default>) { \n" +
            "    <7 default>@Override \n" +
            "   <7 comment>// Perform when all parties arrive<7 default> \n" +
            "   <7 keyword>protected boolean <7 default>onAdvance(<7 keyword>int <7 default>phase, \n" +
            "                       <7 keyword>int <7 default>registeredParties) { \n" +
            "     <7 comment>// return true if the Phaser should\n" +
            "     // terminate on advance, else false<7 default> \n" +
            "     <7 keyword>return false<7 default>; \n" +
            "   } \n" +
            "<0 default>   }; \n" +
            "  \n" +
            " <3 keyword>int <3 default>phase = phaser.arriveAndAwaitAdvance(); \n" +
            "\n" +
            " <1 keyword>int <1 default>phase = phaser.arrive(); \n" +
            "  \n" +
            " <4 keyword>int <4 default>phase = phaser.awaitAdvance(<4 keyword>int <4 default>phase); \n" +
            "\n" +
            " <2 keyword>int <2 default>phase = phaser.arriveAndDeregister(); \n" +
            "\n" +
            " <5 keyword>int <5 default>phase = phaser.register();\n" +
            "  \n" +
            " <6 keyword>int <6 default>phase = phaser.bulkRegister(<6 keyword>int <6 default>parties);";
  }

//  private void snippetText() {
//    Phaser phaser = new Phaser(4) {
//      @Override
//      // Perform when all parties arrive
//      protected boolean onAdvance(int phase, int registeredParties) {
//        // return true if the Phaser should terminate on advance, else false
//        return false;
//      }
//    };
//
//    phaser.arrive();
//
//    phaser.arriveAndDeregister();
//
//    phaser.arriveAndAwaitAdvance();
//
////    phaser.awaitAdvance(int phase);
//
//    phaser.register();
//
////    phaser.bulkRegister(int parties);
//  }


}
