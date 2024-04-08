package com.example.project2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PastSpotifyWrapped extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pastwrapped);
        TextView tv = findViewById(R.id.pastWrappedText);
        FirebaseUser fbu = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("Error", fbu.getUid());
        Button back = findViewById(R.id.pwBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PastSpotifyWrapped.this, MainPage.class));
            }
        });
        List<WrappedData> pastWrappeds = new ArrayList<>();
        db.collection("user").whereEqualTo("User ID", fbu.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                WrappedData wd = new WrappedData();
                                wd.setTopGenres(new ArrayList<>());
                                String[] data = document.getData().get("Wrapped Data").toString().split("\n", -1);
                                Log.d("Error", Arrays.toString(data));
                                wd.setUsername(data[0].substring(10));
                                wd.setSP(Integer.parseInt(data[1].substring(4)));
                                int i = 3;
                                while (i < data.length) {
                                    if (data[i].startsWith("Top Artists")) {
                                        i++;
                                        break;
                                    } else {
                                        wd.addTrack(data[i].substring(1));
                                        i++;
                                    }
                                }
                                while (i < data.length) {
                                    if (data[i].startsWith("Genres")) {
                                        i++;
                                        break;
                                    } else {
                                        wd.addArtist(data[i].substring(1));
                                        i++;
                                    }
                                }
                                while (i < data.length) {
                                    if (data[i].startsWith("Listening Time: ")) {
                                        wd.setListeningTimeMS(Integer.parseInt(data[i].substring(16)));
                                        break;
                                    } else {
                                        wd.addGenre(data[i].substring(1));
                                        i++;
                                    }
                                }
                                pastWrappeds.add(wd);
                            }
                        } else {
                            Log.d("Error", "Help");
                        }
                        Button left = findViewById(R.id.plLeft);
                        Button right = findViewById(R.id.plRight);
                        TextView posView = findViewById(R.id.pwPos);
                        int[] pos = new int[1];
                        displayData(0);
                        left.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (pos[0] > 0) {
                                    pos[0]--;
                                    displayData(pos[0]);
                                }
                            }
                        });
                        right.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (pos[0] + 1 < pastWrappeds.size()) {
                                    pos[0]++;
                                    displayData(pos[0]);
                                }
                            }
                        });
                    }
                    public void displayData(int i) {
                        // Old stuff (you can delete this idc)
                        tv.setText(pastWrappeds.get(i).toString());
                        TextView posView = findViewById(R.id.pwPos);
                        String temp = (i + 1) + "/" + pastWrappeds.size();
                        posView.setText(temp);

                        // Data we need to display
                        WrappedData data = pastWrappeds.get(i);
                        String username = data.getUsername();
                        String timespan = timespan(data.getSp());
                        List<String> topTracks = data.getTopTracks();
                        List<String> topArtists = data.getTopArtists();
                        List<String> topGenres = data.getTopGenres();

                    }

                    private String timespan(int i) {
                        switch (i) {
                            case 0:
                                return "Month";
                            case 1:
                                return "Half-Year";
                            default:
                                return "Year";
                        }
                    }
                });

    }
}
