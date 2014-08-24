package com.larefri;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
	public void onBackPressed(View view) {
		this.finish();
		/**Intent intent = new Intent(view.getContext(), MainActivity.class);
	    this.startActivity(intent);**/
	}
	
	public void goCategories(View view){
		Intent intent = new Intent(view.getContext(), CategoriesActivity.class);
	    this.startActivity(intent);
	}
}
