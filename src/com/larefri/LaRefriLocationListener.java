package com.larefri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class LaRefriLocationListener implements LocationListener {

	private Location currentLocation;
	private double currentLatitude;
	private double currentLongitude;
	private Context context;
	private List<Address> addresses;

	public LaRefriLocationListener(Context context) {
		super();
		this.context = context;
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
}
