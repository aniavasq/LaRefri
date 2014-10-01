package com.larefri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
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
		
		settings = getSharedPreferences("LaRefriPrefsFile", 0);
		
		ImageView image = (ImageView)findViewById(R.id.magnetfridge_logo);
		TextView nameview = (TextView)findViewById(R.id.magnetfridge_name);
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
        
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LaRefriLocationListener(context);
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
	}

	private void loadStores(List<Store> stores){		
		LinearLayout store_call_pane = (LinearLayout) findViewById(R.id.stores_call_buttons);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
		Resources resources = getResources();
		ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_button);
	
		for(final Store s: stores){
			if(s!=null){
				LinearLayout phone_num_pane = new LinearLayout(themeWrapper);
				phone_num_pane.setOrientation(LinearLayout.VERTICAL);
				phone_num_pane.setLayoutParams(lp);
				
				TextView tmp_title = new Button(themeWrapper);
				tmp_title.setLayoutParams(lp);
				tmp_title.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
				tmp_title.setBackgroundColor(Color.parseColor("#4B4743"));
				tmp_title.setText(s.nombre);
				tmp_title.setTextColor(Color.WHITE);
				tmp_title.setGravity(Gravity.LEFT);
				//tmp_title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
				tmp_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_contact, 0);
				tmp_title.setPadding(0, 0, 5, 0);				
				phone_num_pane.addView(tmp_title);
				
				TextView phone_num = new Button(themeWrapper);
				phone_num.setLayoutParams(lp);
				phone_num.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
				phone_num.setText(s.telefono);
				phone_num.setTextColor(Color.WHITE);
				phone_num.setGravity(Gravity.LEFT);
				//phone_num.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
				phone_num.setPadding(0, 0, 0, 15);				
				phone_num_pane.addView(phone_num);
				
				if(!s.telefono2.isEmpty()){
					TextView phone_num2 = new Button(themeWrapper);
					phone_num2.setLayoutParams(lp);
					phone_num2.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
					phone_num2.setText(s.telefono2);
					phone_num2.setTextColor(Color.WHITE);
					phone_num2.setGravity(Gravity.LEFT);
					//phone_num2.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
					phone_num2.setPadding(0, 0, 0, 15);				
					phone_num_pane.addView(phone_num2);
				}
				
				if(!s.telefono3.isEmpty()){
					TextView phone_num3 = new Button(themeWrapper);
					phone_num3.setLayoutParams(lp);
					phone_num3.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
					phone_num3.setText(s.telefono3);
					phone_num3.setTextColor(Color.WHITE);
					phone_num3.setGravity(Gravity.LEFT);
					//phone_num3.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
					phone_num3.setPadding(0, 0, 0, 15);				
					phone_num_pane.addView(phone_num3);
				}
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
		this.finish();
	}
}
