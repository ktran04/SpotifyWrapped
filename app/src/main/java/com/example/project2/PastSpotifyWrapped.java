package com.example.project2;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class PastSpotifyWrapped extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pastwrapped);
        TextView tv = findViewById(R.id.pastWrappedText);
        FirebaseUser fbu = FirebaseAuth.getInstance().getCurrentUser();
        StringBuilder out = new StringBuilder();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("Error", fbu.getUid());
        db.collection("user").whereEqualTo("User ID", fbu.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Error", document.getData().get("Wrapped Data").toString());
                            }
                        } else {
                            Log.d("Error", "Help");
                        }
                    }
                });
    }
}
