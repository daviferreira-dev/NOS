package com.nos.entities;

import com.badlogic.gdx.math.Vector2;
import com.nos.data.Tuning;
import com.nos.world.Trail;

/** Avança pela trilha usando exclusivamente o delta do seu mundo. */
public final class Hero {
    private final Trail trail;
    private final Vector2 position = new Vector2();
    private float distance;

    public Hero(Trail trail) {
        this.trail = trail;
        trail.positionAtDistance(0f, position);
    }

    public void update(float worldDelta) {
        if (!Float.isFinite(worldDelta) || worldDelta < 0f) {
            throw new IllegalArgumentException("O delta do mundo deve ser finito e não negativo.");
        }
        distance = Math.min(trail.getLength(), distance + Tuning.HERO_SPEED * worldDelta);
        trail.positionAtDistance(distance, position);
    }

    public float getX() { return position.x; }
    public float getY() { return position.y; }
    public float getDistance() { return distance; }
    public boolean hasArrived() { return distance >= trail.getLength(); }
}
