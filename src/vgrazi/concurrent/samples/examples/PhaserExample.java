package vgrazi.concurrent.samples.examples;

import jsr166y.Phaser;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

public class PhaserExample extends ConcurrentExample {

  private Phaser phaser;

  private final JButton arriveButton = new JButton("arrive()");
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

      initializeButton(registerButton, new Runnable() {
        public void run() {
          setState(5);
          phaser.register();
          displayPhaseAndParties();
        }
      });
      initializeButton(bulkRegisterButton, new Runnable() {
        public void run() {
          setState(6);
          phaser.bulkRegister(getThreadCount());
          displayPhaseAndParties();
        }
      });
      initializeThreadCountField(threadCountField);
      initialized = true;
    }
    reset();
  }

  private void arrive() {
    methodSetup(null, 1, new Callable<Integer>() {
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
    methodSetup(null, 3, new Callable<Integer>() {
      public Integer call() throws Exception {
        return phaser.arriveAndAwaitAdvance();
      }
    });
  }

  private void awaitAdvanceThisPhase() {
    methodSetup(ConcurrentExampleConstants.ATTEMPTING_COLOR, 4, new Callable<Integer>() {
      public Integer call() throws Exception {
        return phaser.awaitAdvance(phaser.getPhase());
      }
    });
  }

  private void awaitAdvanceWrongPhase() {
    methodSetup(ConcurrentExampleConstants.ATTEMPTING_COLOR, 4, new Callable<Integer>() {
      public Integer call() throws Exception {
        return phaser.awaitAdvance(phaser.getPhase() + 1);
      }
    });
  }

  private void methodSetup(Color spriteColor, int state, Callable<Integer> phaserMethod) {
    ConcurrentSprite sprite = createAcquiringSprite();
    try {
      setState(state);
      message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
      if (spriteColor != null) {
        sprite.setColor(spriteColor);
      }
      displayPhaseAndParties();
      int phase = phaserMethod.call();
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
        message1(String.format("Phase: %d \tRegistered:%d \tArrived:%d \tUnarrived:%d", phaser.getPhase(), phaser.getRegisteredParties(), phaser.getArrivedParties(), phaser.getUnarrivedParties()), ConcurrentExampleConstants.MESSAGE_COLOR);

      }
    }, 500, TimeUnit.MILLISECONDS);
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
        message1(String.format("Phase: %d \tRegistered:%d \tArrived:%d \tUnarrived:%d", phaser.getPhase(), phaser.getRegisteredParties(), phaser.getArrivedParties(), phaser.getUnarrivedParties()), ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
        setState(7);
        return false;
      }
    };
  }

  protected String getSnippetText() {
    return
    "<format state=0 class=default>\n" +
    "   Phaser phaser = </format><format state=0 class=keyword>new </format><format state=0 class=default>Phaser(</format><format state=0 class=literal>4</format><format state=0 class=default>) { \n" +
    "      <format state=7 class=default>@Override</format> \n" +
    "     </format><format state=7 class=comment>// Perform when all parties arrive</format><format state=7 class=default> \n" +
    "     </format><format state=7 class=keyword>protected boolean </format><format state=7 class=default>onAdvance(</format><format state=7 class=keyword>int </format><format state=7 class=default>phase, \n" +
    "                         </format><format state=7 class=keyword>int </format><format state=7 class=default>registeredParties) { \n" +
    "       </format><format state=7 class=comment>// return true if the Phaser should\n" +
    "       // terminate on advance, else false</format><format state=7 class=default> \n" +
    "       </format><format state=7 class=keyword>return false</format><format state=7 class=default>; \n" +
    "     } \n" +
    "<format state=0 class=keyword>   };</format> \n" +
    "    \n" +
    "   <format state=3 class=keyword>int </format><format state=3 class=default>phase = phaser.arriveAndAwaitAdvance();</format> \n" +
    "\n" +
    "   <format state=1 class=keyword>int </format><format state=1 class=default>phase = phaser.arrive();</format> \n" +
    "    \n" +
    "   <format state=4 class=keyword>int </format><format state=4 class=default>phase = phaser.awaitAdvance(</format><format state=4 class=keyword>int </format><format state=4 class=default>phase</format>); \n" +
    "\n" +
    "   <format state=2 class=keyword>int </format><format state=2 class=default>phase = phaser.arriveAndDeregister();</format> \n" +
    "\n" +
    "   <format state=5 class=keyword>int </format><format state=5 class=default>phase = phaser.register();</format>\n" +
    "    \n" +
    "   <format state=6 class=keyword>int </format><format state=6 class=default>phase = phaser.bulkRegister(</format><format state=6 class=keyword>int </format><format state=6 class=default>parties);</format>";

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
