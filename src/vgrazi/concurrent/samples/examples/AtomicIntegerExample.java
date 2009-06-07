package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.CAS;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * @user vgrazi.
 * Time: 12:26:11 AM
 */

/**
 * A Semaphore is created with an initial number of permits. Each acquire decrements the count of available permits.
 * Each release increments the number of available permits.
 * Note: Release calls do not need to come from the acquiring threads; anyone can call release to increase the number
 * of available permits. Also, releases can be called even before the acquires, to effectively increase the available
 * number of permits.<br>
 * Note that acquiring threads are not awarded their acquires in order of arrival, unless the Semaphore was
 * instantiated with the fair = true parameter.
 */
public class AtomicIntegerExample extends ConcurrentExample {

  private AtomicInteger atomicInteger;
  private static ExecutorService threadPool = Executors.newCachedThreadPool();


  private final JButton compareAndSetButton = new JButton("compareAndSet");
  private final JButton incrementAndGetButton = new JButton("incrementAndGet");
  private boolean initialized = false;
  private JTextField threadCountField = createThreadCountField();
  private static final int MIN_SNIPPET_POSITION = 400;
  private int value;

  public AtomicIntegerExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.CAS, MIN_SNIPPET_POSITION, true, slideNumber);
    reset();
    setState(6);
  }

  protected String getSnippet() {
    String snippet;
    snippet = "<html><PRE>\n" +
            "<FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            " \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Construct the AtomicVariable with the initial value</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>final</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> AtomicInteger atomicVariable = </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>new</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> AtomicInteger(</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#0000ff>\">" + 1 + "</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\">); \n" +
            " \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// compareAndSet does an atomic &quot;check and set if&quot;.</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Value is only set if the original value == assumedValue</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>int</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> assumedValue = </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#0000ff>\">10</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">, newValue = </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#0000ff>\">5</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">; \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>boolean</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\"> success = atomicVariable.compareAndSet(assumedValue, newValue); \n" +
            " \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// Arithmetic functions such as add, subtract, multiply,</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// divide, perform their function in an atomic fashion</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"" + ConcurrentExampleConstants.HTML_DISABLED_COLOR + "\"><I>// and return the result.</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n" +
            "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>final</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000080>\"><B>int</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\"> result = atomicVariable.incrementAndGet(</FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state2:#000000>\">); \n" +
            " \n" +
            "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n</PRE></html>";

    return snippet;
  }

  protected void initializeComponents() {
    reset();
    if (!initialized) {
      initializeButton(compareAndSetButton, new Runnable() {
        public void run() {
          compareAndSet();
        }
      });
      initializeButton(incrementAndGetButton, new Runnable() {
        public void run() {
          incrementAndGet();
        }
      });
      initializeThreadCountField(threadCountField);
      initialized = true;
    }
  }

  private void compareAndSet() {
    try {
      setState(1);
      final List<ConcurrentSprite> sprites = new ArrayList<ConcurrentSprite>();
      final int count = getThreadCount(threadCountField);
      for (int i = 0; i < count; i++) {
        ConcurrentSprite sprite = createAcquiringSprite();
        sprite.setType(ConcurrentSprite.SpriteType.CAS);
        sprite.setExpectedValue(CAS.getValue());
        sprite.setValue(++value);
        sprites.add(sprite);
      }

      // give all the animations time to arrive
      Thread.sleep((long) (1.5 * 1000));
      while (!sprites.isEmpty()) {
        int index = random.nextInt(sprites.size());
        final ConcurrentSprite sprite = sprites.remove(index);
        System.out.printf("Checking integer:%2d  expected:%2d   new:%2d%n", atomicInteger.get(), sprite.getExpectedValue(), sprite.getValue());
        boolean success = atomicInteger.compareAndSet(sprite.getExpectedValue(), sprite.getValue());
        if (success) {
          sprite.setAcquired();
          // give the winner time to animate to the inside
          Thread.sleep((long) (.7 * 1000));
          CAS.setValue(sprite.getValue());
          // we want to create the illusion that the sprite "left" its value in the monolith. So set the value to none.
          sprite.setValue(ConcurrentSprite.NO_VALUE);
          sprite.setReleased();
        } else {
          // animate the losers to rejcted state
          Runnable runnable = new Runnable() {
            public void run() {
              try {
                // give the losers time to hang out before returning
                Thread.sleep((long) (.7 * 1000));
                sprite.setRejected();
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            }
          };
          threadPool.submit(runnable);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void incrementAndGet() {
    try {
      setState(2);
      final List<ConcurrentSprite> sprites = new ArrayList<ConcurrentSprite>();
      final int count = getThreadCount(threadCountField);
      for (int i = 0; i < count; i++) {
        ConcurrentSprite sprite = createAcquiringSprite();
        sprite.setType(ConcurrentSprite.SpriteType.CAS);
        sprite.setValue(ConcurrentSprite.NO_VALUE);
        sprite.setExpectedValue(ConcurrentSprite.NO_VALUE);

        sprites.add(sprite);
      }

      // give all the animations time to arrive
      Thread.sleep((long) (1.5 * 1000));
      while (!sprites.isEmpty()) {
        int index = random.nextInt(sprites.size());
        final ConcurrentSprite sprite = sprites.remove(index);
        int value = atomicInteger.incrementAndGet();
        sprite.setExpectedValue(ConcurrentSprite.NO_VALUE);
        sprite.setAcquired();
        // give the winner time to animate to the inside
        Thread.sleep((long) (.7 * 1000));
        CAS.setValue(value);
        sprite.setReleased();
        sprite.setType(ConcurrentSprite.SpriteType.CAS);
        sprite.setValue(value);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }


  public String getDescriptionHtml() {
    StringBuffer sb = new StringBuffer();
    return sb.toString();
  }

  protected void reset() {
    super.reset();
    resetThreadCountField(threadCountField);
    setState(0);
    value = 1;
    atomicInteger = new AtomicInteger(value);
    CAS.setValue(atomicInteger.intValue());
    message1(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
  }

  @Override
  protected void setDefaultState() {
    setState(0);
  }

}