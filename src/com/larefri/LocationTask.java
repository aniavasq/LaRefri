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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

public class LocationTask extends AsyncTask<Void, Void, List<Address>> implements LocationListener {

	private Location currentLocation;
	private LocationManager locationManager;
	private double currentLatitude;
	private double currentLongitude;
	private Context context;
	private Activity parent;
	private List<Address> addresses;
	private SharedPreferences settings;
	private String providerFine;
	private String providerCoarse;
	public static final int OUT_OF_SERVICE = 0;
	public static final int TEMPORARILY_UNAVAILABLE = 1;
	public static final int AVAILABLE = 2;
	

	public LocationTask(Context context, Activity parent) {
		super();
		this.context = context;
		this.parent = parent;
		this.settings = parent.getSharedPreferences("LaRefriPrefsFile", 0);
	}

	@Override
	public void onLocationChanged(Location location) {
		updateLocation(location);
        setAddresses();
	}

	@Override
	public void onProviderDisabled(String provider) {	}

	@Override
	public void onProviderEnabled(String provider) { }

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) { }
	
	private void updateLocation(Location location){
        this.currentLocation = location;
        this.currentLatitude = this.currentLocation.getLatitude();
        this.currentLongitude = this.currentLocation.getLongitude();
    }

	public List<Address> getAddresses() {
		return this.addresses;
	}
	
	public String getCity(){
		return this.settings.getString("current_city", "NO_CITY");
	}
	
	public void setCity(String current_city){
		SharedPreferences.Editor editor = this.settings.edit();
		editor.putString("current_city", current_city);
		editor.commit();
	}
	
	private void setAddresses(){
        int tryes = 0;
		Geocoder gcd = new Geocoder(context, Locale.getDefault());
        this.addresses = new ArrayList<Address>();
        while(tryes<5){
			try {
				this.addresses = gcd.getFromLocation(currentLatitude, currentLongitude,10);
				break;
			} catch (IOException e) { tryes--; }
			tryes++;
        }
		if (addresses.size() > 0){
            this.setCity(this.addresses.get(0).getLocality().toString());
            Log.e("CURRENT CITY",this.getCity());
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
		Looper.prepare();
		Criteria criteria = initCriteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);		
		this.providerFine = this.locationManager.getBestProvider(criteria, true);
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		this.providerCoarse = this.locationManager.getBestProvider(criteria, true);
        if (providerCoarse != null) {
			this.locationManager.requestSingleUpdate(this.providerCoarse, this, Looper.myLooper());
		}else if(providerFine != null){
			this.locationManager.requestSingleUpdate(this.providerFine, this, Looper.myLooper());
		}else{
			this.locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, Looper.myLooper());
		}
		setAddresses();
		Log.e("STOP","Has stoped");
		return getAddresses();
	}

	@Override
	protected void onPostExecute(List<Address> result) {
		this.locationManager.removeUpdates(this);
	}
}
