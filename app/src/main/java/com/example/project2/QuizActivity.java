package com.example.project2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class QuizActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_activity);

        Button retry = findViewById(R.id.retry);

        Button back = findViewById(R.id.back_quiz);


        retry.setOnClickListener(view -> {
            startActivity(new Intent(QuizActivity.this, QuizActivity.class));
        });

        back.setOnClickListener(view -> {
            startActivity(new Intent(QuizActivity.this, MainPage.class));
        });

        FirebaseUser fbu = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("Error", fbu.getUid());
        db.collection("user").whereEqualTo("User ID", fbu.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

           boolean answered = false;
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Integer> counts = new HashMap<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String[] data = document.getData().get("Wrapped Data").toString().split("\n", -1);
                        int i = 0;
                        while (!data[i].startsWith("Top Artists")) {
                            i++;
                        }
                        int j = i;
                        i++;
                        while (!data[i].startsWith("Genres")) {
                            counts.put(data[i], Math.min(i - j, counts.getOrDefault(data[i], Integer.MAX_VALUE)));
                            i++;
                        }
                    }
                    Set<String> artists = counts.keySet();
                    Set<String> options = new HashSet<>();
                    Random r = new Random();
                    Set<Integer> intOptions = new HashSet<>();
                    while (intOptions.size() < 4 && intOptions.size() < artists.size()) {
                        intOptions.add(r.nextInt(artists.size()));
                    }
                    int i = 0;
                    for (String s : artists) {
                        if (intOptions.contains(i)) {
                            options.add(s);
                        }
                        if (intOptions.size() == options.size()) {
                            break;
                        }
                        i++;
                    }
                    int min = Integer.MAX_VALUE;
                    for (String s : options) {
                        min = Math.min(min, counts.get(s));
                    }
                    String[] replacements = new String[]{"Steely Dan", "Nat King Cole", "Crush 40", "Some Other Artist"};
                    int k = 0;
                    String correctAnswer = null;
                    Set<String> realOptions = new HashSet<>(options);
                    for (String s : options) {
                        if (counts.get(s) == min) {
                            if (correctAnswer == null) {
                                correctAnswer = s;
                            } else {
                                realOptions.remove(s);
                                realOptions.add(replacements[k]);
                                k++;
                            }
                        }
                    }
                    TextView tv = findViewById(R.id.qtv);
                    tv.setText(correctAnswer);
                    while (realOptions.size() < 4) {
                        realOptions.add(replacements[k]);
                        k++;
                    }
                    Object[] optionsArr = realOptions.toArray();
                    Button ansA = findViewById(R.id.optA);
                    Button ansB = findViewById(R.id.optB);
                    Button ansC = findViewById(R.id.optC);
                    Button ansD = findViewById(R.id.optD);

                    ansA.setText((String) optionsArr[0]);
                    ansB.setText((String) optionsArr[1]);
                    ansC.setText((String) optionsArr[2]);
                    ansD.setText((String) optionsArr[3]);

                    Log.d("Error", "Made it?");

                    ansA.setOnClickListener(view -> {
                        if (!answered) {
                            String message;
                            answered = true;
                            if (tv.getText().toString().equals(ansA.getText().toString())) {
                                ansA.setBackgroundColor(getResources().getColor(R.color.spotify_green));
                                message = "Correct! Click the button below to answer another question!";
                            } else {
                                ansA.setBackgroundColor(Color.RED);
                                message = "Wrong! Click the button below to try another question!";
                            }
                            Toast.makeText(QuizActivity.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(QuizActivity.this, "You can't answer again!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    ansB.setOnClickListener(view -> {
                        if (!answered) {
                            String message;
                            answered = true;
                            if (tv.getText().toString().equals(ansB.getText().toString())) {
                                ansB.setBackgroundColor(getResources().getColor(R.color.spotify_green));
                                message = "Correct! Click the button below to answer another question!";
                            } else {
                                ansB.setBackgroundColor(Color.RED);
                                message = "Wrong! Click the button below to try another question!";
                            }
                            Toast.makeText(QuizActivity.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(QuizActivity.this, "You can't answer again!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    ansC.setOnClickListener(view -> {
                        if (!answered) {
                            String message;
                            answered = true;
                            if (tv.getText().toString().equals(ansC.getText().toString())) {
                                ansC.setBackgroundColor(getResources().getColor(R.color.spotify_green));
                                message = "Correct! Click the button below to answer another question!";
                            } else {
                                ansC.setBackgroundColor(Color.RED);
                                message = "Wrong! Click the button below to try another question!";
                            }
                            Toast.makeText(QuizActivity.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(QuizActivity.this, "You can't answer again!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    ansD.setOnClickListener(view -> {
                        if (!answered) {
                            String message;
                            answered = true;
                            if (tv.getText().toString().equals(ansD.getText().toString())) {
                                ansD.setBackgroundColor(getResources().getColor(R.color.spotify_green));
                                message = "Correct! Click the button below to answer another question!";
                            } else {
                                ansD.setBackgroundColor(Color.RED);
                                message = "Wrong! Click the button below to try another question!";
                            }
                            Toast.makeText(QuizActivity.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(QuizActivity.this, "You can't answer again!", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Log.d("error", "help me");
                }
            }
        });
    }
}
