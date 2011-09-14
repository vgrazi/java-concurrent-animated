package vgrazi.concurrent.samples.sprites;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.canvases.ConcurrentSpriteCanvas;

import java.awt.*;

/**
 * Created by Victor Grazi.
 * Date: Sep 10, 2005 - 8:48:02 PM
 */
public class ConcurrentSprite {
  /**
   * If value or expected value are set to NO_VALUE, they will not be drawn
   */
  public static int NO_VALUE = Integer.MIN_VALUE;
  private volatile SpriteState state = SpriteState.ACQUIRING;

  /**
   * WORKING sprite is equivalent to an ARROW sprint except in the acquired state.
   * When the sprite is acquired, an ARROW sprite will render as an arrow bouncing off the right border
   * Whereas a WORKING sprite will render as a rotating "working" thread.
   */
  private SpriteType type = SpriteType.WORKING;
  private int index = 0;

  protected int destination = 0;
  private int currentLocation = 0;
  private int verticalPosition;
  int circleLocation;

  public void setIndex(int index) {
    this.index = index;
  }

  public static enum SpriteType {
    WORKING, ARROW, RUNNABLE, OVAL, CAS, PULLER, TEXT, PUT_IF_ABSENT;
  }

  public static enum SpriteState {
    ACQUIRING,
    ACQUIRED,
    RELEASED,
    REJECTED,
    ACTION_COMPLETED,
    PULLING
  }

  /**
   * Used for CAS operations, value is the new value
   */
  private int value = NO_VALUE;
  /**
   * Used for CAS operations, checkValue is the originally checked value
   */
  private int expectedValue = NO_VALUE;

  private String expectedStringValue;

  private Color color = ConcurrentExampleConstants.ACQUIRING_COLOR;

  public ConcurrentSprite(int index, Color color) {
    this.index = index;
    this.color = color;
  }

  public ConcurrentSprite(int index) {
    this.index = index;
  }

  public void setAcquiring() {
    state = SpriteState.ACQUIRING;
    destination = ConcurrentSpriteCanvas.ACQUIRE_BORDER;
  }

  /**
   * Draw this Sprite outside the left border, attempting to get in
   */
  public void setAttempting() {
    setAcquiring();
    color = ConcurrentExampleConstants.ATTEMPTING_COLOR;
  }

  public void setActionCompleted() {
    state = SpriteState.ACTION_COMPLETED;
  }

  /**
   * Draw this Sprite inside the borders, waiting to be released
   */
  public void setAcquired() {
    state = SpriteState.ACQUIRED;
    destination = ConcurrentSpriteCanvas.RELEASE_BORDER;
    if(ConcurrentSpriteCanvas.exampleType == ExampleType.WORKING) {
      destination += 30;
    }
    //    if(currentLocation < ConcurrentSpriteCanvas.ACQUIRE_BORDER) {
    //      currentLocation = ConcurrentSpriteCanvas.ACQUIRE_BORDER;
    //    }
  }

  /**
   * Draw this Sprite pulling right, from mutex right border
   */
  public void setPulling() {
    if(state == SpriteState.ACQUIRING) {
      state = SpriteState.PULLING;
    }
    final int delta = 155;
    destination = ConcurrentSpriteCanvas.ACQUIRE_BORDER + delta + 10;
    if(currentLocation == 0) {
      setCurrentLocation(ConcurrentSpriteCanvas.ACQUIRE_BORDER + delta);
    }
  }

  /**
   * Draw this Sprite escaping from the borders.
   */
  public void setReleased() {
    // never allow a rejected sprite to be resurrected
    if (state != SpriteState.REJECTED) {
      state = SpriteState.RELEASED;
      destination = Integer.MAX_VALUE;
      if(currentLocation < ConcurrentSpriteCanvas.ACQUIRE_BORDER) {
        setCurrentLocation(ConcurrentSpriteCanvas.ACQUIRE_BORDER);
      }
    }
  }

  /**
   * Draw this Sprite rejected, turning away from the border
   */
  public void setRejected() {
    state = SpriteState.REJECTED;
    color = ConcurrentExampleConstants.REJECTED_COLOR;
    destination = 0;
  }

  public SpriteState getState() {
    return state;
  }

  public void setState(SpriteState state) {
    this.state = state;
  }

  public SpriteType getType() {
    return type;
  }

  public void setType(SpriteType type) {
    this.type = type;
  }

  public int getIndex() {
    return index;
  }

  public void setCurrentLocation(int currentLocation) {
    this.currentLocation = currentLocation;
//    System.out.printf("Current location: %d%n", currentLocation);
  }

  public void setVerticalPosition(int position) {
    verticalPosition = position;
  }

  public int getDestination() {
    return destination;
  }

  public void setDestination(int destination) {
    this.destination = destination;
  }

  public int getCurrentLocation() {
    return currentLocation;
  }

  public void bumpCurrentLocation(int pixels) {
    setCurrentLocation(currentLocation + pixels);
    if(currentLocation > destination) {
      setCurrentLocation(destination);
    }
    else if(currentLocation == destination) {
      setCurrentLocation(destination - ConcurrentSpriteCanvas.BACK_DELTA);
    }
  }

  public void bumpLocationToDestination() {
    setCurrentLocation(destination);
  }

  public void kickCurrentLocation(int pixels) {
    setCurrentLocation(currentLocation - pixels);
  }

  /**
   * circle location is only used by working threads to display the circular animation path
   * Other animations ignore it.
   */
  public void bumpCircleLocation() {
    circleLocation++;
  }

  public int getCircleLocation() {
    return circleLocation;
  }

  public void moveToAcquiringBorder() {
    setCurrentLocation(ConcurrentSpriteCanvas.ACQUIRE_BORDER - 30);
  }

  public void moveToAcquiredBorder() {
    setCurrentLocation(ConcurrentSpriteCanvas.ACQUIRE_BORDER);
  }

  public void moveToLocation(int location) {
    setCurrentLocation(location);
  }

  public Color getColor() {
    return color;
  }

  public boolean isAcquiring() {
    return state == SpriteState.ACQUIRING;
  }

  public boolean isAcquired() {
    return state == SpriteState.ACQUIRED;
  }

  public boolean isReleased() {
    return state == SpriteState.RELEASED;
  }

  public boolean isRejected() {
    return state == SpriteState.REJECTED;
  }

  public boolean isActionCompleted() {
    return state == SpriteState.ACTION_COMPLETED;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Used for CAS operations, value is the new value
   * @return the value
   */
  public int getValue() {
    return value;
  }

  /**
   * Used for CAS operations, value is the new value
   * @param value the value
   */
  public void setValue(int value) {
    this.value = value;
  }

  /**
   * Used for CAS operations, checkValue is the originally checked value
   * @return the value of checkValue
   */
  public int getExpectedValue() {
    return expectedValue;
  }

  /**
   * Used for CAS operations, checkValue is the originally checked value
   * @param checkValue the value
   */
  public void setExpectedValue(int checkValue) {
    this.expectedValue = checkValue;
  }

  public void setExpectedStringValue(String expectedStringValue) {
    this.expectedStringValue = expectedStringValue;
  }

  public String getExpectedStringValue() {
    return expectedStringValue;
  }
}
