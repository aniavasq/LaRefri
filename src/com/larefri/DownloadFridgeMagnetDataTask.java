package com.larefri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.util.Log;

public class DownloadFridgeMagnetDataTask{

	//parameters FridgeMagnet and master Activity
	DownloadFridgeMagnetLogoTask downloadFMLogoTask;
	DownloadMagnetStoresRestClient downloadMagnetStoresRestClient;
	DownloadFlyerRestClient downloadFlyerRestClient;

	protected void cancel() {
		Log.e("FM DATA TASK","STOPED");
		if(downloadFMLogoTask != null){
			downloadFMLogoTask.onPostExecute(null);
			downloadFMLogoTask.cancel(true);
		}
		
		if(downloadMagnetStoresRestClient != null) downloadMagnetStoresRestClient.cancel(true);
		if(downloadFlyerRestClient != null) downloadFlyerRestClient.cancel(true);
	}

	protected Void execute(Object... params) {
		Log.e("FM DATA TASK","STARTED");
		FridgeMagnet fm = (FridgeMagnet) params[0];
		Activity master = (Activity) params[1];
		HashMap<String, String> map_params = new HashMap<String, String>();
		map_params.put("id_marca", fm.id_marca.toString());
		try {
			((AddMagnet)master).addMagnetToLocalData(fm);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		//construct form to HttpRequest
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("id_marca", fm.id_marca.toString()));
		downloadFMLogoTask = new DownloadFridgeMagnetLogoTask(master);
		downloadFMLogoTask.execute(
				StaticUrls.FRIDGE_MAGNETS, 
				fm.logo);
		MainActivity.addDownloadFMLogoTasks(downloadFMLogoTask);

		downloadMagnetStoresRestClient = new DownloadMagnetStoresRestClient(master);
		downloadMagnetStoresRestClient.execute(
				StaticUrls.SUCURSALES_URL, 
				map_params,
				nameValuePairs,
				fm);
		downloadFlyerRestClient = new DownloadFlyerRestClient(master);
		downloadFlyerRestClient.execute(
				StaticUrls.FLYER_PROMO, 
				map_params,
				nameValuePairs,
				fm);
		return null;
	}
}
