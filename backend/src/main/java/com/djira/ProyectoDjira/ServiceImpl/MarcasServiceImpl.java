package com.djira.ProyectoDjira.ServiceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.djira.ProyectoDjira.Domain.Marcas;
import com.djira.ProyectoDjira.Repository.MarcasRepository;
import com.djira.ProyectoDjira.Service.MarcasService;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

@Service
public class MarcasServiceImpl implements MarcasService {
	
	@Autowired
	private MarcasRepository marcasRepo;
	
	@Override
	public String obtenerMarca(String marca) throws ServiceException {
		switch (marca.toLowerCase()) {
			case "adidas": return "Adidas";
			case "volcom": return "Volcom";
			case "vans": return "Vans";
			case "puma": return "Puma";
			case "fila": return "Fila";
			case "nike": return "Nike";
			case "converse": return "Converse";
			case "topper": return "Topper";
			case "gola": return "Gola";
			case "element": return "Element";
			case "reebok": return "Reebok";
			case "asics": return "Asics";
			case "wrangler": return "Wrangler";
			case "boating": return "Boating";
			case "mormaii": return "Mormaii";
			case "airwalk": return "Airwalk";
			case "lacoste": return "Lacoste";
			case "reef": return "Reef";
			case "montagne": return "Montagne";
			case "farenheite": return "Farenheite";
			case "fallen": return "Fallen";
			case "democrata": return "Democrata";
			case "skechers": return "Skechers";
			case "pony": return "Pony";
			case "dekline": return "Dekline";
			case "zonazero": return "Zona Zero";
			case "penalty": return "Penalty";
			case "lotto": return "Lotto";
			case "bross": return "Bross";
			case "cat": return "Caterpillar";
			case "caterpillar": return "Caterpillar";
			case "timberland": return "Timberland";
			case "scarpino": return "Scarpino";
			case "batistella": return "Batistella";
			case "flecha": return "Flecha";
			case "everlast": return "Everlast";
			case "rusty": return "Rusty";
			case "sismo": return "Sismo";
			case "prince": return "Prince";
			case "oakley": return "Oakley";
			case "quiksilver": return "Quiksilver";
			case "superga": return "Superga";
			case "zurich": return "Zurich";
			case "joma": return "Joma";
			case "prototype": return "Prototype";
			case "iguana": return "Iguana";
			case "jaguar": return "Jaguar";
			case "golfeet": return "Golfeet";
	        default: return null;
		}
	}
	
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
	        case "coq": return "Le Coq Sportif";
	        case "cross":  return "Cross Creek";
	        case "daniel":  return "Daniel Hechter";
	        case "dc": return "DC Shoes";
	        case "dcshoes": return "DC Shoes";
	        case "felipe": return "Felipe Quinto";
	        case "fight": return "Fight For Your Right";
	        case "franco": return "Franco Pasotti";
	        case "going":  return "Going up";
			case "good":  return "Good Year";
			case "helly":  return "Helly Hansen";
			case "hi": return "Hi teco";
			case "hoka": return "Hoka One One";
			case "huf": return "Huf";
			case "hush": return "Hush Puppies";
			case "j": return "John Foos";
			case "john": return "John Foos";
			case "la": return "multipleResult";
			case "l.a": return "multipleResult";
			case "l.a.": return "multipleResult";
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
			case "rip":  return "Rip Curl";
			case "ruta":  return "Ruta 21";
			case "sergio":  return "Sergio Tacchini";
			case "spiral": return "Spiral Shoes";
			case "sr": return "Sr Gatica";
			case "star": return "Star Tech";
			case "stork":  return "Stork Man";
			case "superga": return "Superga";
			case "this":  return "This Week";
			case "topo":  return "Topo Athletic";
			case "ultimate":  return "Ultimate Performance";
			case "university":  return "University Club";
			case "under":  return "Under Armour";
			case "urban":  return "Urban FIT Shoes";
			case "vibram": return "Vibram Fivefingers";
			case "vuela": return "Vuela Alto";
			case "zona": return "Zona Zero";
	        default: return alias;
		}
		
	}

	@Override
	public String obtenerMarcaSegunAliasSimilar(String alias) throws ServiceException {
		switch (alias.toLowerCase()) {
	        case "la gear":  return "LA Gear";
	        case "l.a gear":  return "LA Gear";
	        case "l.a. gear":  return "LA Gear";
	        case "la sportiva":  return "La Sportiva";
	        case "las tabas":  return "Las Tabas";
	        case "las nornas":  return "Las Nornas";
	        case "le coq":  return "Le Coq Sportif";
	        case "le utopik":  return "Le Utopik";
	        default: return alias;
		}
	}

	@Override
	public List<String> obtenerTodasLasMarcasPorTipo(String tipo) throws ServiceException {
		List<Marcas> marcas = new ArrayList<Marcas>();
		List<String> rta = new ArrayList<String>();
		
		marcas = marcasRepo.findByTipo(tipo);
		
		for(Marcas marca : marcas) {
			rta.add(marca.getNombre());
		}
		return rta;
	}

}
