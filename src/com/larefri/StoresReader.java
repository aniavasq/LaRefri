package com.larefri;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;

public class StoresReader implements RefriJsonReader {
	public List<Store> readJsonStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		}finally{
		   reader.close();
		 }
	  }

	public List<Store> readMessagesArray(JsonReader reader) throws IOException {
		List<Store> messages = new ArrayList<Store>();

		reader.beginArray();
		while (reader.hasNext()) {
			messages.add(readStore(reader));
		}
		reader.endArray();
		return messages;
	}
	
	public Store readStore(JsonReader reader) throws IOException {
		Store store = new Store();
		
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if(name.equalsIgnoreCase("id_marca")){
				store.id_marca = reader.nextInt();
			}else if(name.equalsIgnoreCase("id_marcas_sucursal")){
				store.id_marcas_sucursal = reader.nextInt();
			}else if(name.equalsIgnoreCase("direccion")){
				store.direccion = reader.nextString();				
			}else if(name.equalsIgnoreCase("estado")){
				store.estado = reader.nextInt();				
			}else if(name.equalsIgnoreCase("nombre")){
				store.nombre = reader.nextString();				
			}else if(name.equalsIgnoreCase("telefono")){
				store.telefono = reader.nextString();				
			}else{
				store = null;
				reader.skipValue();
			}
		}
		reader.endObject();
		return store;
	}
}
