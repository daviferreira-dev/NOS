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
