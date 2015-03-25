package com.netter.phonesilent;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class CopyOfMainActivity extends ActionBarActivity {

	AudioManager mAudioManager = null;
	double loc_lat = 0, loc_long = 0;
	String loc_long_String = null, loc_lat_String = null;
	GPSTracker myGPS;
	Button settings, savePrefbtn;
	ToggleButton startServiceButton;
	AlertDialog.Builder sampleDialog = null;
	SharedPreferences saveData;
	View dialoglayout = null;
	int hour, min = 0;
	TextView hour_tv, min_tv, lat_tv, long_tv;
	AlarmManager alarm = null;
	PendingIntent pintent = null;

	public static final int DIALOG_SAMPLE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		myGPS = new GPSTracker(this);
		hour_tv = (TextView) findViewById(R.id.TextViewSetHour);
		min_tv = (TextView) findViewById(R.id.TextViewSetMin);
		lat_tv = (TextView) findViewById(R.id.TextViewSetLat);
		long_tv = (TextView) findViewById(R.id.TextViewSetLong);

		getSavedPreferences();

		Button settings = (Button) findViewById(R.id.buttonSettings);
		settings.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_SAMPLE);
			}
		});
		final Intent startLocationService = new Intent(CopyOfMainActivity.this,
				MyStartService.class);

		startServiceButton = (ToggleButton) findViewById(R.id.toggleButtonStartStop);
		startServiceButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							getSavedPreferences();
							// Set the alarm to start at approximately 2:00 p.m.
							Calendar calendar = Calendar.getInstance();
							calendar.setTimeInMillis(System.currentTimeMillis());
							calendar.set(Calendar.HOUR_OF_DAY, hour);
							calendar.set(Calendar.MINUTE, min);
							Intent intent = new Intent(CopyOfMainActivity.this,
									MyStartService.class);
							pintent = PendingIntent.getService(
									CopyOfMainActivity.this, 0, intent, 0);
							alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
							alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
									calendar.getTimeInMillis(),
									AlarmManager.INTERVAL_DAY, pintent);
							startService(intent);
						} else {
							Toast.makeText(getApplicationContext(),
									"Service Stopped", Toast.LENGTH_SHORT)
									.show();
							myGPS.stopLocationService();
							if (alarm != null) {
								alarm.cancel(pintent);
							}
							stopService(startLocationService);
						}

					}
				});
	}

	protected void onResume() {
		super.onResume();
		getSavedPreferences();
	}

	protected void onRestart() {
		super.onRestart();
		getSavedPreferences();
	}

	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_SAMPLE:
			LayoutInflater inflater = getLayoutInflater();
			dialoglayout = inflater.inflate(R.layout.activity_settings, null);
			sampleDialog = new AlertDialog.Builder(CopyOfMainActivity.this);
			sampleDialog
					.setView(dialoglayout)
					.setPositiveButton("Save",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									boolean isSaved = savePreferences();

									if (isSaved) {
										Toast.makeText(getApplicationContext(),
												"Save Successful",
												Toast.LENGTH_LONG).show();
										dialog.dismiss();
									} else {
										Toast.makeText(getApplicationContext(),
												"Save Unsuccessful",
												Toast.LENGTH_LONG).show();
									}
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
			dialoglayout.findViewById(R.id.buttonGetLoc).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							Toast.makeText(getApplicationContext(),
									"get location", Toast.LENGTH_LONG).show();
							TextView currLocation = (TextView) dialoglayout
									.findViewById(R.id.editTextCurrLoc);
							if (myGPS.canGetLocation) {
								myGPS.getLocation(true);
								loc_lat = myGPS.getLatitude();
								loc_long = myGPS.getLongitude();
								currLocation.setText("Lat: "
										+ myGPS.getLatitude() + " Lon: "
										+ myGPS.getLongitude());
							} else {
								currLocation
										.setText("Error - Unable to find current location");
							}

						}
					});

		}
		return super.onCreateDialog(id);
	}

	public boolean savePreferences() {

		EditText hour_et = (EditText) dialoglayout
				.findViewById(R.id.editTextHour);
		EditText min_et = (EditText) dialoglayout
				.findViewById(R.id.editTextMin);
		boolean isSaved = false;
		int hour_sharedPref = 18, min_sharedPref = 0;
		saveData = getSharedPreferences("myData", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = saveData.edit();

		hour_sharedPref = Integer.parseInt(hour_et.getText().toString());
		min_sharedPref = Integer.parseInt(min_et.getText().toString());

		editor.putInt("Hour", hour_sharedPref);
		editor.putInt("Min", min_sharedPref);
		editor.putString("Lat", String.valueOf(loc_lat));
		editor.putString("Long", String.valueOf(loc_long));

		isSaved = editor.commit();

		return isSaved;
	}

	public void getSavedPreferences() {

		saveData = getSharedPreferences("myData", Context.MODE_PRIVATE);
		if (saveData.contains("Hour")) {
			hour = saveData.getInt("Hour", 11);
		} else
			hour = 10;
		if (saveData.contains("Min")) {
			min = saveData.getInt("Min", 45);
		} else
			min = 45;

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

		if (hour < 10)
			hour_tv.setText("0" + String.valueOf(hour));
		else
			hour_tv.setText(String.valueOf(hour));

		if (min < 10)
			min_tv.setText("0" + String.valueOf(min));
		else
			min_tv.setText(String.valueOf(min));

		lat_tv.setText("(" + loc_lat_String + ", ");
		long_tv.setText(loc_long_String + ")");
	}

}
