package com.nos.data;

import com.badlogic.gdx.Preferences;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class SaveDataTest {
    @Test public void checkpointSurvivesReloadAndOnlyNewAdvancesFlush() {
        MemoryPreferences preferences=new MemoryPreferences();
        SaveData save=new SaveData(preferences);
        assertEquals(SaveData.JOURNEY_START,save.journeyProgress());
        assertFalse(save.canResumeJourney());assertEquals(0,preferences.flushes);
        save.recordGardenCheckpoint();save.recordGardenCheckpoint();
        assertEquals(1,preferences.flushes);
        SaveData reloaded=new SaveData(preferences);
        assertTrue(reloaded.canResumeJourney());assertFalse(reloaded.isJourneyComplete());
        reloaded.recordJourneyComplete();reloaded.recordJourneyComplete();reloaded.recordGardenCheckpoint();
        assertEquals(2,preferences.flushes);
        assertEquals(SaveData.JOURNEY_COMPLETE,new SaveData(preferences).journeyProgress());
    }

    @Test public void replayNeverDowngradesCompletionOrChangesLegacyScore() {
        MemoryPreferences preferences=new MemoryPreferences();
        SaveData save=new SaveData(preferences);
        save.record(720);save.recordJourneyComplete();
        int flushes=preferences.flushes;
        SaveData replay=new SaveData(preferences);
        replay.recordGardenCheckpoint();replay.record(300);
        assertEquals(flushes,preferences.flushes);
        assertEquals(720,replay.bestScore());assertTrue(replay.isJourneyComplete());
        replay.record(900);
        assertEquals(900,new SaveData(preferences).bestScore());
        assertEquals(SaveData.JOURNEY_COMPLETE,new SaveData(preferences).journeyProgress());
        assertEquals(flushes+1,preferences.flushes);
    }

    @Test public void legacyRecordDoesNotUnlockCheckpoint() {
        MemoryPreferences preferences=new MemoryPreferences();
        SaveData save=new SaveData(preferences);save.record(9999);
        assertEquals(SaveData.JOURNEY_START,new SaveData(preferences).journeyProgress());
        assertFalse(save.canResumeJourney());
    }

    @Test public void invalidSavedValuesNeverSkipToGardenOrCompletion() {
        Object[] invalid={-1,3,Integer.MIN_VALUE,Integer.MAX_VALUE,"corrompido",2.0f};
        for(Object value:invalid) {
            MemoryPreferences preferences=new MemoryPreferences();
            preferences.values.put("water01.journey",value);
            SaveData save=new SaveData(preferences);
            assertEquals("Valor inválido: "+value,SaveData.JOURNEY_START,save.journeyProgress());
            assertFalse(save.canResumeJourney());assertFalse(save.isJourneyComplete());
            assertEquals(0,preferences.flushes);
            save.recordGardenCheckpoint();
            assertEquals(SaveData.GARDEN_REACHED,new SaveData(preferences).journeyProgress());
            assertEquals(1,preferences.flushes);
        }
    }

    @Test public void nullBackendIsIsolatedMemoryWithoutApplicationPreferences() {
        SaveData smoke=new SaveData(null);smoke.recordGardenCheckpoint();smoke.recordJourneyComplete();smoke.record(99);
        assertTrue(smoke.isJourneyComplete());assertEquals(99,smoke.bestScore());
        SaveData anotherSmoke=new SaveData(null);
        assertEquals(SaveData.JOURNEY_START,anotherSmoke.journeyProgress());assertEquals(0,anotherSmoke.bestScore());
        assertFalse(anotherSmoke.canResumeJourney());
    }

    @Test public void secondJourneyRequiresFirstCompletionAndSurvivesReload() {
        MemoryPreferences preferences=new MemoryPreferences();
        SaveData save=new SaveData(preferences);
        save.recordSecondJourneyComplete();
        assertFalse(save.isSecondJourneyAvailable());assertFalse(save.isSecondJourneyComplete());
        assertEquals(0,save.menuProgress());assertEquals(0,preferences.flushes);
        save.recordGardenCheckpoint();save.recordSecondJourneyComplete();
        assertEquals(1,save.menuProgress());assertFalse(save.isSecondJourneyAvailable());
        assertEquals(1,preferences.flushes);
        save.recordJourneyComplete();
        assertTrue(save.isSecondJourneyAvailable());assertFalse(save.isSecondJourneyComplete());
        assertEquals(2,save.menuProgress());
        save.recordSecondJourneyComplete();
        SaveData reloaded=new SaveData(preferences);
        assertTrue(reloaded.isSecondJourneyComplete());assertEquals(3,reloaded.menuProgress());
        assertEquals(SaveData.JOURNEY_COMPLETE,reloaded.journeyProgress());
        assertEquals(3,preferences.flushes);
        reloaded.recordGardenCheckpoint();reloaded.recordJourneyComplete();reloaded.recordSecondJourneyComplete();
        assertEquals(3,preferences.flushes);assertEquals(3,reloaded.menuProgress());
    }

    @Test public void orphanSecondCompletionDoesNotUnlockOrSkipFirstJourney() {
        Object[] invalidFirst={-1,3,Integer.MAX_VALUE,"corrompido",2.0f};
        for(Object value:invalidFirst) {
            MemoryPreferences preferences=new MemoryPreferences();
            preferences.values.put("water01.journey",value);
            preferences.values.put("water02.journey",1);
            SaveData save=new SaveData(preferences);
            assertFalse(save.isSecondJourneyAvailable());assertFalse(save.isSecondJourneyComplete());
            assertEquals(0,save.menuProgress());assertEquals(0,preferences.flushes);
        }
        MemoryPreferences preferences=new MemoryPreferences();
        preferences.values.put("water01.journey",SaveData.GARDEN_REACHED);
        preferences.values.put("water02.journey",1);
        SaveData save=new SaveData(preferences);
        assertFalse(save.isSecondJourneyAvailable());assertFalse(save.isSecondJourneyComplete());
        assertEquals(1,save.menuProgress());
    }

    @Test public void invalidSecondValuesNeverGrantCompletionAndCanBeRepairedByFinishing() {
        Object[] invalid={-1,2,Integer.MIN_VALUE,Integer.MAX_VALUE,"corrompido",1.0f,true};
        for(Object value:invalid) {
            MemoryPreferences preferences=new MemoryPreferences();
            preferences.values.put("water01.journey",SaveData.JOURNEY_COMPLETE);
            preferences.values.put("water02.journey",value);
            SaveData save=new SaveData(preferences);
            assertTrue(save.isSecondJourneyAvailable());assertFalse(save.isSecondJourneyComplete());
            assertEquals(2,save.menuProgress());assertEquals(0,preferences.flushes);
            save.recordSecondJourneyComplete();
            assertTrue(new SaveData(preferences).isSecondJourneyComplete());assertEquals(1,preferences.flushes);
        }
    }

    @Test public void secondJourneyMemoryDoesNotLeakBetweenSmokeApplications() {
        SaveData first=new SaveData(null);first.recordJourneyComplete();first.recordSecondJourneyComplete();
        assertEquals(3,first.menuProgress());assertTrue(first.isSecondJourneyComplete());
        SaveData second=new SaveData(null);
        assertEquals(0,second.menuProgress());assertFalse(second.isSecondJourneyComplete());
    }

    @Test public void thirdJourneyRequiresBothEarlierCompletionsAndSurvivesReload() {
        MemoryPreferences preferences=new MemoryPreferences();
        SaveData save=new SaveData(preferences);
        save.recordThirdJourneyComplete();
        assertFalse(save.isThirdJourneyAvailable());assertFalse(save.isThirdJourneyComplete());
        assertEquals(0,preferences.flushes);
        save.recordJourneyComplete();save.recordThirdJourneyComplete();
        assertFalse(save.isThirdJourneyAvailable());assertEquals(2,save.menuProgress());
        assertEquals(1,preferences.flushes);
        save.recordSecondJourneyComplete();
        assertTrue(save.isThirdJourneyAvailable());assertFalse(save.isThirdJourneyComplete());
        assertEquals(3,save.menuProgress());
        save.recordThirdJourneyComplete();
        SaveData reloaded=new SaveData(preferences);
        assertTrue(reloaded.isThirdJourneyComplete());assertEquals(4,reloaded.menuProgress());
        assertEquals(SaveData.JOURNEY_COMPLETE,reloaded.journeyProgress());
        assertTrue(reloaded.isSecondJourneyComplete());assertEquals(3,preferences.flushes);
        reloaded.recordGardenCheckpoint();reloaded.recordJourneyComplete();
        reloaded.recordSecondJourneyComplete();reloaded.recordThirdJourneyComplete();
        assertEquals(3,preferences.flushes);assertEquals(4,reloaded.menuProgress());
    }

    @Test public void orphanThirdCompletionCannotBypassMissingOrInvalidEarlierProgress() {
        Object[] invalidSecond={0,-1,2,Integer.MAX_VALUE,"corrompido",1.0f};
        for(Object value:invalidSecond) {
            MemoryPreferences preferences=new MemoryPreferences();
            preferences.values.put("water01.journey",SaveData.JOURNEY_COMPLETE);
            preferences.values.put("water02.journey",value);
            preferences.values.put("water03.journey",1);
            SaveData save=new SaveData(preferences);
            assertFalse(save.isThirdJourneyAvailable());assertFalse(save.isThirdJourneyComplete());
            assertEquals(2,save.menuProgress());assertEquals(0,preferences.flushes);
            save.recordThirdJourneyComplete();assertEquals(0,preferences.flushes);
        }
        MemoryPreferences preferences=new MemoryPreferences();
        preferences.values.put("water01.journey","corrompido");
        preferences.values.put("water02.journey",1);preferences.values.put("water03.journey",1);
        SaveData save=new SaveData(preferences);
        assertFalse(save.isThirdJourneyAvailable());assertFalse(save.isThirdJourneyComplete());
        assertEquals(0,save.menuProgress());assertEquals(0,preferences.flushes);
    }

    @Test public void invalidThirdCompletionKeepsThirdAvailableWithoutSkippingIt() {
        Object[] invalid={-1,2,Integer.MIN_VALUE,Integer.MAX_VALUE,"corrompido",1.0f,true};
        for(Object value:invalid) {
            MemoryPreferences preferences=new MemoryPreferences();
            preferences.values.put("water01.journey",SaveData.JOURNEY_COMPLETE);
            preferences.values.put("water02.journey",1);preferences.values.put("water03.journey",value);
            SaveData save=new SaveData(preferences);
            assertTrue(save.isThirdJourneyAvailable());assertFalse(save.isThirdJourneyComplete());
            assertEquals(3,save.menuProgress());assertEquals(0,preferences.flushes);
            save.recordThirdJourneyComplete();
            assertTrue(new SaveData(preferences).isThirdJourneyComplete());assertEquals(1,preferences.flushes);
        }
    }

    @Test public void thirdJourneyMemoryIsIsolatedAndKeepsLegacyScore() {
        SaveData first=new SaveData(null);first.record(450);
        first.recordJourneyComplete();first.recordSecondJourneyComplete();first.recordThirdJourneyComplete();
        assertEquals(4,first.menuProgress());assertTrue(first.isThirdJourneyComplete());
        assertEquals(450,first.bestScore());
        SaveData another=new SaveData(null);
        assertEquals(0,another.menuProgress());assertFalse(another.isThirdJourneyAvailable());
        assertFalse(another.isThirdJourneyComplete());assertEquals(0,another.bestScore());
    }

    @Test public void fourthJourneyRequiresEarlierCompletionAndPersistsFinalProgress() {
        MemoryPreferences preferences=new MemoryPreferences();
        SaveData save=new SaveData(preferences);save.record(620);
        save.recordFourthJourneyComplete();assertEquals(0,save.menuProgress());
        save.recordJourneyComplete();save.recordSecondJourneyComplete();
        save.recordFourthJourneyComplete();assertEquals(3,save.menuProgress());
        assertFalse(save.isFourthJourneyAvailable());
        save.recordThirdJourneyComplete();
        assertTrue(save.isFourthJourneyAvailable());assertFalse(save.isFourthJourneyComplete());
        assertEquals(4,save.menuProgress());
        save.recordFourthJourneyComplete();
        SaveData reloaded=new SaveData(preferences);
        assertEquals(5,reloaded.menuProgress());assertTrue(reloaded.isFourthJourneyComplete());
        assertEquals(620,reloaded.bestScore());assertTrue(reloaded.isThirdJourneyComplete());
        int flushes=preferences.flushes;
        reloaded.recordGardenCheckpoint();reloaded.recordJourneyComplete();
        reloaded.recordSecondJourneyComplete();reloaded.recordThirdJourneyComplete();reloaded.recordFourthJourneyComplete();
        assertEquals(flushes,preferences.flushes);assertEquals(5,reloaded.menuProgress());
    }

    @Test public void orphanFourthCompletionCannotSkipInvalidEarlierPhase() {
        Object[] invalidThird={0,-1,2,Integer.MAX_VALUE,"corrompido",1.0f};
        for(Object value:invalidThird) {
            MemoryPreferences preferences=new MemoryPreferences();
            preferences.values.put("water01.journey",SaveData.JOURNEY_COMPLETE);
            preferences.values.put("water02.journey",1);preferences.values.put("water03.journey",value);
            preferences.values.put("water04.journey",1);
            SaveData save=new SaveData(preferences);
            assertFalse(save.isFourthJourneyAvailable());assertFalse(save.isFourthJourneyComplete());
            assertEquals(3,save.menuProgress());assertEquals(0,preferences.flushes);
            save.recordFourthJourneyComplete();assertEquals(0,preferences.flushes);
        }
    }

    @Test public void invalidFourthFlagDoesNotFinishGameAndIsRepairedOnlyByCompletion() {
        Object[] invalid={-1,2,Integer.MIN_VALUE,Integer.MAX_VALUE,"corrompido",1.0f,true};
        for(Object value:invalid) {
            MemoryPreferences preferences=new MemoryPreferences();
            preferences.values.put("water01.journey",SaveData.JOURNEY_COMPLETE);
            preferences.values.put("water02.journey",1);preferences.values.put("water03.journey",1);
            preferences.values.put("water04.journey",value);
            SaveData save=new SaveData(preferences);
            assertTrue(save.isFourthJourneyAvailable());assertFalse(save.isFourthJourneyComplete());
            assertEquals(4,save.menuProgress());assertEquals(0,preferences.flushes);
            save.recordFourthJourneyComplete();
            assertTrue(new SaveData(preferences).isFourthJourneyComplete());assertEquals(1,preferences.flushes);
        }
    }

    @Test public void finalProgressInMemoryDoesNotLeakToAnotherApplication() {
        SaveData save=new SaveData(null);save.recordJourneyComplete();save.recordSecondJourneyComplete();
        save.recordThirdJourneyComplete();save.recordFourthJourneyComplete();
        assertEquals(5,save.menuProgress());assertTrue(save.isFourthJourneyComplete());
        SaveData isolated=new SaveData(null);
        assertEquals(0,isolated.menuProgress());assertFalse(isolated.isFourthJourneyComplete());
    }

    private static final class MemoryPreferences implements Preferences {
        final Map<String,Object> values=new HashMap<>();int flushes;
        @Override public Preferences putBoolean(String key,boolean value) { values.put(key,value);return this; }
        @Override public Preferences putInteger(String key,int value) { values.put(key,value);return this; }
        @Override public Preferences putLong(String key,long value) { values.put(key,value);return this; }
        @Override public Preferences putFloat(String key,float value) { values.put(key,value);return this; }
        @Override public Preferences putString(String key,String value) { values.put(key,value);return this; }
        @Override public Preferences put(Map<String,?> entries) { values.putAll(entries);return this; }
        @Override public boolean getBoolean(String key) { return getBoolean(key,false); }
        @Override public int getInteger(String key) { return getInteger(key,0); }
        @Override public long getLong(String key) { return getLong(key,0); }
        @Override public float getFloat(String key) { return getFloat(key,0); }
        @Override public String getString(String key) { return getString(key,""); }
        @Override public boolean getBoolean(String key,boolean fallback) { return values.containsKey(key)?(Boolean)values.get(key):fallback; }
        @Override public int getInteger(String key,int fallback) { return values.containsKey(key)?(Integer)values.get(key):fallback; }
        @Override public long getLong(String key,long fallback) { return values.containsKey(key)?(Long)values.get(key):fallback; }
        @Override public float getFloat(String key,float fallback) { return values.containsKey(key)?(Float)values.get(key):fallback; }
        @Override public String getString(String key,String fallback) { return values.containsKey(key)?(String)values.get(key):fallback; }
        @Override public Map<String,?> get() { return new HashMap<>(values); }
        @Override public boolean contains(String key) { return values.containsKey(key); }
        @Override public void clear() { values.clear(); }
        @Override public void remove(String key) { values.remove(key); }
        @Override public void flush() { flushes++; }
    }
}
