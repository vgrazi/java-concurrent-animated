package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

public class AtomicIntegerExampleOrig extends ConcurrentExample {

  private AtomicInteger atomicVariable;
  private int value = 3;
  private final JButton incrementButton = new JButton("Increment");
  private final JButton commitButton = new JButton("compareAndSet");
  //  private final JButton swapButton = new JButton("Swap");
  private boolean initialized = false;

  public String getTitle() {
    return "AtomicInteger";
  }

  protected String getSnippet() {
    String snippet;
    snippet = "<html><PRE>\n" +
       "<FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       " \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Construct the AtomicVariable with the initial value</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000080\"><B>final</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> AtomicInteger atomicVariable = </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000080\"><B>new</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> AtomicInteger(</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#0000ff\">0</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\">); \n" +
       " \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Arithmetic functions such as add, subtract, multiply,</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// divide, perform their function in an atomic fashion</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// and return the result.</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000080\"><B>final</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000080\"><B>int</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> result = atomicVariable.incrementAndGet(</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#0000ff\">3</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\">); \n" +
       " \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// compareAndSet does an atomic &quot;check and set if&quot;.</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Value is only set if the original value == assumedValue</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000080\"><B>int</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> assumedValue = </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#0000ff\">10</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\">, newValue = </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#0000ff\">5</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\">; \n" +
       "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000080\"><B>boolean</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> success = atomicVariable.compareAndSet(assumedValue, newValue); \n" +
       " \n" +
       "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n</PRE></html>";

    return snippet;
  }

  public AtomicIntegerExampleOrig(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.BLOCKING, 390, false, slideNumber);
  }

  protected ConcurrentSprite createAcquiringSprite() {
    final int index = atomicVariable.incrementAndGet();
    message1("Index after increment call:" + index, ConcurrentExampleConstants.MESSAGE_COLOR);

    ConcurrentSprite sprite = new ConcurrentSprite(index);
    sprite.setAcquiring();
    getAnimationCanvas().addSprite(sprite);
    return sprite;
  }

  protected void initializeComponents() {
    if(!initialized) {
      initializeButton(incrementButton, new Runnable() {
        public void run() {

          setAnimationCanvasVisible(true);
          ConcurrentSprite sprite = createAcquiringSprite();
          sprite.setAcquired();
          setAcquiredSprite(sprite);
        }
      });

      initializeButton(commitButton, new Runnable() {
        public void run() {
          synchronized(AtomicIntegerExampleOrig.this) {
            final boolean success = atomicVariable.compareAndSet(10, 5);
            if(success) {
              message2("atomicVariable.compareAndSet(10, 5) Commited", ConcurrentExampleConstants.MESSAGE_COLOR);
            } else {
              message2("atomicVariable.compareAndSet(10, 5) NOT Commited", ConcurrentExampleConstants.ERROR_MESSAGE_COLOR);
            }
          }
        }
      });
      initialized = true;
    }
    reset();
  }

  public String getDescriptionHtml() {
    StringBuffer sb = new StringBuffer();
//    sb.append("<html>");
//    sb.append("<table border=\"0\"><tr valign='top'><td valign='top'>");
//    sb.append("<font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_FONT_COLOR + "\">");
//    sb.append("The vgrazi.concurrent package provides many SynchronizedVar implementations, one for each primitive type.<p><p>");
//    sb.append("SynchronizedVar's can be used when contending threads need to grab a distinct value, without having to code any synchronization logic.<p><p>");
//    sb.append("In addition, SynchronizedVar's have a method:<br><font size='" + FONT_SIZE + "'color=\"" + ConcurrentExampleConstants.HTML_EM_FONT_COLOR + "\"><code>compareAndSet(int assumedValue, int newValue)</code></font><p><p>");
//
//    sb.append("</td></tr>");
//    sb.append("<tr><td>&nbsp;</td></tr>");
//    sb.append("<tr><td>&nbsp;</td></tr>");
//    sb.append("<tr><td>&nbsp;</td></tr>");
//    sb.append("<tr><td>&nbsp;</td></tr>");
//    sb.append("</table></html>");
//
    return sb.toString();

  }

  protected void reset() {
    value = 3;
    atomicVariable = new AtomicInteger(value);
    message1(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    message2(" ", ConcurrentExampleConstants.MESSAGE_COLOR);
    setState(0);
    super.reset();
  }
}
