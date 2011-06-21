package vgrazi.concurrent.samples.examples;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.ExampleType;
import vgrazi.concurrent.samples.sprites.ConcurrentSprite;
import vgrazi.concurrent.samples.sprites.GlobalConcurrentMap;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentHashMapExample extends ConcurrentExample {

	private static final int MAP_SIZE = 10;

	private static ExecutorService threadPool = Executors.newCachedThreadPool();

	private final JButton putIfAbsentButton = new JButton("putIfAbsent");
	private boolean initialized = false;
	private JTextField threadCountField = createThreadCountField();
	private static final int MIN_SNIPPET_POSITION = 400;

	public ConcurrentHashMapExample(String title, Container frame,
			int slideNumber) {
		super(title, frame, ExampleType.CONCURRENT_MAP, MIN_SNIPPET_POSITION,
				true, slideNumber);
		reset();
		setState(6);
	}

	protected String getSnippet() {
		String snippet;
		snippet = "<html><PRE>\n"
				+ "<FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n"
				+ " \n"
				+ "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\""
				+ ConcurrentExampleConstants.HTML_DISABLED_COLOR
				+ "\"><I>// Construct empty concurrent map</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n"
				+ "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><B>final</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> ConcurrentMap&lt;Integer, String> concurrentMap = </FONT>"
				+ "<FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000080>\"><br>              <B>new</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state0:#000000>\"> ConcurrentHashMap&lt;Integer, String>();</FONT>\n"
				+ " \n"
				+ "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\""
				+ ConcurrentExampleConstants.HTML_DISABLED_COLOR
				+ "\"><I>// putIfAbsent only puts value if its key was not present in map before.</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n"
				+ "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\""
				+ ConcurrentExampleConstants.HTML_DISABLED_COLOR
				+ "\"><I>// the value returned holds the old value or null if key was not present before</I></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"#000000\"> \n"
				+ "    </FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>int</B></FONT><FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000000>\">"
				+ " key = 1;\n"
				+ "    String value = \"v(1)\";\n"
				+ "    String previousValue = concurrentMap.putIfAbsent(key, value);\n"
				+ "    <FONT style=\"font-family:monospaced;\" COLOR=\"<state1:#000080>\"><B>boolean</B></FONT> wasAbsent = previousValue == null;\n"

				+ " \n" + "</PRE></html>";

		return snippet;
	}

	protected void initializeComponents() {
		reset();
		if (!initialized) {
			initializeButton(putIfAbsentButton, new Runnable() {
				public void run() {
					putIfAbsent();
				}
			});
			initializeThreadCountField(threadCountField);
			resetThreadCountField();
			initialized = true;
		}
	}

	private int getRndValue() {
		return new Random().nextInt(MAP_SIZE);
	}

	private void putIfAbsent() {
		try {
			setState(1);
			final List<ConcurrentSprite> sprites = new ArrayList<ConcurrentSprite>();
			final int count = getThreadCount(threadCountField);
			for (int i = 0; i < count; i++) {
				ConcurrentSprite sprite = createAcquiringSprite();
				sprite.setType(ConcurrentSprite.SpriteType.PUT_IF_ABSENT);
				int rndValue = getRndValue();
				sprite.setValue(rndValue);
				sprite.setExpectedStringValue(", v(" + rndValue + ")");
				sprites.add(sprite);
			}

			// give all the animations time to arrive
			Thread.sleep((long) (1.5 * 1000));
			while (!sprites.isEmpty()) {
				int index = random.nextInt(sprites.size());
				final ConcurrentSprite sprite = sprites.remove(index);
				String present = GlobalConcurrentMap.get().putIfAbsent(
						sprite.getValue(), sprite.getExpectedStringValue());
				if (present == null) {
					sprite.setAcquired();
					// give the winner time to animate to the inside
					Thread.sleep((long) (.7 * 1000));
					// we want to create the illusion that the sprite "left" its
					// value in the monolith. So set the value to none.
					sprite.setValue(ConcurrentSprite.NO_VALUE);
					sprite.setExpectedStringValue(null);
					sprite.setReleased();
				} else {
					// animate the losers to rejcted state
					Runnable runnable = new Runnable() {
						public void run() {
							try {
								// give the losers time to hang out before
								// returning
								Thread.sleep((long) (.7 * 1000));
								sprite.setRejected();
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}
					};
					threadPool.submit(runnable);
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public String getDescriptionHtml() {
		StringBuffer sb = new StringBuffer();
		return sb.toString();
	}

	public void reset() {
		super.reset();
		resetThreadCountField();
		setState(0);
		GlobalConcurrentMap.set(new ConcurrentHashMap<Integer, String>());
		message1(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
		message2(" ", ConcurrentExampleConstants.DEFAULT_BACKGROUND);
	}

	/**
	 * Resets our thread count field to the default value of 4
	 */
	private void resetThreadCountField() {
		resetThreadCountField(threadCountField, 4);
	}

	@Override
	protected void setDefaultState() {
		setState(0);
	}

	public static void main(String[] args) {
		ConcurrentMap<Integer, String> concurrentMap = new ConcurrentHashMap<Integer, String>();
		int key = 1;
		String value = "value1";
		String previousValue = concurrentMap.putIfAbsent(key, value);
		boolean wasAbsent = previousValue == null;
	}

}