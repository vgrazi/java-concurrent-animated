package vgrazi.concurrent.samples.canvases;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import static vgrazi.concurrent.samples.ExampleType.ONE_USE;
import static vgrazi.concurrent.samples.ExampleType.PLURAL;
import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.examples.Pooled;
import vgrazi.concurrent.samples.sprites.*;

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
  public static int BACK_DELTA = 15;
  public static final int ARROW_LENGTH = 10;
  static final int OVAL_LENGTH = 5;
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
  private final Queue<ConcurrentSprite> pooledSprites = new ConcurrentLinkedQueue<ConcurrentSprite>();
  private ConcurrentExample concurrentExample;
  private String labelText;


  public static ExampleType exampleType;

  final int BORDER = 5;
  protected final int deltaY = ARROW_DELTA - BORDER;
  private final int oneUseDeltaY = ARROW_DELTA - BORDER + 12;
  private int NEXT_LOCATION;
  private final static int VERTICAL_ARROW_DELTA = 45;
  private final FontMetrics fontMetrics;

  /**
   * Used only by the {@link ExampleType#ONE_USE} example type, used to position the mutex vertically
   */
  int verticalIndex;
  private final PropertyChangeSupport PROPERTY_CHANGE_SUPPORT = new PropertyChangeSupport(this);
  private final BasicStroke basicStroke = new BasicStroke(3);
  private static final Object animationThreadMutex = new Object();
  private boolean paused;

  public ConcurrentSpriteCanvas(final ConcurrentExample concurrentExample, final String labelText) {
    setFont(ConcurrentExampleConstants.MUTEX_HEADER_FONT);
    this.concurrentExample = concurrentExample;
    setOpaque(true);

    setDoubleBuffered(true);
      if (labelText != null) {
          setLabelText(labelText);
      }
      fontMetrics = getFontMetrics(getFont());
    addMouseMotionListener(new MouseMotionAdapter() {
      private Rectangle labelBounds;

      @Override
      public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        final String toolTipText = concurrentExample.getToolTipText();
        if (toolTipText != null) {
            if (labelText != null) {
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
      pauseClock();
    }
  }

  public void resumeClock() {
    if (clock == null || clock.isShutdown()) {
      clock = new ScheduledThreadPoolExecutor(1);
      clock.scheduleAtFixedRate(new Runnable() {
        public void run() {
          synchronized (animationThreadMutex) {
            try {
              repaint();
              while(!isAnimating()) {
                System.out.println("Animation thread waiting");
                animationThreadMutex.wait();
                System.out.println("Animation thread resuming");
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
        }
      }, 0, DELAY, TimeUnit.MILLISECONDS);
    }
  }

  private boolean isAnimating() {
    return !isPaused() && (!getSprites().isEmpty() || !getPooledSprites().isEmpty() || (concurrentExample instanceof Pooled && ((Pooled) concurrentExample).getAvailableThreadCount() > 0));
  }

  public void pause() {
    synchronized (animationThreadMutex) {
      paused = true;
    }
  }
  public void resume() {
    synchronized (animationThreadMutex) {
      paused = false;
      notifyAnimationThread();
    }
  }

  public boolean isPaused() {
    return paused;
  }

  protected Queue<ConcurrentSprite> getSprites() {
    return sprites;
  }

  private Queue<ConcurrentSprite> getPooledSprites() {
    return pooledSprites;
  }

  private void pauseClock() {
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
      case CONCURRENT_MAP:
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
    notifyAnimationThread();
  }

  public void notifyAnimationThread() {
    synchronized (animationThreadMutex) {
      animationThreadMutex.notifyAll();
    }
  }

  public int getSpriteCount() {
    return sprites.size();
  }

  public void paintComponent(Graphics g1) {
    super.paintComponent(g1);

    Graphics2D g = (Graphics2D) g1;
    Map<RenderingHints.Key, Object> map = new HashMap<RenderingHints.Key, Object>(1);
    map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.addRenderingHints(map);
    g.setColor(ConcurrentExampleConstants.DEFAULT_BACKGROUND);
    g.setStroke(basicStroke);
    g.fillRect(0, 0, 1500, 2000);
    //    Set sprites = new HashSet(this.sprites);
    final Dimension size = getSize();

    // first remove any spent sprites
    for (ConcurrentSprite sprite : sprites) {
      if (sprite.getCurrentLocation() > size.width - rightBorder) {
        sprites.remove(sprite);
        concurrentExample.spriteRemoved(sprite);
      } else if (sprite.isRejected() && sprite.getCurrentLocation() < 0) {
        sprites.remove(sprite);
      }
    }

    // render the mutex
    if(labelText != null) {
// draw the mutex box
      drawMutex(g, size);

      // Draw the label text
      g.setColor(ConcurrentExampleConstants.MUTEX_FONT_COLOR);
      g.setFont(ConcurrentExampleConstants.MUTEX_HEADER_FONT);
      g.drawString(labelText, getLabelXPosition(), getLabelYPosition());
    }

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

  protected void drawMutex(Graphics2D g, Dimension size) {
    g.setColor(ConcurrentExampleConstants.MUTEX_BACKGROUND);
    int fontHeight = fontMetrics.getHeight();
    switch(exampleType) {
		case CONCURRENT_MAP: {
            // render the existing map
			g.fill3DRect(ACQUIRE_BORDER + leftOffset, topOffset, RELEASE_BORDER - ACQUIRE_BORDER + leftOffset, size.height - 20 - topOffset, true);

			g.setColor(ConcurrentExampleConstants.CAS_CIRCLE_COLOR);

			int xPos = ACQUIRE_BORDER + leftOffset;
			int yPos = topOffset + fontHeight;
			Set<Integer> keySet = GlobalConcurrentMap.keySet();
      TreeSet<Integer> set = new TreeSet<Integer>(keySet);
			for (Integer key : set) {
				g.drawString("" + key + GlobalConcurrentMap.get(key), xPos + 20, yPos);
				yPos += fontHeight;
			}
			break;
		}
      case CAS:
        g.fill3DRect(ACQUIRE_BORDER + leftOffset, topOffset, RELEASE_BORDER - ACQUIRE_BORDER + leftOffset, size.height - 20 - topOffset, true);
        // todo: calculate dynamically
        int fontWidth = fontMetrics.stringWidth(String.valueOf(CAS.getValue()));

        g.setColor(ConcurrentExampleConstants.CAS_CIRCLE_COLOR);

        int xPos = ACQUIRE_BORDER + leftOffset + (RELEASE_BORDER - ACQUIRE_BORDER + leftOffset - fontWidth) / 2;
        int yPos = topOffset + fontHeight;
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
          g.fill3DRect(ACQUIRE_BORDER + leftOffset, NEXT_LOCATION, RELEASE_BORDER - ACQUIRE_BORDER + leftOffset, oneUseDeltaY, true);
        }
        break;
    }
  }

  protected void drawPool(Graphics g) {
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
        sprite.setType(ConcurrentSprite.SpriteType.ARROW);
        sprite.setAcquired();
        sprite.moveToLocation(RELEASE_BORDER_WORKING);
        pooledSprites.add(sprite);
      }
    }
    notifyAnimationThread();
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
    if(exampleType == ExampleType.CAS || exampleType == ExampleType.CONCURRENT_MAP) {
      bumpVerticalMutexIndex();
    }
  }

  protected void drawReleased(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    int y;
    switch (sprite.getType()) {
      case RUNNABLE:
        g.fill3DRect(xPos - 57 + 30, yPos-4 + (deltaY + BORDER) * verticalIndex, ARROW_LENGTH * 6 - 30, 8, true);
        break;
      case ARROW:
      case WORKING:
      case CAS:
      case PUT_IF_ABSENT:
        if (sprite.getType() == ConcurrentSprite.SpriteType.CAS || sprite.getType() == ConcurrentSprite.SpriteType.PUT_IF_ABSENT) {
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
        String expectedStringValue = sprite.getExpectedStringValue();
        if (sprite.getType() == ConcurrentSprite.SpriteType.PUT_IF_ABSENT && expectedStringValue != null) {
          g.setColor(ConcurrentExampleConstants.CAS_ANIMATION_COLOR);
            g.drawString(expectedStringValue, xPos - ARROW_LENGTH * 11 + fontMetrics.stringWidth(String.valueOf(value)), y);
        } else if (expectedValue != ConcurrentSprite.NO_VALUE) {
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
  protected void drawPulling(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
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


  protected void drawRejected(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    int y = yPos;
    if (exampleType == ExampleType.ONE_USE) {
      y += (NEXT_LOCATION - VERTICAL_ARROW_DELTA);
    }
    switch (sprite.getType()) {
      case ARROW:
      case WORKING:
      case RUNNABLE:
      case PUT_IF_ABSENT:
      case CAS:
        int width = RADIUS * 2;
        if (exampleType == ExampleType.ONE_USE) {
          width += NEXT_LOCATION - VERTICAL_ARROW_DELTA;
        }
        if(sprite.getType() == ConcurrentSprite.SpriteType.CAS || sprite.getType() == ConcurrentSprite.SpriteType.PUT_IF_ABSENT) {
          g.fillOval(xPos - RADIUS - ARROW_LENGTH * 18 - RADIUS - 1, yPos - RADIUS, width, RADIUS * 2);
          g.drawLine(xPos, y, xPos - ARROW_LENGTH * 18, y);
          g.setColor(ConcurrentExampleConstants.CAS_ANIMATION_COLOR);
          int value = sprite.getValue();
          if (value != ConcurrentSprite.NO_VALUE) {
            g.drawString("<" + String.valueOf(value), xPos - ARROW_LENGTH * 18, y);
          }
          String expectedStringValue = sprite.getExpectedStringValue();
          if (sprite.getType() == ConcurrentSprite.SpriteType.PUT_IF_ABSENT && expectedStringValue != null) {
              g.setColor(ConcurrentExampleConstants.CAS_ANIMATION_COLOR);
              g.drawString(expectedStringValue + ">", xPos - ARROW_LENGTH * 18 + fontMetrics.stringWidth(String.valueOf(value)), y);
        }
        }
        else {
          g.fillOval(xPos - RADIUS - ARROW_LENGTH * 6, yPos - RADIUS, width, RADIUS * 2);
          g.drawLine(xPos, y, xPos - ARROW_LENGTH * 6, y);
        }
        g.drawLine(xPos, y, xPos - ARROW_LENGTH*6, y);
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
  protected void drawAcquiring(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    switch (sprite.getType()) {
      case RUNNABLE:
//        System.out.printf("State:%s location:%d  destination:%d%n", sprite.getState(), sprite.getCurrentLocation(), sprite.getDestination());
        // if right side of rectangle is to the left of mutex, butt up to mutex.
        // otherwise, center it
        if (sprite.isAcquired()) {
          // center the rectangle over the arrow
          if(xPos < ACQUIRE_BORDER + 52) {
            g.fill3DRect(xPos - 52, yPos-4 + (deltaY + BORDER) * verticalIndex, ARROW_LENGTH * 6 - 35, 8, true);
          }
          drawArrowSprite(g, xPos, yPos, sprite);
        }
        else {
          // butt the rectangle up to the mutex
          g.fill3DRect(xPos - 57 + 30, yPos-4 + (deltaY + BORDER) * verticalIndex, ARROW_LENGTH * 6 - 34, 8, true);
        }
        break;
      case WORKING:
      case ARROW:
      case CAS:
      case PUT_IF_ABSENT:
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

  protected void drawArrowSprite(Graphics g, int xPos, int yPos, ConcurrentSprite sprite) {
    int y = yPos;
    int y1 = yPos - ARROW_HEAD_LENGTH;
    int y2 = yPos + ARROW_HEAD_LENGTH;
    if (exampleType == ExampleType.ONE_USE) {
      y += NEXT_LOCATION - VERTICAL_ARROW_DELTA;
      y1 += (NEXT_LOCATION - VERTICAL_ARROW_DELTA);
      y2 += (NEXT_LOCATION - VERTICAL_ARROW_DELTA);
    }
    // WORKIING and RUNNABLE should render as an animated circular arrow
    if((sprite.getType() != ConcurrentSprite.SpriteType.WORKING && sprite.getType() != ConcurrentSprite.SpriteType.RUNNABLE)  || !sprite.isAcquired() || xPos < RELEASE_BORDER_WORKING - 30) {
      g.setColor(sprite.getColor());
      // draw the top arrow head
      g.drawLine(xPos, y, xPos - ARROW_HEAD_LENGTH * 4, y1);
      // draw the bottom arrow head
      g.drawLine(xPos, y, xPos - ARROW_HEAD_LENGTH * 4, y2);
      int length;
      if (sprite.getType() == ConcurrentSprite.SpriteType.CAS || sprite.getType() == ConcurrentSprite.SpriteType.PUT_IF_ABSENT) {
        length = ARROW_LENGTH * 3;
      } else {
        length = ARROW_LENGTH;
      }
      g.drawLine(xPos, y, xPos - length * 6, y);
      if (sprite.getType() == ConcurrentSprite.SpriteType.CAS || sprite.getType() == ConcurrentSprite.SpriteType.PUT_IF_ABSENT) {
        // draw the attempted replacement
        g.setColor(ConcurrentExampleConstants.CAS_ANIMATION_COLOR);
        int spriteValue = sprite.getValue();
        if (spriteValue != ConcurrentSprite.NO_VALUE) {
          String value = String.valueOf(spriteValue);
          if (sprite.getType() == ConcurrentSprite.SpriteType.PUT_IF_ABSENT) {
              g.drawString(value, xPos - 150, y);
          } else {
          g.drawString(value, xPos - 85, y);
        }
      }
      }
      int expectedValue = sprite.getExpectedValue();
      String expectedStringValue = sprite.getExpectedStringValue();
      if (sprite.getType() == ConcurrentSprite.SpriteType.PUT_IF_ABSENT && expectedStringValue != null) {
          g.setColor(ConcurrentExampleConstants.CAS_ANIMATION_COLOR);
          g.drawString(expectedStringValue + ">", xPos - 135, y);
      } else if (expectedValue != ConcurrentSprite.NO_VALUE) {
        g.drawString("(" + expectedValue + ")", xPos - 53, y);
      }
    } else {
      renderWorkingAnimation(g, y - 1, sprite.getCircleLocation());
      sprite.bumpCircleLocation();
    }
  }

  /**
   * Renders a "working" (oval path) animation at the default left border
   */
  protected void renderWorkingAnimation(Graphics g1, int yPos, int circleFrame) {
    renderWorkingAnimation(g1, getWorkerThreadLeftPosition(), yPos, circleFrame);
  }

  /**
   * Renders a "working" (oval path) animation at the specified left border
   * @param xBorder the pixel count to the left border from the start of the canvas
   */
  protected void renderWorkingAnimation(Graphics g1, int xBorder, int yPos, int circleFrame) {
    Graphics2D g = (Graphics2D) g1;
    g.setStroke(basicStroke);


//               <----------------W---------------->
//              (-W, 0)
//               ___________________________________(0,0)
//            .                   ^                   .
//       .                        |                      .
//     .                          |                        .
//    .                           |                         .
//    .                           2R = yDelta               .
//     .                          |                        .
//        .                       |                      .
//             . _________________V_________________  .
//             (-W, 2R)                              (0,2R)


    int W = 59;
    int R = 7;
    int yDelta = 2 * R;
    // top line of curved path
    g.drawLine(xBorder + 20, yPos, xBorder + W + 16, yPos);
    // bottom line of curved path
    g.drawLine(xBorder + 20, yPos + yDelta, xBorder + W + 16, yPos + yDelta);

    // left arc
    g.drawArc(xBorder + 13, yPos, 10, yDelta, 90, 180);
    // right arc
    g.drawArc(xBorder + 13 + W, yPos, 10, yDelta, -90, 180);

    // now render the animation
    // the number of pixels to move per frame
    double frameDelta = 3;
    int x, y;
    int radius = 4;
    // There is a horizontal rectangle with a semi circle on either end.
    // the top right of the rectangle is t=0, x=0, y=0
    // the width of the rectangle is W, the radius of the circle is R (the length of the rectangle is 2R)
    // the formula (as a dependency of t) is as follows:
    // t in [0 to 2R):      x= SQRT((2R-t)*t, y = t                   THIS IS THE RIGHT ARC
    // t in [2R to 2R+W]:   x=-t-2R, y=2R                             THIS IS THE BOTTOM LINE
    // t in (2R+W to 4R+W): T=t-(2R+W), x=-W-SQRT((2R-T)*T), y = 2R-T THIS IS THE LEFT ARC
    // t in [4R+W to 4R+2W]: x=t-(4R+2W), y=0                         THIS IS THE TOP LINE
    // take the modulus
    int t = (int) (frameDelta * circleFrame % (4*R + 2*W - 4));
    if(t >= 0 && t < 2*R) {
      // THE RIGHT ARC
      x = (int) Math.sqrt((2*R-t)*t);
      y = t;
      g.fillOval(xBorder + W + 20 + x - radius*2, yPos - radius / 2 - 2 + y, radius * 2, radius * 2);
    }
    else if(t >= 2*R && t <= 2*R+W) {
      // BOTTOM LINE
      x = -(t - 2*R) + radius;
      y = 2*R;
      g.fillOval(xBorder + W + 20 + x - radius*2, yPos - radius / 2 - 2 + y, radius * 2, radius * 2);
    }
    else if(t > 2*R+W && t < 4*R+W) {
      // LEFT ARC
      int T = t-(2*R+W);
      x = -W - (int) Math.sqrt((2*R-T) * T) + radius;
      y = 2*R-T;
      g.fillOval(xBorder + W + 20 + x - radius*2, yPos - radius / 2 - 2 + y, radius * 2, radius * 2);
    }
    else {//if(t >= 4*R+W && t < 4*R+2*W-1) {
      // TOP LINE
      x = t-(4*R+2*W) + radius;
      y = 0;
      g.fillOval(xBorder + W + 20 + x - radius*2, yPos - radius / 2 - 2 + y, radius * 2, radius * 2);
    }
  }

  /**
   * Returns the leftmost position of the worker thread.
   * In general this will be the acquire border. However canvas subclasses can modify this
   * @return the leftmost position of the worker thread.
   */
  protected int getWorkerThreadLeftPosition() {
    return ACQUIRE_BORDER;
  }

  public void addListener(ConcurrentAnimationEvent event, PropertyChangeListener listener) {
    PROPERTY_CHANGE_SUPPORT.addPropertyChangeListener(event.toString(), listener);
  }
  public void removeListener(ConcurrentAnimationEvent event, PropertyChangeListener listener) {
    PROPERTY_CHANGE_SUPPORT.removePropertyChangeListener(event.toString(), listener);
  }
  void notifyListeners(ConcurrentAnimationEvent event, ConcurrentSprite sprite) {
    PROPERTY_CHANGE_SUPPORT.firePropertyChange(event.toString(), sprite, null);
  }

  public void clearSprites() {
    sprites.clear();
    pooledSprites.clear();
  }

  protected ConcurrentExample getConcurrentExample() {
    return concurrentExample;
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