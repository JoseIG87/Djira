package com.djira.ProyectoDjira.Dto;

import java.io.Serializable;

public class MarcasDTO implements Serializable {
	
	private static final long serialVersionUID = -5918357188347229996L;
	
	private Integer id;
	private String name;
	
	
	public MarcasDTO() {
		super();
	}
	
	public MarcasDTO(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
