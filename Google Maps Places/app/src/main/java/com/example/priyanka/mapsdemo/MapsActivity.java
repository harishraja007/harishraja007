package com.example.priyanka.mapsdemo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,SelectText {
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastlocation;
    static TextView marker_text;
    private Marker currentLocationmMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    int PROXIMITY_RADIUS = 10000;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();

        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        marker_text = (TextView) findViewById(R.id.marker_text);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            bulidGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
        }
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bulidGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }


    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastlocation = location;
        if (currentLocationmMarker != null) {
            currentLocationmMarker.remove();

        }
        Log.d("lat = ", "" + latitude);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationmMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_layout:
                selct_filter_Popup();
                break;
        }
    }

    private void selct_filter_Popup() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MapsActivity.this);
        TextView title = new TextView(this);
        title.setText("Select Item");
        title.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        title.setPadding(10, 20, 10, 20);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        builderSingle.setCustomTitle(title);
        final ArrayAdapter<String> arraylist = new ArrayAdapter<String>(MapsActivity.this, R.layout.simple_textview);
        arraylist.clear();
        arraylist.add("hospital");
        arraylist.add("school");
        arraylist.add("restuarant");


        builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arraylist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strName = arraylist.getItem(which);
                TextView filter_txt = (TextView) findViewById(R.id.filter_txt);
                filter_txt.setText(strName);
                dialog.dismiss();

                if (filter_txt.getText().toString().equalsIgnoreCase("hospital")) {
                    Object dataTransfer[] = new Object[2];
                    GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                    mMap.clear();
                    String hospital = "hospital";
                    String url = getUrl(latitude, longitude, hospital);
                    dataTransfer[0] = mMap;
                    dataTransfer[1] = url;
                    getNearbyPlacesData.execute(dataTransfer);
                    getNearbyPlacesData.setCallBack(MapsActivity.this);
                    Toast.makeText(MapsActivity.this, "Showing Nearby hospital", Toast.LENGTH_SHORT).show();
                }
                else if (filter_txt.getText().toString().equalsIgnoreCase("school")) {
                    Object data[] = new Object[2];
                    GetNearbyPlacesData getNearbyPlacesData1 = new GetNearbyPlacesData();
                    mMap.clear();
                    String school = "school";
                    String url1 = getUrl(latitude, longitude, school);
                    data[0] = mMap;
                    data[1] = url1;
                    getNearbyPlacesData1.execute(data);
                    Toast.makeText(MapsActivity.this, "Showing Nearby school", Toast.LENGTH_SHORT).show();
                }
                else if (filter_txt.getText().toString().equalsIgnoreCase("restuarant")) {
                    Object dataTrans[] = new Object[2];
                    GetNearbyPlacesData getNearbyPlacesData2 = new GetNearbyPlacesData();
                    mMap.clear();
                    String restuarant = "restuarant";
                    String url2 = getUrl(latitude, longitude, restuarant);
                    dataTrans[0] = mMap;
                    dataTrans[1] = url2;
                    getNearbyPlacesData2.execute(dataTrans);
                    Toast.makeText(MapsActivity.this, "Showing Nearby restuarant", Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog dialog = builderSingle.create();
        ListView listView = dialog.getListView();
        listView.setVerticalScrollBarEnabled(false);
        listView.setDivider(new ColorDrawable(Color.parseColor("#E04B2D")));
        listView.setDividerHeight(1);
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + "AIzaSyDr4q0UYKFgo9YBaSL7NxxR1o0KNKYr11o");

        Log.d("MapsActivity", "url = " + googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;

        } else
            return true;
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public void selectedtext(String placeName) {
        marker_text.setText("");
        marker_text.setText(placeName );
    }
}
