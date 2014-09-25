package com.larefri;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CallActivity extends Activity {
	private SharedPreferences settings;
	private Integer id_marca;
	private String logo;
	private String nombre;
	private Context context;
	
	class ThisRestClient extends RestClient{

		/** progress dialog to show user that the backup is processing. */
	    private List<Store> stores;
		
	    @Override
		protected void onPreExecute() {
	        this.dialog = new ProgressDialog(context);
	        this.dialog.setMessage("Actualizando Locales");
	        this.dialog.show();
	        this.dialog.setCancelable(false);
	        this.dialog.setCanceledOnTouchOutside(false);
	    }
		
		@Override
		protected void onPostExecute(Object result) {
			if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
			/*InputStream is = new ByteArrayInputStream(result.toString().getBytes());
			StoresManager storesManager = new StoresManager();*/
			
			LinearLayout store_call_pane = (LinearLayout) findViewById(R.id.stores_call_buttons);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
			Resources resources = getResources();
			ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_button);			
			
			try {
				for(final Store s: stores){
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
								onCall(v, s.telefono);
								
							}
						});
						store_call_pane.addView(tmp_button);
					}
				}
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
			super.onPostExecute(result);
		}

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			Object result = super.doInBackground(params);
			InputStream is = new ByteArrayInputStream(result.toString().getBytes());
			try {
				stores = (new StoresManager()).readJsonStream(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call);
		this.context = this;
		settings = getSharedPreferences("LaRefriPrefsFile", 0);
		
		//get extra content from previous activity
		Bundle b = getIntent().getExtras();
		id_marca = b.getInt("id_marca");
		logo = b.getString("logo");
		nombre = b.getString("nombre");
		
		ImageView image = (ImageView)findViewById(R.id.magnetfridge_logo);
		TextView nameview = (TextView)findViewById(R.id.magnetfridge_name);
		
		//Set policy to HTTP
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("id_marca", id_marca.toString());
		//construct form to HttpRequest
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("id_marca", id_marca.toString()));
        
		/*(new ThisRestClient()).execute(
				StaticUrls.SUCURSALES_URL, 
				params,
				nameValuePairs);*/
        try {
        	loadStores((new StoresManager()).readJsonStream( new FileInputStream(new File(getFilesDir(), this.id_marca+"_sucursales.json")) ));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File imgFile = new File(getFilesDir(), logo);
		Drawable d = Drawable.createFromPath(imgFile.getAbsolutePath());
		image.setImageDrawable(d);
		nameview.setText(nombre);		

        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LaRefriLocationListener(context);
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		setBackground();
	}
	
	private void loadStores(List<Store> stores){		
		LinearLayout store_call_pane = (LinearLayout) findViewById(R.id.stores_call_buttons);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
		Resources resources = getResources();
		ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_button);
	
		for(final Store s: stores){
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
						onCall(v, s.telefono);
						
					}
				});
				store_call_pane.addView(tmp_button);
			}
		}
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
	
	public void onCall(View view, String phone){
	    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phone));
	    startActivity(callIntent);
	}
}
