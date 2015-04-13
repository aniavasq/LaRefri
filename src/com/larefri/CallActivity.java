package com.larefri;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
//import android.util.Log;
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
	private String id_marca;
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
		id_marca = b.getString("id_marca");
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

		/***************************************************
		 * Parse support*/
		ParseConnector.getInstance(this);

		loadLocales();
		/**********************************************/

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

	private void loadStores(List<Local> stores){
		LinearLayout store_call_pane = (LinearLayout) findViewById(R.id.stores_call_buttons);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.START);
		Resources resources = getResources();
		ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_button);

		if(stores.isEmpty() || (stores.size()==1 && stores.get(0)==null) || LocationTask.getCity().equalsIgnoreCase("NO_CITY")){
			LinearLayout phone_num_pane = new LinearLayout(themeWrapper);
			phone_num_pane.setOrientation(LinearLayout.VERTICAL);
			phone_num_pane.setLayoutParams(lp);

			TextView tmp_title = new Button(themeWrapper);
			tmp_title.setLayoutParams(lp);
			tmp_title.setBackground(resources.getDrawable(R.drawable.menu_label_bg));
			tmp_title.setText(R.string.no_fridge_magnets);
			tmp_title.setTextColor(Color.WHITE);
			tmp_title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
			tmp_title.setPadding(10, 0, 10, 1);
			phone_num_pane.addView(tmp_title);
			store_call_pane.addView(phone_num_pane);
		}
		for(final Local s: stores){
			if(s!=null && s.getCity().equalsIgnoreCase(LocationTask.getCity()) && s.getRegion().equalsIgnoreCase(LocationTask.getRegion())){
				LinearLayout phone_num_pane = new LinearLayout(themeWrapper);
				phone_num_pane.setOrientation(LinearLayout.VERTICAL);
				phone_num_pane.setLayoutParams(lp);

				TextView tmp_title = new Button(themeWrapper);
				tmp_title.setLayoutParams(lp);
				tmp_title.setBackground(resources.getDrawable(R.drawable.menu_label_bg));
				tmp_title.setText(s.getName());
				tmp_title.setTextColor(Color.WHITE);
				tmp_title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
				tmp_title.setPadding(10, 0, 10, 1);		
				phone_num_pane.addView(tmp_title);

				for(final String phone: s.getPhones()){
					Button phone_num = new Button(themeWrapper);
					phone_num.setLayoutParams(lp);

					try {
						PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
						PhoneNumber internationalPhoneNumber = phoneUtil.parse(phone, Locale.getDefault().getCountry());
						String internationalFormatPhoneNumber = phoneUtil.format(internationalPhoneNumber, PhoneNumberFormat.NATIONAL);
						phone_num.setText(internationalFormatPhoneNumber);
					} catch (NumberParseException e) {
						phone_num.setText(phone);
					}
					phone_num.setTextColor(Color.WHITE);
					phone_num.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_call, 0);
					phone_num.setBackgroundColor(Color.TRANSPARENT);
					phone_num.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
					phone_num.setPadding(10, 0, 10, 1);		
					phone_num.setOnClickListener(new OnClickListener() {							
						@Override
						public void onClick(View v) {
							onCallLog(s);
							onCall(v, phone);
						}
					});
					if (s.getPhones().indexOf(phone)!= (s.getPhones().size()-1))
						phone_num.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
					if (s.getPhones().indexOf(phone)==0)
						phone_num.setPadding(10, 0, 10, 1);
					phone_num_pane.addView(phone_num);
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
		b.putString("id_marca", this.id_marca);
		b.putString("logo", this.logo);
		b.putString("nombre", this.nombre);		
		intent.putExtras(b);
		this.startActivity(intent);
		this.finish();
	}

	public void onCall(View view, String phone){
		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phone));
		startActivity(callIntent);
	}

	public void onCallLog(Local s){
		//Log.e("USER_ID", "User ID: "+NewAccountActivity.getUserId(this));
		NewAccountActivity.getUserId(this);
		ParseObject call = ParseObject.create("Call");
		ParseUser caller = ParseUser.getCurrentUser();
		LogCall logCall = new LogCall(call, s.getParseReference(), caller);
		logCall.getParseObject().saveEventually();
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

	private void loadLocales() {
		ParseQuery<ParseObject> innerQuery = new ParseQuery<ParseObject>("Store");
		innerQuery.fromLocalDatastore();
		innerQuery.whereEqualTo("objectId",this.id_marca);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Locale");
		query.fromLocalDatastore();
		query.whereMatchesQuery("store", innerQuery);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> result, ParseException e) {
				if (e == null) {
					List<Local> locales = new ArrayList<Local>();
					for (ParseObject parseObject: result){
						Local locale = new Local(parseObject);
						locales.add(locale);
					}
					loadStores(locales);
				}
			}
		});
	}
}

class LogCall{
	private ParseObject parseObject;
	private ParseObject locale;
	private ParseObject caller;
	private Date createdAt;
	private Date updatedAt;
	
	public LogCall(ParseObject parseObject, ParseObject locale,
			ParseObject caller) {
		setParseObject(parseObject);
		setCaller(caller);
		setLocale(locale);
	}
	public ParseObject getLocale() {
		return locale;
	}
	public void setLocale(ParseObject locale) {
		this.locale = locale;
		this.parseObject.put("locale", locale);
	}
	public ParseObject getCaller() {
		return caller;
	}
	public void setCaller(ParseObject caller) {
		this.caller = caller;
		this.parseObject.put("caller", caller);
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
		this.parseObject.put("createdAt", createdAt);
	}
	public Date getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	public ParseObject getParseObject() {
		return parseObject;
	}
	public void setParseObject(ParseObject parseObject) {
		this.parseObject = parseObject;
	}
}
