package com.larefri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.util.Log;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call);
		this.context = this;
		this.settings = getSharedPreferences("LaRefriPrefsFile", 0);

		//get extra content from previous activity
		Bundle b = getIntent().getExtras();
		id_marca = b.getInt("id_marca");
		logo = b.getString("logo");
		nombre = b.getString("nombre");
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;	

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

		try {
			loadStores((new StoresManager()).readJsonStream( new FileInputStream(new File(getFilesDir(), this.id_marca+"_sucursales.json")) ));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(width/2-20,width/2-20);
		image.setLayoutParams(ll);
		File imgFile = new File(getFilesDir(), logo);
		Drawable d = Drawable.createFromPath(imgFile.getAbsolutePath());
		image.setImageDrawable(d);
		nameview.setText(nombre);
	}

	@Override
	protected void onStart() {
		super.onStart();
		setBackground();
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
				tmp_title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				tmp_title.setPadding(10, 0, 10, 1);		
				phone_num_pane.addView(tmp_title);

				Button phone_num = new Button(themeWrapper);
				phone_num.setLayoutParams(lp);
				phone_num.setText(s.telefono);
				phone_num.setTextColor(Color.WHITE);
				phone_num.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_call, 0);
				phone_num.setBackgroundColor(Color.TRANSPARENT);
				phone_num.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				phone_num.setPadding(10, 0, 10, 1);		
				phone_num.setOnClickListener(new OnClickListener() {							
					@Override
					public void onClick(View v) {
						onCall(v, s.telefono);

					}
				});
				phone_num_pane.addView(phone_num);

				Button phone_num2 = new Button(themeWrapper);
				if(!s.telefono2.isEmpty()){
					phone_num.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
					phone_num.setPadding(10, 0, 10, 1);
					phone_num2.setLayoutParams(lp);
					phone_num2.setText(s.telefono2);
					phone_num2.setTextColor(Color.WHITE);
					phone_num2.setBackgroundColor(Color.TRANSPARENT);
					phone_num2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_call, 0);
					phone_num2.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
					phone_num2.setPadding(10, 0, 10, 1);
					phone_num2.setOnClickListener(new OnClickListener() {							
						@Override
						public void onClick(View v) {
							onCall(v, s.telefono2);							
						}
					});
					phone_num_pane.addView(phone_num2);
				}

				if(!s.telefono3.isEmpty()){
					phone_num2.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
					phone_num2.setPadding(10, 0, 10, 1);
					Button phone_num3 = new Button(themeWrapper);
					phone_num3.setLayoutParams(lp);
					phone_num3.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
					phone_num3.setText(s.telefono3);
					phone_num3.setTextColor(Color.WHITE);
					phone_num3.setBackgroundColor(Color.TRANSPARENT);
					phone_num3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_call, 0);
					phone_num3.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
					phone_num3.setPadding(10, 0, 10, 1);
					phone_num3.setOnClickListener(new OnClickListener() {							
						@Override
						public void onClick(View v) {
							onCall(v, s.telefono3);

						}
					});
					phone_num_pane.addView(phone_num3);
				}
				LinearLayout ll = new LinearLayout(this);
				LinearLayout.LayoutParams trlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20);
				ll.setLayoutParams(trlp);
				ll.setBackgroundColor(Color.TRANSPARENT);
				phone_num_pane.addView(ll);
				store_call_pane.addView(phone_num_pane);
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
		Intent intent = new Intent(view.getContext(), FlyerActivity.class);
		Bundle b = new Bundle();
		b.putInt("id_marca", this.id_marca);
		b.putString("logo", this.logo);
		b.putString("nombre", this.nombre);		
		intent.putExtras(b);
	    this.startActivity(intent);
	    this.finish();
	}

	public void onCall(View view, String phone){
		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phone));
		onCallLog(phone);
		startActivity(callIntent);
	}

	public void onCallLog(String phone){
		String android_id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		/*id_usuario id_marca*/
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("id_marca[]", id_marca.toString());
		params.put("id_usuario", android_id);
		//construct form to HttpRequest
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("id_marca[]", id_marca.toString()));
		nameValuePairs.add(new BasicNameValuePair("id_usuario", (android_id == null) ? "0": android_id));
		if (isNetworkAvailable()){
			try {
				for(LogCall l: getLocalCallLog()){
					params.put("id_marca[]", l.id_marca.toString());
					nameValuePairs.add(new BasicNameValuePair("id_marca[]", l.id_marca.toString()));
				}
			} catch (IOException doNotCare) { /*Lost Data*/ }
			RestClient restClient = new RestClient();
			restClient.execute(StaticUrls.LOG_CALLS,params, nameValuePairs);
		}else{
			try {
				saveLocalCallLog(phone);
			} catch (IOException doNotCare) { /*Lost Data*/ }
		}
	}

	private void saveLocalCallLog(String phone) throws IOException{
		FileOutputStream outputStream;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.getDefault()); 
		String now = df.format(new Date());
		File file = new File(getFilesDir(), "callLog.json");
		outputStream = new FileOutputStream(file, true);
		outputStream.write((id_marca.toString()+","+phone+","+now+"\n").getBytes());
		outputStream.flush(); 
		outputStream.close();
	}

	private ArrayList<LogCall> getLocalCallLog() throws IOException{
		FileInputStream inputStream;
		ArrayList<LogCall> logCalls = new ArrayList<LogCall>();
		BufferedReader reader;
		File file = new File(getFilesDir(), "callLog.json");
		inputStream = new FileInputStream(file);
		reader = new BufferedReader(new InputStreamReader(inputStream));
		String line = reader.readLine();
		while(line != null && !line.equalsIgnoreCase("\n")){
			Log.e("LinE",line);
			line = reader.readLine();
			try{
				String[] tmp = line.split(",");
				logCalls.add(new LogCall(Integer.parseInt(tmp[0]) , tmp[1], tmp[2]));
			}catch(Exception doNotCare){ /*Lost Data*/ }
		}
		inputStream.close();
		file.delete();
		return logCalls;		
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public void onHomePressed(View view){
		Intent intent = new Intent(view.getContext(), MainActivity.class);
	    this.startActivity(intent);
	    this.finish();
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}
}

class LogCall{
	Integer id_marca;
	String phone;
	String date_time;

	public LogCall(Integer id_marca, String phone, String date_time) {
		super();
		this.id_marca = id_marca;
		this.phone = phone;
		this.date_time = date_time;
	}	
}
