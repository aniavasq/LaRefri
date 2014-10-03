package com.larefri;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class AccountMenuActivity extends Activity {
	private SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_menu);
		settings = getSharedPreferences("LaRefriPrefsFile", 0);
	}

	@Override
	protected void onStart() {
		super.onStart();
		setBackground();
	}
	
	public void onBackPressed(View view) {
		this.finish();
	}
	
	public void goNewAccount(View view) {
		Intent intent = new Intent(view.getContext(), NewAccountActivity.class);
	    this.startActivity(intent);
	}
	
	public void goLogIn(View view) {
		Intent intent = new Intent(view.getContext(), LogInActivity.class);
	    this.startActivity(intent);
	}
	
	public void goAccountSettings(View view) {
		Intent intent = new Intent(view.getContext(), AccountSettingsActivity.class);
	    this.startActivity(intent);
	}
	
	protected void setBackground() {
		RelativeLayout head = (RelativeLayout)findViewById(R.id.relativeLayout1);
		LinearLayout article = (LinearLayout)findViewById(R.id.article);
		int bg_color = settings.getInt("bg_color", Color.parseColor("#999089"));
		int menu_bg_color = settings.getInt("menu_bg_color", Color.parseColor("#6B6560"));
		article.setBackgroundColor(menu_bg_color);
		head.setBackgroundColor(bg_color);
	}
}
