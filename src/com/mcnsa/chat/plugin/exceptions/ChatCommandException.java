package com.mcnsa.chat.plugin.exceptions;

public class ChatCommandException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2794584664600435475L;

	public ChatCommandException(String message) {
		super(message);
	}

	public ChatCommandException(String format, Object... args) {
		super(String.format(format, args));
	}
}