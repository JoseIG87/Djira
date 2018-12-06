package com.djira.ProyectoDjira.Dto;

import java.io.Serializable;

public class PaginaDTO implements Serializable {
	private static final long serialVersionUID = -6545030721759375281L;
	
	private String nombrePagina;
	private String url;
	
	public PaginaDTO(String nombrePagina, String url) {
		super();
		this.nombrePagina = nombrePagina;
		this.url = url;
	}

	public String getNombrePagina() {
		return nombrePagina;
	}

	public void setNombrePagina(String nombrePagina) {
		this.nombrePagina = nombrePagina;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
