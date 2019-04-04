package com.example.stopauto;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {

    }

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
    private Button btnChoose, btnUpload;
    private Uri filePath;
    FirebaseStorage storage;
    StorageReference storageReference;
    private ImageView imageView;

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
        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        imageView = (ImageView) findViewById(R.id.foto);
        storage = FirebaseStorage.getInstance();


        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

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
                StorageReference pathReference = storage.getReference().child(mAuth.getCurrentUser().getUid());
                pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getApplicationContext()).load(uri).into(imageView);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });

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

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                btnUpload.setEnabled(true);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child(mAuth.getCurrentUser().getUid());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            btnUpload.setEnabled(false);
                            StorageReference pathReference = storage.getReference().child(mAuth.getCurrentUser().getUid());
                            pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(getApplicationContext()).load(uri).into(imageView);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                }
                            });
                            Toast.makeText(EditProfileActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProfileActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploadig "+(int)progress+"%");
                        }
                    });
        }
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

