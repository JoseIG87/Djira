package com.djira.ProyectoDjira.Domain;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "marcas_zapatillas")
public class MarcasZapatillas implements Serializable {
	private static final long serialVersionUID = 498830613705738074L;
	
	@Id
	private String id;
	
	private String nombre;
	
	public MarcasZapatillas() {
		super();
	}
	
	/**
	 * Constructor con todos los parametros
	 * @param id
	 * @param nombre
	 * @param alias
	 */
	public MarcasZapatillas(String id, String nombre) {
		super();
		this.id = id;
		this.nombre = nombre;
	}
	
	/**
	 * Constructor con los parametros minimos
	 * @param id
	 * @param nombre
	 */
	public MarcasZapatillas(String nombre) {
		super();
		this.nombre = nombre;
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
	
	@Override
    public String toString() {
        return String.format(
                "MarcasZapatillas[id=%s, nombre='%s']",
                id, nombre);
    }
	
}
