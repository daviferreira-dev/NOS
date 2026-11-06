package com.nos.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Guarda somente o recorde local da fase. */
public final class SaveData {
    private final Preferences preferences = Gdx.app.getPreferences("com.nos.progress");
    public int bestScore() { return preferences.getInteger("water01.best", 0); }
    public void record(int score) {
        if (score > bestScore()) {
            preferences.putInteger("water01.best", score);
            preferences.flush();
        }
    }
}
