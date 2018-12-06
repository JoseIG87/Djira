package com.djira.ProyectoDjira.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.djira.ProyectoDjira.Domain.MarcasZapatillas;
import com.djira.ProyectoDjira.Dto.MarcasDTO;
import com.djira.ProyectoDjira.Dto.ProductoDTO;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

public interface CalzadoService {
	
	void cargarZapatillasUrbanasHombreEnCloud() throws ServiceException, IOException;
	
	List<ProductoDTO> getAllCalzadosHombre(String tipoCalzado, Integer paginaActual, 
			Integer cantidadPorPagina) throws ServiceException;
	
	List<MarcasZapatillas> getAllMarcasZapatillas() throws ServiceException;
	
	Integer obtenerCantidadDeProductosPorTipo(String tipo) throws ServiceException;
	
	List<ProductoDTO> getAllCalzadoHombreWithFilter(String tipoCalzado, Integer paginaActual, 
			Integer cantidadPorPagina, BigDecimal precioMin, BigDecimal precioMax, List<MarcasDTO> marcas) throws ServiceException;
	
	Integer obtenerCantidadDeProductosPorTipoConFiltros(String tipo, BigDecimal precioMin, BigDecimal precioMax, List<MarcasDTO> marcas) throws ServiceException;
}
