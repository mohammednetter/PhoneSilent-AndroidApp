package com.netter.phonesilent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class GPSTracker extends Activity implements LocationListener {

	private final Context mContext;
	AudioManager mAudioManager = null;
	TextView distanceDisp = null;
	CopyOfMainActivity mainActivity = new CopyOfMainActivity();
	
	
	// flag for GPS status
	public boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// flag for GPS status
	boolean canGetLocation = false;

	Location location; // location
	double latitude; // latitude
	double longitude; // longitude

	double setLatitute, setLongitute;
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 0; // 1 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;


	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation(false);
	}

	public GPSTracker(Context context, double setLat, double setLong) {

		this.mContext = context;
		setLatitute = setLat;
		setLongitute = setLong;
		getLocation(false);
	}

	/**
	 * Function to get the user's current location
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	public Location getLocation(boolean isCurrent) {
		try {

			locationManager = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);
			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			Log.v("isGPSEnabled", "=" + isGPSEnabled);

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

			if (isGPSEnabled == false && isNetworkEnabled == false) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					location = null;
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					location = null;
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS Enabled", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
			}
			if(isCurrent)
				locationManager.removeUpdates(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app
	 * */
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}

		// return latitude
		return latitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}

		// return longitude
		return longitude;
	}
	
	public void stopLocationService() {
			locationManager.removeUpdates(this);
	}

	/**
	 * Function to check GPS/wifi enabled
	 * 
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	/**
	 * Function to show settings alert dialog On pressing Settings button will
	 * lauch Settings Options
	 * */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog
				.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						mContext.startActivity(intent);
					}
				});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location1) {
		mAudioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		int ringermode = mAudioManager.getRingerMode();
		double dist = distance(setLatitute, setLongitute, location1.getLatitude(),
				location1.getLongitude());
		if (dist < 5) { 
				// CALL SERVICE TO SILENT PHONE
				if (!(ringermode == AudioManager.RINGER_MODE_SILENT)) {
					mAudioManager
							.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					Toast.makeText(mContext, "Silent Mode Activated", Toast.LENGTH_SHORT).show();
				}
				
			} else {
				if (!(ringermode == AudioManager.RINGER_MODE_NORMAL)) {
					mAudioManager
							.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					Toast.makeText(mContext, "Silent Mode DeActivated", Toast.LENGTH_SHORT).show();
					stopLocationService();
				}
			}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	/** calculates the distance between two locations in MILES */
	private double distance(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 3958.75; // in miles, change to 6371 for kilometer
										// output
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
				* Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;
		return dist; // output distance, in MILES
	}

}
