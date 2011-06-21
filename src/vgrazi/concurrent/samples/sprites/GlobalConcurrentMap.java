package vgrazi.concurrent.samples.sprites;

import java.util.concurrent.ConcurrentMap;

public class GlobalConcurrentMap {
	private static ConcurrentMap<Integer, String> map;

	public static ConcurrentMap<Integer, String> get() {
		return GlobalConcurrentMap.map;
	}

	public static void set(ConcurrentMap<Integer, String> map) {
		GlobalConcurrentMap.map = map;
	}
}