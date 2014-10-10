package com.larefri;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CategoriesActivity extends Activity {

	private SharedPreferences settings;
	private static Integer BUTTONPAD_COLS = 3;
	private static Integer BUTTONPAD_ROWS = 5;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_categories);
		
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		settings = getSharedPreferences("LaRefriPrefsFile", 0);
		
		TableLayout mTlayout = (TableLayout) findViewById(R.id.category_buttons);
		try {
			List<Category> categories = getCategoriesFromJSON(new File(getFilesDir(),"categories.json"));
			Iterator<Category> categoriesIterator = categories.iterator();
			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width/3,width/3);
			lp.gravity = Gravity.BOTTOM;
			lp.weight = 1.0f;
			lp.setMargins(0, 0, 0, 0);
			for(int j=0; j<BUTTONPAD_ROWS; j++){
				TableRow tr = new TableRow(mTlayout.getContext());
				for(int i=0; i<BUTTONPAD_COLS; i++){
					LinearLayout ll = new LinearLayout(this);
					ll.setOrientation(LinearLayout.VERTICAL);
					ll.setGravity(Gravity.BOTTOM);
					
					final Category c = categoriesIterator.next();
					File imgFile = new File(getFilesDir(), c.icono_categoria);
					ImageButton btn = new ImageButton(this);
					Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
					btn.setLayoutParams(lp);
					btn.setImageBitmap(bmp);
					btn.setBackgroundColor(Color.TRANSPARENT);
					btn.setScaleType( ImageView.ScaleType.FIT_CENTER );
					if(!c.nombre_categoria.equalsIgnoreCase("buscar"))
						btn.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								goToAddMagnets(v, c);
							}
						});
					else
						btn.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								goToSearchMagnets(v, c);
							}
						});
					
					TextView tv = new TextView(this);
					tv.setText(c.nombre_categoria);
					tv.setTextColor(Color.WHITE);
					tv.setGravity(Gravity.CENTER_HORIZONTAL);

		            tr.addView(ll);
					ll.addView(btn);
					ll.addView(tv);
				}
				mTlayout.addView(tr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		setBackground();
	}

	protected void goToAddMagnets(View view, Category c) {
		Intent intent = new Intent(view.getContext(), AddMagnetActivity.class);
	    Bundle b = new Bundle();
	    b.putInt("id_categoria", c.id_categoria);
	    b.putString("logo", c.icono_categoria);
	    b.putString("nombre", c.nombre_categoria);
	    intent.putExtras(b);
	    this.startActivity(intent);
	}
	
	protected void goToSearchMagnets(View view, Category c) {
		Intent intent = new Intent(view.getContext(), SearchFridgeMagnetActivity.class);
	    Bundle b = new Bundle();
	    b.putInt("id_categoria", c.id_categoria);
	    b.putString("logo", c.icono_categoria);
	    b.putString("nombre", c.nombre_categoria);
	    intent.putExtras(b);
	    this.startActivity(intent);
	}

	public static List<Category> getCategoriesFromJSON (File fl) throws Exception {
	    FileInputStream fin = new FileInputStream(fl);
	    List<Category> ret = (new CategoriesManager()).readJsonStream(fin);
	    //Make sure you close all streams.
	    fin.close();        
	    return ret;
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
}
