package vgrazi.concurrent.samples.examples;

/**
 * Used by the ConcurrentSpriteCanvas to determine pool size
 */
public interface Pooled {
  /**
   * Returns the number of available threads in the pool
   * @return the number of available threads in the pool
   */
  int getAvailableThreadCount();
}
