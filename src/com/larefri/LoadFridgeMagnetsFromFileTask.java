package com.larefri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;

public class LoadFridgeMagnetsFromFileTask extends
		AsyncTask<Void, Void, Void> {
	private MainActivity master;
	private static MainActivity reference;
	private LinearLayout left_pane_fridgemagnets;
	private LinearLayout right_pane_fridgemagnets;
	private LayoutParams lp;

	@SuppressWarnings("static-access")
	public LoadFridgeMagnetsFromFileTask(MainActivity master,
			LinearLayout left_pane_fridgemagnets,
			LinearLayout right_pane_fridgemagnets, LayoutParams lp) {
		super();
		this.master = master;
		this.reference = master;
		this.left_pane_fridgemagnets = left_pane_fridgemagnets;
		this.right_pane_fridgemagnets = right_pane_fridgemagnets;
		this.lp = lp;
	}

	@Override
	protected void onPreExecute() {
		loadFridgeMagnetsFromFile(left_pane_fridgemagnets, right_pane_fridgemagnets, lp);
	}

	@Override
	protected Void doInBackground(Void... arg) {	
		try {
			FridgeMagnetsManager fridgeMagnetReader = new FridgeMagnetsManager();
			MainActivity.setMyFridgeMagnets(fridgeMagnetReader.readJsonStream( new FileInputStream(new File(master.getFilesDir(), "data.json")) ));
		} 
		catch (IOException e) {
			Log.e("LOADING FM", e.getMessage(), e); }
		catch (Exception e) {
			Log.e("LOADING FM", e.getMessage(), e); }
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		left_pane_fridgemagnets.removeAllViews();
		right_pane_fridgemagnets.removeAllViews();
		loadFridgeMagnetsFromFile(left_pane_fridgemagnets, right_pane_fridgemagnets, lp);
		super.onPostExecute(result);
	}

	public void loadFridgeMagnetsFromFile(LinearLayout left_pane_fridgemagnets, LinearLayout right_pane_fridgemagnets, LayoutParams lp){
		for(final FridgeMagnet fm: MainActivity.getMyFridgeMagnets()){
			OnTouchListener fridgeMagnetOnTouchListener = master.new FridgeMagnetOnTouchListener(fm);
			final ImageButton tmp_imageButtom = new ImageButton((Context)master);
			tmp_imageButtom.setLayoutParams(lp);
			
			File imgFile = new File(master.getFilesDir(), fm.logo);
			if(imgFile.exists()){
				try {
					loadImageToButtom(imgFile, tmp_imageButtom);
				} catch (FileNotFoundException e) { }
			}
			
			///
			tmp_imageButtom.setScaleType( ImageView.ScaleType.FIT_CENTER );
			tmp_imageButtom.setId(fm.id_marca);
			tmp_imageButtom.setBackgroundColor(Color.TRANSPARENT);
			tmp_imageButtom.setDrawingCacheEnabled(true);
			tmp_imageButtom.setOnDragListener(master.new FridgeMagnetOnDragListener());
			tmp_imageButtom.setOnTouchListener(fridgeMagnetOnTouchListener);
			tmp_imageButtom.setOnLongClickListener(new View.OnLongClickListener() {				
				@Override
				public boolean onLongClick(View v) {
					if(!master.isFridgeMagnetsDraggable() && !master.isHasScrolled()){
						master.showEditMagnetView(v);
					}
					return true;
				}
			});
			RelativeLayout rl = new RelativeLayout((Context)master);
			rl.setLayoutParams(lp);
			rl.addView(tmp_imageButtom);
			if(MainActivity.getMyFridgeMagnets().indexOf(fm)%2 == 0){
				left_pane_fridgemagnets.addView(rl);
			}else{
				right_pane_fridgemagnets.addView(rl);
			}
		}
	}
	
	private void loadImageToButtom(File imgFile, ImageButton tmp_imageButtom) throws FileNotFoundException {
		final String imageKey = String.valueOf(imgFile);

	    Bitmap bitmap = master.mMemoryCache.getBitmapFromMemCache(imageKey);
	    if (bitmap != null) {
			loadBitmap(imgFile, tmp_imageButtom, bitmap);
	    } else {
	    	bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	    	loadBitmap(imgFile, tmp_imageButtom, bitmap);
	    	master.mMemoryCache.addBitmapToMemoryCache(imageKey, bitmap);
	    }
	}

	public void loadBitmap(File imgFile, ImageButton imageButton, Bitmap bitmap) {
		if (cancelPotentialWork(imgFile, imageButton)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageButton, master);
			final BitmapWorkerTask.AsyncRecyclingDrawable asyncDrawable =  new BitmapWorkerTask.AsyncRecyclingDrawable(master.getResources(), bitmap, task);
			imageButton.setImageDrawable(asyncDrawable);
			task.execute(imgFile);
		}
	}

	public static boolean cancelPotentialWork(File data, ImageButton imageButton) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageButton);

		if (bitmapWorkerTask != null) {
			final File bitmapData = bitmapWorkerTask.getData();
			// If bitmapData is not yet set or it differs from the new data
			if (bitmapData == null || bitmapData != data) {
				// Cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was cancelled
		return true;
	}

	public static BitmapWorkerTask getBitmapWorkerTask(ImageButton imageButton) {
		if (imageButton != null) {
			final Drawable drawable = imageButton.getDrawable();
			if (drawable instanceof BitmapWorkerTask.AsyncRecyclingDrawable) {
				final BitmapWorkerTask.AsyncRecyclingDrawable asyncDrawable = (BitmapWorkerTask.AsyncRecyclingDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	public static Bitmap decodeSampledBitmapFromResource(File imgFile){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		int reqWidth = reference.getWidth()/2-20;
		int reqHeight =reference.getWidth()/2-20;		
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
		return bmp;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}
