package com.larefri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SearchFridgeMagnetActivity extends Activity implements AddMagnet{

	private SharedPreferences settings;
	private String logo;
	private Context context;
	private TextView search_txt;
	private List<Store> loadedFridgeMagnets, queryFridgeMagnets;

	class QueryInList implements TextWatcher{
		private List<Store> fridgeMagnets;

		public QueryInList(List<Store> loadedFridgeMagnets) {
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
			List<Store> tmpQueryFridgeMagnets;
			if(!queryText.isEmpty()){
				tmpQueryFridgeMagnets = new ArrayList<Store>();
				for(Store match: this.fridgeMagnets){
					if(match.getName()!= null && match.getName().toLowerCase().startsWith(queryText.toLowerCase())){
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
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		this.settings = getSharedPreferences("LaRefriPrefsFile", 0);
		this.search_txt = (TextView)findViewById(R.id.search_txt);

		//Set policy to HTTP
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		Bundle b = getIntent().getExtras();
		b.getInt("id_category");
		logo = b.getString("logo");

		ImageView image = (ImageView)findViewById(R.id.magnetfridge_logo);
		File imgFile = new File(getFilesDir(), logo);
		try {
			AddMagnetActivity.setImageSVGDrawable(image, imgFile, width/3-40, width/3-40);
		} catch (Exception e) { e.printStackTrace(); }		

		setupUI(findViewById(R.id.parent));
		
		/**********************************************************
		 * Parse support*/
		loadedFridgeMagnets = new ArrayList<Store>();
		getParseFridgeMagnets();
		/***********************************************************/
	}

	public void loadFridgeMagnetsButtons(List<Store> tmpQueryFridgeMagnets) {
		LinearLayout store_call_pane = (LinearLayout) findViewById(R.id.add_fridge_magnets_buttons);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.START);
		Resources resources = getResources();
		ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.menu_button);
		ArrayList<FridgeMagnetButton> buttons = new ArrayList<FridgeMagnetButton>();

		ArrayList<Store> metaRemove = new ArrayList<Store>(loadedFridgeMagnets);

		ArrayList<Store> remove = new ArrayList<Store>(tmpQueryFridgeMagnets);
		remove.removeAll(MainActivity.getMyFridgeMagnets());		
		store_call_pane.removeAllViews();
		for(final Store fm: remove){
			if(fm.getName()!=null){
				final FridgeMagnetButton tmp_button = new FridgeMagnetButton(themeWrapper, fm);
				tmp_button.setLayoutParams(lp);
				tmp_button.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
				tmp_button.setText(fm.getName());
				tmp_button.setTextColor(Color.WHITE);
				tmp_button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
				tmp_button.setPadding(10, 0, 10, 1);
				tmp_button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
				tmp_button.setOnClickListener(new AddOnClickListener(tmp_button, (Activity)context));
				buttons.add(tmp_button);
			}
		}

		metaRemove.removeAll(tmpQueryFridgeMagnets);

		List<Store> myRemove = MainActivity.getMyFridgeMagnets();
		myRemove.removeAll(metaRemove);
		myRemove.removeAll(remove);
		for(final Store fm: myRemove){
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
		Collections.sort(buttons, new FridgeMagnetButton.FridgeMagnetButtonComparator());
		for(FridgeMagnetButton b:buttons){
			store_call_pane.addView(b);
		}
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

	public void saveFridgeMagnetsList() throws FileNotFoundException, IOException {
		File JsonFile = new File(getFilesDir(), "data.json");		
		FridgeMagnetsManager fridgeMagnetWriter = new FridgeMagnetsManager();
		fridgeMagnetWriter.writeJsonStream(new FileOutputStream(JsonFile), MainActivity.getMyFridgeMagnets());
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
	
	public static void hideSoftKeyboard(Activity activity) {
	    InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	}
	
	public void setupUI(View view) {
	    //Set up touch listener for non-text box views to hide keyboard.
	    if(!(view instanceof TextView)) {
	        view.setOnTouchListener(new View.OnTouchListener() {
	            public boolean onTouch(View v, MotionEvent event) {
	            	v.performClick();
	                hideSoftKeyboard(SearchFridgeMagnetActivity.this);
	                return false;
	            }
	        });
	    }

	    //If a layout container, iterate over children and seed recursion.
	    if (view instanceof ViewGroup) {
	        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
	            View innerView = ((ViewGroup) view).getChildAt(i);
	            setupUI(innerView);
	        }
	    }
	}


	/**********************************************
	 * Parse support
	 * */
	private void getParseFridgeMagnets(){

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Store");
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> result, ParseException e) {
				if (e == null) {
					Store s;
					for(ParseObject fm: result){
						s = new Store(fm, context);
						loadedFridgeMagnets.add(s);
					}
					loadFridgeMagnetsButtons(loadedFridgeMagnets);
					search_txt.addTextChangedListener(new QueryInList(loadedFridgeMagnets));
				} else {
					Log.e("ERROR",e.getMessage(),e);
				}
			}
		});
	}
	/**********************************************/
}
