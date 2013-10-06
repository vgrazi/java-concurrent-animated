package vgrazi.concurrent.samples.canvases;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.sprites.ConcurrentAnimationEvent;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import java.awt.*;

public class BasicCanvas extends ConcurrentSpriteCanvas {
  public BasicCanvas(ConcurrentExample concurrentExample, String labelText) {
    super(concurrentExample, labelText);
  }

  protected void drawMutex(Graphics2D g) {
    g.setColor(ConcurrentExampleConstants.MUTEX_BACKGROUND);
    g.fill3DRect(ACQUIRE_BORDER + leftOffset, topOffset, RELEASE_BORDER - ACQUIRE_BORDER + leftOffset, getHeight() - 20 - topOffset, true);
  }

  protected void renderSprites(Graphics2D g) {
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
          drawReleased(g, xPos, yPos, sprite);
          break;
        default:
          System.out.println("BasicCanvas.renderSprites NOT HANDLED: " + state);
      }
    }
  }

  protected void drawAcquiring(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    switch (sprite.getType()) {
      case RUNNABLE:
        if (sprite.isAcquired()) {
          // center the rectangle over the arrow
          if (xPos < ACQUIRE_BORDER + 52) {
            drawArrowSprite(g, xPos, yPos, sprite, null);
            g.setColor(ConcurrentExampleConstants.RUNNABLE_COLOR);
            g.fill3DRect(xPos - 52, yPos - 4 + (deltaY + BORDER) * verticalIndex, ARROW_LENGTH * 6 - 35, 8, true);
          } else {
            drawArrowSprite(g, xPos, yPos, sprite, null);
          }
        } else {
          // butt the rectangle up to the mutex
          g.setColor(ConcurrentExampleConstants.RUNNABLE_COLOR);
          g.fill3DRect(xPos - 57 + 30, yPos - 4 + (deltaY + BORDER) * verticalIndex, ARROW_LENGTH * 6 - 34, 8, true);
        }
        break;
      default:
        drawArrowSprite(g, xPos, yPos, sprite, null);
        sprite.bumpCurrentLocation(DELTA);
    }
    if (sprite.isActionCompleted()) {
      sprite.bumpLocationToDestination();
    }
    sprite.bumpCurrentLocation(DELTA);
    if (sprite.getCurrentLocation() >= sprite.getDestination()) {
      notifyListeners(ConcurrentAnimationEvent.ARRIVED, sprite);
    }
  }

  protected void drawReleased(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    g.fillOval(xPos - RADIUS, yPos - RADIUS, RADIUS * 2, RADIUS * 2);
    g.drawLine(xPos, yPos, xPos - ARROW_LENGTH * 6, yPos);
    sprite.bumpCurrentLocation(DELTA);
  }

  protected void drawRejected(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    g.fillOval(xPos - RADIUS - ARROW_LENGTH * 6, yPos - RADIUS, RADIUS * 2, RADIUS * 2);
    g.drawLine(xPos, yPos, xPos - ARROW_LENGTH * 6, yPos);
    g.drawLine(xPos, yPos, xPos - ARROW_LENGTH * 6, yPos);
    sprite.kickCurrentLocation(DELTA);
  }
}