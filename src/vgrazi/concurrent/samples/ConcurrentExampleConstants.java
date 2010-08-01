package vgrazi.concurrent.samples;

import java.awt.*;

public class ConcurrentExampleConstants {
  public static final String HTML_DISABLED_COLOR = "#808080";
  public static final String HTML_FONT_COLOR = "#ffffff";
  public static final String HTML_EM_FONT_COLOR = "#6CCBED";
  public static final Color MESSAGE_COLOR = new Color(0, 153, 0);
  public static final Color MESSAGE_FLASH_COLOR = new Color(0, 204, 0);
  public static final Color ERROR_MESSAGE_COLOR = Color.white; //new Color(157, 14, 45); // RED
  public static final Color WARNING_MESSAGE_COLOR = new Color(255, 199, 38);
  public static final Color ACQUIRING_COLOR = new Color(108, 203, 237);
  public static final Color ATTEMPTING_COLOR = new Color(255, 199, 38);
  public static final Color REJECTED_COLOR = Color.white;//new Color(157, 14, 45);//RED
  public static final Color DEFAULT_BACKGROUND = new Color(0, 56, 104);// DARK BLUE
  public static final Color MUTEX_BACKGROUND = new Color(135, 134, 126);//GRAY
  public static final Color MUTEX_FONT_COLOR = new Color(227, 223, 219);//GRAY 40%
  public static final Color CAS_CIRCLE_COLOR = Color.black;
  public static final Color CAS_ANIMATION_COLOR = Color.white;
  protected static final Color DEFAULT_BUTTON_COLOR = Color.BLUE;

  public static final Font MUTEX_HEADER_FONT = new Font("Serif", Font.BOLD, 24);
  public static final Font BUTTON_FONT = new Font("SansSerif", Font.PLAIN, 12);
  public static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 16);
  public static final String LOGO = "images/concurrent.gif";
//  public static final String LOGO = "images/CS.JPG";
  public static final String LOGO_APPLET = "concurrent.gif";
  public static final String[] SLIDES = new String[] {
          "CENTER:\\Documents and Settings\\Victor\\My Documents\\My Pictures\\Morris.jpg",
          "CENTER:\\Documents and Settings\\Victor\\My Documents\\My Pictures\\Morris_Frieda.jpg",
          "CENTER:\\Documents and Settings\\Victor\\My Documents\\My Pictures\\Morris_Frieda_Victoria.jpg",
  };

  public static int imageIndex = 0;
  public static final String INSTRUCTIONS_FILE = "/images/instructions.html";
  public static final String REFERENCES_FILE = "/images/references.html";
  public static final String PLAN_FILE = "images/plan.xml";
}
/*
 * $Log: $
 */