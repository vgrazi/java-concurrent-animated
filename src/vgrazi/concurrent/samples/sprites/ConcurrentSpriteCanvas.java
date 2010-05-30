package vgrazi.concurrent.samples.sprites;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import static vgrazi.concurrent.samples.ExampleType.ONE_USE;
import static vgrazi.concurrent.samples.ExampleType.PLURAL;
import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.examples.Pooled;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * User: vgrazi
 * Date: Sep 10, 2005
 * Time: 8:48:22 PM
 */
public class ConcurrentSpriteCanvas extends JPanel {
//  private final static Logger logger = Logger.getLogger(ConcurrentSpriteCanvas.class.getCanonicalName());

  private static long DELAY = 20;
  static int DELTA = 5;
  static int BACK_DELTA = 15;
  private static final int ARROW_LENGTH = 10;
  private static final int OVAL_LENGTH = 5;
  private static final int ARROW_HEAD_LENGTH = 5;
  private static final int RADIUS = 5;
  private int ARROW_DELTA = 20;
  private int rightBorder = 2;
  private int topBorder = 2;
  private int topOffset = 30;
  private int leftOffset = 0;

  private static final int ACQUIRE_BORDER_WORKING = 175;
  private static final int RELEASE_BORDER_WORKING = ACQUIRE_BORDER_WORKING + 50;

  private static final int ACQUIRE_BORDER_BLOCKING = 130;
  private static final int RELEASE_BORDER_BLOCKING = ACQUIRE_BORDER_BLOCKING + 100;

  public static int ACQUIRE_BORDER;
  public static int RELEASE_BORDER;

  private ScheduledExecutorService clock;

  private Queue<ConcurrentSprite> sprites = new ConcurrentLinkedQueue<ConcurrentSprite>();
  private Queue<ConcurrentSprite> pooledSprites = new ConcurrentLinkedQueue<ConcurrentSprite>();
  private ConcurrentExample concurrentExample;
  private String labelText;
  static ExampleType exampleType;

  private final int BORDER = 5;
  private final int deltaY = ARROW_DELTA - BORDER;
  private int NEXT_LOCATION;
  private final static int VERTICAL_ARROW_DELTA = 45;
  private final FontMetrics fontMetrics;

  /**
   * Used only by the {@link ExampleType#ONE_USE} example type, used to position the mutex vertically
   */
  private int verticalIndex;
  private final PropertyChangeSupport PROPERTY_CHANGE_SUPPORT = new PropertyChangeSupport(this);

