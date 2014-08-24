package com.larefri;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.util.JsonReader;

public interface RefriJsonReader {
	@SuppressWarnings("rawtypes")
	public List readJsonStream(InputStream in) throws IOException;
	@SuppressWarnings("rawtypes")
	public List readMessagesArray(JsonReader reader) throws IOException;
}
