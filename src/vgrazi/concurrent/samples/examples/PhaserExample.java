package vgrazi.concurrent.samples.examples;

import jsr166y.Phaser;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;

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
          displayPhaseAndParties(phaser.getPhase());
        }
      });
      initializeButton(bulkRegisterButton, new Runnable() {
        public void run() {
          setState(6);
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
      int phase = phaserMethod.call();
      displayPhaseAndParties(phase);
    } catch (Exception e) {
      message1(e.getMessage(), ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
      sprite.setRejected();
    }

    sprite.setReleased();
  }

  private void displayPhaseAndParties(int phase) {
    message1(String.format("Phase: %d \tRegistered:%d \tArrived:%d", phase, phaser.getRegisteredParties(), phaser.getArrivedParties()), ConcurrentExampleConstants.MESSAGE_COLOR);
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
        setState(7);
        return false;
      }
    };
  }
  protected String getSnippet() {
    final String snippet;
    snippet="<html><head><style type=\"text/css\"> \n" +
            ".ln { color: rgb(0,0,0); font-weight: normal; font-style: normal; }\n" +
            ".s0 { }\n" +
            ".s1 { color: rgb(0,0,128); font-weight: bold; }\n" +
            ".s2 { color: rgb(0,0,255); }\n" +
            ".s3 { color: rgb(128,128,128); font-style: italic; }\n" +
            ".s9 { color: rgb(128,128,128); }\n" +
            "</style> \n" +
            "</head>\n" +
            "<BODY BGCOLOR=\"#ffffff\">\n" +
            "<pre>\n" +
            "<span class=\"<state0:s0>\"> \n" +
            "   Phaser phaser = </span><span class=\"<state0:s1>\">new </span><span class=\"<state0:s0>\">Phaser(</span><span class=\"<state0:s2>\">4</span><span class=\"<state0:s0>\">) { \n" +
            "      <span class=\"<state7:s1>\">@Override</span> \n" +
            "     </span><span class=\"<state7:s3>\">// Perform when all parties arrive</span><span class=\"s0\"> \n" +
            "     </span><span class=\"<state7:s1>\">protected boolean </span><span class=\"<state7:s0>\">onAdvance(</span><span class=\"<state7:s1>\">int </span><span class=\"<state7:s0>\">phase, </span><span class=\"<state7:s1>\">int </span><span class=\"<state7:s0>\">registeredParties) { \n" +
            "       </span><span class=\"<state7:s3>\">// return true if the Phaser should terminate on advance, else false</span><span class=\"<state7:s0>\"> \n" +
            "       </span><span class=\"<state7:s1>\">return false</span><span class=\"<state7:s0>\">; \n" +
            "     } \n" +
            "<span class=\"<state0:s1>\">   };</span> \n" +
            "    \n" +
            "   <span class=\"<state3:s0>\">phaser.arriveAndAwaitAdvance();</span> \n" +
            "\n" +
            "   <span class=\"<state1:s0>\">phaser.arrive();</span> \n" +
            "    \n" +
            "   <span class=\"<state4:s0>\">phaser.awaitAdvance(</span><span class=\"<state4:s1>\">int </span><span class=\"<state4:s0>\">phase); \n" +
            "\n" +
            "   <span class=\"<state2:s0>\">phaser.arriveAndDeregister();</span> \n" +
            "\n" +
            "   <span class=\"<state5:s0>\">phaser.register();</span>\n" +
            "    \n" +
            "   <span class=\"<state6:s0>\">phaser.bulkRegister(</span><span class=\"<state6:s1>\">int </span><span class=\"<state6:s0>\">parties);</span></pre>\n" +
            "</body>\n" +
            "</html>";
    return snippet;
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
