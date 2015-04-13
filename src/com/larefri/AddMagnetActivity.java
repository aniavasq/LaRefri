package com.larefri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParseException;
import com.larvalabs.svgandroid.SVGParser;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
//import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AddMagnetActivity extends Activity implements AddMagnet{

	private SharedPreferences settings;
	private String id_category;
	private String nombre;
	private String logo;
	private Context context;
	private final ArrayList<Store> fridgeMagnets = new ArrayList<Store>();

	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_magnet);

		context = this;
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		settings = getSharedPreferences("LaRefriPrefsFile", 0);
		
		//Set policy to HTTP
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		Bundle b = getIntent().getExtras();
		id_category = b.getString("id_categoria");
		logo = b.getString("logo");
		nombre = b.getString("nombre");

		ImageView image = (ImageView)findViewById(R.id.magnetfridge_logo);
		TextView nameview = (TextView)findViewById(R.id.magnetfridge_name);
		File imgFile = new File(getFilesDir(), logo);
		try {
			setImageSVGDrawable(image, imgFile, width/3-40, width/3-40);
		} catch (Exception e) { e.printStackTrace(); }
		nameview.setText(nombre);

		/***************************************************
		 * Parse support*/
		ParseConnector.getInstance(this);
		
		getParseFridgeMagnets();
		/**********************************************/
	}

	@Override
	protected void onStart() {
		setBackground();
		super.onStart();
	}

	public void addMagnetToLocalData(Store fm){
		try{
			if(!MainActivity.getMyFridgeMagnets().contains(fm)){
				MainActivity.addMyFridgeMagnet(fm);
			}
		}catch (Exception donotCare){ }
	}

	private void loadFridgeMagnetsButtons(List<Store> fridgeMagnets) {
		LinearLayout store_call_pane = (LinearLayout) findViewById(R.id.add_fridge_magnets_buttons);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.START);
		lp.setMargins(0, 0, 0, 0);
		Resources resources = getResources();
		ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_button);
		ArrayList<FridgeMagnetButton> buttons = new ArrayList<FridgeMagnetButton>();

		ArrayList<Store> remove = new ArrayList<Store>(fridgeMagnets);
		remove.removeAll(MainActivity.getMyFridgeMagnets());		
		store_call_pane.removeAllViews();
		for(final Store fm: remove){
			if(fm!=null){
				final FridgeMagnetButton tmp_button = new FridgeMagnetButton(themeWrapper, fm);
				tmp_button.setLayoutParams(lp);
				tmp_button.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
				tmp_button.setText(fm.getName());
				tmp_button.setTextColor(Color.WHITE);
				tmp_button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
				tmp_button.setOnClickListener(new AddOnClickListener(tmp_button, (Activity)context));
				tmp_button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
				tmp_button.setPadding(10, 0, 10, 1);
				buttons.add(tmp_button);
			}
		}

		ArrayList<Store> metaRemove = new ArrayList<Store>(MainActivity.getMyFridgeMagnets());
		ArrayList<Store> myRemove = new ArrayList<Store>(MainActivity.getMyFridgeMagnets());
		metaRemove.removeAll(fridgeMagnets);
		myRemove.removeAll(metaRemove);
		for(final Store fm: myRemove){
			if(fm!=null){
				FridgeMagnetButton tmp_title = new FridgeMagnetButton(themeWrapper, fm);
				tmp_title.setLayoutParams(lp);
				tmp_title.setBackground(resources.getDrawable(R.drawable.menu_button_bg_disabled));
				tmp_title.setText(fm.getName());
				tmp_title.setTextColor(Color.WHITE);
				tmp_title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
				tmp_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_accept, 0);
				tmp_title.setPadding(10, 0, 10, 1);
				tmp_title.setOnClickListener(new RemoveOnClickListener(tmp_title, (Activity)context));
				buttons.add(tmp_title);
			}
		}
		Collections.sort(buttons, new FridgeMagnetButton.FridgeMagnetButtonComparator());
		for(FridgeMagnetButton b:buttons){
			store_call_pane.addView(b);
		}
	}

	public void onBackPressed(View view) {
		Intent intent = new Intent(view.getContext(), CategoriesActivity.class);
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

	public void myFridgeMagnetsRemove(Store fm) {
		MainActivity.removeMyFridgeMagnet(fm);
	}
	
	public static void setImageSVGDrawable(ImageView imageview, File imgFile, int width, int height) throws SVGParseException, FileNotFoundException{
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
		
		imageview.setImageDrawable(vectorDrawing);
		imageview.setBackgroundColor(Color.TRANSPARENT);
		imageview.setScaleType( ImageView.ScaleType.FIT_XY );
		imageview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	/**********************************************
	 * Parse support
	 * */
	private void getParseFridgeMagnets(){

		ParseQuery<ParseObject> innerQuery = new ParseQuery<ParseObject>("Category");
		innerQuery.whereEqualTo("objectId", this.id_category);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Store");
		query.whereMatchesQuery("category", innerQuery);
		query.whereEqualTo("state", 1);
		onLoading();
		query.findInBackground(new FindCallback<ParseObject>() {
			
			public void done(List<ParseObject> result, ParseException e) {
				if (e == null) {
					Store s;
					for(ParseObject fm: result){
						s = new Store(fm, context);
						fridgeMagnets.add(s);
					}
					loadFridgeMagnetsButtons(fridgeMagnets);
				} else {
					//Log.e("ERROR",e.getMessage(),e);
				}
				onLoaded();
			}
		});
	}
	

	private void onLoaded() {
		if (dialog.isShowing()) {
            dialog.dismiss();
        }
	}

	private void onLoading(){
		dialog = new ProgressDialog(context);
        dialog.setMessage("Actualizando Imantados");
        dialog.show();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
	}
	/**********************************************/
}
