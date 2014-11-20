package com.larefri;

import java.util.Comparator;

import android.view.ContextThemeWrapper;
import android.widget.Button;

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

class FridgeMagnetButton extends Button{
	private FridgeMagnet fm;
	
	static class FridgeMagnetButtonComparator implements Comparator<FridgeMagnetButton>{
		@Override
		public int compare(FridgeMagnetButton lhs, FridgeMagnetButton rhs) {
			int result;
			Integer lhs_top = lhs.getFm().imantado_busqueda_top, rhs_top = rhs.getFm().imantado_busqueda_top;
			
			result = lhs_top.compareTo(rhs_top);
			if(result!=0) return -1*result;
			
			result = lhs.getFm().nombre.compareTo(rhs.getFm().nombre);
			return result;
		}
	}

	public FridgeMagnetButton(ContextThemeWrapper themeWrapper, FridgeMagnet fm){
		super(themeWrapper);
		this.fm = fm;
	}

	public FridgeMagnet getFm() {
		return fm;
	}
}
