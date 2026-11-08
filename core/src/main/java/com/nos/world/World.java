package com.nos.world;

import com.nos.data.Tuning;
import com.nos.entities.Hero;

/** Estado temporal de um mundo, independente da renderização. */
public final class World {
    private float speed = Tuning.BASE_SPEED;
    private float worldDelta;
    private double elapsedTime;
    private float ink = 1f;
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
    public float getInk() { return ink; }
    public void setInk(float value) {
        if (!Float.isFinite(value)) throw new IllegalArgumentException("Tinta inválida.");
        ink = Math.max(0f, Math.min(1f, value));
    }
    public void restore(float distance, double elapsed) {
        hero.seek(distance);
        elapsedTime = elapsed;
        worldDelta = 0;
        ink = Tuning.RESPAWN_INK;
    }

    void setSpeed(float speed) {
        this.speed = speed;
    }

    void advance(float delta) {
        worldDelta = delta * speed;
        elapsedTime += worldDelta;
        if (hero != null && ink > 0f) hero.update(worldDelta);
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
