package com.larefri;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class AccountMenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.account_menu, menu);
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
}
