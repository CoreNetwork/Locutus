package com.mcnsa.chat.plugin.exceptions;

public class DatabaseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2794584664600435475L;

	public DatabaseException(String message) {
		super(message);
	}

	public DatabaseException(String format, Object... args) {
		super(String.format(format, args));
	}
}