package com.larefri;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;

class DownloadFridgeMagnetLogoTask extends AsyncTask<String, String, Boolean>{
	private Context context;

	public DownloadFridgeMagnetLogoTask(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String url = params[0];
		String file = params[1];
		try{
			InputStream imageInputStream=new URL(url+file).openStream();
	    	FileOutputStream imageOutputStream;
	    	imageOutputStream = context.openFileOutput(file, Context.MODE_PRIVATE);
	    	MainActivity.CopyStream(imageInputStream, imageOutputStream);
	    	/*Bitmap bmp = BitmapFactory.decodeStream(imageInputStream);
			if(bmp != null){
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, imageOutputStream);
			}*/
	    	imageOutputStream.close();
	    	return true;
		}catch(Exception donotCare){ }
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		MainActivity.removeDownloadFMLogoTasks(this);
	}
}
