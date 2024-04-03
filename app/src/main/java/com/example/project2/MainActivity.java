package com.example.project2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import android.widget.ImageView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.example.project2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import androidx.annotation.NonNull;
import com.google.firebase.analytics.FirebaseAnalytics;

import com.google.firebase.firestore.SetOptions;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.checkerframework.checker.units.qual.C;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "ef598ecfefbc48da953792cd34909460";
    public static final String REDIRECT_URI = "com.example.project2://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    public static final int TOP_ARTIST_REQUEST_CODE = 2;
    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseAnalytics mFirebaseAnalytics;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken, mAccessCode, aAccessToken;
    private Call mCall;

    private TextView tokenTextView, codeTextView, profileTextView;

    private WrappedData user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = new WrappedData();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize the views
        profileTextView = (TextView) findViewById(R.id.response_text_view);

        // Initialize the buttons
        Button tokenBtn = (Button) findViewById(R.id.token_btn);
        Button profileBtn = (Button) findViewById(R.id.profile_btn);

        // Set the click listeners for the buttons
        tokenBtn.setOnClickListener((v) -> {
            Log.d(TAG, "Token Button Clicked");
            getToken(0);
        });

        profileBtn.setOnClickListener((v) -> {
            Log.d(TAG, "Profile Button Clicked");
            onGetUserProfileClicked();
            getToken(2);
            // Call to retrieve top artists
        });


        // Find the Sign In button by its ID
        Button signInButton = findViewById(R.id.buttonSignIn);

        // Set an OnClickListener on the Sign In button
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the SignInActivity
                Intent signInIntent = new Intent(MainActivity.this, UserLogin.class);
                startActivity(signInIntent);
            }
        });




        // Initialize the views

        // Log the initialization completion
        Log.d(TAG, "Activity initialized successfully");
    }


    /**
     * Get token from Spotify
     * This method will open the Spotify login activity and get the token
     * What is token?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getToken(int code) {
        Log.d(TAG, "getToken() called with code: " + code);
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN, code);
        Log.d(TAG, "AuthorizationRequest created: " + request);
        AuthorizationClient.openLoginActivity(MainActivity.this, code, request);
    }

    /**
     * Get code from Spotify
     * This method will open the Spotify login activity and get the code
     * What is code?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getCode() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE, 1);
        AuthorizationClient.openLoginActivity(MainActivity.this, AUTH_CODE_REQUEST_CODE, request);
    }


    /**
     * When the app leaves this activity to momentarily get a token/code, this function
     * fetches the result of that external activity to get the response from Spotify
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        // Check which request code is present (if any)
        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.getAccessToken();
            // updateSpotifyTokenInFirestore(mAccessToken);
            Button profileBtn = (Button) findViewById(R.id.profile_btn);
            profileBtn.performClick();

        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.getCode();
            setTextAsync(mAccessCode, codeTextView);
        } else if (TOP_ARTIST_REQUEST_CODE == requestCode) {
            aAccessToken = response.getAccessToken();
            onGetUserTopArtistsClicked("tracks");
        } else {
            aAccessToken = response.getAccessToken();
            onGetUserTopArtistsClicked("artists");
        }

    }

    /**
     * Puts spotify token into firestore database
     * @param token the token from the user
     */

    private void updateSpotifyTokenInFirestore(String token) {
        EditText editTextDatabase = findViewById(R.id.editTextDatabase);
        String username = editTextDatabase.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(this, "Username is empty, can't store token.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("user", user);
        user.put("spotifyToken", token);

        // Update Firestore collection reference
        db.collection("sample")
                // Update document reference to "sample1"
                .document("sample1")
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Spotify token successfully stored!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error storing Spotify token", e));

        Bundle params = new Bundle();
        params.putString("token_status", "success");
        mFirebaseAnalytics.logEvent("spotify_token_stored", params);

    }

    /**
     * Get user profile
     * This method will get the user profile using the token
     */



    public void onGetUserProfileClicked() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String jsonResponse = response.body().string();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    UserProfile userProfile = gson.fromJson(jsonResponse, UserProfile.class);

                    // Format the user profile data
                    user.setUsername(userProfile.display_name);
                    user.setFollowers(userProfile.followers.total);
                    user.setEmail(userProfile.email);
                    // Parse JSON response into a Java object
                } catch (IOException e) {
                    Log.d("IO", "Failed to read response: " + e);
                    Toast.makeText(MainActivity.this, "Failed to read response, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class UserProfile {
        String display_name;
        Followers followers;
        String email;

        class Followers {
            int total;
        }
    }

    // Define a class to represent user profile data
    public void onGetUserTopArtistsClicked(String type) {
        String url;
        if (type.equals("tracks")) {
            url = "https://api.spotify.com/v1/me/top/tracks";
        } else {
            url = "https://api.spotify.com/v1/me/top/artists";
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + aAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + type);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonResponse = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONArray itemsArray = jsonObject.getJSONArray("items");
                    if (type.equals("tracks")) {
                        for (int i = 0; i < itemsArray.length(); i++) {
                            user.addTrack(itemsArray.getJSONObject(i).getString("name"));
                            user.addTime(itemsArray.getJSONObject(i).getInt("duration_ms"));
                        }
                    } else {
                        Map<String, Integer> genreCounts = new HashMap<>();
                        for (int i = 0; i < itemsArray.length(); i++) {
                            user.addArtist(itemsArray.getJSONObject(i).getString("name"));
                            JSONArray genres = itemsArray.getJSONObject(i).getJSONArray("genres");
                            for (int j = 0; j < genres.length(); j++) {
                                increment(genres.getString(j), genreCounts);
                            }
                        }
                        Log.d("Error", genreCounts.toString());
                        user.setTopGenres(sortedList(genreCounts));
                    }
                    /**
                    int totalDurationMs = 0;

                    String username = user.getUsername(); // This is the username

                    // StringBuilder formattedData = new StringBuilder();

                    // formattedData.append("<h2>Your top tracks!</h2>");

                    Map<String, Integer> artistCounts = new HashMap<>(); // Will be full after for loop
                    Map<String, Integer> genreCounts = new HashMap<>(); // Will be full after for loop

                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject trackObject = itemsArray.getJSONObject(i);
                        int durationMs = trackObject.getInt("duration_ms");

                        totalDurationMs += durationMs;

                        String trackName = trackObject.getString("name");


                        // formattedData.append("<div style=\"border: 1px solid #ccc; padding: 10px; margin-bottom: 10px;\">");
                        // formattedData.append("<p>").append("🎵 ").append(trackName).append("</p>");
                        // formattedData.append("</div>");
                    }

                    List<String> sortedArtists = sortedList(artistCounts); // Sorted by how much they listen to them. sortedArtists.get(0) = Artist they listen to most
                    List<String> sortedGenres = sortedList(genreCounts); // Sorted by how much they listen to that genre. Sorted same way as sorted artists
                    Log.d("Error", "1");
                    int totalDurationMinutes = totalDurationMs / 60000;
*/
                    // formattedData.append("<p>").append("Total listening time: ").append(totalDurationMinutes).append(" minutes").append("</p>");
/**
                    runOnUiThread(() -> {
                        profileTextView.setText(Html.fromHtml(formattedData.toString(), Html.FROM_HTML_MODE_COMPACT));
                        profileTextView.setMovementMethod(LinkMovementMethod.getInstance());
                    }); */
                } catch (IOException | JSONException e) {
                    Log.d("Error", "Failed to read or parse response: " + e);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to process response",
                            Toast.LENGTH_SHORT).show());
                }
            }
        });
        if (type.equals("tracks")) {
            getToken(3);
        } else if (type.equals("artists")){
            while (user.getTopGenres() == null) {
                continue;
            }
            Log.d("Error", user.getUsername());
            Log.d("Error", user.getTopArtists().toString());
            Log.d("Error", user.getTopTracks().toString());
            Log.d("Error", user.getTopGenres().toString());
            displayWrappedData(user);
        }
    }

    private void displayWrappedData(WrappedData user) {
        String username = user.getUsername();
        List<String> topTracks = user.getTopTracks(); // sorted top -> bottom
        List<String> topArtists = user.getTopArtists(); // ^^
        List<String> topGenres = user.getTopGenres(); // ^^
        // PLEASE FORMAT THE DATA AND DISPLAY IT HERE
    }



    private void increment(String key, Map<String, Integer> map) {
        map.putIfAbsent(key, 0);
        map.put(key, map.get(key) + 1);
    }

    private List<String> sortedList(Map<String, Integer> map) {
        List<String> out = new ArrayList<>();
        for (String key : map.keySet()) {
            out.add(key);
            for (int i = out.size() - 1; i > 0 && map.get(out.get(i)) > map.get(out.get(i - 1)); i--) {
                swap(out, i, i - 1);
            }
        }
        return out;
    }

    private void swap(List<String> list, int i, int j) {
        String si = list.get(i);
        list.set(i, list.get(j));
        list.set(j, si);
    }


    /**
     * Creates a UI thread to update a TextView in the background
     * Reduces UI latency and makes the system perform more consistently
     *
     * @param text the text to set
     * @param textView TextView object to update
     */
    private void setTextAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }

    /**
     * Get authentication request
     *
     * @param type the type of the request
     * @return the authentication request
     */
    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type, int code) {
        if (code == AUTH_TOKEN_REQUEST_CODE) {
            return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                    .setShowDialog(true)
                    .setScopes(new String[] { "user-read-email" }) // <--- Change the scope of your requested token here
                    .setCampaign("your-campaign-token")
                    .build();
        } else {
            return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                    .setShowDialog(true)
                    .setScopes(new String[] { "user-top-read" }) // <--- Change the scope of your requested token here
                    .setCampaign("your-campaign-token")
                    .build();
        }
    }

    /**
     * Gets the redirect Uri for Spotify
     *
     * @return redirect Uri object
     */
    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    public void onFailure(Call call, IOException e) {
        Log.d("HTTP", "Failed to fetch data: " + e);
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                Toast.LENGTH_SHORT).show());
    }
    private String formatDuration(int duration) {
        return String.valueOf(duration);
    }
    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

}
