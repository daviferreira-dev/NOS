package com.nos.data;

/** Valores do encontro da ponte, sem pressão de tempo. */
public final class BridgeTuning {
    public static final float SPEED=145, RADIUS=10, FOLLOW_DISTANCE=65, FOLLOW_SPEED=165;
    public static final float ACCELERATION=18, STOP_ACCELERATION=28, INTERACT_DISTANCE=48;
    public static final float SUPPORT_SPEED=1.4f, OPEN_THRESHOLD=.999f, COLLISION_STEP=3;
    public static final float FIXED_STEP=1f/120f;
    public static final float WALK_FRAME_PER_UNIT=.045f;
    public static final float FOLLOW_GRID=20, FOLLOW_REPATH=.3f, FOLLOW_WAYPOINT=2;
    public static final float NOTICE_DURATION=4.5f;
    public static final float COMPANION_START_DISTANCE=96,COMPANION_STOP_DISTANCE=64;
    public static final float COMPANION_CATCHUP_DISTANCE=150,COMPANION_WALK_SPEED=132;
    public static final float COMPANION_REACTION=.4f,COMPANION_OBSERVE_TIME=2.4f;
    public static final float COMPANION_OBSERVE_MAX_DISTANCE=190,COMPANION_RELEASE_DELAY=.55f;
    public static final float COMPANION_WINCH_RADIUS=3,COMPANION_APPROACH_SPEED=145;
    public static final float COMPANION_OBSERVE_PLAYER_RANGE=170,COMPANION_OBSERVE_OWN_RANGE=250;
    public static final float COMPANION_CLEARANCE=36;
    private BridgeTuning() { }
}
