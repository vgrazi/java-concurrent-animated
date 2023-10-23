package vgrazi.concurrent.samples;

import java.awt.*;

public class ConcurrentExampleConstants {
  public static final String HTML_DISABLED_COLOR = "#808080";
  public static final Color MESSAGE_COLOR = new Color(0, 204, 0);
  public static final Color MESSAGE_FLASH_COLOR = new Color(0, 153, 0);
  public static final Color ERROR_MESSAGE_COLOR = Color.RED; //new Color(157, 14, 45); // RED
  public static final Color WARNING_MESSAGE_COLOR = new Color(255, 199, 38).darker();
  public static final Color ACQUIRING_COLOR = new Color(0, 56, 104).brighter().brighter().brighter();
  public static final Color ACQUIRING_INTERRUPTIBLY_COLOR = new Color(220, 220, 220);
  public static final Color ATTEMPTING_COLOR = Color.orange;
  public static final Color REJECTED_COLOR = new Color(157, 14, 45);//RED
//  public static final Color DEFAULT_BACKGROUND = new Color(0,0,0);// BLACK
  public static final Color DEFAULT_BACKGROUND = new Color(134, 204, 250);// SKY BLUE
  public static final Color MUTEX_BACKGROUND = new Color(135, 134, 126).brighter();//GRAY
  public static final Color MUTEX_FONT_COLOR = Color.BLACK;//new Color(255, 255, 255);//GRAY 40%
  public static final Color CAS_CIRCLE_COLOR = Color.black;
  public static final Color POOLED_FONT_COLOR = new Color(0, 0, 255);
  public static final Color CAS_ANIMATION_COLOR = Color.yellow;
  public static final Color TEXT_SPRITE_COLOR = Color.black ;
  public static final Color FORK_JOIN_COMPLETE_COLOR = ConcurrentExampleConstants.ACQUIRING_COLOR.darker();
  public static final Color[] FORK_JOIN_THREAD_COLORS = {Color.YELLOW, Color.CYAN, Color.PINK, Color.GRAY, Color.GREEN, Color.ORANGE};

  public static final Color READ_WRITE_HEAD_COLOR = new Color(0, 200, 0).darker();
  public static final Font MUTEX_HEADER_FONT = new Font("Avant Garde", Font.BOLD, 24);
  public static final Font LABEL_FONT = new Font("Avant Garde", Font.BOLD, 16);
  public static final Font TEXT_SPRITE_FONT = new Font("Avant Garde", Font.BOLD, 20);
//  public static final Font MUTEX_HEADER_FONT = new Font("Serif", Font.BOLD, 24);
//  public static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 16);
//  public static final Font TEXT_SPRITE_FONT = new Font("Serif", Font.BOLD, 20);
  public static final String LOGO = "images/concurrent.png";

  public static final String INSTRUCTIONS_FILE = "/images/instructions.html";
  public static final String REFERENCES_FILE = "/images/references.html";
  public static final String PLAN_FILE = "images/plan.xml";
  public static String FRAME_TITLE = "Visualizing the Java Concurrent API - JDK " + System.getProperty("java.version");
  /**
   * The color of the rectangular "Runnable" images
   */
  public static final Color RUNNABLE_COLOR = Color.yellow;
  // Thread state colors
  public static final Color NEW_THREAD_STATE_COLOR = Color.yellow;
  public static final Color RUNNABLE_THREAD_STATE_COLOR = Color.green;
  public static final Color BLOCKED_THREAD_STATE_COLOR = Color.red;// Color(0, 180, 255);
  public static final Color WAITING_THREAD_STATE_COLOR = Color.orange;
  public static final Color TIMED_WAITING_THREAD_STATE_COLOR = Color.magenta;
  public static final Color TERMINATED_THREAD_STATE_COLOR = Color.RED;
}
