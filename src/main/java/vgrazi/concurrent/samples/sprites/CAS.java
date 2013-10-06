package vgrazi.concurrent.samples.sprites;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;

import java.awt.*;

/**
 * Ecnapsulates the value(s) contained in the MUTEX for a Compare and Swap operation
 * Created by Victor Grazi.
 * Date: Sep 10, 2005 - 8:48:02 PM
 */
public class CAS {
  private static int value;

  public static int getValue() {
    return value;
  }

  public static void setValue(int newValue) {
    value = newValue;
  }
}