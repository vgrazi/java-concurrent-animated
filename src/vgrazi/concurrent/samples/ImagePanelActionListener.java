package vgrazi.concurrent.samples;

import vgrazi.concurrent.samples.Alignment;
import vgrazi.concurrent.samples.launcher.ConcurrentExampleLauncher;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Victor Grazi.
 * Date: Nov 22, 2007 - 12:01:12 AM
 */
public class ImagePanelActionListener implements ActionListener {
  private final String title;
  private final String imageName;
  private JLabel label;
  private final boolean resizeImage;
  private final Alignment alignment;

  /**
   * @param imageName the path name of the image. Null if none
   * @param htmlText the text to overlay the image, in HTML format. null if none
   * @param resizeImage
   * @param alignment
   */
  public ImagePanelActionListener(String imageName, String htmlText, boolean resizeImage, Alignment alignment) {
    this(null, imageName, htmlText, resizeImage, alignment);
  }

  /**
   * @param title the title to display in the frame. null to leave the current title
   * @param imageName the path name of the image. Null if none
   * @param htmlText the text to overlay the image, in HTML format. null if none
   * @param resizeImage
   * @param alignment
   */
  public ImagePanelActionListener(String title, String imageName, String htmlText, boolean resizeImage, Alignment alignment) {
    this.title = title;
    this.imageName = imageName;
    this.resizeImage = resizeImage;
    this.alignment = alignment;
    if(htmlText != null) {
      label = new JLabel(htmlText);
    }
    else {
      label = new JLabel();
    }
  }

  public void actionPerformed(ActionEvent e) {
    ConcurrentExampleLauncher.getInstance().showTitlePane(label, title, imageName, resizeImage, alignment);
  }
}
