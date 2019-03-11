package com.example.stopauto;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgottenPasswordActivity extends AppCompatActivity {

    private Button forgotten;
    private EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotten_password);

        forgotten = (Button) findViewById(R.id.recover);
        email = (EditText) findViewById(R.id.login_email_input_recover);

        forgotten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == forgotten){
                    Resetpassword();
                }
            }
        });
    }

    public void Resetpassword(){
        String Email = email.getText().toString().trim();
        if (TextUtils.isEmpty(Email)){
            Toast.makeText(this, "Some fileds are empty", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(Email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgottenPasswordActivity.this, "Email sent.", Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            Toast.makeText(ForgottenPasswordActivity.this, "Reset password failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
