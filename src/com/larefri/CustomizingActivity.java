package com.larefri;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CustomizingActivity extends Activity {

	private SharedPreferences settings;
	private static Integer BUTTONPAD_COLS = 3;
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
			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width/3,width/3);
			LinearLayout.LayoutParams lptx = new LinearLayout.LayoutParams(width/3,LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams lptr = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lptr.setMargins(0, 0, 0, 0);
			///
			lp.gravity = Gravity.BOTTOM;
			lp.weight = 1.0f;
			lp.setMargins(0, 0, 0, 0);
			///
			lptx.gravity = Gravity.BOTTOM;
			lptx.weight = 1.0f;
			lptx.setMargins(0, 0, 0, 0);
			///
			mTlayout.setLayoutParams(lptr);
			ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_label);
			for(int j=0; j<BUTTONPAD_ROWS; j++){
				TableRow tr = new TableRow(mTlayout.getContext());
				tr.setLayoutParams(lptr);
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
						if(i%3 != 0)
							btn.setPadding(10, 30, 20, 0);
						else if(i%3 != 1)
							btn.setPadding(15, 30, 15, 0);
						else
							btn.setPadding(20, 30, 10, 0);
						btn.setOnClickListener(new OnClickListener() {							
							@Override
							public void onClick(View v) {
								SharedPreferences.Editor editor = settings.edit();
								editor.putInt("bg_color", Color.parseColor(s.head_skin));
								editor.putInt("menu_bg_color", Color.parseColor(s.article_skin));
								editor.commit();
								setBackground();
							}
						});
						
						TextView tv = new TextView(themeWrapper);
						tv.setLayoutParams(lptx);
						tv.setText(s.name);
						tv.setTextColor(Color.WHITE);
						tv.setGravity(Gravity.CENTER);
						tv.setPadding(0, 0, 0, 0);
						
			            tr.addView(ll);
						ll.addView(btn);
						ll.addView(tv);
					}
				}
				mTlayout.addView(tr);
			}
		} catch (Exception e) {
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

	public static List<Skin> getSkinsFromJSON (File fl) throws Exception {
	    FileInputStream fin = new FileInputStream(fl);
	    List<Skin> ret = (new SkinsManager()).readJsonStream(fin);
	    //Make sure you close all streams.
	    fin.close();        
	    return ret;
	}

	public void onBackPressed(View view) {
		Intent intent = new Intent(view.getContext(), MenuActivity.class);
	    this.startActivity(intent);
	    this.finish();
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
}
