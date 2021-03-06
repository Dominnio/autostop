package com.example.stopauto;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.Iterator;

public class OtherUser extends AppCompatActivity {

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
    private RatingBar info_rate_bar;
    private String uuid;
    private ImageView imageView;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();
        currentUser = mAuth.getCurrentUser();
        info_email = (TextView) findViewById(R.id.info_email);
        info_full_name = (TextView) findViewById(R.id.info_full_name);
        info_date_of_birth = (TextView) findViewById(R.id.info_birth_date);
        info_sex = (TextView) findViewById(R.id.info_sex);
        info_rate = (TextView) findViewById(R.id.info_rate);
        info_journey = (TextView) findViewById(R.id.info_current_journey);
        is_journey = (TextView) findViewById(R.id.info_is_current_journey);
        info_rate_bar = (RatingBar) findViewById(R.id.ratingBar);
        imageView = (ImageView) findViewById(R.id.foto);
        storage = FirebaseStorage.getInstance();

        is_journey.setVisibility(View.INVISIBLE);
        info_journey.setVisibility(View.INVISIBLE);

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Bundle bundle = getIntent().getExtras();

                uuid = null;

                Iterable <DataSnapshot> users = dataSnapshot.child("users").getChildren();
                Iterator <DataSnapshot> iter = users.iterator();

                while(iter.hasNext()){
                    DataSnapshot user = iter.next();
                    if(user.child("email").getValue().toString().equals(bundle.get("email").toString())){
                        uuid = user.getKey();
                    }
                }

                storage.getReference().child(uuid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getApplicationContext()).load(uri).into(imageView);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        int errorCode = ((StorageException) exception).getErrorCode();
                        if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                            Log.d("OtherUser: ", "File not found");
                        }
                    }
                });


                info_email.setText(dataSnapshot.child("users").child(uuid).child("email").getValue().toString());
                info_date_of_birth.setText(dataSnapshot.child("users").child(uuid).child("birthDate").getValue().toString());
                info_sex.setText(dataSnapshot.child("users").child(uuid).child("sex").getValue().toString());

                info_full_name.setText(dataSnapshot.child("users").child(uuid).child("first_name").getValue().toString() + " " +
                        dataSnapshot.child("users").child(uuid).child("second_name").getValue().toString());
                if(!dataSnapshot.child("users").child(uuid).child("current_journey").getValue().toString().equals("null")){
                    String journey_id = dataSnapshot.child("users").child(uuid).child("current_journey").getValue().toString();
                    info_journey.setText(dataSnapshot.child("journeys").child(journey_id).child("description").getValue().toString());
                    is_journey.setVisibility(View.VISIBLE);
                    info_journey.setVisibility(View.VISIBLE);
                }

                Iterable <DataSnapshot> rates = dataSnapshot.child("users").child(uuid).child("rates").getChildren();
                Iterator <DataSnapshot> iter_rate = rates.iterator();
                Float sum = new Float(0);
                int num = 0;
                while(iter_rate.hasNext()){
                    DataSnapshot rate = iter_rate.next();
                    sum += Float.parseFloat(rate.getValue().toString());
                    if(rate.getKey().toString().equals(currentUser.getUid())){
                        info_rate_bar.setRating(Float.parseFloat(rate.getValue().toString()));
                    }
                    if(Integer.parseInt(rate.getValue().toString()) != 0) {
                        num += 1;
                    }
                }
                Float n = new Float(num);
                if(n != 0){
                    sum /= n;
                }
                databaseRef.child("users").child(uuid).child("rate").setValue(sum);
                info_rate.setText(dataSnapshot.child("users").child(uuid).child("rate").getValue().toString() + " ("+ num +")");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("UserListActivity", "Error occured");
                // Do something about the error
            }
        });

        info_rate_bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                databaseRef.child("users").child(uuid).child("rates").child(currentUser.getUid()).setValue(ratingBar.getRating());
            }
        });

    }

}
