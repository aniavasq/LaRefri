package com.larefri;

import android.app.Activity;
import android.content.res.Resources;
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
			MainActivity.addMyFridgeMagnet(button.getFm());
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
			
			button.setBackground(resources.getDrawable(R.drawable.menu_button_bg));
			button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add, 0);
			button.setOnClickListener(new AddOnClickListener(button, master));
			button.setPadding(10, 0, 10, 1);
		}
	}
	
	public void addMagnetToLocalData(Store fm);
	public void myFridgeMagnetsRemove(Store fm);
}
