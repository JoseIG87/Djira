package com.djira.ProyectoDjira.ServiceImpl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.djira.ProyectoDjira.Domain.Marcas;
import com.djira.ProyectoDjira.Domain.Ropa;
import com.djira.ProyectoDjira.Dto.MarcasDTO;
import com.djira.ProyectoDjira.Dto.ProductoDTO;
import com.djira.ProyectoDjira.Repository.MarcasRepository;
import com.djira.ProyectoDjira.Repository.RopaRepository;
import com.djira.ProyectoDjira.Service.CalzadoService;
import com.djira.ProyectoDjira.Service.ScrapingService;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

@Service
public class CalzadoServiceImpl implements CalzadoService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CalzadoServiceImpl.class);
	
	@Autowired
	private MarcasRepository marcasRepo;
	
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
		
		/*calzadoEditar.addAll(scrapingService.obtenerProductosDafiti("masculino/calzado/urbanas/?page="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dafiti, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("moda/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page="));
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("skate/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page="));
		calzadoEditar.addAll(scrapingService.obtenerProductosNetshoes("surf/zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Netshoes, total de productos: " + calzadoEditar.size());
		
		/*calzadoEditar.addAll(scrapingService.obtenerProductosDexter("fq=C%3a%2f1000005%2f1000009%2f&fq=specificationFilter_11%3aHombre&fq=specificationFilter_15%3aModa&O=OrderByReleaseDateDESC&PS=12&sl=ae9ea699-1a41-4881-b761-d23b759700d5&cc=4&sm=0&PageNumber="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dexter, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosOpensports("hombre/zapatillas/moda.html?p=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
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
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Reebok, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosAdidas("calzado-lifestyle-hombre?sz=48&start="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Adidas, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosRedSport("fq=C%3a%2f12%2f11%2f&fq=specificationFilter_21%3aHombre&O=OrderByTopSaleDESC&PS=12&sl=dca78f11-a756-4570-94b4-7c2a84fe05d4&cc=12&sm=0&PageNumber="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Red Sport, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosStockCenter("calzados/zapatillas/Hombre?PS=12&map=c,c,specificationFilter_11&O=OrderByTopSaleDESC#"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en StockCenter, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosSportline("fq=C:/1000006/1000013/1000167/&PageNumber=","&fq=specificationFilter_7:MODA&O=OrderByTopSaleDESC&sl=7808f6ed-c896-4a54-9283-fecabc3565de&PS=24&cc=24&sm=0"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en StockCenter, total de productos: " + calzadoEditar.size());
		
		/*calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosVcp("collections/calzado-zapatillas/zapatillas?page="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Vcp, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosLocalsOnly("collections/zapatillas?page="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Localsonly, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosMercadoLibre("urbanas/hombre/_Desde_","zapatilla", "zapatillas", "_ItemTypeID_N_Tienda_all"));
		calzadoEditar.addAll(scrapingService.obtenerProductosMercadoLibre("skate/hombre/_Desde_","zapatilla", "zapatillas", "_ItemTypeID_N_Tienda_all"));
		calzadoEditar.addAll(scrapingService.obtenerProductosMercadoLibre("botitas/hombre/_Desde_","zapatilla", "zapatillas", "_ItemTypeID_N_Tienda_all"));
		calzadoEditar.addAll(scrapingService.obtenerProductosMercadoLibre("nauticas/hombre/_Desde_","zapatilla", "zapatillas", "_ItemTypeID_N_Tienda_all"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en MercadoLibre, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosValkymia("calzados.html", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Valkymia, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosRingo("t/categorias/urbanos/zapatillas", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Ringo, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Moda?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "grid"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Grid, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Moda?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "dash"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dash, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Moda?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "mark"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Mark, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosVicus("botas-mid_qO25508246"));
		calzadoEditar.addAll(scrapingService.obtenerProductosVicus("clasicas_qO30156414"));
		calzadoEditar.addAll(scrapingService.obtenerProductosVicus("genesis_qO30173160"));
		calzadoEditar.addAll(scrapingService.obtenerProductosVicus("folk_qO30172504"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Vicus, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosPanther("calzado/zapatillas.html?p="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Panther, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosGuante("categoria-producto/tipo-de-calzado/zapatillas/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Guante, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosTheNetBoutique("hombres/catalogo/zapatos/zapatillas?pageindex="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en The Net Boutique, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosTejano("hombres/calzado.html?p="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Tejano, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosC1rca("168-zapatillas?page="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en C1rca, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosAndez("categoria-producto/zapatillas/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en C1rca, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosRevolutionss("fq=C%3a%2f10%2f&fq=specificationFilter_21%3aZapatillas&fq=specificationFilter_20%3aModa&PS=30&sl=fcce2c9b-ec20-4c1b-969b-17f981adc78f&cc=30&sm=0&PageNumber="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Revolution, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosSporting("fq=C%3a%2f1%2f21%2f&PS=48&sl=eac6ad06-e34a-44b8-9b86-919b7ab53129&cc=4&sm=0&PageNumber=", "&fq=specificationFilter_32:MODA"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Sporting, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosSolodeportes("hombre/calzado.html?modelo=Urban&p=", "&tipo_de_calzado=Zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Solodeportes, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosLaferiadelcalzado("hombre/zapatillas-urbana/?limit=20&page="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en La feria del calzado, total de productos: " + calzadoEditar.size());*/
		
		//calzadoEditar.addAll(scrapingService.obtenerProductosDigitalsport("sport78", "?category=1%7C25&gender=1%7C4&discipline=14&page="));
		//calzadoEditar.addAll(scrapingService.obtenerProductosDigitalsport("fluid", "?category=1%7C25&gender=1%7C4&discipline=14&page="));
		//calzadoEditar.addAll(scrapingService.obtenerProductosDigitalsport("dionysos", "?category=1%7C25&gender=1%7C4&page="));
		calzadoEditar.addAll(scrapingService.obtenerProductosDigitalsport("blast", "?category=1%7C25&gender=1%7C4&page="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("urbana");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Digital sport, total de productos: " + calzadoEditar.size());

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
		
		/*calzadoEditar.addAll(scrapingService.obtenerProductosDafiti("masculino/calzado/deportivas/?page=", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
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
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
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
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
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
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
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
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Adidas, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosMercadoLibre("running/hombre/_Desde_","zapatilla", "zapatillas", "_ItemTypeID_N_Tienda_all"));
		calzadoEditar.addAll(scrapingService.obtenerProductosMercadoLibre("basquet/hombre/_Desde_","zapatilla", "zapatillas", "_ItemTypeID_N_Tienda_all"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en MercadoLibre, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosRingo("t/categorias/active/man", "zapatillas"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Ringo, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Running?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "grid"));
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Entrenamiento?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "grid"));
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Basket?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "grid"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Grid, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Basket?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "dash"));
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Entrenamiento?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "dash"));
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Hockey?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "dash"));
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Running?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "dash"));
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Tenis?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "dash"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dash, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Basket?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "mark"));
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Entrenamiento?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "mark"));
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Hockey?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "mark"));
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Running?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "mark"));
		calzadoEditar.addAll(scrapingService.obtenerProductosDashGridMark("calzado/zapatillas/Hombre/Tenis?O=OrderByReleaseDateDESC&PS=30&map=c,c,specificationFilter_23,specificationFilter_24", "mark"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Mark, total de productos: " + calzadoEditar.size());*/
		
		calzadoEditar.addAll(scrapingService.obtenerProductosVicus("deportivas_qO27070488"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("deportiva");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Vicus, total de productos: " + calzadoEditar.size());
		
		calzadoGuardar.sort(Comparator.comparing(Ropa::getPrecio));
		ropaRepo.saveAll(calzadoGuardar);
		LOG.info("Finaliza busqueda de zapatillas deportivas masculinas, total de productos: " + calzadoGuardar.size());
	
	}
	
	/**
	 * Metodo para guardar en bbdd todas las zapatillas masculinas de vestir
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public void cargarZapatillasDeVestirHombreEnCloud() throws ServiceException {
		LOG.info("Inicia búsqueda de zapatillas de vestir masculinas");
		List<Ropa> calzadoGuardar = new ArrayList<Ropa>();
		List<Ropa> calzadoEditar = new ArrayList<Ropa>();
		
		/*calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosStone("hombre/urban/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("vestir");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
	    		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
	    	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Stone, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosOggi("zapatos/zapatillas/?limit=12&page="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("vestir");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
	    		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
	    	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Stone, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosDorian("zapatillas/?limit=16&page=", "&results_only=true&uid=1552415021206"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("vestir");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dorian, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosJaquealrey("zapatillas/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("vestir");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Jaquealrey, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosPuertoBlue("zapatillas/?limit=12&page=", "&results_only=true&uid=1552655899419"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("vestir");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Puerto Blue, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosCzaro("men/zapatillas/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("vestir");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Czaro, total de productos: " + calzadoEditar.size());*/
		
		calzadoEditar.addAll(scrapingService.obtenerProductosFedericomarconcini("zapatillas/?limit=16&page=", "&results_only=true&uid=1552915509919"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapatilla");
			zapa.setEstilo("vestir");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapatilla") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapatilla");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Federico Marconcini, total de productos: " + calzadoEditar.size());
		
		calzadoGuardar.sort(Comparator.comparing(Ropa::getPrecio));
		ropaRepo.saveAll(calzadoGuardar);
		LOG.info("Finaliza busqueda de zapatillas de vestir masculinas, total de productos: " + calzadoGuardar.size());
	}
	
	/**
	 * Metodo para guardar en bbdd todos los zapatos masculinos
	 * @return
	 * @throws ServiceException
	 * @throws ParseException 
	 */
	@Override
	public void cargarZapatosEnCloud() throws ServiceException, ParseException {
		LOG.info("Inicia búsqueda de zapatos masculinos");
		List<Ropa> calzadoGuardar = new ArrayList<Ropa>();
		List<Ropa> calzadoEditar = new ArrayList<Ropa>();
		
		/*calzadoEditar.addAll(scrapingService.obtenerProductosDafiti("masculino/calzado/zapatos/?page=", "zapatos"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dafiti, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosRingo("t/categorias/urbanos/zapatos", "zapatos"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Ringo, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosMercadoLibre("hombre/_Desde_","zapato", "zapatos", "_ItemTypeID_N_Tienda_all"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en MercadoLibre, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosBorsalino("categoria-producto/hombre/vestir/page/"));
		calzadoEditar.addAll(scrapingService.obtenerProductosBorsalino("categoria-producto/hombre/acordonado-hombre/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Borsalino, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosPanther("calzado/zapatos.html?p="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Panther, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosColantuono("zapatos/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Colantuono, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosDorian("zapatos/?limit=16&page=", "&results_only=true&uid=1552412448153"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dorian, total de productos: " + calzadoEditar.size());
		
		/*calzadoEditar.addAll(scrapingService.obtenerProductosGuante("categoria-producto/tipo-de-calzado/zapatos/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Guante, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosGiardini("zapatos/clasicos/?limit=12&page=", "&results_only=true&uid=1552420202445"));
		calzadoEditar.addAll(scrapingService.obtenerProductosGiardini("zapatos/color/?limit=12&page=", "&results_only=true&uid=1552485428536"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Giardini, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosItaloCalzados("productos/?limit=20&page=", "&results_only=true&uid=1552485767247"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Italo Calzados, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosJaquealrey("zapatos/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Jaquealrey, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosPuertoBlue("zapatos/?limit=12&page=", "&results_only=true&uid=1552655899419"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Puerto Blue, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosPriamoitaly("zapatos/?limit=12&page=", "&results_only=true&uid=1552658813986"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Priamo italy, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosAsttonBuenosAires("zapatos-hombre/?limit=12&page=", "&results_only=true&uid=1552664032329"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Los Blanco, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosLaferiadelcalzado("hombre/zapatos-de-vestir/?limit=20&page="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en La feria del calzado, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosCzaro("men/zapatos/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Czaro, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosFedericomarconcini("zapatos/?limit=16&page=", "&results_only=true&uid=1552677296553"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("zapato");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "zapato") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "zapato");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Federico Marconcini, total de productos: " + calzadoEditar.size());*/
		
		calzadoGuardar.sort(Comparator.comparing(Ropa::getPrecio));
		ropaRepo.saveAll(calzadoGuardar);
		LOG.info("Finaliza busqueda de zapatos masculinos, total de productos: " + calzadoGuardar.size());
	
	}
	
	/**
	 * Metodo para guardar en bbdd todos los zapatos masculinos
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public void cargarMocasinesEnCloud() throws ServiceException {
		LOG.info("Inicia búsqueda de mocasines");
		List<Ropa> calzadoGuardar = new ArrayList<Ropa>();
		List<Ropa> calzadoEditar = new ArrayList<Ropa>();
		
		/*calzadoEditar.addAll(scrapingService.obtenerProductosDafiti("masculino/calzado/mocasines-masculino/?page="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("mocasin");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "mocasin") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "mocasin");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dafiti, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosRingo("t/categorias/formal", "mocasines"));
		calzadoEditar.addAll(scrapingService.obtenerProductosRingo("t/categorias/nauticos", "mocasines"));
		calzadoEditar.addAll(scrapingService.obtenerProductosRingo("t/categorias/casual", "mocasines"));
		calzadoEditar.addAll(scrapingService.obtenerProductosRingo("t/categorias/flex", "mocasines"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("mocasin");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "mocasin") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "mocasin");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Ringo, total de productos: " + calzadoEditar.size());
		
		calzadoEditar = new ArrayList<Ropa>();
		calzadoEditar.addAll(scrapingService.obtenerProductosMercadoLibre("hombre/_Desde_","mocasin", "mocasines", "_ItemTypeID_N_Tienda_all"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("mocasin");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "mocasin") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "mocasin");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en MercadoLibre, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosDorian("mocasines/?limit=16&page=", "&results_only=true&uid=1552418627412"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("mocasin");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "mocasin") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "mocasin");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Dorian, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosBorsalino("categoria-producto/hombre/mocasin/page/"));
		calzadoEditar.addAll(scrapingService.obtenerProductosBorsalino("categoria-producto/hombre/nautico-hombre/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("mocasin");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "mocasin") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "mocasin");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Borsalino, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosGuante("categoria-producto/tipo-de-calzado/mocasine/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("mocasin");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "mocasin") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "mocasin");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Guante, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosGiardini("zapatos/nauticos/?limit=12&page=", "&results_only=true&uid=1552485502839"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("mocasin");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "mocasin") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "mocasin");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Giardini, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosJaquealrey("nauticos/page/"));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("mocasin");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "mocasin") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "mocasin");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en Jaquealrey, total de productos: " + calzadoEditar.size());
		
		calzadoEditar.addAll(scrapingService.obtenerProductosLaferiadelcalzado("hombre/nauticos/?limit=20&page="));
		for(Ropa zapa : calzadoEditar) {
			zapa.setTipo("mocasin");
			zapa.setGenero("hombre");
			if(marcasRepo.findByNombreAndTipo(zapa.getMarca(), "mocasin") == null) {
        		Marcas marcaZapa = new Marcas(zapa.getMarca(), "mocasin");
				marcasRepo.save(marcaZapa);
        	}
			if(ropaRepo.findByNombreAndNombrePaginaOrigen(zapa.getNombre(), zapa.getNombrePaginaOrigen()).isEmpty()) {
				calzadoGuardar.add(zapa);
			}
		}
		LOG.info("Finaliza busqueda en La feria del calzado, total de productos: " + calzadoEditar.size());*/
		
		calzadoGuardar.sort(Comparator.comparing(Ropa::getPrecio));
		ropaRepo.saveAll(calzadoGuardar);
		LOG.info("Finaliza busqueda de mocasines, total de productos: " + calzadoGuardar.size());
	
	}
	
	@Override
	public List<MarcasDTO> getAllMarcasZapatillas() throws ServiceException {
		
		List<Marcas> marcasModel = this.marcasRepo.findByTipo("zapatilla");
		List<MarcasDTO> marcas = new ArrayList<MarcasDTO>();
		
		for(Marcas marca : marcasModel) {
			MarcasDTO marcaDTO = new MarcasDTO();
			marcaDTO.setName(marca.getNombre());
			marcaDTO.setTipo(marca.getTipo());
		}
		
		return marcas;
	}
	

}
