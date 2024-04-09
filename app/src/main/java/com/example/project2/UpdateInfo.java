package com.example.project2;

import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class UpdateInfo extends AppCompatActivity {

    private static final String TAG = "UpdateCredentials";
    private EditText editTextNewEmail, editTextNewPassword;
    private boolean emailUpdateSuccess = false;
    private boolean passwordUpdateSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info); // Replace with your actual layout file name

        editTextNewEmail = findViewById(R.id.editTextEmail);
        editTextNewPassword = findViewById(R.id.editTextPassword);
        Button buttonUpdateCredentials = findViewById(R.id.updateButton);

        buttonUpdateCredentials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEmailAndPassword();
            }
        });
    }

    private void updateEmailAndPassword() {
        String newEmail = editTextNewEmail.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Reset flags
            emailUpdateSuccess = false;
            passwordUpdateSuccess = false;

            // Update email
            user.updateEmail(newEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User email address updated.");
                                Toast.makeText(UpdateInfo.this, "Email address successfully updated", Toast.LENGTH_SHORT).show();
                                emailUpdateSuccess = true;
                                checkAndNavigateBack();
                            }
                        }
                    });

            // Update password
            user.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User password updated.");
                                Toast.makeText(UpdateInfo.this, "Password successfully updated", Toast.LENGTH_SHORT).show();
                                passwordUpdateSuccess = true;
                                checkAndNavigateBack();
                            }
                        }
                    });
        }
    }

    private void checkAndNavigateBack() {
        if (emailUpdateSuccess && passwordUpdateSuccess) {
            // Both email and password have been successfully updated, navigate back
            Intent intent = new Intent(UpdateInfo.this, UserLogin.class);
            startActivity(intent);
            finish(); // Optional: if you don't want users to return to this screen with the back button.
        }
    }
}
