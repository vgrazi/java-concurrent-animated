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
  private int value;

  public AtomicIntegerExample(String title, Container frame, int slideNumber) {
    super(title, frame, ExampleType.CAS, 450, true, slideNumber);
    reset();
    setState(6);
  }

  @Override
  protected String getSnippetText() {
    return  " <0 comment>// Construct the AtomicVariable, <br/>" +
            " // assigning an initial value\n" +
            " <0 keyword>final <0 default>AtomicInteger atomicInteger<br>" +
            "   = <0 keyword>new <0 default>AtomicInteger(<0 literal>1<0 default>);\n" +
            " \n" +
            " <1 comment>// compareAndSet does an atomic<br/>" +
            " //  \"check and set if\".<br/>" +
            " // Value is only set<br/>" +
            " //  if original value == assumed value\n" +
            " <1 keyword>int <1 default>assumedValue = <1 literal>10<1 default>, newValue = <1 literal>5<1 default>;\n" +
            " <1 keyword>boolean <1 default>success =\n" +
            "         atomicInteger.compareAndSet(\n" +
            "               assumedValue, newValue);\n" +
            " \n\n" +
            "<2 comment>" +
            " // Arithmetic functions on atomics\n" +
            " // perform their computations in\n" +
            " // an atomic fashion and return \n" +
            " // the result.\n" +
            " <2 keyword>int <2 default>result = atomicInteger\n" +
            "       .incrementAndGet();\n";
  }

  protected void initializeComponents() {
    reset();
    if (!initialized) {
      initializeButton(incrementAndGetButton, new Runnable() {
        public void run() {
          incrementAndGet();
        }
      });
      initializeButton(compareAndSetButton, new Runnable() {
        public void run() {
          compareAndSet();
        }
      });
      initializeThreadCountField(threadCountField);
      resetThreadCountField();
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

  public void reset() {
    super.reset();
    resetThreadCountField();
    setState(0);
    value = 1;
    atomicInteger = new AtomicInteger(value);
    CAS.setValue(atomicInteger.intValue());
    message1(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
  }

  /**
   * Resets our thread count field to the default value of 4
   */
  private void resetThreadCountField() {
    resetThreadCountField(threadCountField, 4);
  }

  @Override
  protected void setDefaultState() {
    setState(0);
  }

}