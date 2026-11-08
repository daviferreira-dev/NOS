package com.nos.data;

/** Duração, transições e áreas clicáveis da abertura e do menu. */
public final class OpeningTuning {
    public static final float[] SHOT_ENDS={4,9,14,19,24};
    public static final String[] SHOT_BACKGROUNDS={"art/opening-reflection-v1.png","art/bridge-bank-v1.png","art/fire-bank-v1.png","art/wind-bank-v1.png","art/iris-convergence-v1.png"};
    public static final float SHOT_FADE=.8f,START_FADE=1.1f,MUSIC_FADE=2.5f,MUSIC_VOLUME=.20f;
    public static final float[] START={76,188,264,48},REPLAY={76,135,264,38},MUSIC={76,91,264,30};
    public static final float[] RESUME_START={76,218,264,48},FROM_BEGINNING={76,177,264,30};
    private OpeningTuning() { }
}
