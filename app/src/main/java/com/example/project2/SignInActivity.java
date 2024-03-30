package com.example.project2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "SignInActivity";
    private EditText editTextUsername, editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(SignInActivity.this, "Sign in successful!", Toast.LENGTH_SHORT).show();
                        // Proceed with storing Spotify token in Firebase
                        String spotifyToken = "Your_Spotify_Token"; // Placeholder for the Spotify token
                        updateSpotifyTokenInFirestore(user.getUid(), spotifyToken);
                    } else {
                        Toast.makeText(SignInActivity.this, "Sign in failed. Please check your username and password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSpotifyTokenInFirestore(String userId, String token) {
        Map<String, Object> user = new HashMap<>();
        user.put("spotifyToken", token);

        // Update the user's document in Firestore
        db.collection("sample")
                .document(userId)
                .update(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Spotify token successfully stored in Firestore"))
                .addOnFailureListener(e -> Log.w(TAG, "Error storing Spotify token in Firestore", e));
    }

    private void getAndUseSpotifyToken() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            DocumentReference docRef = db.collection("users").document(currentUser.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String spotifyToken = document.getString("spotifyToken");
                        useSpotifyToken(spotifyToken); // Implement this method to use the Spotify token
                    } else {
                        Log.d(TAG, "No Spotify token found.");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });
        }
    }
    private void useSpotifyToken(String spotifyToken) {
        // Use the Spotify token to make requests to the Spotify Web API
        // For example, you can use Retrofit or OkHttp to send HTTP requests

        // Create an OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Create a request to get user profile data from Spotify using the token
        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + spotifyToken)
                .build();

        // Create a call object and enqueue it
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to fetch data: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Parse the response body (JSON) to extract user profile data
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String userName = jsonObject.optString("display_name");
                        // Extract other user data as needed

                        // Update UI or perform other actions with the user data
                        runOnUiThread(() -> {
                            // Update UI elements with user data
                            //textViewUserName.setText(userName); //LOOK INTO THIS??
                            // Update other UI elements as needed
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Request failed with code: " + response.code());
                }
            }
        });
    }



}
