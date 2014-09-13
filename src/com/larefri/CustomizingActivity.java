package com.larefri;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CustomizingActivity extends Activity {

	private SharedPreferences settings;
	private static Integer BUTTONPAD_COLS = 2;
	private static Integer BUTTONPAD_ROWS = 10;
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customizing);
		
		this.context = this;
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		
		settings = getSharedPreferences("LaRefriPrefsFile", 0);
		setBackground();
		
		TableLayout mTlayout = (TableLayout) findViewById(R.id.set_theme_buttons);
		try {
			List<Skin> skins = getSkinsFromJSON(new File(getFilesDir(),"skins.json"));
			Iterator<Skin> skinsIterator = skins.iterator();
			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width/2,width/2);
			lp.gravity = Gravity.BOTTOM;
			lp.weight = 1.0f;
			lp.setMargins(0, 0, 0, 0);
			ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_label);
			for(int j=0; j<BUTTONPAD_ROWS; j++){
				TableRow tr = new TableRow(mTlayout.getContext());
				for(int i=0; i<BUTTONPAD_COLS; i++){
					LinearLayout ll = new LinearLayout(context);
					ll.setOrientation(LinearLayout.VERTICAL);
					ll.setGravity(Gravity.BOTTOM);
					if(skinsIterator.hasNext()){
						final Skin s = skinsIterator.next();					
						ImageButton btn = new ImageButton(context);
						GradientDrawable d = new GradientDrawable();
						d.setColor(Color.parseColor(s.head_skin));
						d.setShape(GradientDrawable.OVAL);
						d.setStroke(5, Color.WHITE);
						
						btn.setLayoutParams(lp);
						btn.setImageDrawable(d);
						btn.setBackgroundColor(Color.TRANSPARENT);
						btn.setScaleType( ImageView.ScaleType.FIT_CENTER );
						btn.setOnClickListener(new OnClickListener() {							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								SharedPreferences.Editor editor = settings.edit();
								editor.putInt("bg_color", Color.parseColor(s.head_skin));
								editor.putInt("menu_bg_color", Color.parseColor(s.article_skin));
								editor.commit();
								setBackground();
							}
						});
						
						TextView tv = new TextView(themeWrapper);
						tv.setText(s.name);
						tv.setTextColor(Color.WHITE);
						tv.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
						tv.setGravity(Gravity.CENTER_HORIZONTAL);
						
			            tr.addView(ll);
						ll.addView(btn);
						ll.addView(tv);
					}
				}
				mTlayout.addView(tr);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
