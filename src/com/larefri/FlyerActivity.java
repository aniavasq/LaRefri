package com.larefri;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FlyerActivity extends Activity {
	
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
		
		ImageView image = (ImageView)findViewById(R.id.magnetfridge_logo);
		TextView nameview = (TextView)findViewById(R.id.magnetfridge_name);
		try {
			image.setImageDrawable(Drawable.createFromStream(getAssets().open(logo), null));
			nameview.setText(nombre);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.v("id_marca",""+id_marca);
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
