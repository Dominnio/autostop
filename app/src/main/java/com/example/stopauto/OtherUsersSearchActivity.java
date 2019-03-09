package com.example.stopauto;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.DataBufferSafeParcelable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.Iterator;

public class OtherUsersSearchActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onClick(View view){
        TextView textView = (TextView) view;
        Toast.makeText(OtherUsersSearchActivity.this,textView.getText(), Toast.LENGTH_SHORT).show();
    }

    private Button search;
    private EditText email;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;
    private LinearLayout users_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_users_search);

        search = (Button) findViewById(R.id.search);
        email = (EditText) findViewById(R.id.user_search);
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();
        users_view = (LinearLayout) findViewById(R.id.users_view);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == search){
                    SearchUser();
                }
            }
        });
    }
    public void SearchUser(){
        databaseRef.addValueEventListener(new ValueEventListener() {
            String Email = email.getText().toString().trim();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> users = dataSnapshot.child("users").getChildren();
                Iterator<DataSnapshot> iter = users.iterator();
                Boolean find = false;
                users_view.removeAllViews();
                while(iter.hasNext()){
                    DataSnapshot user = iter.next();
                    if(user.child("email").getValue().toString().contains(Email)){
                        Log.d("Mam takie cos","cos");
                        TextView user_view = new TextView(OtherUsersSearchActivity.this);
                        user_view.setText(user.child("email").getValue().toString());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        params.setMargins(10,10,10,10);
                        user_view.setLayoutParams(params);
                        user_view.setOnClickListener(OtherUsersSearchActivity.this);
                        user_view.setPadding(10,10,10,10);
                        user_view.setBackgroundResource(R.drawable.border);

                        users_view.addView(user_view);
                        find = true;
                    }
                }
                if(!find){
                    Toast.makeText(OtherUsersSearchActivity.this, "Not found", Toast.LENGTH_SHORT).show();
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
