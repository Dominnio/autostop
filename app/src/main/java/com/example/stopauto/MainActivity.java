package com.example.stopauto;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;
    private TextView info_email;
    private TextView info_name;
    private NavigationView navigationView;
    private LinearLayout ads_view;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location currentLocation;
    private String Localization;
    private String Description;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();

        super.onCreate(savedInstanceState);

        currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            Intent myIntent = new Intent(this, LoginActivity.class);
            this.startActivity(myIntent);
        }

        setContentView(R.layout.activity_main);

        if(currentUser != null) {

            navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            View headView = navigationView.getHeaderView(0);

            info_email = (TextView) headView.findViewById(R.id.nav_bar_email);
            info_name = (TextView) headView.findViewById(R.id.nav_bar_user_name);

            info_name.setText(currentUser.getDisplayName());
            info_email.setText(currentUser.getEmail());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ads_view = (LinearLayout) findViewById(R.id.ads_view);

        if(currentUser != null) {

            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    currentUser = mAuth.getCurrentUser();
                    if(currentUser != null) {
                    ads_view.removeAllViews();
                    Iterable<DataSnapshot> ads = dataSnapshot.child("journeys").getChildren();
                    Iterator<DataSnapshot> iter = ads.iterator();

                    while (iter.hasNext()) {
                        DataSnapshot ad = iter.next();
                        TextView ad_view = new TextView(MainActivity.this);
                        ad_view.setText(ad.child("description").getValue().toString());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        params.setMargins(10, 10, 10, 10);
                        ad_view.setLayoutParams(params);
                        ad_view.setPadding(10, 10, 10, 10);
                        ad_view.setBackgroundResource(R.drawable.border);
                        ads_view.addView(ad_view);
                    }

                    if (!dataSnapshot.child("users").child(currentUser.getUid()).child("current_journey").getValue().toString().equals("null")) {
                        final String[] localization = dataSnapshot.child("journeys").child(currentUser.getUid()).child("localization").getValue().toString().replace(",", ".").split(" ");

                        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                        try {
                            Task location = mFusedLocationProviderClient.getLastLocation();
                            location.addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        currentLocation = (Location) task.getResult();
                                        Localization = Location.convert(currentLocation.getLatitude(), Location.FORMAT_DEGREES) + " " + Location.convert(currentLocation.getLongitude(), Location.FORMAT_DEGREES);
                                        String lat = Location.convert(currentLocation.getLatitude(), Location.FORMAT_DEGREES).replace(",", ".");
                                        String lng = Location.convert(currentLocation.getLongitude(), Location.FORMAT_DEGREES).replace(",", ".");
                                        double dist = distance(Double.parseDouble(localization[0]), Double.parseDouble(lat), Double.parseDouble(localization[1]), Double.parseDouble(lng), 0, 0);
                                        if (dist > 100.0) {
                                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                            alertDialog.setTitle("Alert");
                                            alertDialog.setMessage("You have changed the location. Complete or update your journey.");
                                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Complete",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            Intent myIntent = new Intent(MainActivity.this, CompleteJourneyActivity.class);
                                                            MainActivity.this.startActivity(myIntent);
                                                        }
                                                    });
                                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Update",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            Intent myIntent = new Intent(MainActivity.this, CompleteJourneyActivityWithLocationUpdate.class);
                                                            MainActivity.this.startActivity(myIntent);
                                                        }
                                                    });
                                            alertDialog.show();
                                        }
                                    }
                                }
                            });
                        } catch (SecurityException e) {
                            Log.e("MainActivity", "getDeviceLocation: SecurityException" + e.getMessage());
                        }

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(this, InfoActivity.class);
            this.startActivity(myIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent myIntent = new Intent(this, ProfileActivity.class);
            this.startActivity(myIntent);
        } else if (id == R.id.nav_add_ride) {
            Intent myIntent = new Intent(this, HitchhikeActivity.class);
            this.startActivity(myIntent);
        } else if (id == R.id.nav_find_place) {
            Intent myIntent = new Intent(this, FindPlaceActivity.class);
            this.startActivity(myIntent);
        } else if (id == R.id.nav_find_hitchihiker) {
            Intent myIntent = new Intent(this, FindHitchhikerActivity.class);
            this.startActivity(myIntent);
        } else if (id == R.id.nav_other_users) {
            Intent myIntent = new Intent(this, OtherUsersSearchActivity.class);
            this.startActivity(myIntent);
        } else if (id == R.id.nav_sign_out) {
            mAuth.signOut();
            Intent myIntent = new Intent(this, LoginActivity.class);
            this.startActivity(myIntent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
