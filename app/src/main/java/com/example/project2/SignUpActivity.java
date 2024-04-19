package com.example.project2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Button signUpButton = findViewById(R.id.signUpButton);
        auth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(view -> {
            EditText emailText = findViewById(R.id.editTextEmail);
            EditText passText = findViewById(R.id.editTextPassword);

            String email = emailText.getText().toString();
            String password = passText.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(SignUpActivity.this, "Email and password cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.contains("@")) {
                Toast.makeText(SignUpActivity.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                            if (isNewUser) {
                                auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(this, createTask -> {
                                            if (createTask.isSuccessful()) {
                                                Log.d(TAG, "createUserWithEmail:success");
                                                Toast.makeText(SignUpActivity.this, "Account successfully created!",
                                                        Toast.LENGTH_SHORT).show();
                                                Intent myIntent = new Intent(SignUpActivity.this, UserLogin.class);
                                                startActivity(myIntent);
                                            } else {
                                                Log.w(TAG, "createUserWithEmail:failure", createTask.getException());
                                                Toast.makeText(SignUpActivity.this, "Failed to create user. User might already have an account.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(SignUpActivity.this, "This email is already registered.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w(TAG, "fetchSignInMethodsForEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Error checking email existence.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, UserLogin.class);
            startActivity(intent);
            finish();
        });
    }
}
