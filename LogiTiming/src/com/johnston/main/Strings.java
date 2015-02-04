package com.johnston.main;

import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.StringGetter;

class Strings {
	private static LocaleManager source
		= new LocaleManager("resources/logisim", "menu");

	public static String get(String key) {
		return source.get(key);
	}
	public static StringGetter getter(String key) {
		return source.getter(key);
	}
}
