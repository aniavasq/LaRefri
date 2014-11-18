package com.larefri;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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
		settings = getSharedPreferences("LaRefriPrefsFile", 0);
			
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
		Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		image.setImageBitmap(bmp);
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
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
		lp.setMargins(0, 0, 0, 0);
		Resources resources = getResources();
		ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_button);
		ArrayList<Button> buttons = new ArrayList<Button>();
		
		ArrayList<FridgeMagnet> remove = new ArrayList<FridgeMagnet>(fridgeMagnets);
		remove.removeAll(MainActivity.getMyFridgeMagnets());		
		store_call_pane.removeAllViews();
		for(final FridgeMagnet fm: remove){
			if(fm!=null){
				final Button tmp_button = new Button(themeWrapper);
				tmp_button.setLayoutParams(lp);
				tmp_button.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
				tmp_button.setText(fm.nombre);
				tmp_button.setTextColor(Color.WHITE);
				tmp_button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
				tmp_button.setOnClickListener(new AddOnClickListener(fm, tmp_button, (Activity)context));
				tmp_button.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
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
				Button tmp_title = new Button(themeWrapper);
				tmp_title.setLayoutParams(lp);
				tmp_title.setBackground(resources.getDrawable(R.drawable.menu_button_bg_disabled));
				tmp_title.setText(fm.nombre);
				tmp_title.setTextColor(Color.WHITE);
				tmp_title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				tmp_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_accept, 0);
				tmp_title.setPadding(10, 0, 10, 1);
				tmp_title.setOnClickListener(new RemoveOnClickListener(fm, tmp_title, (Activity)context));
				buttons.add(tmp_title);
			}
		}
		Collections.sort(buttons, new Comparator<Button>() {
			@Override
			public int compare(Button lhs, Button rhs) {				
				return String.valueOf(lhs.getText()).compareTo(String.valueOf(rhs.getText()));
			}
		});
		for(Button b:buttons){
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
			List<Store> stores = storesManager.readJsonStream(is);
			File JsonFile = new File(master.getFilesDir(), for_add.id_marca+"_sucursales.json");
			storesManager.writeJsonStream(new FileOutputStream(JsonFile), stores);
		} catch (IOException e) {
		}
		return this.getResult();
	}
}
