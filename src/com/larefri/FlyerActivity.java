package com.larefri;

import java.io.File;
import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FlyerActivity extends Activity {

	private SharedPreferences settings;
	private String id_marca;
	private String logo;
	private String nombre;
	private Integer width;
	private BitmapLRUCache mMemoryCache;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flyer_activity);
		Bundle b = getIntent().getExtras();
		this.id_marca = b.getString("id_marca");
		this.logo = b.getString("logo");
		this.nombre = b.getString("nombre");		
		this.settings = getSharedPreferences("LaRefriPrefsFile", 0);
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		this.width = dm.widthPixels;
		this.context = this;

		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	    final int cacheSize = maxMemory / 8;
	    mMemoryCache = new BitmapLRUCache(cacheSize);
	    
	    RecyclingImageView logo_image = (RecyclingImageView)findViewById(R.id.magnetfridge_logo);
		/*List<Promotion> flyers = getFlyer(this.id_marca);

		try {
			for (Promotion f: flyers){
				ImageView flyer = (ImageView)findViewById(R.id.flyer_view);
				File imgFlyer = new File(getFilesDir(), f.imagen);
				loadImageToFlyer(imgFlyer, flyer);
			}
		} catch (FileNotFoundException e) { }*/
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(width/2,width/2);
		logo_image.setLayoutParams(ll);
		TextView nameview = (TextView)findViewById(R.id.magnetfridge_name);
		File imgFile = new File(getFilesDir(), logo);
		Drawable d = Drawable.createFromPath(imgFile.getAbsolutePath());
		logo_image.setImageDrawable(d);
		nameview.setText(nombre);
	    loadPromotions();
	}

	private void loadImageToFlyer(File imgFile){
		final String imageKey = String.valueOf(imgFile);

		Bitmap bitmap = mMemoryCache.getBitmapFromMemCache(imageKey);
		ImageView flyer = (ImageView)findViewById(R.id.flyer_view);
		if (bitmap != null) {
			flyer.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
		} else {
			bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			flyer.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
			mMemoryCache.addBitmapToMemoryCache(imageKey, bitmap);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		setBackground();
	}

	protected void setBackground() {
		RelativeLayout head = (RelativeLayout)findViewById(R.id.relativeLayout1);
		LinearLayout article = (LinearLayout)findViewById(R.id.article);
		int bg_color = settings.getInt("bg_color", Color.parseColor("#999089"));
		int menu_bg_color = settings.getInt("menu_bg_color", Color.parseColor("#6B6560"));
		article.setBackgroundColor(menu_bg_color);
		head.setBackgroundColor(bg_color);
	}

	public void onBackPressed(View view) {
		onHomePressed(view);
	}

	public void onCall(View view) {
		Intent intent = new Intent(view.getContext(), CallActivity.class);
		Bundle b = new Bundle();
		b.putString("id_marca", id_marca);
		b.putString("logo", logo);
		b.putString("nombre", nombre);
		intent.putExtras(b);
		this.startActivity(intent);
		this.finish();
	}

	public void onPhoneGuide(View view) {
		Intent intent = new Intent(view.getContext(), PhoneGuideActivity.class);
		Bundle b = new Bundle();
		b.putString("id_marca", id_marca); //ID
		b.putString("logo", logo);
		b.putString("nombre", nombre);
		intent.putExtras(b);
		this.startActivity(intent);
		this.finish();
	}


	public void onHomePressed(View view){
		Intent intent = new Intent(view.getContext(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		this.startActivity(intent);
		this.finish();
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}
	
	private void loadPromotions() {
		ParseQuery<ParseObject> innerQuery = new ParseQuery<ParseObject>("Store");
		innerQuery.fromLocalDatastore();
		innerQuery.whereEqualTo("objectId",this.id_marca);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Promotion");
		query.fromLocalDatastore();
		query.whereMatchesQuery("store", innerQuery);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> result, ParseException e) {
				if (e == null) {
					//List<Promotion> promotions = new ArrayList<Promotion>();
					for (ParseObject parseObject: result){
						Promotion promotion = new Promotion(parseObject, context);
						
						File imgFile = new File(getFilesDir(), promotion.getName());
						Log.e("FLYER", imgFile.toString());
						if(imgFile.exists()){
							//try {
								loadImageToFlyer(imgFile);
								return;
							//} catch (FileNotFoundException ioe) { }
						}else{
							Log.e("FLYER", "File does not exist");
						}
					}
				}
			}
		});
	}
}
