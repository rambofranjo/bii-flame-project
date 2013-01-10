package com.btd.demo;

import java.text.SimpleDateFormat;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import android.view.View;
import android.view.View.OnClickListener;

public class MySecondActivity extends Activity implements OnClickListener {
	
	private TextView textViewLight = null; 
	private float sensorReading = 0;

	private Button buttonBack = null;
	private Button buttonTwitter = null;
	
	
	 private Twitter mTwitter = null; 
	 
	 // those codes allow our app to let the user log into twitter and post stuff
	 private String OAUTH_CONSUMER_KEY = "xlRLYbLQ2foJZZHFtLgd6Q";
	 private String OAUTH_CONSUMER_SECRET= "JcetMGYoWf7V8BXKdOZU3xg9yoByH6SvYjjli1HnU";
	 private RequestToken mRequestToken = null;
	 private String accessToken = "";
	 private String accessTokenSecret = "";
	 private String BTD_GROUP =  "Group 12 - Crazy sheeps"; // make up some name for your group here


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_second);
		
		
		// we can access our button object from activity_my_second.xml by adding this line of code:
		buttonBack = (Button) findViewById (R.id.buttonBack);
		buttonBack.setOnClickListener(this);
		
		buttonTwitter = (Button) findViewById (R.id.buttonTwitter);
		buttonTwitter.setOnClickListener(this);
		
		
		textViewLight = (TextView) findViewById(R.id.textViewSensorValue);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			sensorReading = bundle.getFloat("sensor");
			textViewLight.setText("Sensor reading: " +sensorReading);
		
			Log.d("MySecondActivity","Sensor reading: "+sensorReading);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonBack:
			Intent myIntent = new Intent(this, MainActivity.class);
	    	startActivity(myIntent);
			break;
		case R.id.buttonTwitter:
        			LogIntoTwitterTask lit = new LogIntoTwitterTask();
        			lit.execute();
			break;
		}
	}
	

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    Log.d("MySecondActivity","onActivityResult: "+requestCode);
	    // twitter
	    if (requestCode == 123456789) {
	        if (resultCode == Activity.RESULT_OK)	{
	            String oauthVerifier = (String) data.getExtras().get("oauth_verifier");
	            if (oauthVerifier!=null) { // Login successfully
	            	TwitterUpdateStatusTask tus = new TwitterUpdateStatusTask();
	            	tus.setVerifier(oauthVerifier);
	            	tus.execute();
	            }      	
		     } 
	    } 
	}
	

	/* AsyncTask for logging into Twitter off the UI thread */
	public class LogIntoTwitterTask extends AsyncTask<String, Void, String> {	
	    protected String doInBackground(String... params) {
	    	Log.d("MySecondActivity","LogIntoTwitterTask doInBackground");
			mTwitter = new TwitterFactory().getInstance();
			mRequestToken = null;
			mTwitter.setOAuthConsumer( OAUTH_CONSUMER_KEY , OAUTH_CONSUMER_SECRET );
		 		
		    try {
				mRequestToken = mTwitter.getOAuthRequestToken("tcallback");
			} catch (TwitterException e) {
				Log.w("MySecondActivity","error login twitter - " + e.toString());
				e.printStackTrace();
			}	     
	    	Log.d("MySecondActivity","LogIntoTwitterTask doInBackground finished.");
		    return null; 
	    }

	    protected void onPostExecute(String result) {	
	    	Log.d("MySecondActivity","LogIntoTwitterTask onPostExecute");
	    	Intent i = new Intent(getApplicationContext(), com.btd.demo.TwitterWebActivity.class);
			i.putExtra("URL", mRequestToken.getAuthenticationURL());
		    startActivityForResult(i, 123456789);  
	    }

	    protected void onPreExecute() {    
	    }
	}
	

	/* AsyncTask for getting Twitter access tokens and for posting stuff on Twitter off the UI thread */
	public class TwitterUpdateStatusTask extends AsyncTask<String, Void, String> {	
		private String oauthVerifier = "";
		public void setVerifier(String v) {
			oauthVerifier = v;
		}	
		 
	    protected String doInBackground(String... params) {
	    	try {
	    		AccessToken at = null;
                    at = mTwitter.getOAuthAccessToken(mRequestToken, oauthVerifier);
                if (at!= null) {
                	accessToken = at.getToken();
                	accessTokenSecret = at.getTokenSecret();             
	           		Configuration conf = new ConfigurationBuilder()
	        	    .setOAuthConsumerKey( OAUTH_CONSUMER_KEY )
	        	    .setOAuthConsumerSecret( OAUTH_CONSUMER_SECRET )
	        	    .setOAuthAccessToken(accessToken)
	        	    .setOAuthAccessTokenSecret(accessTokenSecret)
	        	    .build(); 
	        		TwitterFactory tf = new TwitterFactory(conf);
	        		Twitter twitter = tf.getInstance();
	        		try {
	          		  java.sql.Timestamp filestamp = new java.sql.Timestamp(System.currentTimeMillis());
	        		  String stampstr = new SimpleDateFormat("MM-dd_HH-mm-ss").format(filestamp);
	        		  twitter4j.Status status = twitter.updateStatus(BTD_GROUP+" at "+stampstr+ " current sensor: "+sensorReading);
	        		  Log.d("MySecondActivity","status: "+status.getText()); // gibt einen netten url zum foto zurück
	        		} catch (TwitterException e) { 
	        			
	        		}              
                } 
            }
            catch (TwitterException e)
            {
                e.printStackTrace();
            }  
	       return null; 
	    }

	    protected void onPostExecute(String result) {	
	    }

	    protected void onPreExecute() {    
	    }
	} 	


	
	

}

