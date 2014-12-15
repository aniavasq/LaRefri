package com.larefri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;

public class LocationTask extends AsyncTask<Void, Void, List<Address>> {

	private Location currentLocation;
	private LocationManager locationManager;
	private double currentLatitude;
	private double currentLongitude;
	private Context context;
	private Activity parent;
	private static List<Address> addresses;
	private static SharedPreferences settings;
	private String providerFine;
	private String providerCoarse;
	public static final int OUT_OF_SERVICE = 0;
	public static final int TEMPORARILY_UNAVAILABLE = 1;
	public static final int AVAILABLE = 2;


	public LocationTask(Context context, Activity parent) {
		super();
		this.context = context;
		this.parent = parent;
		settings = parent.getSharedPreferences(MainActivity.PREFS_NAME, 0);
	}
	
	public static void sharedPreferences(Activity master){
		settings = master.getSharedPreferences(MainActivity.PREFS_NAME, 0);
	}

	private void updateLocation(Location location){
		try{
			this.currentLocation = location;
			this.currentLatitude = this.currentLocation.getLatitude();
			this.currentLongitude = this.currentLocation.getLongitude();
		}catch(Exception doNotCare){ }
	}

	public static List<Address> getAddresses() {
		return addresses;
	}

	public static String getCity(){
		return settings.getString("current_city", "NO_CITY");
	}

	public void setCity(String current_city){
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("current_city", current_city);
		editor.commit();
	}
	
	public static String getCountry(){
		return settings.getString("current_country", "NO_COUNTRY");
	}

	public void setCountry(String current_country){
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("current_country", current_country);
		editor.commit();
	}
	
	public static String getRegion(){
		return settings.getString("current_region", "NO_REGION");
	}

	public void setRegion(String current_region){
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("current_region", current_region);
		editor.commit();
	}

	private void setAddresses(){
		int tryes = 0;
		Geocoder gcd = new Geocoder(context, Locale.getDefault());
		addresses = new ArrayList<Address>();
		while(tryes<5){
			try {
				addresses = gcd.getFromLocation(currentLatitude, currentLongitude,10);
				break;
			} catch (IOException doNotCare) { }
			tryes++;
		}
		if (addresses.size() > 0){
			//Log.e("LOCATION",this.addresses.get(0).getAdminArea());
			this.setCity(addresses.get(0).getLocality().toString());
			this.setCountry(addresses.get(0).getCountryName().toString());
			this.setRegion(addresses.get(0).getAdminArea().toString());
		}
	}

	public Location getCurrentLocation() {
		return this.currentLocation;
	}

	public void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
	}

	public Criteria initCriteria(){
		Criteria criteria = new Criteria();
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return criteria;
	}

	@Override
	protected void onPreExecute() {
		this.locationManager = (LocationManager) parent.getSystemService(Context.LOCATION_SERVICE);      
	}

	@Override
	protected List<Address> doInBackground(Void... params) {
		Criteria criteria = initCriteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);		
		this.providerFine = this.locationManager.getBestProvider(criteria, true);
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		this.providerCoarse = this.locationManager.getBestProvider(criteria, true);
		if (providerCoarse != null) {
			updateLocation(this.locationManager.getLastKnownLocation(providerCoarse));
		}else if(providerFine != null){
			updateLocation(this.locationManager.getLastKnownLocation(providerFine));
		}else{
			updateLocation(this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
		}
		setAddresses();
		return getAddresses();
	}
}
