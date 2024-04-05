package com.example.project2;

import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdateInfo extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonUpdate;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);

        auth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonUpdate = findViewById(R.id.updateButton);

        buttonUpdate.setOnClickListener(view -> updateUserInfo());
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(UpdateInfo.this, UserLogin.class);
            startActivity(intent);
            finish();
        });
    }

    private void updateUserInfo() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String newEmail = editTextEmail.getText().toString().trim();
            String newPassword = editTextPassword.getText().toString().trim();

            if (!TextUtils.isEmpty(newEmail)) {
                user.updateEmail(newEmail)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(UpdateInfo.this, "Email updated successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UpdateInfo.this, "Failed to update email.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            if (!TextUtils.isEmpty(newPassword)) {
                user.updatePassword(newPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(UpdateInfo.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UpdateInfo.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }
}
