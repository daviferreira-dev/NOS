package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.BridgeData;
import com.nos.data.BridgeTuning;
import com.nos.world.BridgeEncounter;
import org.junit.Test;
import static org.junit.Assert.*;

/** Água 03 percorrida pelo encontro completo, sem reposicionar nenhuma presença. */
public class WaterThreeEncounterTest {
    private BridgeEncounter create() {
        BridgeData data=new Json().fromJson(BridgeData.class,
            new FileHandle(System.getProperty("nos.assets")+"/levels/water-relay.json"));
        BridgeEncounter world=new BridgeEncounter(data);
        assertEquals(3,data.phaseNumber);assertTrue(world.hasRelay());assertFalse(world.hasGarden());
        assertEquals(BridgeEncounter.State.READY,world.getState());world.start();
        assertTrue(world.isLocked());assertEquals(0,world.getActive());
        assertEquals(170,world.hero(0).getX(),0);assertEquals(255,world.hero(0).getY(),0);
        return world;
    }
    private void waitFor(BridgeEncounter world,int ticks) {
        for(int i=0;i<ticks;i++) world.update(0,0,BridgeTuning.FIXED_STEP);
    }
    private void go(BridgeEncounter world,float x,float y) {
        for(int i=0;i<6000&&world.getState()==BridgeEncounter.State.PLAYING;i++) {
            float dx=x-world.hero(0).getX(),dy=y-world.hero(0).getY(),distance=(float)Math.hypot(dx,dy);
            if(distance<3) break;
            world.update(dx/distance,dy/distance,BridgeTuning.FIXED_STEP);
        }
        waitFor(world,40);
        assertTrue("Percurso bloqueado rumo a "+x+","+y+"; posição "+world.hero(0).getX()+","+world.hero(0).getY(),
            Math.hypot(world.hero(0).getX()-x,world.hero(0).getY()-y)<9);
    }
    private void requestWest(BridgeEncounter world) {
        go(world,602,191);
        float x=world.hero(1).getX(),y=world.hero(1).getY();world.interact();
        assertEquals(x,world.hero(1).getX(),0);assertEquals(y,world.hero(1).getY(),0);
        assertEquals(RelayGateSystem.State.APPROACHING_WEST,world.relay().state());
        waitFor(world,800);
        assertTrue(world.relay().isHoldingWest());assertEquals(1,world.relay().opening(),0);
    }
    private void reachEast(BridgeEncounter world) {
        go(world,740,270);go(world,910,270);go(world,1034,191);
        assertTrue(world.relay().eastPressed());assertTrue(world.relay().isHoldingWest());
    }
    private void handoff(BridgeEncounter world) {
        float x=world.hero(1).getX(),y=world.hero(1).getY();world.interact();
        assertEquals(x,world.hero(1).getX(),0);assertEquals(y,world.hero(1).getY(),0);
        assertEquals(RelayGateSystem.State.CROSSING,world.relay().state());
        assertFalse(world.relay().isLatched());
    }
    @Test public void realLevelRequiresFourObjectivesAndBothPeopleReachTheExitPhysically() {
        BridgeEncounter world=create();assertEquals(4,world.objectiveCount());
        assertEquals(1,world.objectiveStep());assertEquals("relayGoalAsk",world.objectiveKey());
        requestWest(world);
        assertEquals(2,world.objectiveStep());assertEquals("relayGoalCross",world.objectiveKey());
        reachEast(world);handoff(world);
        assertEquals(3,world.objectiveStep());assertEquals("relayGoalWait",world.objectiveKey());
        waitFor(world,1200);
        assertTrue(world.relay().isLatched());assertEquals(4,world.objectiveStep());
        assertEquals("relayGoalExit",world.objectiveKey());assertEquals(BridgeEncounter.State.PLAYING,world.getState());
        go(world,1490,270);waitFor(world,900);
        assertEquals(BridgeEncounter.State.WON,world.getState());assertEquals(0,world.getActive());
        assertTrue(world.hero(0).getX()>=world.getData().exit[0]);
        assertTrue(world.hero(1).getX()>=world.getData().exit[0]);assertEquals(1,world.relay().opening(),0);
    }
    @Test public void pauseDuringHandoffFreezesBothPeopleAndGateThenResumesTheCrossing() {
        BridgeEncounter world=create();requestWest(world);reachEast(world);handoff(world);waitFor(world,30);
        assertEquals(RelayGateSystem.State.CROSSING,world.relay().state());world.togglePause();
        float playerX=world.hero(0).getX(),playerY=world.hero(0).getY();
        float companionX=world.hero(1).getX(),companionY=world.hero(1).getY(),opening=world.relay().opening();
        for(int i=0;i<400;i++) world.update(1,1,.05f);
        world.interact();
        assertEquals(BridgeEncounter.State.PAUSED,world.getState());
        assertEquals(playerX,world.hero(0).getX(),0);assertEquals(playerY,world.hero(0).getY(),0);
        assertEquals(companionX,world.hero(1).getX(),0);assertEquals(companionY,world.hero(1).getY(),0);
        assertEquals(opening,world.relay().opening(),0);assertEquals(RelayGateSystem.State.CROSSING,world.relay().state());
        world.togglePause();waitFor(world,1200);
        assertTrue(world.relay().isLatched());assertEquals(BridgeEncounter.State.PLAYING,world.getState());
    }
    @Test public void leavingEastEarlyStopsCompanionAndReturningRestartsThePhysicalCrossing() {
        BridgeEncounter world=create();requestWest(world);reachEast(world);handoff(world);waitFor(world,3);
        go(world,1034,350);
        float x=world.hero(1).getX(),y=world.hero(1).getY();waitFor(world,400);
        assertEquals(x,world.hero(1).getX(),0);assertEquals(y,world.hero(1).getY(),0);
        assertFalse(world.relay().isLatched());assertEquals(0,world.relay().opening(),0);
        assertEquals(3,world.objectiveStep());assertEquals("relayGoalHold",world.objectiveKey());
        go(world,1034,191);waitFor(world,1200);
        assertTrue(world.relay().isLatched());assertEquals(4,world.objectiveStep());
        assertTrue(world.hero(1).getX()>world.getData().relay.gate[0]+world.getData().relay.gate[2]);
    }
}
