package vgrazi.concurrent.samples.examples;

import jsr166y.LinkedTransferQueue;
import jsr166y.TransferQueue;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.canvases.OvalObjectCanvas;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TransferQueueExample extends BlockingQueueExample {
  private TransferQueue<ConcurrentSprite> queue;
  private Executor executor = Executors.newCachedThreadPool();
  private final JButton transferButton = new JButton("transfer()");
  private final JButton tryTransferButton = new JButton("tryTansfer()");
  private final JButton tryTransferTimeoutButton = new JButton("tryTansfer(T, 5, TimeUnit.SECONDS)");
  private final JButton getWaitingCountButton = new JButton("getWaitingConsumerCount");


  @Override
  protected void initializeOffer() {

  }

  public String getTitle() {
    return "TransferQueue";
  }

  public TransferQueueExample(String title, Container frame, int slideNumber) {
    super(title, frame, 550, slideNumber);
  }

  protected void createCanvas() {
    setCanvas(new OvalObjectCanvas(this, getTitle()));
  }


  @Override
  protected void initializeComponents() {
    reset();
    if (!initialized) {

      initializeButton(transferButton, new Runnable() {
        public void run() {
          setState(5);
          clearMessages();
          int count = getThreadCount();
          for (int i = 0; i < count; i++) {
            final ConcurrentSprite sprite = createAcquiringSprite(ConcurrentSprite.SpriteType.OVAL);
            executor.execute(new Runnable() {
              public void run() {
                try {
                  ((TransferQueue<ConcurrentSprite>) getQueue()).transfer(sprite);
                } catch (InterruptedException e) {
                  message1("InterruptedException", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
                  Thread.currentThread().interrupt();
                }
              }
            });
          }
          delayAfterClick();
        }
      });
      initializeButton(tryTransferButton, new Runnable() {
        public void run() {
          clearMessages();
          setState(6);
          int count = getThreadCount();
          for (int i = 0; i < count; i++) {
            final ConcurrentSprite sprite = createAcquiringSprite(ConcurrentSprite.SpriteType.OVAL);
            executor.execute(new Runnable() {
              public void run() {
                boolean success = false;
                try {
                  // note: a true un-timed try will fail immediately. So spoof it with a small timeout
                  success = ((TransferQueue<ConcurrentSprite>) getQueue()).tryTransfer(sprite, 500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                  message1("InterruptedException", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
                  Thread.currentThread().interrupt();
                }
                if (!success) {
                  sprite.setRejected();
                }
              }
            });
          }
          delayAfterClick();
        }
      });
      initializeButton(takeButton, new Runnable() {
        public void run() {
          clearMessages();
          int count = getThreadCount(threadCountField);
          delayAfterClick();
          for (int i = 0; i < count; i++) {
            executor.execute(new Runnable() {
              public void run() {
                take();
              }
            });
          }
        }
      });
      addButtonSpacer();

      initializeButton(tryTransferTimeoutButton, new Runnable() {
        public void run() {
          clearMessages();
          setState(7);
          int count = getThreadCount();
          for (int i = 0; i < count; i++) {
            final ConcurrentSprite sprite = createAcquiringSprite(ConcurrentSprite.SpriteType.OVAL);
            executor.execute(new Runnable() {
              public void run() {
                boolean success = false;
                try {

                  success = ((TransferQueue<ConcurrentSprite>) getQueue()).tryTransfer(sprite, 5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                  message1("InterruptedException", ConcurrentExampleConstants.WARNING_MESSAGE_COLOR);
                  Thread.currentThread().interrupt();
                }
                if (!success) {
                  sprite.setRejected();
                }
              }
            });
          }
          delayAfterClick();
        }
      });
      addButtonSpacer();

      initializeButton(pollButton, new Runnable() {
        public void run() {
          clearMessages();
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            poll();
          }
          delayAfterClick();
        }
      });
      initializeButton(putButton, new Runnable() {
        public void run() {
          clearMessages();

          setAnimationCanvasVisible(true);
          int count = getThreadCount(threadCountField);
          for (int i = 0; i < count; i++) {
            put();
          }
          delayAfterClick();
        }
      });
      Dimension size = new Dimension(100, transferButton.getPreferredSize().height);
      transferButton.setPreferredSize(size);
//      takeButton.setPreferredSize(size);
      tryTransferButton.setPreferredSize(size);
      tryTransferTimeoutButton.setPreferredSize(new Dimension(285, transferButton.getPreferredSize().height));
      putButton.setPreferredSize(size);
      pollButton.setPreferredSize(size);

      initializeThreadCountField(threadCountField);
      initialized = true;
    }
  }

  protected void afterClick() {
    displayWaitingConsumerCount();
  }

  private void displayWaitingConsumerCount() {
    int waitingConsumerCount = ((TransferQueue<ConcurrentSprite>) getQueue()).getWaitingConsumerCount();
    message2("Waiting consumer count:" + waitingConsumerCount, ConcurrentExampleConstants.MESSAGE_COLOR);
  }

  public String getDescriptionHtml() {
    StringBuffer sb = new StringBuffer();
    //    sb.append("<html>");
    //    sb.append("<table border=\"0\"><tr><td>");
    //    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_FONT_COLOR + "\">");
    //
    //    sb.append("A <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>CountDown</code></font> is instantiated with a specified count.<p><p>");
    //    sb.append("One or more threads call <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>CountDown.acquire()</code></font>, which blocks until the count is achieved.<p><p>");
    //    sb.append("Subsequent threads call <font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>CountDown.release()</code></font>.");
    //    sb.append(" Once the specified count is released, ");
    //    sb.append("then any acquired threads unblock and proceed.");
    //
    //    sb.append("</td></tr></table>");
    //    sb.append("</html>");

    return sb.toString();
  }

  protected BlockingQueue<ConcurrentSprite> createQueue() {
    return new LinkedTransferQueue<ConcurrentSprite>();
  }

  protected String getSnippet() {

    String snippet;
    snippet = "<html><head><style type=\"text/css\"> \n" +
      ".ln { color: rgb(0,0,0); font-weight: normal; font-style: normal; }\n" +
      ".s0 { }\n" +
      ".s1 { color: rgb(0,0,128); font-weight: bold; }\n" +
      ".s2 { color: rgb(0,0,255); }\n" +
      ".s9 { color: rgb(128,128,128); }\n" +
      "</style> \n" +
      "</head>\n" +
      "<BODY BGCOLOR=\"#ffffff\">\n" +
      "<pre>\n" +
      "<span class=\"<state0:s0>\"> \n" +
      " TransferQueue&lt;T&gt; transferQueue\n" +
      "      </span><span class=\"<state0:s1>\"> = new </span><span class=\"<state0:s0>\">LinkedTransferQueue&lt;T&gt;(); \n" +
      " </span><span class=\"<state1:s1>\">try </span><span class=\"<state1:s0>\">{ \n" +
      "     transferQueue.put(t); \n" +
      " } </span><span class=\"<state1:s1>\">catch </span><span class=\"<state1:s0>\">(InterruptedException e) { } \n" +
      "  \n" +
      " </span><span class=\"<state4:s1>\">try </span><span class=\"<state4:s0>\">{ \n" +
      "     T t = transferQueue.take(); \n" +
      " } </span><span class=\"<state4:s1>\">catch </span><span class=\"<state4:s0>\">(InterruptedException e) { } </span>\n" +
      " </span><span class=\"<state2:s0>\"> \n" +
      " T t = transferQueue.poll(); \n" +
      " </span>" +
      "  \n" +
      " <span class=\"<state5:s0>\">" +
      " T t = transferQueue.transfer(); \n" +
      " </span>\n" +
      " <span class=\"<state6:s1>\">boolean </span><span class=\"<state6:s0>\">success = transferQueue.tryTransfer(t); \n" +
      "  \n" +
      " </span><span class=\"<state7:s1>\">boolean </span><span class=\"<state7:s0>\">success =transferQueue.tryTransfer(t, \n" +
      "</span><span class=\"<state7:s2>\">5</span><span class=\"<state7:s0>\">,\n" +
      "                     TimeUnit.SECONDS);</span></pre>\n" +
      "</body>\n" +
      "</html>";
    return snippet;
  }

  public BlockingQueue<ConcurrentSprite> getQueue() {
    return queue;
  }

  public void setQueue(BlockingQueue<ConcurrentSprite> queue) {
    this.queue = (TransferQueue<ConcurrentSprite>) queue;
  }
}
