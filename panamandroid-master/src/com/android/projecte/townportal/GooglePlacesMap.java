/* GooglePlacesMap.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressLint ( "SetJavaScriptEnabled")
/*
 * Google Places Map Activity
 * Description: Used with Map Activity to display map of a user 
 *              selected category and a ListView of relative places.
 * Note: Uses fragments now to avoid deprecation
 *       http://developer.android.com/guide/components/fragments.html
 */
public class GooglePlacesMap extends Fragment {

    // Constants
    final private int defaultRadius = 5997, defaultZoom = 13;
    final private String defaultMapType = "roadmap";
    
    private String type, currentCoords, currentMapType; 
    private int currentRadius, currentZoom, savedFirstVisiblePosition;
    private GooglePlacesSearch gpSearch = null;
    private List<Place> places = new Vector<Place>();
    private ListView placesList = null;
    private ArrayAdapter<Place> adapter = null;
    private WebView mapView;
    private FragmentActivity context;
    private List<ListViewTask> listViewTasks = new Vector<ListViewTask>();
    private List<DetailTask> detailTasks = new Vector<DetailTask>(); 
    private ProgressBar loading;
    private AtomicInteger loadingCounter;
    private PreferredLocationManager plm;
    
    //------------------------
    //  Fragment life-cycle
    //------------------------
    
    public void onCreate (Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	plm = new PreferredLocationManager(getActivity().getApplicationContext());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
    	// Inflate and get views
        View view = inflater.inflate( R.layout.activity_map, container, false );
        this.mapView = (WebView) view.findViewById( R.id.mapview );
        this.placesList = (ListView) view.findViewById( R.id.list );
        this.loading = (ProgressBar) view.findViewById( R.id.loading );
        
        this.mapView.getSettings().setJavaScriptEnabled( true );
        this.mapView.addJavascriptInterface( new WebAppInterface(), "Android" );
        
        // Create custom adapter for places list view
        this.adapter = new ArrayAdapter<Place>( getActivity(), android.R.layout.simple_list_item_1, this.places ) {
            
            @Override
            // Support shading and two text items
            public View getView( int position, View convertView, ViewGroup parent ) {
            	
                // Got some help from http://stackoverflow.com/questions/11722885/what-is-difference-between-android-r-layout-simple-list-item-1-and-android-r-lay
                Place place = (Place) this.getItem( position );
                convertView =  ( (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) )
                        .inflate( android.R.layout.simple_list_item_1, parent, false );
                TextView text1 = (TextView) convertView.findViewById( android.R.id.text1 );
                text1.setText( place.name );

                // Center see more text
                if ( position == 0 && place.name.equals( "Refresh" ) ) {
                    text1.setGravity( Gravity.CENTER );
                    text1.setTextColor( getContext().getResources().getColor( R.color.darkBlue ) );
                }
                return convertView;
            }
        };
        
        // Set adapter and bind list item click event
        this.placesList.setAdapter( this.adapter );
        this.placesList.setOnItemClickListener( new OnItemClickListener() {
            
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
            	Place place = (Place) parent.getItemAtPosition( position );
            	if ( position == 0 && place.name.equals( "Refresh" )  ) {
            		new ListViewTask( savedFirstVisiblePosition ).execute();
                    mapView.loadData( getMapHTML(), "text/html", "UTF-8" );   
            	} else {
            		new DetailTask( place ).execute();
            	}
            }
        });
        
