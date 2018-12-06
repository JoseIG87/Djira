package com.djira.ProyectoDjira.Service;

import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

public interface MarcasZapatillasService {
	
	String obtenerMarcaSegunAlias (String alias) throws ServiceException;
	
	String obtenerMarcaSegunAliasSimilar (String alias) throws ServiceException;

}
