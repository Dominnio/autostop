package com.example.stopauto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText password;
    private EditText email;
    private Button button_register;
    private Button button_login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.login_email_input);
        password =(EditText) findViewById(R.id.login_password_input);
        button_register = (Button)findViewById(R.id.register);
        button_login = (Button)findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_register){
                    startActivity(new Intent(getApplicationContext(),
                            RegisterActivity.class));
                }
            }
        });
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_login){
                    LoginUser();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {

    }

    public void LoginUser(){

        String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();
        if (TextUtils.isEmpty(Email)){
            Toast.makeText(this, "Some fileds are empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Password)){
            Toast.makeText(this, "Some fileds are empty", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            currentUser = mAuth.getCurrentUser();
                            if(!currentUser.isEmailVerified()){
                                Toast.makeText(LoginActivity.this, "Please, verify your email address.", Toast.LENGTH_SHORT).show();
                            }else {
                                finish();
                                startActivity(new Intent(getApplicationContext(),
                                        MainActivity.class));
                            }
                        }else {
                            Toast.makeText(LoginActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}

