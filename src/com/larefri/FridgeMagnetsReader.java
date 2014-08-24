package com.larefri;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;

public class FridgeMagnetsReader implements RefriJsonReader {
	public List<FridgeMagnet> readJsonStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		}finally{
		   reader.close();
		 }
	  }

	public List<FridgeMagnet> readMessagesArray(JsonReader reader) throws IOException {
		List<FridgeMagnet> messages = new ArrayList<FridgeMagnet>();

		reader.beginArray();
		while (reader.hasNext()) {
			messages.add(readMagnet(reader));
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
				reader.skipValue();
			}
		}
		reader.endObject();
		return fridgeMagnet;
	}
}
