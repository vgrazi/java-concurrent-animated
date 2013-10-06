package vgrazi.concurrent.samples.canvases;

import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.sprites.ConcurrentAnimationEvent;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import java.awt.*;

public class OvalObjectCanvas extends BasicCanvas {
  public OvalObjectCanvas(ConcurrentExample concurrentExample, String labelText) {
    super(concurrentExample, labelText);
  }

  protected void drawAcquiring(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    g.fillOval(xPos - 85, yPos, OVAL_LENGTH * 18, OVAL_LENGTH * 2 + 5);

    if (sprite.isActionCompleted()) {
      sprite.bumpLocationToDestination();
    }
    sprite.bumpCurrentLocation(DELTA);
    if (sprite.getCurrentLocation() >= sprite.getDestination()) {
      notifyListeners(ConcurrentAnimationEvent.ARRIVED, sprite);
    }
  }

  @Override
  protected void drawReleased(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    g.fillOval(xPos - 85, yPos, OVAL_LENGTH * 18, OVAL_LENGTH * 2);
    sprite.bumpCurrentLocation(DELTA);
  }

  @Override
  protected void drawRejected(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    g.fillOval(xPos - 85, yPos, OVAL_LENGTH * 18, OVAL_LENGTH * 2);
    sprite.kickCurrentLocation(DELTA);
  }
}
