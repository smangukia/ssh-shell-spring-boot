package com.github.fonimus.ssh.shell;

/**
 * Map enum color to jline ones
 */
public enum PromptColor {

	BLACK(0),
	RED(1),
	GREEN(2),
	YELLOW(3),
	BLUE(4),
	MAGENTA(5),
	CYAN(6),
	WHITE(7),
	BRIGHT(8);

	private final int value;

	PromptColor(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}