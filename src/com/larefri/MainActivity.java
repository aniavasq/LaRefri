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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
	
	private final String PREFS_NAME = "LaRefriPrefsFile";
	private final Integer movViewId = 20000000, delViewId = 20000005, enableViewId = 20000010;
	private final Integer delay = 800;
	private Integer width;
	private Context context;
	private Object[] movNdel;
	
	class ThisRestClient extends RestClient{

		/** progress dialog to show user that the backup is processing. */
	    @Override
	    protected void onPreExecute() {
	        this.dialog = new ProgressDialog(context);
	        this.dialog.setMessage("Cargando...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setIndeterminate(false);
	        this.dialog.show();
	        this.dialog.setCancelable(false);
	        this.dialog.setCanceledOnTouchOutside(false);
	    }
		
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			if (dialog.isShowing()) {
	            dialog.dismiss();
			}  
		}		

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
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
                	//Log.e("image", imageOutputStream.toString());
                	onProgressUpdate(i.toString(), Integer.toString(categories.size()));
                	i++;
                }

    			return httpResponse;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.context = this;
		
		//Set policy to HTTP
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        //get metrics for relative size from display width multiples
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		this.width = dm.widthPixels;
		//this.context = this;
		
		//setContentView(R.layout.activity_main);
				
		//check first time installed
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);       
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
			Log.e("files copied to: ",(new File(getFilesDir(),"pique.png")).toString());
		    settings.edit().putBoolean("my_first_time", false).commit(); 
		}
        movNdel = createEditMagnetView();				
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
	        Log.e("tag", "Failed to get asset file list.", e);
	    }
	    for(String filename : files) {
	        InputStream in = null;
	        FileOutputStream out = null;
	        try {
	          in = assetManager.open(filename);
	          //File outFile = new File(getExternalFilesDir(null), filename);
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
	          //File outFile = new File(getExternalFilesDir(null), filename);
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
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
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
			// TODO Auto-generated catch block
			Log.e("No file",e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onStart();
		//refresh user location and update phone guide
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
	    Log.e("Imantado",fm.nombre);
		
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
		final Handler handler = new Handler();
		FridgeMagnetsManager fridgeMagnetReader = new FridgeMagnetsManager();
		//List<FridgeMagnet> fridgeMagnets = fridgeMagnetReader.readJsonStream(getAssets().open("data.json"));
		List<FridgeMagnet> fridgeMagnets = fridgeMagnetReader.readJsonStream( new FileInputStream(new File(getFilesDir(), "data.json")) );
		for(final FridgeMagnet fm: fridgeMagnets){
			ImageButton tmp_imageButtom = new ImageButton(this);
			//Drawable d = Drawable.createFromStream(getAssets().open(fm.logo), null);
			File imgFile = new File(getFilesDir(), fm.logo);
			Drawable d = Drawable.createFromPath(imgFile.getAbsolutePath());
			tmp_imageButtom.setLayoutParams(lp);
			tmp_imageButtom.setImageDrawable(d);
			tmp_imageButtom.setScaleType( ImageView.ScaleType.FIT_CENTER );
			tmp_imageButtom.setId(fm.id_marca);
			tmp_imageButtom.setBackgroundColor(Color.TRANSPARENT);
			tmp_imageButtom.setOnDragListener(new OnDragListener() {
				
				@Override
				public boolean onDrag(View v, DragEvent evnt) {
					// TODO Auto-generated method stub
					Integer action = evnt.getAction();
					switch(action){
					case DragEvent.ACTION_DRAG_STARTED:
						v.invalidate();
						return true;
					case DragEvent.ACTION_DRAG_LOCATION:
						//collisionDetection(evnt, (View)movNdel[0] );
						break;
					case DragEvent.ACTION_DRAG_ENTERED:
						//Log.e("",""+v.getId());
						break;
					case DragEvent.ACTION_DROP:
						View view = (View) evnt.getLocalState();
						if(view!=v){
					        ImageButton container = (ImageButton) v;
					        ViewGroup owner = (ViewGroup) view.getParent(), destiny = ((ViewGroup)container.getParent());
					        owner.removeView(view);
					        destiny.addView(view);
					        destiny.removeView(container);
					        owner.addView(container);
					        view.setVisibility(View.VISIBLE);
				        }
				        break;
					case DragEvent.ACTION_DRAG_ENDED:
						// Report the drop/no-drop result to the user
		                final boolean dropped = evnt.getResult();
		                if (dropped) {
		                    //Log.e("Big Error mudafaca","THIS SHOULD BE DROPPED: "+v.toString());
		                }
		                break;

                    default:
                    	break;
					}
					return true;
				}
			});
			tmp_imageButtom.setOnTouchListener(new OnTouchListener() {
				
				private Integer touching;
				private Float mDownX;
				private Float mDownY;
				private final Float SCROLL_THRESHOLD = 10.0f;
				Runnable counting = new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						handler.postDelayed(this, delay);
						touching++;
					}
				};
				
				@Override
				public boolean onTouch(View v, MotionEvent evt) {
					// TODO Auto-generated method stub
					Integer action = evt.getAction() & MotionEvent.ACTION_MASK;
					
					v.performClick();
					switch (action) {
					case MotionEvent.ACTION_DOWN:
			            mDownX = evt.getX();
			            mDownY = evt.getY();
						touching = 0;
						handler.removeCallbacks(counting);
						handler.post(counting);
						return true;
						
					case MotionEvent.ACTION_MOVE:
						if((touching!=null) && (touching>1)){
							showEditMagnetView(evt);
							
							
							/*ClipData data = ClipData.newPlainText("", "");
						    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
						    v.startDrag(data, shadowBuilder, v, 0);
						    */
							//Log.e("","ismoving");
						}
						return true;
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:

						if(movNdel!=null){
							hideEditMagnetView();
						}
						handler.removeCallbacks(counting);	
						if( (touching==1) && (Math.abs(mDownX - evt.getX()) < SCROLL_THRESHOLD) && (Math.abs(mDownY - evt.getY()) < SCROLL_THRESHOLD) ){
							goToFlyer(v, fm);						
						}
						touching = 0;
						return true;
						
					default:
						break;
					}
					return false;
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
	
	private Object[] createEditMagnetView(){
		RelativeLayout mov = new RelativeLayout(this);
		RelativeLayout del = new RelativeLayout(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width/4, width/4);
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
		
		
		movImg.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent evt) {
				// TODO Auto-generated method stub
				Integer action = evt.getAction() & MotionEvent.ACTION_MASK;
				
				v.performClick();
				if(action == MotionEvent.ACTION_DOWN)
					Log.e("touched",""+v.getId());
				return true;
			}
		});
		
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
	
	private void showEditMagnetView(MotionEvent evt){
		View mov = (RelativeLayout)findViewById(movViewId);
		View del = (RelativeLayout)findViewById(delViewId);
		View enable_layout =  (RelativeLayout)findViewById(enableViewId);
		if(evt.getRawX() < width/2){
			mov.setX(evt.getRawX()+width/4);
			mov.setY(evt.getRawY()-width/2);
			del.setX(evt.getRawX()+width/4);
			del.setY(evt.getRawY());
		}else{
			mov.setX(evt.getRawX()-width/2);
			mov.setY(evt.getRawY()-width/2);
			del.setX(evt.getRawX()-width/2);
			del.setY(evt.getRawY());
		}
		
		mov.setVisibility(View.VISIBLE);
		del.setVisibility(View.VISIBLE);
		enable_layout.setVisibility(View.VISIBLE);
	}
	
	private void hideEditMagnetView(){
		( (RelativeLayout)findViewById(movViewId) ).setVisibility(View.INVISIBLE);
		( (RelativeLayout)findViewById(delViewId) ).setVisibility(View.INVISIBLE);
		( (RelativeLayout)findViewById(enableViewId) ).setVisibility(View.INVISIBLE);
	}
	
	/*private boolean collisionDetection(DragEvent evnt, View v){
		
		Integer w0 = v.getLeft();
		Integer wt = v.getRight();
		Integer h0 = v.getTop();
		Integer ht = v.getBottom();
		//Log.e("W",""+w+","+v.getWidth() );
		//Log.e("H",""+h+","+v.getHeight() ); 
		int[] array = new int[2];
		array[1] = (int)v.getX();
		array[0] = (int)v.getY();
				
		Log.e("POSITION", ""+(width/2+array[0])+","+array[1]);
		//v.getLocationOnScreen(array);
		//Log.e("POSITION ON SCREEN", ""+array[0]+","+array[1]);
		
		Log.e("DRAG MOVED"," ("+evnt.getX()+","+evnt.getY());
		Log.e("COLLISION BOX","("+w0+","+wt+")"+" ("+h0+","+ht+")");
		if( evnt.getY()<(array[1]+h0) && evnt.getY()<(array[1]+ht) && evnt.getX()<(array[0]+w0) && evnt.getX()<(array[0]+wt) ){
			Log.e("Collision", "Collision detected with "+v.getId());
		}
		return false;
	}*/
}
