package com.djira.ProyectoDjira.Service;

import java.util.List;

import com.djira.ProyectoDjira.Domain.Ropa;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

public interface ScrapingService {
	
	List<Ropa> obtenerProductosDafiti(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosNetshoes(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosOpensports(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosReebok(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosAdidas(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosRedSport(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosStockCenter(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosVcp(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosLocalsOnly(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosMercadoLibre(String path, String tipoSingular, String tipoPlural, String filter) throws ServiceException;
	
	List<Ropa> obtenerProductosValkymia(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosRingo(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosBorsalino(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosDashGridMark(String path, String pagina) throws ServiceException;
	
	List<Ropa> obtenerProductosVicus(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosPanther(String path) throws ServiceException;

}
