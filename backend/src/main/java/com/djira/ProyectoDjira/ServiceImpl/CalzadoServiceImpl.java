package com.djira.ProyectoDjira.ServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.djira.ProyectoDjira.Domain.MarcasZapatillas;
import com.djira.ProyectoDjira.Domain.Ropa;
import com.djira.ProyectoDjira.Dto.ProductoDTO;
import com.djira.ProyectoDjira.Repository.MarcasZapatillasRepository;
import com.djira.ProyectoDjira.Repository.RopaRepository;
import com.djira.ProyectoDjira.Service.CalzadoService;
import com.djira.ProyectoDjira.Service.ScrapingService;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

@Service
public class CalzadoServiceImpl implements CalzadoService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CalzadoServiceImpl.class);
	
	@Autowired
	private MarcasZapatillasRepository marcasZapatillasRepo;
	
	@Autowired
	private ScrapingService scrapingService;
	
	@Autowired
	private RopaRepository ropaRepo;
	
	@SuppressWarnings("deprecation")
	@Override
	public List<ProductoDTO> getAllCalzadosHombre(String tipoCalzado, Integer paginaActual, 
			Integer cantidadPorPagina) throws ServiceException{
		List<ProductoDTO> respuesta = new ArrayList<ProductoDTO>();
		Pageable pageable = new PageRequest(paginaActual - 1, cantidadPorPagina);
		List<Ropa> listaCalzadoPaginado = ropaRepo.findByTipo(tipoCalzado,pageable).getContent();
		for(Ropa ropa : listaCalzadoPaginado) {
			ProductoDTO rta = new ProductoDTO();
			rta.setImagenProductoUrl(ropa.getImagenUrl());
			rta.setNombreOrigen(ropa.getNombrePaginaOrigen());
			rta.setNombreProducto(ropa.getNombre());
			rta.setPaginaOrigen(ropa.getUrlPaginaOrigen());
			rta.setPrecioProducto(ropa.getPrecio());
			respuesta.add(rta);
		}
		return respuesta;
	}
	
	@Override
	public Integer obtenerCantidadDeProductosPorTipo(String tipo) throws ServiceException {
		List<Ropa> listaCalzado = ropaRepo.findByTipo(tipo);
		if(!listaCalzado.isEmpty()) {
			return listaCalzado.size();
		}else {
			return 0;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public List<ProductoDTO> getAllCalzadoHombreWithFilter(String tipoCalzado, Integer paginaActual, 
			Integer cantidadPorPagina, BigDecimal precioMin, BigDecimal precioMax,
			List<String> marcasFiltro) throws ServiceException {
		List<ProductoDTO> respuesta = new ArrayList<ProductoDTO>();
		Pageable pageable = new PageRequest(paginaActual - 1, cantidadPorPagina);
		List<Ropa> listaCalzadoPaginado = ropaRepo.findByPrecioBetweenAndTipo(precioMin, precioMax, tipoCalzado, 
				marcasFiltro, pageable);
		for(Ropa ropa : listaCalzadoPaginado) {
			ProductoDTO rta = new ProductoDTO();
			rta.setImagenProductoUrl(ropa.getImagenUrl());
			rta.setNombreOrigen(ropa.getNombrePaginaOrigen());
			rta.setNombreProducto(ropa.getNombre());
			rta.setPaginaOrigen(ropa.getUrlPaginaOrigen());
			rta.setPrecioProducto(ropa.getPrecio());
			respuesta.add(rta);
		}
		return respuesta;
	}
	
	@Override
	public Integer obtenerCantidadDeProductosPorTipoConFiltros(String tipo, BigDecimal precioMin, BigDecimal precioMax,
			List<String> marcasFiltro) throws ServiceException {
		List<Ropa> respuesta = new ArrayList<Ropa>();
		respuesta = ropaRepo.findByPrecioBetweenAndTipo(precioMin, precioMax, tipo, marcasFiltro);
		if(!respuesta.isEmpty()) {
			return respuesta.size();
		}else {
			return 0;
		}
	}
	
	/**
	 * Metodo para guardar en bbdd todas las zapatillas masculinas urbanas
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public void cargarZapatillasUrbanasHombreEnCloud() throws ServiceException {
		LOG.info("Inicia búsqueda de zapatillas urbanas masculinas");
		List<Ropa> calzadoGuardar = new ArrayList<Ropa>();
		List<Ropa> calzadoEditar = new ArrayList<Ropa>();
		
		calzadoEditar.addAll(scrapingService.obtenerProductosDafiti("masculino/calzado/urbanas/?page=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dafiti, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("moda/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page=", "zapatillas"));
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("skate/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page=", "zapatillas"));
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("surf/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Netshoes, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosOpensports("hombre/zapatillas/moda.html?p=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Open Sports, total de productos: " + calzadoEditar.size());
			
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosReebok("zapatillas-lifestyle-hombre?sz=12&start=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Reebok, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosAdidas("calzado-lifestyle-hombre?sz=48&start=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Adidas, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosRedSport("calzado/zapatillas/Hombre?PS=12&map=c,c,specificationFilter_21&O=OrderByTopSaleDESC#", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Red Sport, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosStockCenter("calzados/zapatillas/Hombre?PS=12&map=c,c,specificationFilter_11&O=OrderByTopSaleDESC#", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en StockCenter, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosVcp("collections/calzado-zapatillas/zapatillas?page=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Vcp, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosFotter("zapatos/zapatos-hombres/zapatillas.html?p=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Fotter, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosDexter("calzados/zapatillas/Hombre/Zapatillas/Moda?PS=12&map=c,c,specificationFilter_11,specificationFilter_19,specificationFilter_15&O=OrderByTopSaleDESC#", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dexter, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosLocalsOnly("collections/zapatillas?page=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Localsonly, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerZapatillasMercadoLibre("urbanas/hombre/_Desde_", "zapatillas", "_ItemTypeID_N_Tienda_all"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Localsonly, total de productos: " + calzadoEditar.size());
		
		calzadoGuardar.sort(Comparator.comparing(Ropa::getPrecio));
		ropaRepo.saveAll(calzadoGuardar);
		LOG.info("Finaliza busqueda de zapatillas urbanas masculinas, total de productos: " + calzadoGuardar.size());
	
	}
	
	/**
	 * Metodo para guardar en bbdd todas las zapatillas masculinas deportivas
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public void cargarZapatillasDeportivasHombreEnCloud() throws ServiceException {
		LOG.info("Inicia búsqueda de zapatillas deportivas masculinas");
		List<Ropa> calzadoGuardar = new ArrayList<Ropa>();
		List<Ropa> calzadoEditar = new ArrayList<Ropa>();
		
		calzadoEditar.addAll(scrapingService.obtenerProductosDafiti("masculino/calzado/deportivas/?page=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dafiti, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("basquet/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page=", "zapatillas"));
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("golf/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page=", "zapatillas"));
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("handball/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page=", "zapatillas"));
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("hockey/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page=", "zapatillas"));
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("running/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page=", "zapatillas"));
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("tenis--squash-y-paddle/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page=", "zapatillas"));
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("training/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page=", "zapatillas"));
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("voley/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Netshoes, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosOpensports("hombre/zapatillas/running.html?p=", "zapatillas"));
		calzadoEditar.addAll(scrapingService.obtenerProductosOpensports("hombre/zapatillas/basquet.html?p=", "zapatillas"));
		calzadoEditar.addAll(scrapingService.obtenerProductosOpensports("hombre/zapatillas/tenis.html?p=", "zapatillas"));
		calzadoEditar.addAll(scrapingService.obtenerProductosOpensports("hombre/zapatillas/training.html?p=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Open Sports, total de productos: " + calzadoEditar.size());
			
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosReebok("zapatillas-crossfit%7Crunning%7Cfitness_training-hombre?sz=12&start=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Reebok, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosAdidas("calzado-zapatillas-running%7Ctraining%7Cbasquet%7Ctenis%7Chockey%7Cgolf1%7Chandball%7Cvolleyball-hombre?start=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Adidas, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosDexter("calzados/zapatillas/Hombre/Zapatillas/Moda?PS=12&map=c,c,specificationFilter_11,specificationFilter_19,specificationFilter_15&O=OrderByTopSaleDESC#", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasZapatillasRepo.findByNombre(zapa.getMarca()) == null) {
        		MarcasZapatillas marcaZapa = new MarcasZapatillas(zapa.getMarca());
				marcasZapatillasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()) == null) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dexter, total de productos: " + calzadoEditar.size());
		
		calzadoGuardar.sort(Comparator.comparing(Ropa::getPrecio));
		ropaRepo.saveAll(calzadoGuardar);
		LOG.info("Finaliza busqueda de zapatillas deportivas masculinas, total de productos: " + calzadoGuardar.size());
	
	}
	
	@Override
	public List<MarcasZapatillas> getAllMarcasZapatillas() throws ServiceException {
		return this.marcasZapatillasRepo.findAll();
	}

}
