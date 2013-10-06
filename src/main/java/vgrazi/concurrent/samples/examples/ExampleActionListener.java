package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.launcher.ConcurrentExampleLauncher;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by Victor Grazi.
 * Date: Nov 21, 2007 - 11:00:19 PM
 */
public class ExampleActionListener implements ActionListener {
  private final ConcurrentExample examplePanel;

  public ExampleActionListener(ConcurrentExample examplePanel) {
    this.examplePanel = examplePanel;
  }

  public void actionPerformed(ActionEvent e) {
    ConcurrentExampleLauncher.getInstance().showExamplePanel(examplePanel);
  }
}
