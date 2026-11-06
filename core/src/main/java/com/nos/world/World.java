package com.nos.world;

import com.nos.data.Tuning;

/** Estado temporal de um mundo, independente da renderização. */
public final class World {
    private float speed = Tuning.BASE_SPEED;
    private float worldDelta;
    private double elapsedTime;

    void setSpeed(float speed) {
        this.speed = speed;
    }

    void advance(float delta) {
        worldDelta = delta * speed;
        elapsedTime += worldDelta;
    }

    public float getSpeed() {
        return speed;
    }

    public float getWorldDelta() {
        return worldDelta;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }
}
