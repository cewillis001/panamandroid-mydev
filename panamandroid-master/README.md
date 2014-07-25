#Panamandroid's Town Portal
Re: forks, clones, and branches: http://stackoverflow.com/questions/5009600/difference-between-fork-and-branch-on-github

Things To Do:
* Social Integration- basically a news feed but with local tweets
* Weather
* Chase down bug related to not all resources being released when app closes. 
	* Probably that will involve overriding OnDestroy


Notes from cop4656-townportal (State as of original fork)
==================

COP4656 Town Portal

# Improvements
* Landscape Mode layouts
* Reduced splash to 5 seconds
* Map Activities can figure out where you are and My Location is now the default
* Places now have Ratings and Prices in their Detail page
* Nearby restaurants are found by My Location
* News and Employment listings are fetched and displayed dynamically for easier consumption
* Activities now scalable across devices. No fixed sizes
* Rotation supported (except for news/jobs webview which won't bring you back to scrolled location)
* Map WebView now supports updatable places after dragging/zooming
* App doesn't crash when an AsyncTask is running and the current activity is destroyed
* App is resistant to network connection issues and provides refresh buttons in this scenario 
* Loading text introduced to give users a visual cue
* Code improved everywhere and documented
	* Many pieces of code rewritten and optimized 
	* Fragments now being used in various parts of the App to avoid deprecation 
