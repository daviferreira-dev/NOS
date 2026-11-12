package com.nos.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Recorde legado e avanço da travessia; repetir um trecho nunca apaga o progresso. */
public final class SaveData {
    public static final int JOURNEY_START=0,GARDEN_REACHED=1,JOURNEY_COMPLETE=2;
    private static final String JOURNEY_KEY="water01.journey";
    private static final String SECOND_JOURNEY_KEY="water02.journey";
    private static final String THIRD_JOURNEY_KEY="water03.journey";
    private static final String FOURTH_JOURNEY_KEY="water04.journey";
    private final Preferences preferences;
    private int memoryBest,memoryJourney,memorySecondJourney,memoryThirdJourney,memoryFourthJourney;
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
    /** A segunda fase só existe no avanço depois da conclusão válida da primeira. */
    public boolean isSecondJourneyAvailable() { return isJourneyComplete(); }
    public boolean isSecondJourneyComplete() {
        if(!isSecondJourneyAvailable()) return false;
        int value;
        try { value=preferences==null?memorySecondJourney:preferences.getInteger(SECOND_JOURNEY_KEY,0); }
        catch(RuntimeException invalidValue) { return false; }
        return value==1;
    }
    /** Avanço órfão da terceira fase não substitui a conclusão válida das anteriores. */
    public boolean isThirdJourneyAvailable() { return isSecondJourneyComplete(); }
    public boolean isThirdJourneyComplete() {
        if(!isThirdJourneyAvailable()) return false;
        int value;
        try { value=preferences==null?memoryThirdJourney:preferences.getInteger(THIRD_JOURNEY_KEY,0); }
        catch(RuntimeException invalidValue) { return false; }
        return value==1;
    }
    public boolean isFourthJourneyAvailable() { return isThirdJourneyComplete(); }
    public boolean isFourthJourneyComplete() {
        if(!isFourthJourneyAvailable()) return false;
        int value;
        try { value=preferences==null?memoryFourthJourney:preferences.getInteger(FOURTH_JOURNEY_KEY,0); }
        catch(RuntimeException invalidValue) { return false; }
        return value==1;
    }
    public int menuProgress() {
        if(isFourthJourneyComplete()) return 5;
        if(isThirdJourneyComplete()) return 4;
        if(isSecondJourneyComplete()) return 3;
        return journeyProgress();
    }
    public void recordGardenCheckpoint() { advanceJourney(GARDEN_REACHED); }
    public void recordJourneyComplete() { advanceJourney(JOURNEY_COMPLETE); }
    public void recordSecondJourneyComplete() {
        if(!isSecondJourneyAvailable()||isSecondJourneyComplete()) return;
        if(preferences==null) memorySecondJourney=1;
        else { preferences.putInteger(SECOND_JOURNEY_KEY,1);preferences.flush(); }
    }
    public void recordThirdJourneyComplete() {
        if(!isThirdJourneyAvailable()||isThirdJourneyComplete()) return;
        if(preferences==null) memoryThirdJourney=1;
        else { preferences.putInteger(THIRD_JOURNEY_KEY,1);preferences.flush(); }
    }
    public void recordFourthJourneyComplete() {
        if(!isFourthJourneyAvailable()||isFourthJourneyComplete()) return;
        if(preferences==null) memoryFourthJourney=1;
        else { preferences.putInteger(FOURTH_JOURNEY_KEY,1);preferences.flush(); }
    }
    private void advanceJourney(int value) {
        if(value<=journeyProgress()) return;
        if(preferences==null) memoryJourney=value;
        else { preferences.putInteger(JOURNEY_KEY,value);preferences.flush(); }
    }
}
