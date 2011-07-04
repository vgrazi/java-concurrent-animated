package vgrazi.concurrent.samples.canvases;

import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.sprites.ConcurrentAnimationEvent;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;

import java.awt.*;

public class ForkAndJoinCanvas extends ConcurrentSpriteCanvas {

  public ForkAndJoinCanvas(final ConcurrentExample concurrentExample, final String labelText) {
    super(concurrentExample, null);
  }

  @Override
  protected int getWorkerThreadLeftPosition() {
    return 300;
  }

  @Override
  protected void drawAcquiring(Graphics2D g, int xPos, int yPos, ConcurrentSprite sprite) {
    if(sprite.getType() == ConcurrentSprite.SpriteType.WORKING) {
      renderWorkingAnimation(g, yPos, xPos);
      sprite.bumpCurrentLocation(1);
      int location = sprite.getCurrentLocation();
      if(location >= 399) {
        sprite.setCurrentLocation(350);
      }
    }
    else {
      drawArrowSprite(g, xPos, yPos, sprite);
      sprite.bumpCurrentLocation(DELTA);
      if (sprite.isActionCompleted()) {
        sprite.bumpLocationToDestination();
      }
      if(sprite.getCurrentLocation() >= sprite.getDestination()) {
        notifyListeners(ConcurrentAnimationEvent.ARRIVED, sprite);
      }
    }
  }
}
