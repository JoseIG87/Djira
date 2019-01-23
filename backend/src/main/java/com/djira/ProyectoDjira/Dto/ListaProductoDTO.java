package com.djira.ProyectoDjira.Dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class ListaProductoDTO implements Serializable {
	private static final long serialVersionUID = 4666254840289438086L;
	
	private List<ProductoDTO> listaProductos;
	private List<String> listaMarcas;
	private double cantidadPaginas;
	private BigDecimal minPrice;
	private BigDecimal maxPrice;
	
	public ListaProductoDTO(List<ProductoDTO> listaProductos, List<String> listaMarcas, double cantidadPaginas) {
		super();
		this.listaProductos = listaProductos;
		this.listaMarcas = listaMarcas;
		this.cantidadPaginas = cantidadPaginas;
	}

	public ListaProductoDTO() {}

	public List<ProductoDTO> getListaProductos() {
		return listaProductos;
	}

	public void setListaProductos(List<ProductoDTO> listaProductos) {
		this.listaProductos = listaProductos;
	}

	public double getCantidadPaginas() {
		return cantidadPaginas;
	}

	public void setCantidadPaginas(double cantidadPaginas) {
		this.cantidadPaginas = cantidadPaginas;
	}

	public BigDecimal getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(BigDecimal minPrice) {
		this.minPrice = minPrice;
	}

	public BigDecimal getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(BigDecimal maxPrice) {
		this.maxPrice = maxPrice;
	}

	public List<String> getListaMarcas() {
		return listaMarcas;
	}

	public void setListaMarcas(List<String> listaMarcas) {
		this.listaMarcas = listaMarcas;
	}
	
}
