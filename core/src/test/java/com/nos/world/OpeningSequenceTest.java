package com.nos.world;

import com.nos.data.OpeningTuning;
import org.junit.Test;
import static org.junit.Assert.*;

public class OpeningSequenceTest {
    @Test public void fiveShotsUseDistinctScenesAndFinishAtMenu() {
        assertEquals(5,OpeningTuning.SHOT_BACKGROUNDS.length);
        assertEquals(5,new java.util.HashSet<String>(java.util.Arrays.asList(OpeningTuning.SHOT_BACKGROUNDS)).size());
        OpeningSequence run=new OpeningSequence();advance(run,600);
        assertEquals(OpeningSequence.State.TITLE,run.getState());
    }
    private void advance(OpeningSequence run,int steps) { for(int i=0;i<steps;i++) run.update(.05f); }
    @Test public void naturalEndingWaitsAtTitleIndefinitely() {
        OpeningSequence run=new OpeningSequence();advance(run,12000);
        assertEquals(OpeningSequence.State.TITLE,run.getState());assertEquals(run.duration(),run.getTime(),0);
    }
    @Test public void skipOnlyReachesMenuAndNeedsSeparateExplicitStart() {
        OpeningSequence run=new OpeningSequence();run.skip();advance(run,2000);
        assertEquals(OpeningSequence.State.TITLE,run.getState());
        run.start();assertEquals(OpeningSequence.State.STARTING,run.getState());
        advance(run,30);assertEquals(OpeningSequence.State.READY_TO_PLAY,run.getState());
    }
    @Test public void cannotStartWhileMovieIsPlayingAndPauseStopsClock() {
        OpeningSequence run=new OpeningSequence();run.start();assertEquals(OpeningSequence.State.CUTSCENE,run.getState());
        run.setPaused(true);advance(run,1000);assertEquals(0,run.getTime(),0);
        run.setPaused(false);advance(run,20);assertTrue(run.getTime()>0);
    }
    @Test public void replayReturnsToFirstShotWithoutStartingGameplay() {
        OpeningSequence run=new OpeningSequence();run.skip();run.replay();
        assertEquals(OpeningSequence.State.CUTSCENE,run.getState());assertEquals(0,run.getTime(),0);
        advance(run,1000);assertEquals(OpeningSequence.State.TITLE,run.getState());
    }
    @Test public void shotsAndFadesStayInRangeThroughoutSequence() {
        OpeningSequence run=new OpeningSequence();
        for(int i=0;i<500;i++) {
            assertTrue(run.shot()>=0&&run.shot()<OpeningTuning.SHOT_ENDS.length);
            assertTrue(run.sceneVisibility()>=0&&run.sceneVisibility()<=1);
            assertTrue(run.shotProgress()>=0&&run.shotProgress()<=1);
            run.update(.05f);
        }
    }
}
