package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.*;
import com.nos.world.BridgeEncounter;
import org.junit.Test;
import static org.junit.Assert.*;

/** Integra ação ambiental, cooperação e a convergência final pelos mapas distribuídos. */
public class WorldCampaignTest {
    @Test public void emberHintWaitsForTheWelcomeAndOnlyAppearsOnce() {
        BridgeEncounter world=create("water-transfer.json");
        go(world,540,260);
        assertEquals("furnaceWelcome",world.notice());
        for(int i=0;i<700 && !"emberApproach".equals(world.notice());i++) world.update(0,0,BridgeTuning.FIXED_STEP);
        assertEquals("emberApproach",world.notice());
        waitFor(world,700);
        assertNull(world.notice());
        go(world,320,260);
        go(world,540,260);
        assertNull(world.notice());
    }
    @Test public void allDestinationsAndOpeningFramesExistInShippedAssets() {
        com.badlogic.gdx.utils.I18NBundle strings=com.badlogic.gdx.utils.I18NBundle.createBundle(new FileHandle(System.getProperty("nos.assets")+"/ui/strings"));
        for(String map:new String[]{"river-meeting.json","water-transfer.json","water-relay.json","iris-convergence.json"}) {
            BridgeData data=read(map);assertFalse(strings.get(data.destinationKey).isEmpty());
            if(data.backgroundPath!=null) assertTrue(new FileHandle(System.getProperty("nos.assets")+"/"+data.backgroundPath).exists());
        }
        for(String frame:OpeningTuning.SHOT_BACKGROUNDS) assertTrue(new FileHandle(System.getProperty("nos.assets")+"/"+frame).exists());
    }
    private BridgeData read(String name) {
        return new Json().fromJson(BridgeData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/"+name));
    }
    private BridgeEncounter create(String name) { BridgeEncounter world=new BridgeEncounter(read(name));world.start();return world; }
    private void waitFor(BridgeEncounter world,int ticks) { for(int i=0;i<ticks;i++) world.update(0,0,BridgeTuning.FIXED_STEP); }
    private void go(BridgeEncounter world,float x,float y) {
        for(int i=0;i<7000&&world.getState()==BridgeEncounter.State.PLAYING;i++) {
            float dx=x-world.hero(0).getX(),dy=y-world.hero(0).getY(),distance=(float)Math.hypot(dx,dy);
            if(distance<3) break;
            world.update(dx/distance,dy/distance,BridgeTuning.FIXED_STEP);
        }
        waitFor(world,40);
        assertTrue("Percurso bloqueado rumo a "+x+","+y+"; posição "+world.hero(0).getX()+","+world.hero(0).getY(),
            world.getState()==BridgeEncounter.State.WON||Math.hypot(world.hero(0).getX()-x,world.hero(0).getY()-y)<9);
    }
    @Test public void fourMapsLoadTheirOwnPhaseAndEachNewEncounterHasItsOwnRules() {
        String[] maps={"river-meeting.json","water-transfer.json","water-relay.json","iris-convergence.json"};
        for(int i=0;i<maps.length;i++) {
            BridgeEncounter world=create(maps[i]);assertEquals(i+1,world.getData().phaseNumber);
            assertEquals(i==1,world.challenge() instanceof FurnaceSystem);
            assertEquals(i==2,world.challenge() instanceof RidgeSystem);
            assertEquals(i==3,world.challenge() instanceof ObservatorySystem);
            assertFalse(world.hasGarden());assertFalse(world.hasRelay());
            assertEquals(BridgeEncounter.State.PLAYING,world.getState());assertEquals(0,world.getActive());
        }
    }
    @Test public void waterFinishesWithTheActualReunionOnTheFarBank() {
        BridgeEncounter world=create("river-meeting.json");
        go(world,493,270);
        world.interact();
        waitFor(world,600);
        assertTrue(world.isOpen());
        go(world,980,270);
        world.interact();
        assertTrue(world.isLocked());
        assertFalse(world.getState()==BridgeEncounter.State.WON);
        go(world,1080,270);
        waitFor(world,900);
        assertWaterReunion(world);
    }
    @Test public void oldWaterCheckpointResumesWithTheBridgeLockedAndReflectionStillApproaching() {
        BridgeEncounter world=new BridgeEncounter(read("river-meeting.json"));
        world.startAtGardenCheckpoint();
        assertEquals(BridgeEncounter.State.PLAYING,world.getState());
        assertTrue(world.isLocked());
        assertTrue(world.hero(1).getX()<910);
        waitFor(world,900);
        assertWaterReunion(world);
    }
    private void assertWaterReunion(BridgeEncounter world) {
        assertEquals(BridgeEncounter.State.WON,world.getState());
        assertTrue(world.hero(1).getX()>=910);
        assertTrue(Math.hypot(world.hero(1).getX()-world.hero(0).getX(),world.hero(1).getY()-world.hero(0).getY())
            <=BridgeTuning.COMPANION_START_DISTANCE);
    }
    @Test public void irisRequiresObservationAndSeparatePhysicalPathsToTheCore() {
        BridgeEncounter world=create("iris-convergence.json");
        assertEquals(4,world.objectiveCount());assertEquals(1,world.objectiveStep());
        waitFor(world,500);assertEquals(786,world.hero(1).getX(),0);assertEquals(270,world.hero(1).getY(),0);
        go(world,422,270);waitFor(world,1200);
        assertEquals(2,world.objectiveStep());assertEquals("observatoryGoalChoose",world.objectiveKey());
        world.interact();assertEquals("observatoryWrongPassage",world.notice());
        assertEquals(BridgeEncounter.State.PLAYING,world.getState());
        go(world,438.59f,353.42f);go(world,485.85f,424.15f);go(world,556.58f,471.41f);go(world,640,484);
        world.interact();assertEquals(3,world.objectiveStep());
        go(world,640,270);waitFor(world,1800);
        assertEquals(BridgeEncounter.State.WON,world.getState());
        assertEquals(4,world.objectiveStep());
        assertTrue(Math.hypot(world.hero(1).getX()-668,world.hero(1).getY()-270)<2.1f);
    }
    @Test public void irisPauseFreezesGalleryAndResumesTheSamePhysicalJourney() {
        BridgeEncounter world=create("iris-convergence.json");go(world,422,270);
        float x=world.hero(1).getX(),y=world.hero(1).getY();world.togglePause();
        for(int i=0;i<500;i++) world.update(1,0,.05f,true);
        assertEquals(x,world.hero(1).getX(),0);assertEquals(y,world.hero(1).getY(),0);
        world.togglePause();waitFor(world,1200);
        assertEquals("observatoryGoalChoose",world.objectiveKey());assertEquals(BridgeEncounter.State.PLAYING,world.getState());
    }
    @Test public void pauseFreezesEnvironmentalClockAndRecoveryDropsCartWithoutResidualMotion() {
        BridgeData data=read("bridge-encounter.json");
        HazardData ember=new HazardData();ember.x=1160;ember.y=270;ember.radius=12;
        ember.warningSeconds=0;ember.activeSeconds=100;ember.restSeconds=0;
        data.hazards=new HazardData[]{ember};data.recoveryPoint=new float[]{1000,255};data.localRecoveryMargin=0;
        BridgeEncounter world=new BridgeEncounter(data);world.startAtGardenCheckpoint();waitFor(world,30);
        float time=world.actions().time();world.togglePause();
        for(int i=0;i<300;i++) world.update(1,0,.05f,true);
        assertEquals(time,world.actions().time(),0);assertEquals(1000,world.hero(0).getX(),0);
        world.togglePause();go(world,1080,270);world.interact();assertTrue(world.garden().isDragging());
        boolean recovered=false;
        for(int i=0;i<300&&!recovered;i++) { world.update(1,0,BridgeTuning.FIXED_STEP);recovered=world.actions().didRecover(); }
        assertTrue(recovered);assertFalse(world.garden().isDragging());assertEquals("actionRecovered",world.notice());
        assertEquals(1000,world.hero(0).getX(),0);assertEquals(255,world.hero(0).getY(),0);
        world.update(0,0,BridgeTuning.FIXED_STEP);assertEquals(1000,world.hero(0).getX(),0);
        assertEquals(BridgeEncounter.State.PLAYING,world.getState());assertFalse(world.garden().isOpen());
    }
    @Test public void sprintSpeedsUpFreeMovementButKeepsTheCartAtItsNormalDraggingRate() {
        BridgeData aData=read("bridge-encounter.json"),bData=read("bridge-encounter.json");
        aData.hazards=null;bData.hazards=null;
        BridgeEncounter walking=new BridgeEncounter(aData),running=new BridgeEncounter(bData);walking.start();running.start();
        for(int i=0;i<120;i++) { walking.update(1,0,BridgeTuning.FIXED_STEP);running.update(1,0,BridgeTuning.FIXED_STEP,true); }
        assertTrue(running.hero(0).getX()>walking.hero(0).getX()+40);
        aData=read("bridge-encounter.json");bData=read("bridge-encounter.json");aData.hazards=null;bData.hazards=null;
        walking=new BridgeEncounter(aData);running=new BridgeEncounter(bData);walking.startAtGardenCheckpoint();running.startAtGardenCheckpoint();
        go(walking,1080,270);go(running,1080,270);walking.interact();running.interact();
        for(int i=0;i<120;i++) { walking.update(1,0,BridgeTuning.FIXED_STEP);running.update(1,0,BridgeTuning.FIXED_STEP,true); }
        assertTrue(walking.garden().isDragging());assertTrue(running.garden().isDragging());
        assertEquals(walking.garden().cartX(),running.garden().cartX(),.001f);
    }
}
