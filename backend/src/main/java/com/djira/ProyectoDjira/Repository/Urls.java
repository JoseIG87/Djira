package com.djira.ProyectoDjira.Repository;

import java.util.ArrayList;

import com.djira.ProyectoDjira.Dto.PaginaDTO;

public class Urls {
	
	private ArrayList<PaginaDTO> urlArray;

	public Urls() {
		super();
		urlArray = new ArrayList<PaginaDTO>();
		this.urlArray.add(new PaginaDTO("dafiti","https://www.dafiti.com.ar/"));
		this.urlArray.add(new PaginaDTO("netshoes","https://www.netshoes.com.ar/"));
		this.urlArray.add(new PaginaDTO("opensports","http://www.opensports.com.ar/"));
		this.urlArray.add(new PaginaDTO("dexter","http://www.dexter.com.ar/"));
		this.urlArray.add(new PaginaDTO("reebok","http://www.reebok.com.ar/"));
		this.urlArray.add(new PaginaDTO("adidas","https://www.adidas.com.ar/"));
		this.urlArray.add(new PaginaDTO("rusty","https://www.shop.rustyargentina.com/"));
		this.urlArray.add(new PaginaDTO("dorian","https://www.dorianargentina.com/"));
		this.urlArray.add(new PaginaDTO("primeclub","http://www.primeclub.com.ar/"));
		this.urlArray.add(new PaginaDTO("stockcenter", "http://www.stockcenter.com.ar/"));
		this.urlArray.add(new PaginaDTO("redsport", "http://www.redsportonline.com.ar/"));
		this.urlArray.add(new PaginaDTO("falabella", "http://www.falabella.com.ar/"));
		this.urlArray.add(new PaginaDTO("fotter", "http://www.fotter.com.ar/"));
		this.urlArray.add(new PaginaDTO("sportline", "http://www.sportline.com.ar/"));
	}

	public ArrayList<PaginaDTO> getUrlArray() {
		return urlArray;
	}

	public void setUrlArray(ArrayList<PaginaDTO> urlArray) {
		this.urlArray = urlArray;
	}
}
