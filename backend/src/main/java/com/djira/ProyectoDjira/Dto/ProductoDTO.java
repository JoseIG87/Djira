package com.djira.ProyectoDjira.Dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ProductoDTO implements Serializable {
	private static final long serialVersionUID = 8530865930636721612L;
	
	private String imagenProductoUrl;
	private String nombreProducto;
	private BigDecimal precioProducto;
	private String paginaOrigen;
	private String nombreOrigen;
	
	public ProductoDTO() {
		super();
	}

	public ProductoDTO(String imagenProductoUrl, String nombreProducto, BigDecimal precioProducto, String paginaOrigen, String nombreOrigen) {
		super();
		this.imagenProductoUrl = imagenProductoUrl;
		this.nombreProducto = nombreProducto;
		this.precioProducto = precioProducto;
		this.paginaOrigen = paginaOrigen;
		this.nombreOrigen = nombreOrigen;
	}
	
	public String getImagenProductoUrl() {
		return imagenProductoUrl;
	}
	public void setImagenProductoUrl(String imagenProductoUrl) {
		this.imagenProductoUrl = imagenProductoUrl;
	}
	public String getNombreProducto() {
		return nombreProducto;
	}
	public void setNombreProducto(String nombreProducto) {
		this.nombreProducto = nombreProducto;
	}
	public BigDecimal getPrecioProducto() {
		return precioProducto;
	}
	public void setPrecioProducto(BigDecimal precioProducto) {
		this.precioProducto = precioProducto;
	}
	public String getPaginaOrigen() {
		return paginaOrigen;
	}
	public void setPaginaOrigen(String paginaOrigen) {
		this.paginaOrigen = paginaOrigen;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getNombreOrigen() {
		return nombreOrigen;
	}
	public void setNombreOrigen(String nombreOrigen) {
		this.nombreOrigen = nombreOrigen;
	}
	
}
