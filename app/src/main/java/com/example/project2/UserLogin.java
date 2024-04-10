package com.example.project2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserLogin extends AppCompatActivity {

    private Button buttonSignIn, buttonSignUp, buttonUpdateInfo, buttonDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);

        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        //buttonUpdateInfo = findViewById(R.id.buttonUpdateInfo);
//        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);

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

//        buttonUpdateInfo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigateToUpdateInfo();
//            }
//        });

//        buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigateToDeleteAccount();
//            }
//        });
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(UserLogin.this, SignInActivity.class);
        startActivity(intent);
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(UserLogin.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void navigateToUpdateInfo() {
        Intent intent = new Intent(UserLogin.this, UpdateInfo.class);
        startActivity(intent);
    }

//    private void navigateToDeleteAccount() {
//        Intent intent = new Intent(UserLogin.this, DeleteAccount.class);
//        startActivity(intent);
//    }
}
