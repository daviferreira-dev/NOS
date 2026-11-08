package com.nos.audio;

import com.nos.data.OpeningTuning;

/** Ganhos independentes do dispositivo de áudio, testáveis sem OpenAL. */
public final class MusicMix {
    public enum Scene { OPENING,JOURNEY,GARDEN,SILENT }
    private Scene scene=Scene.OPENING;
    private float opening,journey,garden;
    private boolean muted;
    public void setScene(Scene value) { scene=value; }
    public void setMuted(boolean value) { muted=value; }
    public boolean isMuted() { return muted; }
    public void update(float delta) {
        float step=Math.max(0,delta)/OpeningTuning.MUSIC_FADE;
        opening=approach(opening,!muted&&scene==Scene.OPENING?1:0,step);
        journey=approach(journey,!muted&&scene==Scene.JOURNEY?1:0,step);
        garden=approach(garden,!muted&&scene==Scene.GARDEN?1:0,step);
    }
    private float approach(float from,float to,float step) { return from<to?Math.min(to,from+step):Math.max(to,from-step); }
    public float opening() { return opening*OpeningTuning.MUSIC_VOLUME; }
    public float journey() { return journey*OpeningTuning.MUSIC_VOLUME; }
    public float garden() { return garden*OpeningTuning.MUSIC_VOLUME; }
}
