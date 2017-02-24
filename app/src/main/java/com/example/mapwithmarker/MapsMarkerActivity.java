package com.example.mapwithmarker;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MapsMarkerActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{


    String[] resultstr;
    String[] res = new String[100];
    LatLng myposition;



    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

//        googlemap = mapFragment.getMap();
//        try {
//            googlemap.setMyLocationEnabled(true);
//        }catch (SecurityException e){
//            System.out.println("cannot set the location");
//        }
//
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//        Criteria criteria = new Criteria();
//
//        String provider = locationManager.getBestProvider(criteria,true);
//
//        try {
//            location = locationManager.getLastKnownLocation(provider);
//        }catch (SecurityException e){
//            System.out.println("cannot set the provider");
//        }
//
//        if(location!=null){
//            // Getting latitude of the current location
//            latitude = location.getLatitude();
//
//            // Getting longitude of the current location
//            longitude = location.getLongitude();
//
//            // Creating a LatLng object for the current location
//            LatLng latLng = new LatLng(latitude, longitude);
//
//            myposition = new LatLng(latitude, longitude);
//        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

    }


    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    public void onStart(){
        super.onStart();
        FetchPlacesTask fpt = new FetchPlacesTask();
        String lat = String.valueOf(currentLatitude);
        String lng = String.valueOf(currentLongitude);
        String fnl = lat + "," + lng;
        fpt.execute(fnl);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }


    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onConnected(Bundle bundle) {

            if(ActivityCompat.checkSelfPermission(MapsMarkerActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsMarkerActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (location == null) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

                } else {
                    //If everything went fine lets get latitude and longitude
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    myposition = new LatLng(currentLatitude, currentLongitude);
                    Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
                }
            }
    }


    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
            /*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /**
     * If locationChanges change lat and long
     *
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
    }
    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        //LatLng sydney = new LatLng(-33.852, 151.211);
//        ArrayList<LatLng> pla = new ArrayList<LatLng>();
//        for(int i=0;i<res.length;++i){
//            pla.add(new LatLng(Double.parseDouble(res[i].split("%")[0]), Double.parseDouble(res[i].split("%")[1])));
//        }
////        pla.add(new LatLng(-33.852, 151.211));
////        pla.add(new LatLng(-32.765, 150.111));
//            int x = 0;
//        for(LatLng l:pla) {
//            googleMap.addMarker(new MarkerOptions().position(l)
//                    .title(res[x].split("%")[2]));
//            ++x;
//        }
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myposition, 100));

    }


    public class FetchPlacesTask extends AsyncTask<String,Void,String[]>{

        private final String LOG_TAG = FetchPlacesTask.class.getSimpleName();
        private String[] getPlacesFromJson(String placesjson) throws JSONException{
            JSONObject placejson = new JSONObject(placesjson);
            JSONArray placesarray = placejson.getJSONArray("results");

            resultstr = new String[placesarray.length()];

            for(int i=0;i<placesarray.length();++i){
                JSONObject a = placesarray.getJSONObject(i);
                JSONObject p = a.getJSONObject("geometry");
                JSONObject pl = p.getJSONObject("location");

                String lat = pl.getString("lat");
                String lng = pl.getString("lng");
                String name = a.getString("name");

                resultstr[i] = lat + "%" + lng + "%" + name;
            }
            for(String s:resultstr){
                Log.v(LOG_TAG, "Places entry:" + s);
            }
            return resultstr;
        }


        @Override
        protected String[] doInBackground(String... params){
            if(params.length == 0){
                return null;
            }

            HttpURLConnection urlconnection = null;
            BufferedReader reader = null;
            String placesjson = null;

            // url = https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=22.5849923,88.4016579&radius=10000&types=bar&opennow=true&key=AIzaSyCEaymuh8MbynAtKPxhPn-d8Mp5KQzCSiA
            try{
                String api = "AIzaSyCEaymuh8MbynAtKPxhPn-d8Mp5KQzCSiA";
//                Uri.Builder builder = new Uri.Builder();
//                builder.scheme("https")
//                        .authority("maps.googleapis.com")
//                        .appendPath("maps")
//                        .appendPath("api")
//                        .appendPath("place")
//                        .appendPath("nearbysearch")
//                        .appendPath("json")
//                        .appendQueryParameter("location", params[0])
//                        .appendQueryParameter("radius", "10000")
//                        .appendQueryParameter("types", "restaurants")
//                        .appendQueryParameter("opennow", "true")
//                        .appendQueryParameter("key", api);
//
//                String myurl = builder.build().toString();

                String myurl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + params[0] + "&radius=10000&types=bar&opennow=true&key=AIzaSyCEaymuh8MbynAtKPxhPn-d8Mp5KQzCSiA";
                URL url = new URL(myurl);

                urlconnection = (HttpURLConnection) url.openConnection();
                urlconnection.setRequestMethod("GET");
                urlconnection.connect();

                InputStream input = urlconnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if(input == null){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(input));
                String line;

                while((line = reader.readLine())!=null){
                    buffer.append(line+"\n");
                }

                if(buffer.length() == 0){
                    return null;
                }
                placesjson = buffer.toString();

                Log.v(LOG_TAG, "Places JSON String" + placesjson);
            }catch (IOException e){
                return null;
            }finally{
                if(urlconnection!=null){
                    urlconnection.disconnect();
                }
                if(reader!=null){
                    try{
                        reader.close();
                    }catch(final IOException e){
                        Log.e(LOG_TAG,"Error closing stream", e);
                    }
                }
            }

            try{
                return getPlacesFromJson(placesjson);
            }catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(final String[] result){
            if(result!=null){
//                for(int i=0;i<result.length;++i){
//                    res[i] = result[i];
//                }
            }
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    ArrayList<LatLng> pla = new ArrayList<LatLng>();
                    for(int i=0;i<result.length;++i){
                        pla.add(new LatLng(Double.parseDouble(res[i].split("%")[0]), Double.parseDouble(res[i].split("%")[1])));
                    }
//        pla.add(new LatLng(-33.852, 151.211));
//        pla.add(new LatLng(-32.765, 150.111));
                    int x = 0;
                    for(LatLng l:pla) {
                        googleMap.addMarker(new MarkerOptions().position(l)
                                .title(res[x].split("%")[2]));
                        ++x;
                    }
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myposition, 100));

                }
            });
        }
    }
}
