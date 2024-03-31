package com.example.project2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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

                    // Parse JSON response into a Java object
                    UserProfile userProfile = gson.fromJson(jsonResponse, UserProfile.class);

                    // Format the user profile data
                    String formattedProfile = "User: " + userProfile.display_name + "\n" +
                            "Followers: " + userProfile.followers.total + "\n" +
                            "Email: " + userProfile.email;
                    user.setUsername(userProfile.display_name);
                    user.setFollowers(userProfile.followers.total);
                    user.setEmail(userProfile.email);
                } catch (IOException e) {
                    Log.d("IO", "Failed to read response: " + e);
                    Toast.makeText(MainActivity.this, "Failed to read response, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
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
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONArray itemsArray = jsonObject.getJSONArray("items");



                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject artistObject = itemsArray.getJSONObject(i);
                        String artistName = artistObject.getString("name");
                        if (type.equals("artists")) {
                            user.addArtist(artistName);
                        } else {
                            user.addTrack(artistName);
                            user.addTime(artistObject.getInt("duration_ms") / 1000);
                        }
                    }
                    if (type.equals("tracks")) {
                        // ADD DISPLAY CODE HERE. ALL NECESSARY DATA IS NOW IN user
                    }

                    runOnUiThread(() -> setTextAsync(user.toString(), profileTextView));
                } catch (IOException | JSONException e) {
                    Log.d("Error", "Failed to read or parse response: " + e);
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

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }
}