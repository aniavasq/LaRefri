package com.larefri;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.util.Log;
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

/*********************************************************************
 * Parse support*/
class Store{
	private String id;
	private String name;
	private Category category;
	private Integer priority;
	private ParseFile image;
	private InputStream imageInputStream;
	private ArrayList<Locale> locales;

	public Store() { }

	public Store(ParseObject parseObject){
		this.id = parseObject.getObjectId();
		this.name = parseObject.getString("name");
		this.category = new Category(parseObject.getParseObject("category"));
		this.priority = parseObject.getInt("priority");
		this.image = parseObject.getParseFile("image");
		this.locales = new ArrayList<Locale>();
	}

	public String getName(){
		return this.name;
	}

	public Category getCategory(){
		return this.category;
	}

	public int getPriority(){
		return this.priority;
	}

	public void setImageInputStream(){
		image.getDataInBackground(new GetDataCallback() {

			@Override
			public void done(byte[] data, ParseException e) {
				if (e == null) {
					imageInputStream = new ByteArrayInputStream(data);
					//download image
				}else{
					Log.e("ERROR", e+"", e);
				}
			}
		});
	}
	
	public InputStream getImageInputStream() {
		return imageInputStream;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	public String getId() {
		return id;
	}

	public ArrayList<Locale> getLocales() {
		return locales;
	}

	public void setLocales() {
		ParseQuery<ParseObject> innerQuery = new ParseQuery<ParseObject>("Store");
		innerQuery.whereEqualTo("objectId",this.getId());
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Locale");
		query.whereMatchesQuery("store", innerQuery);
		query.findInBackground(new FindCallback<ParseObject>() {
		    public void done(List<ParseObject> result, ParseException e) {
		        if (e == null) {
		            for (ParseObject parseObject: result){
		            	locales.add(new Locale(parseObject));
		            	Log.e("LOCALE",(new Locale(parseObject)).toString());
		            }
		        }
		    }
		});
	}
}

class Locale{
	private String id;
	private Date updatedAt;
	private String name;
	private List<String> phones;
	private String country, region, city, address;
	private String service;
	private Integer state;
	
	public Locale() {	}
	
	public Locale(ParseObject parseObject) {
		this.id = parseObject.getObjectId();
		this.updatedAt = parseObject.getDate("updatedAt");
		this.name = parseObject.getString("name");
		this.phones = parseObject.getList("phones");
		this.country = parseObject.getString("country");
		this.region = parseObject.getString("region");
		this.city = parseObject.getString("city");
		this.address = parseObject.getString("address");
		this.service = parseObject.getString("service");
		this.state = parseObject.getInt("state");
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}
	public List<String> getPhones() {
		return phones;
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getCountry() {
		return country;
	}
	public String getRegion() {
		return region;
	}
	public String getCity() {
		return city;
	}
	public String getAddress() {
		return address;
	}
	public String getService() {
		return service;
	}
	public Integer getState() {
		return state;
	}
	@Override
	public String toString() {
		return this.phones.toString();
	}
}
/**********************************************************************/

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
