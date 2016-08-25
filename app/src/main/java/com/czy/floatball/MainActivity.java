package com.czy.floatball;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.czy.floatball.Service.StartFloatBallService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startService(View view) {
        Intent intent = new Intent(this, StartFloatBallService.class);
        startService(intent);
        finish();
    }
}
