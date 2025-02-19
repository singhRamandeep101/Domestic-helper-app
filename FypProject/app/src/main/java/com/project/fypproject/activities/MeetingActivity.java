package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.project.fypproject.R;

import androidx.appcompat.app.AppCompatActivity;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class MeetingActivity extends AppCompatActivity {

    EditText inputbox;
    Button joincreatebutton;
    Button share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        inputbox = findViewById(R.id.inputBox);
        joincreatebutton = findViewById(R.id.joinCreateButton);
        share = findViewById(R.id.ShareCode);

        URL serverurl;

        try {
            serverurl = new URL("https://meet.jit.si");
            JitsiMeetConferenceOptions defaultOption = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverurl)
                    .build();
            JitsiMeet.setDefaultConferenceOptions(defaultOption);

        }catch (MalformedURLException e){
            e.printStackTrace();

        }

        joincreatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text= inputbox.getText().toString();
                if (!text.isEmpty()){

                    JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                            .setRoom(text)
                            .setFeatureFlag("invite.enabled", false)
                            .build();
                    JitsiMeetActivity.launch(MeetingActivity.this,options);

                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "THE MEETING CODE IS: " + inputbox.getText().toString());
                shareIntent.setType("text/plain");
                startActivity(shareIntent);
            }
        });

    }
}