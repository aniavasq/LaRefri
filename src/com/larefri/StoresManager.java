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

public class StoresManager implements RefriJsonReader, RefriJsonWriter {
	public List<StoreFM> readJsonStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		}finally{
		   reader.close();
		}
	}

	public List<StoreFM> readMessagesArray(JsonReader reader) throws IOException, IllegalStateException {
		List<StoreFM> messages = new ArrayList<StoreFM>();

		if (reader.peek() != JsonToken.BEGIN_ARRAY) {
			return messages;
		} else {
			reader.beginArray();
			while (reader.hasNext()) {
				messages.add(readStore(reader));
			}
		}
		reader.endArray();
		return messages;
	}
	
	public StoreFM readStore(JsonReader reader) throws IOException {
		StoreFM store = new StoreFM();
		
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if(name.equalsIgnoreCase("id_marca")){
				store.id_marca = reader.nextInt();
			}else if(name.equalsIgnoreCase("id_local")){
				store.id_marcas_sucursal = reader.nextInt();
			}else if(name.equalsIgnoreCase("ciudad")){
				store.ciudad = reader.nextString();				
			}else if(name.equalsIgnoreCase("direccion")){
				store.direccion = reader.nextString();				
			}else if(name.equalsIgnoreCase("estado")){
				store.estado = reader.nextInt();				
			}else if(name.equalsIgnoreCase("nombre")){
				store.nombre = reader.nextString();				
			}else if(name.equalsIgnoreCase("telefono")){
				store.telefono = reader.nextString();				
			}else if(name.equalsIgnoreCase("telefono2")){
				store.telefono2 = reader.nextString();				
			}else if(name.equalsIgnoreCase("telefono3")){
				store.telefono3 = reader.nextString();				
			}else if(name.equalsIgnoreCase("tipo")){
				store.tipo = reader.nextString();				
			}else{
				store = null;
				reader.skipValue();
			}
		}
		reader.endObject();
		return store;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void writeMessagesArray(JsonWriter writer, List objectsList)
			throws IOException {
		writer.beginArray();
	     for (Object s : objectsList) {
	    	 if(s!=null)
	    		 writeStores(writer, (StoreFM)s);
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
	
	private void writeStores(JsonWriter writer, StoreFM s) throws IOException{
		writer.beginObject();
		writer.name("id_marca").value(s.id_marca.toString());
		writer.name("id_local").value(s.id_marcas_sucursal.toString());
		writer.name("nombre").value(s.nombre.toString());
		writer.name("telefono").value(s.telefono.toString());
		writer.name("telefono2").value(s.telefono2.toString());
		writer.name("telefono3").value(s.telefono3.toString());
		writer.name("ciudad").value(s.ciudad.toString());
		writer.name("direccion").value(s.direccion.toString());
		writer.name("tipo").value(s.tipo.toString());
		writer.name("estado").value(s.estado.toString());
	    writer.endObject();
	}
}
