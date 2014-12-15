package com.larefri;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

public class UpdateFridgeMagnetsListener {
	private Activity master;
	private static Context context;
	private static SharedPreferences settings;
	private static UpdateFridgeMagnetsListener INSTANCE;

	private UpdateFridgeMagnetsListener(Activity master) {
		super();
		this.master = master;
		//this.fridgeMagnets = fridgeMagnets;
		context = master;
		settings = this.master.getSharedPreferences(MainActivity.PREFS_NAME, 0);
	}
	
	private static void createInstance(Activity master){
		if (INSTANCE == null){
			INSTANCE = new UpdateFridgeMagnetsListener(master);
		}else{
			INSTANCE.master = master;
		}
	}

	public static UpdateFridgeMagnetsListener getInstance(Activity master){
		createInstance(master);
		return INSTANCE;
	}
	
	public static void update(Activity master) {
		/*try {
			if(result != null){
				letsUpdate();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(result != null && !result.toString().equalsIgnoreCase("{\"codigo\":0}")){
			String now = updateMessages.get(updateMessages.size()-1).fecha_actualizacion;
			settings.edit().putString("last_update", now).commit();
		}
		return result;*/
		getInstance(master);
		
		try {
			updatePromotions();
			updateLocales();
		}catch (java.text.ParseException ex) {
			Log.e("ERROR", ex.getMessage(), ex);
		}
	}

	private static void updatePromotions() throws java.text.ParseException {
		String[] ids_marca = MainActivity.getMyFridgeMagnetsId();
		ParseQuery<ParseObject> innerQuery = new ParseQuery<ParseObject>("Store");
		innerQuery.fromLocalDatastore();
		innerQuery.whereContainedIn("objectId", Arrays.asList(ids_marca));
		SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
		final String now = (new Date()).toString();
		final Date lastUpdate = formatter.parse(settings.getString("last_update", now));

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Promotion");
		query.whereMatchesQuery("store", innerQuery);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> result, ParseException e) {
				if (e == null) {
					for (ParseObject parseObject: result){
						Promotion promotion = new Promotion(parseObject, context);
						if (promotion.getUpdatedAt().compareTo(lastUpdate)>1){
							promotion.downloadImage();
							parseObject.pinInBackground();
							settings.edit().putString("last_update", now).commit();
							Log.e("FLYER UPDATE", promotion.getId());
						}
					}
				}
			}
		});
	}

	private static void updateLocales() throws java.text.ParseException {
		String[] ids_marca = MainActivity.getMyFridgeMagnetsId();
		ParseQuery<ParseObject> innerQuery = new ParseQuery<ParseObject>("Store");
		innerQuery.fromLocalDatastore();
		innerQuery.whereContainedIn("objectId", Arrays.asList(ids_marca));
		SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
		final String now = (new Date()).toString();
		final Date lastUpdate = formatter.parse(settings.getString("last_update", now));

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Locale");
		query.whereMatchesQuery("store", innerQuery);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> result, ParseException e) {
				if (e == null) {
					for (ParseObject parseObject: result){
						Local local = new Local(parseObject);
						if (local.getUpdatedAt().compareTo(lastUpdate)>1){
							parseObject.pinInBackground();
							settings.edit().putString("last_update", now).commit();
							Log.e("LOCAL UPDATE", local.getName());
						}
					}
				}
			}
		});
	}
}