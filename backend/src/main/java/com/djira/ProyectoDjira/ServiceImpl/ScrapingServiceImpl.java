package com.djira.ProyectoDjira.ServiceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import org.springframework.util.StringUtils;

import com.djira.ProyectoDjira.Domain.Ropa;
import com.djira.ProyectoDjira.Service.MarcasService;
import com.djira.ProyectoDjira.Service.ScrapingService;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

@Service
public class ScrapingServiceImpl implements ScrapingService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CalzadoServiceImpl.class);
	
	@Autowired
	private MarcasService marcasService;
	
	@Override
	public List<Ropa> obtenerProductosDafiti(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.dafiti.com.ar/" + path + contPagina.toString();
        while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("div.itm-product-main-info");
            if(entradas.size() != 0) {
            	for (Element elem : entradas) {
            		Elements entradaNombre = elem.select("p.itm-title");
                	String nombre = entradaNombre.get(0).text();
                	nombre = toCamelCase(nombre.toLowerCase());
                	String marca = null;
                	String [] marcas = nombre.split(" ");
            		for(int i=0; i < marcas.length; i++) {
            			marca = marcasService.obtenerMarcaSegunAlias(marcas[i]);
            			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
            				if(marca.equals("multipleResult")) {
        	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
        	            	}
            				break;
            			} else {
            				marca = marcasService.obtenerMarca(marcas[i]);
            				if(marca != null) {
            					break;
            				}
            			}
            		}
            		if(marca == null) {
            			marca = "Otro";
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
            } else {
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosNetshoes(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.netshoes.com.ar/" + path + contPagina.toString();
	    while (getStatusConnectionCode(urlCalzado) == 200) {
	    	Document document = getHtmlDocument(urlCalzado);
	        Elements entradas = document.select("div.item-list").select("div.wrapper [itemscope]").after("link");
	        if(entradas.size() != 0) {
	        	for (Element elem : entradas) {
	        		Elements entradaNombre = elem.select("a.i");
	        		if(entradaNombre.size() != 0) {
	        			String nombre = entradaNombre.attr("title");
		            	nombre = toCamelCase(nombre.toLowerCase());
		            	String marca = null;
		            	if(nombre != null) {
	        				String [] marcas = nombre.split(" ");
		            		for(int i=0; i < marcas.length; i++) {
		            			marca = marcasService.obtenerMarcaSegunAlias(marcas[i]);
		            			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
		            				if(marca.equals("multipleResult")) {
			    	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
			    	            	}
		            				break;
		            			} else {
		            				marca = marcasService.obtenerMarca(marcas[i]);
		            				if(marca != null) {
		            					break;
		            				}
		            			}
		            		}
		            		if(marca == null || marca == "zapatilla" ) {
		            			marca = "Otro";
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
	    		}
		    	contPagina++;
		    	urlCalzado = "https://www.netshoes.com.ar/" + path + contPagina.toString();
	        } else {
	        	break;
	        }
        }
	    return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosDexter(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.dexter.com.ar/buscapagina?" + path + contPagina.toString();
		while (getStatusConnectionCode(urlCalzado) == 200) {
			Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.prateleira.vitrine.n4colunas > ul > li");       
        	if(entradas.size() != 0) {
        		for(Element el : entradas) {
        			Elements nombreE = el.select("h1.title > a");
        			if(nombreE.size() != 0) {
        				String nombre = toCamelCase(nombreE.attr("title").toLowerCase());
        				String marca = null;
        				String [] marcas = nombre.split(" ");
	            		for(int i=0; i < marcas.length; i++) {
	            			marca = marcasService.obtenerMarcaSegunAlias(marcas[i]);
	            			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
	            				if(marca.equals("multipleResult")) {
		    	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
		    	            	}
	            				break;
	            			} else {
	            				marca = marcasService.obtenerMarca(marcas[i]);
	            				if(marca != null) {
	            					break;
	            				}
	            			}
	            		}
	            		if(marca == null || marca == "zapatilla" ) {
	            			marca = "Otro";
	            		}
    	            	String link = nombreE.attr("href");
                    	String img = el.select("div.image-box > a > img").attr("src");
                        String precioE = el.select("div.price-box > span.price").text();
                        String[] precioArr;
                        String[] precioArrEnt;
    	            	String precioS = "0";
    	            	if(precioE.indexOf(".") != -1) {
    	            		precioArr = precioE.substring(1, precioE.length()).split("\\,");
    	            		precioArrEnt = precioArr[0].split("\\.");
    	            		precioS = (precioArrEnt[0]+precioArrEnt[1]+"."+precioArr[1]);
    	            	} else{
    	            		precioS = precioE.substring(1, precioE.length()).replace(',','.');
    	            	}
    	            	BigDecimal precio = new BigDecimal(precioS.trim());
                    	ropaGuardar.add(new Ropa(img, nombre, precio, 
                    			marca, null, null, null, link, "dexter"));
        			}
            	}
            	contPagina++;
            	urlCalzado = "https://www.dexter.com.ar/buscapagina?" + path + contPagina.toString();
        	} else {
        		break;
        	}
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosOpensports(String path) throws ServiceException {
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
  	            	nombre = toCamelCase(nombre.toLowerCase());
  	            	String marca = null;
  	            	String [] marcas = nombre.split(" ");
            		for(int i=0; i < marcas.length; i++) {
            			marca = marcasService.obtenerMarcaSegunAlias(marcas[i]);
            			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
            				if(marca.equals("multipleResult")) {
	    	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
	    	            	}
            				break;
            			} else {
            				marca = marcasService.obtenerMarca(marcas[i]);
            				if(marca != null) {
            					break;
            				}
            			}
            		}
            		if(marca == null || marca == "zapatilla" ) {
            			marca = "Otro";
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
	public List<Ropa> obtenerProductosReebok(String path) throws ServiceException {
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
	public List<Ropa> obtenerProductosAdidas(String path) throws ServiceException {
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
	public List<Ropa> obtenerProductosStone(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.stoneshoes.com.ar/categoria-producto/" + path + contPagina.toString();
        while (getStatusConnectionCode(urlCalzado) == 200) {
        	Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("div.products.row.row-small.large-columns-3.medium-columns-3.small-columns-2 > div");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements data = elem.select("div.product-small.box ");
            		Elements imgLink = data.select("div.image-fade_in_back > a");
            		Elements nombrePrecio = data.select("div.box-text.box-text-products");
            		if(imgLink.size() != 0 && nombrePrecio.size() != 0){
            			String linkHref = imgLink.attr("href");
            			String img = imgLink.select("img").attr("src");
		            	String nombre = nombrePrecio.select("div.title-wrapper > p.name.product-title > a").first().toString();
		            	int index1 = nombre.indexOf(">");
		            	nombre = nombre.substring(index1+1, nombre.length());
		            	int index2 = nombre.indexOf("<");
		            	nombre = nombre.substring(0, index2);
		            	nombre = toCamelCase(nombre.toLowerCase());
		            	String entradaPrecio = nombrePrecio.select("div.price-wrapper > span.price > span.woocommerce-Price-amount.amount").first().toString();
		            	index1 = entradaPrecio.indexOf("$");
		            	entradaPrecio = entradaPrecio.substring(index1+8, entradaPrecio.length());
		            	index2 = entradaPrecio.indexOf("<");
		            	entradaPrecio = entradaPrecio.substring(0,index2);
		            	String[] precioArr;
		            	String precioFinal = "0";
		            	if(entradaPrecio.indexOf(",") != -1) {
		            		precioArr = entradaPrecio.split(",");
		            		precioFinal = precioArr[0]+precioArr[1];
		            	} else {
		            		precioFinal = entradaPrecio;
		            	}
		            	BigDecimal precio = new BigDecimal(precioFinal);
    	            	ropaGuardar.add(new Ropa(img, nombre, precio, "Stone", null, null, null , linkHref, "stoneshoes"));
	            	}
	            }
            	contPagina++;
            	urlCalzado = "https://www.stoneshoes.com.ar/categoria-producto/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosRedSport(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado =  "https://www.redsportonline.com.ar/buscapagina?" + path + contPagina.toString();
	    while (getStatusConnectionCode(urlCalzado) == 200) {
	    	Document document = getHtmlDocument(urlCalzado);
	    	document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.shelf > ul > li");
        	if(entradas.size() != 0) {
        		for(Element el : entradas) {
            		Elements nombreE = el.select("div.name");
            		if(nombreE.size() != 0) {
            			String nombre = nombreE.select("a").attr("title");
                    	nombre = toCamelCase(nombre.toLowerCase());
                    	String link = nombreE.select("a").attr("href");
                    	String marca = null;
                    	if(nombre != null) {
                    		String [] marcas = nombre.split(" ");
                    		for(int i=0; i < marcas.length; i++) {
                    			marca = marcasService.obtenerMarcaSegunAlias(marcas[i]);
                    			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
                    				if(marca.equals("multipleResult")) {
        	    	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
        	    	            	}
                    				break;
                    			} else {
                    				marca = marcasService.obtenerMarca(marcas[i]);
                    				if(marca != null) {
                    					break;
                    				}
                    			}
                    		}
                    		if(marca == null || marca == "zapatilla" ) {
                    			marca = "Otro";
                    		}
                    	}
                        Elements imgE = el.select("div.image");
                        String img = imgE.select("img").attr("src");
                        Elements priceE = el.select("div.price");
                        String precio = priceE.select("span.best_price").text();
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
        	        		ropaGuardar.add(new Ropa(img, nombre, precioD, 
        	            			marca, null, null, null, link, "redsport"));
                    	}
            		}
            	}
            	contPagina++;
            	urlCalzado =  "https://www.redsportonline.com.ar/buscapagina?" + path + contPagina.toString();
        	} else {
        		break;
        	}
	    }
	    return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosStockCenter(String path) throws ServiceException {
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
  	            	if(nombre != null) {
  	            		String [] marcas = nombre.split(" ");
	            		for(int i=0; i < marcas.length; i++) {
	            			marca = marcasService.obtenerMarcaSegunAlias(marcas[i]);
	            			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
	            				if(marca.equals("multipleResult")) {
		    	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
		    	            	}
	            				break;
	            			} else {
	            				marca = marcasService.obtenerMarca(marcas[i]);
	            				if(marca != null) {
	            					break;
	            				}
	            			}
	            		}
	            		if(marca == null || marca == "zapatilla" ) {
	            			marca = "Otro";
	            		}
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
	public List<Ropa> obtenerProductosSportline(String path1, String path2) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.sportline.com.ar/buscapagina?" + path1 + contPagina.toString() + path2;
		while (getStatusConnectionCode(urlCalzado) == 200) {
			Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.product-data.n24colunas > ul > li");       
        	if(entradas.size() != 0) {
        		for(Element el : entradas) {
        			String img = el.select("div.product-image > img").attr("src");
        			String json = el.select("div.data").text();
        			String[] datos = json.split(",");
        			if(json.length() != 0) {
        				String nombre = toCamelCase(datos[9].substring(10, datos[9].length()-1).toLowerCase());
            			String marca = null;
        				String [] marcas = nombre.split(" ");
                		for(int i=0; i < marcas.length; i++) {
                			marca = marcasService.obtenerMarcaSegunAlias(marcas[i]);
                			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
                				if(marca.equals("multipleResult")) {
    	    	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
    	    	            	}
                				break;
                			} else {
                				marca = marcasService.obtenerMarca(marcas[i]);
                				if(marca != null) {
                					break;
                				}
                			}
                		}
                		if(marca == null || marca == "zapatilla" ) {
                			marca = "Otro";
                		}
                		String link = datos[0].substring(9, datos[0].length()-1);
                		String precioE = datos[5].substring(16, datos[5].length()) + "," + datos[6].substring(0, datos[6].length()-1);
                		String[] precioArr;
                        String[] precioArrEnt;
    	            	String precioS = "0";
    	            	if(precioE.indexOf(".") != -1) {
    	            		precioArr = precioE.substring(1, precioE.length()).split("\\,");
    	            		precioArrEnt = precioArr[0].split("\\.");
    	            		precioS = (precioArrEnt[0]+precioArrEnt[1]+"."+precioArr[1]);
    	            	} else{
    	            		precioS = precioE.substring(1, precioE.length()).replace(',','.');
    	            	}
    	            	BigDecimal precio = new BigDecimal(precioS.trim());
                    	ropaGuardar.add(new Ropa(img, nombre, precio, 
                    			marca, null, null, null, link, "sportline"));
        			}
            	}
            	contPagina++;
            	urlCalzado = "https://www.sportline.com.ar/buscapagina?" + path1 + contPagina.toString() + path2;
        	} else {
        		break;
        	}
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosVcp(String path) throws ServiceException {
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
	public List<Ropa> obtenerProductosLocalsOnly(String path) throws ServiceException {
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
		            	nombre = toCamelCase(nombre.toLowerCase());
		            	String marca = null;
		            	String [] marcas = nombre.split(" ");
	            		for(int i=0; i < marcas.length; i++) {
	            			marca = marcasService.obtenerMarcaSegunAlias(marcas[i]);
	            			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
	            				if(marca.equals("multipleResult")) {
		    	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
		    	            	}
	            				break;
	            			} else {
	            				marca = marcasService.obtenerMarca(marcas[i]);
	            				if(marca != null) {
	            					break;
	            				}
	            			}
	            		}
	            		if(marca == null || marca == "zapatilla" ) {
	            			marca = "Otro";
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
	public List<Ropa> obtenerProductosRingo(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		String urlCalzado = "https://tienda.ringo.com.ar/" + path;
		if (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("div#products > div");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombre = elem.select("h3.title > a");
	            	String nombre = entradaNombre.attr("title");
	            	nombre = toCamelCase(tipo) + " Ringo " + toCamelCase(nombre.toLowerCase());
	            	Elements entradaImagen = elem.select("a.product_img_link");
	            	Elements imagen = entradaImagen.select("img");
	            	String img = imagen.attr("src").trim();
	            	Elements entradaPrecio = elem.select("div.content_price");
	            	Elements precio = entradaPrecio.select("span.price.product-price.precio-list");
	            	String precioS = precio.get(0).text();
	            	String[] precioArr;
	            	String precioFinal = "0";
	            	if(precioS.indexOf(",") != -1) {
	            		precioArr = precioS.split(",");
	            		if(precioArr[0].indexOf(".") != -1) {
	            			precioFinal = precioArr[0].substring(1, 2) + precioArr[0].substring(3, precioArr[0].length());
	            		}else {
	            			precioFinal = precioArr[0].substring(1, precioArr[0].length());
	            		}
	            		precioFinal = precioFinal+"."+precioArr[1];
	            	}
	            	BigDecimal precioBd = new BigDecimal(precioFinal);
	            	Element link = elem.select("h3.title > a").first();
	            	String linkHref = link.attr("href");
	            	ropaGuardar.add(new Ropa(img, nombre, precioBd, "Ringo", null, null, null, linkHref, "ringo"));
	            }
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosMercadoLibre(String path, String tipoSingular, String tipoPlural, String filter) throws ServiceException {
		String uri = "";
		switch (tipoSingular.toLowerCase()) {
	        case "zapatilla":  uri = "https://zapatillas.mercadolibre.com.ar/";
	        break;
	        case "zapato":  uri = "https://zapatos.mercadolibre.com.ar/";
	        break;
	        case "mocasin":  uri = "https://zapatos.mercadolibre.com.ar/";
		}
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = uri + path + contPagina.toString() + filter;
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("section.results.grid > ol > li");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombre = elem.select("h2.item__title.list-view-item-title > span");
	            	String nombre = entradaNombre.get(0).text();
	            	if(nombre.toUpperCase().indexOf(tipoPlural.toUpperCase()) != -1 || nombre.toUpperCase().indexOf(tipoSingular.toUpperCase()) != -1) {
	            		nombre = toCamelCase(nombre.toLowerCase());
		            	String marca = null;
		            	String [] marcas = nombre.split(" ");
	            		for(int i=0; i < marcas.length; i++) {
	            			marca = marcasService.obtenerMarcaSegunAlias(marcas[i]);
	            			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
	            				if(marca.equals("multipleResult")) {
		    	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
		    	            	}
	            				break;
	            			} else {
	            				marca = marcasService.obtenerMarca(marcas[i]);
	            				if(marca != null) {
	            					break;
	            				}
	            			}
	            		}
	            		if(marca == null || marca == "zapatilla" ) {
	            			marca = "Otro";
	            		}
		            	Elements entradaImagen = elem.select("div.images-viewer");
		            	Elements imagen = entradaImagen.select("img.lazy-load");
		            	String img = "";
		            	if(imagen.size() == 0) {
		            		imagen = entradaImagen.select("img.loading");
		            		img = imagen.attr("data-src");
		            	} else {
		            		img = imagen.attr("src").trim();
		            	}
		            	Elements entradaPrecio = elem.select("div.item__price.item__price-discount");
		            	if(entradaPrecio.size() == 0) {
		            		entradaPrecio = elem.select("div.item__price ");
		            	}
		            	Elements precioEntero = entradaPrecio.select("span.price__fraction");
		            	Elements precioDecimal = entradaPrecio.select("span.price__decimals");
		            	String precioEnteroS = precioEntero.get(0).text();
		            	if(precioEnteroS.indexOf(".") != -1) {
		            		precioEnteroS = precioEnteroS.substring(0,1) + precioEnteroS.substring(2, precioEnteroS.length());
		            	}
		            	String precioDecimalS = "00";
		            	if(precioDecimal.size() != 0) {
		            		precioDecimalS = precioDecimal.get(0).text();
		            	}
		            	String precioS = precioEnteroS+"."+precioDecimalS;
		            	BigDecimal precioBd = new BigDecimal(precioS);
		            	Element link = elem.select("div.images-viewer").first();
		            	String linkHref = link.attr("item-url");
		            	ropaGuardar.add(new Ropa(img, nombre, precioBd, marca, null, null, null, linkHref, "mercadolibre"));
	            	}
	            }
	        	contPagina+=50;
	        	LOG.info(contPagina.toString());
	        	urlCalzado = uri + path + contPagina.toString() + filter;
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosBorsalino(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://calzadosborsalino.com/" + path + contPagina.toString();
        while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("div.products.row.row-small.large-columns-3.medium-columns-3.small-columns-2 > div");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombre = elem.select("p.name.product-title");
	            	String nombre = entradaNombre.get(0).text();
	            	nombre = toCamelCase(nombre.toLowerCase());
	            	Elements entradaImagen = elem.select("div.box-image");
	            	String img = entradaImagen.select("img").attr("src");
	            	Elements entradaPrecio = elem.select("div.price-wrapper > span.price");
	            	Elements entradaPrecio2 = entradaPrecio.select("ins");
	            	String precioE = "0";
	            	if(entradaPrecio2.size() != 0) {
	            		String precioAux = entradaPrecio2.select("span.woocommerce-Price-amount.amount").toString();
	            		int index = precioAux.indexOf("$");
	            		precioE = precioAux.substring(index+8);
	            		int index2 = precioE.indexOf("<");
	            		precioE = precioE.substring(0,index2);
	            	} else {
	            		String precioAux = entradaPrecio.select("span.woocommerce-Price-amount.amount").toString();
	            		int index = precioAux.indexOf("$");
	            		precioE = precioAux.substring(index+8);
	            		int index2 = precioE.indexOf("<");
	            		precioE = precioE.substring(0,index2);
	            	}
	            	String[] precioArr;
	            	String precioS = "0";
	            	if(precioE.indexOf(".") != -1) {
	            		precioArr = precioE.split("\\.");
	            		precioS = precioArr[0]+precioArr[1];
	            	} else{
	            		precioS = precioE;
	            	}
	            	BigDecimal precio = new BigDecimal(precioS);
	            	Elements link = elem.select("div.box-imagea");
	            	String linkHref = link.select("a").attr("href");
	            	ropaGuardar.add(new Ropa(img, nombre, precio, "Borsalino", null, null, null, linkHref, "borsalino"));
            		
	            }
	        	contPagina++;
	        	urlCalzado = "https://calzadosborsalino.com/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosDashGridMark(String path, String pagina) throws ServiceException {
		String uri = "";
		switch (pagina) {
	        case "dash":  uri = "https://www.tiendadash.com.ar/";
	        break;
	        case "grid":  uri = "https://www.grid.com.ar/";
	        break;
	        case "mark":  uri = "https://www.marksports.com.ar/";
		}
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		String urlCalzado = uri + path;
		if (getStatusConnectionCode(urlCalzado) == 200) {
        	Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = null;
        	if(pagina.equals("mark")) {
        		entradas = document.select("div.prateleira.vitrine.n1colunas > ul");
        	}else {
        		entradas = document.select("div.prateleira.vitrine.shelf.n1colunas > ul");
        	}
        	for(Element el : entradas) {
        		Elements entradasName = el.select("div.product-name");
        		String nombre = entradasName.select("a").attr("title");
            	nombre = toCamelCase(nombre.toLowerCase());
            	String link = entradasName.select("a").attr("href");
            	String marca = null;
            	marca = marcasService.obtenerMarcaSegunAlias(nombre.split(" ")[1]);
            	if(marca.equals("multipleResult")) {
            		marca= marcasService.obtenerMarcaSegunAliasSimilar(nombre.split(" ")[1]+" "+nombre.split(" ")[2]);
            	}
                Elements entradasImg = el.select("a.product-image");
                String img = entradasImg.select("img").attr("src");
                Elements entradasPrice = el.select("div.price");
                String precio = entradasPrice.select("span.best-price").text();
                BigDecimal precioD = null;
                if(!precio.isEmpty()) {
	        		String[] precioArr1 = precio.split(",");
	        		String precioMerge = null;
	        		if(precioArr1[0].indexOf(".") != -1) {
	        			precioMerge = precioArr1[0].substring(2,3) + precioArr1[0].substring(4,precioArr1[0].length());
	        		} else{ 
	        			precioMerge = precioArr1[0].substring(2,precioArr1[0].length());
	        		}
	        		String precioS2 = precioMerge+"."+precioArr1[1].substring(0, 2);
	        		precioD = new BigDecimal(precioS2);
            	}
            	ropaGuardar.add(new Ropa(img, nombre, precioD, 
            			marca, null, null, null, link, pagina));
        	}
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosVicus(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.vicusba.com/" + path + "XpO" + contPagina.toString() + "XtOcXvOgalleryxSM";
		while (getStatusConnectionCode(urlCalzado) == 200) {
			Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.rowsWrapper > ol > li");
        	if(entradas.size() != 0) {
        		for(Element el : entradas) {
            		Elements entradasName = el.select("a.title");
            		final String nombre = "Zapatillas Vicus " + entradasName.attr("title");
                	String link = "https://www.vicusba.com"+entradasName.attr("href");
                    Elements entradasImg = el.select("figure > a");
                    String img = entradasImg.select("img").attr("src");
                    Elements entradasPrice = el.select("ul.itemPrice");
                    String precio = entradasPrice.select("span.ch-price.price").text();
                    precio = precio.substring(2,precio.length()).replace(',','.');
                    BigDecimal precioD = null;
                    if(!precio.isEmpty()) {
    	        		precioD = new BigDecimal(precio);
                	}
                	ropaGuardar.add(new Ropa(img, nombre, precioD, 
                			"Vicus", null, null, null, link, "vicus"));
            	}
            	contPagina++;
            	urlCalzado = "https://www.vicusba.com/" + path + "XpO" + contPagina.toString() + "XtOcXvOgalleryxSM";
        	} else {
        		break;
        	}
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosTheNetBoutique(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.thenetboutique.com/" + path + contPagina.toString();
		while (getStatusConnectionCode(urlCalzado) == 200) {
			Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.container-fluid.products-list > div.row > div");
        	if(entradas.size() != 0) {
        		for(Element el : entradas) {
        			Elements info = el.select("div.info");
        			String nombre = info.select("span").get(0).toString();
        			int indx = nombre.indexOf(">");
	            	nombre = nombre.substring(indx+2, nombre.length());
	            	indx = nombre.indexOf("<");
	            	nombre = nombre.substring(0,indx);
            		String marca = info.select("h5").get(0).toString();
            		int indx2 = marca.indexOf(">");
            		marca = marca.substring(indx2+2, marca.length());
	            	indx2 = marca.indexOf("<");
	            	marca = marca.substring(0,indx2);
            		marca = marcasService.obtenerMarcaSegunAlias(marca.split(" ")[0]);
	            	if(marca.equals("multipleResult")) {
	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marca.split(" ")[0]+" "+marca.split(" ")[1]);
	            	}
	            	Elements linkImg = el.select("a.col-xs-6.col-sm-4.product");
                	String link = linkImg.attr("href");
                	String img = linkImg.select("figure > img").attr("data-src");
                    String precioE = info.select("span.price").text();
                    precioE = precioE.substring(1,precioE.length()).trim();
                    String[] precioArr;
	            	String precioS = "0";
	            	if(precioE.indexOf(".") != -1) {
	            		precioArr = precioE.split("\\.");
	            		precioS = (precioArr[0]+precioArr[1]).replace(',','.');
	            	} else{
	            		precioS = precioE.replace(',','.');
	            	}
	            	BigDecimal precio = new BigDecimal(precioS);
                	ropaGuardar.add(new Ropa(img, nombre, precio, 
                			marca, null, null, null, link, "thenetboutique"));
            	}
            	contPagina++;
            	urlCalzado = "https://www.thenetboutique.com/" + path + contPagina.toString();
        	} else {
        		break;
        	}
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosTejano(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://tejano.com.ar/" + path + contPagina.toString();
		Ropa ropa = null;
		while (getStatusConnectionCode(urlCalzado) == 200) {
			Document document = getHtmlDocument(urlCalzado);
			Elements entradas = document.select("div.products.wrapper.grid.columns4.products-grid > ol > li");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombre = elem.select("a.product-item-link");
	            	String nombre = entradaNombre.toString();
	            	if(nombre.length() == 0) {
	            		break;
	            	}
	            	int indx = nombre.indexOf(">");
	            	nombre = nombre.substring(indx+1, nombre.length());
	            	indx = nombre.indexOf("<");
	            	nombre = nombre.substring(0,indx);
	            	nombre = toCamelCase(nombre.toLowerCase()).trim();
	            	final String name = nombre;
	            	Optional<Ropa> matchingObject = ropaGuardar.stream().filter(r->r.getNombre().equals(name)).findFirst();
	            	ropa = matchingObject.orElse(null);
	            	if(ropa != null){
	            		break;
	            	}
	            	String linkHref = entradaNombre.attr("href");
	            	Elements entradaImagen = elem.select("div.product.photo.product-item-photo > a > img");
	            	String img = entradaImagen.attr("src").trim();
	            	Elements entradaPrecio = elem.select("span.price");
	            	String precioAux = entradaPrecio.get(0).toString();
            		int index1 = precioAux.indexOf("$");
            		precioAux = precioAux.substring(index1+1,precioAux.length());
            		int index2 = precioAux.indexOf("<");
            		precioAux = precioAux.substring(0,index2);
	            	String[] precioArr;
	            	String precioS = "0";
	            	if(precioAux.indexOf(".") != -1) {
	            		precioArr = precioAux.split("\\.");
	            		precioS = (precioArr[0]+precioArr[1]).replace(',','.');
	            	} else{
	            		precioS = precioAux.replace(',','.');
	            	}
	            	BigDecimal precio = new BigDecimal(precioS);
	            	ropaGuardar.add(new Ropa(img, nombre, precio, "Tejano", null, null, null, linkHref, "tejano"));
	            }
            	if(ropa != null){
            		break;
            	}
	        	contPagina++;
	        	urlCalzado = "https://tejano.com.ar/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosC1rca(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://c1rca.com.ar/shop/" + path + contPagina.toString();
		while (getStatusConnectionCode(urlCalzado) == 200) {
			Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.products > article");
        	if(entradas.size() != 0) {
        		for(Element el : entradas) {
        			Elements info = el.select("div.product-description");
        			String nombre = info.select("h3.h3.product-title > a").get(0).toString();
        			int indx = nombre.indexOf("\">");
	            	nombre = nombre.substring(indx+2, nombre.length());
	            	indx = nombre.indexOf("<");
	            	nombre = nombre.substring(0,indx);
	            	nombre = toCamelCase(nombre.toLowerCase()).trim();
	            	Elements linkImg = info.select("h3.h3.product-title");
                	String link = linkImg.attr("href");
                	String img = el.select("a.thumbnail.product-thumbnail > img").attr("src");
                    String precioE = info.select("div.product-price-and-shipping > span.price").text();
                    precioE = precioE.substring(1,precioE.length());
                    String[] precioArr;
	            	String precioS = "0";
	            	if(precioE.indexOf(".") != -1) {
	            		precioArr = precioE.split("\\.");
	            		precioS = (precioArr[0]+precioArr[1]).replace(',','.');
	            	} else{
	            		precioS = precioE.replace(',','.');
	            	}
	            	precioS = precioS.substring(1,precioS.length());
	            	BigDecimal precio = new BigDecimal(precioS);
                	ropaGuardar.add(new Ropa(img, nombre, precio, 
                			"C1rca", null, null, null, link, "c1rca"));
            	}
            	contPagina++;
            	urlCalzado = "https://c1rca.com.ar/shop/" + path + contPagina.toString();
        	} else {
        		break;
        	}
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosRevolutionss(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.revolutionss.com.ar/buscapagina?" + path + contPagina.toString();
		while (getStatusConnectionCode(urlCalzado) == 200) {
			Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.prateleira-category.n30colunas > ul > li");       
        	if(entradas.size() != 0) {
        		for(Element el : entradas) {
        			Elements nombreE = el.select("div.product-name > a");
        			if(nombreE.size() != 0) {
        				String nombre = nombreE.attr("title");
        				String marca = null;
        				String [] marcas = nombre.split(" ");
	            		for(int i=0; i < marcas.length; i++) {
	            			marca = marcasService.obtenerMarcaSegunAlias(marcas[i]);
	            			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
	            				if(marca.equals("multipleResult")) {
		    	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
		    	            	}
	            				break;
	            			} else {
	            				marca = marcasService.obtenerMarca(marcas[i]);
	            				if(marca != null) {
	            					break;
	            				}
	            			}
	            		}
	            		if(marca == null || marca == "zapatilla" ) {
	            			marca = "Otro";
	            		}
    	            	String link = nombreE.attr("href");
                    	String img = el.select("div.producto-inside > a > img").attr("src");
                        String precioE = el.select("div.precio-content").text();
                        if(StringUtils.countOccurrencesOf(precioE, "$") > 1) {
                        	String[] precioArrAux;
                        	precioE = precioE.substring(1,precioE.length());
                        	precioArrAux = precioE.split("\\$");
                        	precioE = precioArrAux[1];
                        }else {
                        	precioE = precioE.substring(1,precioE.length());
                        }
                        String[] precioArr;
                        String[] precioArrEnt;
    	            	String precioS = "0";
    	            	if(precioE.indexOf(".") != -1) {
    	            		precioArr = precioE.split("\\,");
    	            		precioArrEnt = precioArr[0].split("\\.");
    	            		precioS = (precioArrEnt[0]+precioArrEnt[1]+"."+precioArr[1]);
    	            	} else{
    	            		precioS = precioE.replace(',','.');
    	            	}
    	            	BigDecimal precio = new BigDecimal(precioS.trim());
                    	ropaGuardar.add(new Ropa(img, nombre, precio, 
                    			marca, null, null, null, link, "revolution"));
        			}
            	}
            	contPagina++;
            	urlCalzado = "https://www.revolutionss.com.ar/buscapagina?" + path + contPagina.toString();
        	} else {
        		break;
        	}
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosSporting(String path1, String path2) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.sporting.com.ar/buscapagina?" + path1 + contPagina.toString() + path2;
		while (getStatusConnectionCode(urlCalzado) == 200) {
			Document document = getHtmlDocument(urlCalzado);
        	Elements entradas1 = document.select("div.product-list.n4colunas > ul");       
        	if(entradas1.size() != 0) {
        		for(Element elAux : entradas1) {
        			Elements entradas2 = elAux.select("li");
        			for(Element el : entradas2) {
        				Elements nombreE = el.select("a.product-name");
            			if(nombreE.size() != 0) {
            				String nombre = toCamelCase(nombreE.attr("title").toLowerCase());
            				String marca = null;
            				String [] marcas = nombre.split(" ");
    	            		for(int i=0; i < marcas.length; i++) {
    	            			marca = marcasService.obtenerMarcaSegunAlias(marcas[i]);
    	            			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
    	            				if(marca.equals("multipleResult")) {
    		    	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
    		    	            	}
    	            				break;
    	            			} else {
    	            				marca = marcasService.obtenerMarca(marcas[i]);
    	            				if(marca != null) {
    	            					break;
    	            				}
    	            			}
    	            		}
    	            		if(marca == null || marca == "zapatilla" ) {
    	            			marca = "Otro";
    	            		}
        	            	String link = nombreE.attr("href");
                        	String img = el.select("a.imagen-prod > img").attr("src");
                            String precioE = el.select("div.product-price > span.bestPrice").text();
                            String[] precioArr;
                            String[] precioArrEnt;
        	            	String precioS = "0";
        	            	if(precioE.indexOf(".") != -1) {
        	            		precioArr = precioE.substring(1, precioE.length()).split("\\,");
        	            		precioArrEnt = precioArr[0].split("\\.");
        	            		precioS = (precioArrEnt[0]+precioArrEnt[1]+"."+precioArr[1]);
        	            	} else{
        	            		precioS = precioE.substring(1, precioE.length()).replace(',','.');
        	            	}
        	            	BigDecimal precio = new BigDecimal(precioS.trim());
                        	ropaGuardar.add(new Ropa(img, nombre, precio, 
                        			marca, null, null, null, link, "sporting"));
            			}
        			}
            	}
            	contPagina++;
            	urlCalzado = "https://www.sporting.com.ar/buscapagina?" + path1 + contPagina.toString() + path2;
        	} else {
        		break;
        	}
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosSolodeportes(String path1, String path2) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.solodeportes.com.ar/" + path1 + contPagina.toString() + path2;
		while (getStatusConnectionCode(urlCalzado) == 200) {
			Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.products.wrapper.grid.products-grid > ol > li");       
        	if(entradas.size() != 0) {
        		for(Element el : entradas) {
        			Elements nameImag = el.select("span.product-image-wrapper > img");
        			if(nameImag.size() != 0) {
        				String nombre = toCamelCase(nameImag.attr("alt").toLowerCase());
        				String marca = null;
        				String [] marcas = nombre.split(" ");
	            		for(int i=0; i < marcas.length; i++) {
	            			marca = marcasService.obtenerMarcaSegunAlias(marcas[i]);
	            			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
	            				if(marca.equals("multipleResult")) {
		    	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marcas[i]+" "+marcas[i+1]);
		    	            	}
	            				break;
	            			} else {
	            				marca = marcasService.obtenerMarca(marcas[i]);
	            				if(marca != null) {
	            					break;
	            				}
	            			}
	            		}
	            		if(marca == null || marca == "zapatilla" ) {
	            			marca = "Otro";
	            		}
    	            	String link = el.select("a.product.photo.product-item-photo").attr("href");
                    	String img = nameImag.attr("data-src");
                        String precioE = el.select("span.price-container").text();
                        String[] precioArrEnt;
    	            	String precioS = "0";
    	            	int indx = precioE.indexOf("Valor mínimo");
    	            	int indx2 = precioE.indexOf("Precio especial");
    	            	if(indx != -1) {
    	            		precioE = precioE.substring(indx+13,precioE.length());
    	            	}
    	            	if(indx2 != -1) {
    	            		precioE = precioE.substring(indx2+16,precioE.length());
    	            	}
    	            	if(precioE.indexOf(".") != -1) {
    	            		precioArrEnt = precioE.substring(1,precioE.length()).split("\\.");
    	            		precioS =  precioArrEnt[0]+precioArrEnt[1]+".00";
    	            	} else{
    	            		precioS = precioE.substring(1, precioE.length()).replace(',','.');
    	            	}
    	            	BigDecimal precio = new BigDecimal(precioS.trim());
                    	ropaGuardar.add(new Ropa(img, nombre, precio, 
                    			marca, null, null, null, link, "dexter"));
        			}
            	}
            	contPagina++;
            	urlCalzado = "https://www.solodeportes.com.ar/" + path1 + contPagina.toString() + path2;
        	} else {
        		break;
        	}
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosLaferiadelcalzado(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.laferiadelcalzado.com/" + path + contPagina.toString();
        while (getStatusConnectionCode(urlCalzado) == 200) {
        	Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("div.js-product-table.product-grid").select("div.item");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Element entradaImgLink = elem.select("div.p-relative > a").first();
            		Element entradaNombreprecio = elem.select("div.item-info-container ").first();
            		if(entradaImgLink != null &&  entradaNombreprecio != null){
            			String imgPrimera = entradaImgLink.select("img").attr("data-srcset");
            			String[] imgArr = imgPrimera.split(",");
            			String img = imgArr[imgArr.length-1];
            			img = img.substring(0, img.length()-4);
            			String linkHref = entradaImgLink.attr("href");
            			String nombre = entradaNombreprecio.select("div.title > a").attr("title");
    	            	nombre = toCamelCase(nombre.toLowerCase());
    	            	String precioE = entradaNombreprecio.select("div.item-price-container.price.m-none-xs ").select("span.price.item-price.p-left-quarter").first().text();
    	            	precioE = precioE.substring(1,precioE.length());
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
    	            	ropaGuardar.add(new Ropa(img, nombre, precio, "Otro", null, null, null , linkHref, "laferiadelcalzado"));
            		}
	            }
            	contPagina++;
            	urlCalzado = "https://www.laferiadelcalzado.com/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosDigitalsport(String pagina, String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.digitalsport.com.ar/" + pagina + "/prods/" + path + contPagina.toString();
		while (getStatusConnectionCode(urlCalzado) == 200) {
			Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("div.productos.conmenu > a.producto");       
        	if(entradas.size() != 0) {
        		for(Element el : entradas) {
        			Elements nameImag = el.select("img.img");
        			Elements marcaE = el.select("img.logo_marca");
        			if(nameImag.size() != 0 && marcaE.size() != 0) {
        				String nombre = toCamelCase(nameImag.attr("alt").toLowerCase());
        				String marca = null;
        				marca = marcasService.obtenerMarcaSegunAlias(marcaE.attr("alt"));
            			if(marca.split(" ").length >= 2 || marca.equals("multipleResult")) {
            				if(marca.equals("multipleResult")) {
	    	            		marca= marcasService.obtenerMarcaSegunAliasSimilar(marca);
	    	            	}
            			} else {
            				marca = marcasService.obtenerMarca(marca);
            			}
	            		if(marca == null || marca == "zapatilla" ) {
	            			marca = "Otro";
	            		}
    	            	String link = "https://www.digitalsport.com.ar/" + pagina + "/" + el.attr("href");
                    	String img = "https://www.digitalsport.com.ar/" + pagina + "/" + nameImag.attr("src");
                        String precioE = el.select("div.precio").text();
                        precioE = precioE.substring(1,precioE.length());
    	            	BigDecimal precio = new BigDecimal(precioE);
                    	ropaGuardar.add(new Ropa(img, nombre, precio, 
                    			marca, null, null, null, link, pagina));
        			}
            	}
            	contPagina++;
            	urlCalzado = "https://www.digitalsport.com.ar/" + pagina + "/prods/" + path + contPagina.toString();
        	} else {
        		break;
        	}
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosAsttonBuenosAires(String path1, String path2) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://shop.asttonbuenosaires.com/" + path1 + contPagina.toString() + path2;
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas1 = document.select("div.grid-row");
            if(entradas1.size() != 0){
            	for (Element elem1 : entradas1) {
            		Elements entradas2 = elem1.select("div.span3.item-container.m-bottom-half");
            		for (Element elem : entradas2) {
            			Elements entradaNombreLinkPrecio = elem.select("div.item-info-container");
    	            	String nombre = entradaNombreLinkPrecio.select("a").attr("title").toString();
    	            	nombre = toCamelCase(nombre.toLowerCase());
    	            	String linkHref = entradaNombreLinkPrecio.select("a").attr("href");
    	            	Elements entradaImagen = elem.select("div.item-image-container");
    	            	String img = entradaImagen.select("a > img").attr("data-srcset");
    	            	img = img.split(" ")[2];
    	            	Elements entradaPrecio = entradaNombreLinkPrecio.select("div.item-price-container.m-none-xs > span.item-price.h6");
    	            	String precio = entradaPrecio.attr("content");
    	            	BigDecimal precioBd = new BigDecimal(precio);
    	            	ropaGuardar.add(new Ropa(img, nombre, precioBd, "Astton Buenos Aires", null, null, null, linkHref, "asttonbuenosaires"));
            		}
	            }
	        	contPagina++;
	        	urlCalzado = "https://shop.asttonbuenosaires.com/" + path1 + contPagina.toString() + path2;
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosCzaro(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://czaro.com.ar/categoria-producto/" + path + contPagina.toString();
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("ul.products.columns-4 > li");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombreLinkImgPrecio = elem.select("a.woocommerce-LoopProduct-link.woocommerce-loop-product__link");
	            	String nombre = entradaNombreLinkImgPrecio.select("h2.woocommerce-loop-product__title").toString();
	            	int indx = nombre.indexOf("\">");
	            	nombre = nombre.substring(indx+2, nombre.length());
	            	indx = nombre.indexOf("<");
	            	nombre = nombre.substring(0,indx);
	            	nombre = nombre.indexOf("#") != -1 ? nombre.substring(3,nombre.length()).trim() : nombre;
	            	nombre = toCamelCase(nombre.toLowerCase());
	            	String linkHref = entradaNombreLinkImgPrecio.attr("href");
	            	Elements entradaImagen = entradaNombreLinkImgPrecio.select("img");
	            	String img = entradaImagen.attr("src");
	            	Elements entradaPrecio = entradaNombreLinkImgPrecio.select("span.price > span.woocommerce-Price-amount.amount");
	            	String precio = entradaPrecio.first().toString();
	            	indx = precio.indexOf("$");
	            	precio = precio.substring(indx+8);
            		int indx2 = precio.indexOf("<");
            		precio = precio.substring(0,indx2);
            		precio = precio.replace(".","");
            		precio = precio.replace(",",".");
	            	BigDecimal precioBd = new BigDecimal(precio);
	            	ropaGuardar.add(new Ropa(img, nombre, precioBd, "Czaro", null, null, null, linkHref, "czaro"));
	            }
	        	contPagina++;
	        	urlCalzado = "https://czaro.com.ar/categoria-producto/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	/*
	 * ***********************************
	 *  SOLO HOMBRES
	 * ***********************************
	 */
	
	@Override
	public List<Ropa> obtenerProductosValkymia(String path, String tipo) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		String urlCalzado = "https://valkymia.com/" + path;
		if (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("div.category-products > ul > li");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombre = elem.select("h2.product-name > a");
	            	String nombre = entradaNombre.attr("title");
	            	nombre = toCamelCase(tipo) + " Valkymia " + toCamelCase(nombre.toLowerCase());
	            	Elements entradaImagen = elem.select("a.product-image");
	            	Elements imagen = entradaImagen.select("img");
	            	String img = imagen.attr("src").trim();
	            	Elements entradaPrecio = elem.select("div.price-box > p.special-price");
	            	if(entradaPrecio.size() == 0) {
	            		entradaPrecio = elem.select("div.price-box > p.old-price");
	            	}
	            	Elements precio = entradaPrecio.select("span.price");
	            	String precioS = precio.get(0).text();
	            	BigDecimal precioBd = new BigDecimal(precioS.substring(1, precioS.length()));
	            	Element link = elem.select("h2.product-name > a").first();
	            	String linkHref = link.attr("href");
	            	ropaGuardar.add(new Ropa(img, nombre, precioBd, "Valkymia", null, null, null, linkHref, "valkymia"));
	            }
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosPanther(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.panther.com.ar/index.php/" + path + contPagina.toString();
		Ropa ropa = null;
		int cont = 0;
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("div.category-products > ul > li");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombre = elem.select("h2.product-name");
	            	String nombre = entradaNombre.select("a").attr("title").toString();
	            	nombre = toCamelCase(nombre.toLowerCase());
	            	final String name = nombre;
	            	Optional<Ropa> matchingObject = ropaGuardar.stream().filter(r->r.getNombre().equals(name)).findFirst();
	            	ropa = matchingObject.orElse(null);
	            	if(ropa != null){
	            		cont++;
	            		break;
	            	}
	            	String linkHref = entradaNombre.select("a").attr("href");
	            	Elements entradaImagen = elem.select("div.product-hover");
	            	String img = entradaImagen.select("img.img-responsive").attr("src").trim();
	            	Elements entradaPrecio = elem.select("div.price-box");
	            	String precio = entradaPrecio.select("span.price").text();
	            	if(precio.indexOf(".") != -1) {
	            		String[] precioArr = precio.split("\\.");
		            	precio = precioArr[0]+precioArr[1];
	            	}
	            	precio = precio.substring(2,precio.length()).replace(',','.');
	            	BigDecimal precioBd = new BigDecimal(precio);
	            	ropaGuardar.add(new Ropa(img, nombre, precioBd, "Panther", null, null, null, linkHref, "panther"));
	            }
            	if(ropa != null && cont == 10){
            		break;
            	}
	        	contPagina++;
	        	urlCalzado = "https://www.panther.com.ar/index.php/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosDorian(String path1, String path2) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		int contPagina = 1;
		String urlCalzado = "https://www.dorianargentina.com/" + path1 + contPagina + path2;
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas1 = document.select("div.product-row");
            if(entradas1.size() != 0){
            	for (Element elem1 : entradas1) {
            		Elements entradas2 = elem1.select("div.product-item.dest-gral ");
            		for(Element elem : entradas2) {
            			Elements entradaNombreLinkPrecio = elem.select("div.bajada");
    	            	String nombre = entradaNombreLinkPrecio.select("div.title > h3 > a").attr("title");
    	            	nombre = toCamelCase(nombre.toLowerCase());
    	            	String linkHref = entradaNombreLinkPrecio.select("div.title > h3 > a").attr("href");
    	            	Elements entradaImagen = elem.select("div.head");
    	            	String img = entradaImagen.select("img").attr("src").trim();
    	            	Elements entradaPrecio = entradaNombreLinkPrecio.select("div.price");
    	            	String precio = entradaPrecio.select("span.price").attr("content");
    	            	BigDecimal precioBd = new BigDecimal(precio);
    	            	ropaGuardar.add(new Ropa(img, nombre, precioBd, "Dorian", null, null, null, linkHref, "dorian"));
            		}
	            }
            	contPagina++;
            	urlCalzado = "https://www.dorianargentina.com/" + path1 + contPagina + path2;
            } else {
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosColantuono(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "http://colantuono.com.ar/categoria-producto/" + path + contPagina.toString();
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("ul.products.grid > li");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombrePrecioLink = elem.select("div.desc");
	            	String nombre =  entradaNombrePrecioLink.select("h4 > a").toString();
	            	if(nombre.length() == 0) {
	            		break;
	            	}
	            	int indx = nombre.indexOf("\">");
	            	nombre = nombre.substring(indx+2, nombre.length());
	            	indx = nombre.indexOf("<");
	            	nombre = nombre.substring(0,indx);
	            	nombre = toCamelCase(nombre.toLowerCase());
	            	String linkHref = entradaNombrePrecioLink.select("h4 > a").attr("href");
	            	Elements entradaImagen = elem.select("div.hover_box.hover_box_product > a > div.hover_box_wrapper");
	            	String img = entradaImagen.select("img.visible_photo.scale-with-grid.wp-post-image").attr("src").trim();
	            	Elements entradaPrecio = entradaNombrePrecioLink.select("span.price");
	            	String precioE = "0";
	            	String precioAux = entradaPrecio.select("span.woocommerce-Price-amount.amount").toString();
            		int index = precioAux.indexOf("$");
            		precioE = precioAux.substring(index+8);
            		int index2 = precioE.indexOf("<");
            		precioE = precioE.substring(0,index2);
	            	String[] precioArr;
	            	String precioS = "0";
	            	if(precioE.indexOf(",") != -1) {
	            		precioArr = precioE.split("\\,");
	            		precioS = (precioArr[0]+precioArr[1]).replace(',','.');
	            	} else{
	            		precioS = precioE.replace(',','.');
	            	}
	            	BigDecimal precio = new BigDecimal(precioS);
	            	ropaGuardar.add(new Ropa(img, nombre, precio, "Colantuono", null, null, null, linkHref, "colantuono"));
	            }
	        	contPagina++;
	        	urlCalzado = "http://colantuono.com.ar/categoria-producto/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosGuante(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.guante.com.ar/" + path + contPagina.toString();
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("div.woocommerce.columns-3 > ul > li");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombre = elem.select("a > h2.woocommerce-loop-product__title");
	            	String nombre = entradaNombre.toString();
	            	if(nombre.length() == 0) {
	            		break;
	            	}
	            	int indx = nombre.indexOf("\">");
	            	nombre = nombre.substring(indx+2, nombre.length());
	            	indx = nombre.indexOf("<");
	            	nombre = nombre.substring(0,indx);
	            	nombre = toCamelCase(nombre.toLowerCase());
	            	String linkHref = elem.select("a").attr("href");
	            	Elements entradaImagen = elem.select("div.orise-store-product-image-container.progression-studios-shop-image-scale");
	            	String img = entradaImagen.select("img.attachment-shop_catalog.size-shop_catalog.wp-post-image").attr("src").trim();
	            	Elements entradaPrecio = elem.select("a > span.price");
	            	Elements entradaPrecio2 = entradaPrecio.select("ins");
	            	String precioE = "0";
	            	if(entradaPrecio2.size() != 0) {
	            		String precioAux = entradaPrecio2.select("span.woocommerce-Price-amount.amount").toString();
	            		int index = precioAux.indexOf("$");
	            		precioE = precioAux.substring(index+8);
	            		int index2 = precioE.indexOf("<");
	            		precioE = precioE.substring(0,index2);
	            	} else {
	            		String precioAux = entradaPrecio.select("span.woocommerce-Price-amount.amount").toString();
	            		int index = precioAux.indexOf("$");
	            		precioE = precioAux.substring(index+8);
	            		int index2 = precioE.indexOf("<");
	            		precioE = precioE.substring(0,index2);
	            	}
	            	String[] precioArr;
	            	String precioS = "0";
	            	if(precioE.indexOf(".") != -1) {
	            		precioArr = precioE.split("\\.");
	            		precioS = (precioArr[0]+precioArr[1]).replace(',','.');
	            	} else{
	            		precioS = precioE.replace(',','.');
	            	}
	            	BigDecimal precio = new BigDecimal(precioS);
	            	ropaGuardar.add(new Ropa(img, nombre, precio, "Guante", null, null, null, linkHref, "guante"));
	            }
	        	contPagina++;
	        	urlCalzado = "https://www.guante.com.ar/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosAndez(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://andezoficial.com.ar/" + path + contPagina.toString();
		while (getStatusConnectionCode(urlCalzado) == 200) {
			Document document = getHtmlDocument(urlCalzado);
        	Elements entradas = document.select("ul.products.columns-4 > li");       
        	if(entradas.size() != 0) {
        		for(Element el : entradas) {
        			Elements nombreE = el.select("h3.woocommerce-loop-product__title > a");
        			String nombre = nombreE.get(0).toString();
        			int indx = nombre.indexOf("\">");
	            	nombre = nombre.substring(indx+2, nombre.length());
	            	indx = nombre.indexOf("<");
	            	nombre = nombre.substring(0,indx);
	            	nombre = toCamelCase(nombre.toLowerCase()).trim();
	            	String link = nombreE.attr("href");
                	String img = el.select("div.product-header > a > img").attr("src");
                    String precioE = el.select("span.price").text();
                    precioE = precioE.substring(1,precioE.length());
                    String[] precioArr;
	            	String precioS = "0";
	            	if(precioE.indexOf(".") != -1) {
	            		precioArr = precioE.split("\\,");
	            		precioS = (precioArr[0]+precioArr[1]);
	            	} else{
	            		precioS = precioE;
	            	}
	            	BigDecimal precio = new BigDecimal(precioS);
                	ropaGuardar.add(new Ropa(img, nombre, precio, 
                			"Andez", null, null, null, link, "andez"));
            	}
            	contPagina++;
            	urlCalzado = "https://andezoficial.com.ar/" + path + contPagina.toString();
        	} else {
        		break;
        	}
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosOggi(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.oggi.com.ar/" + path + contPagina.toString();
        while (getStatusConnectionCode(urlCalzado) == 200) {
        	Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("div.row.products-category-grid > div.masonry-grid > div.product-item.col-xs-6.col-md-4");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Element entradaImgLink = elem.select("div.product-upper > a").first();
            		Element entradaNombreprecio = elem.select("div.product-info").first();
            		if(entradaImgLink != null &&  entradaNombreprecio != null){
            			String img = entradaImgLink.select("img").attr("src");
            			String linkHref = entradaImgLink.attr("href");
            			String nombre = entradaNombreprecio.select("div.product-title > h5 > a").attr("title");
    	            	nombre = toCamelCase(nombre.toLowerCase());
    	            	String precioE = entradaNombreprecio.select("div.product-price").select("h4.new-price").first().text();
    	            	precioE = precioE.substring(1,precioE.length());
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
    	            	ropaGuardar.add(new Ropa(img, nombre, precio, "Oggi", null, null, null , linkHref, "oggi"));
            		}
	            }
            	contPagina++;
            	urlCalzado = "https://www.oggi.com.ar/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosGiardini(String path1, String path2) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://giardini.mitiendanube.com/" + path1 + contPagina.toString() + path2;
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas1 = document.select("div.product-row.row");
            if(entradas1.size() != 0){
            	for (Element elem1 : entradas1) {
            		Elements entradas2 = elem1.select("div.js-masonry-item.item-container.col-xs-6.col-sm-4.col-md-3.col-lg-3");
            		if(entradas2.size() != 0){
                    	for (Element elem : entradas2) {
		            		Elements entradaNombreLinkPrecio = elem.select("div.item-info-container.clear-both");
			            	String nombre = entradaNombreLinkPrecio.select("a").attr("title").toString();
			            	nombre = toCamelCase(nombre.toLowerCase());
			            	String linkHref = entradaNombreLinkPrecio.select("a").attr("href");
			            	Elements entradaImagen = elem.select("div.item-image-container.pull-left.p-relative");
			            	String img = entradaImagen.select("a > img").attr("data-srcset");
			            	img = img.split(" ")[4];
			            	Elements entradaPrecio = entradaNombreLinkPrecio.select("span.item-price.d-inline-block.m-top-none.m-bottom-none");
			            	String precio = entradaPrecio.attr("content");
			            	BigDecimal precioBd = new BigDecimal(precio);
			            	ropaGuardar.add(new Ropa(img, nombre, precioBd, "Giardini", null, null, null, linkHref, "giardini"));
                    	}
            		}
	            }
	        	contPagina++;
	        	urlCalzado = "https://giardini.mitiendanube.com/" + path1 + contPagina.toString() + path2;
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosPuertoBlue(String path1, String path2) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://puertoblue.mitiendanube.com/" + path1 + contPagina.toString() + path2;
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas1 = document.select("div.grid-row");
            if(entradas1.size() != 0){
            	for (Element elem1 : entradas1) {
            		Elements entradas2 = elem1.select("div.span3.item-container.m-bottom-half");
            		for (Element elem : entradas2) {
            			Elements entradaNombreLinkPrecio = elem.select("div.item-info-container");
    	            	String nombre = entradaNombreLinkPrecio.select("a").attr("title").toString();
    	            	nombre = toCamelCase(nombre.toLowerCase());
    	            	String linkHref = entradaNombreLinkPrecio.select("a").attr("href");
    	            	Elements entradaImagen = elem.select("div.item-image-container");
    	            	String img = entradaImagen.select("a > img").attr("data-srcset");
    	            	img = img.split(" ")[2];
    	            	Elements entradaPrecio = entradaNombreLinkPrecio.select("div.item-price-container.m-none-xs > span.item-price.h6");
    	            	String precio = entradaPrecio.attr("content");
    	            	BigDecimal precioBd = new BigDecimal(precio);
    	            	ropaGuardar.add(new Ropa(img, nombre, precioBd, "Puerto Blue", null, null, null, linkHref, "puertoblue"));
            		}
	            }
	        	contPagina++;
	        	urlCalzado = "https://puertoblue.mitiendanube.com/" + path1 + contPagina.toString() + path2;
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosPriamoitaly(String path1, String path2) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://shop.priamoitaly.com/" + path1 + contPagina.toString() + path2;
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas1 = document.select("div.grid-row");
            if(entradas1.size() != 0){
            	for (Element elem1 : entradas1) {
            		Elements entradas2 = elem1.select("div.span3.item-container.m-bottom-half");
            		for (Element elem : entradas2) {
            			Elements entradaNombreLinkPrecio = elem.select("div.item-info-container");
    	            	String nombre = entradaNombreLinkPrecio.select("a").attr("title").toString();
    	            	nombre = toCamelCase(nombre.toLowerCase());
    	            	String linkHref = entradaNombreLinkPrecio.select("a").attr("href");
    	            	Elements entradaImagen = elem.select("div.item-image-container");
    	            	String img = entradaImagen.select("a > img").attr("data-srcset");
    	            	img = img.split(" ")[2];
    	            	Elements entradaPrecio = entradaNombreLinkPrecio.select("div.item-price-container.m-none-xs > span.item-price.h6");
    	            	String precio = entradaPrecio.attr("content");
    	            	BigDecimal precioBd = new BigDecimal(precio);
    	            	ropaGuardar.add(new Ropa(img, nombre, precioBd, "Priamo Italy", null, null, null, linkHref, "priamoitaly"));
            		}
	            }
	        	contPagina++;
	        	urlCalzado = "https://shop.priamoitaly.com/" + path1 + contPagina.toString() + path2;
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosItaloCalzados(String path1, String path2) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.italocalzados.com.ar/" + path1 + contPagina.toString() + path2;
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas = document.select("div.item");
            if(entradas.size() != 0){
            	for (Element elem : entradas) {
            		Elements entradaNombreLinkPrecio = elem.select("div.item-info-container");
	            	String nombre = entradaNombreLinkPrecio.select("div.title > a").attr("title").toString();
	            	nombre = toCamelCase(nombre.toLowerCase());
	            	String linkHref = entradaNombreLinkPrecio.select("div.title > a").attr("href");
	            	Elements entradaImagen = elem.select("div.item-image-container");
	            	String img = entradaImagen.select("a > img").attr("data-srcset");
	            	img = img.split(" ")[2];
	            	String entradaPrecio = entradaNombreLinkPrecio.select("div.item-price-container.price.m-none-xs > span.price.item-price.p-left-quarter").toString();
	            	int index1 = entradaPrecio.indexOf("$");
	            	entradaPrecio = entradaPrecio.substring(index1+1, entradaPrecio.length());
	            	int index2 = entradaPrecio.indexOf("<");
	            	entradaPrecio = entradaPrecio.substring(0,index2);
	            	entradaPrecio = entradaPrecio.replace(".", "");
	            	entradaPrecio = entradaPrecio.replaceAll(",", ".");
	            	BigDecimal precio = new BigDecimal(entradaPrecio.trim());
	            	ropaGuardar.add(new Ropa(img, nombre, precio, "Italocalzados", null, null, null, linkHref, "italocalzados"));
            	}
	        	contPagina++;
	        	urlCalzado = "https://www.italocalzados.com.ar/" + path1 + contPagina.toString() + path2;
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosJaquealrey(String path) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.jaquealrey.com.ar/" + path + contPagina.toString();
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas1 = document.select("div.js-product-table.pull-left.row-fluid.m-bottom > div.grid-row");
            if(entradas1.size() != 0){
            	for (Element elem1 : entradas1) {
            		Elements entradas2 = elem1.select("div.span3.item-container.m-bottom-half");
            		for (Element elem : entradas2) {
            			Elements entradaNombreLinkImg = elem.select("div.item-image-container");
    	            	String nombre = entradaNombreLinkImg.select("div.p-relative > a").attr("title").toString();
    	            	nombre = toCamelCase(nombre.toLowerCase());
    	            	String linkHref = entradaNombreLinkImg.select("div.p-relative > a").attr("href");
    	            	String img = entradaNombreLinkImg.select("div.p-relative > a > img").attr("data-srcset");
    	            	img = img.split(" ")[2];
    	            	String entradaPrecio = elem.select("div.item-info-container > div.item-price-container.m-none-xs > span.item-price.h6").toString();
    	            	int index1 = entradaPrecio.indexOf("$");
    	            	entradaPrecio = entradaPrecio.substring(index1+1, entradaPrecio.length());
    	            	int index2 = entradaPrecio.indexOf("<");
    	            	entradaPrecio = entradaPrecio.substring(0,index2);
    	            	entradaPrecio = entradaPrecio.replace(".", "");
    	            	entradaPrecio = entradaPrecio.replaceAll(",", ".");
    	            	BigDecimal precio = new BigDecimal(entradaPrecio.trim());
    	            	ropaGuardar.add(new Ropa(img, nombre, precio, "Jaquealrey", null, null, null, linkHref, "jaquealrey"));
            		}
            	}
	        	contPagina++;
	        	urlCalzado = "https://www.jaquealrey.com.ar/" + path + contPagina.toString();
            }else{
            	break;
            }
        }
        return ropaGuardar;
	}
	
	@Override
	public List<Ropa> obtenerProductosFedericomarconcini(String path1, String path2) throws ServiceException {
		List<Ropa> ropaGuardar = new ArrayList<Ropa>();
		Integer contPagina = 1;
		String urlCalzado = "https://www.federicomarconcini.com.ar/" + path1 + contPagina.toString() + path2;
		while (getStatusConnectionCode(urlCalzado) == 200) {
            Document document = getHtmlDocument(urlCalzado);
            Elements entradas1 = document.select("div.product-row");
            if(entradas1.size() != 0){
            	for (Element elem1 : entradas1) {
            		Elements entradas2 = elem1.select("div.product-item.dest-gral");
            		for (Element elem : entradas2) {
            			Elements entradaImagen = elem.select("div.head");
    	            	Elements sinStock = entradaImagen.select("a > div.out-stock-img");
    	            	if(sinStock.size() == 0) {
    	            		Elements entradaNombreLinkPrecio = elem.select("div.bajada");
        	            	String nombre = entradaNombreLinkPrecio.select("div.title > h3 > a").attr("title").toString();
        	            	nombre = toCamelCase(nombre.toLowerCase());
        	            	String linkHref = entradaNombreLinkPrecio.select("div.title > h3 > a").attr("href");
    	            		String img = entradaImagen.select("a > img").attr("src");
        	            	Elements entradaPrecio = entradaNombreLinkPrecio.select("div.price > span.price");
        	            	String precio = entradaPrecio.attr("content");
        	            	BigDecimal precioBd = new BigDecimal(precio);
        	            	ropaGuardar.add(new Ropa(img, nombre, precioBd, "Federico Marconcini", null, null, null, linkHref, "federicomarconcini"));
    	            	}
            		}
	            }
	        	contPagina++;
	        	urlCalzado = "https://www.federicomarconcini.com.ar/" + path1 + contPagina.toString() + path2;
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
