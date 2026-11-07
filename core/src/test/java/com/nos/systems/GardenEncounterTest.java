package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.I18NBundle;
import com.nos.data.*;
import com.nos.world.BridgeEncounter;
import org.junit.Test;
import static org.junit.Assert.*;

/** Percursos reais no JSON distribuído, sem janela ou contexto OpenGL. */
public class GardenEncounterTest {
    private BridgeEncounter create() {
        BridgeData data=new Json().fromJson(BridgeData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/bridge-encounter.json"));
        BridgeEncounter world=new BridgeEncounter(data);world.startAtGardenCheckpoint();return world;
    }
    private void waitFor(BridgeEncounter world,int ticks) { for(int i=0;i<ticks;i++) world.update(0,0,BridgeTuning.FIXED_STEP); }
    private void go(BridgeEncounter world,float x,float y) {
        for(int i=0;i<6000&&world.getState()==BridgeEncounter.State.PLAYING;i++) {
            float dx=x-world.hero(0).getX(),dy=y-world.hero(0).getY(),distance=(float)Math.hypot(dx,dy);
            if(distance<3) break;world.update(dx/distance,dy/distance,BridgeTuning.FIXED_STEP);
        }
        waitFor(world,40);
        assertTrue("Percurso bloqueado para "+x+","+y+"; ficou "+world.hero(0).getX()+","+world.hero(0).getY(),
            Math.hypot(world.hero(0).getX()-x,world.hero(0).getY()-y)<9);
    }
    private void ask(BridgeEncounter world) {
        float[] plate=world.getData().garden.helperPlate;
        go(world,plate[0]+plate[2]/2-42,plate[1]+plate[3]/2);world.interact();waitFor(world,600);
        assertTrue(world.garden().isHelperHolding());
    }
    private void placeCart(BridgeEncounter world) {
        GardenData data=world.getData().garden;
        go(world,data.cartStart[0]-40,data.cartStart[1]);world.interact();assertTrue(world.garden().isDragging());
        go(world,data.weightPlate[0]+data.weightPlate[2]/2-40,data.weightPlate[1]+data.weightPlate[3]/2);
        world.interact();assertFalse(world.garden().isDragging());
    }
    @Test public void checkpointKeepsBridgeButDoesNotSolveGarden() {
        BridgeEncounter world=create();assertTrue(world.isLocked());assertTrue(world.isOpen());
        assertEquals(0,world.getActive());assertFalse(world.garden().isOpen());assertEquals(5,world.objectiveCount());
        assertEquals(world.getData().garden.checkpointShadow[0],world.hero(0).getX(),0);
    }
    @Test public void helperMustWalkAndWaitForPlayerToLeavePedal() {
        BridgeEncounter world=create();float[] plate=world.getData().garden.helperPlate;
        go(world,plate[0]+plate[2]/2,plate[1]+plate[3]/2);
        float x=world.hero(1).getX(),y=world.hero(1).getY();world.interact();
        assertEquals(x,world.hero(1).getX(),0);assertEquals(y,world.hero(1).getY(),0);
        waitFor(world,400);assertFalse(world.garden().isHelperHolding());
        assertEquals("gardenGoalMakeRoom",world.objectiveKey());
        go(world,plate[0]+plate[2]/2-44,plate[1]+plate[3]/2);waitFor(world,400);
        assertTrue(world.garden().isHelperHolding());assertFalse(world.garden().isOpen());
        world.interact();assertFalse(world.garden().isHelperRequested());
    }
    @Test public void twoPeopleAreInsufficientForWideCounterweight() {
        BridgeEncounter world=create();ask(world);
        float[] weight=world.getData().garden.weightPlate;go(world,weight[0]+weight[2]/2,weight[1]+weight[3]/2);
        waitFor(world,300);assertFalse(world.garden().weightPressed());assertFalse(world.garden().isOpen());
    }
    @Test public void cartCanBePushedAndPulledAndPauseFreezesBothBodiesAndCart() {
        BridgeEncounter world=create();go(world,1080,270);world.interact();
        go(world,1140,270);assertTrue(world.garden().cartX()>1165);
        go(world,1080,270);assertEquals(1120,world.garden().cartX(),9);
        float x=world.hero(0).getX(),cart=world.garden().cartX(),other=world.hero(1).getX();
        world.togglePause();for(int i=0;i<120;i++) world.update(1,1,.05f);world.interact();
        assertEquals(x,world.hero(0).getX(),0);assertEquals(cart,world.garden().cartX(),0);assertEquals(other,world.hero(1).getX(),0);
        world.togglePause();assertTrue(world.garden().isDragging());
    }
    @Test public void completeGardenOpensPermanentlyAndBothWalkThroughGate() {
        BridgeEncounter world=create();ask(world);placeCart(world);waitFor(world,300);
        assertTrue(world.garden().isOpen());assertFalse(world.garden().isHelperRequested());
        go(world,1236,248);go(world,1490,270);waitFor(world,900);
        assertEquals(BridgeEncounter.State.WON,world.getState());assertTrue(world.garden().isOpen());assertTrue(world.isLocked());
        assertTrue(world.hero(1).getX()>world.getData().garden.gate[0]+world.getData().garden.gate[2]);
    }
    @Test public void movingBoxCannotCrossClosedGateOrStaticBeds() {
        BridgeEncounter world=create();BridgeCollisionSystem collision=new BridgeCollisionSystem(world.getData());
        assertFalse(collision.boxWalkableIgnoringCart(1390,270,42,30,true));
        assertFalse(collision.boxWalkableIgnoringCart(1400,195,42,30,true));
        assertFalse(collision.boxWalkableIgnoringCart(750,350,42,30,true));
        collision.setGardenGateOpen(true);assertTrue(collision.boxWalkableIgnoringCart(1390,270,42,30,true));
    }
    @Test public void cartCannotTunnelThroughCompanionEvenWithLargeMovement() {
        BridgeEncounter world=create();BridgeCollisionSystem collision=new BridgeCollisionSystem(world.getData());
        GardenGateSystem garden=new GardenGateSystem(world.getData(),collision);
        com.nos.entities.PuzzleHero player=new com.nos.entities.PuzzleHero(1078,270),other=new com.nos.entities.PuzzleHero(1160,270);
        assertTrue(garden.interact(player,other));garden.movePlayer(player,other,200,0,true);
        assertTrue(garden.cartX()<=1129.1f);assertEquals(270,garden.cartY(),0);
    }
    @Test public void cartMovementHasSameRateAcrossFrameSizes() {
        BridgeEncounter a=create(),b=create();go(a,1080,270);go(b,1080,270);a.interact();b.interact();
        for(int i=0;i<60;i++) a.update(1,0,1f/60);
        for(int i=0;i<120;i++) b.update(1,0,1f/120);
        assertEquals(a.garden().cartX(),b.garden().cartX(),1);
    }
    @Test public void realLevelCanBeSolvedFromBridgeStartThroughGardenWithoutCheckpointSkip() {
        BridgeData data=new Json().fromJson(BridgeData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/bridge-encounter.json"));
        BridgeEncounter world=new BridgeEncounter(data);world.start();go(world,493,270);world.interact();waitFor(world,600);
        assertTrue(world.isOpen());go(world,980,270);world.interact();assertTrue(world.isLocked());
        ask(world);placeCart(world);waitFor(world,300);assertTrue(world.garden().isOpen());
        go(world,1236,248);go(world,1490,270);waitFor(world,900);assertEquals(BridgeEncounter.State.WON,world.getState());
    }
    @Test public void gardenTextAndDynamicStageCounterArePresent() {
        I18NBundle text=I18NBundle.createBundle(new FileHandle(System.getProperty("nos.assets")+"/ui/strings"));
        for(String key:new String[]{"gardenGrabAction","gardenReleaseAction","gardenAskHelpAction","gardenCancelHelpAction",
            "gardenGoalAsk","gardenGoalWeight","gardenGoalExit","gardenGoalWaiting","gardenGoalMakeRoom"}) {
            assertFalse(text.get(key).isEmpty());if(key.startsWith("gardenGoal")) assertFalse(text.get(key+"Compact").isEmpty());
        }
        assertTrue(text.format("bridgeStageCounter",3,5).contains("5"));
        assertTrue(text.format("bridgeStageCounter",3,3).contains("3"));
    }
}
