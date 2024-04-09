package com.example.project2;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static com.example.project2.TextDrawable.generateBitmap;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.Manifest;

public class MainActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "ef598ecfefbc48da953792cd34909460";
    public static final String REDIRECT_URI = "com.example.project2://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseAnalytics mFirebaseAnalytics;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken, mAccessCode;
    private Call mCall;

    private TextView tokenTextView, codeTextView, profileTextView;

    private static final int REQUEST_CODE = 1;
    ImageView profile_info;
    Button SaveImg;
    OutputStream outputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize the views
        profileTextView = (TextView) findViewById(R.id.response_text_view);


        // Initialize the buttons
        Button tokenBtn = (Button) findViewById(R.id.token_btn);
        Button profileBtn = (Button) findViewById(R.id.profile_btn);
        Button download_image_btn = (Button) findViewById(R.id.download_image_btn);

        profile_info = findViewById(R.id.profile_image_view);

        // Set the click listeners for the buttons

        tokenBtn.setOnClickListener((v) -> {
            getToken();
        });

        profileBtn.setOnClickListener((v) -> {
            onGetUserProfileClicked();
        });
        tokenTextView = (TextView) findViewById(R.id.token_text_view);
        profileTextView = (TextView) findViewById(R.id.response_text_view);

        download_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                    saveImage();

                }else {


                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },REQUEST_CODE);

                }

            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {

        if (requestCode == REQUEST_CODE)
        {

            if (true){


                saveImage();

            }else {


                Toast.makeText(MainActivity.this,"Please provide the required permissions",Toast.LENGTH_SHORT).show();

            }

        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void saveImage() {
        Uri images;
        ContentResolver contentResolver = getContentResolver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
        Uri uri = contentResolver.insert(images, contentValues);

        try {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) profile_info.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);
            Toast.makeText(MainActivity.this,"Image Saved Successfully",Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Toast.makeText(MainActivity.this,"Image Not Saved",Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }



    }






    // Initialize the views


//        Button writeButton = findViewById(R.id.writeButton);
//        Button readButton = findViewById(R.id.readButton);
//        final TextView textViewDatabase = findViewById(R.id.textViewDatabase);

        // Initialize the buttons


        // Set the click listeners for the buttons




    /**
     * Get token from Spotify
     * This method will open the Spotify login activity and get the token
     * What is token?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getToken() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        AuthorizationClient.openLoginActivity(MainActivity.this, AUTH_TOKEN_REQUEST_CODE, request);
    }

    /**
     * Get code from Spotify
     * This method will open the Spotify login activity and get the code
     * What is code?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getCode() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
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

        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.getCode();
            setTextAsync(mAccessCode, codeTextView);
        }

        Button profileBtn = (Button) findViewById(R.id.profile_btn);
        profileBtn.performClick();
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
                Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }
            // Method to generate Bitmap from user profile data
            private Bitmap generateImageFromProfile(Context context, UserProfile userProfile) {
                String text = "Followers: " + userProfile.followers.total + "\nEmail: " + userProfile.email + "\nDisplay Name: " + userProfile.display_name;
                //String text = "Followers: " + userProfile.followers.total + "\nEmail: " + userProfile.email;

                // Assuming screenWidth and screenHeight are available in your context
                Bitmap backgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.spotify_profile_info_background); // Change R.drawable.background_image to your desired background image resource

                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int screenHeight = getResources().getDisplayMetrics().heightPixels;

                // Generate the bitmap with the provided text

                return generateBitmap(context, text, backgroundImage, screenWidth, screenHeight);


            }





            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String jsonResponse = response.body().string();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();

                    /*// Parse JSON response into a Java object
                    UserProfile userProfile = gson.fromJson(jsonResponse, UserProfile.class);
                    Bitmap profileImage = generateImageFromProfile(userProfile);
                    runOnUiThread(() -> {
                        ImageView imageView = findViewById(R.id.profile_image_view); // Replace with your ImageView ID
                        imageView.setImageBitmap(profileImage);
                    });*/
                    // Parse JSON response into a Java object
                    UserProfile userProfile = gson.fromJson(jsonResponse, UserProfile.class);

                    // Generate image from user profile data
                    Bitmap profileImage = generateImageFromProfile(MainActivity.this, userProfile);
                    runOnUiThread(() -> {
                        ImageView imageView = findViewById(R.id.profile_image_view); // Replace with your ImageView ID
                        imageView.setImageBitmap(profileImage);
                    });

                    // Format the user profile data
                    /*String formattedProfile = "User: " + userProfile.display_name + "\n" +
                            "Followers: " + userProfile.followers.total + "\n" +
                            "Email: " + userProfile.email;

                    setTextAsync(formattedProfile, profileTextView);*/
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
    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(true)
                .setScopes(new String[] { "user-read-email" }) // <--- Change the scope of your requested token here
                .setCampaign("your-campaign-token")
                .build();
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

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }
}