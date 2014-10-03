package com.larefri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class LocationTask extends AsyncTask<Void, Void, List<Address>> implements LocationListener {

	private Location currentLocation;
	private LocationManager locationManager;
	private double currentLatitude;
	private double currentLongitude;
	private Context context;
	private Activity parent;
	private List<Address> addresses;

	public LocationTask(Context context, Activity parent) {
		super();
		this.context = context;
		this.parent = parent;
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
        currentLocation = location;
        currentLatitude = currentLocation.getLatitude();
        currentLongitude = currentLocation.getLongitude();
    }

	public List<Address> getAddresses() {
		return addresses;
	}
	
	private void setAddresses(){
		Geocoder gcd = new Geocoder(context, Locale.getDefault());
        addresses = new ArrayList<Address>();
		try {
			addresses = gcd.getFromLocation(currentLatitude, currentLongitude,1);
		} catch (IOException e) {
			Log.e("ERROR GETTING LOCATION",e.getMessage(),e);
		}
        if (addresses.size() > 0){
            Log.e("ADRESSES", addresses.toString());
        }
	}

	public Location getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
	}

	@Override
	protected void onPreExecute() {
		locationManager = (LocationManager) parent.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);        
	}

	@Override
	protected List<Address> doInBackground(Void... params) {
		setAddresses();
		return getAddresses();
	}

	@Override
	protected void onPostExecute(List<Address> result) {
		locationManager = (LocationManager) parent.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(this);
	}
}
