package com.example.project2;

import static com.example.project2.TextDrawable.generateBitmap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;

import java.io.OutputStream;
import java.util.Objects;
import java.util.Random;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import android.Manifest;


public class MainActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "ef598ecfefbc48da953792cd34909460";
    public static final String REDIRECT_URI = "com.example.project2://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    public static final int TOP_ARTIST_REQUEST_CODE = 2;
    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView trackRecyclerView;
    private FirebaseAnalytics mFirebaseAnalytics;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken, mAccessCode, aAccessToken;
    private Call mCall;

    private TextView tokenTextView, codeTextView, profileTextView;

    private WrappedData user;
    private RecyclerView topTracksRecyclerView;
    private RecyclerView topArtistsRecyclerView;
    private TopTracksAdapter topTracksAdapter;
    private TopGenresAdapter topGenresAdapter;
    private RecyclerView topGenresRecyclerView;

    private int spinnerPosition;


    Button LLMAccess;
    String value = new String("hello");

    private static final int REQUEST_CODE = 1;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinnerPosition = 0;


        Spinner spinner = findViewById(R.id.spinner);
        String[] stuff = new String[]{"Month", "Half Year", "Year"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, stuff);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerPosition = position;
                Log.d("Error", "" + spinnerPosition);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerPosition = spinnerPosition;
                Log.d("Error", "" + spinnerPosition);
            }
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize the views
        profileTextView = (TextView) findViewById(R.id.response_text_view);

        // Initialize the buttons
        Button tokenBtn = (Button) findViewById(R.id.token_btn);

        topGenresRecyclerView = findViewById(R.id.top_genres_recycler_view);
        LinearLayoutManager genreLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        topGenresRecyclerView.setLayoutManager(genreLayoutManager);
        TopGenresAdapter genreAdapter = new TopGenresAdapter(new ArrayList<>());
        topGenresRecyclerView.setAdapter(genreAdapter);

        topArtistsRecyclerView = findViewById(R.id.top_artists_recycler_view);
        LinearLayoutManager artistLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        trackRecyclerView = findViewById(R.id.trackRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        trackRecyclerView.setLayoutManager(layoutManager);
        topArtistsRecyclerView.setLayoutManager(artistLayoutManager);
        TopArtistsAdapter artistsAdapter = new TopArtistsAdapter(this, new ArrayList<>()); // Pass empty list initially
        topArtistsRecyclerView.setAdapter(artistsAdapter);


        // Set up adapter
        TopTracksAdapter trackAdapter; // Pass empty list initially
        trackAdapter = new TopTracksAdapter(new ArrayList<>());
        trackRecyclerView.setAdapter(trackAdapter);
        // Set the click listeners for the buttons
        tokenBtn.setOnClickListener((v) -> {
            Log.d(TAG, "Token Button Clicked");
            user = new WrappedData();
            getToken(0);
        });

        Button back = findViewById(R.id.back_btn);

        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent backToHome = new Intent(MainActivity.this, MainPage.class);
                startActivity(backToHome);
            }
        });


        // Find the Sign In button by its ID





        // Initialize the views

        // Log the initialization completion
        Log.d(TAG, "Activity initialized successfully");

        LLMAccess = findViewById(R.id.LLMAccess);
        LLMAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LLMAccess.class);
                value = "HIIIIII";
                intent.putExtra("key", value);
                startActivity(intent);
            }
        });
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
            onGetUserProfileClicked();
            getToken(2);

        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.getCode();
            setTextAsync(mAccessCode, codeTextView);
        } else if (TOP_ARTIST_REQUEST_CODE == requestCode) {
            aAccessToken = response.getAccessToken();
            parseTopData("tracks");
        } else {
            aAccessToken = response.getAccessToken();
            parseTopData("artists");
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
    public void parseTopData(String type) {
        Log.d(TAG, "Get user top artists clicked");
        String url;
        if (type.equals("tracks")) {
            url = "https://api.spotify.com/v1/me/top/tracks";
        } else {
            url = "https://api.spotify.com/v1/me/top/artists";
        }
        url += range();

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
    private String range() {
        String out = "?time_range=";
        switch (spinnerPosition) {
            case 0:
                out += "short_term&limit=5";
                break;
            case 1:
                out += "medium_term&limit=25";
                break;
            default:
                out += "long_term&limit=50";
                break;
        }
        return out;
    }

    private void displayWrappedData(WrappedData user) {
        String username = user.getUsername();
        List<String> topTracks = user.getTopTracks();
        List<String> topArtists = user.getTopArtists();
        List<String> topGenres = user.getTopGenres();
        int totalListeningTimeMinutes = user.getListeningTimeMS() / 60000;
        user.setSP(spinnerPosition);
        FirebaseUser fbu = FirebaseAuth.getInstance().getCurrentUser();
        if (fbu != null) {
            Map<String, Object> input = new HashMap<>();
            input.put("User ID", fbu.getUid());
            input.put("Wrapped Data", user.toString());
            db.collection("user").add(input).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d(TAG, "Yippee");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Help");
                }
            });
        }

        // Set the username
        TextView usernameTextView = findViewById(R.id.username_text_view);
        usernameTextView.setText("Welcome: " + username + exString());

        // Set the top artists using RecyclerView
        RecyclerView topArtistsRecyclerView = findViewById(R.id.top_artists_recycler_view);
        LinearLayoutManager artistLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        topArtistsRecyclerView.setLayoutManager(artistLayoutManager);
        TopArtistsAdapter artistsAdapter = new TopArtistsAdapter(this, topArtists);
        topArtistsRecyclerView.setAdapter(artistsAdapter);

        // Set the top genres using RecyclerView
        RecyclerView topGenresRecyclerView = findViewById(R.id.top_genres_recycler_view);
        LinearLayoutManager genresLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        topGenresRecyclerView.setLayoutManager(genresLayoutManager);
        TopGenresAdapter genresAdapter = new TopGenresAdapter(topGenres);
        topGenresRecyclerView.setAdapter(genresAdapter);

        // Set the total listening time
        TextView listeningTimeTextView = findViewById(R.id.listening_time_text_view);
        listeningTimeTextView.setText("            Total Listening Time: " + totalListeningTimeMinutes + " minutes");

        // Set the top tracks using RecyclerView
        RecyclerView topTracksRecyclerView = findViewById(R.id.top_tracks_recycler_view);
        LinearLayoutManager trackLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        topTracksRecyclerView.setLayoutManager(trackLayoutManager);
        TopTracksAdapter trackAdapter = new TopTracksAdapter(topTracks);
        topTracksRecyclerView.setAdapter(trackAdapter);
        imageView = findViewById(R.id.image_summary);
        imageView.setImageBitmap(generateImageFromProfile(user));
        Button saveImageButton = findViewById(R.id.save_img_btn);
        saveImageButton.setVisibility(View.VISIBLE);
        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveImage();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (true) {
                saveImage();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE);
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
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/");
        Uri uri = contentResolver.insert(images, contentValues);

        try {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);
            Toast.makeText(MainActivity.this, "Image saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Image not saved", Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }
    }



    private Bitmap generateImageFromProfile(WrappedData user) {
        String text = "Top Tracks: " + "\n"  + user.getTopTracks().get(0) + "\n" + user.getTopTracks().get(1) + "\n" + user.getTopTracks().get(2)+ "\n"
                + "\nTop Artists: " + "\n" + user.getTopArtists().get(0) + "\n" + user.getTopArtists().get(1) + "\n" + user.getTopArtists().get(2) + "\n" + user.getTopArtists().get(3) + "\n" + user.getTopArtists().get(4) + "\n" +
                "\nTop Genres: " + "\n" + user.getTopGenres().get(0) + "\n" +  user.getTopGenres().get(1) + "\n" +  user.getTopGenres().get(2) + "\n" +  user.getTopGenres().get(3) + "\n" +  user.getTopGenres().get(4);
        //String text = "Followers: " + userProfile.followers.total + "\nEmail: " + userProfile.email;

        // Assuming screenWidth and screenHeight are available in your context
        Bitmap backgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.your_wrapped_5); // Change R.drawable.background_image to your desired background image resource

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Generate the bitmap with the provided text

        return generateBitmap(MainActivity.this, text, backgroundImage, screenWidth, screenHeight);


    }

    private String exString() {
        String out = "\nHere's your last ";
        switch (spinnerPosition) {
            case 0:
                out += "month in Spotify!";
                break;
            case 1:
                out += "half a year in Spotify!";
                break;
            default:
                out += "year in Spotify!";
                break;
        }
        return out;
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
