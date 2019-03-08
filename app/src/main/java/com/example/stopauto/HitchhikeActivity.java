package com.example.stopauto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class HitchhikeActivity extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static class Hitchhike {
        public String userUid;
        public String description;
        public String localization;

        public Hitchhike(String uuid, String desc, String localization) {
            this.userUid = uuid;
            this.description = desc;
            this.localization = localization;
        }
    }

    private static final String TAG = "HitchhikeActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;
    private EditText description;
    private Button button_add;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getLocationPermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hitchhike);
        button_add = (Button) findViewById(R.id.button_add);
        description = (EditText) findViewById(R.id.journey_describe);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_add){
                    AddRide();
                }
            }
        });
    }

    private String Localization;
    private String Description;

    public void AddRide(){
        Description = description.getText().toString().trim();
        if (TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Description is Empty", Toast.LENGTH_SHORT).show();
            return;
        }else{
            if(Description.length() < 10) {
                Toast.makeText(this, "Description is too short", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        currentLocation = (Location) task.getResult();
                        Log.e(TAG,"getting location"+ task.getResult().toString());
                        Localization = Location.convert(currentLocation.getLatitude(), Location.FORMAT_DEGREES) + " " + Location.convert(currentLocation.getLongitude(), Location.FORMAT_DEGREES);
                        currentUser = mAuth.getCurrentUser();
                        String hash = Integer.toString(Math.abs((Description + Localization + currentUser.getUid()).hashCode()));
                        final Hitchhike newJourney = new Hitchhike(currentUser.getUid(),Description,Localization);
                        databaseRef.child("journeys").child(hash).setValue(newJourney);
                        databaseRef.child("users").child(currentUser.getUid()).child("current_journey").setValue(hash);

                        startActivity(new Intent(HitchhikeActivity.this, MainActivity.class));

                        Toast.makeText(HitchhikeActivity.this,"Added", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(HitchhikeActivity.this,"Couldn't read location.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (SecurityException e){
            Log.e(TAG,"getDeviceLocation: SecurityException" + e.getMessage());
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    private Boolean mLocationPermissionGranted = false;

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode){
            case 1:{
                if(grantResults.length > 0){
                    for (int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                }
            }
        }
    }
}

