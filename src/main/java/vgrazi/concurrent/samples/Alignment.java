package vgrazi.concurrent.samples;

import javax.swing.*;

/**
 * Created by Victor Grazi.
 * Date: Nov 25, 2007 - 9:24:55 PM
 */
public enum Alignment {
  NE(SwingConstants.RIGHT, SwingConstants.NORTH),
  E(SwingConstants.RIGHT, SwingConstants.CENTER),
  SE(SwingConstants.RIGHT, SwingConstants.SOUTH),

  N(SwingConstants.CENTER, SwingConstants.NORTH),
  CENTER(SwingConstants.CENTER, SwingConstants.CENTER),
  S(SwingConstants.CENTER, SwingConstants.SOUTH),

  NW(SwingConstants.LEFT, SwingConstants.NORTH),
  W(SwingConstants.LEFT, SwingConstants.CENTER),
  SW(SwingConstants.LEFT, SwingConstants.SOUTH);

  private final int horizontal;
  private final int vertical;

  Alignment(int horizontal, int vertical) {
    this.horizontal = horizontal;
    this.vertical = vertical;
  }

  public int getHorizontal() {
    return horizontal;
  }

  public int getVertical() {
    return vertical;
  }
}
