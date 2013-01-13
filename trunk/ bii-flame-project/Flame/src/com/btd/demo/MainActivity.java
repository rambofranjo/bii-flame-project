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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.facebook.*;
import com.facebook.model.*;

public class MainActivity extends FragmentActivity implements SensorEventListener, OnClickListener{
	
	private UiLifecycleHelper uiHelper;
	
	private boolean isResumed = false;
	private static final int SPLASH = 0;
	private static final int SELECTION = 1;
	private static final int FRAGMENT_COUNT = SELECTION +1;

	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
	
	private SensorManager mSensorManager;
	private Sensor mLight; 
	
	//private float sensorReading = 0;

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
				/**
				if (msg==1) { 
					Bundle b = new Bundle();
					b.putFloat("sensor", sensorReading);
		    	    Intent myIntent = new Intent(getApplicationContext(), MySecondActivity.class);
		    	    myIntent.putExtras(b);
			    	startActivity(myIntent);
				}**/
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
		
	    uiHelper = new UiLifecycleHelper(this, callback);
	    uiHelper.onCreate(savedInstanceState);
		
		// here you set the layout resource that is defining the UI of the MainActivity
		setContentView(R.layout.main);
		
		mSensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		
		IntentFilter filter = new IntentFilter("com.google.android.BeyondTheDesktop.action.USB_PERMISSION");
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
		
		
		
		FragmentManager fm = getSupportFragmentManager();
	    fragments[SPLASH] = fm.findFragmentById(R.id.splashFragment);
	    fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);

	    FragmentTransaction transaction = fm.beginTransaction();
	    for(int i = 0; i < fragments.length; i++) {
	        transaction.hide(fragments[i]);
	    }
	    transaction.commit();
		
	}
	
	private void showFragment(int fragmentIndex, boolean addToBackStack) {
	    FragmentManager fm = getSupportFragmentManager();
	    FragmentTransaction transaction = fm.beginTransaction();
	    for (int i = 0; i < fragments.length; i++) {
	        if (i == fragmentIndex) {
	            transaction.show(fragments[i]);
	        } else {
	            transaction.hide(fragments[i]);
	        }
	    }
	    if (addToBackStack) {
	        transaction.addToBackStack(null);
	    }
	    transaction.commit();
	}
	
	
	@Override
	protected void onResume() {
		 
		final Intent netzwerkIntent = new Intent(getApplicationContext(), ArduinoService.class);
		bindService(netzwerkIntent, arduinoConnection, Context.BIND_AUTO_CREATE);
		    	
		
		// add listener. The listener will be MainActivity (this) class
		mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
		super.onResume();
	    uiHelper.onResume();
	    isResumed = true;
	}
	
	@Override
	protected void onPause() {	
		mSensorManager.unregisterListener(this);
		super.onPause();
	    uiHelper.onPause();
	    isResumed = false;
	    
	    Log.d("MainActivity","onPause"); 
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    // Only make changes if the activity is visible
	    if (isResumed) {
	        FragmentManager manager = getSupportFragmentManager();
	        // Get the number of entries in the back stack
	        int backStackSize = manager.getBackStackEntryCount();
	        // Clear the back stack
	        for (int i = 0; i < backStackSize; i++) {
	            manager.popBackStack();
	        }
	        if (state.isOpened()) {
	            // If the session state is open:
	            // Show the authenticated fragment
	            showFragment(SELECTION, false);
	        } else if (state.isClosed()) {
	            // If the session state is closed:
	            // Show the login fragment
	            showFragment(SPLASH, false);
	        }
	    }
	}
	
	@Override
	protected void onResumeFragments() {
	    super.onResumeFragments();
	    Session session = Session.getActiveSession();

	    if (session != null && session.isOpened()) {
	        // if the session is already open,
	        // try to show the selection fragment
	        showFragment(SELECTION, false);
	    } else {
	        // otherwise present the splash screen
	        // and ask the user to login.
	        showFragment(SPLASH, false);
	    }
	}
	
	
	private Session.StatusCallback callback = 
	    new Session.StatusCallback() {
	    @Override
	    public void call(Session session, 
	            SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		/*
		if(event.sensor.getType()==Sensor.TYPE_LIGHT) {
			sensorReading = event.values[0];
			Log.d("MainActivity","Sensor Reading: "+event.values[0]);
		}
		*/
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		/**
		case R.id.buttonSensor:
			// Bundle is a mapping from String values to various Parcelable types. 
			Bundle b = new Bundle();
			b.putFloat("sensor", sensorReading);
    	    Intent myIntent = new Intent(this, MySecondActivity.class);
    	    myIntent.putExtras(b);
	    	startActivity(myIntent);
			break;
			**/
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	
	
	
}


