package com.example.stopauto;

import android.content.DialogInterface;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class EditProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText password;
    private EditText fName;
    private EditText sName;
    private Spinner sex;
    private Spinner day;
    private Spinner month;
    private Spinner year;

    private Button button_save;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseRef = database.getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ArrayList<String> years = new ArrayList<String>();
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 1900; i <= thisYear; i++) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, years);

        Spinner spinYear = (Spinner)findViewById(R.id.spin_year);
        spinYear.setAdapter(adapter);

        Spinner spinner = (Spinner) findViewById(R.id.spin);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        password =(EditText) findViewById(R.id.signup_password_input);
        fName = (EditText) findViewById(R.id.signup_fname);
        sName = (EditText) findViewById(R.id.signup_sname);
        sex = (Spinner) findViewById(R.id.spin);
        day = (Spinner) findViewById(R.id.spin_day);
        month = (Spinner) findViewById(R.id.spin_month);
        year = (Spinner) findViewById(R.id.spin_year);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        databaseRef.addValueEventListener(new ValueEventListener() {

            private int getIndex(Spinner spin, String s){
                for (int i=0;i<spin.getCount();i++){
                    if (spin.getItemAtPosition(i).toString().equalsIgnoreCase(s)){
                        return i;
                    }
                }
                return 0;
            }

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sex.setSelection(getIndex(sex,dataSnapshot.child("users").child(currentUser.getUid()).child("sex").getValue().toString()));
                String sDate = dataSnapshot.child("users").child(currentUser.getUid()).child("birthDate").getValue().toString();
                String [] split = sDate.split("/");
                day.setSelection(getIndex(day,split[0]));
                month.setSelection(getIndex(month,split[1]));
                year.setSelection(getIndex(year,split[2]));
                fName.setText(dataSnapshot.child("users").child(currentUser.getUid()).child("first_name").getValue().toString());
                sName.setText(dataSnapshot.child("users").child(currentUser.getUid()).child("second_name").getValue().toString());

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("UserListActivity", "Error occured");
                // Do something about the error
            }
        });

        button_save = (Button)findViewById(R.id.button_save);
        mAuth = FirebaseAuth.getInstance();

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_save){
                    RegisterUser();
                }
            }
        });
    }

    public void RegisterUser(){
        String Password = password.getText().toString().trim();
        String Fname = fName.getText().toString().trim();
        String Sname = sName.getText().toString().trim();
        String Sex = sex.getSelectedItem().toString();
        String Day = day.getSelectedItem().toString();
        String Month = month.getSelectedItem().toString();
        String Year = year.getSelectedItem().toString();
        String sDate = Day+"/"+ Month + "/" + Year;

        if (TextUtils.isEmpty(Password)){
            Toast.makeText(this, "Some fileds are empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Fname)){
            Toast.makeText(this, "Some fileds are empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Sname)){
            Toast.makeText(this, "Some fileds are empty", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser = mAuth.getCurrentUser();
        databaseRef.child("users").child(currentUser.getUid()).child("birthDate").setValue(sDate);
        databaseRef.child("users").child(currentUser.getUid()).child("first_name").setValue(Fname);
        databaseRef.child("users").child(currentUser.getUid()).child("second_name").setValue(Sname);
        databaseRef.child("users").child(currentUser.getUid()).child("sex").setValue(Sex);

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(Fname +" "+ Sname).build();
        currentUser.updateProfile(profileUpdates);

        currentUser.updatePassword(Password);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(this, MainActivity.class);
        this.startActivity(myIntent);
    }


}

