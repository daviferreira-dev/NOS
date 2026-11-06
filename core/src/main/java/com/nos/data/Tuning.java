package com.nos.data;

/** Valores provisórios de balanceamento para o protótipo M1. */
public final class Tuning {
    public static final float BASE_SPEED = 1f;
    public static final float GAZE_STRENGTH = 0.8f;
    public static final float GAZE_DISTANCE_EPSILON = 0.0001f;
    public static final float VIEW_WIDTH = 960f;
    public static final float VIEW_HEIGHT = 540f;
    public static final float HERO_SPEED = 55f;
    public static final float HERO_RADIUS = 9f;
    public static final float TRAIL_WIDTH = 3f;
    public static final int TRAIL_SAMPLES = 512;
    public static final float FIXED_STEP = 1f / 120f;
    public static final float INK_DRAIN = .085f;
    public static final float INK_RECOVERY = .23f;
    public static final float HARMONY_THRESHOLD = .62f;
    public static final float HARMONY_GRACE = 2f;
    public static final float HARMONY_RISE = .09f;
    public static final float HARMONY_RECOVERY = .08f;
    public static final float HIT_DAMAGE = .34f;
    public static final float HIT_INK_COST = .18f;
    public static final float HIT_PUSHBACK = 38f;
    public static final float RESPAWN_INK = .85f;
    public static final int MAX_ATTEMPTS = 3;
    public static final float SYNC_WINDOW = 4f;
    public static final float ENCOUNTER_WINDOW = 2f;
    public static final float CHECKPOINT_PROGRESS = .48f;
    public static final float RUN_TIMEOUT = 120f;

    private Tuning() {
    }
}
