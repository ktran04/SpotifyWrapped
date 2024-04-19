package com.example.project2;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.common.util.concurrent.ListenableFuture;


public class LLMAccess extends AppCompatActivity {
    TextView textView;
    TextView textView2;
    Button Return;
    String apiKey = "AIzaSyDL0pWCmXUW2HQFFatBgoUtjI-5vRc2RLo";
    GenerativeModel gm = new GenerativeModel("MODEL_NAME", "AIzaSyDL0pWCmXUW2HQFFatBgoUtjI-5vRc2RLo");

    GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_llmaccess);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        String text = getIntent().getStringExtra("key");
        if (text != null) {
            textView.setText("Based off of your top artists, we can describe you as: ");
        }

        Return = findViewById(R.id.returnButton);
        Return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LLMAccess.this, MainActivity.class);
                startActivity(intent);
            }
        });
        GenerativeModel gm = new GenerativeModel( "gemini-pro", "AIzaSyDL0pWCmXUW2HQFFatBgoUtjI-5vRc2RLo");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText("Describe how I think and act based off of these being my top artists in about 8 " +
                        "sentences" +
                        ". Also give me three recommended " +
                        "artists based off of"+
                        " these as well (Give no additional explanation to artists): " + text)
                .build();

        Executor executor = Executors.newCachedThreadPool();;

                ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                System.out.println(resultText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView2.setText(resultText);
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, executor);
    }
}