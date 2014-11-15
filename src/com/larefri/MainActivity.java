package com.larefri;

//Repository git@github.com:aniavasq/LaRefri.git

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private final String PREFS_NAME = "LaRefriPrefsFile";
	private final Integer movViewId = 20000000, delViewId = 20000005, enableViewId = 20000010;
	private final Integer TEXT_SIZE = 22;
	private final Handler handler = new Handler();
	private SharedPreferences settings;
	private Integer width, height;
	private Context context;
	private Object[] movNdel;
	private boolean fridgeMagnetsClickable;
	private boolean hasScrolled;
	private boolean fridgeMagnetsDraggable;
	private boolean editMagnetViewShowed;
	private List<FridgeMagnet> fridgeMagnets;
	private ScrollView myScrollView;
	private LocationTask locationTask;
	private BitmapLRUCache mMemoryCache;

	class FridgeMagnetOnTouchListener extends GestureDetector.SimpleOnGestureListener  implements OnTouchListener {
		private FridgeMagnet fm;
		private Float mDownX;
		private Float mDownY;
		private final Float SCROLL_THRESHOLD = 10.0f;

		public FridgeMagnetOnTouchListener(FridgeMagnet fm) {
			this.fm = fm;
		}

		@Override
		public boolean onTouch(View v, MotionEvent evt) {
			Integer action = evt.getAction() & MotionEvent.ACTION_MASK;			
			v.performClick();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mDownX = evt.getX();
				mDownY = evt.getY();
				hasScrolled = false;
				return false;

			case MotionEvent.ACTION_MOVE:
				if(fridgeMagnetsDraggable){

					Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
					try{
						int color = bmp.getPixel((int) evt.getX(), (int) evt.getY());
						if (color == Color.TRANSPARENT){
							hasScrolled = true;
							bmp.recycle();
							bmp=null;
							return false;
						}else {

							ClipData data = ClipData.newPlainText("", "");
							DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
							v.startDrag(data, shadowBuilder, v, 0);
							bmp.recycle();
							bmp=null;
							return true;
						}
					}catch(Exception donotcare){ }
				}
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if( (Math.abs(mDownX - evt.getX()) < SCROLL_THRESHOLD) && (Math.abs(mDownY - evt.getY()) < SCROLL_THRESHOLD) && fridgeMagnetsClickable && !fridgeMagnetsDraggable){
					goToFlyer(v, fm);						
				}
				hasScrolled = false;
				return false;

			default:
				return true;
			}
		}
	}

	class FridgeMagnetOnDragListener implements OnDragListener{

		@Override
		public boolean onDrag(View v, DragEvent evnt) {
			Integer action = evnt.getAction();
			View view;
			switch(action){
			case DragEvent.ACTION_DRAG_STARTED:
				v.invalidate();
				return true;
			case DragEvent.ACTION_DRAG_ENTERED:
				chageOpacityFridgeMagnets(0.4f);
				break;
			case DragEvent.ACTION_DRAG_LOCATION:
				int[] array =new int[2];
				v.getLocationOnScreen(array);
				if (array[1] < height/4) {
					myScrollView.smoothScrollBy(0, -75);
				}else if (array[1] > 3*height/4) {
					myScrollView.smoothScrollBy(0, 75);
				}
				break;
			case DragEvent.ACTION_DROP:
				view = (View) evnt.getLocalState();
				if(view!=v){
					ImageButton container = (ImageButton) v;
					ViewGroup owner = (ViewGroup) view.getParent(), destiny = ((ViewGroup)container.getParent());
					owner.removeView(view);
					destiny.addView(view);
					destiny.removeView(container);
					owner.addView(container);
					v.setVisibility(View.VISIBLE);
					view.setVisibility(View.VISIBLE);
					swapFridgeMagnetsInList(view, v);
				}
				break;
			default:
				break;
			}
			return true;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.context = this;
		this.fridgeMagnetsClickable = true;
		this.fridgeMagnetsDraggable = false;
		this.editMagnetViewShowed = false;

		FontsOverride.setDefaultFont(this, "MONOSPACE", "Roboto-Thin.ttf");
		//Set policy to HTTP
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		//get metrics for relative size from display width multiples
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		this.width = dm.widthPixels;
		this.height = dm.heightPixels;
		settings = getSharedPreferences(PREFS_NAME, 0);

		//EULA
		AppEULA appEULA = new AppEULA(this);
		if (settings.getBoolean(appEULA.getEulaKey(), true)) {
			appEULA.show();
		}
		//check first time installed
		if (settings.getBoolean("my_first_time", true)) {
			copyAssets();
			// record the fact that the APP has been started at least once
			settings.edit().putBoolean("my_first_time", false).commit();
		}
		
	    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	    final int cacheSize = maxMemory / 8;
	    mMemoryCache = new BitmapLRUCache(cacheSize);
		
		this.movNdel = createEditMagnetView();
		//ScrolView
		this.myScrollView = (ScrollView) findViewById(R.id.the_scroll_view);
		//Location
		this.locationTask = new LocationTask(this.context, this);
	}

	public void downloadImageFromServer(String url, String file) throws MalformedURLException, IOException {
		InputStream imageInputStream = new URL(url+file).openStream();
		FileOutputStream imageOutputStream;
		imageOutputStream = openFileOutput(file, Context.MODE_PRIVATE);
		Bitmap bmp = BitmapFactory.decodeStream(imageInputStream);
		if(bmp != null){
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, imageOutputStream);
		}
		imageOutputStream.close();
	}

	private void copyAssets() {
		AssetManager assetManager = getAssets();
		String[] files = null, images = null;
		try {
			files = assetManager.list("");
			images = assetManager.list("images");
		} catch (IOException doNotCare) {  }
		for(String filename : files) {
			InputStream in = null;
			FileOutputStream out = null;
			try {
				in = assetManager.open(filename);
				out = openFileOutput(filename, Context.MODE_PRIVATE);
				CopyStream(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			} catch(IOException doNotCare) {  }       
		}

		for(String filename : images) {
			InputStream in = null;
			FileOutputStream out = null;
			try {
				in = assetManager.open(filename);
				out = openFileOutput(filename, Context.MODE_PRIVATE);
				CopyStream(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			} catch(IOException doNotCare) {  }       
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		setBackground();
		//add fridgeMagnets from local data
		LinearLayout left_pane_fridgemagnets = (LinearLayout) findViewById(R.id.left_pane_fridgemagnets);
		LinearLayout right_pane_fridgemagnets = (LinearLayout)findViewById(R.id.right_pane_fridgemagnets);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width/2, width/2);
		((ViewGroup)left_pane_fridgemagnets).removeAllViews();
		((ViewGroup)right_pane_fridgemagnets).removeAllViews();
		lp.setMargins(0, 0, 0, 0);
		lp.gravity = Gravity.BOTTOM;
		//add default icons for the fridgeMagnets as buttons
		try {
			loadFridgeMagnetsFromFile(left_pane_fridgemagnets, right_pane_fridgemagnets, lp);
		} 
		catch (IOException e) { }
		catch (Exception e) { }
		//Update phone guide
		//Check Updates
		HashMap<String, String> params = new HashMap<String, String>();
		String last_update = settings.getString("last_update", "2014-10-14 18:49:50");
		params.put("ultima_actualizacion", last_update);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("ultima_actualizacion", last_update));
		(new UpdateFridgeMagnetsListener(this, fridgeMagnets)).execute(
				StaticUrls.UPDATES,
				params,
				nameValuePairs);
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationTask.cancel(true);
		locationTask = new LocationTask(context, this);
		locationTask.execute();
	}

	public void goToMenu(View view) {
		Intent intent = new Intent(view.getContext(), MenuActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		this.startActivity(intent);
	}

	public void goToFlyer(View view, FridgeMagnet fm) {
		Intent intent = new Intent(view.getContext(), FlyerActivity.class);
		Bundle b = new Bundle();
		b.putInt("id_marca", fm.id_marca);
		b.putString("logo", fm.logo);
		b.putString("nombre", fm.nombre);		
		intent.putExtras(b);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		this.startActivity(intent);
	}

	//For image copy from HTTP response
	public static void CopyStream(InputStream is, FileOutputStream os)
	{
		final int buffer_size=1024;
		try
		{
			byte[] bytes=new byte[buffer_size];
			for(;;)
			{
				int count=is.read(bytes, 0, buffer_size);
				if(count==-1)
					break;
				os.write(bytes, 0, count);
			}
		}
		catch(Exception ex){ }
	}

	public void loadFridgeMagnetsFromFile(LinearLayout left_pane_fridgemagnets, LinearLayout right_pane_fridgemagnets, LayoutParams lp) throws IOException{
		FridgeMagnetsManager fridgeMagnetReader = new FridgeMagnetsManager();
		this.fridgeMagnets = fridgeMagnetReader.readJsonStream( new FileInputStream(new File(getFilesDir(), "data.json")) );
		for(final FridgeMagnet fm: fridgeMagnets){
			OnTouchListener fridgeMagnetOnTouchListener = new FridgeMagnetOnTouchListener(fm);
			final ImageButton tmp_imageButtom = new ImageButton(this);
			tmp_imageButtom.setLayoutParams(lp);
			///
			Runnable imageLoader = new Runnable() {
				@Override
				public void run() {
					File imgFile;
					do{
						imgFile = new File(getFilesDir(), fm.logo);
						if(imgFile.exists()){
							try {
								loadImageToButtom(imgFile, tmp_imageButtom);
							} catch (FileNotFoundException e) { }
						}
					}while(!imgFile.exists());
				}
			};
			File imgFile = new File(getFilesDir(), fm.logo);
			if(imgFile.exists()){
				try {
					loadImageToButtom(imgFile, tmp_imageButtom);
				} catch (FileNotFoundException e) { }
			}else{
				handler.post(imageLoader);
			}
			///
			tmp_imageButtom.setScaleType( ImageView.ScaleType.FIT_CENTER );
			tmp_imageButtom.setId(fm.id_marca);
			tmp_imageButtom.setBackgroundColor(Color.TRANSPARENT);
			tmp_imageButtom.setDrawingCacheEnabled(true);
			tmp_imageButtom.setOnDragListener(new FridgeMagnetOnDragListener());
			tmp_imageButtom.setOnTouchListener(fridgeMagnetOnTouchListener);
			tmp_imageButtom.setOnLongClickListener(new View.OnLongClickListener() {				
				@Override
				public boolean onLongClick(View v) {
					if(!fridgeMagnetsDraggable && !hasScrolled){
						showEditMagnetView(v);
					}
					return true;
				}
			});
			RelativeLayout rl = new RelativeLayout(context);
			rl.setLayoutParams(lp);
			rl.addView(tmp_imageButtom);
			if(fridgeMagnets.indexOf(fm)%2 == 0){
				left_pane_fridgemagnets.addView(rl);
			}else{
				right_pane_fridgemagnets.addView(rl);
			}
		}
	}

	private void loadImageToButtom(File imgFile, ImageButton tmp_imageButtom) throws FileNotFoundException {
		final String imageKey = String.valueOf(imgFile);

	    Bitmap bitmap = mMemoryCache.getBitmapFromMemCache(imageKey);
	    if (bitmap != null) {
			loadBitmap(imgFile, tmp_imageButtom, bitmap);
	    } else {
	    	bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	    	loadBitmap(imgFile, tmp_imageButtom, bitmap);
	    	mMemoryCache.addBitmapToMemoryCache(imageKey, bitmap);
	    }
	}

	public void loadBitmap(File imgFile, ImageButton imageButton, Bitmap bitmap) {
		if (cancelPotentialWork(imgFile, imageButton)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageButton, this);
			final BitmapWorkerTask.AsyncRecyclingDrawable asyncDrawable =  new BitmapWorkerTask.AsyncRecyclingDrawable(getResources(), bitmap, task);
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

	public Bitmap decodeSampledBitmapFromResource(File imgFile){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		int reqWidth = width/2-20;
		int reqHeight = width/2-20;		
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

	protected void swapFridgeMagnetsInList(View view, View v) {
		FridgeMagnet from_view = null, from_v = null;
		for(FridgeMagnet fm: this.fridgeMagnets){
			if(fm.id_marca == view.getId()){
				from_view = fm;
			}else if(fm.id_marca == v.getId()){
				from_v = fm;
			}
		}
		if(from_v!=null && from_view!=null){
			int i = fridgeMagnets.indexOf(from_v), j = fridgeMagnets.indexOf(from_view);
			Collections.swap(fridgeMagnets, i, j);
		}
	}

	private Object[] createEditMagnetView(){
		RelativeLayout mov = new RelativeLayout(this);
		RelativeLayout del = new RelativeLayout(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width/4, width/3);
		LinearLayout.LayoutParams lpImg = new LinearLayout.LayoutParams(width/4, width/6);
		lp.setMargins(0, 0, 0, 0);
		lpImg.setMargins(0, 0, 0, 0);
		lp.gravity = Gravity.BOTTOM;
		LinearLayout.LayoutParams enable_lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		enable_lp.setMargins(0, 0, 0, 0);
		enable_lp.gravity = Gravity.TOP;

		RelativeLayout scrollable = (RelativeLayout) findViewById(R.id.scrollable);
		mov.setLayoutParams(lp);
		del.setLayoutParams(lp);
		mov.setId(movViewId);
		del.setId(delViewId);
		//Image for Move and Delete
		ImageView movImg = new ImageView(context);
		ImageView delImg = new ImageView(context);
		movImg.setLayoutParams(lpImg);
		delImg.setLayoutParams(lpImg);
		movImg.setBackgroundColor(Color.TRANSPARENT);
		movImg.setScaleType( ImageView.ScaleType.FIT_CENTER );
		delImg.setBackgroundColor(Color.TRANSPARENT);
		delImg.setScaleType( ImageView.ScaleType.FIT_CENTER );
		movImg.setImageResource(R.drawable.ic_move);
		delImg.setImageResource(R.drawable.ic_delete);
		//Text for Move and Delete
		TextView movTxt = new TextView(context);
		TextView delTxt = new TextView(context);
		movTxt.setLayoutParams(lp);
		delTxt.setLayoutParams(lp);
		movTxt.setBackgroundColor(Color.TRANSPARENT);
		delTxt.setBackgroundColor(Color.TRANSPARENT);
		movTxt.setText(R.string.move_btn);
		delTxt.setText(R.string.delete_btn);
		movTxt.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
		delTxt.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
		movTxt.setTextColor(Color.WHITE);
		delTxt.setTextColor(Color.WHITE);
		movTxt.setTextSize(26.0f);
		delTxt.setTextSize(26.0f);
		//
		mov.addView(movImg);
		del.addView(delImg);
		mov.addView(movTxt);
		del.addView(delTxt);
		mov.setVisibility(View.INVISIBLE);
		del.setVisibility(View.INVISIBLE);
		//

		RelativeLayout enable_layout = new RelativeLayout(context);
		enable_layout.setId(enableViewId);
		enable_layout.setLayoutParams(enable_lp);
		enable_layout.setBackgroundColor(Color.BLACK);
		enable_layout.setAlpha(0.75f);
		enable_layout.setVisibility(View.INVISIBLE);

		scrollable.addView(enable_layout);
		scrollable.addView(mov);
		scrollable.addView(del);

		Object[] result = new Object[3];
		result[0] = mov;
		result[1] = del;
		result[2] = enable_layout;

		return result;
	}

	protected void letsDelFridgeMagnets(final View v) {
		FridgeMagnet fm_tmp = new FridgeMagnet();

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					//Yes button clicked
					for(FridgeMagnet fm: fridgeMagnets){
						if(fm.id_marca == v.getId()){
							fridgeMagnets.remove(fm);
							try {
								saveFridgeMagnetsList();
								onStart();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						}
					}
					modifyBackButton();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
				default:
					//No button clicked
					modifyBackButton();
					break;
				}
			}
		};
		DialogInterface.OnCancelListener dialogCancelListener = new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				//On Cancel
				modifyBackButton();
			}
		};

		for(FridgeMagnet fm: fridgeMagnets){
			if(fm.id_marca == v.getId()) fm_tmp = fm;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getText(R.string.delete_fridge_magnet)+" "+fm_tmp.nombre+"?")
		.setPositiveButton(R.string.positive, dialogClickListener)
		.setNegativeButton(R.string.negative, dialogClickListener)
		.setOnCancelListener(dialogCancelListener);
		final AlertDialog alert = builder.create();
		alert.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Button btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE);
				btnPositive.setTextSize(TEXT_SIZE);

				Button btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE);
				btnNegative.setTextSize(TEXT_SIZE);
			}
		});
		alert.show();
	}

	protected void letsDragFridgeMagnets(View v) {
		this.fridgeMagnetsDraggable = true;
		chageOpacityFridgeMagnets(0.4f);
		modifyOverflowButton();
	}

	private void modifyOverflowButton() {
		Button menuButton = (Button)findViewById(R.id.overflowbutton);
		menuButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_previous_item, 0, 0, 0);
		menuButton.setText("Ok");
		menuButton.setTextSize(TEXT_SIZE);
		menuButton.setTextColor(Color.WHITE);
		menuButton.setTypeface(Typeface.MONOSPACE);
		menuButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				fridgeMagnetsDraggable = false;
				chageOpacityFridgeMagnets(1.0f);
				modifyBackButton();
			}
		});	
	}

	protected void modifyBackButton() {
		Button menuButton = ( (Button)findViewById(R.id.overflowbutton) );
		menuButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_overflow, 0, 0, 0);
		menuButton.setText("");
		menuButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				goToMenu(v);
			}
		});
		try {
			saveFridgeMagnetsList();
		} catch (FileNotFoundException e) { 
		} catch (IOException e) {
		}
		hideEditMagnetView();
	}

	private void chageOpacityFridgeMagnets(float opacity) {
		for(final FridgeMagnet fm: this.fridgeMagnets){
			ImageButton tmp_imageButtom = (ImageButton)findViewById(fm.id_marca);
			tmp_imageButtom.setAlpha(opacity);
		}		
	}

	private void showEditMagnetView(final View view){
		editMagnetViewShowed = true;
		RelativeLayout mov = (RelativeLayout)findViewById(movViewId);
		RelativeLayout del = (RelativeLayout)findViewById(delViewId);
		RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) del.getLayoutParams();
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		del.setLayoutParams(lp);
		View enable_layout =  (RelativeLayout)findViewById(enableViewId);
		//Animation
		ObjectAnimator animMovX = ObjectAnimator.ofFloat(mov, "x", -mov.getWidth(), width/4-mov.getWidth()/2);
		ObjectAnimator animMovY = ObjectAnimator.ofFloat(mov, "y", mov.getHeight(), height/2-mov.getHeight());
		ObjectAnimator animDelX = ObjectAnimator.ofFloat(del, "x", width, 3*width/4-del.getWidth()/2);
		ObjectAnimator animDelY = ObjectAnimator.ofFloat(del, "y", del.getHeight(), height/2-del.getHeight());
		animMovX.setDuration(250); animMovY.setDuration(250);
		animDelX.setDuration(250); animDelY.setDuration(250);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animMovX, animMovY, animDelX, animDelY);
		animSetXY.start();
		//
		mov.setVisibility(View.VISIBLE);
		del.setVisibility(View.VISIBLE);
		mov.getChildAt(0).setVisibility(View.VISIBLE);
		del.getChildAt(0).setVisibility(View.VISIBLE);
		enable_layout.setVisibility(View.VISIBLE);

		enable_layout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent evt) {
				Integer action = evt.getAction() & MotionEvent.ACTION_MASK;				
				v.performClick();
				if(action == MotionEvent.ACTION_DOWN){
					editMagnetViewShowed = false;
					hideEditMagnetView();
					return true;
				}
				return false;
			}
		});

		mov.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent evt) {
				Integer action = evt.getAction() & MotionEvent.ACTION_MASK;				
				v.performClick();
				if(action == MotionEvent.ACTION_DOWN && movNdel!=null){
					hideEditMagnetView();
					letsDragFridgeMagnets(view);
					fridgeMagnetsClickable = true;
				}
				return false;
			}
		});

		del.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent evt) {
				Integer action = evt.getAction() & MotionEvent.ACTION_MASK;
				v.performClick();
				if(action == MotionEvent.ACTION_DOWN && movNdel!=null){
					letsDelFridgeMagnets(view);
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							fridgeMagnetsClickable = true;
							hideEditMagnetView();
						}
					}, 750);
				}
				return false;
			}
		});
		this.fridgeMagnetsClickable = false;
	}

	private void hideEditMagnetView(){
		RelativeLayout del = (RelativeLayout)findViewById(delViewId);
		RelativeLayout mov = (RelativeLayout)findViewById(movViewId);
		mov.setVisibility(View.INVISIBLE);
		del.setVisibility(View.INVISIBLE);
		mov.getChildAt(0).setVisibility(View.INVISIBLE);
		del.getChildAt(0).setVisibility(View.INVISIBLE);
		RelativeLayout enable_layout = (RelativeLayout)findViewById(enableViewId);
		enable_layout.setVisibility(View.INVISIBLE);
		enable_layout.setOnTouchListener(null);
		this.fridgeMagnetsClickable = true;
	}

	private void saveFridgeMagnetsList() throws FileNotFoundException, IOException {
		File JsonFile = new File(getFilesDir(), "data.json");		
		FridgeMagnetsManager fridgeMagnetWriter = new FridgeMagnetsManager();
		fridgeMagnetWriter.writeJsonStream(new FileOutputStream(JsonFile), fridgeMagnets);
	}

	protected void setBackground() {
		RelativeLayout head = (RelativeLayout)findViewById(R.id.relativeLayout1);
		LinearLayout article = (LinearLayout)findViewById(R.id.article);
		int bg_color = settings.getInt("bg_color", Color.parseColor("#999089"));
		article.setBackgroundColor(bg_color);
		head.setBackgroundColor(bg_color);
	}

	@Override
	public void onBackPressed() {
		modifyBackButton();
		if(fridgeMagnetsDraggable){
			fridgeMagnetsDraggable = false;
			chageOpacityFridgeMagnets(1.0f);
		}
		if(editMagnetViewShowed){
			editMagnetViewShowed = false;
			hideEditMagnetView();
		}else{
			super.onBackPressed();
		}
	}
}
