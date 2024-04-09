package com.example.project2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DeleteAccount extends AppCompatActivity {
    private static final String TAG = "DeleteAccount";
    private EditText emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        Button deleteButton = findViewById(R.id.deleteButton);
        emailText = findViewById(R.id.editTextEmailToDelete);

        deleteButton.setOnClickListener(view -> {
            String emailToDelete = emailText.getText().toString();
            if (!emailToDelete.isEmpty()) {
                deleteAccount(emailToDelete);
            } else {
                Toast.makeText(DeleteAccount.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAccount(String emailToDelete) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "deleteAccount:success");
                            Toast.makeText(DeleteAccount.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                            finish(); // Close this activity after deletion
                        } else {
                            Log.w(TAG, "deleteAccount:failure", task.getException());
                            Toast.makeText(DeleteAccount.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.d(TAG, "User is null.");
            Toast.makeText(DeleteAccount.this, "User is null.", Toast.LENGTH_SHORT).show();
        }
    }
}



