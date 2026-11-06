package com.nos.world;

import com.nos.data.Tuning;
import com.nos.entities.Hero;

/** Estado temporal de um mundo, independente da renderização. */
public final class World {
    private float speed = Tuning.BASE_SPEED;
    private float worldDelta;
    private double elapsedTime;
    private final Trail trail;
    private final Hero hero;

    public World() {
        trail = null;
        hero = null;
    }

    public World(Trail trail) {
        if (trail == null) throw new IllegalArgumentException("A trilha não pode ser nula.");
        this.trail = trail;
        hero = new Hero(trail);
    }

    public Trail getTrail() { return trail; }
    public Hero getHero() { return hero; }

    void setSpeed(float speed) {
        this.speed = speed;
    }

    void advance(float delta) {
        worldDelta = delta * speed;
        elapsedTime += worldDelta;
        if (hero != null) hero.update(worldDelta);
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
