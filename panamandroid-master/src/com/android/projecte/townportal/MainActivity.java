/* MainActivity.java
 * Project E - Eric Daniels
 */

package com.android.projecte.townportal;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/*
 * Main Activity
 * Description: Main area for user to select different activities in this
 *              application.
 */
public class MainActivity extends Activity {
	
	// Constants
	private final String CURRENT_LOCATION_TITLE = "Around Me";

    // Used for constructing types of places to views
    private Vector<PlaceType> vFood   = new Vector<PlaceType>();
    private Vector<PlaceType> vEnt    = new Vector<PlaceType>();
    private Vector<PlaceType> vShop   = new Vector<PlaceType>();
    private Vector<PlaceType> vSchool = new Vector<PlaceType>();
    
    private String foodTitle, entertainmentTitle, shoppingTitle, schoolsTitle;
    private PreferredLocationManager plm;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        plm = new PreferredLocationManager(getApplicationContext());
        
        // Set title
        if (plm.getPreferredLocation().equals("Current Location"))
    		setTitle(CURRENT_LOCATION_TITLE);
    	else
    		setTitle(plm.getPreferredLocation());
        
        // Get titles
        this.foodTitle = getString( R.string.food_text );
        this.entertainmentTitle = getString( R.string.entertainment_text );
        this.shoppingTitle = getString( R.string.shopping_text );
        this.schoolsTitle = getString( R.string.schools_text );

        // Setup food
        this.vFood.add( new PlaceType( "cafe", "Cafes" ) );
        this.vFood.add( new PlaceType( "restaurant", "Restaurants" ) );
        this.vFood.add( new PlaceType( "grocery_or_supermarket", "Markets" ) );

        // Setup Entertainment
        this.vEnt.add( new PlaceType( "movie_theater", "Movies" ) );
        this.vEnt.add( new PlaceType( "night_club", "Night Clubs" ) );
        this.vEnt.add( new PlaceType( "museum", "Museums" ) );

        // Setup Shopping
        this.vShop.add( new PlaceType( "shopping_mall", "Malls" ) );
        this.vShop.add( new PlaceType( "book_store", "Books" ) );
        this.vShop.add( new PlaceType( "electronics_store", "Electronics" ) );

        // Setup Schools
        this.vSchool.add( new PlaceType( "school", "Schools" ) );
        this.vSchool.add( new PlaceType( "university", "Universities" ) );
            
    } // onCreate
    
    @Override
    protected void onPause() {
    	super.onPause();
    	plm.stopLocationManager();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Update title
    	if (plm.getPreferredLocation().equals("Current Location")) {
    		setTitle(CURRENT_LOCATION_TITLE);
    	} else {
    		setTitle(plm.getPreferredLocation());
        	plm.stopLocationManager(); // if on
    	}
    	
    } // onResume
   
    @Override
    protected void onDestroy () {
    	super.onStop();
    	plm.stopLocationManager(); // if on	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_change_location:
                openChangeLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    // Open the change location activity
    private void openChangeLocation() {
    	Intent intent = new Intent( this, ChangeLocationActivity.class );
        startActivity( intent );
    }
    
    /*
     * Button On Click Listener
     * Description: Listens for any of the buttons being clicked
     *              and launches their respective activity
     */
    public void onClick( View v ) {
        switch ( v.getId() ) {
        case R.id.btnFood: {
            openPlaceList( this.foodTitle, this.vFood );
            break;
        }
        case R.id.btnEntertainment: {   
            openPlaceList( this.entertainmentTitle, this.vEnt );
            break;
        }
        case R.id.btnShopping: {
            openPlaceList( this.shoppingTitle, this.vShop );
            break; 
        }
        case R.id.btnSchools: {
            openPlaceList( this.schoolsTitle, this.vSchool );
            break; 
        }
        case R.id.btnEmployment: {
            Intent employmentIntent = new Intent( this, EmploymentActivity.class );
            startActivity( employmentIntent );
            break; 
        }
        case R.id.btnNews:{
            Intent newsIntent = new Intent( this, NewsActivity.class );
            startActivity( newsIntent );
            break;
        }   
        case R.id.btnSports:{
            
            Intent sportsIntent = new Intent( this, SportsActivity.class );
            startActivity( sportsIntent );
            
            break;
        }
        case R.id.btnTwitter:{
        	Intent twitterIntent = new Intent( this, TwitterActivity.class );
            startActivity( twitterIntent );
            break;
        }
        default:
            break;
        }  
    }

    /*
     * Open Place List
     * Description: Start a MapActivity based off certain place info.
     */
    private void openPlaceList( String title, Vector<PlaceType> places ) {

        Intent intent = new Intent( this, MapActivity.class );
        intent.putExtra( "title", title );
        
        for ( int i = 0; i < places.size(); i++ )
            intent.putExtra( "PlaceType" + Integer.toString( i + 1 ), places.get( i ) );
        
        startActivity( intent );
    }
    
} // MainActivity
