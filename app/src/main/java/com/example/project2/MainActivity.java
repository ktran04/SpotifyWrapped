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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import androidx.annotation.NonNull;
import com.google.firebase.analytics.FirebaseAnalytics;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
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
            getToken(0);
        });

        profileBtn.setOnClickListener((v) -> {
            onGetUserProfileClicked();
            getToken(2);
            // Call to retrieve top artists
        });

        // Initialize the views
        tokenTextView = (TextView) findViewById(R.id.token_text_view);

    }


    /**
     * Get token from Spotify
     * This method will open the Spotify login activity and get the token
     * What is token?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getToken(int code) {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN, code);
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
            onGetUserTopArtistsClicked("artists");
            onGetUserTopArtistsClicked("tracks");
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
                    String jsonResponse = response.body().string();
                    Log.d(TAG, "JSON Response: " + jsonResponse);

                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONArray itemsArray = jsonObject.getJSONArray("items");

                    StringBuilder formattedData = new StringBuilder();

                    // Add header for top tracks
                    formattedData.append("<h2>Your top tracks!</h2>");

                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject trackObject = itemsArray.getJSONObject(i);
                        JSONObject albumObject = trackObject.getJSONObject("album");
                        JSONArray imagesArray = albumObject.getJSONArray("images");
                        String imageUrl = "";
                        if (imagesArray.length() > 0) {
                            JSONObject imageObject = imagesArray.getJSONObject(0);
                            imageUrl = imageObject.getString("url");
                            Log.d(TAG, "Image URL for track " + i + ": " + imageUrl);
                        }

                        String artistName = "";
                        JSONArray artistsArray = trackObject.getJSONArray("artists");
                        if (artistsArray.length() > 0) {
                            JSONObject artistObject = artistsArray.getJSONObject(0);
                            artistName = artistObject.getString("name");
                        }

                        String trackName = trackObject.getString("name");

                        // Load image using Glide
                        ImageView imageView = new ImageView(MainActivity.this);
                        Glide.with(MainActivity.this)
                                .load(imageUrl)
                                .into(imageView);

                        // Create HTML content for each song box
                        formattedData.append("<div style=\"display:flex; align-items:center;\">")
                                .append("<div style=\"width: 200px; height: 200px; margin-right: 10px;\">")
                                .append(imageView)
                                .append("</div>")
                                .append("<div style=\"border: 1px solid #ccc; padding: 10px; margin-bottom: 10px;\">")
                                .append("<p>").append(trackName).append(" - ").append(artistName).append("</p>")
                                .append("</div>")
                                .append("</div>");
                    }

                    // Display the formatted data with HTML formatting
                    runOnUiThread(() -> {
                        profileTextView.setText(Html.fromHtml(formattedData.toString(), Html.FROM_HTML_MODE_COMPACT));
                        profileTextView.setMovementMethod(LinkMovementMethod.getInstance());
                    });
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Error processing response: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to process response",
                            Toast.LENGTH_SHORT).show());
                }
            }

        });
    }

    // Define a class to represent user profile data
    class UserProfile {
        String display_name;
        Followers followers;
        String email;

        class Followers {
            int total;
        }
    }
    public void onGetUserTopArtistsClicked(String type) {

        mAccessToken = this.mAccessToken;


        String url = "https://api.spotify.com/v1/me/top/" + type;


        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + aAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show());
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonResponse = response.body().string();
                    Log.d(TAG, "JSON Response: " + jsonResponse);

                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONArray itemsArray = jsonObject.getJSONArray("items");

                    StringBuilder formattedData = new StringBuilder();

                    // Add header for top tracks
                    formattedData.append("<h2>Your top tracks!</h2>");

                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject trackObject = itemsArray.getJSONObject(i);
                        JSONObject albumObject = trackObject.getJSONObject("album");
                        JSONArray imagesArray = albumObject.getJSONArray("images");
                        String imageUrl = "";
                        if (imagesArray.length() > 0) {
                            JSONObject imageObject = imagesArray.getJSONObject(0);
                            imageUrl = imageObject.getString("url");
                            Log.d(TAG, "Image URL for track " + i + ": " + imageUrl);
                        }

                        String artistName = "";
                        JSONArray artistsArray = trackObject.getJSONArray("artists");
                        if (artistsArray.length() > 0) {
                            JSONObject artistObject = artistsArray.getJSONObject(0);
                            artistName = artistObject.getString("name");
                        }

                        String trackName = trackObject.getString("name");

                        // Create HTML content for each song box
                        formattedData.append("<div style=\"display:flex; align-items:center;\">");
                        formattedData.append("<img src=\"").append(imageUrl).append("\" style=\"width: 200px; height: 200px; margin-right: 10px;\">");
                        formattedData.append("<div style=\"border: 1px solid #ccc; padding: 10px; margin-bottom: 10px;\">");
                        formattedData.append("<p>").append(trackName).append(" - ").append(artistName).append("</p>");
                        formattedData.append("</div>");
                        formattedData.append("</div>");
                    }

                    // Display the formatted data with HTML formatting
                    runOnUiThread(() -> {
                        profileTextView.setText(Html.fromHtml(formattedData.toString(), Html.FROM_HTML_MODE_COMPACT));
                        profileTextView.setMovementMethod(LinkMovementMethod.getInstance());
                    });
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Error processing response: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to process response",
                            Toast.LENGTH_SHORT).show());
                }
            }





        });
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