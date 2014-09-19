package com.larefri;

import java.io.File;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flyer_activity);
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
		//image.setImageDrawable(Drawable.createFromStream(getAssets().open(logo), null));
		nameview.setText(nombre);
		//Log.v("id_marca",""+id_marca);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.flyer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onBackPressed(View view) {
		this.finish();
	}
	
	public void onCall(View view) {
		Intent intent = new Intent(view.getContext(), CallActivity.class);
		Bundle b = new Bundle();
	    b.putInt("id_marca", id_marca); //Your id
	    b.putString("logo", logo);
	    b.putString("nombre", nombre);
	    intent.putExtras(b); //Put your id to your next Intent
	    this.startActivity(intent);
	}
	
	public void onPhoneGuide(View view) {
		Intent intent = new Intent(view.getContext(), PhoneGuideActivity.class);
		Bundle b = new Bundle();
	    b.putInt("id_marca", id_marca); //Your id
	    b.putString("logo", logo);
	    b.putString("nombre", nombre);
	    intent.putExtras(b); //Put your id to your next Intent
	    this.startActivity(intent);
	}
}