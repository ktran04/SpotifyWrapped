package com.example.project2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserLogin extends AppCompatActivity {

    private Button buttonSignIn, buttonSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);

        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSignIn();
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSignUp();
            }
        });
    }

    private void navigateToSignIn() {
        // Implement navigation to sign-in page logic here
        Intent intent = new Intent(UserLogin.this, SignInActivity.class);
        startActivity(intent);
    }

    private void navigateToSignUp() {
        // Implement navigation to sign-up page logic here
        Intent intent = new Intent(UserLogin.this, SignUpActivity.class);
        startActivity(intent);
    }
}
