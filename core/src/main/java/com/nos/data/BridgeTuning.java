package com.nos.data;

/** Valores do encontro da ponte, sem pressão de tempo. */
public final class BridgeTuning {
    public static final float SPEED=145, RADIUS=10, FOLLOW_DISTANCE=65, FOLLOW_SPEED=165;
    public static final float ACCELERATION=18, STOP_ACCELERATION=28, INTERACT_DISTANCE=48;
    public static final float SUPPORT_SPEED=1.4f, OPEN_THRESHOLD=.999f, COLLISION_STEP=3;
    public static final float FIXED_STEP=1f/120f;
    public static final float WALK_FRAME_PER_UNIT=.045f;
    private BridgeTuning() { }
}
