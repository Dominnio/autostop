package com.example.stopauto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Date;

public class TermsActivity extends AppCompatActivity {

    private TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        info = (TextView) findViewById(R.id.info_text);
        String text = "Last updated: 11.03.2019" + "\n\n"
                + "Please read these terms and conditions carefully before use Hitchhike application." + "\n\n" +
                "When writing announcements and reviews about places you should follow the basic rules set out below:" + "\n" +
                "- follow the grammar and spelling rules of language in which you write" + "\n" +
                "- do not use upper case in order to distinguish the announcement" + "\n" +
                "- do not overuse exclamation and other punctuation marks" + "\n" +
                "- do not swear" + "\n" +
                "\nAnnouncements and reviews that do not meet these rules will be removed.";

        info.setText(text);
    }
}
