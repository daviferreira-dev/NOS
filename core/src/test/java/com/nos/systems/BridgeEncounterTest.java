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
        BridgeData data=new Json().fromJson(BridgeData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/bridge-encounter.json"));
        data.garden=null;
        data.exit=new float[]{1370,205,150,180};
        return new BridgeEncounter(data);
    }
    private void waitFor(BridgeEncounter run,int steps) { for(int i=0;i<steps;i++) run.update(0,0,BridgeTuning.FIXED_STEP); }
    private void go(BridgeEncounter run,float x,float y) {
        for(int i=0;i<6000&&run.getState()==BridgeEncounter.State.PLAYING;i++) {
            PuzzleHero hero=run.hero(0);float dx=x-hero.getX(),dy=y-hero.getY();
            float d=(float)Math.sqrt(dx*dx+dy*dy);if(d<8) break;
            run.update(dx/d,dy/d,BridgeTuning.FIXED_STEP);
        }
        waitFor(run,40);
    }
    private void requestHelp(BridgeEncounter run) { run.start();go(run,493,270);run.interact(); }
    private void hold(BridgeEncounter run) {
        requestHelp(run);waitFor(run,600);
        assertTrue("Reflexo deve chegar ao guincho e abrir a ponte",run.isOpen());
    }
    @Test public void closedWaterAndBridgeEdgesRespectVisibleGround() {
        BridgeEncounter run=create();BridgeCollisionSystem c=new BridgeCollisionSystem(run.getData());
        assertFalse(c.walkable(750,270,false));assertTrue(c.walkable(750,270,true));
        assertFalse(c.walkable(750,360,true));assertFalse(c.walkable(50,270,true));
        run.start();go(run,800,270);assertTrue(run.hero(0).getX()<600-BridgeTuning.RADIUS+1);
    }
    @Test public void helpRequestDoesNotTeleportEitherPersonOrHandOverControl() {
        BridgeEncounter run=create();run.start();go(run,493,270);
        float playerX=run.hero(0).getX(),companionX=run.hero(1).getX();run.interact();
        assertEquals(0,run.getActive());assertEquals(-1,run.getBound());assertFalse(run.isOpen());
        assertEquals(playerX,run.hero(0).getX(),0);assertEquals(companionX,run.hero(1).getX(),0);
        assertEquals(CompanionBehaviorSystem.State.APPROACHING_WINCH,run.getCompanionState());
        run.update(0,0,.05f);assertTrue(Math.abs(run.hero(1).getX()-companionX)<=BridgeTuning.COMPANION_APPROACH_SPEED*.05f+.01f);
        waitFor(run,600);assertEquals(1,run.getBound());assertTrue(run.isOpen());assertEquals(0,run.getActive());
    }
    @Test public void companionHoldsWhilePlayerCrossesButNeverFixesFarLatchAlone() {
        BridgeEncounter run=create();hold(run);
        float x=run.hero(1).getX(),y=run.hero(1).getY();go(run,800,270);waitFor(run,600);
        assertEquals(x,run.hero(1).getX(),0);assertEquals(y,run.hero(1).getY(),0);
        assertTrue(run.hero(0).getX()>600);assertFalse(run.isLocked());assertEquals(0,run.getActive());
    }
    @Test public void interactionAwayFromMechanismsCannotAskForHelpOrOpenBridge() {
        BridgeEncounter run=create();run.start();run.interact();waitFor(run,300);
        assertEquals(-1,run.getBound());assertFalse(run.isOpen());assertFalse(run.isLocked());
        assertEquals(CompanionBehaviorSystem.State.FOLLOWING,run.getCompanionState());
    }
    @Test public void fixingFarLatchFreesCompanionAndKeepsBridgeOpen() {
        BridgeEncounter run=create();hold(run);go(run,980,270);run.interact();
        assertTrue(run.isLocked());assertEquals(-1,run.getBound());
        assertEquals(CompanionBehaviorSystem.State.REJOINING,run.getCompanionState());
        waitFor(run,600);assertTrue(run.isOpen());assertTrue(run.hero(1).getX()>600);
    }
    @Test public void playerCanCancelLocallyButCannotReleaseCompanionFromMiddleOfBridge() {
        BridgeEncounter run=create();hold(run);go(run,750,270);run.interact();
        assertEquals(1,run.getBound());assertTrue(run.isOpen());
        go(run,493,270);assertEquals("bridgeCallBackAction",run.action());run.interact();waitFor(run,300);
        assertEquals(-1,run.getBound());assertFalse(run.isOpen());assertFalse(run.isLocked());
    }
    @Test public void requestingAgainWhileCompanionIsOnTheWayCancelsTheRequest() {
        BridgeEncounter run=create();requestHelp(run);run.interact();waitFor(run,600);
        assertEquals(-1,run.getBound());assertFalse(run.isOpen());
        assertEquals(CompanionBehaviorSystem.State.FOLLOWING,run.getCompanionState());
    }
    @Test public void companionWaitsForSpaceWhenPlayerStandsOnWinch() {
        BridgeEncounter run=create();run.start();go(run,535,270);run.interact();waitFor(run,600);
        assertEquals(-1,run.getBound());assertEquals("bridgeGoalMakeRoom",run.objectiveKey());
        go(run,580,270);waitFor(run,500);assertEquals(1,run.getBound());assertTrue(run.isOpen());
    }
    @Test public void fullEncounterCanBeSolvedByControllingOnlyShadow() {
        BridgeEncounter run=create();hold(run);go(run,980,270);run.interact();go(run,1480,270);waitFor(run,600);
        assertEquals(BridgeEncounter.State.WON,run.getState());assertEquals(0,run.getActive());
        assertTrue(run.hero(0).getX()>=run.getData().exit[0]);assertTrue(run.hero(1).getX()>=run.getData().exit[0]);
    }
    @Test public void bothArriveWhenShadowLeavesBridgeDiagonallyAndCirclesFarRock() {
        BridgeEncounter run=create();hold(run);go(run,980,270);run.interact();
        go(run,1090,320);go(run,1280,320);go(run,1470,360);waitFor(run,900);
        assertEquals(BridgeEncounter.State.WON,run.getState());
    }
    @Test public void noInputCannotSolveEncounterAndPauseFreezesBothPeople() {
        BridgeEncounter run=create();run.start();waitFor(run,1200);assertFalse(run.isLocked());
        go(run,493,270);run.interact();run.togglePause();float x=run.hero(0).getX(),other=run.hero(1).getX();
        for(int i=0;i<600;i++) run.update(1,0,.05f);
        assertEquals(x,run.hero(0).getX(),0);assertEquals(other,run.hero(1).getX(),0);
        assertEquals(BridgeEncounter.State.PAUSED,run.getState());run.togglePause();waitFor(run,600);assertTrue(run.isOpen());
    }
    @Test public void releasingMovementSettlesAndCollisionDoesNotPlayWalkingFrames() {
        BridgeEncounter run=create();run.start();go(run,440,270);
        float x=run.hero(0).getX();waitFor(run,120);assertEquals(x,run.hero(0).getX(),.2f);
        go(run,800,270);int frame=run.frame(0);
        for(int i=0;i<200;i++) run.update(1,0,BridgeTuning.FIXED_STEP);
        assertFalse(run.hero(0).isMoving());assertEquals(frame,run.frame(0));
    }
    @Test public void contextualPromptsAndObjectivesTrackCooperationNotAnExpiredNotice() {
        BridgeEncounter run=create();assertNull(run.notice());assertEquals(1,run.objectiveStep());
        run.start();assertNull(run.interactionPoint());go(run,493,270);
        assertSame(run.getData().winch,run.interactionPoint());assertEquals("bridgeAskHelpAction",run.action());
        run.interact();assertEquals("bridgeGoalWaiting",run.objectiveKey());waitFor(run,600);
        assertEquals(2,run.objectiveStep());assertEquals("bridgeCompanionHolding",run.companionStatus());
        go(run,980,270);assertSame(run.getData().latch,run.interactionPoint());assertEquals("bridgeFixAction",run.action());
        run.interact();assertEquals(3,run.objectiveStep());assertNull(run.interactionPoint());
        waitFor(run,1200);assertNull(run.notice());assertEquals("bridgeGoal3",run.objectiveKey());
    }
    @Test public void returningFromUnexpectedWaterKeepsRunRecoverable() {
        BridgeEncounter run=create();run.start();run.hero(0).setPosition(750,270);waitFor(run,10);
        assertTrue(new BridgeCollisionSystem(run.getData()).walkable(run.hero(0).getX(),run.hero(0).getY(),false));
        assertEquals(BridgeEncounter.State.PLAYING,run.getState());
    }
}
