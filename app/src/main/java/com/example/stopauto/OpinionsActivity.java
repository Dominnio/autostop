package com.example.stopauto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
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
                    String[] loc_2 = note.child("localization").getValue().toString().replace(",",".").split(" ");
                    Float la_2 = Float.parseFloat(loc_2[0]);
                    Float lo_2 = Float.parseFloat(loc_2[1]);
                    if(distance(la,la_2,lo,lo_2,0,0) < 1.0) {
                        String description = note.child("description").getValue().toString();
                        String user_uid = note.child("user").getValue().toString();
                        String user = "";
                        if (dataSnapshot.child("users").hasChild(user_uid)) {
                            user = dataSnapshot.child("users").child(user_uid).child("first_name").getValue().toString() + " " + dataSnapshot.child("users").child(user_uid).child("second_name").getValue().toString()
                                    + " (" + dataSnapshot.child("users").child(user_uid).child("email").getValue().toString() + ")";
                        }else{
                            user = "(user account deleted)";
                        }
                        TextView opinion = new TextView(OpinionsActivity.this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        params.setMargins(10, 10, 10, 10);
                        opinion.setLayoutParams(params);
                        opinion.setPadding(10, 10, 10, 10);
                        opinion.setBackgroundResource(R.drawable.border);
                        opinion.setText(description + "\n\n" + user);
                        opinions.addView(opinion);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("UserListActivity", "Error occured");
                // Do something about the error
            }
        });

    }
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        double height = el1 - el2;
        distance = Math.pow(distance, 2) + Math.pow(height, 2);
        return Math.sqrt(distance);
    }
}
