package vgrazi.concurrent.samples.sprites;

import java.awt.*;

import static vgrazi.concurrent.samples.ConcurrentExampleConstants.*;
import static vgrazi.concurrent.samples.ConcurrentExampleConstants.TERMINATED_THREAD_STATE_COLOR;
import static vgrazi.concurrent.samples.ConcurrentExampleConstants.TIMED_WAITING_THREAD_STATE_COLOR;

public class ThreadStateToColorMapper {

  public static Color getColorForState(ConcurrentSprite sprite) {
    Thread.State state = sprite.getThreadState();
    if (state != null) {
      switch (state) {
        case NEW:
          return NEW_THREAD_STATE_COLOR;
        case RUNNABLE:
          return RUNNABLE_THREAD_STATE_COLOR;
        case BLOCKED:
          return BLOCKED_THREAD_STATE_COLOR;
        case WAITING:
          return WAITING_THREAD_STATE_COLOR;
        case TIMED_WAITING:
          return TIMED_WAITING_THREAD_STATE_COLOR;
        case TERMINATED:
          return TERMINATED_THREAD_STATE_COLOR;
      }
    }
    return null;
  }
}
