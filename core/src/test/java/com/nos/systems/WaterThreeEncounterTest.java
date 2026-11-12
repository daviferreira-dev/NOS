package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.BridgeData;
import com.nos.data.BridgeTuning;
import com.nos.world.BridgeEncounter;
import org.junit.Test;
import static org.junit.Assert.*;

/** Vento percorrido pela trilha real: subida, rajada, abrigo e reunião. */
public class WaterThreeEncounterTest {
    private BridgeEncounter create() {
        BridgeData data=new Json().fromJson(BridgeData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/water-relay.json"));
        BridgeEncounter world=new BridgeEncounter(data);world.start();
        assertEquals(3,data.phaseNumber);assertTrue(world.challenge() instanceof RidgeSystem);
        assertFalse(world.hasGarden());assertFalse(world.hasRelay());assertEquals(0,world.getActive());return world;
    }
    private RidgeSystem ridge(BridgeEncounter world) { return (RidgeSystem)world.challenge(); }
    private void waitFor(BridgeEncounter world,int ticks) { for(int i=0;i<ticks;i++) world.update(0,0,BridgeTuning.FIXED_STEP); }
    private void go(BridgeEncounter world,float x,float y) {
        for(int i=0;i<7000&&world.getState()==BridgeEncounter.State.PLAYING;i++) {
            float dx=x-world.hero(0).getX(),dy=y-world.hero(0).getY(),distance=(float)Math.hypot(dx,dy);
            if(distance<3) break;world.update(dx/distance,dy/distance,BridgeTuning.FIXED_STEP);
        }
        waitFor(world,40);
        assertTrue("Percurso bloqueado rumo a "+x+","+y+"; posição "+world.hero(0).getX()+","+world.hero(0).getY(),
            Math.hypot(world.hero(0).getX()-x,world.hero(0).getY()-y)<9);
    }
    private void climb(BridgeEncounter world) {
        go(world,1180,170);go(world,1180,430);go(world,430,430);go(world,430,710);
        go(world,1030,710);go(world,1030,880);
        assertEquals(RidgeSystem.State.WAITING_WIND,ridge(world).state());assertEquals(2,world.objectiveStep());
    }
    private void enterWind(BridgeEncounter world) {
        for(int i=0;i<1600&&!ridge(world).isRiding()&&!ridge(world).hasCrossed();i++)
            world.update(0,world.hero(0).getY()<915?1:0,BridgeTuning.FIXED_STEP);
        assertTrue(ridge(world).isRiding());
    }
    private void land(BridgeEncounter world) {
        for(int i=0;i<1600&&!ridge(world).hasCrossed();i++) world.update(0,0,BridgeTuning.FIXED_STEP);
        assertTrue(ridge(world).hasCrossed());assertEquals(3,world.objectiveStep());
        assertTrue(world.hero(0).getX()>1200);assertFalse(ridge(world).allowsExit());
    }
    @Test public void fourObjectivesRequireTheRealZigzagWindRideAndPhysicalReunion() {
        BridgeEncounter world=create();assertEquals(4,world.objectiveCount());assertEquals(1,world.objectiveStep());
        assertEquals("ridgeGoalAscent",world.objectiveKey());climb(world);enterWind(world);land(world);
        go(world,1290,940);go(world,1290,1140);go(world,1370,1140);waitFor(world,300);
        assertEquals(BridgeEncounter.State.PLAYING,world.getState());
        float x=world.hero(1).getX(),y=world.hero(1).getY();world.interact();
        assertEquals(x,world.hero(1).getX(),0);assertEquals(y,world.hero(1).getY(),0);assertEquals(4,world.objectiveStep());
        waitFor(world,1800);assertTrue(ridge(world).allowsExit());assertEquals(BridgeEncounter.State.WON,world.getState());
        assertTrue(world.hero(0).getY()>1090);assertTrue(world.hero(1).getY()>1090);assertEquals(0,world.getActive());
    }
    @Test public void pauseDuringTheWindRideFreezesClockAndBothBodiesThenResumes() {
        BridgeEncounter world=create();climb(world);enterWind(world);waitFor(world,12);
        assertTrue(ridge(world).isRiding());world.togglePause();
        float x=world.hero(0).getX(),y=world.hero(0).getY(),otherX=world.hero(1).getX(),otherY=world.hero(1).getY(),time=ridge(world).time();
        for(int i=0;i<300;i++) world.update(1,1,.05f);world.interact();
        assertEquals(x,world.hero(0).getX(),0);assertEquals(y,world.hero(0).getY(),0);
        assertEquals(otherX,world.hero(1).getX(),0);assertEquals(otherY,world.hero(1).getY(),0);assertEquals(time,ridge(world).time(),0);
        assertEquals(BridgeEncounter.State.PAUSED,world.getState());world.togglePause();land(world);
    }
    @Test public void voidPreventsStraightShortcutsAndNoInputCannotCompleteTheAscent() {
        BridgeEncounter world=create();BridgeCollisionSystem collision=new BridgeCollisionSystem(world.getData());
        assertFalse(collision.clearPath(170,170,1370,1140,true));assertFalse(collision.walkable(700,300,true));
        waitFor(world,3000);assertEquals(170,world.hero(0).getX(),0);assertEquals(170,world.hero(0).getY(),0);
        assertEquals(1,world.objectiveStep());assertFalse(ridge(world).allowsExit());assertEquals(BridgeEncounter.State.PLAYING,world.getState());
    }
}
