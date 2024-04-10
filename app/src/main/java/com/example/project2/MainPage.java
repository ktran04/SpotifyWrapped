package com.example.project2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainPage extends AppCompatActivity {
    private static final String TAG = "MainPage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);

        Button buttonUpdateInfo = findViewById(R.id.buttonUpdateInfo);
        Button buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);
        Button buttonPastSpotifyWrapped = findViewById(R.id.buttonPastSpotifyWrapped);
        Button buttonSignOut = findViewById(R.id.buttonSignOut);
        Button buttonGenWrapped = findViewById(R.id.genWrappedActivity);
        Button quiz = findViewById(R.id.quizButton);

        buttonPastSpotifyWrapped.setOnClickListener(view -> {
            startActivity(new Intent(MainPage.this, PastSpotifyWrapped.class));
        });
        buttonGenWrapped.setOnClickListener(view -> {
            startActivity(new Intent(MainPage.this, MainActivity.class));
        });

        buttonUpdateInfo.setOnClickListener(view -> {
            startActivity(new Intent(MainPage.this, UpdateInfo.class));
        });

        quiz.setOnClickListener(view -> {
            startActivity(new Intent(MainPage.this, QuizActivity.class));
        });


        buttonDeleteAccount.setOnClickListener(view -> {
//            startActivity(new Intent(MainPage.this, DeleteAccount.class));
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                user.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User account deleted.");
                                    Toast.makeText(MainPage.this, "Account Successfully Deleted", Toast.LENGTH_SHORT).show();
                                    // Navigate back to UserLogin.class after showing toast
                                    Intent intent = new Intent(MainPage.this, UserLogin.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the activity stack
                                    startActivity(intent);
                                    finish(); // Ensure this activity is closed
                                } else {
                                    // Handle the failure case, optionally with another toast
                                    Log.d(TAG, "User account deletion failed.");
                                    Toast.makeText(MainPage.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


        //To Be Implemented

//        buttonPastSpotifyWrapped.setOnClickListener(view -> {
//            startActivity(new Intent(MainPage.this, PastSpotifyWrapped.class));
//        });

        buttonSignOut.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            // Log success message
            Log.d(TAG, "User signed out successfully.");
            Toast.makeText(MainPage.this, "You've successfully signed out!", Toast.LENGTH_SHORT).show();

            // For demonstration purposes, navigate back to UserLogin class
            // after signing out. Replace this with any action you deem necessary.
            Intent intent = new Intent(MainPage.this, UserLogin.class);
            startActivity(intent);
            finish(); // Close the current activity
        });
    }
}
