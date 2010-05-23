package vgrazi.concurrent.samples.slides;

import java.awt.event.ActionListener;
import java.awt.*;
import java.util.TreeMap;
import java.util.Map;

public class ConcurrentSlideShow {
  /**
   * The index of the current slide
   */
  public static int slideShowIndex = 0;
  public static Map<Integer, ActionListener> slideShowSlides;


  /**
   * Advance the slide index. If it is beyond the number of slides, stop
   */
  public static void bumpSlideShowIndex() {
    if(slideShowIndex < slideShowSlides.size()) {
      slideShowIndex++;
    }
    else {
      slideShowIndex = 1;
    }
//    System.out.println("ConcurrentSlideShow.bumpSlideShowIndex displaying slide " + slideShowIndex);
  }

  public static void kickSlideShowIndex() {
    if(slideShowIndex > 1) {
      slideShowIndex--;
    }
    else {
      slideShowIndex = slideShowSlides.size();
    }
//    System.out.println("ConcurrentSlideShow.bumpSlideShowIndex displaying slide " + slideShowIndex);
  }

  public static ActionListener getCurrentSlideAction() {
    return slideShowSlides.get(ConcurrentSlideShow.slideShowIndex);
  }

  public static String getSlideShowIndex() {
    return null;
  }

  /**
   * When ConcurrrentExampleLauncher starts this example, it create a TreeMap&lt;Integer, ActionListener> for the slides.
   * @param slideShowSlides the TreeMap&lt;Integer, ActionListener> containing all of the slides keyed by index.
   */
  public static void setSlideShowSlides(TreeMap<Integer, ActionListener> slideShowSlides) {
    ConcurrentSlideShow.slideShowSlides = slideShowSlides;
  }

  public static void setSlideShowIndex(int slideNumber) {
    slideShowIndex = slideNumber;
  }

  public static void nextSlide() {
    bumpSlideShowIndex();
    ActionListener listener = getCurrentSlideAction();
    if(listener != null) {
      listener.actionPerformed(null);
    }
  }

  public static void previousSlide() {
    kickSlideShowIndex();
    ActionListener listener = getCurrentSlideAction();
    if(listener != null) {
      listener.actionPerformed(null);
    }
  }
}