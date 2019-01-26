package com.djira.ProyectoDjira.Domain;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "marcas")
public class Marcas implements Serializable {
	private static final long serialVersionUID = 498830613705738074L;
	
	@Id
	private String id;
	
	private String nombre;
	
	private String tipo;
	
	public Marcas() {
		super();
	}
	
	/**
	 * Constructor con todos los parametros
	 * @param id
	 * @param nombre
	 * @param alias
	 */
	public Marcas(String id, String nombre, String tipo) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.tipo = tipo;
	}
	
	/**
	 * Constructor con los parametros minimos
	 * @param id
	 * @param nombre
	 */
	public Marcas(String nombre, String tipo) {
		super();
		this.nombre = nombre;
		this.tipo = tipo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	@Override
    public String toString() {
        return String.format(
                "MarcasZapatillas[id=%s, nombre='%s', tipo='%s']",
                id, nombre, tipo);
    }
	
}
