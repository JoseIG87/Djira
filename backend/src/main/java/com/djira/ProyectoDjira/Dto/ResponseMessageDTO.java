package com.djira.ProyectoDjira.Dto;

import java.io.Serializable;

public class ResponseMessageDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String error;
	
	private String mensaje;

	public ResponseMessageDTO(String error, String mensaje) {
		this.error = error;
		this.mensaje = mensaje;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	
}
