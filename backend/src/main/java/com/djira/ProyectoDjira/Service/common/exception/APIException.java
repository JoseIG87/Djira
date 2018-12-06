package com.djira.ProyectoDjira.Service.common.exception;

/**
 * clase encargada de manejar las excepciones generales de la aplicacion
 */
public class APIException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6000965124722452146L;

	

	public APIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public APIException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public APIException(String message) {
		super(message);
		
	}

	public APIException(Throwable cause) {
		super(cause);
		
	}
}
