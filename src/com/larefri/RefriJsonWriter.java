package com.larefri;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import android.util.JsonWriter;

public interface RefriJsonWriter {
	
	public void writeMessagesArray(JsonWriter writer,List<Object> objectsList) throws IOException;
	
	public void writeJsonStream(OutputStream out, List<Object> objectsList) throws IOException;
}
