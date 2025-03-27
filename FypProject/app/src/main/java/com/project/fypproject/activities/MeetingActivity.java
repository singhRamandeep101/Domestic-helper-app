package com.project.fypproject.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.modules.core.PermissionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.fypproject.R;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class MeetingActivity extends AppCompatActivity implements JitsiMeetActivityInterface {
    private static final String TAG = "MeetingActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET
    };

    private EditText inputbox;
    private Button joincreatebutton;
    private Button share;
    private FrameLayout meetingContainer;
    private FloatingActionButton questionsButton;
    private FloatingActionButton leaveButton;
    private JitsiMeetView jitsiMeetView;
    private boolean isMeetingActive = false;
    private Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        initializeViews();
        setupJitsiDefaultOptions();
        setupButtonListeners();
    }

    private void initializeViews() {
        inputbox = findViewById(R.id.inputBox);
        joincreatebutton = findViewById(R.id.joinCreateButton);
        share = findViewById(R.id.ShareCode);
        meetingContainer = findViewById(R.id.meetingContainer);
        questionsButton = findViewById(R.id.questionsButton);
        leaveButton = findViewById(R.id.leaveButton);
        questionsButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);
    }

    private void setupJitsiDefaultOptions() {
        try {
            URL serverUrl = new URL("https://meet.guifi.net");
            JitsiMeetConferenceOptions defaultOptions = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverUrl)
                    .setFeatureFlag("welcomepage.enabled", false)
                    .setFeatureFlag("invite.enabled", false)
                    .setFeatureFlag("live-streaming.enabled", false)
                    .setFeatureFlag("pip.enabled", false)
                    .setFeatureFlag("chat.enabled", true)
                    .setFeatureFlag("lobby.enabled", false)
                    .setConfigOverride("startWithAudioMuted", false)
                    .setConfigOverride("startWithVideoMuted", false)
                    .build();
            JitsiMeet.setDefaultConferenceOptions(defaultOptions);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Jitsi server URL error", e);
            showToast("Error setting up meeting server");
        }
    }

    private void setupButtonListeners() {
        joincreatebutton.setOnClickListener(v -> {
            String meetingCode = inputbox.getText().toString().trim();
            if (meetingCode.isEmpty()) {
                showToast("Please enter a meeting code");
                return;
            }

            if (checkPermissions()) {
                joinMeeting(meetingCode);
            } else {
                requestPermissions();
            }
        });

        share.setOnClickListener(v -> {
            String meetingCode = inputbox.getText().toString().trim();
            if (meetingCode.isEmpty()) {
                showToast("No meeting code to share");
                return;
            }
            shareMeetingCode(meetingCode);
        });

        questionsButton.setOnClickListener(v -> showQuestionsDialog());

        leaveButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Leave Meeting")
                    .setMessage("Are you sure you want to leave the meeting?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        cleanupJitsiView();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .setCancelable(true)
                    .show();
        });
    }

    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    private void joinMeeting(String meetingCode) {
        try {
            cleanupJitsiView();

            jitsiMeetView = new JitsiMeetView(this);
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(meetingCode)
                    .setFeatureFlag("welcomepage.enabled", false)
                    .setFeatureFlag("chat.enabled", true)
                    .setFeatureFlag("invite.enabled", false)
                    .setFeatureFlag("lobby.enabled", false)
                    .setConfigOverride("startWithAudioMuted", false)
                    .setConfigOverride("startWithVideoMuted", false)
                    .build();

            jitsiMeetView.join(options);
            meetingContainer.addView(jitsiMeetView);

            // Hide input/buttons UI
            inputbox.setVisibility(View.GONE);
            joincreatebutton.setVisibility(View.GONE);
            share.setVisibility(View.GONE);
            // Show meeting controls
            questionsButton.setVisibility(View.VISIBLE);
            leaveButton.setVisibility(View.VISIBLE);

            isMeetingActive = true;
            Log.d(TAG, "Meeting started with code: " + meetingCode);
            showToast("Joined meeting successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start meeting", e);
            showToast("Failed to start meeting: " + e.getMessage());
            cleanupJitsiView();
        }
    }

    private void shareMeetingCode(String meetingCode) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Join my meeting with code: " + meetingCode +
                            "\n\nhttps://meet.guifi.net/" + meetingCode);
            startActivity(Intent.createChooser(shareIntent, "Share Meeting Code"));
        } catch (Exception e) {
            Log.e(TAG, "Share error", e);
            showToast("Failed to share meeting code");
        }
    }

    private void showQuestionsDialog() {
        List<String> questions = Arrays.asList(
                "What’s your experience with this topic?",
                "How can we improve this process?",
                "Any suggestions for next steps?",
                "What challenges are you facing?"
        );

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Meeting Questions");
        builder.setItems(questions.toArray(new String[0]), null);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void requestPermissions(String[] permissions, int requestCode, PermissionListener listener) {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    public JitsiMeetView getJitsiView() {
        return jitsiMeetView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (areAllPermissionsGranted(grantResults)) {
                String meetingCode = inputbox.getText().toString().trim();
                if (!meetingCode.isEmpty()) {
                    joinMeeting(meetingCode);
                }
            } else {
                showToast("Camera and microphone permissions are required");
            }
        }
    }

    private boolean areAllPermissionsGranted(int[] grantResults) {
        if (grantResults.length < REQUIRED_PERMISSIONS.length) {
            return false;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupJitsiView();
    }

    @Override
    public void onBackPressed() {
        if (isMeetingActive) {
            new AlertDialog.Builder(this)
                    .setTitle("Leave Meeting")
                    .setMessage("Are you sure you want to leave the meeting?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        cleanupJitsiView();
                        super.onBackPressed();
                    })
                    .setNegativeButton("No", null)
                    .setCancelable(true)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void cleanupJitsiView() {
        if (jitsiMeetView != null) {
            try {
                meetingContainer.removeView(jitsiMeetView);
                jitsiMeetView.dispose();
                jitsiMeetView = null;
                questionsButton.setVisibility(View.GONE);
                leaveButton.setVisibility(View.GONE);
                // Restore input/buttons UI
                inputbox.setVisibility(View.VISIBLE);
                joincreatebutton.setVisibility(View.VISIBLE);
                share.setVisibility(View.VISIBLE);
                isMeetingActive = false;
                Log.d(TAG, "Jitsi view cleaned up");
            } catch (Exception e) {
                Log.e(TAG, "Cleanup error", e);
            }
        }
    }

    private void showToast(String message) {
        runOnUiThread(() -> {
            if (currentToast != null) {
                currentToast.cancel();
            }
            currentToast = Toast.makeText(MeetingActivity.this, message, Toast.LENGTH_SHORT);
            currentToast.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL, 0, 100);
            currentToast.show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (currentToast != null) {
                    currentToast.cancel();
                }
            }, 2000);
        });
    }
}