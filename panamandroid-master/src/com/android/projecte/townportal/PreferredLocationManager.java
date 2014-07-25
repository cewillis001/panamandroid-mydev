package com.android.projecte.townportal;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

//
// PreferredLocationManager handles all that is associated with a user's (persistent) preferred location.
//
// Persistence is achieved by storing the preferred place name to shared preferences.
// After a user has selected a preferred location, coordinates are also remembered using 
// shared preferences whenever the map is dragged. This ensures that the map maintains
// its location (more or less) and doesn't jump back to the hard-coded coordinates when changing maps.
// 
// TODO [No time this semester]
// Place data is currently hard-coded to FSU campuses. It would make sense to make this dynamic
// so that new locations can be added & removed by the user. I don't think I'll have time for 
// this during group project.
//
public class PreferredLocationManager extends Object {
	
	private final String CURRENT_LOCATION = "Current Location";
	private Context context;
	private SharedPreferences sp;
	private LocationManager locationManager;
	private Location locationDetails;
	private String bestProvider;
	private Boolean alreadyStarted = false; // tracks if LocationManager has been initialized
	private ArrayList<PreferredLocationData> locations = new ArrayList<PreferredLocationData>(); // hard-code place data
	private Toast toast;
	
	public PreferredLocationManager(Context context) {
		
		// Populate preferred location ArrayList with hard-coded data
		locations.add(new PreferredLocationData(CURRENT_LOCATION, 0, 0));
		locations.add(new PreferredLocationData("Panama City", 30.1588130, -85.6602060));
		locations.add(new PreferredLocationData("Tallahassee", 30.4382560, -84.2807330));
		locations.add(new PreferredLocationData("London, UK", 51.5073510, -0.1277580));
		locations.add(new PreferredLocationData("Florence, Italy", 43.7710330, 11.2480010));
		locations.add(new PreferredLocationData("Valencia, Spain", 39.4699070, -0.3762880));
		locations.add(new PreferredLocationData("Republic of Panama", 8.9829946, -79.5164677));
    	   	
    	// Initialize the preferred location or set it to Panama City if first time running app.
    	this.context = context;
    	sp = this.context.getSharedPreferences("preferred_location", Context.MODE_PRIVATE);
    	initLocation(getPreferredLocation()); // remembered or preferred	
    }
	
	private void initLocation(String location) {
		if (!location.equals(CURRENT_LOCATION)) {
			if (alreadyStarted) stopLocationManager();
		}
		SharedPreferences.Editor editor = sp.edit();
		editor.putString("preferred_location", location);
		editor.commit();
	}
	
	// Returns coordinates of the location formatted for Google.
	public String getGoogleCoordinates() {
		if ( getRememberedCoordinates() != null ) {
			return getRememberedCoordinates();
		}
		return getPreferredCoordinates();
	}
	
	// Formats preferred latitude and longitude into single string for Google.
	public String getPreferredCoordinates() {
		return Double.toString(getLatitude()) + "," + Double.toString(getLongitude());
	}
	
	// Sets the name of the preferred location.
	// Stops locationManager if it is already started and preferred location is not current.
	public void setPreferredLocation(String location) {
		resetRememberedCoordinates();
		if (!location.equals(CURRENT_LOCATION)) {
			if (alreadyStarted) stopLocationManager();
		}
		SharedPreferences.Editor editor = sp.edit();
		editor.putString("preferred_location", location);
		editor.commit();
	}
	
	// Retrieves the name of the preferred location.
	// Initializes locationManager if the preferred location is current location.
	public String getPreferredLocation() {
		String preferredLocation = sp.getString("preferred_location", "Panama City");
		if ( preferredLocation.equals(CURRENT_LOCATION) ) {
			initLocationManager();
		}
		return preferredLocation;
	}
	
