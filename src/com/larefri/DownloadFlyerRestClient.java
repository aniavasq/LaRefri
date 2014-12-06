package com.larefri;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

public class DownloadFlyerRestClient extends RestClient{
	private Activity master;
	
	public DownloadFlyerRestClient(Activity master) {
		super();
		this.master = master;
	}

	@Override
	protected Object doInBackground(Object... params) {
		this.setResult(super.doInBackground(params));
		FridgeMagnet for_add = new FridgeMagnet();
		
		for_add = (FridgeMagnet) params[3];
		InputStream is = new ByteArrayInputStream(getResult().toString().getBytes());
		FlyerManager flyerManager = new FlyerManager();
		try {			
			List<Flyer> flyer = flyerManager.readJsonStream(is);
			for(Flyer f: flyer){
					InputStream imageInputStream=new URL(StaticUrls.FLYER_IMGS+f.imagen).openStream();
			    	FileOutputStream imageOutputStream;
			    	imageOutputStream = master.openFileOutput(f.imagen, Context.MODE_PRIVATE);
			    	MainActivity.CopyStream(imageInputStream, imageOutputStream);
			    	imageOutputStream.close();
			}
			File JsonFile = new File(master.getFilesDir(), for_add.id_marca+"_flyer.json");
			flyerManager.writeJsonStream(new FileOutputStream(JsonFile), flyer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this.getResult();
	}
}

class FlyerManager implements RefriJsonReader, RefriJsonWriter {

	@SuppressWarnings("rawtypes")
	@Override
	public void writeMessagesArray(JsonWriter writer, List objectsList)
			throws IOException {
		writer.beginArray();
	     for (Object f : objectsList) {
	    	 if(f!=null)
	    		 writeFlyer(writer, (Flyer)f);
	     }
	     writer.endArray();	
	}

	private void writeFlyer(JsonWriter writer, Flyer f) throws IOException {
		writer.beginObject();
		writer.name("id_promo").value(f.id_promo.toString());
		writer.name("id_marca").value(f.id_marca.toString());
		writer.name("imagen").value(f.imagen.toString());
		writer.name("info").value(f.info.toString());
		writer.name("fecha_inicio").value(f.fecha_inicio.toString());
		writer.name("fecha_fin").value(f.fecha_fin.toString());
		writer.name("estado").value(f.estado.toString());
	    writer.endObject();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void writeJsonStream(OutputStream out, List objectsList)
			throws IOException {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		writer.setIndent("  ");
		writeMessagesArray(writer, objectsList);
		writer.close();
	}

	@Override
	public List<Flyer> readJsonStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		}finally{
		   reader.close();
		}
	}

	@Override
	public List<Flyer> readMessagesArray(JsonReader reader) throws IOException {
		List<Flyer> messages = new ArrayList<Flyer>();

		if (reader.peek() != JsonToken.BEGIN_ARRAY) {
			return messages;
		} else {
			reader.beginArray();
			while (reader.hasNext()) {
				Flyer flyer = readFlyer(reader);
				if(flyer!=null) messages.add(flyer);
			}
		}
		reader.endArray();
		return messages;
	}
	
	public Flyer readFlyer(JsonReader reader) throws IOException {
		Flyer flyer = new Flyer();
		
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if(name.equalsIgnoreCase("id_promo")){
				flyer.id_promo = reader.nextInt();
			}else if(name.equalsIgnoreCase("id_marca")){
				flyer.id_marca = reader.nextInt();
			}else if(name.equalsIgnoreCase("info")){
				flyer.info = reader.nextString();
			}else if(name.equalsIgnoreCase("imagen")){
				flyer.imagen = reader.nextString();
			}else if(name.equalsIgnoreCase("fecha_inicio")){
				flyer.fecha_inicio = reader.nextString();
			}else if(name.equalsIgnoreCase("fecha_fin")){
				flyer.fecha_fin = reader.nextString();
			}else if(name.equalsIgnoreCase("estado")){
				flyer.estado = reader.nextInt();
			}else{
				flyer = null;
				reader.skipValue();
			}
		}
		reader.endObject();
		return flyer;
	}
	
}

class Flyer{
	Integer id_promo;
	Integer id_marca;
	String info;
	String imagen;
	String fecha_inicio;
	String fecha_fin;
	Integer estado;
}