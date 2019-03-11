package com.example.stopauto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CompleteJourneyActivity extends AppCompatActivity {

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
    private Button button_add;
    private Button button_delete;
    private String Uuid;
    private String Id_journey;
    private String Localization;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_journey);

        button_add = (Button) findViewById(R.id.button_add_comment);
        button_delete = (Button) findViewById(R.id.button_delete);
        description = (EditText) findViewById(R.id.complete_describe);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_add) {
                    AddNote();
                }
            }
        });

        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_delete) {
                    Delete();
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
    }

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
        currentUser = mAuth.getCurrentUser();
        String hash = Integer.toString(Math.abs((Description + Id_journey + currentUser.getUid()).hashCode()));
        final Note newNote = new Note(Localization, Uuid, Description);
        databaseRef.child("notes").child(hash).setValue(newNote);
        databaseRef.child("users").child(currentUser.getUid()).child("current_journey").setValue("null");
        databaseRef.child("journeys").child(Id_journey).removeValue();
        Toast.makeText(CompleteJourneyActivity.this,"Added", Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(this, MainActivity.class);
        this.startActivity(myIntent);
    }

    public void Delete(){
        currentUser = mAuth.getCurrentUser();
        databaseRef.child("users").child(currentUser.getUid()).child("current_journey").setValue("null");
        databaseRef.child("journeys").child(Id_journey).removeValue();
        Toast.makeText(CompleteJourneyActivity.this,"Deleted", Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(this, MainActivity.class);
        this.startActivity(myIntent);
    }

}
