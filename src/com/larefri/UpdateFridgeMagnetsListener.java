package com.larefri;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

public class UpdateFridgeMagnetsListener extends RestClient {
	private Activity master;
	private SharedPreferences settings;
	private List<UpdateMessage> updateMessages;
	private List<FridgeMagnet> fridgeMagnets;

	public UpdateFridgeMagnetsListener(Activity master, List<FridgeMagnet> fridgeMagnets) {
		super();
		this.master = master;
		this.fridgeMagnets = fridgeMagnets;
		settings = this.master.getSharedPreferences("LaRefriPrefsFile", 0);
	}

	@Override
	protected Object doInBackground(Object... params) {
		Object result = super.doInBackground(params);
		UpdateManager updateManager = new UpdateManager();
		try {
			setUpdateMessages(updateManager.readJsonStream( new ByteArrayInputStream(result.toString().getBytes())));
			letsUpdate();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(result != null && !result.toString().equalsIgnoreCase("{\"codigo\":0}")){
			String now = updateMessages.get(updateMessages.size()-1).fecha_actualizacion;
			settings.edit().putString("last_update", now).commit();
		}
		return result;
	}

	private void setUpdateMessages(List<UpdateMessage> updateMessages) {
		this.updateMessages = updateMessages;
	}

	public void letsUpdate(){
		for(UpdateMessage update: updateMessages){
			//
			FridgeMagnet tmpFm = new FridgeMagnet(update.id_marca);
			if(fridgeMagnets.contains(tmpFm)){
				Log.e("UPDATING", fridgeMagnets.get(fridgeMagnets.indexOf(tmpFm)).toString());
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("id_marca", update.id_marca.toString());
				//construct form to HttpRequest
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("id_marca", update.id_marca.toString()));		
				switch (update.tipo_actualizacion) {
				case SUCURSAL:
					(new DownloadMagnetStoresRestClient(master)).execute(
							StaticUrls.SUCURSALES_URL, 
							params,
							nameValuePairs,
							tmpFm);
					break;
				case FLYER:
					(new DownloadFlyerRestClient(master)).execute(
							StaticUrls.FLYER_PROMO, 
							params,
							nameValuePairs,
							tmpFm);
					break;
				default:
					break;
				}
			}
		}
	}
}

enum UpdateType{
	SUCURSAL, FLYER;
}

class UpdateManager implements RefriJsonReader{

	@Override
	public List<UpdateMessage> readJsonStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		}catch(Exception e){
			return null;
		}finally{
			reader.close();
		}
	}

	@Override
	public List<UpdateMessage> readMessagesArray(JsonReader reader) throws IOException {
		List<UpdateMessage> messages = new ArrayList<UpdateMessage>();

		if (reader.peek() != JsonToken.BEGIN_ARRAY) {
			return messages;
		} else {
			reader.beginArray();
			while (reader.hasNext()) {
				UpdateMessage um = readUpdateMessage(reader);
				if(um!=null) messages.add(um);
			}
		}
		reader.endArray();
		return messages;
	}

	private UpdateMessage readUpdateMessage(JsonReader reader) throws IOException {
		UpdateMessage updateMessage = new UpdateMessage();

		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if(name.equalsIgnoreCase("id_marca")){
				updateMessage.id_marca = reader.nextInt();
			}else if(name.equalsIgnoreCase("tipo_actualizacion")){
				updateMessage.tipo_actualizacion = UpdateType.valueOf(reader.nextString());
			}else if(name.equalsIgnoreCase("fecha_actualizacion")){
				updateMessage.fecha_actualizacion = reader.nextString();
			}else{
				updateMessage = null;
				reader.skipValue();
			}
		}
		reader.endObject();
		return updateMessage;
	}
}

class UpdateMessage{
	Integer id_marca;
	UpdateType tipo_actualizacion;
	String fecha_actualizacion;
}