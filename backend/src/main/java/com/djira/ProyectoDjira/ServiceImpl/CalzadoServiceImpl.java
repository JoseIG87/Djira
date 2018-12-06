package com.djira.ProyectoDjira.ServiceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.djira.ProyectoDjira.Domain.MarcasZapatillas;
import com.djira.ProyectoDjira.Domain.Ropa;
import com.djira.ProyectoDjira.Dto.MarcasDTO;
import com.djira.ProyectoDjira.Dto.PaginaDTO;
import com.djira.ProyectoDjira.Dto.ProductoDTO;
import com.djira.ProyectoDjira.Repository.MarcasZapatillasRepository;
import com.djira.ProyectoDjira.Repository.RopaRepository;
import com.djira.ProyectoDjira.Repository.Urls;
import com.djira.ProyectoDjira.Service.CalzadoService;
import com.djira.ProyectoDjira.Service.MarcasZapatillasService;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

@Service
public class CalzadoServiceImpl implements CalzadoService{
	
	private static final Logger LOG = LoggerFactory.getLogger(CalzadoServiceImpl.class);
	
	private static Urls urls;
	
	@Autowired
	private MarcasZapatillasService marcasZapatillasService;
	
	@Autowired
	private MarcasZapatillasRepository marcasZapatillasRepo;
	
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
			List<MarcasDTO> marcas) throws ServiceException {
		List<ProductoDTO> respuesta = new ArrayList<ProductoDTO>();
		Pageable pageable = new PageRequest(paginaActual - 1, cantidadPorPagina);
		MarcasDTO m = new MarcasDTO(1,"Nike");
		marcas.add(m);
		m = new MarcasDTO(2,"Adidas");
		marcas.add(m);
		m = new MarcasDTO(3,"Roi");
		marcas.add(m);
		String[] marcasFiltro = new String[marcas.size()];
		for(int i = 0; i<marcas.size(); i++) {
			marcasFiltro[i] = marcas.get(i).getName();
		}
		List<Ropa> listaCalzadoPaginado = ropaRepo.findByPrecioBetweenAndTipo(precioMin, precioMax, tipoCalzado, marcasFiltro, pageable);
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
			List<MarcasDTO> marcas) throws ServiceException {
		List<Ropa> respuesta = new ArrayList<Ropa>();
		MarcasDTO m = new MarcasDTO(1,"Nike");
		marcas.add(m);
		m = new MarcasDTO(2,"Adidas");
		marcas.add(m);
		m = new MarcasDTO(3,"Roi");
		marcas.add(m);
		String[] marcasFiltro = new String[100];
		for(int i = 0; i<marcas.size(); i++) {
			marcasFiltro[i] = marcas.get(i).getName();
		}
		respuesta = ropaRepo.findByPrecioBetweenAndTipo(precioMin, precioMax, tipo, marcasFiltro);
		if(!respuesta.isEmpty()) {
			return respuesta.size();
		}else {
			return 0;
		}
	}
	
	/**
	 * Metodo para scrapear zapatillas masculinas
	 * @return
	 * @throws ServiceException
	 * @throws IOException 
	 */
	@Override
	public void cargarZapatillasUrbanasHombreEnCloud() throws ServiceException, IOException{
		int totalTmp = 0;
		urls = new Urls();
		List<Ropa> calzadoGuardar = new ArrayList<Ropa>();
		ArrayList<PaginaDTO> paginas = new ArrayList<PaginaDTO>();
		paginas = urls.getUrlArray();
		Integer contPagina = 0;
		String urlCalzado = "";
		LOG.info("Inicia búsqueda de zapatillas masculinas");
		for(PaginaDTO url : paginas){
			if(url.getNombrePagina() == "dafiti") {
            	// Recorro toda la paginacion y compruebo si me da un 200 al hacer la petición
            	contPagina = 1;
            	urlCalzado = url.getUrl()+"masculino/calzado/zapatillas/urbanas/?page="+contPagina.toString();
    	        while (getStatusConnectionCode(urlCalzado) == 200) {
    	        	// Obtengo el HTML de la web en un objeto Document
    	            Document document = getHtmlDocument(urlCalzado);
    	            // Busco todas las entradas que estan dentro de: 
    	            Elements entradas = document.select("div.itm-product-main-info");
    	            // Parseo cada una de las entradas
    	            if(entradas.size() != 0){
    	            	for (Element elem : entradas) {
    	            		Elements entradaNombre = elem.select("p.itm-title");
        	            	String nombre = entradaNombre.get(0).text();
        	            	if(nombre.length() > 30){
        	            		nombre = nombre.substring(0, 30)+"...";
        	            	}
        	            	nombre = toCamelCase(nombre.toLowerCase());
        	            	String marca = null;
        	            	if(nombre.split(" ").length <= 2) {
        	            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
        	            	}else {
        	            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[2]);
            	            	if(marca.equals("multipleResult")) {
            	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[2]+" "+nombre.split(" ")[3]);
            	            	}
        	            	}
        	            	Elements entradaImagen = elem.select("div.lazyImage [data-src]");
        	            	String imagenTag = entradaImagen.get(0).toString();
        	            	int indx = imagenTag.indexOf("data-src");
        	            	String imgTmp = imagenTag.substring(indx+10, imagenTag.length());
        	            	int indx2 = imgTmp.indexOf(" ");
        	            	String img = imgTmp.substring(0, indx2-1);
        	            	Elements entradaPrecio = elem.select("p.itm-priceBox [class=itm-price special]");
        	            	if(entradaPrecio.size() == 0){
        	            		entradaPrecio = elem.select("p.itm-priceBox [class=itm-price]");
        	            	}
        	            	String tmp = entradaPrecio.get(0).text();
        	            	int dotindx = tmp.indexOf(",");
        	            	String enteros = tmp.substring(2,dotindx);
        	            	String decimales = tmp.substring(dotindx+1, tmp.length());
        	            	String precioParse = enteros+"."+decimales;
        	            	BigDecimal precio = new BigDecimal(precioParse);
        	            	Document doc = Jsoup.parse(elem.toString());
        	            	Element link = doc.select("a").first();
        	            	String linkHref = link.attr("href");
        	            	calzadoGuardar.add(new Ropa(img,nombre,precio,marca,"zapatilla","urbana","hombre",linkHref,
        	            			url.getNombrePagina()));
        	            	if(marcasZapatillasRepo.findByNombre(marca) == null) {
        	            		MarcasZapatillas marcaZapa = new MarcasZapatillas(marca);
	            				marcasZapatillasRepo.save(marcaZapa);
        	            	}
    	            		
        	            }
        	        	contPagina++;
        	        	urlCalzado = url.getUrl()+"masculino/calzado/zapatillas/urbanas/?page="+contPagina.toString();
    	            }else{
    	            	break;
    	            }
    	        }
    	        totalTmp = calzadoGuardar.size() - totalTmp;
    	        LOG.info("Finaliza busqueda de zapatillas en Dafiti, total: "+totalTmp);
    	        totalTmp = calzadoGuardar.size() + totalTmp;
			}
			if(url.getNombrePagina() == "netshoes") {
            	contPagina = 1;
              	urlCalzado = url.getUrl()+
              			"zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page="+
              			contPagina.toString();
      	        while (getStatusConnectionCode(urlCalzado) == 200) {
      	        	Document document = getHtmlDocument(urlCalzado);
      	            Elements entradas = document.select("div.item-list").select("div.wrapper [itemscope]").after("link");
      	            if(entradas.size() != 0){
      	            	int contadorAux = 0;
      	            	for (Element elem : entradas) {
      	            		contadorAux++;
      	            		Elements entradaNombre = elem.select("a.i");
      	            		if(entradaNombre.size() != 0) {
      	            			String nombre = entradaNombre.attr("title");
              	            	if(nombre.length() > 30){
            	            		nombre = nombre.substring(0, 30)+"...";
            	            	}
              	            	nombre = toCamelCase(nombre.toLowerCase());
              	            	String marca = null;
              	            	if(nombre != null) {
            	            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
                	            	if(marca.equals("multipleResult")) {
                	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[1]+" "+nombre.split(" ")[2]);
                	            	}
            	            	}
              	            	Elements tagImg = elem.select("a.i").select("noscript img");
              	            	String imagenTag = tagImg.toString();
            	            	int indx = imagenTag.indexOf("src");
            	            	String imgTmp = imagenTag.substring(indx+5, imagenTag.length());
            	            	String img = "https:"+imgTmp.substring(0, imgTmp.length()-2);
              	            	Elements link = elem.select("a.in");
              	            	String linkHref = link.attr("href");
              	            	Document documentPrice = getHtmlDocument("https:"+linkHref);
              	            	Elements entradaPrecio = documentPrice.getElementsByAttributeValue("class", "showcase three-col").select("section.buy-box")
              	            			.select("div.if-available").select("span.price");
              	            	if(entradaPrecio.size() != 0){
              	            		String precioS1 = null;
              	            		try {
              	            			precioS1 = entradaPrecio.select("strong").text().toString().split(" ")[1];
              	            		}catch(Exception e) {
              	            			precioS1 = entradaPrecio.select("strong").text().toString().split(" ")[0];
              	            		}
              	            		if(!precioS1.equals("")) {
              	            			String[] precioArr1 = precioS1.split(",");
                  	            		String precioMerge = null;
                  	            		if(precioArr1[0].indexOf(".") != -1) {
                  	            			precioMerge = precioArr1[0].substring(0,1)+precioArr1[0].substring(2,precioArr1[0].length());
                  	            		}
                  	            		String precioS2 = precioMerge != null ? precioMerge+"."+precioArr1[1] : precioArr1[0]+"."+precioArr1[1];
                  	            		BigDecimal precio = new BigDecimal(precioS2);
                      	            	calzadoGuardar.add(new Ropa(img,nombre,precio,marca,"zapatilla","urbana","hombre",linkHref,
                    	            			url.getNombrePagina()));
                      	            	if(marcasZapatillasRepo.findByNombre(marca) == null) {
                    	            		MarcasZapatillas marcaZapa = new MarcasZapatillas(marca);
            	            				marcasZapatillasRepo.save(marcaZapa);
                    	            	}
              	            		}
              	            	}
      	            		}
          	            	
          	            }
          	        	contPagina++;
          	        	urlCalzado = url.getUrl()+
          	        			"zapatillas/masculino?mi=hm_ger_mntop_HOM-CALZ-Zapatillas&psn=Menu_Top&nsCat=Artificial&page="+
          	        			contPagina.toString();
      	            }else{
      	            	break;
      	            }
    	        }
      	        totalTmp = calzadoGuardar.size() - totalTmp;
      	        LOG.info("Finaliza busqueda de zapatillas en Netshoes, total: "+totalTmp);
      	        totalTmp = calzadoGuardar.size() + totalTmp;
			}
			if(url.getNombrePagina() == "opensports") {
            	contPagina = 1;
              	urlCalzado = url.getUrl()+"hombre/zapatillas.html?disciplina=187&p="+contPagina.toString()+"&tipo_producto=769";
              	String primerNombre = "";
      	        while (getStatusConnectionCode(urlCalzado) == 200) {
      	        	Document document = getHtmlDocument(urlCalzado);
      	            Elements entradas = document.select("div.category-products").select("ul.products-grid").select("li.item");
      	            Element entradaNombreCont = entradas.get(0).select("h3.product-name a").first();
	            	String nombreCont = entradaNombreCont.attr("title");
      	            if(!nombreCont.equalsIgnoreCase(primerNombre)){
      	            	for (Element elem : entradas) {
      	            		Element entradaNombre = elem.select("h3.product-name a").first();
          	            	String nombre = entradaNombre.attr("title");
          	            	if(nombre.length() > 30){
        	            		nombre = nombre.substring(0, 30)+"...";
        	            	}
          	            	nombre = toCamelCase(nombre.toLowerCase());
          	            	String marca = null;
          	            	if(nombre != null) {
        	            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
            	            	if(marca.equals("multipleResult")) {
            	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[1]+" "+nombre.split(" ")[2]);
            	            	}
        	            	}
          	            	Element tagImg = elem.select("a.product-image img").first();
          	            	String imagenTag = tagImg.toString();
        	            	int indx = imagenTag.indexOf("src");
        	            	String imgTmp = imagenTag.substring(indx+10, imagenTag.length());
        	            	int indx2 = imgTmp.indexOf("jpg");
        	            	String img = "https:"+imgTmp.substring(0, indx2+3);
        	            	String entradaPrecio = elem.select("div.price-box").select("span.price").text();
          	            	int indxSpace = entradaPrecio.indexOf(" ");
          	            	if(indxSpace != -1){
          	            		entradaPrecio = entradaPrecio.substring(indxSpace+1,entradaPrecio.length());
          	            	}
          	            	int dotIndx = entradaPrecio.indexOf(",");
          	            	entradaPrecio = entradaPrecio.substring(0,dotIndx+3);
          	            	String enteros = "";
          	            	if(entradaPrecio.length() != 8 && entradaPrecio.length() != 10){
          	            		enteros = entradaPrecio.substring(1,dotIndx);
          	            	}else{
          	            		enteros = entradaPrecio.substring(2,dotIndx);
          	            	}
          	            	String decimales = entradaPrecio.substring(dotIndx+1,entradaPrecio.length());
          	            	int dotIndex2 = enteros.indexOf(".");
          	            	if(dotIndex2 != -1){
          	            		enteros = enteros.substring(0,dotIndex2)+enteros.substring(dotIndex2+1,enteros.length());
          	            	}
          	            	String precioFinal = enteros+"."+decimales;
          	            	BigDecimal precio = new BigDecimal(precioFinal);
          	            	Element link = elem.select("h3.product-name a").first();
          	            	String linkHref = link.attr("href");
          	            	calzadoGuardar.add(new Ropa(img,nombre,precio,marca,"zapatilla","urbana","hombre",linkHref,
        	            			url.getNombrePagina()));
          	            	if(marcasZapatillasRepo.findByNombre(marca) == null) {
        	            		MarcasZapatillas marcaZapa = new MarcasZapatillas(marca);
	            				marcasZapatillasRepo.save(marcaZapa);
        	            	}
          	            	Element entradaPrimerNombre = entradas.get(0).select("h3.product-name a").first();
          	            	primerNombre = entradaPrimerNombre.attr("title");
          	        	}
      	            }else{
	            		break;
	            	}
      	            contPagina++;
      	            urlCalzado = url.getUrl()+"hombre/zapatillas.html?disciplina=187&p="+contPagina.toString()+"&tipo_producto=769";
  	            }
      	        totalTmp = calzadoGuardar.size() - totalTmp;
      	        LOG.info("Finaliza busqueda de zapatillas en OpenSports, total: "+totalTmp);
      	        totalTmp = calzadoGuardar.size() + totalTmp;
      	    }
			if(url.getNombrePagina() == "reebok") {
            	contPagina = 0;
              	urlCalzado = url.getUrl()+"zapatillas-lifestyle-mujer?sz=12&start="+contPagina.toString();
      	        while (getStatusConnectionCode(urlCalzado) == 200) {
      	        	Document document = getHtmlDocument(urlCalzado);
      	            Elements entradas = document.select("div.hoverable.clearfix").select("div.product-tile");
      	            if(entradas.size() != 0){
    	            	for (Element elem : entradas) {
    	            		Element entradaNombre = elem.select("div.product-info-inner-content.clearfix a").first();
        	            	String nombre = entradaNombre.attr("data-productname");
        	            	if(nombre.length() > 30){
        	            		nombre = nombre.substring(0, 30)+"...";
        	            	}
        	            	nombre = toCamelCase(nombre.toLowerCase());
        	            	Element tagImg = elem.select("div.image.plp-image-bg").select("img").first();
        	            	String imagenTag = tagImg.toString();
        	            	int indx = imagenTag.indexOf("data-original");
        	            	String imgTmp = imagenTag.substring(indx+15, imagenTag.length());
        	            	int indx2 = imgTmp.indexOf(" ");
        	            	String img = imgTmp.substring(0, indx2-1);
        	            	Element entradaPrecio = elem.select("div.price").select("span.salesprice").first();
        	            	if(entradaPrecio != null){
        	            		String precioE = entradaPrecio.text();
            	            	String enteros = "";
            	            	String decimales = "";
            	            	int dotindx = precioE.indexOf(".");
            	            	int dotindx2 = precioE.indexOf(",");
            	            	if(dotindx != -1){
            	            		if(dotindx2 != -1){
                	            		enteros = precioE.substring(0,1)+precioE.substring(dotindx+1,dotindx2);
                	            		decimales = precioE.substring(dotindx2+1,precioE.length());
                	            	}else{
                	            		enteros = precioE.substring(0,1)+precioE.substring(dotindx+1,precioE.length());
                	            		decimales = "00";
                	            	}
            	            	}else{
            	            		if(dotindx2 != -1){
                	            		enteros = precioE.substring(0,dotindx2);
                	            		decimales = precioE.substring(dotindx2+1,precioE.length());
                	            	}else{
                	            		enteros = precioE;
                	            		decimales = "00";
                	            	}
            	            		
            	            	}
            	            	String precioParse = enteros+"."+decimales;
            	            	BigDecimal precio = new BigDecimal(precioParse);
            	            	Element link = elem.select("div.image.plp-image-bg a").first();
            	            	String linkHref = url.getUrl()+link.attr("href");
            	            	calzadoGuardar.add(new Ropa(img,nombre,precio,"Reebok","zapatilla","urbana","hombre",linkHref,
            	            			url.getNombrePagina()));
              	            	if(marcasZapatillasRepo.findByNombre("Reebok") == null) {
            	            		MarcasZapatillas marcaZapa = new MarcasZapatillas("Reebok");
    	            				marcasZapatillasRepo.save(marcaZapa);
            	            	}
        	            	}
        	            }
        	        	contPagina+=12;
        	        	urlCalzado = url.getUrl()+"zapatillas-lifestyle-mujer?sz=12&start="+contPagina.toString();
    	            }else{
      	            	break;
      	            }
    	        }
      	        totalTmp = calzadoGuardar.size() - totalTmp;
      	        LOG.info("Finaliza busqueda de zapatillas en Reebok, total: "+totalTmp);
      	        totalTmp = calzadoGuardar.size() + totalTmp;
			}
			/*if(url.getNombrePagina() == "adidas") {
            	contPagina = 0;
              	urlCalzado = url.getUrl()+"calzado-lifestyle-hombre?sz=48&start="+contPagina.toString();
      	        while (getStatusConnectionCode(urlCalzado) == 200) {
      	        	Document document = getHtmlDocument(urlCalzado);
      	            Elements entradas = document.select("div.hoverable.clearfix").select("div.product-tile");
      	            if(entradas.size() != 0){
      	            	for (Element elem : entradas) {
    	            		Element entradaNombre = elem.select("div.product-info-inner-content.clearfix a").first();
    	            		if(entradaNombre == null){
    	            			break;
    	            		}
        	            	String nombre = entradaNombre.attr("data-productname");
        	            	if(nombre.length() > 30){
        	            		nombre = nombre.substring(0, 30)+"...";
        	            	}
        	            	nombre = toCamelCase(nombre.toLowerCase());
        	            	Element tagImg = elem.select("div.image.plp-image-bg").select("img").first();
        	            	String imagenTag = tagImg.toString();
        	            	int indx = imagenTag.indexOf("data-original");
        	            	String imgTmp = imagenTag.substring(indx+15, imagenTag.length());
        	            	int indx2 = imgTmp.indexOf(" ");
        	            	String img = imgTmp.substring(0, indx2-1);
        	            	Element entradaPrecio = elem.select("div.price").select("span.salesprice").first();
        	            	if(entradaPrecio != null){
        	            		String precioE = entradaPrecio.text();
            	            	String enteros = "";
            	            	String decimales = "";
            	            	int dotindx = precioE.indexOf(".");
            	            	int dotindx2 = precioE.indexOf(",");
            	            	if(dotindx != -1){
            	            		if(dotindx2 != -1){
                	            		enteros = precioE.substring(0,1)+precioE.substring(dotindx+1,dotindx2);
                	            		decimales = precioE.substring(dotindx2+1,precioE.length());
                	            	}else{
                	            		enteros = precioE.substring(0,1)+precioE.substring(dotindx+1,precioE.length());
                	            		decimales = "00";
                	            	}
            	            	}else{
            	            		if(dotindx2 != -1){
                	            		enteros = precioE.substring(0,dotindx2);
                	            		decimales = precioE.substring(dotindx2+1,precioE.length());
                	            	}else{
                	            		enteros = precioE;
                	            		decimales = "00";
                	            	}
            	            		
            	            	}
            	            	String precioParse = enteros+"."+decimales;
            	            	BigDecimal precio = new BigDecimal(precioParse);
            	            	Element link = elem.select("div.image.plp-image-bg a").first();
            	            	String linkHref = url.getUrl()+link.attr("href");
            	            	calzadoGuardar.add(new Ropa(img,nombre,precio,"Adidas","zapatilla","urbana","hombre",linkHref,
            	            			url.getNombrePagina()));
              	            	if(marcasZapatillasRepo.findByNombre("Adidas") == null) {
            	            		MarcasZapatillas marcaZapa = new MarcasZapatillas("Adidas");
    	            				marcasZapatillasRepo.save(marcaZapa);
            	            	}
        	            	}
        	            }
    	            	contPagina+=48;
        	        	urlCalzado = url.getUrl()+"calzado-lifestyle-hombre?sz=48&start="+contPagina.toString();
    	            }else{
      	            	break;
      	            }
    	        }
      	        LOG.info("Finaliza busqueda de zapatillas en Adidas");
			}
			if(url.getNombrePagina() == "dexter") {
				contPagina = 1;
		      	urlCalzado = url.getUrl()+"calzados/zapatillas/Hombre/Zapatillas/Moda?O=OrderByReleaseDateDESC&PS=12&map=c,c,specificationFilter_11,specificationFilter_19,specificationFilter_15#"+contPagina.toString();
		        while (getStatusConnectionCode(urlCalzado) == 200) {
		        	Document document = getHtmlDocument(urlCalzado);
		            Elements entradas = document.getElementsByAttributeValue("class", "prateleira vitrine n4colunas").select("ul");
		            if(entradas.size() != 0){
		            	for (Element el : entradas) {
		            		Elements subEntradas = el.select("li");
		            		if(subEntradas.size() != 0) {
		            			for(Element elem : subEntradas) {
		            				Element entradaNombre = elem.getElementsByAttributeValue("class", "data").select("h1.title").first();
		    	            		if(entradaNombre != null){
		    	            			String nombre = entradaNombre.select("a").attr("title");
		    	            			if(nombre.length() > 30){
	  	        	            		nombre = nombre.substring(0, 30)+"...";
		  	        	            	}
		  	        	            	nombre = toCamelCase(nombre.toLowerCase());
		  	        	            	String marca = null;
		  	          	            	if(nombre != null) {
		  	        	            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
		  	            	            	if(marca.equals("multipleResult")) {
		  	            	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[1]+" "+nombre.split(" ")[2]);
		  	            	            	}
		  	        	            	}
		  	        	            	String linkHref = elem.getElementsByAttributeValue("class", "image-box").select("a.link-image").attr("href");
		  	        	            	String img = elem.getElementsByAttributeValue("class", "image-box").select("a.link-image").select("img").attr("src");
		  	        	            	String entradaPrecio = elem.getElementsByAttributeValue("class", "price-box").select("span.price").first().text();
		  	        	            	if(entradaPrecio != null){
		  	        	            		String precioS1 = null;
		              	            		try {
		              	            			precioS1 = entradaPrecio.split(" ")[1];
		              	            		}catch(Exception e) {
		              	            			precioS1 = entradaPrecio.split(" ")[0];
		              	            		}
		              	            		if(!precioS1.equals("")) {
		              	            			String[] precioArr1 = precioS1.split(",");
		                  	            		String precioMerge = null;
		                  	            		if(precioArr1[0].indexOf(".") != -1) {
		                  	            			precioMerge = precioArr1[0].substring(0,1)+precioArr1[0].substring(2,precioArr1[0].length());
		                  	            		}
		                  	            		String precioS2 = precioMerge != null ? precioMerge+"."+precioArr1[1] : precioArr1[0]+"."+precioArr1[1];
		                  	            		BigDecimal precio = new BigDecimal(precioS2);
		                      	            	calzadoGuardar.add(new Ropa(img,nombre,precio,marca,"zapatilla","urbana","hombre",linkHref,
		                      	            			url.getNombrePagina()));
		                      	            	if(marcasZapatillasRepo.findByNombre(marca) == null) {
		                    	            		MarcasZapatillas marcaZapa = new MarcasZapatillas(marca);
		            	            				marcasZapatillasRepo.save(marcaZapa);
		                    	            	}
		              	            		}
		  	        	            	}
		    	            		}
		            			}
		            		}
		            	}
		            	contPagina++;
			        	urlCalzado = url.getUrl()+"calzados/zapatillas/Hombre/Zapatillas/Moda?O=OrderByReleaseDateDESC&PS=12&map=c,c,specificationFilter_11,specificationFilter_19,specificationFilter_15#"+contPagina.toString();
			        	LOG.info(contPagina.toString());
		            }else{
		            	break;
		            }
		        }
		        LOG.info("Finaliza busqueda de zapatillas en Dexter");
			}
			if(url.getNombrePagina() == "stockcenter") {
				contPagina = 1;
		      	urlCalzado = url.getUrl()+"calzados/zapatillas/Hombre?O=OrderByReleaseDateDESC&PS=12&map=c,c,specificationFilter_11#"+contPagina.toString();
		        while (getStatusConnectionCode(urlCalzado) == 200) {
		        	Document document = getHtmlDocument(urlCalzado);
		            Elements entradas = document.getElementsByAttributeValue("class", "prateleira n4colunas").select("ul");
		            if(entradas.size() != 0){
		            	for (Element el : entradas) {
		            		Elements subEntradas = el.select("li");
		            		if(subEntradas.size() != 0) {
		            			for(Element elem : subEntradas) {
		            				Element entradaNombre = elem.getElementsByAttributeValue("class", "data").select("h3 > a").first();
		    	            		if(entradaNombre != null){
		    	            			String nombre = entradaNombre.select("a").attr("title");
		    	            			if(nombre.length() > 30){
	  	        	            		nombre = nombre.substring(0, 30)+"...";
		  	        	            	}
		  	        	            	nombre = toCamelCase(nombre.toLowerCase());
		  	        	            	String marca = null;
		  	          	            	if(nombre != null) {
		  	        	            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
		  	            	            	if(marca.equals("multipleResult")) {
		  	            	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[1]+" "+nombre.split(" ")[2]);
		  	            	            	}
		  	        	            	}
		  	        	            	String linkHref = elem.getElementsByAttributeValue("class", "productImage").select("a").attr("href");
		  	        	            	String img = elem.getElementsByAttributeValue("class", "productImage").select("a").select("img").attr("src");
		  	        	            	String entradaPrecio = elem.getElementsByAttributeValue("class", "price").select("span.newPrice").first().text();
		  	        	            	if(entradaPrecio != null){
		  	        	            		String precioS1 = null;
		              	            		try {
		              	            			precioS1 = entradaPrecio.split(" ")[1];
		              	            		}catch(Exception e) {
		              	            			precioS1 = entradaPrecio.split(" ")[0];
		              	            		}
		              	            		if(!precioS1.equals("")) {
		              	            			String[] precioArr1 = precioS1.split(",");
		                  	            		String precioMerge = null;
		                  	            		if(precioArr1[0].indexOf(".") != -1) {
		                  	            			precioMerge = precioArr1[0].substring(0,1)+precioArr1[0].substring(2,precioArr1[0].length());
		                  	            		}
		                  	            		String precioS2 = precioMerge != null ? precioMerge+"."+precioArr1[1] : precioArr1[0]+"."+precioArr1[1];
		                  	            		BigDecimal precio = new BigDecimal(precioS2);
		                      	            	calzadoGuardar.add(new Ropa(img,nombre,precio,marca,"zapatilla","urbana","hombre",linkHref,
		                      	            			url.getNombrePagina()));
		                      	            	if(marcasZapatillasRepo.findByNombre(marca) == null) {
		                    	            		MarcasZapatillas marcaZapa = new MarcasZapatillas(marca);
		            	            				marcasZapatillasRepo.save(marcaZapa);
		                    	            	}
		              	            		}
		  	        	            	}
		    	            		}
		            			}
		            		}
		            	}
		            	contPagina++;
			        	urlCalzado = url.getUrl()+"calzados/zapatillas/Hombre?O=OrderByReleaseDateDESC&PS=12&map=c,c,specificationFilter_11#"+contPagina.toString();
		            }else{
		            	break;
		            }
		        }
		        LOG.info("Finaliza busqueda de zapatillas en Stockcenter");
			}
			if(url.getNombrePagina() == "redsport") {
				contPagina = 1;
		      	urlCalzado = url.getUrl()+"calzado/zapatillas/Hombre?map=c,c,specificationFilter_21&O=OrderByReleaseDateDESC#"+contPagina.toString();
		        while (getStatusConnectionCode(urlCalzado) == 200) {
		        	Document document = getHtmlDocument(urlCalzado);
		            Elements entradas = document.getElementsByAttributeValue("class", "shelf n12colunas").select("ul");
		            if(entradas.size() != 0){
		            	for (Element el : entradas) {
		            		Elements subEntradas = el.select("li");
		            		if(subEntradas.size() != 0) {
		            			for(Element elem : subEntradas) {
		            				Element entradaNombre = elem.getElementsByAttributeValue("class", "name").select("a").first();
		    	            		if(entradaNombre != null){
		    	            			String nombre = entradaNombre.attr("title");
		    	            			if(nombre.length() > 30){
	  	        	            		nombre = nombre.substring(0, 30)+"...";
		  	        	            	}
		  	        	            	nombre = toCamelCase(nombre.toLowerCase());
		  	        	            	String marca = null;
		  	          	            	if(nombre != null) {
		  	        	            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
		  	            	            	if(marca.equals("multipleResult")) {
		  	            	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[1]+" "+nombre.split(" ")[2]);
		  	            	            	}
		  	        	            	}
		  	        	            	String linkHref = elem.getElementsByAttributeValue("class", "image").select("a").attr("href");
		  	        	            	String img = elem.getElementsByAttributeValue("class", "image").select("a").select("img").attr("src");
		  	        	            	String entradaPrecio = elem.getElementsByAttributeValue("class", "price").select("span.best_price").first().text();
		  	        	            	if(entradaPrecio != null){
		  	        	            		String precioS1 = null;
		              	            		try {
		              	            			precioS1 = entradaPrecio.split(" ")[1];
		              	            		}catch(Exception e) {
		              	            			precioS1 = entradaPrecio.split(" ")[0];
		              	            		}
		              	            		if(!precioS1.equals("")) {
		              	            			String[] precioArr1 = precioS1.split(",");
		                  	            		String precioMerge = null;
		                  	            		if(precioArr1[0].indexOf(".") != -1) {
		                  	            			precioMerge = precioArr1[0].substring(0,1)+precioArr1[0].substring(2,precioArr1[0].length());
		                  	            		}
		                  	            		String precioS2 = precioMerge != null ? precioMerge+"."+precioArr1[1] : precioArr1[0]+"."+precioArr1[1];
		                  	            		BigDecimal precio = new BigDecimal(precioS2);
		                      	            	calzadoGuardar.add(new Ropa(img,nombre,precio,marca,"zapatilla","urbana","hombre",linkHref,
		                      	            			url.getNombrePagina()));
		                      	            	if(marcasZapatillasRepo.findByNombre(marca) == null) {
		                    	            		MarcasZapatillas marcaZapa = new MarcasZapatillas(marca);
		            	            				marcasZapatillasRepo.save(marcaZapa);
		                    	            	}
		              	            		}
		  	        	            	}
		    	            		}
		            			}
		            		}
		            	}
		            	contPagina++;
			        	urlCalzado = url.getUrl()+"calzado/zapatillas/Hombre?map=c,c,specificationFilter_21&O=OrderByReleaseDateDESC#"+contPagina.toString();
		            }else{
		            	break;
		            }
		        }
		        LOG.info("Finaliza busqueda de zapatillas en Red Sport");
			}*/
			if(url.getNombrePagina() == "falabella") {
				contPagina = 1;
		      	urlCalzado = url.getUrl()+"falabella-ar/category/cat4210012/Zapatillas-urbanas/N-1z13uheZ1z13wi4?page="+contPagina.toString();
		        while (getStatusConnectionCode(urlCalzado) == 200) {
		        	Document document = getHtmlDocument(urlCalzado);
		            Elements entradas2 = document.getAllElements();
		            Elements entradas = entradas2.select("div.pod-group pod-group__four-pod");
		            if(entradas.size() != 0){
		            	for (Element elem : entradas) {
		            		Element entradaNombre = elem.getElementsByAttributeValue("class", "pod-body").select("a.section__pod-top")
		            				.select("div.section__pod-top-title").first();
		            		String nombre = entradaNombre.attr("title");
	            			if(nombre.length() > 30){
      	            		nombre = nombre.substring(0, 30)+"...";
	        	            	}
	        	            	nombre = toCamelCase(nombre.toLowerCase());
	        	            	String marca = null;
	          	            	if(nombre != null) {
	        	            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
	            	            	if(marca.equals("multipleResult")) {
	            	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[1]+" "+nombre.split(" ")[2]);
	            	            	}
	        	            	}
	        	            	String linkHref = elem.getElementsByAttributeValue("class", "image").select("a").attr("href");
	        	            	String img = elem.getElementsByAttributeValue("class", "image").select("a").select("img").attr("src");
	        	            	String entradaPrecio = elem.getElementsByAttributeValue("class", "price").select("span.best_price").first().text();
	        	            	if(entradaPrecio != null){
	        	            		String precioS1 = null;
          	            		try {
          	            			precioS1 = entradaPrecio.split(" ")[1];
          	            		}catch(Exception e) {
          	            			precioS1 = entradaPrecio.split(" ")[0];
          	            		}
          	            		if(!precioS1.equals("")) {
          	            			String[] precioArr1 = precioS1.split(",");
              	            		String precioMerge = null;
              	            		if(precioArr1[0].indexOf(".") != -1) {
              	            			precioMerge = precioArr1[0].substring(0,1)+precioArr1[0].substring(2,precioArr1[0].length());
              	            		}
              	            		String precioS2 = precioMerge != null ? precioMerge+"."+precioArr1[1] : precioArr1[0]+"."+precioArr1[1];
              	            		BigDecimal precio = new BigDecimal(precioS2);
                  	            	calzadoGuardar.add(new Ropa(img,nombre,precio,marca,"zapatilla","urbana","hombre",linkHref,
                  	            			url.getNombrePagina()));
                  	            	if(marcasZapatillasRepo.findByNombre(marca) == null) {
                	            		MarcasZapatillas marcaZapa = new MarcasZapatillas(marca);
        	            				marcasZapatillasRepo.save(marcaZapa);
                	            	}
          	            		}
        	            	}
	            		
		            	}
		            	contPagina++;
			        	urlCalzado = url.getUrl()+"falabella-ar/category/cat4210012/Zapatillas-urbanas/N-1z13uheZ1z13wi4?page="+contPagina.toString();
		            }else{
		            	break;
		            }
		        }
		        LOG.info("Finaliza busqueda de zapatillas en Falabella");
			}
		}
		calzadoGuardar.sort(Comparator.comparing(Ropa::getPrecio));
		ropaRepo.saveAll(calzadoGuardar);
		LOG.info("Finaliza busqueda de zapatillas masculinas, total de productos: "+ropaRepo.findAll().size());
	}
	
	
	/**
	 * Metodo para scrapear zapatos masculinos
	 * 
	 */
	private List<ProductoDTO> getZapatosHombre() throws ServiceException{
		urls = new Urls();
		List<ProductoDTO> resultado = new ArrayList<ProductoDTO>();
		ArrayList<PaginaDTO> paginas = new ArrayList<PaginaDTO>();
		paginas = urls.getUrlArray();
		Integer contPagina = 0;
		String urlCalzado = "";
		LOG.info("Inicia búsqueda de zapatos masculinos");
		for(PaginaDTO url : paginas){
			if(url.getNombrePagina() == "dorian"){
				contPagina = 0;
		      	urlCalzado = url.getUrl()+"zapatos/";
			        if(getStatusConnectionCode(urlCalzado) == 200) {
			        	Document document = getHtmlDocument(urlCalzado);
			            Elements entradasTmp = document.select("div.product-table").select("div.product-row");
			            if(entradasTmp.size() != 0){
			            	for(Element elemTmp : entradasTmp){
		  	            	Elements entradas = elemTmp.select("div.product-item.dest-gral ");
		  	            	for (Element elem : entradas) {
			            		Element entradaNombre = elem.select("div.title h3 a").first();
		    	            	String nombre = entradaNombre.ownText();
		    	            	if(nombre.length() > 30){
	        	            		nombre = nombre.substring(0, 30)+"...";
	        	            	}
		    	            	nombre = toCamelCase(nombre.toLowerCase());
		    	            	Element tagImg = elem.select("div.head").select(".dosfotos").first();
		    	            	String img = "https:"+tagImg.attr("src");
		    	            	Element entradaPrecio = elem.select("div.bajada").select("div.price").first();
		    	            	String precioStr = entradaPrecio.select("span.price").text();
		    	            	String precioSinSigno = precioStr.substring(1,precioStr.length());
		    	            	BigDecimal precio = new BigDecimal(precioSinSigno);
		    	            	Element link = elem.select("div.title h3 a").first();
		    	            	String linkHref = link.attr("href");
		    	            	String nombreOrigen = "Dorian";
	        	            	ProductoDTO producto = new ProductoDTO(img,nombre,precio,linkHref,nombreOrigen);
		    	            	resultado.add(producto);
			            	}
		  	            }
		            }else{
			            	break;
			            }
			        }
			        LOG.info("Finaliza busqueda de zapatos en Dorian");
			}
		}
		
		resultado.sort((o1, o2) -> {
			int cmp = o1.getPrecioProducto().compareTo(o2.getPrecioProducto());
			return cmp;
		});
	
		LOG.info("Finaliza busqueda de zapatos masculinos, total: "+resultado.size()+" ...");
		return resultado;
	}
	
	/**
	 * Con esta método compruebo el Status code de la respuesta que recibo al hacer la petición
	 * EJM:
	 * 		200 OK			300 Multiple Choices
	 * 		301 Moved Permanently	305 Use Proxy
	 * 		400 Bad Request		403 Forbidden
	 * 		404 Not Found		500 Internal Server Error
	 * 		502 Bad Gateway		503 Service Unavailable
	 * @param url
	 * @return Status Code
	 */
	public static int getStatusConnectionCode(String url) {
			
	    Response response = null;
		
	    try {
		response = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100000).ignoreHttpErrors(true).execute();
	    } catch (IOException ex) {
	    	LOG.info("Excepción al obtener el Status Code: " + ex.getMessage());
	    }
	    return response.statusCode();
	}
	
	/**
	 * Con este método devuelvo un objeto de la clase Document con el contenido del
	 * HTML de la web que me permitirá parsearlo con los métodos de la librelia JSoup
	 * @param url
	 * @return Documento con el HTML
	 */
	public static Document getHtmlDocument(String url) {

	    Document doc = null;
		try {
		    doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100000).get();
		    } catch (IOException ex) {
		    	LOG.info("Excepción al obtener el HTML de la página" + ex.getMessage());
		    }
	    return doc;
	}
	
	/**
	 * Metodo para castear los nombres de los productos
	 * @param s
	 * @return
	 */
	public static String toCamelCase(String s){
		String result = "";
        char firstChar = s.charAt(0);
        result = result + Character.toUpperCase(firstChar);
        for (int i = 1; i < s.length(); i++) {
            char currentChar = s.charAt(i);
            char previousChar = s.charAt(i - 1);
            if (previousChar == ' ') {
                result = result + Character.toUpperCase(currentChar);
            } else {
                result = result + currentChar;
            }
        }
        return result;
	}

	@Override
	public List<MarcasZapatillas> getAllMarcasZapatillas() throws ServiceException {
		return this.marcasZapatillasRepo.findAll();
	}
	
}