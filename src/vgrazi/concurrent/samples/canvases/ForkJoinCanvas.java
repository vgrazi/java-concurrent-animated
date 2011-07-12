package vgrazi.concurrent.samples.canvases;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.examples.ConcurrentExample;
import vgrazi.concurrent.samples.examples.ForkJoinConcurrentExample;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;
import vgrazi.concurrent.samples.sprites.ForkJoinSprite;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ForkJoinCanvas extends ConcurrentSpriteCanvas {

  private final int spriteWidth = 100;
  private final int spriteHeight = 45;
  private final int leftBorder = 40;
  private int lastActiveCount;
  private int maxActiveThreadCount;
  private FontMetrics fontMetrics;
  private List<ForkJoinSprite> sprites = new ArrayList<ForkJoinSprite>();
  private final Map<Integer, Integer> levelMap = new ConcurrentHashMap<Integer, Integer>();
  public ForkJoinCanvas(final ConcurrentExample concurrentExample, final String labelText) {
    super(concurrentExample, labelText);
    setFont(ConcurrentExampleConstants.TEXT_SPRITE_FONT);
    fontMetrics = getFontMetrics(getFont());
  }

  @Override
  public void addSprite(ConcurrentSprite sprite) {
    ((ForkJoinConcurrentExample) getConcurrentExample()).setAnimating(true);
    sprites.add((ForkJoinSprite) sprite);
    notifyAnimationThread();
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
//    g.drawRect(0, 0, getWidth(), getHeight());
    drawForkJoinSprites((Graphics2D) g);
  }

  private void drawForkJoinSprites(Graphics2D g) {
    int activeThreadCount = 0;
    for(int i = 0, spritesSize = sprites.size(); i < spritesSize; i++) {
      ForkJoinSprite sprite = sprites.get(i);
      if(sprite == null) {
          continue;
      }
      if(!sprite.isComplete()) {
        g.setColor(ConcurrentExampleConstants.ACQUIRING_COLOR);
      }
      else {
        g.setColor(ConcurrentExampleConstants.FORK_JOIN_COMPLETE_COLOR);
      }
      final int level = sprite.getLevel();
      // get the next vertical position for this level
      Integer levelIndex = sprite.getLevelIndex();
      if(levelIndex == -1) {
        levelIndex = levelMap.get(level);
        if(levelIndex == null) {
          levelIndex = 0;
        }
        sprite.setLevelIndex(levelIndex);
      }
      int yPos = (levelIndex + 1)*spriteHeight ;
      int left = leftBorder + level*spriteWidth;
      g.fill3DRect(left, yPos, spriteWidth, spriteHeight, true);
      // center the text
      g.setColor(ConcurrentExampleConstants.TEXT_SPRITE_COLOR);
      g.setFont(ConcurrentExampleConstants.TEXT_SPRITE_FONT);

      int textWidth;
      int xPos;
      String text;
      if(sprite.isComplete()) {
        // render the solution
        text = sprite.getSolution();
        textWidth = fontMetrics.stringWidth(text);
        xPos = (spriteWidth - textWidth)/2 + left;

        g.drawString(text, xPos, yPos + spriteHeight - fontMetrics.getHeight() -3);
      }
      text = String.format("(%d, %d)", sprite.getStart(), sprite.getEnd());
      textWidth = fontMetrics.stringWidth(text);
      xPos = (spriteWidth - textWidth)/2 + left;
      g.drawString(text, xPos, yPos + spriteHeight - fontMetrics.getDescent() - 2);

      // render the worker thread animation
      if(sprite.getForkJoinThread() != null) {
        g.setColor(sprite.getForkJoinThread().getThreadColor());
//        g.drawLine(left + 5, yPos + 5, left + spriteWidth - 10, yPos + 5);
        renderWorkingAnimation(g, left, yPos + 5, sprite.getCircleLocation());
        sprite.bumpCircleLocation();
        activeThreadCount++;
      }
      levelMap.put(level, levelIndex + 1);
    }
    if(activeThreadCount != 0 && lastActiveCount != activeThreadCount) {
      lastActiveCount = activeThreadCount;
        if(activeThreadCount > maxActiveThreadCount) {
            maxActiveThreadCount = activeThreadCount;
        }
      getConcurrentExample().message1(String.format("active threads:%d\tmax active threads:%d", activeThreadCount, maxActiveThreadCount), ConcurrentExampleConstants.MESSAGE_COLOR);
//      System.out.printf("ForkJoinCanvas.drawForkJoinSprites active thread count:%d%n", activeThreadCount);
    }
  }

  @Override
  protected boolean isAnimating() {
    return ((ForkJoinConcurrentExample) getConcurrentExample()).isAnimating();
  }

  @Override
  public void clearSprites() {
    super.clearSprites();
    sprites.clear();
  }

  @Override
  protected void drawMutex(Graphics2D g, Dimension size) {
    // no mutex in this example
  }

  public void reset() {
    levelMap.clear();
    clearSprites();
  }
}
