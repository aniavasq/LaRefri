package com.larefri;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CallActivity extends Activity {
	private Integer id_marca;
	private String logo;
	private String nombre;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call);
		//get extra content from previous activity
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
		
		//Set policy to HTTP
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("id_marca", id_marca.toString());
		//construct form to HttpRequest
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("id_marca", id_marca.toString()));
        
		try {
			Object http_repsonse = (new RestClient()).doInBackground(
					StaticUrls.SUCURSALES_URL, 
					params,
					nameValuePairs);
			
			InputStream is = new ByteArrayInputStream(http_repsonse.toString().getBytes());
			StoresManager storesManager = new StoresManager();
			
			LinearLayout store_call_pane = (LinearLayout) findViewById(R.id.stores_call_buttons);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
			Resources resources = getResources();
			ContextThemeWrapper themeWrapper = new ContextThemeWrapper(this, R.style.menu_button);
			
			for(final Store s: storesManager.readJsonStream(is)){
				if(s!=null){
					Button tmp_button = new Button(themeWrapper);
					tmp_button.setLayoutParams(lp);
					tmp_button.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
					tmp_button.setText(s.nombre);
					tmp_button.setTextColor(Color.WHITE);
					tmp_button.setGravity(Gravity.LEFT);
					tmp_button.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
					tmp_button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_call, 0);
					tmp_button.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							onCall(v, s.telefono);
							
						}
					});
					store_call_pane.addView(tmp_button);
				}
			}
			
			//tv.setText(storesReader.readJsonStream(is).toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onBackPressed(View view) {
		this.finish();
	}
	
	public void onCall(View view, String phone){
	    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phone));
	    startActivity(callIntent);
	}
}
