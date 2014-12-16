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
//import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Button;

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
	private List<Promotion> promotions;
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
		this.promotions = new ArrayList<Promotion>();
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
							//Log.e("ERROR", "Error writing files"+logo);
						}
					}else{
						//Log.e("ERROR", e+"", e);
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
		query.whereEqualTo("country", LocationTask.getCountry());
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
	
	public List<Promotion> getPromotions() {
		return promotions;
	}

	public void downloadPromotions() {
		Date today = new Date();
		ParseQuery<ParseObject> innerQuery = new ParseQuery<ParseObject>("Store");
		innerQuery.whereEqualTo("objectId",this.getId());

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Promotion");
		query.whereMatchesQuery("store", innerQuery);
		query.whereGreaterThan("endDate", today);
		query.whereLessThan("startDate", today);
		query.whereEqualTo("country", LocationTask.getCountry());
		//Log.e("TODAY", today.toString());
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> result, ParseException e) {
				if (e == null) {
					for (ParseObject parseObject: result){
						Promotion promotion = new Promotion(parseObject, context);
						promotion.downloadImage();
						promotions.add(promotion);
						parseObject.pinInBackground();
					}
				}else{
					//Log.e("ERROR", e.getMessage(), e);
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
		if(getIndex()==-1 || getIndex()==0){
			int i = Math.abs(this.id.hashCode());
			//Log.e("I", "i="+i );
			setIndex(i);
		}
		//Log.e("INDEX", ""+getIndex() );
		this.parseObject.pinInBackground();
		this.downloadImage();
		this.downloadLocales();
		this.downloadPromotions();
	}

	public void removeFromLocalDataStore(){
		removeLocales();
		removePromotions();
		this.parseObject.unpinInBackground();
	}
	
	private void removeLocales(){
		for(Local s: getLocales()){
			s.getParseReference().unpinInBackground();
		}
	}
	
	private void removePromotions(){
		for(Promotion p: getPromotions()){
			p.getParseReference().unpinInBackground();
			File flyer = new File(context.getFilesDir(), logo);
			flyer.delete();
		}
	}
	
	@Override
	public boolean equals(Object o) {
		return this.getId().equals(((Store)o).getId());
	}
}
/************************************************************************/
class Local{
	private String id;
	private Date updatedAt;
	private String name;
	private List<String> phones;
	private String country, region, city, address;
	private String service;
	private Integer state;
	private ParseObject parseReference;

	public Local() {	}

	public Local(ParseObject parseObject) {
		this.id = parseObject.getObjectId();
		this.updatedAt = parseObject.getUpdatedAt();//.getDate("updatedAt");
		this.name = parseObject.getString("name");
		this.country = parseObject.getString("country");
		this.region = parseObject.getString("region");
		this.city = parseObject.getString("city");
		this.address = parseObject.getString("address");
		this.service = parseObject.getString("service");
		this.state = parseObject.getInt("state");
		this.parseReference = parseObject;
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
	public ParseObject getParseReference() {
		return parseReference;
	}

	@Override
	public String toString() {
		return this.phones.toString();
	}

	@Override
	public boolean equals(Object o) {
		return this.getId().equals(((Local)o).getId());
	}
}

/**********************************************************************/
class Promotion{
	private String id;
	private ParseFile image;
	private byte[] imageInputStream;
	private String description;
	private Date startDate;
	private Date endDate;
	private Date updatedAt;
	private String country;
	private Date imageDate;
	private Context context;
	private ParseObject parseReference;
	
	public Promotion() { }

	public Promotion(ParseObject parseObject, Context context) {
		this.id = parseObject.getObjectId();
		this.image = parseObject.getParseFile("image");
		this.description = parseObject.getString("description");
		this.updatedAt = parseObject.getUpdatedAt();//.getDate("updatedAt");
		this.startDate = parseObject.getDate("startDate");
		this.endDate = parseObject.getDate("endDate");
		this.country = parseObject.getString("country");
		this.imageDate = parseObject.getDate("imageDate");
		this.parseReference = parseObject;
		this.context = context;
	}

	public byte[] getImageInputStream() {
		return imageInputStream;
	}

	public void setImageInputStream(byte[] imageInputStream) {
		this.imageInputStream = imageInputStream;
	}
	
	public void downloadImage(){
		File imgFile = new File(context.getFilesDir(), this.getName());
		if(!imgFile.exists()){
			image.getDataInBackground(new GetDataCallback() {

				@Override
				public void done(byte[] data, ParseException e) {
					if (e == null) {
						imageInputStream = data;
						//download image
						String name = getName();

						FileOutputStream out = null;
						InputStream in = new ByteArrayInputStream(imageInputStream);
						try {
							out = context.openFileOutput(name, Context.MODE_PRIVATE);
							MainActivity.CopyStream(in, out);
							out.flush();
							out.close();
							out = null;
						} catch(IOException doNotCare) {	}

					}else{
						//Log.e("ERROR", e+"", e);
					}
				}
			});
		}
	}

	public Date getImageDate() {
		return imageDate;
	}

	public void setImageDate(Date imageDate) {
		this.imageDate = imageDate;
	}

	public String getId() {
		return id;
	}

	public ParseFile getImage() {
		return image;
	}

	public String getDescription() {
		return description;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getName() {
		return getId()+".png";
	}

	public String getCountry() {
		return country;
	}

	public ParseObject getParseReference() {
		return parseReference;
	}

	@Override
	public boolean equals(Object o) {
		return this.getId().equals(((Promotion)o).getId());
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
