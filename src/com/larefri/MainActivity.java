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
import java.util.Map;

import org.apache.http.NameValuePair;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class MainActivity extends Activity {
	
	private final String PREFS_NAME = "LaRefriPrefsFile";
	private final Integer movViewId = 20000000, delViewId = 20000005, enableViewId = 20000010;
	private final Integer delay = 700;
	private final Handler handler = new Handler();
	private SharedPreferences settings;
	private Integer width, height;
	private Context context;
	private Object[] movNdel;
	private boolean fridgeMagnetsClickable;
	private boolean fridgeMagnetsDraggable;
	private List<FridgeMagnet> fridgeMagnets;
	private ScrollView myScrollView;
	private LocationTask locationTask;
	
	class ThisRestClient extends RestClient{
		@Override
	    protected void onPreExecute() {
	        this.dialog = new ProgressDialog(context);
	        this.dialog.setMessage("Cargando...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setIndeterminate(false);
	        this.dialog.setCancelable(false);
	        this.dialog.setCanceledOnTouchOutside(false);
	        this.dialog.show();
	    }
		
		@Override
		protected void onPostExecute(Object result) {
			if (dialog.isShowing()) {
	            dialog.dismiss();
			}  
		}		

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected Object doInBackground(Object... params) {
			try {
        		Object httpResponse = this.makeRequestByForm((String)params[0], (Map)params[1], (List<NameValuePair>)params[2]);    		
        		FileOutputStream outputStream;
        		List<Category> categories;        		
        		outputStream = openFileOutput("categories.json", Context.MODE_PRIVATE);
                outputStream.write(httpResponse.toString().getBytes());
                outputStream.close();
        		categories = CategoriesActivity.getCategoriesFromJSON(new File(getFilesDir(),"categories.json"));        		
                Integer i=1;
                for(Category c: categories){
                	//download images from server at the first time running
                	downloadImageFromServer(StaticUrls.ICONS_CATEGORIES, c.icono_categoria);
                	onProgressUpdate(i.toString(), Integer.toString(categories.size()));
                	i++;
                }
    			return httpResponse;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return super.doInBackground(params);
		}

		@Override
		protected void onProgressUpdate(String... values) {
		    if (values.length == 2) {
		    	//Log.e("progress",values[0].toString());
		        dialog.setProgress(Integer.parseInt(values[0]));
		        dialog.setMax(Integer.parseInt(values[1]));
		    }
		}
	}

	class FridgeMagnetOnTouchListener implements OnTouchListener {		
		private Integer touching;
		private FridgeMagnet fm;
		private Float mDownX;
		private Float mDownY;
		private final Float SCROLL_THRESHOLD = 10.0f;
		private final Runnable counting = new Runnable() {			
			@Override
			public void run() {
				handler.postDelayed(this, delay);
				touching++;
			}
		};

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
				touching = 0;
				if(!fridgeMagnetsDraggable){
					handler.removeCallbacks(counting);
					handler.post(counting);
				}else{
					return false;
				}
				return true;

			case MotionEvent.ACTION_MOVE:
				if((touching!=null) && (touching>1)){
					showEditMagnetView(v,evt);
				}
				if(fridgeMagnetsDraggable){

					Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
					try{
						int color = bmp.getPixel((int) evt.getX(), (int) evt.getY());
						if (color == Color.TRANSPARENT)
							return false;
						else {

							ClipData data = ClipData.newPlainText("", "");
							DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
							v.startDrag(data, shadowBuilder, v, 0);
							return true;
						}
					}catch(Exception donotcare){ }
				}
				return false;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				handler.removeCallbacks(counting);	
				if( (touching==1) && (Math.abs(mDownX - evt.getX()) < SCROLL_THRESHOLD) && (Math.abs(mDownY - evt.getY()) < SCROLL_THRESHOLD) && fridgeMagnetsClickable){
					goToFlyer(v, fm);						
				}
				touching = 0;
				return true;

			default:
				break;
			}
			return false;
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
				chageOpacityFridgeMagnets(v, 0.5f);
				break;
			case DragEvent.ACTION_DRAG_LOCATION:
				int[] array =new int[2];
				v.getLocationOnScreen(array);
				if (array[1] < height/4) {
					myScrollView.smoothScrollBy(0, -75);
				}
				if (array[1] > 3*height/4) {
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
			case DragEvent.ACTION_DRAG_ENDED:
                final boolean dropped = evnt.getResult();
                if (dropped) {
                	v.setAlpha(0.5f);
                	view = (View) evnt.getLocalState();
                	view.setAlpha(1.0f);
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
		
		FontsOverride.setDefaultFont(this, "MONOSPACE", "Roboto-Thin.ttf");
		//Set policy to HTTP
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        //get metrics for relative size from display width multiples
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		this.width = dm.widthPixels;
		this.height = dm.heightPixels;
		
		//check first time installed
		settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean("my_first_time", true)) {
		    //the APP is being launched for first time, do something        
		    Log.e("Comments", "First time for ");

		    //Create RestClient object to get HTTP response data and get the categories in the server
			(new ThisRestClient()).execute(
					StaticUrls.CATEGORIES, 
					new HashMap<Object, Object>(),
					new ArrayList<Object>());
		    // record the fact that the APP has been started at least once
			copyAssets();
		    settings.edit().putBoolean("my_first_time", false).commit(); 
		}
        this.movNdel = createEditMagnetView();	
        //ScrolView
        this.myScrollView = (ScrollView) findViewById(R.id.the_scroll_view);
        //Location
        locationTask = new LocationTask(context, this);
	}

	public void downloadImageFromServer(String url, String file) throws MalformedURLException, IOException {
		InputStream imageInputStream=new URL(url+file).openStream();
    	FileOutputStream imageOutputStream;
    	imageOutputStream = openFileOutput(file, Context.MODE_PRIVATE);
    	CopyStream(imageInputStream, imageOutputStream);
    	imageOutputStream.close();
	}
	
	private void copyAssets() {
	    AssetManager assetManager = getAssets();
	    String[] files = null, images = null;
	    try {
	        files = assetManager.list("");
	        images = assetManager.list("images");
	    	//files = assetManager.
	    } catch (IOException e) {
	        Log.e("tag", "Failed to get asset file list.");
	    }
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
	        } catch(IOException e) {
	            Log.e("tag", "Failed to copy asset file: " + filename, e);
	        }       
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
	        } catch(IOException e) {
	            Log.e("tag", "Failed to copy asset file: " + filename);
	        }       
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
		} catch (IOException e) {
			Log.e("No file",e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Update phone guide
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
	    this.startActivity(intent);
	}
	
	public void goToFlyer(View view, FridgeMagnet fm) {
	    Intent intent = new Intent(view.getContext(), FlyerActivity.class);
	    Bundle b = new Bundle();
	    b.putInt("id_marca", fm.id_marca); //Your id
	    b.putString("logo", fm.logo);
	    b.putString("nombre", fm.nombre);
		
	    intent.putExtras(b); //Put your id to your next Intent
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
	    catch(Exception ex){}
	}
	
	public void loadFridgeMagnetsFromFile(LinearLayout left_pane_fridgemagnets, LinearLayout right_pane_fridgemagnets, LayoutParams lp) throws IOException{
		FridgeMagnetsManager fridgeMagnetReader = new FridgeMagnetsManager();
		this.fridgeMagnets = fridgeMagnetReader.readJsonStream( new FileInputStream(new File(getFilesDir(), "data.json")) );
		for(final FridgeMagnet fm: fridgeMagnets){
			OnTouchListener fridgeMagnetOnTouchListener = new FridgeMagnetOnTouchListener(fm);
			ImageButton tmp_imageButtom = new ImageButton(this);
			File imgFile = new File(getFilesDir(), fm.logo);
			Drawable d = Drawable.createFromPath(imgFile.getAbsolutePath());
			tmp_imageButtom.setLayoutParams(lp);
			tmp_imageButtom.setImageDrawable(d);
			tmp_imageButtom.setScaleType( ImageView.ScaleType.FIT_CENTER );
			tmp_imageButtom.setId(fm.id_marca);
			tmp_imageButtom.setBackgroundColor(Color.TRANSPARENT);
			tmp_imageButtom.setDrawingCacheEnabled(true);
			tmp_imageButtom.setOnDragListener(new FridgeMagnetOnDragListener());
			tmp_imageButtom.setOnTouchListener(fridgeMagnetOnTouchListener);
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
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width/4, width/6);
		lp.setMargins(0, 0, 0, 0);
		lp.gravity = Gravity.BOTTOM;
		LinearLayout.LayoutParams enable_lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		enable_lp.setMargins(0, 0, 0, 0);
		enable_lp.gravity = Gravity.TOP;
		
		RelativeLayout scrollable = (RelativeLayout) findViewById(R.id.scrollable);
		mov.setLayoutParams(lp);
		del.setLayoutParams(lp);
		mov.setId(movViewId);
		del.setId(delViewId);
		ImageView movImg = new ImageView(context);
		ImageView delImg = new ImageView(context);
		movImg.setLayoutParams(lp);
		delImg.setLayoutParams(lp);
		movImg.setBackgroundColor(Color.TRANSPARENT);
		movImg.setScaleType( ImageView.ScaleType.FIT_CENTER );
		delImg.setBackgroundColor(Color.TRANSPARENT);
		delImg.setScaleType( ImageView.ScaleType.FIT_CENTER );
		movImg.setImageResource(R.drawable.ic_move);
		delImg.setImageResource(R.drawable.ic_delete);
		mov.addView(movImg);
		del.addView(delImg);
		mov.setVisibility(View.INVISIBLE);
		del.setVisibility(View.INVISIBLE);
		//Log.e("MagnetX",""+evt.getRawX()+" W "+width/2);
		
		RelativeLayout enable_layout = new RelativeLayout(context);
		enable_layout.setId(enableViewId);
		enable_layout.setLayoutParams(enable_lp);
		enable_layout.setBackgroundColor(Color.BLACK);
		enable_layout.setAlpha(0.5f);
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
		            //No button clicked
		        	modifyBackButton();
		            break;
		        }
		    }
		};

		for(FridgeMagnet fm: fridgeMagnets){
			if(fm.id_marca == v.getId()) fm_tmp = fm;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("¿Quiere eliminar el imantado de "+fm_tmp.nombre+"?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
	}

	protected void letsDragFridgeMagnets(View v) {
		this.fridgeMagnetsDraggable = true;
		chageOpacityFridgeMagnets(v, 0.5f);
		modifyOverflowButton();
	}

	private void modifyOverflowButton() {
		ImageButton menuButton = (ImageButton)findViewById(R.id.overflowbutton);
		menuButton.setImageResource(R.drawable.ic_action_previous_item);
		menuButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				fridgeMagnetsDraggable = false;
				chageOpacityFridgeMagnets(v, 1.0f);
				modifyBackButton();
			}
		});	
	}

	protected void modifyBackButton() {
		ImageButton mbtn = ( (ImageButton)findViewById(R.id.overflowbutton) );
		mbtn.setImageResource(R.drawable.ic_action_overflow);
		mbtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				goToMenu(v);
			}
		});
    	try {
			saveFridgeMagnetsList();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		hideEditMagnetView();
	}

	private void chageOpacityFridgeMagnets(View v, float opacity) {
		for(final FridgeMagnet fm: this.fridgeMagnets){
			ImageButton tmp_imageButtom = (ImageButton)findViewById(fm.id_marca);
			if(v.getId()!=fm.id_marca){
				tmp_imageButtom.setAlpha(opacity);
			}else{
				tmp_imageButtom.setAlpha(1.0f);
			}
		}		
	}

	private void showEditMagnetView(final View view, MotionEvent evt){
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
				if(action == MotionEvent.ACTION_DOWN)
					return true;
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
		modifyOverflowButton();
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
}
