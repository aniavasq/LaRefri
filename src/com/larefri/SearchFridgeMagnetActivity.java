package com.larefri;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SearchFridgeMagnetActivity extends Activity {

	private SharedPreferences settings;
	private Integer id_category;
	private String logo;
	private Context context;
	private TextView search_txt;
	private List<FridgeMagnet> myFridgeMagnets, loadedFridgeMagnets, queryFridgeMagnets;
	
	class ThisRestClient extends RestClient{
		@Override
		protected void onPreExecute() {
	        dialog = new ProgressDialog(context);
	        this.dialog.setMessage("Actualizando Imantados");
	        this.dialog.show();
	        this.dialog.setCancelable(true);
	        this.dialog.setCanceledOnTouchOutside(true);
	    }
		
		@Override
		protected Object doInBackground(Object... params) {
			return super.doInBackground(params);
		}

		@Override
		protected void onPostExecute(Object result) {
			if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
			InputStream is = new ByteArrayInputStream(result.toString().getBytes());
			FridgeMagnetsManager fridgeMagnetsManager = new FridgeMagnetsManager();
			try {
				loadedFridgeMagnets = fridgeMagnetsManager.readJsonStream(is);
				loadFridgeMagnetsButtons(loadedFridgeMagnets);
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}  catch (Exception e) {
				loadedFridgeMagnets = new ArrayList<FridgeMagnet>();
			}
			search_txt.addTextChangedListener(new QueryInList(loadedFridgeMagnets));
			super.onPostExecute(result);
		}
		
	}
	
	class DownloadMagnetStoresRestClient extends RestClient{
		@Override
		protected Object doInBackground(Object... params) {
			this.setResult(super.doInBackground(params));
			FridgeMagnet for_add = new FridgeMagnet();
			
			for_add = (FridgeMagnet) params[3];
			InputStream is = new ByteArrayInputStream(getResult().toString().getBytes());
			StoresManager storesManager = new StoresManager();
			try {			
				List<Store> stores = storesManager.readJsonStream(is);
				File JsonFile = new File(getFilesDir(), for_add.id_marca+"_sucursales.json");
				storesManager.writeJsonStream(new FileOutputStream(JsonFile), stores);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return this.getResult();
		}
	}
	
	class DownloadFridgeMagnetLogo extends AsyncTask<String, String, Boolean>{
		@Override
		protected Boolean doInBackground(String... params) {
			String url = params[0];
			String file = params[1];
			try{
				InputStream imageInputStream=new URL(url+file).openStream();
		    	FileOutputStream imageOutputStream;
		    	imageOutputStream = openFileOutput(file, Context.MODE_PRIVATE);
		    	MainActivity.CopyStream(imageInputStream, imageOutputStream);
		    	imageOutputStream.close();
		    	return true;
			}catch(Exception donotCare){ }
			return false;
		}		
	}
	
	class AddOnClickListener implements OnClickListener{
		private FridgeMagnet fm;
		private Button button;
		
		public AddOnClickListener(FridgeMagnet fm, Button button){
			this.fm = fm;
			this.button = button;
		}
		
		@Override
		public void onClick(View v) {
			Resources resources = getResources();
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("id_marca", fm.id_marca.toString());
			//construct form to HttpRequest
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("id_marca", fm.id_marca.toString()));
			(new DownloadFridgeMagnetLogo()).execute(StaticUrls.FRIDGE_MAGNETS, fm.logo);
			try {
				addMagnetToLocalData(this.fm);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			(new DownloadMagnetStoresRestClient()).execute(
					StaticUrls.SUCURSALES_URL, 
					params,
					nameValuePairs,
					this.fm);
			button.setBackground(resources.getDrawable(R.drawable.menu_button_bg_disabled));
			button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_favorite, 0);
			button.setOnClickListener(new RemoveOnClickListener(fm, button));
			button.setPadding(10, 0, 10, 1);
		}
	}
	
	class RemoveOnClickListener implements OnClickListener{
		private FridgeMagnet fm;
		private Button button;
		
		public RemoveOnClickListener(FridgeMagnet fm, Button button){
			this.fm = fm;
			this.button = button;
		}
		@Override
		public void onClick(View v) {
			Resources resources = getResources();
			myFridgeMagnets.remove(fm);
			try {
				saveFridgeMagnetsList();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			button.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
			button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
			button.setOnClickListener(new AddOnClickListener(fm, button));
			button.setPadding(10, 0, 10, 1);
		}
	}
	
	class QueryInList implements TextWatcher{
		private List<FridgeMagnet> fridgeMagnets;
			
		public QueryInList(List<FridgeMagnet> loadedFridgeMagnets) {
			super();
			this.fridgeMagnets = loadedFridgeMagnets;
		}
		
		@Override
		public void afterTextChanged(Editable s) {	}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@SuppressLint("DefaultLocale")
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			String queryText = String.valueOf(s);//String.valueOf(search_txt.getText());
			List<FridgeMagnet> tmpQueryFridgeMagnets;
			if(!queryText.isEmpty()){
				tmpQueryFridgeMagnets = new ArrayList<FridgeMagnet>();
				for(FridgeMagnet match: this.fridgeMagnets){
					if(match.nombre!= null && match.nombre.toLowerCase().startsWith(queryText.toLowerCase())){
						tmpQueryFridgeMagnets.add(match);
					}
				}
			}else{
				tmpQueryFridgeMagnets = this.fridgeMagnets;
			}
			if(tmpQueryFridgeMagnets != queryFridgeMagnets){
				loadFridgeMagnetsButtons(tmpQueryFridgeMagnets);
			}else{
				tmpQueryFridgeMagnets = queryFridgeMagnets;
			}
			
		}		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_fridge_magnet);	
		
		this.context = this;
		this.settings = getSharedPreferences("LaRefriPrefsFile", 0);
		this.search_txt = (TextView)findViewById(R.id.search_txt);
		
		//Set policy to HTTP
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
		
		Bundle b = getIntent().getExtras();
		id_category = b.getInt("id_category");
		logo = b.getString("logo");
		Log.v("id_category",id_category.toString());
		
		ImageView image = (ImageView)findViewById(R.id.magnetfridge_logo);
		File imgFile = new File(getFilesDir(), logo);
		Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		image.setImageBitmap(bmp);
		
		(new ThisRestClient()).execute(
				StaticUrls.MAGNETS, 
				new HashMap<String, String>(),
				new ArrayList<NameValuePair>());
	}

	public void loadFridgeMagnetsButtons(List<FridgeMagnet> fridgeMagnets) {
		LinearLayout store_call_pane = (LinearLayout) findViewById(R.id.add_fridge_magnets_buttons);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
		Resources resources = getResources();
		ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_button);
		ArrayList<Button> buttons = new ArrayList<Button>();
		
		ArrayList<FridgeMagnet> metaRemove = new ArrayList<FridgeMagnet>(loadedFridgeMagnets);
		
		ArrayList<FridgeMagnet> remove = new ArrayList<FridgeMagnet>(fridgeMagnets);
		remove.removeAll(this.myFridgeMagnets);		
		store_call_pane.removeAllViews();
		for(final FridgeMagnet fm: remove){
			if(fm.nombre!=null){
				final Button tmp_button = new Button(themeWrapper);
				tmp_button.setLayoutParams(lp);
				tmp_button.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
				tmp_button.setText(fm.nombre);
				tmp_button.setTextColor(Color.WHITE);
				tmp_button.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				tmp_button.setPadding(10, 0, 10, 1);
				tmp_button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
				tmp_button.setOnClickListener(new AddOnClickListener(fm, tmp_button));
				buttons.add(tmp_button);
			}
		}
		
		metaRemove.removeAll(fridgeMagnets);
		
		ArrayList<FridgeMagnet> myRemove = new ArrayList<FridgeMagnet>(this.myFridgeMagnets);
		myRemove.removeAll(metaRemove);
		myRemove.removeAll(remove);
		for(final FridgeMagnet fm: myRemove){
			Button tmp_title = new Button(themeWrapper);
			tmp_title.setLayoutParams(lp);
			tmp_title.setBackground(resources.getDrawable(R.drawable.menu_button_bg_disabled));
			tmp_title.setText(fm.nombre);
			tmp_title.setTextColor(Color.WHITE);
			tmp_title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			tmp_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_favorite, 0);
			tmp_title.setPadding(10, 0, 10, 1);
			tmp_title.setOnClickListener(new RemoveOnClickListener(fm, tmp_title));
			buttons.add(tmp_title);
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
	protected void onStart() {
		setBackground();
		File JsonFile = new File(getFilesDir(), "data.json");
		FridgeMagnetsManager fridgeMagnetReader = new FridgeMagnetsManager();
		try {
			this.myFridgeMagnets = fridgeMagnetReader.readJsonStream( new FileInputStream(JsonFile) );
		} catch (Exception e) { }
		super.onStart();
	}

	public void addMagnetToLocalData(FridgeMagnet fm) throws FileNotFoundException, IOException{
		File JsonFile = new File(getFilesDir(), "data.json");		
		FridgeMagnetsManager fridgeMagnetWriter = new FridgeMagnetsManager();
		try{
			(new DownloadFridgeMagnetLogo()).execute(StaticUrls.FRIDGE_MAGNETS, fm.logo);
			if(!this.myFridgeMagnets.contains(fm)){
				myFridgeMagnets.add(fm);
				fridgeMagnetWriter.writeJsonStream(new FileOutputStream(JsonFile), myFridgeMagnets);
			}			
		}catch (Exception donotCare){ }
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
	
	private void saveFridgeMagnetsList() throws FileNotFoundException, IOException {
		File JsonFile = new File(getFilesDir(), "data.json");		
		FridgeMagnetsManager fridgeMagnetWriter = new FridgeMagnetsManager();
		fridgeMagnetWriter.writeJsonStream(new FileOutputStream(JsonFile), this.myFridgeMagnets);
	}
	
	public void onHomePressed(View view){
		Intent intent = new Intent(view.getContext(), MainActivity.class);
	    this.startActivity(intent);
	    this.finish();
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}
}
