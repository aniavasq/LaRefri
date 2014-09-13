package com.larefri;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;
import android.util.JsonToken;

public class SkinsManager implements RefriJsonReader{

	public List<Skin> readJsonStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		}finally{
		   reader.close();
		 }
	  }

	public List<Skin> readMessagesArray(JsonReader reader) throws IOException {
		List<Skin> messages = new ArrayList<Skin>();

		if (reader.peek() != JsonToken.BEGIN_ARRAY) {
			return messages;
		} else {
			reader.beginArray();
			while (reader.hasNext()) {
				messages.add(readSkin(reader));
			}
		}
		reader.endArray();
		return messages;
	}

	public Skin readSkin(JsonReader reader) throws IOException {
		Skin skin = new Skin();
		
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if(name.equalsIgnoreCase("name")){
				skin.name = reader.nextString();
			}else if(name.equalsIgnoreCase("article_skin")){
				skin.article_skin = reader.nextString();
			}else if(name.equalsIgnoreCase("head_skin")){
				skin.head_skin = reader.nextString();
			}else{
				reader.skipValue();
			}
		}
		reader.endObject();
		return skin;
	}

}

class Skin{
	String name;
	String head_skin;
	String article_skin;
}
