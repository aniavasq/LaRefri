package com.larefri;

//Repository git@github.com:aniavasq/LaRefri.git

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final String PREFS_NAME = "LaRefriPrefsFile";
	private final Integer movViewId = 20000000, delViewId = 20000005, enableViewId = 20000010;
	private final Integer TEXT_SIZE = 22;
	private final Handler handler = new Handler();
	private SharedPreferences settings;
	private Integer width, height;
	private Context context;
	private static MainActivity reference;
	private Object[] movNdel;
	private boolean fridgeMagnetsClickable;
	private boolean hasScrolled;
	private boolean fridgeMagnetsDraggable;
	private boolean editMagnetViewShowed;
	private static List<Store> myFridgeMagnets;
	private ScrollView myScrollView;
	private LocationTask locationTask;
	public BitmapLRUCache mMemoryCache;

	class FridgeMagnetOnTouchListener extends GestureDetector.SimpleOnGestureListener  implements OnTouchListener {
		private Store fm;
		private Float mDownX;
		private Float mDownY;
		private final Float SCROLL_THRESHOLD = 10.0f;

		public FridgeMagnetOnTouchListener(Store fm) {
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
				setHasScrolled(false);
				return false;

			case MotionEvent.ACTION_MOVE:
				if(isFridgeMagnetsDraggable()){

					Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
					try{
						int color = bmp.getPixel((int) evt.getX(), (int) evt.getY());
						if (color == Color.TRANSPARENT){
							setHasScrolled(true);
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
				if( (Math.abs(mDownX - evt.getX()) < SCROLL_THRESHOLD) && (Math.abs(mDownY - evt.getY()) < SCROLL_THRESHOLD) && fridgeMagnetsClickable && !isFridgeMagnetsDraggable()){
					goToFlyer(v, fm);						
				}
				setHasScrolled(false);
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

	public synchronized static List<Store> getMyFridgeMagnets() {
		if (myFridgeMagnets!=null)
			return myFridgeMagnets;
		else{
			myFridgeMagnets = new ArrayList<Store>();
			return myFridgeMagnets;
		}			
	}

	public synchronized static void setMyFridgeMagnets(List<Store> myFridgeMagnets) {
		MainActivity.myFridgeMagnets = myFridgeMagnets;
	}

	public synchronized static void removeMyFridgeMagnet(Store fm){
		myFridgeMagnets.remove(fm);
		fm.removeFromLocalDataStore();
		Log.e("FM REMOVED", fm.toString());
		Log.e("AFTER REMOVED", myFridgeMagnets.toString());
	}
	
	public synchronized static void addMyFridgeMagnet(Store fm){
		MainActivity.myFridgeMagnets.add(fm);
	}
	
	public Integer getWidth() {
		return width;
	}

	public boolean isHasScrolled() {
		return hasScrolled;
	}

	public void setHasScrolled(boolean hasScrolled) {
		this.hasScrolled = hasScrolled;
	}

	public boolean isFridgeMagnetsDraggable() {
		return fridgeMagnetsDraggable;
	}

	public void setFridgeMagnetsDraggable(boolean fridgeMagnetsDraggable) {
		this.fridgeMagnetsDraggable = fridgeMagnetsDraggable;
	}

	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.context = this;
		this.reference = this;
		this.fridgeMagnetsClickable = true;
		this.setFridgeMagnetsDraggable(false);
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


		/***************************************************
		 * Parse support*/
		
		ParseConnector.getInstance(this);
		
		getParseFridgeMagnets();
		/**********************************************/
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
		loadFridgeMagnetsButtons(getMyFridgeMagnets());
		//Update phone guide
		//Check Updates
		UpdateFridgeMagnetsListener.update(this);
	}
	
	/**********************************************
	 * Parse support
	 * */
	private void getParseFridgeMagnets(){

		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Store");
		query.fromLocalDatastore();
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> result, ParseException e) {
				List<Store> tmp_fridgemagnets  = new ArrayList<Store>();
				if (e == null) {
					Store s;
					for(ParseObject fm: result){
						s = new Store(fm, context);
						tmp_fridgemagnets.add(s);
					}
					if(tmp_fridgemagnets!=getMyFridgeMagnets())
						setMyFridgeMagnets(tmp_fridgemagnets);
					Collections.sort(getMyFridgeMagnets(), new Comparator<Store>() {

						@Override
						public int compare(Store lhs, Store rhs) {
							Integer i = lhs.getIndex(), j = rhs.getIndex();
							return i.compareTo(j);
						}
					});
					loadFridgeMagnetsButtons(getMyFridgeMagnets());
				} else {
					Log.e("ERROR",e.getMessage(),e);
				}
			}
		});
	}
	
	protected void loadFridgeMagnetsButtons(List<Store> myFridgeMagnets) {
		LinearLayout left_pane_fridgemagnets = (LinearLayout) findViewById(R.id.left_pane_fridgemagnets);
		LinearLayout right_pane_fridgemagnets = (LinearLayout)findViewById(R.id.right_pane_fridgemagnets);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width/2, width/2);
		((ViewGroup)left_pane_fridgemagnets).removeAllViews();
		((ViewGroup)right_pane_fridgemagnets).removeAllViews();
		left_pane_fridgemagnets.setPadding(2, 0, 0, 0);
		right_pane_fridgemagnets.setPadding(0, 0, 2, 0);
		lp.setMargins(0, 0, 0, 0);
		lp.gravity = Gravity.BOTTOM;
		
		for(final Store fm: getMyFridgeMagnets()){
			OnTouchListener fridgeMagnetOnTouchListener = new FridgeMagnetOnTouchListener(fm);
			final ImageButton tmp_imageButtom = new ImageButton(context);
			tmp_imageButtom.setLayoutParams(lp);
			tmp_imageButtom.setPadding(0, 0, 0, 0);
			File imgFile = new File(getFilesDir(), fm.getLogo());
			if(imgFile.exists()){
				try {
					loadImageToButtom(imgFile, tmp_imageButtom);
				} catch (FileNotFoundException e) { }
			}

			///
			tmp_imageButtom.setScaleType( ImageView.ScaleType.FIT_CENTER );
			tmp_imageButtom.setId(fm.getIndex());
			tmp_imageButtom.setBackgroundColor(Color.TRANSPARENT);
			tmp_imageButtom.setDrawingCacheEnabled(true);
			tmp_imageButtom.setOnDragListener(new FridgeMagnetOnDragListener());
			tmp_imageButtom.setOnTouchListener(fridgeMagnetOnTouchListener);
			tmp_imageButtom.setOnLongClickListener(new View.OnLongClickListener() {				
				@Override
				public boolean onLongClick(View v) {
					if(!isFridgeMagnetsDraggable() && !isHasScrolled()){
						showEditMagnetView(v);
					}
					return true;
				}
			});
			RelativeLayout rl = new RelativeLayout(context);
			rl.setLayoutParams(lp);
			rl.addView(tmp_imageButtom);
			if(MainActivity.getMyFridgeMagnets().indexOf(fm)%2 == 0){
				left_pane_fridgemagnets.addView(rl);
			}else{
				right_pane_fridgemagnets.addView(rl);
			}
		}
	}

	/**********************************************/

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

	public void goToFlyer(View view, Store fm) {
		Intent intent = new Intent(view.getContext(), FlyerActivity.class);
		Bundle b = new Bundle();
		b.putString("id_marca", fm.getId());
		b.putString("logo", fm.getLogo());
		b.putString("nombre", fm.getName());		
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

	protected void swapFridgeMagnetsInList(View view, View v) {
		Store from_view = null, from_v = null;
		for(Store fm:getMyFridgeMagnets()){
			if(fm.getIndex() == view.getId()){
				from_view = fm;
			}else if(fm.getIndex() == v.getId()){
				from_v = fm;
			}
		}
		if(from_v!=null && from_view!=null){
			List<Store> myFridgeMagnets = getMyFridgeMagnets();
			int i = myFridgeMagnets.indexOf(from_v), j = myFridgeMagnets.indexOf(from_view);
			Collections.swap(myFridgeMagnets, i, j);
			
			for(int k=0; k<myFridgeMagnets.size(); k++){
				Store tmp = myFridgeMagnets.get(k);
				Log.e("INDEX BEFORE", tmp.getIndex()+"");
				tmp.setIndex(k);
				Log.e("INDEX AFTER", tmp.getIndex()+"");
				myFridgeMagnets.set(k, tmp);
			}
			setMyFridgeMagnets(myFridgeMagnets);
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
		Store fm_tmp = new Store();

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					//Yes button clicked
					for(Store fm: getMyFridgeMagnets()){
						if(fm.getIndex() == v.getId()){
							removeMyFridgeMagnet(fm);
							loadFridgeMagnetsButtons(getMyFridgeMagnets());
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

		for(Store fm: getMyFridgeMagnets()){
			if(fm.getIndex() == v.getId()) fm_tmp = fm;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getText(R.string.delete_fridge_magnet)+" "+fm_tmp.getName()+"?")
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
		this.setFridgeMagnetsDraggable(true);
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
				setFridgeMagnetsDraggable(false);
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
		hideEditMagnetView();
	}

	private void chageOpacityFridgeMagnets(float opacity) {
		LinearLayout left_pane_fridgemagnets = (LinearLayout) findViewById(R.id.left_pane_fridgemagnets);
		LinearLayout right_pane_fridgemagnets = (LinearLayout)findViewById(R.id.right_pane_fridgemagnets);
		for (int i=0; i<left_pane_fridgemagnets.getChildCount(); i++) left_pane_fridgemagnets.getChildAt(i).setAlpha(opacity);
		for (int i=0; i<right_pane_fridgemagnets.getChildCount(); i++) right_pane_fridgemagnets.getChildAt(i).setAlpha(opacity);
	}

	public void showEditMagnetView(final View view){
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
		if(isFridgeMagnetsDraggable()){
			setFridgeMagnetsDraggable(false);
			chageOpacityFridgeMagnets(1.0f);
		}
		if(editMagnetViewShowed){
			editMagnetViewShowed = false;
			hideEditMagnetView();
		}else{
			super.onBackPressed();
		}
	}
	
	public void loadImageToButtom(File imgFile, ImageButton tmp_imageButtom) throws FileNotFoundException {
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

	public static String[] getMyFridgeMagnetsId() {
		String[] result = new String[getMyFridgeMagnets().size()];
		for (int i=0; i<getMyFridgeMagnets().size(); i++){
			result[i] = getMyFridgeMagnets().get(i).getId();
		}
		return result;
	}
}
