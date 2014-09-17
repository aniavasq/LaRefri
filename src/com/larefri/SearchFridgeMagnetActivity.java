package com.larefri;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
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
	private QueryInList queryInList;
	private List<FridgeMagnet> myFridgeMagnets, loadedFridgeMagnets, queryFridgeMagnets;
	private final Handler handler = new Handler();
	private final Integer delay = 1000;
	
	class ThisRestClient extends RestClient{

		/** progress dialog to show user that the backup is processing. */
	    private ProgressDialog dialog;
		
	    @Override
		protected void onPreExecute() {
	        dialog = new ProgressDialog(context);
	        this.dialog.setMessage("Actualizando Imantados");
	        this.dialog.show();
	        this.dialog.setCancelable(false);
	        this.dialog.setCanceledOnTouchOutside(false);
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
			
			queryInList = new QueryInList(loadedFridgeMagnets);		
			handler.removeCallbacks(queryInList);
			handler.post(queryInList);
			
			super.onPostExecute(result);
		}
		
	}

	class QueryInList implements Runnable{
		private List<FridgeMagnet> fridgeMagnets;
			
		public QueryInList(List<FridgeMagnet> loadedFridgeMagnets) {
			super();
			this.fridgeMagnets = loadedFridgeMagnets;
			Log.e("LOADED MAGNETS",""+loadedFridgeMagnets);
		}

		@SuppressLint("DefaultLocale")
		@Override
		public void run() {
			handler.postDelayed(this, delay);
			String queryText = String.valueOf(search_txt.getText());
			//Log.e("QUERY TEXT EMPTY",""+queryText.isEmpty());
			if(!queryText.isEmpty()){
				queryFridgeMagnets = new ArrayList<FridgeMagnet>();
				for(FridgeMagnet match: this.fridgeMagnets){
					if(match.nombre!= null && match.nombre.toLowerCase().startsWith(queryText.toLowerCase())){
						//Log.e("MATCH",""+match.nombre.toLowerCase());
						queryFridgeMagnets.add(match);
					}
				}
			}else{
				queryFridgeMagnets = this.fridgeMagnets;
			}
			loadFridgeMagnetsButtons(queryFridgeMagnets);
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
		//nameview.setText(nombre);
		
		/*HashMap<String, String> params = new HashMap<String, String>();
		params.put("id_category", id_category.toString());
		
		//construct form to HttpRequest
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("id_category", id_category.toString()));
        */
		(new ThisRestClient()).execute(
				StaticUrls.MAGNETS_BY_CATEGORY, 
				new HashMap<String, String>(),
				new ArrayList<NameValuePair>());		
	}

	public void loadFridgeMagnetsButtons(List<FridgeMagnet> fridgeMagnets) {
		LinearLayout store_call_pane = (LinearLayout) findViewById(R.id.add_fridge_magnets_buttons);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
		Resources resources = getResources();
		ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_button);
		store_call_pane.removeAllViews();
		for(final FridgeMagnet fm: fridgeMagnets){
			if(fm.nombre!=null){
				Button tmp_button = new Button(themeWrapper);
				tmp_button.setLayoutParams(lp);
				tmp_button.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
				tmp_button.setText(fm.nombre);
				tmp_button.setTextColor(Color.WHITE);
				tmp_button.setGravity(Gravity.LEFT);
				tmp_button.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
				tmp_button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
				tmp_button.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						//onCall(v, s.telefono);
						try {
							addMagnetToLocalData(fm);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							Log.e("Error",""+e.getMessage(),e);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Log.e("Error",""+e.getMessage(),e);
						}
					}
				});
				store_call_pane.addView(tmp_button);
			}
		}		
	}

	@Override
	protected void onStart() {
		setBackground();
		File JsonFile = new File(getFilesDir(), "data.json");
		FridgeMagnetsManager fridgeMagnetReader = new FridgeMagnetsManager();
		try {
			this.myFridgeMagnets = fridgeMagnetReader.readJsonStream( new FileInputStream(JsonFile) );
			//Log.e("",fridgeMagnets.toString());
		} catch (Exception e) {
			//this.fridgeMagnets = new ArrayList<FridgeMagnet>();
		}
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		handler.removeCallbacks(queryInList);
		super.onStop();
	}

	public void addMagnetToLocalData(FridgeMagnet fm) throws FileNotFoundException, IOException{
		File JsonFile = new File(getFilesDir(), "data.json");		
		FridgeMagnetsManager fridgeMagnetWriter = new FridgeMagnetsManager();
		try{
			downloadImageFromServer(StaticUrls.FRIDGE_MAGNETS, fm.logo);
			Log.e("",fm.nombre);
			if(!this.myFridgeMagnets.contains(fm)){
				myFridgeMagnets.add(fm);
				fridgeMagnetWriter.writeJsonStream(new FileOutputStream(JsonFile), myFridgeMagnets);
			}
		}catch (Exception donotCare){ }
	}
	
	public void downloadImageFromServer(String url, String file) throws MalformedURLException, IOException {
		InputStream imageInputStream=new URL(url+file).openStream();
    	FileOutputStream imageOutputStream;
    	imageOutputStream = openFileOutput(file, Context.MODE_PRIVATE);
    	MainActivity.CopyStream(imageInputStream, imageOutputStream);
    	imageOutputStream.close();
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_fridge_magnet, menu);
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
}
