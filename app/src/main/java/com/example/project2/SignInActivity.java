package com.example.project2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    private static final String TAG = "SignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        Button signInButton = findViewById(R.id.loginButton);
        auth = FirebaseAuth.getInstance();

        signInButton.setOnClickListener(view -> {
            EditText emailText = findViewById(R.id.editTextEmail);
            EditText passText = findViewById(R.id.editTextPassword);

            auth.signInWithEmailAndPassword(emailText.getText().toString(), passText.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            Intent myIntent = new Intent(SignInActivity.this, MainPage.class);
                            startActivity(myIntent);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Please enter the correct email and password.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, UserLogin.class);
            startActivity(intent);
            finish();
        });
    }
}


