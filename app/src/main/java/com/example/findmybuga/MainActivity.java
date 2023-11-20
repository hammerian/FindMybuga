package com.example.findmybuga;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationEngineListener,
        PermissionsListener {

    private DatabaseReference dbRef;
    private PermissionsManager permissionsManager;
    private Location originLocation;
    private LocationEngine locationEngine;
    private MyListAdapter listAdapter;
    private Point originPosition;
    private RecyclerView rclPos;
    public ArrayList<PoiPos> listPos;

    private Button btnPos;
    private Button btnHist;

    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();

        if(locationEngine != null){
            locationEngine.requestLocationUpdates();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        rclPos = (RecyclerView) findViewById(R.id.rclPos);
        btnPos = (Button) findViewById(R.id.btnPos);
        btnHist = (Button) findViewById(R.id.btnHist);
        enableLocation();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Lugares");

        rclPos.setLayoutManager(new LinearLayoutManager(this));
        listPos = new ArrayList<PoiPos>();
        listAdapter= new MyListAdapter(listPos,this.getApplicationContext());
        // Y carga el Array de imagenes
        rclPos.setAdapter(listAdapter);
        rclPos.setHasFixedSize(false);


        btnPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originLocation != null) {
                    originPosition = Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());
                    onButtonShowPopupWindowClick(v);
                 // Toast.makeText(MainActivity.this, "Longitud: " + originLocation.getLongitude() + " Latitud: " + originLocation.getLatitude(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "No hemos podido acceder a su ubicación", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listPos.size() > 0) {
                    //Definimos el Intent y creamos el Bundle
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    Bundle b = new Bundle();
                    //agregamos la coordenada.
                    intent.putExtra("mapData", listPos);
                    intent.putExtras(b);
                    //begin activity
                    startActivity(intent);
                }else {
                    Toast.makeText(MainActivity.this, "Tienes que añadir varias localizaciones para ver el historial", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ValueEventListener taskitaListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<PoiPos> newListData = new ArrayList<PoiPos>();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    PoiPos tsk = (PoiPos) ds.getValue(PoiPos.class);
                    newListData.add(tsk);
                }
                listPos = newListData;
                updateList(listPos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Don't ignore errors!
            }
        };
        dbRef.addListenerForSingleValueEvent(taskitaListener);

    }

    private void updateList (ArrayList<PoiPos> myNewList) {
        listAdapter = new MyListAdapter(myNewList,this.getApplicationContext());
        rclPos.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            originLocation = location;
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private void enableLocation(){
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            initializeLocationEngine();
            initializeLocationLayer();
        }else{
            permissionsManager = new PermissionsManager((PermissionsListener) this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationEngine(){
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation != null){
            originLocation = lastLocation;
        }else{
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationLayer(){

    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "Toast Informativo", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted) {
            enableLocation();
        }
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
                    String newLat = ""+originLocation.getLatitude();
                    String newLon = ""+originLocation.getLongitude();
                    PoiPos newPos = new PoiPos(posDesc,newLon, newLat);
                    listPos.add(newPos);
                    popupWindow.dismiss();
                    dbRef.setValue(listPos).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(MainActivity.this, "El listado no se ha guardado", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
    }

    private boolean testForm(String posDesc) {
        if (posDesc.isEmpty()) {
            Toast.makeText(MainActivity.this, "Debes escribir una descripción para continuar", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}