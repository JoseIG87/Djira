package com.djira.ProyectoDjira.Service;

import java.util.List;

import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

public interface MarcasService {
	
	String obtenerMarcaSegunAlias (String alias) throws ServiceException;
	
	String obtenerMarcaSegunAliasSimilar (String alias) throws ServiceException;
	
	String obtenerMarca (String marca) throws ServiceException;
	
	List<String> obtenerTodasLasMarcasPorTipo(String tipo) throws ServiceException;

}
