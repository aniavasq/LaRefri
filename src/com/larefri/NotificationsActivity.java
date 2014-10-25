package com.larefri;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class NotificationsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notifications);
	}
	
	public void onBackPressed(View view) {
		Intent intent = new Intent(view.getContext(), MenuActivity.class);
	    this.startActivity(intent);
	    this.finish();
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
