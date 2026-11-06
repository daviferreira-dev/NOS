package com.nos.world;

import com.nos.data.Tuning;

/** Centraliza o espelho: observar A desacelera A e acelera B. */
public final class MirrorPair {
    private final World worldA = new World();
    private final World worldB = new World();
    private float balance;

    public void setBalance(float balance) {
        if (Float.isNaN(balance) || Float.isInfinite(balance)) {
            throw new IllegalArgumentException("O equilíbrio deve ser finito.");
        }
        this.balance = Math.max(-1f, Math.min(1f, balance));
        float offset = Tuning.GAZE_STRENGTH * this.balance;
        worldA.setSpeed(Tuning.BASE_SPEED - offset);
        worldB.setSpeed(Tuning.BASE_SPEED + offset);
    }

    public void update(float delta) {
        if (Float.isNaN(delta) || Float.isInfinite(delta) || delta < 0f) {
            throw new IllegalArgumentException("O delta deve ser finito e não negativo.");
        }
        worldA.advance(delta);
        worldB.advance(delta);
    }

    public World getWorldA() {
        return worldA;
    }

    public World getWorldB() {
        return worldB;
    }

    public float getBalance() {
        return balance;
    }
}
