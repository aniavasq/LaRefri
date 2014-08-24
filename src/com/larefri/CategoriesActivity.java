package com.larefri;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CategoriesActivity extends Activity {

	private static Integer BUTTONPAD_COLS = 3;
	private static Integer BUTTONPAD_ROWS = 5;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_categories);
		
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		
		TableLayout mTlayout = (TableLayout) findViewById(R.id.category_buttons);
		try {
			List<Category> categories = getCategoriesFromJSON(new File(getFilesDir(),"categories.json"));
			Iterator<Category> categoriesIterator = categories.iterator();
			for(int j=0; j<BUTTONPAD_ROWS; j++){
				TableRow tr = new TableRow(mTlayout.getContext());
				for(int i=0; i<BUTTONPAD_COLS; i++){
					LinearLayout ll = new LinearLayout(this);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width/3,width/3);//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					lp.setMargins(0, 0, 0, 0);
					ll.setOrientation(LinearLayout.VERTICAL);
					
					
					Category c = categoriesIterator.next();
					File imgFile = new File(getFilesDir(), c.icono_categoria);
					ImageButton btn = new ImageButton(this);
					Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
					btn.setLayoutParams(lp);
					btn.setImageBitmap(bmp);
					btn.setBackgroundColor(Color.TRANSPARENT);
					btn.setScaleType( ImageView.ScaleType.FIT_CENTER );
					
					
					TextView tv = new TextView(this);
					tv.setText(c.nombre_categoria);
					tv.setTextColor(Color.WHITE);
					tv.setGravity(Gravity.CENTER_HORIZONTAL);
					/*Button btn = new Button(this);
					File imgFile = new File(getFilesDir(), c.icono_categoria);
					//Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
					Drawable d = Drawable.createFromPath(imgFile.getAbsolutePath());
					btn.setCompoundDrawables(null, d, null, null);
					//Log.e("icon", d.toString());
					//btn.setCompoundDrawablesWithIntrinsicBounds();
		            btn.setText(c.nombre_categoria);*/
		            tr.addView(ll);
					ll.addView(btn);
					ll.addView(tv);
				}
				mTlayout.addView(tr);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//tv.setText(input.);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.categories, menu);
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

	public static List<Category> getCategoriesFromJSON (File fl) throws Exception {
	    FileInputStream fin = new FileInputStream(fl);
	    List<Category> ret = (new CategoriesReader()).readJsonStream(fin);
	    //Make sure you close all streams.
	    fin.close();        
	    return ret;
	}
	
	public void onBackPressed(View view) {
		this.finish();
	}
}
