package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.*;
import com.nos.entities.PuzzleHero;
import com.nos.world.BridgeEncounter;
import org.junit.Test;
import static org.junit.Assert.*;

public class BridgeEncounterTest {
    private BridgeEncounter create() {
        return new BridgeEncounter(new Json().fromJson(BridgeData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/bridge-encounter.json")));
    }
    private void waitFor(BridgeEncounter run,int steps) { for(int i=0;i<steps;i++) run.update(0,0,BridgeTuning.FIXED_STEP); }
    private void go(BridgeEncounter run,float x,float y) {
        for(int i=0;i<6000&&run.getState()==BridgeEncounter.State.PLAYING;i++) {
            PuzzleHero hero=run.hero(run.getActive());float dx=x-hero.getX(),dy=y-hero.getY();
            float d=(float)Math.sqrt(dx*dx+dy*dy);if(d<8) break;
            run.update(dx/d,dy/d,BridgeTuning.FIXED_STEP);
        }
        waitFor(run,40);
    }
    private void tie(BridgeEncounter run) { run.start();go(run,535,270);run.interact();waitFor(run,120); }
    @Test public void closedBridgeReallyBlocksWaterAndOpenBridgeDoesNotAllowWalkingAnywhere() {
        BridgeEncounter run=create();BridgeCollisionSystem c=new BridgeCollisionSystem(run.getData());
        assertFalse(c.walkable(750,270,false));assertTrue(c.walkable(750,270,true));
        assertFalse(c.walkable(750,360,true));assertFalse(c.walkable(50,270,true));
        run.start();go(run,800,270);assertTrue(run.hero(0).getX()<600-BridgeTuning.RADIUS+1);
    }
    @Test public void tyingNearMechanismHoldsOneAndAutomaticallyHandsOverControl() {
        BridgeEncounter run=create();tie(run);
        assertEquals(0,run.getBound());assertEquals(1,run.getActive());assertTrue(run.isOpen());
        float x=run.hero(0).getX(),y=run.hero(0).getY();go(run,800,270);
        assertEquals(x,run.hero(0).getX(),0);assertEquals(y,run.hero(0).getY(),0);
        assertTrue(run.hero(1).getX()>600);
    }
    @Test public void interactionAtWrongLocationDoesNotOpenBridge() {
        BridgeEncounter run=create();run.start();run.interact();waitFor(run,200);
        assertEquals(-1,run.getBound());assertFalse(run.isOpen());assertFalse(run.isLocked());
    }
    @Test public void supportAloneIsNotPermanentButFarLatchSecuresIt() {
        BridgeEncounter run=create();tie(run);go(run,980,270);run.interact();
        assertTrue(run.isLocked());assertEquals(-1,run.getBound());waitFor(run,200);
        assertTrue(run.isOpen());assertTrue(run.hero(0).getX()>535);
    }
    @Test public void withdrawingSupportReturnsStrandedCompanionToSafeGroundWithoutDeath() {
        BridgeEncounter run=create();tie(run);go(run,750,270);run.swap();run.interact();waitFor(run,200);
        assertFalse(run.isLocked());assertFalse(run.isOpen());
        BridgeCollisionSystem c=new BridgeCollisionSystem(run.getData());
        assertTrue(c.walkable(run.hero(0).getX(),run.hero(0).getY(),false));
        assertTrue(c.walkable(run.hero(1).getX(),run.hero(1).getY(),false));
        assertEquals(BridgeEncounter.State.PLAYING,run.getState());
    }
    @Test public void fullEncounterCanBeSolvedWithActualMovementAndContextualActions() {
        BridgeEncounter run=create();tie(run);go(run,980,270);run.interact();go(run,1480,270);
        waitFor(run,300);
        assertEquals(BridgeEncounter.State.WON,run.getState());
        assertTrue(run.hero(0).getX()>=run.getData().exit[0]);
        assertTrue(run.hero(1).getX()>=run.getData().exit[0]);
    }
    @Test public void noInputCannotSolveEncounterAndPauseStopsSimulation() {
        BridgeEncounter run=create();run.start();waitFor(run,1200);assertFalse(run.isLocked());
        assertEquals(BridgeEncounter.State.PLAYING,run.getState());
        run.togglePause();float x=run.hero(0).getX();run.update(1,0,.05f);
        assertEquals(x,run.hero(0).getX(),0);assertEquals(BridgeEncounter.State.PAUSED,run.getState());
    }
    @Test public void releasingMovementSettlesAndChangingCharacterDoesNotKeepPreviousVelocity() {
        BridgeEncounter run=create();run.start();go(run,440,270);
        float x=run.hero(0).getX();waitFor(run,120);assertEquals(x,run.hero(0).getX(),.2f);
        run.swap();assertEquals(1,run.getActive());
        float next=run.hero(1).getX();waitFor(run,120);assertEquals(next,run.hero(1).getX(),.2f);
    }
    @Test public void nearbyPromptMatchesActionAndHoldingStatusMatchesActualPartner() {
        BridgeEncounter run=create();run.start();assertNull(run.interactionPoint());
        go(run,535,270);assertSame(run.getData().winch,run.interactionPoint());
        assertEquals("bridgeTieAction",run.action());run.interact();waitFor(run,120);
        assertEquals("bridgeShadowHolding",run.companionStatus());
        run.swap();assertEquals("bridgeActiveHolding",run.companionStatus());run.swap();
        go(run,980,270);assertSame(run.getData().latch,run.interactionPoint());
        assertEquals("bridgeFixAction",run.action());run.interact();assertNull(run.interactionPoint());
    }
    @Test public void bothArriveEvenWhenLeaderLeavesBridgeDiagonallyAndCirclesFarRock() {
        BridgeEncounter run=create();tie(run);go(run,980,270);run.interact();
        go(run,1090,320);go(run,1280,320);go(run,1470,360);waitFor(run,600);
        assertEquals(BridgeEncounter.State.WON,run.getState());
    }
    @Test public void pushingAgainstClosedRiverDoesNotKeepPlayingWalkFrames() {
        BridgeEncounter run=create();run.start();go(run,800,270);
        int frame=run.frame(0);
        for(int i=0;i<200;i++) run.update(1,0,BridgeTuning.FIXED_STEP);
        assertFalse(run.hero(0).isMoving());assertEquals(frame,run.frame(0));
    }
}
