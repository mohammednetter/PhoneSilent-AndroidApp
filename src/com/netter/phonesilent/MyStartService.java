package com.netter.phonesilent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.IBinder;
import android.widget.Toast;

public class MyStartService extends Service {

	GPSTracker myGPS;
	SharedPreferences saveData = null;
	double loc_lat = 0, loc_long = 0;
	String loc_long_String = null, loc_lat_String = null;
	CopyOfMainActivity main_activity = null;
	Thread t;
	LocationManager locationManager = null;

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 0; // 1 minute

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		myGPS.stopLocationService();
		if (t.isAlive())
			t.stop();
	}

	@Override
	public int onStartCommand(Intent Intent, int flags, int startId) {

		Toast.makeText(getApplicationContext(), "Service Started",
				Toast.LENGTH_SHORT).show();
		saveData = getSharedPreferences("myData", Context.MODE_PRIVATE);
		if (saveData.contains("Lat")) {
			loc_lat_String = saveData.getString("Lat", "0.0");
			loc_lat = Double.parseDouble(loc_lat_String);
		} else
			loc_lat = 0.0;

		if (saveData.contains("Lat")) {
			loc_long_String = saveData.getString("Long", "0.0");
			loc_long = Double.parseDouble(loc_long_String);
		} else
			loc_long = 0.0;

		myGPS = new GPSTracker(this, loc_lat, loc_long);

		Runnable r = new Runnable() {
			public void run() {
				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				Criteria criteria = new Criteria();
				criteria.setPowerRequirement(Criteria.POWER_LOW);
				criteria.setAccuracy(Criteria.ACCURACY_LOW);
				criteria.setAltitudeRequired(false);
				criteria.setBearingRequired(false);
				criteria.setCostAllowed(true);
				criteria.setSpeedRequired(false);
				String bestProvider = locationManager.getBestProvider(criteria,
						false);
				locationManager.requestLocationUpdates(bestProvider, 0, 0,
						myGPS);
			}
		};

		// Start Thread
		t = new Thread(r);
		t.start();
		return START_STICKY;
	}

}
