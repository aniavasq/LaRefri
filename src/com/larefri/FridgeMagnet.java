package com.larefri;

public class FridgeMagnet {

	/**
	 * {
		"id_marca":"17",
		"id_pais":"5",
		"id_categoria":"1",
		"nombre":"Pepsi",
		"logo":"pepsi.png",
		"descripcion":"Pepsi",
		"info":"Pepsi",
		"imantado_default":"0",
		"imantado_busqueda_top":"6",
		"imantado_busqueda_cat":"6",
		"estado":"1"}
	 */
	Integer id_marca;
	Integer id_pais;
	Integer id_categoria;
	String nombre;
	String logo;
	String descripcion;
	String info;
	Integer imantado_default;
	Integer imantado_busqueda_top;
	Integer imantado_busqueda_cat;
	Integer estado;
	Integer pos_x;
	Integer pos_y;
	
	public FridgeMagnet(Integer id_marca) {
		super();
		this.id_marca = id_marca;
	}

	public FridgeMagnet() {
		super();
	}

	@Override
	public boolean equals(Object o) {
		return ((FridgeMagnet) o).id_marca == this.id_marca;
	}
	
	@Override
	public String toString(){
		return this.nombre+" "+this.id_marca;
	}
}
