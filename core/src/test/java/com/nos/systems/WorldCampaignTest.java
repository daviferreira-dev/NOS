package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.*;
import com.nos.world.BridgeEncounter;
import org.junit.Test;
import static org.junit.Assert.*;

/** Integra ação ambiental, cooperação e a convergência final pelos mapas distribuídos. */
public class WorldCampaignTest {
    @Test public void allDestinationsAndOpeningFramesExistInShippedAssets() {
        com.badlogic.gdx.utils.I18NBundle strings=com.badlogic.gdx.utils.I18NBundle.createBundle(new FileHandle(System.getProperty("nos.assets")+"/ui/strings"));
        for(String map:new String[]{"bridge-encounter.json","water-transfer.json","water-relay.json","iris-convergence.json"}) {
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
    private void waitForRest(BridgeEncounter world,int index) {
        HazardData hazard=world.getData().hazards[index];
        float cycle=hazard.warningSeconds+hazard.activeSeconds+hazard.restSeconds;
        float restStart=hazard.warningSeconds+hazard.activeSeconds;
        for(int i=0;i<4000;i++) {
            float local=(world.actions().time()+hazard.phase)%cycle;
            if(local>=restStart&&local<restStart+BridgeTuning.FIXED_STEP*2) return;
            world.update(0,0,BridgeTuning.FIXED_STEP);
        }
        fail("O perigo não alcançou a janela de repouso.");
    }
    @Test public void fourMapsLoadTheirOwnPhaseAndOnlyIrisCombinesBothCooperativeSystems() {
        String[] maps={"bridge-encounter.json","water-transfer.json","water-relay.json","iris-convergence.json"};
        for(int i=0;i<maps.length;i++) {
            BridgeEncounter world=create(maps[i]);assertEquals(i+1,world.getData().phaseNumber);
            assertEquals(i==3,world.hasGarden()&&world.hasRelay());
            assertEquals(BridgeEncounter.State.PLAYING,world.getState());assertEquals(0,world.getActive());
        }
    }
    @Test public void irisRequiresTransferThenRelayAndBothPeopleReachTheFinalExit() {
        BridgeEncounter world=create("iris-convergence.json");
        assertEquals(7,world.objectiveCount());assertEquals(1,world.objectiveStep());assertFalse(world.isRelayActive());
        assertEquals(RelayGateSystem.State.IDLE,world.relay().state());
        go(world,290,270);world.interact();assertTrue(world.garden().isDragging());
        go(world,658,272);world.interact();waitFor(world,200);
        assertEquals(2,world.objectiveStep());assertEquals(1,world.garden().transferOpening(),0);
        // A rota evita a jardineira e espera a janela legível da brasa antes do vão.
        go(world,658,370);go(world,740,370);go(world,740,350);waitForRest(world,0);
        go(world,740,270);go(world,930,270);go(world,982,186);world.interact();waitFor(world,1800);
        assertTrue(world.garden().isHelperHolding());assertTrue(world.garden().isTransferLatched());
        assertEquals(3,world.objectiveStep());assertFalse(world.isRelayActive());
        assertEquals(RelayGateSystem.State.IDLE,world.relay().state());
        go(world,930,270);waitForRest(world,0);go(world,760,270);go(world,760,350);
        go(world,658,370);go(world,658,272);world.interact();assertTrue(world.garden().isDragging());
        go(world,700,272);waitForRest(world,0);go(world,880,272);go(world,1178,309);waitFor(world,300);
        assertTrue(world.garden().isOpen());assertFalse(world.garden().isDragging());
        assertTrue(world.isRelayActive());assertEquals(4,world.objectiveStep());
        assertEquals("relayGoalAsk",world.objectiveKey());
        go(world,1230,248);go(world,1600,270);go(world,1622,191);world.interact();waitFor(world,1000);
        assertTrue(world.relay().isHoldingWest());assertEquals(5,world.objectiveStep());
        go(world,1770,270);go(world,1940,270);go(world,2044,191);world.interact();
        assertEquals(RelayGateSystem.State.CROSSING,world.relay().state());assertEquals(6,world.objectiveStep());
        waitFor(world,1200);assertTrue(world.relay().isLatched());assertEquals(7,world.objectiveStep());
        go(world,2140,270);waitFor(world,1200);
        assertEquals(BridgeEncounter.State.WON,world.getState());
        assertTrue(world.hero(0).getX()>=world.getData().exit[0]);assertTrue(world.hero(1).getX()>=world.getData().exit[0]);
    }
    @Test public void pauseFreezesEnvironmentalClockAndRecoveryDropsCartWithoutResidualMotion() {
        BridgeData data=read("water-transfer.json");
        HazardData ember=new HazardData();ember.x=360;ember.y=270;ember.radius=12;
        ember.warningSeconds=0;ember.activeSeconds=100;ember.restSeconds=0;
        data.hazards=new HazardData[]{ember};data.recoveryPoint=new float[]{170,255};
        BridgeEncounter world=new BridgeEncounter(data);world.start();waitFor(world,30);
        float time=world.actions().time();world.togglePause();
        for(int i=0;i<300;i++) world.update(1,0,.05f,true);
        assertEquals(time,world.actions().time(),0);assertEquals(170,world.hero(0).getX(),0);
        world.togglePause();go(world,290,270);world.interact();assertTrue(world.garden().isDragging());
        boolean recovered=false;
        for(int i=0;i<300&&!recovered;i++) { world.update(1,0,BridgeTuning.FIXED_STEP);recovered=world.actions().didRecover(); }
        assertTrue(recovered);assertFalse(world.garden().isDragging());assertEquals("actionRecovered",world.notice());
        assertEquals(170,world.hero(0).getX(),0);assertEquals(255,world.hero(0).getY(),0);
        world.update(0,0,BridgeTuning.FIXED_STEP);assertEquals(170,world.hero(0).getX(),0);
        assertEquals(BridgeEncounter.State.PLAYING,world.getState());assertFalse(world.garden().isOpen());
    }
    @Test public void sprintSpeedsUpFreeMovementButKeepsTheCartAtItsNormalDraggingRate() {
        BridgeData aData=read("bridge-encounter.json"),bData=read("bridge-encounter.json");
        aData.hazards=null;bData.hazards=null;
        BridgeEncounter walking=new BridgeEncounter(aData),running=new BridgeEncounter(bData);walking.start();running.start();
        for(int i=0;i<120;i++) { walking.update(1,0,BridgeTuning.FIXED_STEP);running.update(1,0,BridgeTuning.FIXED_STEP,true); }
        assertTrue(running.hero(0).getX()>walking.hero(0).getX()+40);
        aData=read("water-transfer.json");bData=read("water-transfer.json");aData.hazards=null;bData.hazards=null;
        walking=new BridgeEncounter(aData);running=new BridgeEncounter(bData);walking.start();running.start();
        go(walking,290,270);go(running,290,270);walking.interact();running.interact();
        for(int i=0;i<120;i++) { walking.update(1,0,BridgeTuning.FIXED_STEP);running.update(1,0,BridgeTuning.FIXED_STEP,true); }
        assertTrue(walking.garden().isDragging());assertTrue(running.garden().isDragging());
        assertEquals(walking.garden().cartX(),running.garden().cartX(),.001f);
    }
}
