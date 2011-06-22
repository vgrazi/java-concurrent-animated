package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

public class FutureExample extends ConcurrentExample {

  private final JButton launchButton = new JButton("(Launch)");
  private final JButton getButton = new JButton("get");
  private Future<Object> future;
  private ConcurrentSprite sprite;

  private boolean initialized = false;
  private static final int MIN_SNIPPET_POSITION = 400;

  public FutureExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.ONE_USE, MIN_SNIPPET_POSITION, false, slideNumber);
  }

  private void launchAcquiringSprite() throws ExecutionException, InterruptedException {
    setAnimationCanvasVisible(true);

//    sprite = createAcquiringSprite(ConcurrentSprite.SpriteType.OVAL);
    sprite = createAcquiringSprite();
    sprite.setType(ConcurrentSprite.SpriteType.RUNNABLE);
    sprite.setAcquired();
    sprite.moveToAcquiringBorder();
    future = Executors.newCachedThreadPool().submit(new Callable<Object>() {
      public Object call() throws Exception {
        try {
          Thread.sleep(2000);
          sprite.setActionCompleted();
        }
        catch(InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        return "execution completed.";
      }
    });


    //    Object result = future.get();
//    sprite.setReleased();
  }

  public String getTitle() {
    return "Future";
  }

  protected String getSnippet() {
    return "<html><PRE><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Future objects are returned on submit to ExecutorService</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>//   or can be created by constructing a FutureTask.</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       " \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// The Future.get() method blocks</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>//   until some result is available.</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       " \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// ExecutorService submit() returns a Future object</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>final</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> final Future&lt;Object> future = </FONT>\n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">     Executors.newCachedThreadPool().submit(someCallable); \n" +
       " \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\">//  Finally, the Future task completes</FONT>\n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\">//       and the block passes through.</FONT>\n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">Object result = future.get(); \n" +
            " \n" +
       "    </FONT></PRE></html";
  }

  protected void initializeComponents() {
    if(!initialized) {
      initializeButton(launchButton, new Runnable() {
        public void run() {
          try {
            setState(1);
            enableSetButton();
            launchAcquiringSprite();
          } catch(ExecutionException e) {
            e.printStackTrace();
          } catch(InterruptedException e) {
            e.printStackTrace();
          }
        }
      });
      initializeButton(getButton, new Runnable() {
        public void run() {
          getButton.setEnabled(false);
          setState(2);
          if(future != null) {
            try {
              ConcurrentSprite pullerSprite = createPullingSprite(sprite);
              future.get();
              if (sprite != null) {
                sprite.setReleased();
                pullerSprite.setReleased();
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
              e.printStackTrace();
            }
          }
          // select a random mutex from the list

        }
      });
      initialized = true;
    }
  }

  @Override
  public void spriteRemoved(ConcurrentSprite sprite) {
    if (sprite.getType() != ConcurrentSprite.SpriteType.PULLER) {
      bumpMutexVerticalIndex();
      enableGetButton();
    }
  }

  public String getDescriptionHtml() {
    StringBuffer sb = new StringBuffer();
    return sb.toString();
  }

  @Override
  public void reset() {
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    resetMutexVerticalIndex();
    setState(0);
    enableGetButton();
    super.reset();
  }

  private void enableGetButton() {
    getButton.setEnabled(false);
    launchButton.setEnabled(true);
    launchButton.requestFocus();
  }

  private void enableSetButton() {
    getButton.setEnabled(true);
    launchButton.setEnabled(false);
    getButton.requestFocus();
  }
}
