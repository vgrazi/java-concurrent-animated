package vgrazi.concurrent.samples;

/**
 * Created by Victor Grazi.
* Date: Oct 20, 2007 - 8:16:50 PM
*/
public enum ExampleType {

  /**
   * Renders a blocking mutex
   */
  BLOCKING,

  /**
   * Renders a working mutex
   */
  WORKING,

  /**
   * Renders a short mutex, and changes the position with each use, emphasizing the object is not reused.
   */
  ONE_USE,

  /**
   * Renders a broken mutex indicating a plurality of mutex's
   */
  PLURAL,

  /**
   * Manipulates a number on the arrow and mutex
   */
  CAS
}
