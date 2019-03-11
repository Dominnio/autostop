package com.example.stopauto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    private TextView info;
    private Button button_terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        info = (TextView) findViewById(R.id.info_text);
        button_terms = (Button)findViewById(R.id.terms);
        String text = "Questions, suggestions, contact with the developer : wpampw@gmail.com." + "\n\n" +
                "Hitchhike app makes it easier to travel by hitchhiking." + "\n\n"
                +"1) Adding announcement"+ "\n"
                + "Go to the 'Hitchhike' tab. There you can add an announcement that will be visible to other users. You do not need to describe your location, it will be shared with others automatically. You can write the advertisement in any language. It's best to add several language versions, e.g. English, the language of the country you are in and your native language. After adding the announcement it will be visible in the 'My profile' tab. If you change the place you should either add new announcement or update the location. Remeber that only one announcement can be active." +"\n\n"
                +"2) Sharing opinion about places"+ "\n"
                + "After the journey or changing the location, do not forget to share your opinion about the place from which you were taken. To do this, click 'Complete' or 'Update location' in the 'My profile' tab. Describe: if you recommend this place, in which direction you traveled, how you caught the driver, how long have you been waiting, etc. In the future, it will help other users in choosing the place to hitchhike." +"\n\n"
                +"3) Rating other users"+ "\n"
                + "You can rate other users. If you are a driver and picked up a hitchhiker who was nice, do not forget to give him a good mark. It will help him hitchhiking in the future." +"\n\n"
                +"4) Finding hitchhikers"+ "\n"
                + "As a driver, you can help others. In the 'Search for a hitchhiker' tab, you can search for hitchhikers in your area. Read the description and decide if you can help someone who travels in the same direction." +"\n\n"
                +"5) Searching best place to hitchhike"+"\n"
                + "If you're planning a trip, in the 'Find a place' tab, you can find the best place to stop in your area or on the route you want to go. Read the reviews and decide if this is the right place for you.\n"
                ;

        info.setText(text);

        button_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_terms){
                    ShowTerms();
                }
            }
        });
    }

    public void ShowTerms(){
        Intent myIntent = new Intent(this, TermsActivity.class);
        this.startActivity(myIntent);
    }
}
