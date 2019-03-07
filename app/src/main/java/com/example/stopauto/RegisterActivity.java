package com.example.stopauto;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    public static class User {
        public String email;
        public String first_Name;
        public String second_Name;
        public String sex;
        public String birhtDate;

        public User(String email, String fName, String sName, String sex, String birhtDate) {
            this.birhtDate = birhtDate;
            this.sex = sex;
            this.second_Name = sName;
            this.first_Name = fName;
            this.email = email;
        }
    }

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText password;
    private EditText email;
    private EditText fName;
    private EditText sName;
    private Spinner sex;
    private Spinner day;
    private Spinner month;
    private Spinner year;

    private Button button_register;
    private Button button_login;
    List<User> users = new LinkedList<>();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseRef = database.getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
        email = (EditText) findViewById(R.id.signup_email_input);
        password =(EditText) findViewById(R.id.signup_password_input);
        fName = (EditText) findViewById(R.id.signup_fname);
        sName = (EditText) findViewById(R.id.signup_sname);
        sex = (Spinner) findViewById(R.id.spin);
        day = (Spinner) findViewById(R.id.spin_day);
        month = (Spinner) findViewById(R.id.spin_month);
        year = (Spinner) findViewById(R.id.spin_year);

        button_register = (Button)findViewById(R.id.button_register);
        button_login = (Button)findViewById(R.id.button_login);
        mAuth = FirebaseAuth.getInstance();

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_register){
                    RegisterUser();
                }
            }
        });
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_login){
                    startActivity(new Intent(getApplicationContext(),
                            LoginActivity.class));
                }
            }
        });

    }

    public void RegisterUser(){
        final String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();
        String Fname = fName.getText().toString().trim();
        String Sname = sName.getText().toString().trim();
        String Sex = sex.getSelectedItem().toString();
        String Day = day.getSelectedItem().toString();
        String Month = month.getSelectedItem().toString();
        String Year = year.getSelectedItem().toString();
        String sDate = Day+"/"+ Month + "/" + Year;

        if (TextUtils.isEmpty(Email)){
            Toast.makeText(this, "A Field is Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Password)){
            Toast.makeText(this, "A Field is Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Fname)){
            Toast.makeText(this, "A Field is Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Sname)){
            Toast.makeText(this, "A Field is Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        final User newUser = new User(Email,Fname,Sname,Sex,sDate);

        mAuth.createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        try {
                            //check if successful
                            if (task.isSuccessful()) {
                                //start Profile Activity here
                                Toast.makeText(RegisterActivity.this, "registration successful",
                                        Toast.LENGTH_SHORT).show();

                                currentUser = mAuth.getCurrentUser();

                                currentUser.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    FirebaseAuth.getInstance().signOut();

                                                    databaseRef.child("users").child(currentUser.getUid()).setValue(newUser);

                                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                    finish();
                                                }
                                                else
                                                {
                                                    overridePendingTransition(0, 0);
                                                    finish();
                                                    overridePendingTransition(0, 0);
                                                    startActivity(getIntent());
                                                }
                                            }
                                        });

                                finish();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            }else{
                                Toast.makeText(RegisterActivity.this, "Couldn't register, try again",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
    }

}

