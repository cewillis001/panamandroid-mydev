package com.android.projecte.townportal;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

//
// Activity that allows user to select a new location
//
public class ChangeLocationActivity extends Activity {

	ListView placeList;
	PreferredLocationManager plm;
	
	@Override
    protected void onCreate( Bundle savedInstanceState ) {
		
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_change_location );
        setTitle("Select Location");
        
        plm = new PreferredLocationManager(getApplicationContext());
        placeList = (ListView) findViewById(R.id.places);
        
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, plm.getAllPlaceNames());
        placeList.setAdapter(adapter);
        
        placeList.setOnItemClickListener( new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String selected = ((TextView)view).getText().toString();
				plm.setPreferredLocation(selected);
				finish();
			}
		});
        
	}
	
	@Override
    protected void onStop() {
    	super.onStop();
    	plm.stopLocationManager();
    }
	
} // preferredLocationChangeIsWaiting
