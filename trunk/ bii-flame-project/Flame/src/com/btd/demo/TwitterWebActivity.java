package com.btd.demo;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent; 
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterWebActivity extends Activity {

	private Intent mIntent;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_twitter_web);
	    mIntent = getIntent();
	    String url = (String) mIntent.getExtras().get("URL");
	    WebView webView = (WebView)findViewById(R.id.webview);
	    WebSettings mWebSettings = webView.getSettings();
	    mWebSettings.setSavePassword(false);
	    webView.setWebViewClient( new WebViewClient() {	    	
	    	@Override
	    	public void onPageFinished(WebView view, String url) {		
	            if( url.contains("tcallback") ){
	                Uri uri = Uri.parse( url );
	                String oauthVerifier = uri.getQueryParameter( "oauth_verifier" );
	                String oauthToken = uri.getQueryParameter( "oauth_token" );
	                mIntent.putExtra( "oauth_verifier", oauthVerifier );
	                mIntent.putExtra( "oauth_token", oauthToken );
	                setResult( RESULT_OK, mIntent ); 
	                finish();
	            } 
	    	}    	
	    }); 
	    webView.loadUrl(url);
	}    
}

