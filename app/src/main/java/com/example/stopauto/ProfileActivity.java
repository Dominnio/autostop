package com.example.stopauto;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;

    private TextView info_email;
    private TextView info_full_name;
    private TextView info_sex;
    private TextView info_rate;
    private TextView info_date_of_birth;
    private TextView info_journey;
    private TextView is_journey;
    private Button complete;
    private Button update;
    private Button delete;
    private Button edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();
        currentUser = mAuth.getCurrentUser();
        info_email = (TextView) findViewById(R.id.info_email);
        info_full_name = (TextView) findViewById(R.id.info_full_name);
        info_date_of_birth = (TextView) findViewById(R.id.info_birth_date);
        info_sex = (TextView) findViewById(R.id.info_sex);
        info_journey = (TextView) findViewById(R.id.info_current_journey);
        info_rate = (TextView) findViewById(R.id.info_rate);
        is_journey = (TextView) findViewById(R.id.info_is_current_journey);
        complete = (Button) findViewById(R.id.button_complete);
        update = (Button) findViewById(R.id.button_update);
        edit = (Button) findViewById(R.id.button_edit);
        delete = (Button) findViewById(R.id.button_delete_profile);

        is_journey.setVisibility(View.INVISIBLE);
        info_journey.setVisibility(View.INVISIBLE);
        complete.setVisibility(View.INVISIBLE);
        update.setVisibility(View.INVISIBLE);

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == complete){
                    CompleteJourney();
                }
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == update){
                    UpdateLocation();
                }
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == edit){
                    EditUser();
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == delete){
                    DeleteAccount();
                }
            }
        });

        databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        info_email.setText(dataSnapshot.child("users").child(currentUser.getUid()).child("email").getValue().toString());
                        info_date_of_birth.setText(dataSnapshot.child("users").child(currentUser.getUid()).child("birthDate").getValue().toString());
                        info_sex.setText(dataSnapshot.child("users").child(currentUser.getUid()).child("sex").getValue().toString());
                        info_full_name.setText(dataSnapshot.child("users").child(currentUser.getUid()).child("first_name").getValue().toString() + " " +
                                dataSnapshot.child("users").child(currentUser.getUid()).child("second_name").getValue().toString());
                        if (!dataSnapshot.child("users").child(currentUser.getUid()).child("current_journey").getValue().toString().equals("null")) {
                            String journey_id = dataSnapshot.child("users").child(currentUser.getUid()).child("current_journey").getValue().toString();
                            info_journey.setText(dataSnapshot.child("journeys").child(journey_id).child("description").getValue().toString());
                            is_journey.setVisibility(View.VISIBLE);
                            info_journey.setVisibility(View.VISIBLE);
                            complete.setVisibility(View.VISIBLE);
                            update.setVisibility(View.VISIBLE);
                        }
                        Iterable<DataSnapshot> rates = dataSnapshot.child("users").child(currentUser.getUid()).child("rates").getChildren();
                        Iterator<DataSnapshot> iter_rate = rates.iterator();
                        int num = 0;
                        while (iter_rate.hasNext()) {
                            DataSnapshot rate = iter_rate.next();
                            num += 1;
                        }
                        info_rate.setText(dataSnapshot.child("users").child(currentUser.getUid()).child("rate").getValue().toString() + " (" + num + ")");
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("UserListActivity", "Error occured");
                    // Do something about the error
                }
            });
    }

    public void CompleteJourney(){
        startActivity(new Intent(getApplicationContext(),
                CompleteJourneyActivity.class));
    }

    public void UpdateLocation(){
        startActivity(new Intent(getApplicationContext(),
                CompleteJourneyActivityWithLocationUpdate.class));
    }

    public void EditUser(){
        startActivity(new Intent(getApplicationContext(),
                EditProfileActivity.class));
    }

    public void DeleteAccount(){
        AlertDialog alertDialog= new AlertDialog.Builder(ProfileActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Are you sure you want to delete your account? It can not be undone.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes, delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        currentUser.delete();
                        mAuth.signOut();
                        databaseRef.child("journeys").child(currentUser.getUid()).removeValue();
                        databaseRef.child("users").child(currentUser.getUid()).removeValue();
                        Intent myIntent = new Intent(ProfileActivity.this, LoginActivity.class);
                        ProfileActivity.this.startActivity(myIntent);
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
