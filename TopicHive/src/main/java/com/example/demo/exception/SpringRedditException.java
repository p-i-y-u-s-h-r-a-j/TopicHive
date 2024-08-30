package com.example.demo.exception;

import org.springframework.mail.MailException;

public class SpringRedditException extends RuntimeException {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SpringRedditException(String exMessage, MailException exception) {
        super(exMessage, exception);
    }

    public SpringRedditException(String exMessage) {
        super(exMessage);
    }
}