package com.example.stopauto;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class OpinionsActivity extends AppCompatActivity {

    private LinearLayout opinions;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opinions);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();
        currentUser = mAuth.getCurrentUser();
        opinions = (LinearLayout) findViewById(R.id.opinins);

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Bundle bundle = getIntent().getExtras();

                Iterable <DataSnapshot> notes = dataSnapshot.child("notes").getChildren();
                Iterator <DataSnapshot> iter = notes.iterator();
                String location = bundle.get("location").toString();
                String[] loc = location.replace(",",".").split(" ");
                Float la = Float.parseFloat(loc[0]);
                Float lo = Float.parseFloat(loc[1]);

                opinions.removeAllViews();

                while (iter.hasNext()){
                    DataSnapshot note = iter.next();
                    String description = note.child("description").getValue().toString();
                    String user_uid = note.child("user").getValue().toString();
                    String user = dataSnapshot.child("users").child(user_uid).child("first_name").getValue().toString() + " " + dataSnapshot.child("users").child(user_uid).child("second_name").getValue().toString();
                    TextView opinion = new TextView(OpinionsActivity.this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    params.setMargins(10,10,10,10);
                    opinion.setLayoutParams(params);
                    opinion.setPadding(10,10,10,10);
                    opinion.setBackgroundResource(R.drawable.border);
                    opinion.setText(description + "\n\n" + user);
                    opinions.addView(opinion);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("UserListActivity", "Error occured");
                // Do something about the error
            }
        });
    }
}
