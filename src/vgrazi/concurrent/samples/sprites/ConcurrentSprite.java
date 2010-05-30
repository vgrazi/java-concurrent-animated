package vgrazi.concurrent.samples.sprites;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;

import java.awt.*;

/**
 * Created by Victor Grazi.
 * Date: Sep 10, 2005 - 8:48:02 PM
 */
public class ConcurrentSprite {


  public static enum SpriteState {
    ACQUIRING,
    ACQUIRED,
    RELEASED,
    REJECTED,
    ACTION_COMPLETED,
    PULLING
  }

  /**
   * If value or expected value are set to NO_VALUE, they will not be drawn
   */
  public static int NO_VALUE = Integer.MIN_VALUE;
  private SpriteState state = SpriteState.ACQUIRING;


  public static enum SpriteType {
    ARROW, RUNNABLE, OVAL, CAS, PULLER, TEXT;
  }

  private SpriteType type = SpriteType.ARROW;

  private int index = 0;

  protected int destination = 0;

  protected int currentLocation = 0;

  /**
   * Used for CAS operations, value is the new value
   */
  private int value = NO_VALUE;

  /**
   * Used for CAS operations, checkValue is the originally checked value
   */
  private int expectedValue = NO_VALUE;

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
    if (state == SpriteState.ACQUIRING) {
      state = SpriteState.PULLING;
    }
    final int delta = 155;
    destination = ConcurrentSpriteCanvas.ACQUIRE_BORDER + delta + 10;
    if (currentLocation  == 0) {
      currentLocation = ConcurrentSpriteCanvas.ACQUIRE_BORDER + delta;
    }
  }
  /**
   * Draw this Sprite escaping from the borders.
   */
  public void setReleased() {
    state = SpriteState.RELEASED;
    destination = Integer.MAX_VALUE;
    if (currentLocation < ConcurrentSpriteCanvas.ACQUIRE_BORDER) {
      currentLocation = ConcurrentSpriteCanvas.ACQUIRE_BORDER;
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

  public SpriteType getType() {
    return type;
  }

  public void setType(SpriteType type) {
    this.type = type;
  }

  public int getIndex() {
    return index;
  }

  public int getDestination() {
    return destination;
  }

  public int getCurrentLocation() {
    return currentLocation;
  }

  public void bumpCurrentLocation(int pixels) {
    currentLocation += pixels;
    if (currentLocation > destination) {
      currentLocation = destination;
    } else if (currentLocation == destination) {
      currentLocation = destination - ConcurrentSpriteCanvas.BACK_DELTA;
    }
  }

  public void bumpLocationToDestination() {
    currentLocation = destination;
  }

  public void kickCurrentLocation(int pixels) {
    currentLocation -= pixels;
  }

  public void moveToAcquiringBorder() {
    currentLocation = ConcurrentSpriteCanvas.ACQUIRE_BORDER - 30;
  }

  public void moveToAcquiredBorder() {
    currentLocation = ConcurrentSpriteCanvas.ACQUIRE_BORDER;
  }

  public void moveToLocation(int location) {
    currentLocation = location;
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
}
