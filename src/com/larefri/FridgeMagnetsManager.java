package com.larefri;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import android.util.Log;

public class FridgeMagnetsManager implements RefriJsonReader, RefriJsonWriter {
	public List<FridgeMagnet> readJsonStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		}catch(Exception e){
			Log.e("ERROR",e.getMessage(),e);
			return null;
		}finally{
			reader.close();
		}
	}

	public List<FridgeMagnet> readMessagesArray(JsonReader reader) throws IOException {
		List<FridgeMagnet> messages = new ArrayList<FridgeMagnet>();

		if (reader.peek() != JsonToken.BEGIN_ARRAY) {
			return messages;
		} else {
			reader.beginArray();
			while (reader.hasNext()) {
				FridgeMagnet fm = readMagnet(reader);
				if(fm!=null) messages.add(fm);
			}
		}
		reader.endArray();
		return messages;
	}

	public FridgeMagnet readMagnet(JsonReader reader) throws IOException {
		FridgeMagnet fridgeMagnet = new FridgeMagnet();

		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if(name.equalsIgnoreCase("id_marca")){
				fridgeMagnet.id_marca = reader.nextInt();
			}else if(name.equalsIgnoreCase("id_pais")){
				fridgeMagnet.id_pais = reader.nextInt();
			}else if(name.equalsIgnoreCase("id_categoria")){
				fridgeMagnet.id_categoria = reader.nextInt();
			}else if(name.equalsIgnoreCase("nombre")){
				fridgeMagnet.nombre = reader.nextString();
			}else if(name.equalsIgnoreCase("logo")){
				fridgeMagnet.logo = reader.nextString();
			}else if(name.equalsIgnoreCase("descripcion")){
				fridgeMagnet.descripcion = reader.nextString();
			}else if(name.equalsIgnoreCase("info")){
				fridgeMagnet.info = reader.nextString();
			}else if(name.equalsIgnoreCase("imantado_default")){
				fridgeMagnet.imantado_default = reader.nextInt();
			}else if(name.equalsIgnoreCase("imantado_busqueda_cat")){
				fridgeMagnet.imantado_busqueda_cat = reader.nextInt();
			}else if(name.equalsIgnoreCase("imantado_busqueda_top")){
				fridgeMagnet.imantado_busqueda_top = reader.nextInt();
			}else if(name.equalsIgnoreCase("estado")){
				fridgeMagnet.estado = reader.nextInt();
			}else if(name.equalsIgnoreCase("pos_x")){
				fridgeMagnet.pos_x = reader.nextInt();
			}else if(name.equalsIgnoreCase("pos_y")){
				fridgeMagnet.pos_y = reader.nextInt();
			}else {
				fridgeMagnet = null;
				reader.skipValue();
			}
		}
		reader.endObject();
		return fridgeMagnet;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void writeMessagesArray(JsonWriter writer, List objectsList)
			throws IOException {
		writer.beginArray();
		for (Object fm : objectsList) {
			if(fm!=null){
				writeFridgeMagnet(writer, (FridgeMagnet)fm);
			}
		}	
		writer.endArray();		
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

	private void writeFridgeMagnet(JsonWriter writer, FridgeMagnet fm) throws IOException{
		writer.beginObject();
		writer.name("id_marca").value(fm.id_marca);
		writer.name("id_pais").value(fm.id_pais);
		writer.name("id_categoria").value(fm.id_categoria);
		writer.name("nombre").value(fm.nombre);
		writer.name("logo").value(fm.logo);
		writer.name("descripcion").value(fm.descripcion);
		writer.name("info").value(fm.info);
		writer.name("imantado_default").value(fm.imantado_default);
		writer.name("imantado_busqueda_top").value(fm.imantado_busqueda_top);
		writer.name("imantado_busqueda_cat").value(fm.imantado_busqueda_cat);
		writer.endObject();
	}
}
