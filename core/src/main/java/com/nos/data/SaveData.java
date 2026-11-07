package com.nos.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Recorde legado e avanço da travessia; repetir um trecho nunca apaga o progresso. */
public final class SaveData {
    public static final int JOURNEY_START=0,GARDEN_REACHED=1,JOURNEY_COMPLETE=2;
    private static final String JOURNEY_KEY="water01.journey";
    private final Preferences preferences;
    private int memoryBest,memoryJourney;
    public SaveData() { this(Gdx.app.getPreferences("com.nos.progress")); }
    /** Um backend nulo mantém somente memória, para inspeção e testes isolados. */
    public SaveData(Preferences preferences) { this.preferences=preferences; }
    public int bestScore() { return preferences==null?memoryBest:preferences.getInteger("water01.best",0); }
    public void record(int score) {
        if (score > bestScore()) {
            if(preferences==null) memoryBest=score;
            else { preferences.putInteger("water01.best", score);preferences.flush(); }
        }
    }
    public int journeyProgress() {
        int value;
        try { value=preferences==null?memoryJourney:preferences.getInteger(JOURNEY_KEY,JOURNEY_START); }
        catch(RuntimeException invalidValue) { return JOURNEY_START; }
        return value>=JOURNEY_START&&value<=JOURNEY_COMPLETE?value:JOURNEY_START;
    }
    public boolean canResumeJourney() { return journeyProgress()>=GARDEN_REACHED; }
    public boolean isJourneyComplete() { return journeyProgress()==JOURNEY_COMPLETE; }
    public void recordGardenCheckpoint() { advanceJourney(GARDEN_REACHED); }
    public void recordJourneyComplete() { advanceJourney(JOURNEY_COMPLETE); }
    private void advanceJourney(int value) {
        if(value<=journeyProgress()) return;
        if(preferences==null) memoryJourney=value;
        else { preferences.putInteger(JOURNEY_KEY,value);preferences.flush(); }
    }
}
