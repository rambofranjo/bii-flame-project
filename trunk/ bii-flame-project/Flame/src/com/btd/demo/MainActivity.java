package com.btd.demo;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements SensorEventListener, OnClickListener{
	
	private SensorManager mSensorManager;
	private Sensor mLight;
	private Button buttonRead = null;  
	private float sensorReading = 0;

	private Button buttonLight = null;
	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			 if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
			    unbindService(arduinoConnection);
			    Log.d("MainActivity","Service unbound");
				finish();
			}
		}
	}; 
	
	private Handler messageHandler = new Handler();
	class RunnableForArduinoService implements Runnable {
	  	public long msg; // 
			@Override
			public void run() {
				if (msg==1) { 
					Bundle b = new Bundle();
					b.putFloat("sensor", sensorReading);
		    	    Intent myIntent = new Intent(getApplicationContext(), MySecondActivity.class);
		    	    myIntent.putExtras(b);
			    	startActivity(myIntent);
				}
			}
	  	 
	  }  

	private ArduinoService.MyServiceBinder arduinoBinder = null;
	private ServiceConnection arduinoConnection = 
		new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			arduinoBinder = (ArduinoService.MyServiceBinder) arg1;
			arduinoBinder.setRunnable(new RunnableForArduinoService());
			arduinoBinder.setActivityCallbackHandler(messageHandler);
			Log.d("MainActivity","Arduino Service is connected!");				
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {		
			Log.d("MainActivity","Arduino Service is disconnected!");	
		}

	};


	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// here you set the layout resource that is defining the UI of the MainActivity
		setContentView(R.layout.activity_main);
		
		mSensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		
		// we can access our button object from activity_main.xml by adding this line of code:
		buttonRead = (Button) findViewById (R.id.buttonSensor);
		buttonRead.setOnClickListener(this);
		
		buttonLight = (Button) findViewById(R.id.buttonLight);
		buttonLight.setOnClickListener(this);
		buttonLight.setVisibility(View.VISIBLE);
		IntentFilter filter = new IntentFilter("com.google.android.BeyondTheDesktop.action.USB_PERMISSION");
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);	

		
		
	}
	
	@Override
	protected void onResume() {
		 
		final Intent netzwerkIntent = new Intent(getApplicationContext(), ArduinoService.class);
		bindService(netzwerkIntent, arduinoConnection, Context.BIND_AUTO_CREATE);
		    	
		
		// add listener. The listener will be MainActivity (this) class
		mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
		super.onResume();
	}
	
	@Override
	protected void onPause() {	
		mSensorManager.unregisterListener(this);
		super.onPause();
	    Log.d("MainActivity","onPause"); 
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType()==Sensor.TYPE_LIGHT) {
			sensorReading = event.values[0];
			Log.d("MainActivity","Sensor Reading: "+event.values[0]);
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.buttonSensor:
			// Bundle is a mapping from String values to various Parcelable types. 
			Bundle b = new Bundle();
			b.putFloat("sensor", sensorReading);
    	    Intent myIntent = new Intent(this, MySecondActivity.class);
    	    myIntent.putExtras(b);
	    	startActivity(myIntent);
			break;
			
		case R.id.buttonLight:
			arduinoBinder.sendMessageToArduino();
			break;
		}
	}
	
	
	
	
}


