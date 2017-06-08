package edu.umich.ctools.esb.utils;

public class WAPIException extends RuntimeException {


	/*
	 * Exception for un-recoverable errors in PersistString 
	 */

	private static final long serialVersionUID = -2232997115239589804L;

	WAPIException(String message) {
		super(message);
	}

	public WAPIException(String message, Throwable e) {
		super(message,e);
	}

}
