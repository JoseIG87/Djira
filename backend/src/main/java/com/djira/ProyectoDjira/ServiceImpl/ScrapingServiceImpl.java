package com.djira.ProyectoDjira.ServiceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.djira.ProyectoDjira.Domain.Ropa;
import com.djira.ProyectoDjira.Service.MarcasZapatillasService;
import com.djira.ProyectoDjira.Service.ScrapingService;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

@Service
public class ScrapingServiceImpl implements ScrapingService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CalzadoServiceImpl.class);
	
	@Autowired
	private MarcasZapatillasService marcasZapatillasService;
	
	@Override
	public List<Ropa> obtenerProductosDafiti(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.dafiti.com.ar/" + path + contPagina.toString();
        while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("div.itm-product-main-info");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombre = elem.select("p.itm-title");
	            	String nombre = entradaNombre.get(0).text();
	            	if(nombre.length() > 30){
	            		nombre = nombre.substring(0, 30)+"...";
	            	}
	            	nombre = toCamelCase(nombre.toLowerCase());
	            	String marca = null;
	            	if(tipo.equals("zapatillas")) {
	            		if(nombre.split(" ").length <= 2) {
		            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
		            	}else {
		            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[2]);
	    	            	if(marca.equals("multipleResult")) {
	    	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[2]+" "+nombre.split(" ")[3]);
	    	            	}
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
	            	ropaGuardar.add(new Ropa(img, nombre, precio, marca, null, null, null, linkHref, "dafiti"));
            		
	            }
	        	contPagina++;
	        	urlCalzado = "https://www.dafiti.com.ar/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosNetshoes(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.netshoes.com.ar/" + path + contPagina.toString();
	    while (getStatusConnectionCode(urlCalzado) == 200) {
	    	Document document = getHtmlDocument(urlCalzado);
	        Elements entradas = document.select("div.item-list").select("div.wrapper [itemscope]").after("link");
	        if(entradas.size() != 0){
	        	for (Element elem : entradas) {
	        		Elements entradaNombre = elem.select("a.i");
	        		if(entradaNombre.size() != 0) {
	        			String nombre = entradaNombre.attr("title");
		            	if(nombre.length() > 30){
		            		nombre = nombre.substring(0, 30)+"...";
		            	}
		            	nombre = toCamelCase(nombre.toLowerCase());
		            	String marca = null;
		            	if(nombre != null && tipo.equals("zapatillas")) {
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
		  	            		ropaGuardar.add(new Ropa(img, nombre, precio, marca, null , null, null, linkHref, "netshoes"));
		            		}
		            	}
	        		}
        		}
		    	contPagina++;
		    	LOG.info("pagina: " + contPagina);
		    	urlCalzado = "https://www.netshoes.com.ar/" + path + contPagina.toString();
	        }else{
	        	break;
	        }
        }
	    return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosOpensports(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "http://www.opensports.com.ar/"+ path + contPagina.toString();
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
  	            	if(nombre != null && tipo.equals("zapatillas")) {
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
  	            	ropaGuardar.add(new Ropa(img, nombre, precio, marca, null, null, null, linkHref, "opensports"));
  	            	Element entradaPrimerNombre = entradas.get(0).select("h3.product-name a").first();
  	            	primerNombre = entradaPrimerNombre.attr("title");
  	        	}
            }else{
            	break;
            }
            contPagina++;
            urlCalzado = "http://www.opensports.com.ar/"+ path + contPagina.toString();
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosReebok(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "http://www.reebok.com.ar/" + path + contPagina.toString();
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
		            	ropaGuardar.add(new Ropa(img, nombre, precio, "Reebok", null, null, null, null, "reebok"));
	            	}
	            }
	        	contPagina+=12;
	        	urlCalzado = "http://www.reebok.com.ar/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosAdidas(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.adidas.com.ar/" + path + contPagina.toString();
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
    	            	String linkHref = "https://www.adidas.com.ar/" + link.attr("href");
    	            	ropaGuardar.add(new Ropa(img, nombre, precio, "Adidas", null, null, null , linkHref, "adidas"));
	            	}
	            }
            	contPagina+=48;
            	urlCalzado = "https://www.adidas.com.ar/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosRedSport(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado =  "https://www.redsportonline.com.ar/" + path + contPagina.toString();
		Ropa ropa = null;
	    while (getStatusConnectionCode(urlCalzado) == 200) {
	    	Document document = getHtmlDocument(urlCalzado);
	    	document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.shelf > ul");
        	for(Element el : entradas) {
        		List<String> nombresArr = new ArrayList<String>();
        		List<String> marcaArr = new ArrayList<String>();
    			List<String> linkArr = new ArrayList<String>();
    			List<String> imagenArr = new ArrayList<String>();
    			List<BigDecimal> preciokArr = new ArrayList<BigDecimal>();
        		Elements entradas1 = el.select("div.name");
                if(entradas1.isEmpty()) {
                	break;
                }
                for (Element elem : entradas1) {
                	String nombre = elem.select("a").attr("title");
                	nombre = toCamelCase(nombre.toLowerCase());
                	String link = elem.select("a").attr("href");
                	nombresArr.add(nombre);
                	linkArr.add(link);
                	String marca = null;
  	            	if(nombre != null && tipo.equals("zapatillas")) {
	            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
    	            	if(marca.equals("multipleResult")) {
    	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[1]+" "+nombre.split(" ")[2]);
    	            	}
    	            	marcaArr.add(marca);
	            	}
                }
                Elements entradas2 = el.select("div.image");
                for (Element elem : entradas2) {
                	String img = elem.select("img").attr("src");
                	imagenArr.add(img);
                }
                Elements entradas3 = el.select("div.price");
                for (Element elem : entradas3) {
                	String precio = elem.select("span.best_price").text();
                	if(!precio.isEmpty()) {
    	        		String[] precioArr1 = precio.split(",");
    	        		String precioMerge = null;
    	        		if(precioArr1[0].indexOf(".") != -1) {
    	        			precioMerge = precioArr1[0].substring(2,3) + precioArr1[0].substring(4,precioArr1[0].length());
    	        		} else{ 
    	        			precioMerge = precioArr1[0].substring(2,precioArr1[0].length());
    	        		}
    	        		String precioS2 = precioMerge+"."+precioArr1[1].substring(0, 2);
    	        		BigDecimal precioD = new BigDecimal(precioS2);
    	        		preciokArr.add(precioD);
                	}
                }
                Optional<Ropa> matchingObject = ropaGuardar.stream().
                	    filter(r -> r.getNombre().equals(nombresArr.get(0))).
                	    findFirst();
                ropa = matchingObject.orElse(null);
                if(ropa != null) {
                	break;
                }
                for(int i=0; i<preciokArr.size(); i++) {
                	ropaGuardar.add(new Ropa(imagenArr.get(i), nombresArr.get(i), preciokArr.get(i), 
                			marcaArr.get(i), null, null, null, linkArr.get(i), "redsport"));
                }
        	}
        	if(ropa != null) {
            	break;
            }
        	contPagina++;
        	urlCalzado =  "https://www.redsportonline.com.ar/" + path + contPagina.toString();
	    }
	    return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosStockCenter(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "http://www.stockcenter.com.ar/" + path + contPagina.toString();
		Ropa ropa = null;
		while (getStatusConnectionCode(urlCalzado) == 200) {
        	Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.prateleira.n4colunas > ul");
        	for(Element el : entradas) {
        		List<String> nombresArr = new ArrayList<String>();
        		List<String> marcaArr = new ArrayList<String>();
    			List<String> linkArr = new ArrayList<String>();
    			List<String> imagenArr = new ArrayList<String>();
    			List<BigDecimal> preciokArr = new ArrayList<BigDecimal>();
        		Elements entradas1 = el.select("div.data");
                if(entradas1.isEmpty()) {
                	break;
                }
                for (Element elem : entradas1) {
                	String nombre = elem.select("h3 > a").attr("title");
                	nombre = toCamelCase(nombre.toLowerCase());
                	String link = elem.select("h3 > a").attr("href");
                	nombresArr.add(nombre);
                	linkArr.add(link);
                	String marca = null;
  	            	if(nombre != null && tipo.equals("zapatillas")) {
	            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
    	            	if(marca.equals("multipleResult")) {
    	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[1]+" "+nombre.split(" ")[2]);
    	            	}
    	            	marcaArr.add(marca);
	            	}
                }
                Elements entradas2 = el.select("a.productImage");
                for (Element elem : entradas2) {
                	String img = elem.select("img").attr("src");
                	imagenArr.add(img);
                }
                Elements entradas3 = el.select("p.price");
                for (Element elem : entradas3) {
                	String precio = elem.select("span.newPrice").text();
                	if(!precio.isEmpty()) {
    	        		String[] precioArr1 = precio.split(",");
    	        		String precioMerge = null;
    	        		if(precioArr1[0].indexOf(".") != -1) {
    	        			precioMerge = precioArr1[0].substring(2,3) + precioArr1[0].substring(4,precioArr1[0].length());
    	        		} else{ 
    	        			precioMerge = precioArr1[0].substring(2,precioArr1[0].length());
    	        		}
    	        		String precioS2 = precioMerge+"."+precioArr1[1].substring(0, 2);
    	        		BigDecimal precioD = new BigDecimal(precioS2);
    	        		preciokArr.add(precioD);
                	}
                }
                Optional<Ropa> matchingObject = ropaGuardar.stream().
                	    filter(r -> r.getNombre().equals(nombresArr.get(0))).
                	    findFirst();
                ropa = matchingObject.orElse(null);
                if(ropa != null) {
                	break;
                }
                for(int i=0; i<preciokArr.size(); i++) {
                	ropaGuardar.add(new Ropa(imagenArr.get(i), nombresArr.get(i), preciokArr.get(i), 
                			marcaArr.get(i), null, null, null, linkArr.get(i), "stockcenter"));
                }
        	}
        	if(ropa != null) {
            	break;
            }
        	contPagina++;
        	urlCalzado = "http://www.stockcenter.com.ar/" + path + contPagina.toString();
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosVcp(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://vancomopina.com.ar/" + path + contPagina.toString();
		while (getStatusConnectionCode(urlCalzado) == 200) {
			List<String> nombresArr = new ArrayList<String>();
			List<String> linkArr = new ArrayList<String>();
			List<String> imagenArr = new ArrayList<String>();
			List<BigDecimal> preciokArr = new ArrayList<BigDecimal>();
        	Document document = getHtmlDocument(urlCalzado);
            Elements entradas1 = document.select("div.product-title");
            if(entradas1.isEmpty()) {
            	break;
            }
            for (Element elem : entradas1) {
            	String nombre = elem.select("a.title").text();
            	nombre = toCamelCase(nombre.toLowerCase());
            	String link = "https://vancomopina.com.ar/" + elem.select("a").attr("href");
            	nombresArr.add(nombre);
            	linkArr.add(link);
            }
            Elements entradas2 = document.select("a.product_card");
            for (Element elem : entradas2) {
            	String img = elem.select("img").attr("src");
            	imagenArr.add(img);
            }
            Elements entradas3 = document.select("span.price");
            for (Element elem : entradas3) {
            	String precio = elem.select("span.money").text();
            	if(!precio.isEmpty()) {
	        		String[] precioArr1 = precio.split(",");
	        		String precioMerge = null;
	        		if(precioArr1[0].indexOf(".") != -1) {
	        			precioMerge = precioArr1[0].substring(1,2) + precioArr1[0].substring(3,precioArr1[0].length());
	        		} else{ 
	        			precioMerge = precioArr1[0].substring(1,precioArr1[0].length());
	        		}
	        		String precioS2 = precioMerge+"."+precioArr1[1].substring(0, 2);
	        		BigDecimal precioD = new BigDecimal(precioS2);
	        		preciokArr.add(precioD);
            	}
            }
            for(int i=0; i<preciokArr.size(); i++) {
            	ropaGuardar.add(new Ropa(imagenArr.get(i), nombresArr.get(i), preciokArr.get(i), 
            			"VCP", null, null, null, linkArr.get(i), "vcp"));
            }
        	contPagina++;
        	urlCalzado = "https://vancomopina.com.ar/" + path + contPagina.toString();
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosDexter(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.dexter.com.ar/" + path + contPagina.toString();
		Ropa ropa = null;
		while (getStatusConnectionCode(urlCalzado) == 200) {
        	Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.prateleira.vitrine.n4colunas > ul");
        	for(Element el : entradas) {
        		List<String> nombresArr = new ArrayList<String>();
        		List<String> marcaArr = new ArrayList<String>();
    			List<String> linkArr = new ArrayList<String>();
    			List<String> imagenArr = new ArrayList<String>();
    			List<BigDecimal> preciokArr = new ArrayList<BigDecimal>();
        		Elements entradas1 = el.select("h1.title");
                if(entradas1.isEmpty()) {
                	break;
                }
                for (Element elem : entradas1) {
                	String nombre = elem.select("a").attr("title");
                	nombre = toCamelCase(nombre.toLowerCase());
                	String link = elem.select("a").attr("href");
                	nombresArr.add(nombre);
                	linkArr.add(link);
                	String marca = null;
  	            	if(nombre != null && tipo.equals("zapatillas")) {
	            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
    	            	if(marca.equals("multipleResult")) {
    	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[1]+" "+nombre.split(" ")[2]);
    	            	}
    	            	marcaArr.add(marca);
	            	}
                }
                Elements entradas2 = el.select("div.image-box");
                for (Element elem : entradas2) {
                	String img = elem.select("img").attr("src");
                	imagenArr.add(img);
                }
                Elements entradas3 = el.select("div.price-box");
                for (Element elem : entradas3) {
                	String precio = elem.select("span.price").text();
                	if(!precio.isEmpty()) {
    	        		String[] precioArr1 = precio.split(",");
    	        		String precioMerge = null;
    	        		if(precioArr1[0].indexOf(".") != -1) {
    	        			precioMerge = precioArr1[0].substring(2,3) + precioArr1[0].substring(4,precioArr1[0].length());
    	        		} else{ 
    	        			precioMerge = precioArr1[0].substring(2,precioArr1[0].length());
    	        		}
    	        		String precioS2 = precioMerge+"."+precioArr1[1].substring(0, 2);
    	        		BigDecimal precioD = new BigDecimal(precioS2);
    	        		preciokArr.add(precioD);
                	}
                }
                Optional<Ropa> matchingObject = ropaGuardar.stream().
                	    filter(r -> r.getNombre().equals(nombresArr.get(0))).
                	    findFirst();
                ropa = matchingObject.orElse(null);
                if(ropa != null) {
                	break;
                }
                for(int i=0; i<preciokArr.size(); i++) {
                	ropaGuardar.add(new Ropa(imagenArr.get(i), nombresArr.get(i), preciokArr.get(i), 
                			marcaArr.get(i), null, null, null, linkArr.get(i), "dexter"));
                }
        	}
        	if(ropa != null) {
            	break;
            }
        	contPagina++;
        	urlCalzado = "https://www.dexter.com.ar/" + path + contPagina.toString();
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosFotter(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.fotter.com.ar/" + path + contPagina.toString();
		Ropa ropa = null;
		while (getStatusConnectionCode(urlCalzado) == 200) {
        	Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.category-products > ul");
        	for(Element el : entradas) {
        		List<String> nombresArr = new ArrayList<String>();
        		List<String> marcaArr = new ArrayList<String>();
    			List<String> linkArr = new ArrayList<String>();
    			List<String> imagenArr = new ArrayList<String>();
    			List<BigDecimal> preciokArr = new ArrayList<BigDecimal>();
        		Elements entradas1 = el.select("h2.product-name");
                for (Element elem : entradas1) {
                	String nombre = elem.select("a").attr("title");
                	nombre = toCamelCase(nombre.toLowerCase());
                	String link = elem.select("a").attr("href");
                	nombresArr.add(nombre);
                	linkArr.add(link);
                	String marca = null;
  	            	if(nombre != null && tipo.equals("zapatillas")) {
	            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
    	            	if(marca.equals("multipleResult")) {
    	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[1]+" "+nombre.split(" ")[2]);
    	            	}
    	            	marcaArr.add(marca);
	            	}
                }
                Elements entradas2 = el.select("a.product-image");
                for (Element elem : entradas2) {
                	String img = elem.select("img").attr("src");
                	imagenArr.add(img);
                }
                Elements entradas3 = el.select("div.price-box");
                for (Element elem : entradas3) {
                	String precio = elem.select("span.price").text();
                	if(!precio.isEmpty()) {
    	        		String[] precioArr1 = precio.split(",");
    	        		String precioMerge = null;
    	        		if(precioArr1[0].indexOf(".") != -1) {
    	        			precioMerge = precioArr1[0].substring(2,3) + precioArr1[0].substring(4,precioArr1[0].length());
    	        		} else{ 
    	        			precioMerge = precioArr1[0].substring(2,precioArr1[0].length());
    	        		}
    	        		String precioS2 = precioMerge+"."+precioArr1[1].substring(0, 2);
    	        		BigDecimal precioD = new BigDecimal(precioS2);
    	        		preciokArr.add(precioD);
                	}
                }
                Optional<Ropa> matchingObject = ropaGuardar.stream().
                	    filter(r -> r.getNombre().equals(nombresArr.get(0))).
                	    findFirst();
                ropa = matchingObject.orElse(null);
                if(ropa != null) {
                	break;
                }
                for(int i=0; i<preciokArr.size(); i++) {
                	ropaGuardar.add(new Ropa(imagenArr.get(i), nombresArr.get(i), preciokArr.get(i), 
                			marcaArr.get(i), null, null, null, linkArr.get(i), "fotter"));
                }
        	}
        	if(ropa != null) {
            	break;
            }
        	contPagina++;
        	urlCalzado = "https://www.fotter.com.ar/" + path + contPagina.toString();
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosLocalsOnly(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.localsonly.com.ar/" + path + contPagina.toString();
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("div.cata-product.cp-grid.clearfix > div");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombre = elem.select("h5.product-name");
	            	String nombre = entradaNombre.get(0).text();
	            	if(nombre.toUpperCase().indexOf("CROCS") == -1) {
	            		if(nombre.length() > 30){
		            		nombre = nombre.substring(0, 30)+"...";
		            	}
		            	nombre = toCamelCase(nombre.toLowerCase());
		            	String marca = null;
		            	if(tipo.equals("zapatillas")) {
		            		marca = marcasZapatillasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
	    	            	if(marca.equals("multipleResult")) {
	    	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[2]+" "+nombre.split(" ")[3]);
	    	            	}
		            	}
		            	Elements entradaImagen = elem.select("div.featured-img.switch-image");
		            	Elements imagen = entradaImagen.select("img.featured-image.front");
		            	String img = imagen.attr("src").trim();
		            	Elements entradaPrecio = elem.select("div.product-price");
		            	Elements precio = entradaPrecio.select("span.price");
		            	if(precio.size() == 0) {
		            		precio = entradaPrecio.select("span.price-sale");
		            		if(precio.size() != 0) {
		            			String tmp = precio.get(0).text();
				            	String precioParse = tmp.substring(1,tmp.length());
				            	BigDecimal precioBd = new BigDecimal(precioParse);
				            	Document doc = Jsoup.parse(elem.toString());
				            	Element link = doc.select("a").first();
				            	String linkHref = "https://www.localsonly.com.ar"+link.attr("href");
				            	ropaGuardar.add(new Ropa(img, nombre, precioBd, marca, null, null, null, linkHref, "localsonly"));
		            		}
		            	}else {
		            		String tmp = entradaPrecio.get(0).text();
		            		String precioParse = tmp.substring(1,tmp.length());
			            	BigDecimal precioBd = new BigDecimal(precioParse);
			            	Document doc = Jsoup.parse(elem.toString());
			            	Element link = doc.select("a").first();
			            	String linkHref = "https://www.localsonly.com.ar"+link.attr("href");
			            	ropaGuardar.add(new Ropa(img, nombre, precioBd, marca, null, null, null, linkHref, "localsonly"));
		            	}
	            	}
	            }
	        	contPagina++;
	        	LOG.info(contPagina.toString());
	        	urlCalzado = "https://www.localsonly.com.ar/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerZapatillasMercadoLibre(String path, String tipo, String filter) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://zapatillas.mercadolibre.com.ar/" + path + contPagina.toString() + filter;
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("section.results.grid > ol > li");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombre = elem.select("h2.item__title.list-view-item-title > span");
	            	String nombre = entradaNombre.get(0).text();
	            	if(nombre.toUpperCase().indexOf("CROCS") == -1) {
	            		String nombreAux = toCamelCase(nombre.toLowerCase());
	            		if(nombre.length() > 30){
		            		nombre = nombre.substring(0, 30)+"...";
		            	}
		            	nombre = toCamelCase(nombre.toLowerCase());
		            	String marca = null;
		            	if(tipo.equals("zapatillas")) {
		            		String [] marcas = nombreAux.split(" ");
		            		for(int i=0; i < marcas.length; i++) {
		            			marca = marcasZapatillasService.obtenerMarcaSegunAlias(marcas[i]);
		            			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
		            				if(marca.equals("multipleResult")) {
			    	            		marca= marcasZapatillasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
			    	            	}
		            				break;
		            			} else {
		            				marca = marcasZapatillasService.obtenerMarca(marcas[i]);
		            				if(marca != null) {
		            					break;
		            				}
		            			}
		            		}
		            		if(marca == null) {
		            			marca = "Otro";
		            		}
		            	}
		            	Elements entradaImagen = elem.select("div.images-viewer");
		            	Elements imagen = entradaImagen.select("img.lazy-load");
		            	String img = imagen.attr("src").trim();
		            	Elements entradaPrecio = elem.select("div.item__price.item__price-discount");
		            	if(entradaPrecio.size() == 0) {
		            		entradaPrecio = elem.select("div.item__price ");
		            	}
		            	Elements precio = entradaPrecio.select("span.price__fraction");
		            	String precioS = precio.get(0).text();
		            	BigDecimal precioBd = new BigDecimal(precioS);
		            	Element link = elem.select("div.images-viewer").first();
		            	String linkHref = link.attr("item-url");
		            	ropaGuardar.add(new Ropa(img, nombre, precioBd, marca, null, null, null, linkHref, "mercadolibre"));
	            	}
	            }
	        	contPagina+=50;
	        	LOG.info(contPagina.toString());
	        	urlCalzado = "https://zapatillas.mercadolibre.com.ar/" + path + contPagina.toString() + filter;
            }else{
            	break;
            }
        }
        return ropaGuardar;
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
	
}
