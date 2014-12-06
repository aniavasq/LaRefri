package com.larefri;

import com.parse.ParseFile;
import com.parse.ParseObject;

public class CategoryFM {
/**
 * {
id_categoria: "1",
nombre_categoria: "bebidas",
icono_categoria: "bebidas.png",
estado: "1"
},
 */
	String id_categoria;
	String nombre_categoria;
	String icono_categoria;
	Integer estado;
}
/*********************************************************************
 * Parse support*/
class Category{
	private String id;
	private String name;
	private ParseFile image;
	
	public Category() { }
	
	public Category(ParseObject parseObject) {
		this.id = parseObject.getObjectId();
	}

	public String getName(){
		return this.name;
	}
	
	public ParseFile getImage(){
		return this.image;
	}

	public String getId() {
		return id;
	}
}
/**********************************************************************/