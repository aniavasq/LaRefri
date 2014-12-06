package com.larefri;

import java.io.FileNotFoundException;
import java.io.IOException;
import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public interface AddMagnet {
	
	class AddOnClickListener implements OnClickListener{
		private FridgeMagnetButton button;
		private Activity master;
		
		public AddOnClickListener( FridgeMagnetButton button, Activity master){
			button.getFm();
			this.button = button;
			this.master = master;
		}
		
		@Override
		public void onClick(View v) {
			Resources resources = master.getResources();
			button.setBackground(resources.getDrawable(R.drawable.menu_button_bg_disabled));
			button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_accept, 0);
			button.setOnClickListener(new RemoveOnClickListener(button, master));
			button.setPadding(10, 0, 10, 1);
			button.getFm().saveToLocalDataStore();
		}
	}
	
	class RemoveOnClickListener implements OnClickListener{
		private Store fm;
		private FridgeMagnetButton button;
		private Activity master;
		
		public RemoveOnClickListener(FridgeMagnetButton button, Activity master){
			this.fm = button.getFm();
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
			button.setOnClickListener(new AddOnClickListener(button, master));
			button.setPadding(10, 0, 10, 1);
		}
	}
	
	class Indexer{
		private static Integer index;
		private static Indexer INSTANCE = null;
		
		private Indexer(){
			Indexer.index = 0;
		}
		
		private static void createInstance(){
			Indexer.INSTANCE = new Indexer();
		}
		
		public Indexer getInstance(){
			if (Indexer.INSTANCE == null){
				createInstance();
			}
			return Indexer.INSTANCE;
		}
		
		public synchronized static int nextIndex(){
			if (Indexer.INSTANCE == null){
				createInstance();
				Log.e("NEW INSTANCE","");
			}
			Indexer.index= Indexer.index +1;
			return Indexer.index;
		}
		
		public synchronized static int removeIndex(){
			if (Indexer.INSTANCE == null){
				createInstance();
			}
			Indexer.index=-1;
			return Indexer.index;
		}
	}
	
	public void addMagnetToLocalData(Store fm) throws FileNotFoundException, IOException;
	public void myFridgeMagnetsRemove(Store fm);
	public  void saveFridgeMagnetsList() throws FileNotFoundException, IOException;
}
