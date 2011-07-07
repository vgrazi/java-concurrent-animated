package vgrazi.concurrent.samples.examples;

import jsr166y.LinkedTransferQueue;
import jsr166y.TransferQueue;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
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
    private static final int MIN_SNIPPET_POSITION = 460;


    @Override
    protected void initializeOffer() {
        
    }

    public String getTitle() {
      return "TransferQueue";
    }

    public TransferQueueExample(String title, Container frame, int slideNumber) {
      super(title, frame, MIN_SNIPPET_POSITION, slideNumber);
    }


    protected void initializeOthers() {
        initializeTransfer();
        initializeTryTransfer();
    }

    protected void initializeGetWaitingCount() {
        initializeButton(getWaitingCountButton, new Runnable() {
          public void run() {
              displayWaitingConsumerCount();
          }
        });
    }

    protected void initializeTryTransfer() {
        initializeButton(tryTransferButton, new Runnable() {
              public void run() {
                  clearMessages();
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

        initializeButton(tryTransferTimeoutButton, new Runnable() {
              public void run() {
                  clearMessages();
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
    }

    protected void initializeTransfer() {
        initializeButton(transferButton, new Runnable() {
          public void run() {
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
      snippet = "<html><PRE>" +
              "<font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Constructor - pass in the upper bound</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>final</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> BlockingQueue queue = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> ArrayBlockingQueue<ConcurrentSprite>(4); \n" +
              " \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Threads attempting to put will block</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// until there is room in the buffer</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\">Thread putThread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> Thread() { \n" +
              "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> run() { \n" +
              "        </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>try</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> { \n" +
              "          queue.put(); \n" +
              "        } </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>catch</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">(InterruptedException e) { }\n" +
              //       "          Thread.currentThread().interrupt(); \n" +
              //       "        } \n" +
              //       "      } \n" +
              //       "    }); \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// offer is like put except that it</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// times out after the specified timeout period</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\">Thread offerThread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> Thread() { \n" +
              "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> run() { \n" +
              "        </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>try</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\"> { \n" +
              "          queue.offer(someObject, 1L, TimeUnit.SECONDS</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">); \n" +
              "        }</FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000080>\"><B>catch</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state3:#000000>\">(InterruptedException e) { }\n" +
              //       "          Thread.currentThread().interrupt(); \n" +
              //       "        } \n" +
              //       "      } \n" +
              //       "    });" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Threads attempting to poll will return </I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// null if there is nothing on the queue</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\">Thread pollThread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> Thread() { \n" +
              "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> run() { \n" +
              "        queue.poll(); \n" +
              "      } \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Threads attempting to take will block</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// until the there is something to take</I></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
              "    </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000080>\">Thread takeThread = </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000080>\"><B>new</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000000>\"> Thread() { \n" +
              "      </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000080>\"><B>public</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000000>\"> </FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000080>\"><B>void</B></FONT><font 'style=\"font-family:monospaced;\" COLOR=\"<state4:#000000>\"> run() { \n" +
              "        queue.take(); \n" +
              "      } \n" +
              //       "    }); \n" +
              "</FONT></PRE></html>";

      return snippet;
    }

    public BlockingQueue<ConcurrentSprite> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<ConcurrentSprite> queue) {
        this.queue = (TransferQueue<ConcurrentSprite>) queue;
    }
}
