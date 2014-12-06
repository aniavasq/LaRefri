package com.larefri;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;
import android.util.JsonToken;

public class CategoriesManager implements RefriJsonReader {

	@Override
	public List<CategoryFM> readJsonStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		}finally{
		   reader.close();
		 }
	  }

	public List<CategoryFM> readMessagesArray(JsonReader reader) throws IOException {
		List<CategoryFM> messages = new ArrayList<CategoryFM>();

		if (reader.peek() != JsonToken.BEGIN_ARRAY) {
			return messages;
		} else {
			reader.beginArray();
			while (reader.hasNext()) {
				messages.add(readCategory(reader));
			}
		}
		reader.endArray();
		return messages;
	}

	public CategoryFM readCategory(JsonReader reader) throws IOException {
		CategoryFM category = new CategoryFM();
		
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if(name.equalsIgnoreCase("id_categoria")){
				category.id_categoria = reader.nextString();
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
