package com.djira.ProyectoDjira.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

import com.djira.ProyectoDjira.Dto.MarcasDTO;
import com.djira.ProyectoDjira.Dto.ProductoDTO;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

public interface CalzadoService {
	
	void cargarZapatillasUrbanasHombreEnCloud() throws ServiceException;
	
	void cargarZapatillasDeportivasHombreEnCloud() throws ServiceException;
	
	void cargarZapatillasDeVestirHombreEnCloud() throws ServiceException;
	
	void cargarZapatosEnCloud() throws ServiceException, ParseException;
	
	void cargarMocasinesEnCloud() throws ServiceException;
	
	List<ProductoDTO> getAllCalzadosHombre(String tipoCalzado, Integer paginaActual, 
			Integer cantidadPorPagina) throws ServiceException;
	
	List<MarcasDTO> getAllMarcasZapatillas() throws ServiceException;
	
	Integer obtenerCantidadDeProductosPorTipo(String tipo) throws ServiceException;
	
	List<ProductoDTO> getAllCalzadoHombreWithFilter(String tipoCalzado, Integer paginaActual, 
			Integer cantidadPorPagina, BigDecimal precioMin, BigDecimal precioMax, List<String> marcas) throws ServiceException;
	
	Integer obtenerCantidadDeProductosPorTipoConFiltros(String tipo, BigDecimal precioMin, BigDecimal precioMax, List<String> marcasFiltro) throws ServiceException;
}
