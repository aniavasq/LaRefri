package com.larefri;

import java.io.FileNotFoundException;
import java.io.IOException;
import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;

public interface AddMagnet {
	
	class AddOnClickListener implements OnClickListener{
		private FridgeMagnet fm;
		private FridgeMagnetButton button;
		private Activity master;
		private DownloadFridgeMagnetDataTask downloadDataTask;
		
		public AddOnClickListener( FridgeMagnetButton button, Activity master){
			this.fm = button.getFm();
			this.button = button;
			this.master = master;
			downloadDataTask = new DownloadFridgeMagnetDataTask();
		}
		
		@Override
		public void onClick(View v) {
			Resources resources = master.getResources();
			downloadDataTask.execute(fm, master);
			button.setBackground(resources.getDrawable(R.drawable.menu_button_bg_disabled));
			button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_accept, 0);
			button.setOnClickListener(new RemoveOnClickListener(button, master, downloadDataTask));
			button.setPadding(10, 0, 10, 1);
		}
	}
	
	class RemoveOnClickListener implements OnClickListener{
		private FridgeMagnet fm;
		private FridgeMagnetButton button;
		private Activity master;
		private DownloadFridgeMagnetDataTask downloadDataTask;
		
		public RemoveOnClickListener(FridgeMagnetButton button, Activity master, DownloadFridgeMagnetDataTask downloadDataTask){
			this.fm = button.getFm();
			this.button = button;
			this.master = master;
			this.downloadDataTask = downloadDataTask;
		}
		@Override
		public void onClick(View v) {
			if(downloadDataTask!=null) downloadDataTask.cancel();
			Resources resources = master.getResources();
			((AddMagnet) master).myFridgeMagnetsRemove(fm);
			try {
				((AddMagnet) master).saveFridgeMagnetsList();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			button.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
			button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
			button.setOnClickListener(new AddOnClickListener(button, master));
			button.setPadding(10, 0, 10, 1);
		}
	}
	
	public void addMagnetToLocalData(FridgeMagnet fm) throws FileNotFoundException, IOException;
	public void myFridgeMagnetsRemove(FridgeMagnet fm);
	public  void saveFridgeMagnetsList() throws FileNotFoundException, IOException;
}
