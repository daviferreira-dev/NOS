package com.nos.data;

import org.junit.Test;
import static org.junit.Assert.*;

public class JourneyProgressTest {
    @Test public void resumeSelectsOnlyTheLatestUnlockedPlayableStage() {
        SaveData save=new SaveData(null);
        assertEquals(JourneyProgress.Stage.BRIDGE,JourneyProgress.start(save,true));
        save.recordGardenCheckpoint();
        assertEquals(JourneyProgress.Stage.GARDEN,JourneyProgress.start(save,true));
        save.recordJourneyComplete();
        assertEquals(JourneyProgress.Stage.WATER_TWO,JourneyProgress.start(save,true));
        save.recordSecondJourneyComplete();
        assertEquals(JourneyProgress.Stage.WATER_THREE,JourneyProgress.start(save,true));
        save.recordThirdJourneyComplete();
        assertEquals(JourneyProgress.Stage.WATER_THREE,JourneyProgress.start(save,true));
    }

    @Test public void explicitFreshStartDoesNotModifySavedAdvance() {
        SaveData save=new SaveData(null);save.recordJourneyComplete();save.recordSecondJourneyComplete();save.recordThirdJourneyComplete();
        assertEquals(JourneyProgress.Stage.BRIDGE,JourneyProgress.start(save,false));
        assertEquals(4,save.menuProgress());assertTrue(save.isSecondJourneyComplete());assertTrue(save.isThirdJourneyComplete());
        assertEquals(JourneyProgress.Stage.WATER_THREE,JourneyProgress.start(save,true));
    }

    @Test public void isolatedOrMissingProgressAlwaysStartsAtBridge() {
        assertEquals(JourneyProgress.Stage.BRIDGE,JourneyProgress.start(null,true));
        assertEquals(JourneyProgress.Stage.BRIDGE,JourneyProgress.start(null,false));
        SaveData save=new SaveData(null);save.recordSecondJourneyComplete();save.recordThirdJourneyComplete();
        assertEquals(JourneyProgress.Stage.BRIDGE,JourneyProgress.start(save,true));
    }

    @Test public void freshStartBeforeFinishingThirdKeepsItAvailable() {
        SaveData save=new SaveData(null);save.recordJourneyComplete();save.recordSecondJourneyComplete();
        assertEquals(JourneyProgress.Stage.BRIDGE,JourneyProgress.start(save,false));
        assertEquals(3,save.menuProgress());assertFalse(save.isThirdJourneyComplete());
        assertEquals(JourneyProgress.Stage.WATER_THREE,JourneyProgress.start(save,true));
    }
}
