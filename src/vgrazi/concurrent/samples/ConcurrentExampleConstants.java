package vgrazi.concurrent.samples;

import java.awt.*;

/*
* Copyright (CENTER) 2007 - Holt - Credit Suisse
* Date: Oct 17, 2007 * Time: 5:56:39 PM
* @version $Revision: $
*/
public class ConcurrentExampleConstants {
  public static final String HTML_DISABLED_COLOR = "#808080";
  public static final String HTML_FONT_COLOR = "#ffffff";
  public static final String HTML_EM_FONT_COLOR = "#6CCBED";
  public static final Color MESSAGE_COLOR = new Color(0, 153, 0);
  public static final Color MESSAGE_FLASH_COLOR = new Color(0, 204, 0);
  public static final Color ERROR_MESSAGE_COLOR = Color.white; //new Color(157, 14, 45); // RED
  public static final Color WARNING_MESSAGE_COLOR = new Color(255, 199, 38);
  public static final Color ACQUIRING_COLOR = new Color(108, 203, 237);
  //  private static final Color REJECTED_COLOR = Color.orange;
  public static final Color ATTEMPTING_COLOR = new Color(255, 199, 38);
  public static final Color REJECTED_COLOR = Color.white;//new Color(157, 14, 45);//RED
  public static final Color DEFAULT_BACKGROUND = new Color(0, 56, 104);// BLUE
  public static final Color MUTEX_BACKGROUND = new Color(135, 134, 126);//GRAY
  public static final Color MUTEX_FONT_COLOR = new Color(227, 223, 219);//GRAY 40%
  public static final Font MUTEX_HEADER_FONT = new Font("TimesRoman", Font.BOLD, 24);
  public static final Font BUTTON_FONT = new Font("Arial", Font.PLAIN, 12);
  public static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 12);
  public static final String LOGO = "images/concurrent.gif";
//  public static final String LOGO = "images/CS.JPG";
  public static final String LOGO_APPLET = "concurrent.gif";
  public static final String[] SLIDES = new String[] {
          "CENTER:\\Documents and Settings\\Victor\\My Documents\\My Pictures\\Morris.jpg",
          "CENTER:\\Documents and Settings\\Victor\\My Documents\\My Pictures\\Morris_Frieda.jpg",
          "CENTER:\\Documents and Settings\\Victor\\My Documents\\My Pictures\\Morris_Frieda_Victoria.jpg",
  };

  public static int imageIndex = 0;
  public static final String SPLASH_LABEL =
          "<html>" +
          "<table align=\"LEFT\" valign=\"center\" width=\"1000\" border=\"0\">" +
          "<tr><td width=\"30\">&nbsp;</td><td align=\"CENTER\"><font size=\"12\" color=\"white\">Visualizing the Java Concurrent API</font></td><td width=\"300\">&nbsp;</td><td rowspan=7>&nbsp;</TD></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"6\" color=\"white\">Instructions for use</font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\"><ul><li>Select menu items above to load animations.</li></ul></font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\"><ul><li>The arrows represent contending threads.</li></ul></font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\"><ul><li>" +
          "The monolith in the center of each animation <br>represents a lock of some kind. <br>Threads to the left are waiting to grab the lock. <br>Threads inside already own the lock." +
//          "A thin monolith represents a lock that is held by the acquiring thread. A wide monolith represents a lock that is controlled by an external thread." +
          "</li></ul></font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\"><ul><li>Blue arrows wait indefinitely, <br>orange arrows time out, <br>red arrows represent write locks." +
//          " (In the read-write lock blue arrows are read locks and orange are write locks)." +
          "</li></ul></font></td><td>&nbsp;</td></tr>" +
//          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\"><ul><li>Ctrl-B brings the button frame to the front (Except when splash is showing).</li></ul></font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\"><ul><li>Ctrl-R resets the current animation.</li></ul></font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\"><ul><li>Ctrl-S pauses/resumes the current animation.</li></ul></font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"red\"><ul><li>Page Up and Page Dn to see the slide show.</li></ul></font></td><td>&nbsp;</td></tr>" +
//          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\"><ul><li>Clicking the \"Splash\" button toggles opening-graphic/references</li></ul></font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td>" +
          "<td align=\"left\"><font size=\"4\" color=\"white\">" +
//          "Copyright(CENTER) 2007 Victor J. Grazi<BR>" +
          "Created by <BR>" +
          "" +
          "<ul>" +
          "<li>Victor J. Grazi<br>" +
          "VP Application Development - Credit Suisse<br>" +
          "vgrazi@gmail.com<br><br></li>" +
          "</ul>" +
          "</font></td><td>&nbsp;</font></td><td>&nbsp;</td></tr>" +
          "</table>" +
          "</html>";
  public static final String SPLASH_LABEL_APPLET = "<html>" +
     "<table align=\"left\" valign=\"top\" width=\"1000\" border=\"0\">" +
     "<tr><td align=\"center\"><font size=\"12\" color=\"black\">Java Concurrent Library</font></td><td rowspan=7>&nbsp;</TD></tr>" +
     "<tr><td align=\"center\"><font size=\"7\" color=\"black\">Animated</td></tr>" +
     "<tr><td align=\"center\"><font size=\"7\" color=\"black\">&nbsp</td></tr>" +
     "<tr><td align=\"center\"><font size=\"6\" color=\"black\">By Victor Grazi</td></tr>" +
          "<tr><td align=\"center\"><font size=\"6\" color=\"black\"></td></tr>" +
          "<tr><td align=\"center\"><font size=\"7\" color=\"black\">&nbsp</td></tr>" +
          "<tr><td align=\"center\"><font size=\"7\" color=\"black\">&nbsp</td></tr>" +
          "<tr><td align=\"center\"><font size=\"7\" color=\"black\">&nbsp</td></tr>" +
     "</table>" +
     "</html>";

  public static final String REFERENCES_LABEL = "<html>" +
          "<table align=\"LEFT\" valign=\"center\" width=\"1000\" border=\"0\">" +
