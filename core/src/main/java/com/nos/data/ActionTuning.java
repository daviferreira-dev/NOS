package com.nos.data;

/** Ritmo da ação ambiental, com recuperação e sem dano ou gestão de barras. */
public final class ActionTuning {
    public static final float HINT_DISTANCE = 160;
    public static final float MAX_DELTA=.1f,CONTACT_STEP=1f/120f;
    public static final float RECOVERY_COOLDOWN=.8f,GUST_SPEED=140;
    public static final float SPRINT_SPEED=220;
    public static final float DIRECTION_EPSILON=.001f;
    private ActionTuning() { }
}
