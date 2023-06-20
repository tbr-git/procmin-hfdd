package org.processmining.hfddbackend.controller;

/**
 * Class that holds the content of the {@link HFDDController} responses.
 * 
 * Facilitates the use of the backend for frontends that assume json responses.
 * 
 * @author brockhoff
 *
 */
public class ResponseMessage {

	/**
	 * Message
	 */
	private String message;

	public ResponseMessage(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
