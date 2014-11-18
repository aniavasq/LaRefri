package com.larefri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public interface AddMagnet {
	
	class AddOnClickListener implements OnClickListener{
		private FridgeMagnet fm;
		private Button button;
		private Activity master;
		
		public AddOnClickListener(FridgeMagnet fm, Button button, Activity master){
			this.fm = fm;
			this.button = button;
			this.master = master;
		}
		
		@Override
		public void onClick(View v) {
			Resources resources = master.getResources();
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("id_marca", fm.id_marca.toString());
			try {
				((AddMagnet)master).addMagnetToLocalData(this.fm);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			//construct form to HttpRequest
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("id_marca", fm.id_marca.toString()));
			DownloadFridgeMagnetLogoTask downloadFMLogoTask = new DownloadFridgeMagnetLogoTask(master);
			downloadFMLogoTask.execute(
					StaticUrls.FRIDGE_MAGNETS, 
					fm.logo);
			MainActivity.addDownloadFMLogoTasks(downloadFMLogoTask);
			
			(new DownloadMagnetStoresRestClient(master)).execute(
					StaticUrls.SUCURSALES_URL, 
					params,
					nameValuePairs,
					this.fm);
			(new DownloadFlyerRestClient(master)).execute(
					StaticUrls.FLYER_PROMO, 
					params,
					nameValuePairs,
					this.fm);
			button.setBackground(resources.getDrawable(R.drawable.menu_button_bg_disabled));
			button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_accept, 0);
			button.setOnClickListener(new RemoveOnClickListener(fm, button, master));
			button.setPadding(10, 0, 10, 1);
		}
	}
	
	class RemoveOnClickListener implements OnClickListener{
		private FridgeMagnet fm;
		private Button button;
		private Activity master;
		
		public RemoveOnClickListener(FridgeMagnet fm, Button button, Activity master){
			this.fm = fm;
			this.button = button;
			this.master = master;
		}
		@Override
		public void onClick(View v) {
			Resources resources = master.getResources();
			((AddMagnet) master).myFridgeMagnetsRemove(fm);
			try {
				((AddMagnet) master).saveFridgeMagnetsList();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			button.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
			button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
			button.setOnClickListener(new AddOnClickListener(fm, button, master));
			button.setPadding(10, 0, 10, 1);
		}
	}
	
	public void addMagnetToLocalData(FridgeMagnet fm) throws FileNotFoundException, IOException;
	public void myFridgeMagnetsRemove(FridgeMagnet fm);
	public  void saveFridgeMagnetsList() throws FileNotFoundException, IOException;
}
