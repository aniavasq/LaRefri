package com.larefri;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;

public class CategoriesManager implements RefriJsonReader {

	@Override
	public List<Category> readJsonStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		}finally{
		   reader.close();
		 }
	  }

	public List<Category> readMessagesArray(JsonReader reader) throws IOException {
		List<Category> messages = new ArrayList<Category>();

		reader.beginArray();
		while (reader.hasNext()) {
			messages.add(readCategory(reader));
		}
		reader.endArray();
		return messages;
	}

	public Category readCategory(JsonReader reader) throws IOException {
		Category category = new Category();
		
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if(name.equalsIgnoreCase("id_categoria")){
				category.id_categoria = reader.nextInt();
			}else if(name.equalsIgnoreCase("nombre_categoria")){
				category.nombre_categoria = reader.nextString();
			}else if(name.equalsIgnoreCase("icono_categoria")){
				category.icono_categoria = reader.nextString();
			}else if(name.equalsIgnoreCase("estado")){
				category.estado = reader.nextInt();
			}else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return category;
	}
}
