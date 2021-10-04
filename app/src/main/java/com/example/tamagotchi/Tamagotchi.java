package com.example.tamagotchi;

import android.content.SharedPreferences;

public class Tamagotchi {
    private int strength;
    private int happiness;
    private int lifeTime;
    private int counter = 0;
    private int eggFase;

    public int getStrength() {
        return strength;
    }

    public void feed() {
        if (!isDead() && eggFase == 3) strength += 1;
    }

    public int getHappiness() {
        return happiness;
    }

    public void love() {
        if (!isDead() && eggFase == 3) happiness += 1;
    }

    public void breakEgg() {
        eggFase++;
    }

    public int getEggFase() {
        return eggFase;
    }

    public void secondPassed() {
        lifeTime++;
        counter++;
        if (counter == 5) happiness--;
        if (counter == 10) {
            strength--;
            counter = 0;
        }
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public void setData(int strength, int happiness, int lifeTime, int eggFase) {
        this.strength = strength;
        this.happiness = happiness;
        this.lifeTime = lifeTime;
        this.eggFase = eggFase;
    }

    public boolean isDead() {
        if (happiness <= 0 || strength <= 0) return true;
        return false;
    }
}
