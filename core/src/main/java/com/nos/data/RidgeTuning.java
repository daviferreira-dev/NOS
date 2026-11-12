package com.nos.data;

/** Ritmo da subida e da rajada; não há prazo, queda ou dano. */
public final class RidgeTuning {
    public static final float MAX_DELTA=.1f,WIND_WARNING=1.3f,WIND_ACTIVE=1.5f,WIND_REST=2.4f;
    public static final float WIND_SPEED=290,LANDING_DISTANCE=3,INTERACT_DISTANCE=48;
    public static final float COMPANION_SPEED=150,COMPANION_ARRIVAL=3,REUNION_DISTANCE=85;
    private RidgeTuning() { }
}
