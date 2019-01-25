package com.promosys.mapprojectv1_1;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity
                          implements OnMapReadyCallback,
                                    GoogleApiClient.ConnectionCallbacks,
                                    GoogleApiClient.OnConnectionFailedListener,
                                    GoogleMap.OnMarkerDragListener,
                                    GoogleMap.OnMapLongClickListener,
                                    GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public ArrayList<LocationObject> planTravelList;

    //To store longitude and latitude from map
    private double longitude;
    private double latitude;

    //From -> the first coordinate from where we need to calculate the distance
    private double fromLongitude;
    private double fromLatitude;

    private double clickedMarkerLongitude;
    private double clickedMarkerLatitude;

    //To -> the second coordinate to where we need to calculate the distance
    private double toLongitude;
    private double toLatitude;

    //Google ApiClient
    private GoogleApiClient googleApiClient;

    Polyline line;

    private boolean isLineDrawn = false;

    private TextView txtDistance,txtLocationName,btnSetMarker,btnResetCalculation,txtTotalDistance,txtLocationAddress;
    //private RelativeLayout layoutLocationDescription;

    private double currentLocLongitude;
    private double currentLocLatitude;

    private CardView layoutLocationDescription,layoutTotalDistance;
    private Button btnStartJourney;

    private Marker currentMarker;
    private boolean isJourneyStart = false;
    private double distanceTotal = 0;
    private double distanceCurrent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        currentLocLongitude = Double.valueOf(getIntent().getExtras().getString("locLongitude"));
        currentLocLatitude = Double.valueOf(getIntent().getExtras().getString("locLatitude"));

        sharedPreferences = getSharedPreferences("mapLocationProject", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        planTravelList = new ArrayList<LocationObject>();

        txtDistance = (TextView)findViewById(R.id.txt_distance_from_marker);
        txtLocationName = (TextView)findViewById(R.id.txt_location_name);
        txtTotalDistance = (TextView)findViewById(R.id.txt_total_travelled);

        layoutLocationDescription = (CardView)findViewById(R.id.layout_description);
        layoutLocationDescription.setVisibility(View.GONE);

        layoutTotalDistance = (CardView)findViewById(R.id.layout_total_distance);
        layoutTotalDistance.setVisibility(View.GONE);

        txtLocationAddress = (TextView)findViewById(R.id.txt_location_address);

        btnSetMarker = (TextView)findViewById(R.id.btn_set_marker);
        btnSetMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromLatitude = clickedMarkerLatitude;
                fromLongitude = clickedMarkerLongitude;

                String message = "Origin marker has been changed";
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                //currentMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }
        });

        btnResetCalculation = (TextView)findViewById(R.id.btn_reset_calculation);
        btnResetCalculation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnStartJourney = (Button)findViewById(R.id.btn_start_journey);
        btnStartJourney.setText("CALCULATE ROUTE");
        btnStartJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnStartJourney.getText().equals("CALCULATE ROUTE")){
                    btnStartJourney.setText("DONE");
                    isJourneyStart = true;
                    layoutTotalDistance.setVisibility(View.VISIBLE);

                }else {
                    btnStartJourney.setText("CALCULATE ROUTE");
                    isJourneyStart = false;
                    line.remove();
                    layoutTotalDistance.setVisibility(View.GONE);
                    mMap.clear();
                }

            }
        });
        btnStartJourney.setVisibility(View.GONE);
    }

    public String getPlanMyTravelLocation(){
        String jsonGroup;
        if (sharedPreferences.getString("PlanMyTravelLocation","").isEmpty()){
            jsonGroup = "";
        }else {
            jsonGroup = sharedPreferences.getString("PlanMyTravelLocation","");
        }
        return jsonGroup;
    }

    public void getLocationList(){
        String jsonLocation = getPlanMyTravelLocation();
        if (!(jsonLocation.isEmpty())){

            try {
                JSONArray jsonArray = new JSONArray(jsonLocation);
                for (int i = 0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String groupName = jsonObject.getString("locGroup");
                    String name = jsonObject.getString("locName");
                    String latitude = jsonObject.getString("locLatitude");
                    String longitude = jsonObject.getString("locLongitude");

                    LocationObject locationObject = new LocationObject(groupName,name,latitude,longitude,"isClicked");
                    planTravelList.add(locationObject);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            addMarkerToMap();
        }
    }

    private void addMarkerToMap(){
        for (int i = 0;i<planTravelList.size();i++){
            double lat = Double.valueOf(planTravelList.get(i).getLocLatitude());
            double longi = Double.valueOf(planTravelList.get(i).getLocLongitude());

            LatLng location = new LatLng(lat, longi);

            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(planTravelList.get(i).getLocName())
                    //.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_done_trans_43dp)));
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(3.0060357, 101.4559726), 13));

        //Animating the camera
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //getCurrentLocation();
        LatLng MyPosition = new LatLng(currentLocLatitude,currentLocLongitude);

        currentMarker = mMap.addMarker(new MarkerOptions()
                .position(MyPosition)
                .title("My Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                //.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_done_trans_43dp)));

        /*
        LatLng Promosys = new LatLng(3.0060357, 101.4559726);
        LatLng aeon = new LatLng(2.994573, 101.444165);
        LatLng hospTAR = new LatLng(3.019551, 101.440230);
        LatLng tescoKlang = new LatLng(3.005836, 101.441115);
        LatLng cimbBukitTggi = new LatLng(3.007164, 101.435087);

        mMap.addMarker(new MarkerOptions()
            .position(Promosys)
            .title("Promosys Technology")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


        fromLatitude = Promosys.latitude;
        fromLongitude = Promosys.longitude;

        longitude = fromLongitude;
        latitude = fromLatitude;
 */

        fromLatitude = currentLocLatitude;
        fromLongitude = currentLocLongitude;

        getLocationList();
        moveMap();

        /*
        mMap.addMarker(new MarkerOptions()
                .position(hospTAR)
                .title("Hosp. TAR")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        mMap.addMarker(new MarkerOptions()
                .position(aeon)
                .title("Aeon Bukit Tinggi")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        mMap.addMarker(new MarkerOptions()
                .position(tescoKlang)
                .title("Tesco Klang")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        mMap.addMarker(new MarkerOptions()
                .position(cimbBukitTggi)
                .title("CIMB Bukit Tinggi")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
*/

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(Promosys));
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(3.0060357, 101.4559726), 10));
    }

    private String getAddress( LatLng latLng ) {
        // 1
        Geocoder geocoder = new Geocoder( this );
        String addressText = "";
        List<Address> addresses = null;
        Address address = null;
        try {
            // 2
            addresses = geocoder.getFromLocation( latLng.latitude, latLng.longitude, 1 );
            // 3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses.get(0);
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressText += (i == 0)?address.getAddressLine(i):("\n" + address.getAddressLine(i));
                }
            }
        } catch (IOException e ) {
        }
        return addressText;
    }


    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if(isLineDrawn){
            isLineDrawn = false;
            layoutLocationDescription.setVisibility(View.GONE);
            line.remove();
        }else {
            editor.putString("PlanMyTravelLocation","");
            editor.commit();
            finish();
        }
        //super.onBackPressed();
    }

    //Getting current location
    private void getCurrentLocation() {
        mMap.clear();
        //Creating a location object
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            //moving the map to location
            moveMap();
        }
    }

    //Function to move the map
    private void moveMap() {
        /*
        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(latitude, longitude);

        //Adding marker to map
        mMap.addMarker(new MarkerOptions()
                .position(latLng) //setting position
                .draggable(true) //Making the marker draggable
                .title("Current Location")); //Adding a title

        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        */

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    public String makeURL (double sourcelat, double sourcelog, double destlat, double destlog ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString( sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=AIzaSyA1HrEKhxbyTevTZIV2s8xGHYxTaYjKgEg");
        return urlString.toString();
    }

    private void getDirection(){
        //Getting the URL
        String url = makeURL(fromLatitude, fromLongitude, toLatitude, toLongitude);

        //Showing a dialog till we get the route
        final ProgressDialog loading = ProgressDialog.show(this, "Getting Route", "Please wait...", false, false);

        //Creating a string request
        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        //Calling the method drawPath to draw the path
                        drawPath(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                    }
                });

        //Adding the request to request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //The parameter is the server response
    public void drawPath(String  result) {
        //Getting both the coordinates
        LatLng from = new LatLng(fromLatitude,fromLongitude);
        LatLng to = new LatLng(toLatitude,toLongitude);

        //Calculating the distance in meters
        Double distance = SphericalUtil.computeDistanceBetween(from, to);
        Double distanceInKm = (distance*1.609)/1000;
        double roundOff = Math.round(distanceInKm * 100.0) / 100.0;

        txtDistance.setText("Distance from origin: " + roundOff + " KM");
        distanceCurrent = roundOff;

        if (isJourneyStart){
            distanceTotal = distanceTotal + distanceCurrent;
        }else {
            distanceTotal = distanceCurrent;
        }
        double roundOffTotal = Math.round(distanceTotal * 100.0) / 100.0;
        txtTotalDistance.setText("Total Distance Traveled: " + roundOffTotal + " KM");
        //Displaying the distance
        //Toast.makeText(this,String.valueOf(roundOff+" Km"),Toast.LENGTH_SHORT).show();
        Log.i("MapGoogleApi","distance: " +roundOff );

        try {
            //Parsing json
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            line = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(18)
                    .clickable(true)
                    .color(getResources().getColor(R.color.map_line))
                    .geodesic(true)
            );
            isLineDrawn = true;
        }
        catch (JSONException e) {

        }
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.i("MapGoogleApi","name: " + marker.getTitle());
        toLatitude = marker.getPosition().latitude;
        toLongitude = marker.getPosition().longitude;

        //currentMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentMarker = marker;

        if (isJourneyStart){
            fromLatitude = clickedMarkerLatitude;
            fromLongitude = clickedMarkerLongitude;
        }

        clickedMarkerLatitude = toLatitude;
        clickedMarkerLongitude = toLongitude;

        layoutLocationDescription.setVisibility(View.VISIBLE);
        txtLocationName.setText(marker.getTitle());

        txtLocationAddress.setText(getAddress(marker.getPosition()));

        if (isLineDrawn && !isJourneyStart){
            line.remove();
        }
        btnStartJourney.setVisibility(View.VISIBLE);
        getDirection();

        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        //getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.i("MapGoogleApi","onMapLongClick");
        /*
        if (isLineDrawn){
            line.remove();
        }
        toLatitude = latLng.latitude;
        toLongitude = latLng.longitude;
        getDirection();
        */
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        /*
        //Getting the coordinates
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        //Moving the map
        moveMap();
        */
    }


}
