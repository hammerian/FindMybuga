package com.example.findmybuga;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity  implements OnMapReadyCallback, LocationEngineListener,
        PermissionsListener,MapboxMap.OnMapClickListener {

    private DatabaseReference dbRef;
    private MapView mvBox;
    private MapboxMap map;
    private Button btnNav;
    private Button btnNew;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private PoiPos originPosition;
    private Point destinationPosition;
    private Marker destinationMarker;
    private Location originLocation;
    public ArrayList<PoiPos> listPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btnNav = (Button) findViewById(R.id.btnNav);
        btnNew = (Button) findViewById(R.id.btnNew);
        mvBox = (MapView) findViewById(R.id.mvBox);
        mvBox.onCreate(savedInstanceState);
        mvBox.getMapAsync(this);

        dbRef = FirebaseDatabase.getInstance().getReference().child("Lugares");

        Bundle extras = getIntent().getExtras();
        listPos = (ArrayList<PoiPos>) extras.getSerializable("mapData");

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonShowPopupWindowClick(v);
            }
        });

        btnNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    public void onButtonShowPopupWindowClick(View view) {
        // Open PopupView to create a new Recipe Object
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.pos_form, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // Initiate Popup elements
        EditText edtTxtDesc = (EditText) popupView.findViewById(R.id.edtTxtDesc);
        TextView txtPLat = (TextView) popupView.findViewById(R.id.txtPLat);
        String newLat = ""+originLocation.getLatitude();
        txtPLat.setText(newLat);
        TextView txtPLon = (TextView) popupView.findViewById(R.id.txtPLon);
        String newLon = ""+originLocation.getLongitude();
        txtPLon.setText(newLon);
        Button btnRcp = (Button) popupView.findViewById((R.id.btnRcp));


        // Launch PopupView
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // Dismiss PopupView
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

        // Save recipe button action
        btnRcp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Recollection of Form Fields
                String posDesc = edtTxtDesc.getText().toString().trim();

                if (testForm(posDesc)) {
                    String newLat = ""+destinationPosition.latitude();
                    String newLon = ""+destinationPosition.longitude();
                    PoiPos newPos = new PoiPos(posDesc,newLon, newLat);
                    listPos.add(newPos);
                    popupWindow.dismiss();
                    dbRef.setValue(listPos).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                addPoi(posDesc,destinationPosition.latitude(),destinationPosition.longitude(),listPos.size());
                                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MapActivity.this, "El listado no se ha guardado", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
    }

    private boolean testForm(String posDesc) {
        if (posDesc.isEmpty()) {
            Toast.makeText(MapActivity.this, "Debes escribir una descripción para continuar", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        destinationMarker = map.addMarker(new MarkerOptions().position(point));
        destinationPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());
      //originPosition = Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());
        btnNew.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Longitud: "+point.getLongitude()+" Latitud: "+point.getLatitude(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        map.addOnMapClickListener(this);
        enableLocation();
        if (listPos!=null) {
            int pos = 0;
            for (PoiPos myPoi: listPos) {
                String poiDesc = myPoi.getDescription();
                Double poiLati = Double.parseDouble(myPoi.getLati());
                Double poiLong = Double.parseDouble(myPoi.getLong());

                addPoi(poiDesc,poiLati,poiLong, pos);
                pos=pos+1;
            }
        }
        map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // on marker click we are getting the title of our marker
                // which is clicked and displaying it in a toast message.
                String markerName = marker.getTitle();
                Toast.makeText(MapActivity.this, "El marcador pulsado es " + markerName, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void enableLocation(){
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            initializeLocationEngine();
            initializeLocationLayer();
        }else{
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationEngine(){
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation != null){
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        }else{
            locationEngine.addLocationEngineListener(this);
        }
    }

    private void setCameraPosition(Location location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                location.getLongitude()),13));
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationLayer(){
        locationLayerPlugin = new LocationLayerPlugin(mvBox, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private void addPoi (String desc, Double latitude, Double longitude, int myPos){
        LatLng point = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions().position(point).title(desc).setSnippet(""+myPos));
        map.moveCamera(CameraUpdateFactory.newLatLng(point));
    }
}