package com.larefri;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

public class UpdateFridgeMagnetsListener extends RestClient {
	private Activity master;
	private SharedPreferences settings;
	
	public UpdateFridgeMagnetsListener(Activity master) {
		super();
		this.master = master;
		settings = this.master.getSharedPreferences("LaRefriPrefsFile", 0);
	}

	@Override
	protected Object doInBackground(Object... params) {
		Object result = super.doInBackground(params);
		if(result != null && result.toString().equalsIgnoreCase("{\"codigo\":0}")){
			SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.getDefault());
			String now = df.format(new Date());
			settings.edit().putString("last_update", now).commit();
		}
		//Log.e("RESULT",result.toString());
		return result;
	}	
}
