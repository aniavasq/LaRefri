package com.larefri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FlyerActivity extends Activity {
	
	private SharedPreferences settings;
	private Integer id_marca;
	private String logo;
	private String nombre;
	private Integer width;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flyer_activity);
		Bundle b = getIntent().getExtras();
		this.id_marca = b.getInt("id_marca");
		this.logo = b.getString("logo");
		this.nombre = b.getString("nombre");		
		this.settings = getSharedPreferences("LaRefriPrefsFile", 0);
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		this.width = dm.widthPixels;		
		
		ImageView image = (ImageView)findViewById(R.id.magnetfridge_logo);
		//RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int)(width*0.95),(int)(width*0.95));

		List<Flyer> flyers = getFlyer(this.id_marca);
		for (Flyer f: flyers){
			ImageView flyer = (ImageView)findViewById(R.id.flyer_view);
			File imgFlyer = new File(getFilesDir(), f.imagen);
			Drawable df = Drawable.createFromPath(imgFlyer.getAbsolutePath());			
			flyer.setImageDrawable(df);
		}
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(width/2,width/2);
		image.setLayoutParams(ll);
		TextView nameview = (TextView)findViewById(R.id.magnetfridge_name);
		File imgFile = new File(getFilesDir(), logo);
		Drawable d = Drawable.createFromPath(imgFile.getAbsolutePath());
		image.setImageDrawable(d);
		//image.setImageDrawable(Drawable.createFromStream(getAssets().open(logo), null));
		nameview.setText(nombre);
	}

	private List<Flyer> getFlyer(Integer id_marca) {		
		try {
			File fl = new File(getFilesDir(),id_marca+"_flyer.json");
			FileInputStream fin;
			fin = new FileInputStream(fl);
		    List<Flyer> ret = (new FlyerManager()).readJsonStream(fin);
		    fin.close();
		    return ret;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<Flyer>();
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
	    b.putInt("id_marca", id_marca);
	    b.putString("logo", logo);
	    b.putString("nombre", nombre);
	    intent.putExtras(b);
	    this.startActivity(intent);
	    this.finish();
	}
	
	public void onPhoneGuide(View view) {
		Intent intent = new Intent(view.getContext(), PhoneGuideActivity.class);
		Bundle b = new Bundle();
	    b.putInt("id_marca", id_marca); //ID
	    b.putString("logo", logo);
	    b.putString("nombre", nombre);
	    intent.putExtras(b); //Put your id to your next Intent
	    this.startActivity(intent);
	    this.finish();
	}
	
	public void onHomePressed(View view){
		Intent intent = new Intent(view.getContext(), MainActivity.class);
	    this.startActivity(intent);
	    this.finish();
	}
}
