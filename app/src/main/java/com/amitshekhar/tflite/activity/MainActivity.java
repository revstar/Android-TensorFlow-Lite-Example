package com.amitshekhar.tflite.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.amitshekhar.tflite.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_album).setOnClickListener(this);
        findViewById(R.id.btn_select).setOnClickListener(this);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_album:
                startActivity(new Intent(this,AlbumActivity.class));
                break;
            case R.id.btn_select:
                startActivity(new Intent(this,SelectActivity.class));
                break;
                default:
                    break;
        }
    }
}
