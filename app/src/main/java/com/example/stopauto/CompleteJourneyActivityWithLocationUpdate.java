package com.example.stopauto;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CompleteJourneyActivityWithLocationUpdate extends AppCompatActivity {

    public static class Note{
        public String localization;
        public String user;
        public String description;

        public Note(String l,  String u, String d){
            this.localization = l;
            this.user = u;
            this.description = d;
        }
    }

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;
    private EditText description;
    private Button button_add_comment;
    private Button button_update_without_comment;

    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location currentLocation;
    private String Localization;
    private String Localization_updated;
    private String Description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_journey_with_location_update);

        button_add_comment = (Button) findViewById(R.id.button_add_comment);
        button_update_without_comment = (Button) findViewById(R.id.button_update_without_comment);
        description = (EditText) findViewById(R.id.complete_describe);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();

        button_add_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_add_comment) {
                    AddNote();
                }
            }
        });

        button_update_without_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_update_without_comment) {
                    Update();
                }
            }
        });

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = mAuth.getCurrentUser();
                Uuid = currentUser.getUid().toString();
                Id_journey = dataSnapshot.child("users").child(currentUser.getUid()).child("current_journey").getValue().toString();
                if(!Id_journey.equals("null")){
                    Localization = dataSnapshot.child("journeys").child(Id_journey).child("localization").getValue().toString();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("UserListActivity", "Error occured");
                // Do something about the error
            }
        });
        currentUser = mAuth.getCurrentUser();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(CompleteJourneyActivityWithLocationUpdate.this);
        try{
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        currentLocation = (Location) task.getResult();
                        Localization_updated = Location.convert(currentLocation.getLatitude(), Location.FORMAT_DEGREES) + " " + Location.convert(currentLocation.getLongitude(), Location.FORMAT_DEGREES);
                    }
                }
            });
        }catch (SecurityException e){
            Log.e("MainActivity","getDeviceLocation: SecurityException" + e.getMessage());
        }

    }

    private String Uuid;
    private String Id_journey;

    public void AddNote(){
        String Description = description.getText().toString().trim();
        if (TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Description is Empty", Toast.LENGTH_SHORT).show();
            return;
        }else{
            if(Description.length() < 10) {
                Toast.makeText(this, "Description is too short", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String hash = Integer.toString(Math.abs((Description + Id_journey + currentUser.getUid()).hashCode()));
        final Note newNote = new Note(Localization, Uuid, Description);
        databaseRef.child("notes").child(hash).setValue(newNote);
        databaseRef.child("journeys").child(currentUser.getUid()).child("localization").setValue(Localization_updated);
        Toast.makeText(CompleteJourneyActivityWithLocationUpdate.this,"Added and updated", Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(this, MainActivity.class);
        this.startActivity(myIntent);
    }

    public void Update(){
        databaseRef.child("journeys").child(currentUser.getUid()).child("localization").setValue(Localization_updated);
        Toast.makeText(CompleteJourneyActivityWithLocationUpdate.this,"Updated", Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(this, MainActivity.class);
        this.startActivity(myIntent);
    }

}
