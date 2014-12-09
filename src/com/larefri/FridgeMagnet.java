package com.larefri;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

import android.content.Context;
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
	private byte[] imageInputStream;
	private List<Local> locales;
	private String logo;
	private Integer index;
	private ParseObject parseObject;
	private Context context;

	public Store(){ }

	public Store(ParseObject parseObject, Context context){
		this.id = parseObject.getObjectId();
		this.name = parseObject.getString("name");
		this.category = new Category(parseObject.getParseObject("category"));
		this.priority = parseObject.getInt("priority");
		this.image = parseObject.getParseFile("image");
		this.locales = new ArrayList<Local>();
		this.parseObject = parseObject;
		this.context = context;
		this.logo = this.name+".png";
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

	public void downloadImage(){
		File imgFile = new File(context.getFilesDir(), this.getLogo());
		if(!imgFile.exists()){
			image.getDataInBackground(new GetDataCallback() {

				@Override
				public void done(byte[] data, ParseException e) {
					if (e == null) {
						imageInputStream = data;
						//download image
						logo = getName()+".png";

						FileOutputStream out = null;
						InputStream in = new ByteArrayInputStream(imageInputStream);
						try {
							out = context.openFileOutput(logo, Context.MODE_PRIVATE);
							MainActivity.CopyStream(in, out);
							out.flush();
							out.close();
							out = null;
						} catch(IOException doNotCare) { 
							Log.e("ERROR", "Error writing files"+logo);
						}

						Log.e("FILE", image.getName());
					}else{
						Log.e("ERROR", e+"", e);
					}
				}
			});
		}
	}

	public byte[] getImageInputStream() {
		return imageInputStream;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	public String getId() {
		return id;
	}

	public List<Local> getLocales() {
		return locales;
	}

	public void downloadLocales() {
		ParseQuery<ParseObject> innerQuery = new ParseQuery<ParseObject>("Store");
		innerQuery.whereEqualTo("objectId",this.getId());

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Locale");
		query.whereMatchesQuery("store", innerQuery);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> result, ParseException e) {
				if (e == null) {
					for (ParseObject parseObject: result){
						Local locale = new Local(parseObject);
						locales.add(locale);
						parseObject.pinInBackground();
					}
				}
			}
		});
	}

	public String getLogo(){
		return this.logo;
	}

	public Integer getIndex() {
		if(index==null){
			try{
				setIndex(this.parseObject.getInt("index"));
				return index;
			}catch (Exception e) {
				return -1;
			}
		}else{
			return index;
		}
	}

	public void setIndex(Integer index) {
		this.index = index;
		this.parseObject.put("index", this.index);
	}

	public void saveToLocalDataStore(){
		if(getIndex()==-1 || getIndex()==0) setIndex(this.id.hashCode());
		Log.e("INDEX", ""+getIndex() );
		this.parseObject.pinInBackground();
		this.downloadImage();
		this.downloadLocales();
	}

	public void removeFromLocalDataStore(){
		this.parseObject.unpinInBackground();
	}
	@Override
	public boolean equals(Object o) {
		return this.getId().equals(((Store)o).getId());
	}
}

class Local{
	private String id;
	private Date updatedAt;
	private String name;
	private List<String> phones;
	private String country, region, city, address;
	private String service;
	private Integer state;

	public Local() {	}

	public Local(ParseObject parseObject) {
		this.id = parseObject.getObjectId();
		this.updatedAt = parseObject.getDate("updatedAt");
		this.name = parseObject.getString("name");
		this.country = parseObject.getString("country");
		this.region = parseObject.getString("region");
		this.city = parseObject.getString("city");
		this.address = parseObject.getString("address");
		this.service = parseObject.getString("service");
		this.state = parseObject.getInt("state");
		this.phones = new ArrayList<String>();
		setPhones(parseObject);
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}	
	public List<String> getPhones() {
		List<String> result = new ArrayList<String>();
		for(String phone: phones){
			result.add(phone);
		}
		return result;
	}
	public void setPhones(ParseObject parseObject){
		List<String> tmp_phones = parseObject.getList("phones");
		phones = tmp_phones != null? tmp_phones : new ArrayList<String>();
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

class PhoneNumber{
	String number;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
}
/**********************************************************************/

class FridgeMagnetButton extends Button{
	private Store fm;

	static class FridgeMagnetButtonComparator implements Comparator<FridgeMagnetButton>{
		@Override
		public int compare(FridgeMagnetButton lhs, FridgeMagnetButton rhs) {
			int result;
			Integer lhs_top = lhs.getFm().getPriority(), rhs_top = rhs.getFm().getPriority();

			result = lhs_top.compareTo(rhs_top);
			if(result!=0) return -1*result;

			result = lhs.getFm().getName().compareTo(rhs.getFm().getName());
			return result;
		}
	}

	public FridgeMagnetButton(ContextThemeWrapper themeWrapper, Store fm){
		super(themeWrapper);
		this.fm = fm;
	}

	public Store getFm() {
		return fm;
	}
}
