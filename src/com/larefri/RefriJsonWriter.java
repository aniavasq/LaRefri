package com.larefri;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import android.util.JsonWriter;

public interface RefriJsonWriter {
	
	@SuppressWarnings("rawtypes")
	public void writeMessagesArray(JsonWriter writer,List objectsList) throws IOException;
	
	@SuppressWarnings("rawtypes")
	public void writeJsonStream(OutputStream out, List objectsList) throws IOException;
}
