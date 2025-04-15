package com.project.fypproject.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.R;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MeetingActivity extends AppCompatActivity implements JitsiMeetActivityInterface {
    private static final String TAG = "MeetingActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};
    private static final String SERVER_URL = "https://meet.ffmuc.net";
    private static final String[] LANGUAGES = {"English (en)", "Cantonese (yue)", "Mandarin (zh)", "Tagalog (tl)", "Indonesian (id)", "Thai (th)"};
    private static final String[] LANGUAGE_CODES = {"en", "yue", "zh", "tl", "id", "th"};
    private static final String[] PRESET_NAMES = {"Childcare", "Elderly Care", "Household Skills", "General"};
    private static final String[][] PRESET_QUESTIONS = {
            {"How would you handle a child who won’t sleep?", "What activities do you do with kids?", "How do you help with homework?"},
            {"What do you do if an elderly person refuses to eat?", "Can you assist with medication?", "How do you ensure their safety at home?"},
            {"What’s your cleaning routine for a kitchen?", "Can you cook Cantonese dishes?", "How do you manage laundry?"},
            {"What’s your experience with this topic?", "How can we improve this process?", "Any suggestions for next steps?", "What challenges are you facing?"}
    };

    private EditText inputBox, chatInputBox;
    private Button joinCreateButton, shareButton, sendChatButton, translationHistoryButton, voiceTranslateButton, copyTranslationButton, backButton;
    private FrameLayout meetingContainer;
    private FloatingActionButton questionsButton, translateButton, leaveButton;
    private TextView translationOutput, meetingTimer;
    private LinearLayout chatInputContainer;
    private JitsiMeetView jitsiMeetView;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private BroadcastReceiver conferenceReceiver;
    private boolean isMeetingActive = false;
    private boolean isTranslationEnabled = false;
    private boolean isVoiceTranslating = false;
    private String sourceLanguage = "en";
    private String targetLanguage = "yue";
    private ExecutorService executorService;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, String> translationCache = new ConcurrentHashMap<>();
    private final StringBuilder responseBuilder = new StringBuilder();
    private Vibrator vibrator;
    private long meetingStartTime;
    private Runnable timerRunnable;

    // Firebase variables
    private FirebaseFirestore db;
    private String email;
    private String meetingID;
    private String bookingDocId; // Store the Firestore document ID
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Firebase Auth and get the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Check if user is signed in
        if (user == null) {
            Log.e(TAG, "No user is signed in, redirecting to LoginActivity");
            Toast.makeText(this, "Please sign in to continue.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, Login.class); // Replace with your login activity
            startActivity(intent);
            finish();
            return;
        }

        // Get the email from Firebase Auth
        email = user.getEmail();
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "User email is null or empty, redirecting to LoginActivity");
            Toast.makeText(this, "User email not found. Please sign in again.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
            return;
        }

        // Get bookingID and meetingCode from Intent
        bookingDocId = getIntent().getStringExtra("bookingID"); // Expecting the Firestore document ID
        String meetingCode = getIntent().getStringExtra("meetingCode");

        // Assign to class variables
        this.email = email.toLowerCase(); // Normalize email to lowercase
        this.meetingID = null; // Will be fetched from Firestore

        // Reset state to ensure initial UI is shown
        isMeetingActive = false;
        isTranslationEnabled = false;
        isVoiceTranslating = false;
        sourceLanguage = "en";
        targetLanguage = "yue";

        executorService = Executors.newCachedThreadPool();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLanguage.equals("yue") ? "zh-HK" : sourceLanguage)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        setupSpeechListener();
        initializeViews();
        setupJitsiDefaultOptions();
        setupButtonListeners();
        setupBackPressedHandler();

        // Explicitly ensure initial UI is visible
        Log.d(TAG, "onCreate: Forcing initial UI visibility");
        toggleUiVisibility(true);
        setInitialVisibility();

        // Fetch userType and meetingID from Firestore
        fetchFirestoreData(meetingCode);
    }

    private void fetchFirestoreData(String meetingCode) {
        // Fetch userType from Firestore
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        userType = userDoc.getString("userType");
                        Log.d(TAG, "Retrieved userType: " + userType);

                        // Check if bookingID was provided via Intent
                        if (bookingDocId == null || bookingDocId.isEmpty()) {
                            Log.e(TAG, "bookingID not provided via Intent");
                            showToast("Booking ID not provided.");
                            finish();
                            return;
                        }

                        // Fetch the booking document using the bookingID
                        Log.d(TAG, "Fetching booking document with bookingID: " + bookingDocId);
                        db.collection("booking")
                                .document(bookingDocId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        // Verify the user's email matches one of the participants
                                        String agentEmail = documentSnapshot.getString("agentEmail");
                                        String translatorEmail = documentSnapshot.getString("translatorEmail");
                                        String employeeEmail = documentSnapshot.getString("employeeEmail");
                                        String employerEmail = documentSnapshot.getString("employerEmail");

                                        if (!(email.equalsIgnoreCase(agentEmail) ||
                                                email.equalsIgnoreCase(translatorEmail) ||
                                                email.equalsIgnoreCase(employeeEmail) ||
                                                email.equalsIgnoreCase(employerEmail))) {
                                            Log.e(TAG, "User email does not match any participant in the booking: " + email);
                                            showToast("You are not authorized to join this meeting.");
                                            finish();
                                            return;
                                        }

                                        meetingID = documentSnapshot.getString("meetingID");
                                        String meetingStatus = documentSnapshot.getString("meetingStatus");
                                        Log.d(TAG, "Retrieved meetingID from Firestore: " + meetingID);
                                        Log.d(TAG, "Booking document ID: " + bookingDocId);
                                        Log.d(TAG, "Meeting status: " + meetingStatus);

                                        // Check if the meeting has already ended
                                        if ("Ended Interview".equals(meetingStatus)) {
                                            Log.w(TAG, "Meeting has already ended: " + meetingID);
                                            showToast("This meeting has already ended.");
                                            finish();
                                            return;
                                        }

                                        // Auto-populate inputBox and join meeting if meetingCode is not provided
                                        if (meetingCode == null || meetingCode.isEmpty()) {
                                            if (meetingID != null && !meetingID.isEmpty()) {
                                                Log.d(TAG, "Auto-populating inputBox with meetingID: " + meetingID);
                                                inputBox.setText(meetingID);
                                                handleJoinCreateClick();
                                            } else {
                                                Log.e(TAG, "MeetingID is null or empty in booking document");
                                                showToast("Failed to retrieve meeting ID.");
                                                finish();
                                            }
                                        } else {
                                            inputBox.setText(meetingCode);
                                            handleJoinCreateClick();
                                        }
                                    } else {
                                        Log.e(TAG, "Booking document not found for bookingID: " + bookingDocId);
                                        showToast("Booking not found.");
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to fetch booking document: ", e);
                                    showToast("Failed to retrieve booking information.");
                                    finish();
                                });
                    } else {
                        Log.e(TAG, "User not found in Firebase");
                        showToast("User not found");
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user data", e);
                    showToast("Failed to fetch user data");
                    finish();
                });
    }

    private void initializeViews() {
        inputBox = findViewById(R.id.inputBox);
        joinCreateButton = findViewById(R.id.joinCreateButton);
        shareButton = findViewById(R.id.ShareCode);
        meetingContainer = findViewById(R.id.meetingContainer);
        questionsButton = findViewById(R.id.questionsButton);
        translateButton = findViewById(R.id.translateButton);
        leaveButton = findViewById(R.id.leaveButton);
        translationOutput = findViewById(R.id.translationOutput);
        chatInputContainer = findViewById(R.id.chat_input_container);
        chatInputBox = findViewById(R.id.chatInputBox);
        sendChatButton = findViewById(R.id.sendChatButton);
        translationHistoryButton = findViewById(R.id.translationHistoryButton);
        voiceTranslateButton = findViewById(R.id.voiceTranslateButton);
        copyTranslationButton = findViewById(R.id.copyTranslationButton);
        meetingTimer = findViewById(R.id.meetingTimer);
        backButton = findViewById(R.id.backButton);

        setInitialVisibility();
    }

    private void setInitialVisibility() {
        Log.d(TAG, "setInitialVisibility: Setting initial UI elements to VISIBLE");
        backButton.setVisibility(View.VISIBLE);
        inputBox.setVisibility(View.VISIBLE);
        joinCreateButton.setVisibility(View.VISIBLE);
        shareButton.setVisibility(View.VISIBLE);

        Log.d(TAG, "setInitialVisibility: Setting meeting UI elements to GONE");
        meetingContainer.setVisibility(View.GONE);
        questionsButton.setVisibility(View.GONE);
        translateButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);
        translationOutput.setVisibility(View.GONE);
        chatInputContainer.setVisibility(View.GONE);
        translationHistoryButton.setVisibility(View.GONE);
        voiceTranslateButton.setVisibility(View.GONE);
        copyTranslationButton.setVisibility(View.GONE);
        meetingTimer.setVisibility(View.GONE);
    }

    private void setupJitsiDefaultOptions() {
        try {
            Log.d(TAG, "setupJitsiDefaultOptions: Initializing Jitsi default options");
            JitsiMeetConferenceOptions defaultOptions = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL(SERVER_URL))
                    .setFeatureFlag("welcomepage.enabled", false)
                    .setFeatureFlag("invite.enabled", false)
                    .setFeatureFlag("live-streaming.enabled", false)
                    .setFeatureFlag("pip.enabled", false)
                    .setFeatureFlag("chat.enabled", true)
                    .setConfigOverride("startWithAudioMuted", false)
                    .setConfigOverride("startWithVideoMuted", false)
                    .build();
            JitsiMeet.setDefaultConferenceOptions(defaultOptions);
            Log.d(TAG, "setupJitsiDefaultOptions: Jitsi default options set successfully");
        } catch (Exception e) {
            Log.e(TAG, "setupJitsiDefaultOptions: Failed to set Jitsi default options", e);
            showToast(R.string.error_init_meeting_server);
        }
    }

    private void setupButtonListeners() {
        inputBox.setOnClickListener(v -> copyMeetingCode(inputBox.getText().toString().trim()));
        joinCreateButton.setOnClickListener(v -> handleJoinCreateClick());
        shareButton.setOnClickListener(v -> shareMeetingCode(inputBox.getText().toString().trim()));
        questionsButton.setOnClickListener(v -> showPresetSelectionDialog());
        translateButton.setOnClickListener(v -> toggleTranslation());
        leaveButton.setOnClickListener(v -> confirmLeaveMeeting());
        sendChatButton.setOnClickListener(v -> sendChatMessage());
        translationHistoryButton.setOnClickListener(v -> showTranslationHistory());
        voiceTranslateButton.setOnLongClickListener(v -> {
            translateAndSendMessage("Hello from emulator");
            showToast(R.string.simulated_voice_input);
            return true;
        });
        voiceTranslateButton.setOnClickListener(v -> toggleVoiceTranslation());
        copyTranslationButton.setOnClickListener(v -> copyTranslatedText());
        backButton.setOnClickListener(v -> finish());
    }

    private void setupSpeechListener() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) { showToast(R.string.listening); }
            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onError(int error) {
                int errorMsgId = R.string.voice_error_generic;
                if (error == SpeechRecognizer.ERROR_SERVER) errorMsgId = R.string.voice_error_server;
                else if (error == SpeechRecognizer.ERROR_NO_MATCH) errorMsgId = R.string.voice_error_no_match;
                else if (error == SpeechRecognizer.ERROR_AUDIO) errorMsgId = R.string.voice_error_audio;
                Log.e(TAG, "Voice error " + error);
                showToast(errorMsgId);
                stopVoiceTranslation();
            }
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    Log.d(TAG, "Voice translation using targetLanguage: " + targetLanguage);
                    translateAndSendMessage(matches.get(0));
                }
                stopVoiceTranslation();
            }
            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) updateTranslationUI("[Partial] " + matches.get(0));
            }
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void toggleVoiceTranslation() {
        if (!isTranslationEnabled) {
            showToast(R.string.enable_translation_first);
            return;
        }
        if (!isNetworkAvailable()) {
            showToast(R.string.no_internet);
            return;
        }
        if (!checkPermissions()) {
            requestPermissions();
            return;
        }

        if (!isVoiceTranslating) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.select_source_language)
                    .setItems(LANGUAGES, (dialog, which) -> {
                        sourceLanguage = LANGUAGE_CODES[which];
                        showToast(getString(R.string.source_language_set_to, LANGUAGES[which]));
                        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLanguage.equals("yue") ? "zh-HK" : sourceLanguage);
                        isVoiceTranslating = true;
                        voiceTranslateButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red));
                        voiceTranslateButton.setText(getString(R.string.stop));
                        try {
                            speechRecognizer.startListening(recognizerIntent);
                        } catch (Exception e) {
                            Log.e(TAG, "Speech recognition failed", e);
                            showToast(R.string.speech_recognition_failed);
                            stopVoiceTranslation();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            stopVoiceTranslation();
        }
    }

    private void stopVoiceTranslation() {
        if (isVoiceTranslating) {
            speechRecognizer.stopListening();
            isVoiceTranslating = false;
            voiceTranslateButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
            voiceTranslateButton.setText(getString(R.string.voice));
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = cm.getActiveNetwork();
        return activeNetwork != null;
    }

    private void showPresetSelectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_question_preset)
                .setItems(PRESET_NAMES, (dialog, which) -> showQuestionsDialog(PRESET_QUESTIONS[which], PRESET_NAMES[which] + " Questions"))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showQuestionsDialog(String[] questions, String title) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(questions, null)
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private void handleJoinCreateClick() {
        String meetingCode = inputBox.getText().toString().trim();
        if (meetingCode.isEmpty()) {
            showToast(R.string.enter_meeting_code_prompt);
            return;
        }
        if (checkPermissions()) {
            joinMeeting(meetingCode);
        } else {
            requestPermissions();
        }
    }

    private void toggleTranslation() {
        isTranslationEnabled = !isTranslationEnabled;
        translateButton.setImageResource(isTranslationEnabled ? android.R.drawable.ic_menu_search : android.R.drawable.ic_menu_add);
        showToast(isTranslationEnabled ? R.string.translation_enabled : R.string.translation_disabled);

        int visibility = isTranslationEnabled ? View.VISIBLE : View.GONE;
        setTranslationVisibility(visibility);

        if (isTranslationEnabled) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.select_source_language)
                    .setItems(LANGUAGES, (dialog, which) -> {
                        sourceLanguage = LANGUAGE_CODES[which];
                        showToast(getString(R.string.source_language_set_to, LANGUAGES[which]));
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.select_target_language)
                                .setItems(LANGUAGES, (dialog2, which2) -> {
                                    targetLanguage = LANGUAGE_CODES[which2];
                                    if (sourceLanguage.equals(targetLanguage)) {
                                        showToast(R.string.languages_cannot_be_same);
                                        return;
                                    }
                                    showToast(getString(R.string.translation_set_to, LANGUAGES[which2]));
                                    translateAndSendMessage("Test message");
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            translationOutput.setText("");
            chatInputBox.setText("");
            stopVoiceTranslation();
            sourceLanguage = "en";
            targetLanguage = "yue";
        }

        if (!isMeetingActive) toggleUiVisibility(true);
    }

    private void sendChatMessage() {
        String message = chatInputBox.getText().toString().trim();
        if (message.isEmpty()) {
            showToast(R.string.enter_message);
            return;
        }

        if (isMeetingActive) updateTranslationUI("[You] " + message);
        if (isTranslationEnabled) translateAndSendMessage(message);
        else {
            chatInputBox.setText("");
            if (isMeetingActive) updateTranslationUI(message);
        }
    }

    private void translateAndSendMessage(String message) {
        String cacheKey = message + "|" + sourceLanguage + "|" + targetLanguage;
        String cachedResult = translationCache.get(cacheKey);
        if (cachedResult != null) {
            updateTranslationUI(cachedResult);
            chatInputBox.setText("");
            return;
        }

        executorService.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String urlString = "https://api.mymemory.translated.net/get?q=" + URLEncoder.encode(message, StandardCharsets.UTF_8.name()) +
                        "&langpair=" + sourceLanguage + "|" + targetLanguage;
                conn = (HttpURLConnection) new URL(urlString).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    responseBuilder.setLength(0);
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) responseBuilder.append(line.trim());
                    }
                    JSONObject response = new JSONObject(responseBuilder.toString());
                    JSONObject responseData = response.getJSONObject("responseData");
                    String translatedText = responseData.getString("translatedText");

                    if (translatedText.contains("IS AN INVALID SOURCE LANGUAGE")) {
                        handler.post(() -> showToast(R.string.translation_error));
                        return;
                    }

                    String finalText = "[Translated from " + sourceLanguage + " to " + targetLanguage + "] " + translatedText;
                    translationCache.put(cacheKey, finalText);
                    handler.post(() -> updateTranslationUI(finalText));
                } else {
                    handler.post(() -> showToast(R.string.translation_failed));
                }
            } catch (Exception e) {
                Log.e(TAG, "Translation error", e);
                handler.post(() -> showToast(R.string.translation_error));
            } finally {
                if (conn != null) conn.disconnect();
                handler.post(() -> chatInputBox.setText(""));
            }
        });
    }

    private void copyTranslatedText() {
        String fullText = translationOutput.getText().toString().trim();
        if (fullText.isEmpty()) {
            showToast(R.string.no_text_to_copy);
            return;
        }
        String[] lines = fullText.split("\n");
        String recentText = lines[lines.length - 1].trim();
        String translatedText = recentText.contains("] ") ? recentText.substring(recentText.indexOf("] ") + 2).trim() : recentText;

        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Translated Text", translatedText);
        clipboard.setPrimaryClip(clip);
        showToast(getString(R.string.copied_text, translatedText));
    }

    private void showTranslationHistory() {
        if (translationCache.isEmpty()) {
            showToast(R.string.no_translation_history);
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.translation_history_title)
                .setItems(translationCache.values().toArray(new String[0]), null)
                .setPositiveButton(R.string.close, null)
                .setNeutralButton("Clear", (dialog, which) -> {
                    translationCache.clear();
                    Log.d(TAG, "Translation history cleared");
                    showToast("Translation history cleared");
                    dialog.dismiss();
                })
                .show();
    }

    private void copyMeetingCode(String code) {
        if (!code.isEmpty()) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Meeting Code", code);
            clipboard.setPrimaryClip(clip);
            showToast(R.string.meeting_code_copied);
        }
    }

    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    private void joinMeeting(String meetingCode) {
        if (!isNetworkAvailable()) {
            showToast(R.string.no_internet);
            return;
        }

        if (email == null || email.isEmpty()) {
            Log.e(TAG, "Email is null or empty, cannot join meeting");
            showToast("User email not found. Please sign in again.");
            return;
        }

        if (bookingDocId == null || bookingDocId.isEmpty()) {
            Log.e(TAG, "Booking document ID is null or empty, skipping Firestore updates");
            showToast("Booking document ID not provided. Status update skipped.");
        } else {
            if ("Agent".equals(userType)) {
                Log.d(TAG, "joinMeeting: Updating meeting status to Started Interview in Firestore");
                db.collection("booking")
                        .document(bookingDocId)
                        .update("meetingStatus", "Started interview")
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Meeting status updated to Started Interview"))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to update meeting status to Started Interview", e));
            } else {
                Log.d(TAG, "User is not an Agent, skipping status update");
            }

            Log.d(TAG, "joinMeeting: Storing meeting code in Firestore");
            db.collection("booking")
                    .document(bookingDocId)
                    .update("meetingCode", meetingCode)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Meeting code stored in Firestore: " + meetingCode))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to store meeting code", e));
        }

        executorService.execute(() -> {
            try {
                Log.d(TAG, "joinMeeting: Cleaning up previous Jitsi view if exists");
                if (jitsiMeetView != null) {
                    jitsiMeetView.dispose();
                    meetingContainer.removeView(jitsiMeetView);
                    jitsiMeetView = null;
                }

                Log.d(TAG, "joinMeeting: Creating new JitsiMeetView");
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

                setupConferenceReceiver();

                handler.post(() -> {
                    Log.d(TAG, "joinMeeting: Joining meeting with code: " + meetingCode);
                    jitsiMeetView.join(options);
                    if (jitsiMeetView.getParent() == null) {
                        Log.d(TAG, "joinMeeting: Adding JitsiMeetView to meetingContainer");
                        meetingContainer.addView(jitsiMeetView);
                    } else {
                        Log.w(TAG, "joinMeeting: JitsiMeetView already has a parent");
                    }
                    toggleUiVisibility(false);
                    isMeetingActive = true;
                    leaveButton.setVisibility(View.VISIBLE);
                    startMeetingTimer();
                    if (isTranslationEnabled) setTranslationVisibility(View.VISIBLE);
                    showToast(R.string.joined_meeting_success);
                });
            } catch (Exception e) {
                Log.e(TAG, "joinMeeting: Failed to initialize Jitsi view", e);
                handler.post(() -> {
                    showToast(R.string.meeting_setup_failed);
                    toggleUiVisibility(true);
                    isMeetingActive = false;
                });
            }
        });
    }

    private void startMeetingTimer() {
        meetingStartTime = System.currentTimeMillis();
        meetingTimer.setVisibility(View.VISIBLE);
        timerRunnable = () -> {
            long elapsedMillis = System.currentTimeMillis() - meetingStartTime;
            int seconds = (int) (elapsedMillis / 1000) % 60;
            int minutes = (int) (elapsedMillis / (1000 * 60)) % 60;
            int hours = (int) (elapsedMillis / (1000 * 60 * 60));
            meetingTimer.setText(String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds));
            handler.postDelayed(timerRunnable, 1000);
        };
        handler.post(timerRunnable);
    }

    private void stopMeetingTimer() {
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
            timerRunnable = null;
        }
        meetingTimer.setVisibility(View.GONE);
        meetingTimer.setText(getString(R.string.timer_reset));
    }

    private void setupConferenceReceiver() {
        if (conferenceReceiver != null) {
            try {
                unregisterReceiver(conferenceReceiver);
            } catch (Exception e) {
                Log.w(TAG, "Failed to unregister conference receiver", e);
            }
        }
        conferenceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case "org.jitsi.meet.CONFERENCE_TERMINATED":
                            Log.d(TAG, "Conference terminated via receiver");
                            cleanupJitsiView();
                            break;
                        case "org.jitsi.meet.RECEIVE_CHAT_MESSAGE":
                            String message = intent.getStringExtra("message");
                            String senderId = intent.getStringExtra("senderId");
                            Log.d(TAG, "Chat message received: " + message + " from " + (senderId != null ? senderId : "unknown"));
                            updateTranslationUI(message);
                            break;
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("org.jitsi.meet.CONFERENCE_TERMINATED");
        filter.addAction("org.jitsi.meet.RECEIVE_CHAT_MESSAGE");
        ContextCompat.registerReceiver(this, conferenceReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void updateTranslationUI(String text) {
        String currentText = translationOutput.getText().toString();
        translationOutput.setText(currentText.isEmpty() ? text : currentText + "\n" + text);
        setTranslationVisibility(View.VISIBLE);
    }

    private void setTranslationVisibility(int visibility) {
        translationOutput.setVisibility(visibility);
        chatInputContainer.setVisibility(visibility);
        translationHistoryButton.setVisibility(visibility);
        voiceTranslateButton.setVisibility(visibility);
        copyTranslationButton.setVisibility(visibility);
    }

    private void shareMeetingCode(String meetingCode) {
        if (meetingCode.isEmpty()) {
            showToast(R.string.no_meeting_code_to_share);
            return;
        }
        String shareText = getString(R.string.share_meeting_text, SERVER_URL, meetingCode);
        Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, shareText)
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_meeting_code));
        try {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_meeting_code)));
        } catch (Exception e) {
            Log.e(TAG, "Error sharing meeting code", e);
            showToast(R.string.share_failed);
        }
    }

    private void confirmLeaveMeeting() {
        if (isMeetingActive) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.leave_meeting_title)
                    .setMessage(R.string.leave_meeting_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> cleanupJitsiView())
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    }

    @Override
    public void requestPermissions(String[] permissions, int requestCode, com.facebook.react.modules.core.PermissionListener listener) {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length >= REQUIRED_PERMISSIONS.length &&
                Arrays.stream(grantResults).allMatch(result -> result == PackageManager.PERMISSION_GRANTED)) {
            String meetingCode = inputBox.getText().toString().trim();
            if (!meetingCode.isEmpty()) joinMeeting(meetingCode);
        } else {
            showToast(R.string.permissions_required);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (conferenceReceiver != null) {
            try {
                unregisterReceiver(conferenceReceiver);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Receiver not registered", e);
            }
        }
        speechRecognizer.destroy();
        cleanupJitsiView();
        executorService.shutdown();
        handler.removeCallbacksAndMessages(null);
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isMeetingActive) {
                    new AlertDialog.Builder(MeetingActivity.this)
                            .setTitle(R.string.leave_meeting_title)
                            .setMessage(R.string.leave_meeting_message)
                            .setPositiveButton(R.string.yes, (dialog, which) -> cleanupJitsiView())
                            .setNegativeButton(R.string.no, null)
                            .show();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void cleanupJitsiView() {
        if (jitsiMeetView != null) {
            Log.d(TAG, "cleanupJitsiView: Disposing Jitsi view");
            jitsiMeetView.dispose();
            meetingContainer.removeView(jitsiMeetView);
            jitsiMeetView = null;
            meetingContainer.removeAllViews();
            toggleUiVisibility(true);
            stopMeetingTimer();
            isMeetingActive = false;
            leaveButton.setVisibility(View.GONE);
            if (!isTranslationEnabled) setTranslationVisibility(View.GONE);
            stopVoiceTranslation();
            Log.d(TAG, "cleanupJitsiView: Jitsi view cleaned up successfully");

            if (bookingDocId != null && !bookingDocId.isEmpty()) {
                if ("Agent".equals(userType)) {
                    Log.d(TAG, "cleanupJitsiView: Updating meeting status to Waiting for Employer Response in Firestore");
                    db.collection("booking")
                            .document(bookingDocId)
                            .update("meetingStatus", "Waiting for Employer Response")
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Meeting status updated successfully");

                                Intent intent = new Intent(MeetingActivity.this, BookRecordActivity.class);
                                intent.putExtra("selectedTab", "all");
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update meeting status", e));
                } else {
                    Log.d(TAG, "User is not an Agent, skipping status update");
                    Intent intent = new Intent(MeetingActivity.this, BookRecordActivity.class);
                    intent.putExtra("selectedTab", "all");
                    startActivity(intent);
                    finish();
                }
            } else {
                Log.e(TAG, "Booking document ID is null or empty");
                Intent intent = new Intent(MeetingActivity.this, BookRecordActivity.class);
                intent.putExtra("selectedTab", "ALL");
                startActivity(intent);
                finish();
            }
        }
    }

    private void toggleUiVisibility(boolean showInputUi) {
        Log.d(TAG, "toggleUiVisibility: showInputUi=" + showInputUi);
        int inputVisibility = showInputUi ? View.VISIBLE : View.GONE;
        int meetingVisibility = showInputUi ? View.GONE : View.VISIBLE;

        backButton.setVisibility(inputVisibility);
        inputBox.setVisibility(inputVisibility);
        joinCreateButton.setVisibility(inputVisibility);
        shareButton.setVisibility(inputVisibility);

        meetingContainer.setVisibility(meetingVisibility);
        questionsButton.setVisibility(meetingVisibility);
        translateButton.setVisibility(meetingVisibility);

        Log.d(TAG, "toggleUiVisibility: backButton visibility=" + backButton.getVisibility());
        Log.d(TAG, "toggleUiVisibility: inputBox visibility=" + inputBox.getVisibility());
        Log.d(TAG, "toggleUiVisibility: joinCreateButton visibility=" + joinCreateButton.getVisibility());
        Log.d(TAG, "toggleUiVisibility: shareButton visibility=" + shareButton.getVisibility());
        Log.d(TAG, "toggleUiVisibility: meetingContainer visibility=" + meetingContainer.getVisibility());
    }

    private void showToast(int resId) {
        handler.post(() -> Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show());
    }

    private void showToast(String message) {
        handler.post(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private void showToast(int resId, String arg) {
        handler.post(() -> Toast.makeText(this, getString(resId, arg), Toast.LENGTH_SHORT).show());
    }
}