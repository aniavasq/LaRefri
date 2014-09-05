package com.larefri;

import java.io.ByteArrayInputStream;
import java.io.File;
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
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PhoneGuideActivity extends Activity {
	private Integer id_marca;
	private String logo;
	private String nombre;
	private Context context;
	
	class ThisRestClient extends RestClient{

		/** progress dialog to show user that the backup is processing. */
	    private ProgressDialog dialog;
		
	    @Override
		protected void onPreExecute() {
	        dialog = new ProgressDialog(context);
	        this.dialog.setMessage("Actualizando Locales");
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
			//Log.e("result",result.toString());
			InputStream is = new ByteArrayInputStream(result.toString().getBytes());
			StoresManager storesManager = new StoresManager();
			
			LinearLayout store_call_pane = (LinearLayout) findViewById(R.id.stores_call_buttons);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
			Resources resources = getResources();
			ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_button);
			
			try {
				for(final Store s: storesManager.readJsonStream(is)){
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
						tmp_title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
						tmp_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_contact, 0);
						tmp_title.setPadding(0, 0, 5, 0);
						//tmp_title
						
						phone_num_pane.addView(tmp_title);
						
						TextView tmp_text = new Button(themeWrapper);
						tmp_text.setLayoutParams(lp);
						tmp_text.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
						tmp_text.setText(s.telefono);
						tmp_text.setTextColor(Color.WHITE);
						tmp_text.setGravity(Gravity.LEFT);
						tmp_text.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
						tmp_text.setPadding(0, 0, 0, 15);
						
						phone_num_pane.addView(tmp_text);
						
						store_call_pane.addView(phone_num_pane);
					}
				
				}
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.onPostExecute(result);
		}
		
	}
	
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
		
		ImageView image = (ImageView)findViewById(R.id.magnetfridge_logo);
		TextView nameview = (TextView)findViewById(R.id.magnetfridge_name);
		File imgFile = new File(getFilesDir(), logo);
		Drawable d = Drawable.createFromPath(imgFile.getAbsolutePath());
		image.setImageDrawable(d);
		nameview.setText(nombre);
		
		//Set policy to HTTP
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("id_marca", id_marca.toString());
		//construct form to HttpRequest
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("id_marca", id_marca.toString()));

		(new ThisRestClient()).execute(
				StaticUrls.SUCURSALES_URL, 
				params,
				nameValuePairs);
	}
	
	public void onBackPressed(View view) {
		this.finish();
	}
}