	// Gets latitude of preferred or current location.
	public double getLatitude() {
		String preferredLocation = getPreferredLocation();
		if ( preferredLocation.equals(CURRENT_LOCATION) ) {
			// return latitude based on current location
			locationDetails = locationManager.getLastKnownLocation( bestProvider );
			if ( locationDetails != null ) {
                return locationDetails.getLatitude();
            } else {
            	setPreferredLocation("Panama City");
            	toast = Toast.makeText( context, "error: Failed to find current location. Defaulting to Panama City.", Toast.LENGTH_SHORT );
	            toast.setGravity( Gravity.CENTER_HORIZONTAL, 0, 0 );
	            toast.show();
	            return getLatitude();
            }
		} else {
			// return latitude based on hard-coded location 
			for ( int i = 0; i < locations.size(); i++ ) {
				PreferredLocationData location = locations.get(i);
				if( preferredLocation.equals(location.name) ) {
					return location.getLatitude();
				}
			}
		}
		return 0;
	}

	// Gets longitude of preferred or current location.
	public double getLongitude() {
		String preferredLocation = getPreferredLocation();
		if ( preferredLocation.equals(CURRENT_LOCATION) ) {
			// return longitude based on current location
			locationDetails = locationManager.getLastKnownLocation( bestProvider );
			if ( locationDetails != null ) {
                return locationDetails.getLongitude();
            } 	
		} else {
			// return longitude based on hard-coded location
			for ( int i = 0; i < locations.size(); i++ ) {
				PreferredLocationData location = locations.get(i);
				if( preferredLocation.equals(location.name) ) {
					return location.getLongitude();
				}
			}
		}
		return 0;
	}
	
	// Saves coordinates whenever the map is dragged.
	public void rememberDraggedCoordinates(String coords) {
		if (coords != null) {
			SharedPreferences.Editor editor = sp.edit();
			editor.putString("remember_coords", coords);
			editor.commit();
		}
	}
	
	public String getRememberedCoordinates() {
		return sp.getString("remember_coords", null);
	}
	
	// Nulls remembered coordinates. Performed whenever preferred location is changed.
	public void resetRememberedCoordinates() {
		SharedPreferences.Editor editor = sp.edit();
		editor.putString("remember_coords", null);
		editor.commit();
	}
	
	// Return list of all place names for populating a list view.
	public String[] getAllPlaceNames() {	
		String[] locationNames = new String[locations.size()];
		for ( int i = 0; i < locations.size(); i++ ) {
			PreferredLocationData location = locations.get(i);
			locationNames[i] = location.getName();
		}
		return locationNames;
	}
	
	// Initializes locationManager service.
	private void initLocationManager() {
		
		if (!alreadyStarted) {
			this.alreadyStarted = true;
	        locationManager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
	        if ( locationManager == null ) {
	            toast = Toast.makeText( context, "error: Failed to use the Location Service.", Toast.LENGTH_SHORT );
	            toast.setGravity( Gravity.CENTER_HORIZONTAL, 0, 0 );
	            toast.show();
	        } else {  
	            this.bestProvider = this.locationManager.getBestProvider( new Criteria(), true ); // find best provider for searching locations
	            if ( this.bestProvider == null ) {
	                Toast toast = Toast.makeText( context, "error: Please enable Location Services.", Toast.LENGTH_SHORT );
	                toast.setGravity( Gravity.CENTER_HORIZONTAL, 0, 0 );
	                toast.show();
	            } else {
	                // Ask for updates every once in a while but we don't actually care when we get them
	                locationManager.requestLocationUpdates( this.bestProvider, 6000, 20, locationListenerGPSProvider);
	            }
	        }
		}
		
	} // initLocationManager
		
	// Listener for locationManager
    LocationListener locationListenerGPSProvider = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {}
        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}    
    };
    
    // Stops locationManager service.
    public void stopLocationManager() {
        if (locationManager != null) { 
        	locationManager.removeUpdates(locationListenerGPSProvider);
         	locationManager = null;
        }	 
        alreadyStarted = false;
    }
   
	// Data structure for preferred places 
	// Stores place name, latitude, and longitude
	public class PreferredLocationData {
		
		String name;
		double latitude;
		double longitude;
		
		PreferredLocationData(String name, double latitude, double longitude) {
			setName(name);
			setLatitude(latitude);
			setLongitude(longitude);
		}
		
		public String getName() { return name; }
		public double getLatitude() { return latitude; }
		public double getLongitude() { return longitude; }
		public void setName( String name ) { this.name = name; }
		public void setLatitude( double latitude ) { this.latitude = latitude; }
		public void setLongitude( double longitude ) { this.longitude = longitude; }
		
	} // PlaceData
	
} // PreferredLocationManager
