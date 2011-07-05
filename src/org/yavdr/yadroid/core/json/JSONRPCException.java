package org.yavdr.yadroid.core.json;

public class JSONRPCException extends Exception {

	private static final long serialVersionUID = 4657697652848090922L;

	public JSONRPCException(Object error) {
		super(error.toString());
	}

	public JSONRPCException(String message, Throwable innerException) {
		super(message, innerException);
	}
}
