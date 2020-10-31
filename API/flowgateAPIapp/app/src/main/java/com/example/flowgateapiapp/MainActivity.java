package com.example.flowgateapiapp;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.data);
        Context context = this.getApplicationContext();
        Thread t = new Thread(){
            @Override
            public void run(){
                flowgateClient fc = new flowgateClient("202.121.180.32", "admin", "Ar_InDataCenter_450");
                fc.getAssetByNameOnScreen(context, "testServer", textView);
            }
        };
        t.start();

    }

}
