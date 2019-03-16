package com.example.digipump;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = findViewById(R.id.iv);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.transition);
        iv.startAnimation(anim);
        if (!checkpermission(Manifest.permission.SEND_SMS)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(MainActivity.this, SMSActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        timer.start();
    }

    public boolean checkpermission(String Permission) {
        int check = ContextCompat.checkSelfPermission(this, Permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }
}
