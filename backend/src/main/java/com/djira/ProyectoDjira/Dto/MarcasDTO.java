package com.djira.ProyectoDjira.Dto;

import java.io.Serializable;

public class MarcasDTO implements Serializable {
	
	private static final long serialVersionUID = -5918357188347229996L;
	
	private String name;
	
	private String tipo;
	
	public MarcasDTO() {
		super();
	}
	
	public MarcasDTO(String name, String tipo) {
		super();
		this.tipo = tipo;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
}
