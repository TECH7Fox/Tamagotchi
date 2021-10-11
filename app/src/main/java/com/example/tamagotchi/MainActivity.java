package com.example.tamagotchi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

        setImage();
        if (tamagotchi.getEggFase() >= 3) startTimer();
        updateTamagotchi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.resetBtn:
                resetTamagotchi();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

                    SharedPreferences.Editor spEdit = sp.edit();
                    spEdit.putInt("eggFase", tamagotchi.getEggFase());
                    spEdit.apply();

                    shakeCount = 0;
                    setImage();
                    if (tamagotchi.getEggFase() >= 3) startTimer();
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
        setImage();

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
                if (tamagotchi.isDead()) {
                    ivTamagotchi.setImageResource(R.drawable.dead);
                } else {
                    tamagotchi.secondPassed();
                    updateTamagotchi();
                }
                handler.postDelayed(this, 1000);
                System.out.print("run");
            }
        }, 1000);
    }

    private void setImage() {
        if (!tamagotchi.isDead()) {
            switch (tamagotchi.getEggFase()) {
                case 0:ivTamagotchi.setImageResource(R.drawable.egg);break;
                case 1:ivTamagotchi.setImageResource(R.drawable.egg_cracked);break;
                case 2:ivTamagotchi.setImageResource(R.drawable.egg_open);break;
                case 3:
                    if (tamagotchi.getHappiness() > 20) ivTamagotchi.setImageResource(R.drawable.happy);
                    else if (tamagotchi.getHappiness() < 10) ivTamagotchi.setImageResource(R.drawable.angry);
                    else ivTamagotchi.setImageResource(R.drawable.idle);
                    break;
            }
        }
    }

    public void resetTamagotchi() {
        SharedPreferences.Editor spEdit = sp.edit();
        spEdit.putInt("strength", 10);
        spEdit.putInt("happiness", 15);
        spEdit.putInt("lifeTime", 0);
        spEdit.putInt("eggFase", 0);
        spEdit.commit();

        tamagotchi.setData(10, 15, 0, 0);
        updateTamagotchi();
    }
}