package com.larefri;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class NewAccountActivity extends Activity {
	private static SharedPreferences settings;
	private static ParseUser _user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_account);
		settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
	}

	@Override
	protected void onStart() {
		super.onStart();
		setBackground();
	}
	
	public void onBackPressed(View view) {
		this.finish();
	}
	
	protected void setBackground() {
		RelativeLayout head = (RelativeLayout)findViewById(R.id.relativeLayout1);
		LinearLayout article = (LinearLayout)findViewById(R.id.article);
		int bg_color = settings.getInt("bg_color", Color.parseColor("#999089"));
		int menu_bg_color = settings.getInt("menu_bg_color", Color.parseColor("#6B6560"));
		article.setBackgroundColor(menu_bg_color);
		head.setBackgroundColor(bg_color);
	}
	
	public void onHomePressed(View view){
		Intent intent = new Intent(view.getContext(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    this.startActivity(intent);
	    this.finish();
	}
	
	/*public static ParseUser getUser(Activity master){
		if (_user==null){
			initUser(master);
			return _user;
		}else{
			return _user;
		}
	}*/
	
	public static String getUserId(Activity master){
		settings = master.getSharedPreferences(MainActivity.PREFS_NAME, 0);
		return settings.getString("user_id", "");
	}
	
	private static void setUserId(String user_id){
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("user_id", user_id);
		editor.commit();
	}
	
	public static void initUser(Activity master){
		settings = master.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        _user = new ParseUser();
        String android_id = Secure.getString(master.getContentResolver(), Secure.ANDROID_ID);
        _user.setUsername("a"+android_id);
        _user.setEmail("anemail@email.com");
        _user.setPassword("some password");
        _user.put("phone", "00000000000");
        _user.saveInBackground();
        try {
			_user.signUp();
		} catch (ParseException e) {
			ParseUser.logInInBackground(_user.getUsername(), "some password", new LogInCallback() {				
				@Override
				public void done(ParseUser user, ParseException ex) {
					if(ex==null){
						_user = user;
						setUserId(_user.getObjectId());
					}else{
						Log.e("USER ERROR", ex.getMessage(), ex);
					}
				}
			});
		}
        setUserId(_user.getObjectId());
	}
}
