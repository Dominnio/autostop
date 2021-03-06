package com.example.stopauto;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    public static class User {
        public String email;
        public String first_name;
        public String second_name;
        public String sex;
        public String birthDate;
        public String current_journey = "null";
        public String rate = "0";

        public User(String email, String fName, String sName, String sex, String birhtDate) {
            this.birthDate = birhtDate;
            this.sex = sex;
            this.second_name = sName;
            this.first_name = fName;
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
    private Button button_terms;
    private CheckBox check;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseRef = database.getReference();

    @Override
    public void onBackPressed() {

    }

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
        button_terms = (Button)findViewById(R.id.terms);
        check = (CheckBox) findViewById(R.id.terms_of_use);
        mAuth = FirebaseAuth.getInstance();
        year.setAdapter(adapter);

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_register){
                    RegisterUser();
                }
            }
        });

        button_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_terms){
                    ShowTerms();
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

    public void ShowTerms(){
        Intent myIntent = new Intent(this, TermsActivity.class);
        this.startActivity(myIntent);
    }

    public void RegisterUser(){
        final String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();
        final String Fname = fName.getText().toString().trim();
        final String Sname = sName.getText().toString().trim();
        String Sex = sex.getSelectedItem().toString();
        String Day = day.getSelectedItem().toString();
        String Month = month.getSelectedItem().toString();
        String Year = year.getSelectedItem().toString();
        String sDate = Day+"/"+ Month + "/" + Year;

        if (TextUtils.isEmpty(Email)){
            Toast.makeText(this, "Some fileds are empty", Toast.LENGTH_SHORT).show();
            return;
        }
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
        if (!check.isChecked()){
            Toast.makeText(this, "You must accept the terms and conditions", Toast.LENGTH_SHORT).show();
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
                                currentUser = mAuth.getCurrentUser();
                                currentUser.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    FirebaseAuth.getInstance().signOut();
                                                    databaseRef.child("users").child(currentUser.getUid()).setValue(newUser);

                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(Fname +" "+ Sname).build();
                                                    currentUser.updateProfile(profileUpdates);
                                                    currentUser = null;
                                                    AlertDialog alertDialog= new AlertDialog.Builder(RegisterActivity.this).create();
                                                    alertDialog.setTitle("Alert");
                                                    alertDialog.setMessage("Verify your email address.");
                                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();
                                                                    Toast.makeText(RegisterActivity.this, "Registration succeeded.",
                                                                            Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                }
                                                            });
                                                    alertDialog.show();
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

                            }else{
                                Toast.makeText(RegisterActivity.this, "Register failed." + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

    }

}

