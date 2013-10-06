package vgrazi.concurrent.samples.examples;

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

  private final JButton launchButton = new JButton("Thread.start()");
  private final JButton getButton = new JButton("get");
  private Future<Object> future;
  private ConcurrentSprite sprite;

  private boolean initialized = false;

  public FutureExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.ONE_USE, 500, false, slideNumber);
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

  protected String getSnippetText() {
    return
            "<0 comment>" +
                    "  // Future objects are returned on submit\n" +
                    "  // to ExecutorService or can be created\n" +
                    "  // by constructing a FutureTask.\n" +
                    "\n" +
                    "  // The Future.get() method blocks\n" +
                    "  //   until some result is available.\n" +
                    "\n" +
                    "  <1 keyword>final<1 default> Future&nbsp;future =\n" +
                    "       Executors.newCachedThreadPool()\n" +
                    "         .submit(someCallable); \n\n" +
                    "<0 comment>" +
                    "  // OR\n" +
                    "\n" +
                    "  <1 default>FutureTask&lt;Callable> future =\n" +
                    "      <1 keyword>new<1 default> FutureTask<Callable>(someCallable);\n" +
                    "  <1 default>Thread thread = <1 keyword>new<1 default> Thread(futureTask);\n" +
                    "  <1 default>thread.start();\n" +
                    "<0 comment>\n\n\n" +
                    "  //  Finally, the Future task completes\n" +
                    "  //       and the block passes through.\n" +
                    "  <2 keyword>try{\n" +
                    "  <2 default>  Object result = future.get();\n"+
                    "  <2 keyword>} catch(<2 default>InterruptedException e<2 keyword>){...}\n";


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
