package com.djira.ProyectoDjira.Domain;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ropa")
public class Ropa implements Serializable {
	
	private static final long serialVersionUID = -8821839309318262656L;
	
	@Id
	private String id;
	
	private String imagenUrl;
	private String nombre;
	private BigDecimal precio;
	private String marca;
	private String tipo;
	private String estilo;
	private String genero;
	private String urlPaginaOrigen;
	private String nombrePaginaOrigen;
	
	/**
	 * Constructor por defecto
	 */
	public Ropa() {
		super();
	}
	
	/**
	 * Constructor con todos los campos
	 * @param id
	 * @param imagenUrl
	 * @param nombre
	 * @param precio
	 * @param tipo
	 * @param talle
	 * @param estilo
	 * @param genero
	 * @param urlPaginaOrigen
	 * @param nombrePaginaOrigen
	 */
	public Ropa(String id, String imagenUrl, String nombre, BigDecimal precio, String marca, String tipo, String estilo,
			String genero, String urlPaginaOrigen, String nombrePaginaOrigen) {
		super();
		this.id = id;
		this.imagenUrl = imagenUrl;
		this.nombre = nombre;
		this.precio = precio;
		this.marca = marca;
		this.tipo = tipo;
		this.estilo = estilo;
		this.genero = genero;
		this.urlPaginaOrigen = urlPaginaOrigen;
		this.nombrePaginaOrigen = nombrePaginaOrigen;
	}
	
	/**
	 * Constructor con los campos minimos
	 * @param imagenUrl
	 * @param nombre
	 * @param precio
	 * @param tipo
	 * @param talle
	 * @param estilo
	 * @param genero
	 * @param urlPaginaOrigen
	 * @param nombrePaginaOrigen
	 */
	public Ropa(String imagenUrl, String nombre, BigDecimal precio, String marca, String tipo, String estilo,
			String genero, String urlPaginaOrigen, String nombrePaginaOrigen) {
		super();
		this.imagenUrl = imagenUrl;
		this.nombre = nombre;
		this.precio = precio;
		this.marca = marca;
		this.tipo = tipo;
		this.estilo = estilo;
		this.genero = genero;
		this.urlPaginaOrigen = urlPaginaOrigen;
		this.nombrePaginaOrigen = nombrePaginaOrigen;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImagenUrl() {
		return imagenUrl;
	}

	public void setImagenUrl(String imagenUrl) {
		this.imagenUrl = imagenUrl;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getEstilo() {
		return estilo;
	}

	public void setEstilo(String estilo) {
		this.estilo = estilo;
	}

	public String getGenero() {
		return genero;
	}

	public void setGenero(String genero) {
		this.genero = genero;
	}

	public String getUrlPaginaOrigen() {
		return urlPaginaOrigen;
	}

	public void setUrlPaginaOrigen(String urlPaginaOrigen) {
		this.urlPaginaOrigen = urlPaginaOrigen;
	}

	public String getNombrePaginaOrigen() {
		return nombrePaginaOrigen;
	}

	public void setNombrePaginaOrigen(String nombrePaginaOrigen) {
		this.nombrePaginaOrigen = nombrePaginaOrigen;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	@Override
    public String toString() {
        return String.format(
                "Ropa[id=%s, imagenUrl='%s', nombre='%s', precio='%s', marca='%s', tipo='%s'"
                + ", estilo='%s', genero='%s', urlPaginaOrigen='%s', nombrePaginaOrigen='%s']",
                id, imagenUrl, nombre, precio, marca, tipo, estilo, genero, urlPaginaOrigen, nombrePaginaOrigen);
    }

}
