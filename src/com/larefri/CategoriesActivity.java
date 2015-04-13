package com.larefri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParseException;
import com.larvalabs.svgandroid.SVGParser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
//import android.util.Log;
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
import android.widget.LinearLayout.LayoutParams;

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
			List<CategoryFM> categories = getCategoriesFromJSON(new File(getFilesDir(),"categories.json"));
			Iterator<CategoryFM> categoriesIterator = categories.iterator();

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
			for(int j=0; j<BUTTONPAD_ROWS; j++){
				TableRow tr = new TableRow(mTlayout.getContext());
				for(int i=0; i<BUTTONPAD_COLS; i++){
					LinearLayout ll = new LinearLayout(this);
					ll.setOrientation(LinearLayout.VERTICAL);
					ll.setGravity(Gravity.BOTTOM);
					final CategoryFM c = categoriesIterator.next();
					File imgFile = new File(getFilesDir(), c.icono_categoria);
					ImageButton btn = new ImageButton(this);
					
					try{
					setImageSVGDrawable(btn, imgFile, width/3-30, width/3-30, lp);
					}catch(Exception ex){ /*Log.e("",imgFile.toString());*/ }
					if(i%3 != 0)
						btn.setPadding(10, 30, 20, 0);
					else if(i%3 != 1)
						btn.setPadding(15, 30, 15, 0);
					else
						btn.setPadding(20, 30, 10, 0);
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
					tv.setLayoutParams(lptx);
					tv.setText(c.nombre_categoria);
					tv.setTextColor(Color.WHITE);
					tv.setGravity(Gravity.CENTER);

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

	protected void goToAddMagnets(View view, CategoryFM c) {
		Intent intent = new Intent(view.getContext(), AddMagnetActivity.class);
		Bundle b = new Bundle();
		b.putString("id_categoria", c.id_categoria);
		b.putString("logo", c.icono_categoria);
		b.putString("nombre", c.nombre_categoria);
		intent.putExtras(b);
		this.startActivity(intent);
	    this.finish();
	}

	protected void goToSearchMagnets(View view, CategoryFM c) {
		Intent intent = new Intent(view.getContext(), SearchFridgeMagnetActivity.class);
		Bundle b = new Bundle();
		b.putString("logo", c.icono_categoria);
		b.putString("nombre", c.nombre_categoria);
		intent.putExtras(b);
		this.startActivity(intent);
	    this.finish();
	}

	public static List<CategoryFM> getCategoriesFromJSON (File fl) throws Exception {
		FileInputStream fin = new FileInputStream(fl);
		List<CategoryFM> ret = (new CategoriesManager()).readJsonStream(fin);
		//Make sure you close all streams.
		fin.close();        
		return ret;
	}

	public void onBackPressed(View view) {
		Intent intent = new Intent(view.getContext(), MenuActivity.class);
	    this.startActivity(intent);
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

	@Override
	public void onBackPressed() {
		this.finish();
	}
	
	private void setImageSVGDrawable(ImageButton btn, File imgFile, int width, int height, LinearLayout.LayoutParams lp) throws SVGParseException, FileNotFoundException{
		SVG svg = SVGParser.getSVGFromInputStream(new FileInputStream(imgFile));
		Picture test = svg.getPicture();

		//Redraw the picture to a new size
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Picture resizePicture = new Picture();
		canvas = resizePicture.beginRecording(width, height);
		canvas.drawPicture(test, new Rect(0,0, width, height));
		resizePicture.endRecording();

		//get a drawable from resizePicture
		Drawable vectorDrawing = new PictureDrawable(resizePicture);
		
		btn.setLayoutParams(lp);
		btn.setImageDrawable(vectorDrawing);
		btn.setBackgroundColor(Color.TRANSPARENT);
		btn.setScaleType( ImageView.ScaleType.FIT_XY );
		btn.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}
}
