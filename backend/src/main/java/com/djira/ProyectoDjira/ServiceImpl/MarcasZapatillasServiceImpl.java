package com.djira.ProyectoDjira.ServiceImpl;

import org.springframework.stereotype.Service;

import com.djira.ProyectoDjira.Service.MarcasZapatillasService;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

@Service
public class MarcasZapatillasServiceImpl implements MarcasZapatillasService {
	
	@Override
	public String obtenerMarcaSegunAlias(String alias) throws ServiceException {
		
		switch (alias.toLowerCase()) {
	        case "47":  return "47 Street";
	        case "a":  return "A Nation";
	        case "all":  return "All Terra";
	        case "boca":  return "Boca Juniors";
	        case "call":  return "Call It Spring";
	        case "chelsea":  return "Chelsea Market";
	        case "coca":  return "Coca Cola Shoes";
	        case "columbia":  return "Columbia Sportwear";
	        case "cross":  return "Cross Creek";
	        case "dc": return "DC Shoes";
	        case "felipe": return "Felipe Quinto";
	        case "fight": return "Fight For Your Right";
	        case "going":  return "Going up";
			case "good":  return "Good Year";
			case "helly":  return "Helly Hansen";
			case "hi": return "Hi teco";
			case "hoka": return "Hoka One One";
			case "hush": return "Hush Puppies";
			case "john": return "John Foos";
			case "la": return "multipleResult";
			case "las": return "multipleResult";
			case "latin": return "Latin Shoes";
			case "le": return "multipleResult";
			case "malcom": return "Malcom Calzados";
			case "mocassino": return "Mocassino S.A.";
			case "new": return "New Balance";
			case "pato":  return "Pato Pampa";
			case "punto":  return "Punto Limite";
			case "reset":  return "Reset Sport";
			case "rg":  return "RG GoalKeeper Gloves";
			case "ruta":  return "Ruta 21";
			case "sergio":  return "Sergio Tacchini";
			case "spiral": return "Spiral Shoes";
			case "sr": return "Sr Gatica";
			case "star": return "Star Tech";
			case "stork":  return "Stork Man";
			case "this":  return "This Week";
			case "topo":  return "Topo Athletic";
			case "ultimate":  return "Ultimate Performance";
			case "university":  return "University Club";
			case "under":  return "Under Armour";
			case "urban":  return "Urban FIT Shoes";
			case "vibram": return "Vibram Fivefingers";
			case "vuela": return "Vuela Alto";
	        default: return alias;
		}
		
	}

	@Override
	public String obtenerMarcaSegunAliasSimilar(String alias) throws ServiceException {
		switch (alias.toLowerCase()) {
	        case "la gear":  return "LA Gear";
	        case "la sportiva":  return "La Sportiva";
	        case "las tabas":  return "Las Tabas";
	        case "las nornas":  return "Las Nornas";
	        case "le coq":  return "Le Coq Sportif";
	        case "le utopik":  return "Le Utopik";
	        default: return alias;
		}
	}

}
