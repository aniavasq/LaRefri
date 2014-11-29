package com.larefri;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

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
import android.content.res.Resources.NotFoundException;
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
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AddMagnetActivity extends Activity implements AddMagnet{

	private SharedPreferences settings;
	private Integer id_category;
	private String nombre;
	private String logo;
	private Context context;
	private List<FridgeMagnet> loadedFridgeMagnets;
	final ArrayList<Store> fridgeMagnets = new ArrayList<Store>();

	class ThisRestClient extends RestClient{

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(context);
			this.dialog.setMessage(getResources().getText(R.string.downloading_fridge_magnets));
			this.dialog.show();
			this.dialog.setCancelable(true);
			this.dialog.setCanceledOnTouchOutside(true);
		}

		@Override
		protected void onPostExecute(Object result) {


			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			try {
				InputStream is = new ByteArrayInputStream(result.toString().getBytes());
				FridgeMagnetsManager fridgeMagnetsManager = new FridgeMagnetsManager();
				loadedFridgeMagnets = fridgeMagnetsManager.readJsonStream(is);
				loadFridgeMagnetsButtons(loadedFridgeMagnets);
			} catch (NotFoundException e) {
			} catch (IOException e) {
			}  catch (Exception e) {
				loadedFridgeMagnets = new ArrayList<FridgeMagnet>();
			}
			super.onPostExecute(result);
		}


	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_magnet);

		context = this;
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		settings = getSharedPreferences("LaRefriPrefsFile", 0);

		/***************************************************
		 * Parse support*/
		ParseConnector.getInstance(this);
		/***************************************************/
		/**********************************************
		 * Parse support
		 * */
		getParseFridgeMagnets();
		/**********************************************/

		//Set policy to HTTP
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		Bundle b = getIntent().getExtras();
		id_category = b.getInt("id_categoria");
		logo = b.getString("logo");
		nombre = b.getString("nombre");

		ImageView image = (ImageView)findViewById(R.id.magnetfridge_logo);
		TextView nameview = (TextView)findViewById(R.id.magnetfridge_name);
		File imgFile = new File(getFilesDir(), logo);
		try {
			setImageSVGDrawable(image, imgFile, width/3-40, width/3-40);
		} catch (Exception e) { e.printStackTrace(); }
		nameview.setText(nombre);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("id_categoria", id_category.toString());
		//construct form to HttpRequest
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("id_categoria", id_category.toString()));
		(new ThisRestClient()).execute(
				StaticUrls.MAGNETS_BY_CATEGORY, 
				params,
				nameValuePairs);
	}

	@Override
	protected void onStart() {
		setBackground();
		super.onStart();
	}

	public void addMagnetToLocalData(FridgeMagnet fm) throws FileNotFoundException, IOException{
		try{
			if(!MainActivity.getMyFridgeMagnets().contains(fm)){
				MainActivity.addMyFridgeMagnet(fm);
				saveFridgeMagnetsList();
			}
		}catch (Exception donotCare){ }
	}

	public void loadFridgeMagnetsButtons(List<FridgeMagnet> fridgeMagnets) {
		LinearLayout store_call_pane = (LinearLayout) findViewById(R.id.add_fridge_magnets_buttons);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.START);
		lp.setMargins(0, 0, 0, 0);
		Resources resources = getResources();
		ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_button);
		ArrayList<FridgeMagnetButton> buttons = new ArrayList<FridgeMagnetButton>();

		ArrayList<FridgeMagnet> remove = new ArrayList<FridgeMagnet>(fridgeMagnets);
		remove.removeAll(MainActivity.getMyFridgeMagnets());		
		store_call_pane.removeAllViews();
		for(final FridgeMagnet fm: remove){
			if(fm!=null){
				final FridgeMagnetButton tmp_button = new FridgeMagnetButton(themeWrapper, fm);
				tmp_button.setLayoutParams(lp);
				tmp_button.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
				tmp_button.setText(fm.nombre);
				tmp_button.setTextColor(Color.WHITE);
				tmp_button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
				tmp_button.setOnClickListener(new AddOnClickListener(tmp_button, (Activity)context));
				tmp_button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
				tmp_button.setPadding(10, 0, 10, 1);
				buttons.add(tmp_button);
			}
		}

		ArrayList<FridgeMagnet> metaRemove = new ArrayList<FridgeMagnet>(MainActivity.getMyFridgeMagnets());
		ArrayList<FridgeMagnet> myRemove = new ArrayList<FridgeMagnet>(MainActivity.getMyFridgeMagnets());
		metaRemove.removeAll(fridgeMagnets);
		myRemove.removeAll(metaRemove);
		for(final FridgeMagnet fm: myRemove){
			if(fm!=null){
				FridgeMagnetButton tmp_title = new FridgeMagnetButton(themeWrapper, fm);
				tmp_title.setLayoutParams(lp);
				tmp_title.setBackground(resources.getDrawable(R.drawable.menu_button_bg_disabled));
				tmp_title.setText(fm.nombre);
				tmp_title.setTextColor(Color.WHITE);
				tmp_title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
				tmp_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_accept, 0);
				tmp_title.setPadding(10, 0, 10, 1);
				tmp_title.setOnClickListener(new RemoveOnClickListener(tmp_title, (Activity)context, null));
				buttons.add(tmp_title);
			}
		}
		Collections.sort(buttons, new FridgeMagnetButton.FridgeMagnetButtonComparator());
		for(FridgeMagnetButton b:buttons){
			store_call_pane.addView(b);
		}
	}

	@Override
	public void saveFridgeMagnetsList() throws FileNotFoundException, IOException {
		File JsonFile = new File(getFilesDir(), "data.json");
		FridgeMagnetsManager fridgeMagnetWriter = new FridgeMagnetsManager();
		fridgeMagnetWriter.writeJsonStream(new FileOutputStream(JsonFile), MainActivity.getMyFridgeMagnets());
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

	public void myFridgeMagnetsRemove(FridgeMagnet fm) {
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

		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Store");
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> result, ParseException e) {
				if (e == null) {

					for(ParseObject fm: result){
						Store s = new Store(fm);
						fridgeMagnets.add(s);
					}
					Log.e("FRIDGEMAGNETS",fridgeMagnets.toString());
				} else {
					Log.e("ERROR",e.getMessage(),e);
				}
			}
		});
	}
	/**********************************************/
}

class DownloadMagnetStoresRestClient extends RestClient{
	private Activity master;

	public DownloadMagnetStoresRestClient(Activity master) {
		super();
		this.master = master;
	}

	@Override
	protected Object doInBackground(Object... params) {
		this.setResult(super.doInBackground(params));
		FridgeMagnet for_add = new FridgeMagnet();

		for_add = (FridgeMagnet) params[3];
		InputStream is = new ByteArrayInputStream(getResult().toString().getBytes());
		StoresManager storesManager = new StoresManager();
		try {			
			List<StoreFM> stores = storesManager.readJsonStream(is);
			File JsonFile = new File(master.getFilesDir(), for_add.id_marca+"_sucursales.json");
			storesManager.writeJsonStream(new FileOutputStream(JsonFile), stores);
		} catch (IOException e) {
		}
		return this.getResult();
	}
}
