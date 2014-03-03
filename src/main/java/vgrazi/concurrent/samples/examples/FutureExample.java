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
  private final JButton cancelButton = new JButton("cancel");
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
          Thread.sleep(4000);
          sprite.setActionCompleted();
        } catch (InterruptedException e) {
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
        "  <2 default>  Object result = future.get();\n" +
        "  <2 keyword>} catch(<2 default>InterruptedException e<2 keyword>){...}\n" +
        "  <3 keyword>} catch(<3 default>CancellationException e<3 keyword>){...}\n" +
        "<3 comment>\n\n\n" +
        "  //  A Future may be canceled before it has completed\n" +
        "  <3 keyword>try{\n" +
        "  <3 default>  boolean canceled = future.cancel();\n";


  }

  protected void initializeComponents() {
    if (!initialized) {
      final ConcurrentSprite[] pullerSprite = new ConcurrentSprite[1];
      initializeButton(launchButton, new Runnable() {
        public void run() {
          try {
            message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
            setState(1);
            enableGetButton();
            launchAcquiringSprite();
          } catch (ExecutionException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      });
      initializeButton(getButton, new Runnable() {
        public void run() {
          getButton.setEnabled(false);
          setState(2);
          if (future != null) {
            try {
              pullerSprite[0] = createPullingSprite(sprite);
              future.get();
              if (sprite != null) {
                sprite.setReleased();
              }
              pullerSprite[0].setReleased();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
              e.printStackTrace();
            }catch (CancellationException e) {
              message1("cancel() failed. Threw CancellationException", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
            }
          }
          // select a random mutex from the list

        }
      });
      initializeButton(cancelButton, new Runnable() {
        public void run() {
          enableLaunchButton();
          setState(3);
          if (future != null) {
            boolean canceled = future.cancel(true);
            if (canceled) {
              message1("cancel() succeeded.", ConcurrentExampleConstants.MESSAGE_COLOR);
              if (sprite != null) {
                Thread.State state = sprite.getThread().getState();
                sprite.setThreadState(state);
                sprite.setRejected();
              }
              if(pullerSprite[0]!= null){
                pullerSprite[0].setReleased();
              }
            }
            else {
              message1("cancel() failed.", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
            }
          }
        }
      });
      initialized = true;
    }
  }

  @Override
  public void spriteRemoved(ConcurrentSprite sprite) {
    if (sprite.getType() != ConcurrentSprite.SpriteType.PULLER) {
//      bumpMutexVerticalIndex();
      enableLaunchButton();
    }
  }

  public String getDescriptionHtml() {
    return "";
  }

  @Override
  public void reset() {
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    setState(0);
    enableLaunchButton();
    super.reset();
  }

  private void enableLaunchButton() {
    getButton.setEnabled(false);
    cancelButton.setEnabled(false);
    launchButton.setEnabled(true);
    launchButton.requestFocus();
  }

  private void enableGetButton() {
    getButton.setEnabled(true);
    cancelButton.setEnabled(true);
    launchButton.setEnabled(false);
    getButton.requestFocus();
  }
}
