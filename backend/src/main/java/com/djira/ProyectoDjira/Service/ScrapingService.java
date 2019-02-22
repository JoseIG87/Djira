package com.djira.ProyectoDjira.Service;

import java.util.List;

import com.djira.ProyectoDjira.Domain.Ropa;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

public interface ScrapingService {
	
	List<Ropa> obtenerProductosDafiti(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosNetshoes(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosDexter(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosOpensports(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosReebok(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosAdidas(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosStone(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosRedSport(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosStockCenter(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosSportline(String path, String path2) throws ServiceException;
	
	List<Ropa> obtenerProductosMercadoLibre(String path, String tipoSingular, String tipoPlural, String filter) throws ServiceException;
	
	List<Ropa> obtenerProductosVcp(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosLocalsOnly(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosValkymia(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosRingo(String path, String tipo) throws ServiceException;
	
	List<Ropa> obtenerProductosBorsalino(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosDashGridMark(String path, String pagina) throws ServiceException;
	
	List<Ropa> obtenerProductosVicus(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosPanther(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosDorian(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosGuante(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosTheNetBoutique(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosTejano(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosC1rca(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosAndez(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosRevolutionss(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosSporting(String path1, String path2) throws ServiceException;
	
	List<Ropa> obtenerProductosSolodeportes(String path1, String path2) throws ServiceException;
	
	List<Ropa> obtenerProductosLaferiadelcalzado(String path) throws ServiceException;
	
	List<Ropa> obtenerProductosOggi(String path) throws ServiceException;
	
}
