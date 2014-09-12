package com.larefri;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CustomizingActivity extends Activity {

	private static Integer BUTTONPAD_COLS = 2;
	private static Integer BUTTONPAD_ROWS = 2;
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customizing);
		
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		
		this.context = this;
		
		TableLayout mTlayout = (TableLayout) findViewById(R.id.set_theme_buttons);
		try {
			List<Skin> skins = getSkinsFromJSON(new File(getFilesDir(),"skins.json"));
			Iterator<Skin> skinsIterator = skins.iterator();
			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width/2,width/2);
			lp.gravity = Gravity.BOTTOM;
			lp.weight = 1.0f;
			lp.setMargins(0, 0, 0, 0);
			for(int j=0; j<BUTTONPAD_ROWS; j++){
				TableRow tr = new TableRow(mTlayout.getContext());
				for(int i=0; i<BUTTONPAD_COLS; i++){
					LinearLayout ll = new LinearLayout(this);
					ll.setOrientation(LinearLayout.VERTICAL);
					ll.setGravity(Gravity.BOTTOM);
					
					final Skin s = skinsIterator.next();
					/*File imgFile = new File(getFilesDir(), c.icono_categoria);
					ImageButton btn = new ImageButton(this);
					Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
					btn.setLayoutParams(lp);
					btn.setImageBitmap(bmp);
					btn.setBackgroundColor(Color.TRANSPARENT);
					btn.setScaleType( ImageView.ScaleType.FIT_CENTER );
					btn.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							//onCall(v, s.telefono);
							//goToAddMagnets(v, c);
						}
					});
					
					
					TextView tv = new TextView(this);
					tv.setText(c.nombre_categoria);
					tv.setTextColor(Color.WHITE);
					tv.setGravity(Gravity.CENTER_HORIZONTAL);*/
					
					ImageButton btn = new ImageButton(this);
					//ShapeDrawable d = (ShapeDrawable) context.getResources().getDrawable(R.drawable.skin_button);
					GradientDrawable d = new GradientDrawable();
					d.setColor(Color.parseColor(s.skin));
					d.setShape(GradientDrawable.OVAL);
					d.setStroke(10, Color.WHITE);
					
					//d.setColorFilter(Color.parseColor(s.skin), PorterDuff.Mode.SRC_ATOP);
					btn.setLayoutParams(lp);
					btn.setBackground(d);
					//btn.setImageDrawable(d);
					//btn.setBackgroundColor(Color.TRANSPARENT);
					btn.setScaleType( ImageView.ScaleType.FIT_CENTER );
					btn.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							//onCall(v, s.telefono);
							//goToAddMagnets(v, c);
						}
					});
					
					
					TextView tv = new TextView(this);
					tv.setText(s.name);
					tv.setTextColor(Color.WHITE);
					tv.setGravity(Gravity.CENTER_HORIZONTAL);
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.customizing, menu);
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
	
	public static List<Skin> getSkinsFromJSON (File fl) throws Exception {
	    FileInputStream fin = new FileInputStream(fl);
	    List<Skin> ret = (new SkinsManager()).readJsonStream(fin);
	    //Make sure you close all streams.
	    fin.close();        
	    return ret;
	}

	public void onBackPressed(View view) {
		this.finish();
	}
}
