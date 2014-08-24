package com.larefri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
	
	final String PREFS_NAME = "LaRefriPrefsFile";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//Set policy to HTTP
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
				
		//check first time installed
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);       
        if (settings.getBoolean("my_first_time", true)) {
		    //the app is being launched for first time, do something        
		    Log.e("Comments", "First time for ");//+android_id);
		           // first time task
		    try{
		    	Object http_repsonse = (new RestClient()).doInBackground(
						StaticUrls.CATEGORIES, 
						new HashMap<Object, Object>(),
						new ArrayList<Object>());
		        FileOutputStream outputStream;
		        outputStream = openFileOutput("categories.json", Context.MODE_PRIVATE);
		        outputStream.write(http_repsonse.toString().getBytes());
		        outputStream.close();       
			
			    List<Category> categories = CategoriesActivity.getCategoriesFromJSON(new File(getFilesDir(),"categories.json"));
		        for(Category c: categories){
		        	//if(c!=null){
			        	InputStream imageInputStream=new URL(StaticUrls.ICONS_CATEGORIES+c.icono_categoria).openStream();
			        	FileOutputStream imageOutputStream;
			        	imageOutputStream = openFileOutput(c.icono_categoria, Context.MODE_PRIVATE);
			        	CopyStream(imageInputStream, imageOutputStream);
			        	Log.e("outputImage",imageOutputStream.toString());
			        	imageOutputStream.close();
	        	//}
		        }
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
		    // record the fact that the app has been started at least once
		    settings.edit().putBoolean("my_first_time", false).commit(); 
		} 
		
		//create internal folder
		ContextWrapper cw = new ContextWrapper(this);
		/**File directory = **/cw.getDir("media", Context.MODE_PRIVATE);
		
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		
		//add fridge magnets from local data
		LinearLayout left_pane_fridgemagnets = (LinearLayout) findViewById(R.id.left_pane_fridgemagnets);
		LinearLayout right_pane_frudgemagnets = (LinearLayout)findViewById(R.id.right_pane_fridgemagnets);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, width/2);
		lp.setMargins(0, 0, 0, 0);
		
		try {
			FridgeMagnetsReader fridgeMagnetReader = new FridgeMagnetsReader();
			List<FridgeMagnet> fridgeMagnets = fridgeMagnetReader.readJsonStream(getAssets().open("data.json"));
			for(final FridgeMagnet fm: fridgeMagnets){
				ImageButton tmp_imageButtom = new ImageButton(this);
				Drawable d = Drawable.createFromStream(getAssets().open(fm.logo), null);
				tmp_imageButtom.setLayoutParams(lp);
				tmp_imageButtom.setImageDrawable(d);
				tmp_imageButtom.setScaleType( ImageView.ScaleType.FIT_CENTER );
				tmp_imageButtom.setId(fm.id_marca);
				tmp_imageButtom.setBackgroundColor(Color.TRANSPARENT);
				tmp_imageButtom.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						goToFlyer(v, fm);
					}
				});
				if(fridgeMagnets.indexOf(fm)%2 == 0){
					left_pane_fridgemagnets.addView(tmp_imageButtom);
				}else{
					right_pane_frudgemagnets.addView(tmp_imageButtom);
				}
			}	

	        
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("No file",e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//refresh user location and update phone guide
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** Called when the user clicks the Send button */
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
}