        return view;
        
    } // onCreateView
    
    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated(savedInstanceState );       
        Bundle arguments = this.getArguments();
        this.type = arguments.getString( "type" );
        this.loadingCounter = (AtomicInteger) arguments.getSerializable( "loadingCounter" );
        this.context = this.getActivity();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        this.currentCoords = plm.getGoogleCoordinates();
        updateGPSearchParameters(); 
    }
    
    @Override
    public void onPause() {
        this.savedFirstVisiblePosition = this.placesList.getFirstVisiblePosition();
        super.onPause();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        plm.stopLocationManager();
    }
    
    @Override
    public void onDestroy() {
    	for ( ListViewTask task : this.listViewTasks )
    		task.cancel( true );
    	for ( DetailTask task : this.detailTasks )
    		task.cancel( true );
    	super.onDestroy();
    }
    
    //---------------
    //  Map stuff
    //---------------
    
    private void updateGPSearchParameters() {
    	this.currentRadius = this.defaultRadius;
        this.currentZoom = this.defaultZoom;
        this.currentMapType = this.defaultMapType;
        this.gpSearch = new GooglePlacesSearch( this.type, this.currentCoords, this.defaultRadius );
        new ListViewTask().execute(); 
        this.mapView.stopLoading();
        this.mapView.loadData( getMapHTML(), "text/html", "UTF-8" );
    }
      
    //
    // Web App Interface
    // Description: Links to our Google Maps WebView in order to receive events
    // Help: http://developer.android.com/guide/webapps/webview.html
    //
    private class WebAppInterface {

        /// Perform a new search
        @JavascriptInterface
        public void mapDragged( String coords, int radius ) {
            currentCoords = coords;
            currentRadius = radius;
            gpSearch = new GooglePlacesSearch( type, currentCoords, currentRadius );
            plm.rememberDraggedCoordinates(coords);
            getActivity().runOnUiThread( new Runnable() {
				@Override
				public void run() {
					new ListViewTask().execute();
				}
            });
        }
        
        @JavascriptInterface
        public void zoomChanged( int radius, int zoom ) {
            currentRadius = radius;
            currentZoom = zoom;
            gpSearch = new GooglePlacesSearch( type, currentCoords, currentRadius );
            getActivity().runOnUiThread( new Runnable() {
				@Override
				public void run() {
					new ListViewTask().execute();
				}
            });
        }
        
        @JavascriptInterface
        public void maptypeidChanged( String mapType ) {
            currentMapType = mapType;
        }
    }
    
    //
    // Get Map HTML
    // Description: Gets map HTML for location selected
    //
    private String getMapHTML() {
        // HTML and JavaScript source code retrieved from
        // https://developers.google.com/maps/documentation/javascript/examples/place-search
        // Got help from http://stackoverflow.com/questions/10482854/google-map-radius
        String HTMLdata = 
                "<html> <head> <title>Place searches</title> <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\">" + 
                        "<meta charset=\"utf-8\"> <link href=\"https://developers.google.com/maps/documentation/javascript/examples/default.css\" rel=\"stylesheet\">" + 
                        "<script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=true&libraries=places,geometry\"></script>" + 
                        "<script>" + 
                        		"function idealRadius( northEast, southWest ) { " + 
                        			"var north = new google.maps.LatLng( northEast.lat(), 0 ); " + 
                        			"var south = new google.maps.LatLng( southWest.lat(), 0 ); " + 
                        			"var east = new google.maps.LatLng( 0, northEast.lng() ); " + 
                        			"var west = new google.maps.LatLng( 0, southWest.lng() ); " + 
                        			"return Math.min( google.maps.geometry.spherical.computeDistanceBetween( north, south )/2, google.maps.geometry.spherical.computeDistanceBetween( east, west )/2 ) } " +
                                "google.maps.visualRefresh = true; var map; var markers = []; " + 
                                "function clearMap(){ for ( var i = 0; i < markers.length; i++ ) markers[i].setMap( null ); markers = []; } var infowindow; " + 
                                "function initialize() { " + 
                                "       var loc = new google.maps.LatLng(" + this.currentCoords + "); map = new google.maps.Map(document.getElementById('map-canvas'), " + 
                                        "{ mapTypeId: \"" + this.currentMapType + "\", center: loc, zoom: " + this.currentZoom + "  }); google.maps.event.addListener(map, 'dragend', " + 
                                        "function () { " + 
                                                "var bounds = map.getBounds(); var center = bounds.getCenter(); var northEast = bounds.getNorthEast(); var southWest = bounds.getSouthWest(); " + 
                                                "var service = new google.maps.places.PlacesService(map); var r = idealRadius( northEast, southWest ); " + 
                                                "service.nearbySearch( { location: center, radius: r, " + 
                                                "types: ['" + type + "']   } , callback);  Android.mapDragged( center.toUrlValue(), r ); } ); google.maps.event.addListener(map, 'zoom_changed', " + 
                                        "function () { " + 
                                                "var bounds = map.getBounds(); var center = bounds.getCenter(); var northEast = bounds.getNorthEast(); var southWest = bounds.getSouthWest(); " + 
                                                "var service = new google.maps.places.PlacesService(map); var r = idealRadius( northEast, southWest ); " + 
                                                "service.nearbySearch( { location: center, radius: r, " + 
                                                "types: ['" + type + "']   } , callback);  Android.zoomChanged( r, map.getZoom() ); } ); " + 
                                        "google.maps.event.addListener(map, 'maptypeid_changed', function() { Android.maptypeidChanged( map.getMapTypeId() ); } ); var request = { location: loc, " + 
                                        "radius: " + this.currentRadius + ", types: ['" + type + "']  };  infowindow = new google.maps.InfoWindow(); " + 
                                        "var service = new google.maps.places.PlacesService(map);  service.nearbySearch(request, callback); " + 
                                "} " + 
                                "function callback(results, status) { " + 
                                        "clearMap(); if (status == google.maps.places.PlacesServiceStatus.OK) { " + 
                                                "for (var i = 0; i < results.length; i++) { createMarker(results[i]); } } } " + 
                                "function createMarker(place) {  " + 
                                        "var placeLoc = place.geometry.location;  var marker = new google.maps.Marker({ map: map, position: place.geometry.location  });  markers.push(marker); " + 
                                        "google.maps.event.addListener(marker, 'click', function() { infowindow.setContent(place.name); infowindow.open(map, this); });} " + 
                                        "google.maps.event.addDomListener(window, 'load', initialize); " + 
                        "</script>  </head>  <body>    <div id=\"map-canvas\" style=\"width: 100%;height: 100%; float:center\"></div>  </body></html>"; 
        return HTMLdata;
    }

    //
    // ListView Task
    // Description: AsyncTask to get Places.
    //
    private class ListViewTask extends AsyncTask<Void, Void, ArrayList<Place>> {

        private int firstVisiblePosition;
        
        public ListViewTask() {
            this.firstVisiblePosition = 0;
        }
        
        public ListViewTask( int firstVisiblePosition ) {
            this.firstVisiblePosition = firstVisiblePosition;
        }
        
        @Override
        protected void onPreExecute() {
        	listViewTasks.add( this );
        	loadingCounter.addAndGet( 1 );
        	if ( loadingCounter.get() == 1 ) {
        		placesList.setVisibility(View.GONE);
        		loading.setVisibility(View.VISIBLE);
        	}		
        }
        
        @Override
        protected ArrayList<Place> doInBackground( Void... unused ) {
            return gpSearch.findPlaces();
        }

        @Override
        protected void onPostExecute( ArrayList<Place> result ) {
        	
        	if (isCancelled()) return;
        	loadingCounter.addAndGet( -1 );
        	
        	if ( loadingCounter.get() == 0 ) {
        		placesList.setVisibility(View.VISIBLE);
        		loading.setVisibility(View.GONE);
        	}
        	
        	if ( result != null ) {
        		
        		adapter.clear();
        		for ( int i = 0 ; i < result.size(); ++i )
                    adapter.add( result.get( i ) );
                adapter.notifyDataSetChanged();
                placesList.setSelection( this.firstVisiblePosition );
        	
        	} else {
        		
        		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        		builder.setMessage( "Failed to find places. Check your internet connection" ).setTitle( "Error" )
        			.setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {}
    			});
        		builder.create().show();
        		
        		result = new ArrayList<Place>();
        		Place place = new Place();
        		place.name = "Refresh";
        		result.add( place );
        		adapter.clear();
        		
        		for ( int i = 0 ; i < result.size(); ++i )
                    adapter.add( result.get( i ) );
                
                adapter.notifyDataSetChanged();
                placesList.setSelection( this.firstVisiblePosition );
        	}
        }
        
    } // ListViewTask

    //
    // Detail Task
    // Description: AsyncTask to get Google Places Detail.
    //
    private class DetailTask extends AsyncTask<Void, Void, PlaceDetail> {

        private Place place;

        public DetailTask( Place place ) {
            this.place = place;
        }

        @Override
        protected void onPreExecute() {
        	detailTasks.add( this );
        	loadingCounter.addAndGet( 1 );
        }
        
        @Override
        protected PlaceDetail doInBackground( Void... unused ) {
            return gpSearch.findPlaceDetail( this.place.placeReference );
        }

        @Override
        protected void onPostExecute( PlaceDetail placeDetail ) {
        	
        	if (isCancelled()) return;
        	loadingCounter.addAndGet( -1 );
        
        	if ( placeDetail != null ) {
        		
        		// Load placeDetail into its activity
                Intent placeDetailIntent = new Intent( context, PlaceDetailActivity.class );
                placeDetailIntent.putExtra( "name", placeDetail.siteName );
                placeDetailIntent.putExtra( "rating", place.rating );
                placeDetailIntent.putExtra( "price", place.price );
                placeDetailIntent.putExtra( "address", placeDetail.address );
                placeDetailIntent.putExtra( "phonenumber", placeDetail.phoneNumber );
                placeDetailIntent.putExtra( "website", placeDetail.website );
                placeDetailIntent.putExtra( "photoRef", placeDetail.photoRef );
                placeDetailIntent.putExtra( "gpSearchType", type );
                placeDetailIntent.putExtra( "gpSearchGeoLocation", currentCoords );
                placeDetailIntent.putExtra( "loadingCounter", loadingCounter );
                startActivity( placeDetailIntent );
                
        	} else {
        		
        		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        		builder.setMessage( "Failed to get details. Check your internet connection" ).setTitle( "Error" )
        			.setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {}
    			});
        		builder.create().show();
        	}     
        }
        
    } // DetailTask
    
} // GooglePlacesMap
