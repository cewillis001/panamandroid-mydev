package com.android.projecte.townportal;


import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/*
 * Twitter Activity
 * Description: pulls local tweets 
 */
public class TwitterActivity extends Activity {

    //re-design: when the button is pressed in the main activity a webview pops up containing the widget
	//from twitter. Will need to enable javascript.
	//heavily inspired by textbook, pg 208
	WebView browser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_feed);
		browser=(WebView)findViewById(R.id.webkit);
		browser.getSettings().setJavaScriptEnabled(true);
		browser.loadUrl("file:///android_asset/twitter.html");
		
		//I'm considering adding some code to update the height of the widget to match the screen of the
		//device but only if there is time.
		
		//also, should probably add at min the ability to use local tweets by long/lat if that option was picked
		//this will probably actually be harder??
	}
	
}