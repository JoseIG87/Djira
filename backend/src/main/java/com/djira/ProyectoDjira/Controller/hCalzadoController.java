package com.djira.ProyectoDjira.Controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.djira.ProyectoDjira.Dto.ListaProductoDTO;
import com.djira.ProyectoDjira.Dto.MarcasDTO;
import com.djira.ProyectoDjira.Dto.ProductoDTO;
import com.djira.ProyectoDjira.Service.CalzadoService;
import com.djira.ProyectoDjira.Service.MarcasService;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

@RestController
@RequestMapping("/masculino/calzado/")
public class hCalzadoController extends BaseRestController{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(hCalzadoController.class);
	
	@Autowired
	private CalzadoService servicio;
	
	@Autowired
	private MarcasService marcasZapatillasService;
	
	public hCalzadoController(CalzadoService servicio, MarcasService marcasZapatillasService) {
		super();
		this.servicio = servicio;
		this.marcasZapatillasService = marcasZapatillasService;
	}
	
	@RequestMapping(value = "/zapatillas/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE) 
	public ResponseEntity<ListaProductoDTO> getAllZapatillasPaginado(@RequestParam("pagina") int page, @RequestParam("cantidad") int amount) throws ServiceException{
		ResponseEntity<ListaProductoDTO> respuesta = new ResponseEntity<ListaProductoDTO>(HttpStatus.NOT_FOUND);
		ListaProductoDTO rta = new ListaProductoDTO();
		List<String> marcas = new ArrayList<String>();
		List<MarcasDTO> marcasDTO = new ArrayList<MarcasDTO>();
		List<ProductoDTO> zapas = new ArrayList<ProductoDTO>();
		zapas = servicio.getAllCalzadosHombre("zapatilla", page, amount);	
		Integer cantidadZapas = servicio.obtenerCantidadDeProductosPorTipo("zapatilla");
		List<ProductoDTO> listaTotal = servicio.getAllCalzadosHombre("zapatilla", 1, cantidadZapas);
		
		rta.setCantidadPaginas(cantidadZapas);
		rta.setListaProductos(zapas);
		rta.setMinPrice(listaTotal.isEmpty() ? new BigDecimal(0) : listaTotal.get(0).getPrecioProducto());
		rta.setMaxPrice(listaTotal.isEmpty() ? new BigDecimal(0) : listaTotal.get(listaTotal.size()-1).getPrecioProducto());
		marcasDTO = servicio.getAllMarcasZapatillas();
		for(MarcasDTO marca : marcasDTO) {
			marcas.add(marca.getName());
		}
		rta.setListaMarcas(marcas);
		
		if(!zapas.isEmpty()) {
			respuesta = new ResponseEntity<ListaProductoDTO>(rta, HttpStatus.OK);	
		}	
		return respuesta;
	}
	
	@RequestMapping(value = "/zapatillas/all", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<ListaProductoDTO> getBetweenPrice(@RequestParam("precioMin") BigDecimal precioMin, @RequestParam("precioMax") BigDecimal precioMax, 
			@RequestParam("pagina") int page, @RequestParam("cantidad") int amount, @RequestBody List<String> marcas) throws ServiceException {
		ResponseEntity<ListaProductoDTO> respuesta = new ResponseEntity<ListaProductoDTO>(HttpStatus.NOT_FOUND);
		ListaProductoDTO rta = new ListaProductoDTO();
		List<ProductoDTO> zapas = new ArrayList<ProductoDTO>();
		
		if(marcas.isEmpty()) {
			marcas = marcasZapatillasService.obtenerTodasLasMarcasPorTipo("zapatilla");
		}
		
		zapas = servicio.getAllCalzadoHombreWithFilter("zapatilla", page, amount, precioMin, precioMax, marcas);
		Integer cantidadZapas = servicio.obtenerCantidadDeProductosPorTipoConFiltros("zapatilla", precioMin, precioMax, marcas);
		
		rta.setCantidadPaginas(cantidadZapas);
		rta.setListaProductos(zapas);
		rta.setMinPrice(precioMin);
		rta.setMaxPrice(precioMax);	
		
		if(!zapas.isEmpty()) {
			respuesta = new ResponseEntity<ListaProductoDTO>(rta, HttpStatus.OK);	
		}	
		return respuesta;
	}

}
