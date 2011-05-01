package vgrazi.concurrent.samples.sprites;

import vgrazi.concurrent.samples.canvases.ConcurrentSpriteCanvas;
import vgrazi.concurrent.samples.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date: Sep 10, 2005
 * Time: 8:54:49 PM
 */
public class TestSpriteEngine {
  private final ConcurrentSpriteCanvas canvas = new ConcurrentSpriteCanvas(null, "");
  private final AtomicInteger acquiring = new AtomicInteger(0);
  private final java.util.Stack acquiringSprites = new Stack();
  private final java.util.Stack acquiredSprites = new Stack();

  public static void main(String[] args) {
    new TestSpriteEngine().test();
  }

  private void test() {
    JFrame frame = new JFrame("Concurrent Sprite Test");
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(canvas, BorderLayout.CENTER);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    frame.setSize(800, 400);
    UIUtils.center(frame);


    JPanel buttonPanel = createButtonPanel();
    frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    frame.show();
  }

  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setOpaque(true);

    JButton button;
    button = new JButton("Acquiring");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = acquiring.incrementAndGet();
        ConcurrentSprite sprite = new ConcurrentSprite(index);
        sprite.setAcquiring();
        acquiringSprites.push(sprite);
        canvas.addSprite(sprite);
      }
    });
    buttonPanel.add(button);

    button = new JButton("Acquired");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ConcurrentSprite sprite = (ConcurrentSprite) acquiringSprites.pop();
        acquiredSprites.push(sprite);
        sprite.setAcquired();
      }
    });
    buttonPanel.add(button);

    button = new JButton("Released");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(!acquiredSprites.empty()) {
          ConcurrentSprite sprite = (ConcurrentSprite) acquiredSprites.pop();
          sprite.setReleased();
        }
      }
    });
    buttonPanel.add(button);
    return buttonPanel;
  }
}