  public ConcurrentSpriteCanvas(final ConcurrentExample concurrentExample, final String labelText) {
    setFont(ConcurrentExampleConstants.MUTEX_HEADER_FONT);
    this.concurrentExample = concurrentExample;
    setOpaque(true);

    setDoubleBuffered(true);
    setLabelText(labelText);
    fontMetrics = getFontMetrics(getFont());
    addMouseMotionListener(new MouseMotionAdapter() {
      private Rectangle labelBounds;

      @Override
      public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        final String toolTipText = concurrentExample.getToolTipText();
        if (toolTipText != null) {
          Rectangle labelBounds = getLabelBounds();
          final Point point = e.getPoint();
//          System.out.println("Mouse:" + point + " title:" + labelBounds + " text:" + toolTipText);
          if(labelBounds.contains(point)) {
            ConcurrentSpriteCanvas.this.setToolTipText(toolTipText);
          } else{
            ConcurrentSpriteCanvas.this.setToolTipText("");
          }
        }
      }

      public Rectangle getLabelBounds() {
        if(labelBounds == null) {
          int w = fontMetrics.stringWidth(labelText);
          int h = fontMetrics.getHeight();
          int x = getLabelXPosition();
          // we want the rectangle to start at the top left, not bottom, so subtract the text height displacement
          int y = getLabelYPosition() - h;
          labelBounds = new Rectangle(x, y, w, h);
        }
        return labelBounds;
      }
    });
    resumeClock();
  }

  public void togglePauseResume() {
    if(clock.isShutdown()) {
      resumeClock();
    }
    else {
      pause();
    }
  }

  public void resumeClock() {
    if (clock == null || clock.isShutdown()) {
      clock = new ScheduledThreadPoolExecutor(1);
      clock.scheduleAtFixedRate(new Runnable() {
        public void run() {
          repaint();
        }
      }, 0, DELAY, TimeUnit.MILLISECONDS);
    }
  }

  private void pause() {
    if (!clock.isShutdown()) {
      clock.shutdownNow();
    }
  }

  /**
   * Returns the approximate time in MS required to reach the "acquire" border
   *
   * @return the approximate time in MS required to reach the "acquire" border
   */
  public static int getTimeToAcquireBorder() {
    return (int) (1.5 * ACQUIRE_BORDER / (((float) DELTA) / DELAY));
  }


  public void setLabelText(String labelText) {
    this.labelText = labelText;
  }

  /**
   * Sets the type of Mutex to render
   *
   * @param type the type of mutex
   */
  public void setExampleType(ExampleType type) {
    exampleType = type;
    switch (exampleType) {
      case CAS:
        DELTA = 3;
        BACK_DELTA = 0;
        topBorder = 32;
        int borderDelta = 80;
        ACQUIRE_BORDER = ACQUIRE_BORDER_BLOCKING + borderDelta;
        RELEASE_BORDER = RELEASE_BORDER_BLOCKING + borderDelta + 10;
        break;
      case BLOCKING:
      case POOLED:
      case PLURAL:
      case ONE_USE:
        DELTA = 5;
        BACK_DELTA = 15;
        topBorder = 2;
        ACQUIRE_BORDER = ACQUIRE_BORDER_BLOCKING;
        RELEASE_BORDER = RELEASE_BORDER_BLOCKING;
        break;
      case WORKING:
        DELTA = 5;
        BACK_DELTA = 15;
        topBorder = 2;
        ACQUIRE_BORDER = ACQUIRE_BORDER_WORKING;
        RELEASE_BORDER = RELEASE_BORDER_WORKING;
        break;
    }

  }

  public void addSprite(ConcurrentSprite sprite) {
    sprites.add(sprite);
  }

  public int getSpriteCount() {
    return sprites.size();
  }

  public void paint(Graphics g1) {
    super.paint(g1);

    Graphics2D g = (Graphics2D) g1;
    g.setColor(ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    g.setStroke(new BasicStroke(2));
    g.fill3DRect(0, 0, 1500, 2000, true);
    //    Set sprites = new HashSet(this.sprites);
    final Dimension size = getSize();
    for (ConcurrentSprite sprite : sprites) {
      if (sprite.getCurrentLocation() > size.width - rightBorder) {
        sprites.remove(sprite);
        concurrentExample.spriteRemoved(sprite);
      } else if (sprite.isRejected() && sprite.getCurrentLocation() < 0) {
        sprites.remove(sprite);
      }
    }

    // draw the mutex box
    drawMutex(g, size);

    // Draw the label text
    g.setColor(ConcurrentExampleConstants.MUTEX_FONT_COLOR);
    g.drawString(labelText, getLabelXPosition(), getLabelYPosition());

    //    System.out.println("ConcurrentSpriteCanvas.paint sprite count:" + sprites.size());
    try {
      for (ConcurrentSprite sprite : sprites) {
        int index = sprite.getIndex();
        ConcurrentSprite.SpriteState state = sprite.getState();
        int xPos = sprite.getCurrentLocation() + leftOffset;
        int yPos = index * ARROW_DELTA + topBorder + topOffset;
        g.setColor(sprite.getColor());
        switch (state) {
          case ACQUIRING:
          case ACQUIRED:
          case ACTION_COMPLETED:
            drawAcquiring(g, xPos, yPos, sprite);
            break;
          case REJECTED:
            drawRejected(g, xPos, yPos, sprite);
            break;
          case RELEASED:
            if (sprite.getCurrentLocation() < ACQUIRE_BORDER) {
              drawAcquiring(g, xPos, yPos, sprite);
            } else {
              drawReleased(g, xPos, yPos, sprite);
            }
            break;
          case PULLING:
            drawPulling(g, xPos, yPos, sprite);
            break;
          default:
        }
      }
      if (exampleType == ExampleType.POOLED) {
        drawPool(g);
      }
    } catch (ConcurrentModificationException e) {
      System.out.println("ConcurrentSpriteCanvas.paint " + e);
    }
  }

  /**
   * Returns the x position to start drawing the label
   * @return the x position to start drawing the label
   */
  private int getLabelXPosition() {
    return (ACQUIRE_BORDER + RELEASE_BORDER) / 2 + leftOffset - (fontMetrics.stringWidth(labelText)) / 2;
  }

  /**
   * Returns the y position to start drawing the label
   * @return the y position to start drawing the label
   */
  private int getLabelYPosition() {
    return topOffset - 10;
  }

  private void drawMutex(Graphics2D g, Dimension size) {
    g.setColor(ConcurrentExampleConstants.MUTEX_BACKGROUND);
    switch(exampleType) {
      case CAS:
        g.fill3DRect(ACQUIRE_BORDER + leftOffset, topOffset, RELEASE_BORDER - ACQUIRE_BORDER + leftOffset, size.height - 20 - topOffset, true);
        // todo: calculate dymanically
        FontMetrics fm = g.getFontMetrics();
        int fontWidth = fm.stringWidth(String.valueOf(CAS.getValue()));
        int fontHeight = fm.getHeight();

        g.setColor(ConcurrentExampleConstants.CAS_CIRCLE_COLOR);

        int xPos = ACQUIRE_BORDER + leftOffset + (RELEASE_BORDER - ACQUIRE_BORDER + leftOffset - fontWidth) / 2;
        int yPos = topOffset + fontHeight - 10;
        g.drawString(String.valueOf(CAS.getValue()), xPos, yPos);
        break;
      case BLOCKING:
      case POOLED:
        g.fill3DRect(ACQUIRE_BORDER + leftOffset, topOffset, RELEASE_BORDER - ACQUIRE_BORDER + leftOffset, size.height - 20 - topOffset, true);
        break;
      case WORKING:
        g.fill3DRect(ACQUIRE_BORDER + leftOffset, topOffset, RELEASE_BORDER - ACQUIRE_BORDER + leftOffset + 10, size.height - 20 - topOffset, true);
        break;
      case PLURAL:
      case ONE_USE:
        int nextLocation = topOffset + ARROW_DELTA * 3 / 4;
        int lastLocation = size.height - 20 - topOffset;
        if (exampleType == PLURAL) {
          while (nextLocation <= lastLocation) {
            g.fill3DRect(ACQUIRE_BORDER + leftOffset, nextLocation, RELEASE_BORDER - ACQUIRE_BORDER + leftOffset, deltaY, true);
            nextLocation += deltaY + BORDER;
          }
        }
        else if(exampleType == ONE_USE) {
          if(NEXT_LOCATION == 0) {
            NEXT_LOCATION = topOffset + ARROW_DELTA * 3 / 4 + (deltaY + BORDER) * verticalIndex;
          }
          g.fill3DRect(ACQUIRE_BORDER + leftOffset, NEXT_LOCATION, RELEASE_BORDER - ACQUIRE_BORDER + leftOffset, deltaY, true);
        }
        break;
    }
  }

  public void drawPool(Graphics g) {
    int availableThreadCount = ((Pooled) concurrentExample).getAvailableThreadCount();
    int poolSize = pooledSprites.size();
    if(availableThreadCount < poolSize) {
      for(int i = 0; i < poolSize - availableThreadCount; i++) {
        pooledSprites.remove();
      }
    }
    else {
      for(int i = 0; i < availableThreadCount - poolSize; i++) {
        ConcurrentSprite sprite = new ConcurrentSprite(pooledSprites.size());
        sprite.setAcquired();
        sprite.moveToLocation(RELEASE_BORDER_WORKING);
        pooledSprites.add(sprite);
      }
    }
    int yPos = getSize().height - 20 - topOffset - 7 * ARROW_DELTA;
    g.setColor(ConcurrentExampleConstants.MUTEX_FONT_COLOR);
    g.drawString("Pooled",  ACQUIRE_BORDER + leftOffset + 12, yPos - 45);
    g.drawString("Threads", ACQUIRE_BORDER + leftOffset + 8, yPos - 20);
    g.drawLine(ACQUIRE_BORDER + leftOffset + 8, yPos - 17, ACQUIRE_BORDER + leftOffset + RELEASE_BORDER - ACQUIRE_BORDER + leftOffset - 8, yPos - 17);
//    int yPos = topOffset + ARROW_DELTA * 3 / 4 + (deltaY + BORDER) * verticalIndex + (getSize().height - 20 - topOffset) / 2 + 10;
    for (ConcurrentSprite sprite : pooledSprites) {
      int xPos = sprite.getCurrentLocation();
      sprite.bumpCurrentLocation(DELTA);
      drawArrowSprite(g, xPos, yPos, sprite);
      yPos += deltaY;
    }
  }

  public void bumpVerticalMutexIndex() {
    verticalIndex += 1;
    NEXT_LOCATION += deltaY + BORDER;
    if(NEXT_LOCATION >= getSize().height - 100) {
      resetMutexVerticalIndex();
    }
  }

  public void resetMutexVerticalIndex() {
    verticalIndex = 0;
    NEXT_LOCATION = 0;
    if(exampleType == ExampleType.CAS) {
      bumpVerticalMutexIndex();
    }
  }

  private void drawReleased(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    int y;
    switch (sprite.getType()) {
      case RUNNABLE:
        g.fill3DRect(xPos - 57 + 30, yPos-4 + (deltaY + BORDER) * verticalIndex, ARROW_LENGTH * 6 - 30, 8, true);
        break;
      case ARROW:
      case CAS:
        if (sprite.getType() == ConcurrentSprite.SpriteType.CAS) {
          y = yPos;
          int y1 = yPos - ARROW_LENGTH;
          int y2 = yPos + ARROW_LENGTH;
          // draw the top arrow head
          g.drawLine(xPos, y, xPos - ARROW_HEAD_LENGTH * 4, y1);
          // draw the bottom arrow head
          g.drawLine(xPos, y, xPos - ARROW_HEAD_LENGTH * 4, y2);
          int length = ARROW_LENGTH * 3;
          g.drawLine(xPos, y, xPos - length * 6, y);

          y = yPos - RADIUS + 5;
          g.setColor(ConcurrentExampleConstants.CAS_ANIMATION_COLOR);
        } else {
          y = yPos - RADIUS;
          if (exampleType == ExampleType.ONE_USE) {
            y += NEXT_LOCATION - VERTICAL_ARROW_DELTA;
          }
          g.fillOval(xPos - RADIUS, y, RADIUS * 2, RADIUS * 2);
          y = yPos;
          if (exampleType == ExampleType.ONE_USE) {
            y += NEXT_LOCATION - VERTICAL_ARROW_DELTA;
          }
          g.drawLine(xPos, y, xPos - ARROW_LENGTH * 6, y);
        }
        int expectedValue = sprite.getExpectedValue();
        int value = sprite.getValue();
        if (expectedValue != ConcurrentSprite.NO_VALUE) {
          g.setColor(ConcurrentExampleConstants.CAS_ANIMATION_COLOR);
          g.drawString("(" + expectedValue + ")", xPos - ARROW_LENGTH * 11, y);
        }
        if (value != ConcurrentSprite.NO_VALUE) {
          g.setColor(ConcurrentExampleConstants.CAS_ANIMATION_COLOR);
          g.drawString(String.valueOf(value), xPos - ARROW_LENGTH * 11, y);
        }
        break;
      case PULLER: {
        drawPulling(g, xPos, yPos, sprite);
      }
      break;
      case OVAL: {
        y = yPos;
        if (exampleType == ExampleType.ONE_USE) {
          y += NEXT_LOCATION - VERTICAL_ARROW_DELTA;
        }
        g.fillOval(xPos - 85, y, OVAL_LENGTH * 18, OVAL_LENGTH * 2);
      }
      break;
      case TEXT:
        final ConcurrentTextSprite textSprite = (ConcurrentTextSprite) sprite;
        final String text = textSprite.getText();
        final int stringWidth = fontMetrics.stringWidth(text);
        g.drawString(text, xPos - stringWidth, yPos);
        break;
    }
    sprite.bumpCurrentLocation(DELTA);
  }

  /**
   * A pulling sprite is a "getter" thread that pulls the result from a Future
   * It is drawn to the right of the mutex and waits for the associated sprite to be released
   * @param index
   * @param type
   * @return
   */
  private void drawPulling(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    int y = yPos - RADIUS;
    if (exampleType == ExampleType.ONE_USE) {
      y += NEXT_LOCATION - VERTICAL_ARROW_DELTA;
    }
    g.fillOval(xPos - RADIUS, y, RADIUS * 2, RADIUS * 2);
    y = yPos;
    if (exampleType == ExampleType.ONE_USE) {
      y += NEXT_LOCATION - VERTICAL_ARROW_DELTA;
    }
    g.drawLine(xPos, y, xPos - ARROW_LENGTH * 6 , y);
    if (!sprite.isReleased()) {
      sprite.bumpCurrentLocation(DELTA);
    }
  }


  private void drawRejected(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    int y = yPos;
    if (exampleType == ExampleType.ONE_USE) {
      y += (NEXT_LOCATION - VERTICAL_ARROW_DELTA);
    }
    switch (sprite.getType()) {
      case ARROW:
      case RUNNABLE:
      case CAS:
        int width = RADIUS * 2;
        if (exampleType == ExampleType.ONE_USE) {
          width += NEXT_LOCATION - VERTICAL_ARROW_DELTA;
        }
        if(sprite.getType() == ConcurrentSprite.SpriteType.CAS) {
          g.fillOval(xPos - RADIUS - ARROW_LENGTH * 18 - RADIUS - 1, yPos - RADIUS, width, RADIUS * 2);
          g.drawLine(xPos, y, xPos - ARROW_LENGTH * 18, y);
          g.setColor(ConcurrentExampleConstants.CAS_ANIMATION_COLOR);
          int value = sprite.getValue();
          if (value != ConcurrentSprite.NO_VALUE) {
            g.drawString(String.valueOf(value), xPos - ARROW_LENGTH * 18, y);
          }
        }
        else {
          g.fillOval(xPos - RADIUS - ARROW_LENGTH * 6, yPos - RADIUS, width, RADIUS * 2);
          g.drawLine(xPos, y, xPos - ARROW_LENGTH * 6, y);
        }
        g.drawLine(xPos, y, xPos - ARROW_LENGTH * 6, y);
        break;
      case OVAL:
        g.fillOval(xPos - 85, y, OVAL_LENGTH * 18, OVAL_LENGTH * 2);
        break;
    }
    sprite.kickCurrentLocation(DELTA);
  }

  /**
   * Draws the animation of the sprite into the mutex and stops once inside.
   * If the state of the sprite is ACQUIRING, animates into the mutex
   * If the state of the sprite is ACQUIRED, stops inside the mutex
   * @param g the graphics to paint
   * @param xPos the x pixel position
   * @param yPos the y pixel position
   * @param sprite the sprite to animate
   */
  private void drawAcquiring(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    switch (sprite.getType()) {
      case RUNNABLE:
//        System.out.printf("State:%s location:%d  destination:%d%n", sprite.getState(), sprite.getCurrentLocation(), sprite.getDestination());
        // if right side of rectangle is to the left of mutex, butt up to mutex.
        // otherwise, center it
        if (sprite.isAcquired()) {
          // center the rectangle over the arrow
          g.fill3DRect(xPos - 52, yPos-4 + (deltaY + BORDER) * verticalIndex, ARROW_LENGTH * 6 - 35, 8, true);
          drawArrowSprite(g, xPos, yPos, sprite);
        }
        else {
          // butt the rectangle up to the mutex
          g.fill3DRect(xPos - 57 + 30, yPos-4 + (deltaY + BORDER) * verticalIndex, ARROW_LENGTH * 6 - 34, 8, true);
        }
        break;
      case ARROW:
      case CAS:
        drawArrowSprite(g, xPos, yPos, sprite);
        break;
      case OVAL:
        g.fillOval(xPos - 85, yPos, OVAL_LENGTH * 18, OVAL_LENGTH * 2 + 5);
        break;
      case TEXT:
        final ConcurrentTextSprite textSprite = (ConcurrentTextSprite) sprite;
        final String text = textSprite.getText();
        final int stringWidth = fontMetrics.stringWidth(text);
        g.drawString(text, xPos - stringWidth, yPos);
        break;
    }
    if (sprite.isActionCompleted()) {
      sprite.bumpLocationToDestination();
    }
    sprite.bumpCurrentLocation(DELTA);
    if(sprite.getCurrentLocation() >= sprite.getDestination()) {
      notifyListeners(ConcurrentAnimationEvent.ARRIVED, sprite);
    }
  }

  private void drawArrowSprite(Graphics g, int xPos, int yPos, ConcurrentSprite sprite) {
    int y = yPos;
    int y1 = yPos - ARROW_HEAD_LENGTH;
    int y2 = yPos + ARROW_HEAD_LENGTH;
    if (exampleType == ExampleType.ONE_USE) {
      y += NEXT_LOCATION - VERTICAL_ARROW_DELTA;
      y1 += (NEXT_LOCATION - VERTICAL_ARROW_DELTA);
      y2 += (NEXT_LOCATION - VERTICAL_ARROW_DELTA);
    }
    g.setColor(sprite.getColor());
    // draw the top arrow head
    g.drawLine(xPos, y, xPos - ARROW_HEAD_LENGTH * 4, y1);
    // draw the bottom arrow head
    g.drawLine(xPos, y, xPos - ARROW_HEAD_LENGTH * 4, y2);
    int length;
    if (sprite.getType() == ConcurrentSprite.SpriteType.CAS) {
      length = ARROW_LENGTH * 3;
    }
    else {
      length = ARROW_LENGTH;
    }
    g.drawLine(xPos, y, xPos - length * 6, y);
    if(sprite.getType() == ConcurrentSprite.SpriteType.CAS) {
      // draw the attempted replacement
      g.setColor(ConcurrentExampleConstants.CAS_ANIMATION_COLOR);
      int spriteValue = sprite.getValue();
      if (spriteValue != ConcurrentSprite.NO_VALUE) {
        String value = String.valueOf(spriteValue);
        g.drawString(value, xPos - 85, y);
      }
    }
    int expectedValue = sprite.getExpectedValue();
    if (expectedValue != ConcurrentSprite.NO_VALUE) {
      g.drawString("(" + expectedValue + ")", xPos - 53, y);
    }
  }


  public void addListener(ConcurrentAnimationEvent event, PropertyChangeListener listener) {
    PROPERTY_CHANGE_SUPPORT.addPropertyChangeListener(event.toString(), listener);
  }
  public void removeListener(ConcurrentAnimationEvent event, PropertyChangeListener listener) {
    PROPERTY_CHANGE_SUPPORT.removePropertyChangeListener(event.toString(), listener);
  }
  private void notifyListeners(ConcurrentAnimationEvent event, ConcurrentSprite sprite) {
    PROPERTY_CHANGE_SUPPORT.firePropertyChange(event.toString(), sprite, null);
  }

  public void clearSprites() {
    sprites.clear();
    pooledSprites.clear();
  }

  //  private static void parameterTestFrame() {
  //    JFrame frame = new JFrame("Set Canvas Parameters");
  //
  //    frame.getContentPane().setLayout(new FlowLayout());
  //    final JTextField deltaField = new JTextField(String.valueOf(DELTA), 6);
  //    deltaField.addFocusListener(new FocusAdapter() {
  //      public void focusGained(FocusEvent e) {
  //        deltaField.select(0, 100);
  //      }
  //    });
  //    final JTextField backDeltaField = new JTextField(String.valueOf(BACK_DELTA), 6);
  //    backDeltaField.addFocusListener(new FocusAdapter() {
  //      public void focusGained(FocusEvent e) {
  //        backDeltaField.select(0, 100);
  //      }
  //    });
  //    final JTextField delayField = new JTextField(String.valueOf(DELAY));
  //    backDeltaField.addFocusListener(new FocusAdapter() {
  //      public void focusGained(FocusEvent e) {
  //        delayField.select(0, 100);
  //      }
  //    });
  //
  //    deltaField.addActionListener(new ActionListener() {
  //      public void actionPerformed(ActionEvent e) {
  //        int value = Integer.parseInt(deltaField.getText());
  //        if(value > 0) {
  //          DELTA = value;
  //        }
  //      }
  //    });
  //    backDeltaField.addActionListener(new ActionListener() {
  //      public void actionPerformed(ActionEvent e) {
  //        int value = Integer.parseInt(backDeltaField.getText());
  //        if(value > 0) {
  //          BACK_DELTA = value;
  //        }
  //      }
  //    });
  //    delayField.addActionListener(new ActionListener() {
  //      public void actionPerformed(ActionEvent e) {
  //        int value = Integer.parseInt(delayField.getText());
  //        if(value > 0) {
  //          DELAY = value;
  //        }
  //      }
  //    });
  //    frame.getContentPane().add(new JLabel("DELTA"));
  //    frame.getContentPane().add(deltaField);
  //
  //    frame.getContentPane().add(new JLabel("BACK_DELTA"));
  //    frame.getContentPane().add(backDeltaField);
  //
  //    frame.getContentPane().add(new JLabel("DELAY"));
  //    frame.getContentPane().add(delayField);
  //
  //    frame.pack();
  //    UIUtils.center(frame);
  //
  //    frame.setLocation(frame.getLocation().x, frame.getLocation().y - 200);
  //    frame.show();
  //  }

  public void shuffleSprites() {
    List<ConcurrentSprite> sprites = new ArrayList<ConcurrentSprite>(this.sprites);
    Collections.shuffle(sprites);
    this.sprites = new ConcurrentLinkedQueue<ConcurrentSprite>(sprites);
  }
}