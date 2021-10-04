package com.example.tamagotchi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    public ImageView ivTamagotchi;
    private TextView tvStrength;
    private TextView tvHappiness;
    private TextView tvLifeTime;

    public Tamagotchi tamagotchi;
    private int shakeCount;
    private long lastTime;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        tamagotchi = new Tamagotchi();
        sp = getSharedPreferences("settings", MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivTamagotchi = findViewById(R.id.ivTamagotchi);
        tvStrength = findViewById(R.id.tvStrength);
        tvHappiness = findViewById(R.id.tvHappiness);
        tvLifeTime = findViewById(R.id.tvLifeTime);

        tamagotchi.setData(
            sp.getInt("strength", 0),
            sp.getInt("happiness", 10),
            sp.getInt("lifeTime", 10),
            sp.getInt("eggFase", 0));

        switch (tamagotchi.getEggFase()) {
            case 1: ivTamagotchi.setImageResource(R.drawable.egg_crack); break;
            case 2: ivTamagotchi.setImageResource(R.drawable.egg_cracked); break;
            case 3: ivTamagotchi.setImageResource(R.drawable.animal);
                startTimer();
                break;
        }

        updateTamagotchi();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && tamagotchi.getEggFase() < 3) {
            float[] values = sensorEvent.values;

            float x = values[0];
            float y = values[1];
            float z = values[2];

            float ac = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
            long now = (new Date()).getTime()
                    + (sensorEvent.timestamp - System.nanoTime()) / 1000000L;

            if (ac >= 2 && now - lastTime > 400)
            {
                lastTime = now;
                shakeCount++;
                if (shakeCount >= 5) {
                    tamagotchi.breakEgg();
                    shakeCount = 0;
                    switch (tamagotchi.getEggFase()) {
                        case 1: ivTamagotchi.setImageResource(R.drawable.egg_crack); break;
                        case 2: ivTamagotchi.setImageResource(R.drawable.egg_cracked); break;
                        case 3: ivTamagotchi.setImageResource(R.drawable.animal);
                            startTimer();
                        break;
                    }

                    SharedPreferences.Editor spEdit = sp.edit();
                    spEdit.putInt("eggFase", tamagotchi.getEggFase());
                    spEdit.apply();
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void feed(View view) {
        tamagotchi.feed();
        updateTamagotchi();
    }

    public void love(View view) {
        tamagotchi.love();
        updateTamagotchi();
    }

    private void updateTamagotchi() {
        tvStrength.setText("Strength: " + tamagotchi.getStrength());
        tvHappiness.setText("Happiness: " + tamagotchi.getHappiness());
        tvLifeTime.setText("LifeTime: " + tamagotchi.getLifeTime() + " seconds");

        SharedPreferences.Editor spEdit = sp.edit();
        spEdit.putInt("strength", tamagotchi.getStrength());
        spEdit.putInt("happiness", tamagotchi.getHappiness());
        spEdit.putInt("lifeTime", tamagotchi.getLifeTime());
        spEdit.apply();
    }

    private void startTimer() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (tamagotchi.getHappiness() <= 0 || tamagotchi.getStrength() <= 0) {
                    ivTamagotchi.setImageResource(R.drawable.dead);
                } else {
                    tamagotchi.secondPassed();
                    updateTamagotchi();
                    handler.postDelayed(this, 1000);
                    System.out.print("run");
                }
            }
        }, 1000);
    }
}