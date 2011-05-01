package vgrazi.concurrent.samples.sprites;

/**
 * Used to render a text banner
 */
public class ConcurrentTextSprite extends ConcurrentSprite {
  private String text;

  public ConcurrentTextSprite(String text, int index) {
    super(index);
    setType(ConcurrentSprite.SpriteType.TEXT);
    this.text = text;
  }

  @Override
  public void bumpCurrentLocation(int pixels) {
    setCurrentLocation(getCurrentLocation() + pixels);
    if (getCurrentLocation() > destination) {
      setCurrentLocation(destination);
    }
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
