package com.larefri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PhoneGuideActivity extends Activity {
	private SharedPreferences settings;
	private Integer id_marca;
	private String logo;
	private String nombre;
	private Context context;
    Location currentLocation;
    double currentLatitude;
    double currentLongitude;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_guide);
		this.context = this;
		//get extra content from previous activity
		Bundle b = getIntent().getExtras();
		id_marca = b.getInt("id_marca");
		logo = b.getString("logo");
		nombre = b.getString("nombre");
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;	
		
		settings = getSharedPreferences("LaRefriPrefsFile", 0);
		
		ImageView image = (ImageView)findViewById(R.id.magnetfridge_logo);
		TextView nameview = (TextView)findViewById(R.id.magnetfridge_name);
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(width/2-20,width/2-20);
		image.setLayoutParams(ll);
		File imgFile = new File(getFilesDir(), logo);
		Drawable d = Drawable.createFromPath(imgFile.getAbsolutePath());
		image.setImageDrawable(d);
		nameview.setText(nombre);
		
		//Set policy to HTTP
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        try {
        	loadStores((new StoresManager()).readJsonStream( new FileInputStream(new File(getFilesDir(), this.id_marca+"_sucursales.json")) ));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadStores(List<Store> stores){		
		LinearLayout store_call_pane = (LinearLayout) findViewById(R.id.stores_call_buttons);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
		Resources resources = getResources();
		ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_button);
	
		for(final Store s: stores){
			if(s!=null && s.ciudad.equalsIgnoreCase(settings.getString("current_city", "NO_CITY"))){
				LinearLayout phone_num_pane = new LinearLayout(themeWrapper);
				phone_num_pane.setOrientation(LinearLayout.VERTICAL);
				phone_num_pane.setLayoutParams(lp);
				
				TextView tmp_title = new Button(themeWrapper);
				tmp_title.setLayoutParams(lp);
				tmp_title.setBackground(resources.getDrawable(R.drawable.menu_label_bg));
				tmp_title.setText(s.nombre);
				tmp_title.setTextColor(Color.WHITE);
				tmp_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_contact, 0);
				tmp_title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				tmp_title.setPadding(10, 0, 10, 1);	
				phone_num_pane.addView(tmp_title);
				
				TextView phone_dir = new TextView(themeWrapper);
				phone_dir.setLayoutParams(lp);
				phone_dir.setText(s.direccion);
				phone_dir.setTextColor(Color.WHITE);
				phone_dir.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				phone_dir.setPadding(10, 0, 10, 1);				
				phone_num_pane.addView(phone_dir);
				LinearLayout ll = new LinearLayout(this);
				LinearLayout.LayoutParams trlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20);
				ll.setLayoutParams(trlp);
				ll.setBackgroundColor(Color.TRANSPARENT);
				phone_num_pane.addView(ll);
				store_call_pane.addView(phone_num_pane);
			}		
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
		Intent intent = new Intent(view.getContext(), FlyerActivity.class);
		Bundle b = new Bundle();
		b.putInt("id_marca", this.id_marca);
		b.putString("logo", this.logo);
		b.putString("nombre", this.nombre);		
		intent.putExtras(b);
	    this.startActivity(intent);
	    this.finish();
	}
	
	public void onHomePressed(View view){
		Intent intent = new Intent(view.getContext(), MainActivity.class);
	    this.startActivity(intent);
	    this.finish();
	}
}
