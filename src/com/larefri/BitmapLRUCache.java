package com.larefri;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class BitmapLRUCache extends LruCache<String, Bitmap>{
	public BitmapLRUCache(int maxSize) {
	    super(maxSize);
	}

	@Override
	protected int sizeOf(String key, Bitmap value) {
	    return value.getRowBytes() * value.getHeight();
	}	


	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			this.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return this.get(key);
	}
}