//          "<tr><td width=\"30\">&nbsp;</td><td align=\"CENTER\"><font size=\"12\" color=\"white\">Java Concurrent Library</font></td><td width=\"300\">&nbsp;</td><td rowspan=7><img src=\"" + LOGO + "\"/></TD></tr>" +
          "<tr><td width=\"30\">&nbsp;</td><td align=\"CENTER\"><font size=\"8\" color=\"white\"><u>References</u></font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\">Concurrent Programming in Java: Design Principles and Patterns (2nd Edition) </font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\">&#09;by Doug Lea</font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\">&nbsp;</font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\">Java Concurrency in Practice </font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color= \"white\">&#09;by Brian Goetz, Tim Peierls, </td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color= \"white\">&#09;Joshua Bloch and Joseph Bowbeer</td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\">&nbsp;</font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\">Backport:http://http://backport-jsr166.sourceforge.net/index.php/<br></font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\">&nbsp;</font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"5\" color=\"white\">" +
//          "Presented by Victor Grazi<br>" +
//          "<BR>VP - Global Application Development<BR>Credit Suisse - HOLT<br>" +
//          "victor.grazi@credit-suisse.com" +
          "Created by <BR>" +
          "<ul><li>Victor J. Grazi<br>" +
          "VP Application Development - Credit Suisse<br>" +
          "vgrazi@gmail.com<br><br></li>" +
          "</ul>" +
          "</font></td><td>&nbsp;</td></tr>" +
//          "<tr><td >&nbsp;</td><td align=\"left\"><font size=\"4\" color=\"dark blue\">Copyright(CENTER) 2007 Victor J. Grazi</font></td><td>&nbsp;</td></tr>" +
          "</table>" +
          "</html>";
  public static final String REFERENCES_LABEL_APPLET = "<html>" +
          "<table align=\"LEFT\" valign=\"center\" width=\"1000\" border=\"0\">" +
//          "<tr><td width=\"30\">&nbsp;</td><td align=\"CENTER\"><font size=\"12\" color=\"black\">Java Concurrent Library</font></td><td width=\"300\">&nbsp;</td><td rowspan=7><img src=\"" + LOGO + "\"/></TD></tr>" +
          "<tr><td width=\"30\">&nbsp;</td><td align=\"CENTER\"><font size=\"8\" color=\"black\"><u>References</u></font></td><td>&nbsp;</td></tr>" +
          "<tr><td >&nbsp;</td><td align=\"left\"><font size=\"6\" color=\"black\">Concurrent Programming in Java: Design Principles and Patterns (2nd Edition) </font></td><td>&nbsp;</td></tr>" +
          "<tr><td >&nbsp;</td><td align=\"left\"><font size=\"6\" color=\"black\">&#09;by Doug Lea</font></td><td>&nbsp;</td></tr>" +
          "<tr><td >&nbsp;</td><td align=\"left\"><font size=\"6\" color=\"black\">&nbsp;</font></td><td>&nbsp;</td></tr>" +
          "<tr><td >&nbsp;</td><td align=\"left\"><font size=\"6\" color=\"black\">Java Concurrency in Practice </font></td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"6\" color= \"black\">&#09;by Brian Goetz, Tim Peierls, </td><td>&nbsp;</td></tr>" +
          "<tr><td>&nbsp;</td><td align=\"left\"><font size=\"6\" color= \"black\">&#09;Joshua Bloch and Joseph Bowbeer</td><td>&nbsp;</td></tr>" +
          "<tr><td >&nbsp;</td><td align=\"left\"><font size=\"6\" color=\"black\">&nbsp;</font></td><td>&nbsp;</td></tr>" +
          "<tr><td >&nbsp;</td><td align=\"left\"><font size=\"6\" color=\"black\">Backport:http://dcl.mathcs.emory.edu/<br>&#09;&#09;util/backport-util-vgrazi.concurrent/</font></td><td>&nbsp;</td></tr>" +
          "<tr><td >&nbsp;</td><td align=\"left\"><font size=\"6\" color=\"black\">&nbsp;</font></td><td>&nbsp;</td></tr>" +
          "<tr><td >&nbsp;</td><td align=\"left\"><font size=\"6\" color=\"black\">Presented by Victor Grazi<BR>" +
//          "VP - Global Application Development<BR>Credit Suisse - HOLT<br>" +
          "victor.grazi@credit-suisse.com</font></td><td>&nbsp;</td></tr>" +
//          "<tr><td >&nbsp;</td><td align=\"left\"><font size=\"4\" color=\"black\">Copyright(CENTER) 2007 Victor J. Grazi <br> May not be reused without express permission of the author</font></td><td>&nbsp;</td></tr>" +
          "</table>" +
          "</html>";
  public static final String INSTRUCTIONS_FILE = "/images/instructions.html";
  public static final String REFERENCES_FILE = "/images/references.html";
  public static final String PLAN_FILE = "images/plan.xml";
  public static final Color CAS_CIRCLE_COLOR = Color.black;
  public static final Color CAS_ANIMATION_COLOR = Color.white;
}
/*
 * $Log: $
 */