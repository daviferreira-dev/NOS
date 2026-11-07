package com.nos.systems;

import com.nos.data.*;
import com.nos.entities.PuzzleHero;
import com.nos.world.BridgeEncounter;
import org.junit.Test;
import static org.junit.Assert.*;

/** A mesma jardineira abre a aproximação e depois o contrapeso de saída. */
public class GardenTransferTest {
    private BridgeData data() {
        BridgeData data=new BridgeData();data.width=1360;data.height=540;
        data.ground=new float[]{80,100,1200,300};data.river=new float[]{-500,0,0,540};
        data.bridge=new float[]{-500,200,0,100};data.winch=new float[]{90,250};data.latch=new float[]{100,250};
        data.shadowStart=new float[]{340,250};data.reflectionStart=new float[]{260,220};
        data.exit=new float[]{1180,200,80,100};data.obstacles=new float[0][];
        GardenData garden=new GardenData();data.garden=garden;
        garden.cartStart=new float[]{380,250};garden.helperPlate=new float[]{750,180,48,32};
        garden.transferPlate=new float[]{500,250,72,48};garden.transferGate=new float[]{650,200,24,100};
        garden.weightPlate=new float[]{950,240,72,48};garden.gate=new float[]{1100,200,24,100};
        garden.barriers=new float[][]{{650,100,24,100},{650,300,24,100},{1100,100,24,100},{1100,300,24,100}};
        garden.checkpointShadow=data.shadowStart;garden.checkpointReflection=data.reflectionStart;
        return data;
    }
    private BridgeEncounter world() { BridgeEncounter result=new BridgeEncounter(data());result.startAtGardenCheckpoint();return result; }
    private void waitFor(BridgeEncounter world,int ticks) { for(int i=0;i<ticks;i++) world.update(0,0,BridgeTuning.FIXED_STEP); }
    private void go(BridgeEncounter world,float x,float y) {
        for(int i=0;i<4000&&world.getState()==BridgeEncounter.State.PLAYING;i++) {
            float dx=x-world.hero(0).getX(),dy=y-world.hero(0).getY(),distance=(float)Math.hypot(dx,dy);
            if(distance<3) break;world.update(dx/distance,dy/distance,BridgeTuning.FIXED_STEP);
        }
        waitFor(world,40);
        assertTrue("Percurso bloqueado: "+world.hero(0).getX()+","+world.hero(0).getY()+" rumo a "+x+","+y,
            Math.hypot(world.hero(0).getX()-x,world.hero(0).getY()-y)<9);
    }
    private void loadTransfer(BridgeEncounter world) {
        world.interact();assertTrue(world.garden().isDragging());go(world,496,274);world.interact();waitFor(world,180);
        assertTrue(world.garden().transferPressed());assertEquals(1,world.garden().transferOpening(),0);
    }
    private void askAcross(BridgeEncounter world) {
        go(world,496,340);go(world,620,340);go(world,620,250);go(world,720,250);go(world,732,196);
        float x=world.hero(1).getX(),y=world.hero(1).getY();world.interact();
        assertEquals(x,world.hero(1).getX(),0);assertEquals(y,world.hero(1).getY(),0);
        waitFor(world,900);assertTrue(world.garden().isHelperHolding());assertTrue(world.garden().isTransferLatched());
    }
    @Test public void cartMustFirstOpenPassageAndCompanionMustPhysicallyReachItsPedal() {
        BridgeEncounter world=world();assertEquals("transferGoalLoad",world.objectiveKey());
        assertFalse(world.garden().isTransferLatched());loadTransfer(world);assertFalse(world.garden().isOpen());
        assertEquals("transferGoalHelp",world.objectiveKey());askAcross(world);assertEquals("transferGoalMove",world.objectiveKey());
        go(world,700,250);go(world,620,250);go(world,600,340);go(world,496,340);go(world,496,274);
        world.interact();assertTrue(world.garden().isDragging());go(world,620,274);go(world,946,264);
        world.interact();waitFor(world,180);assertTrue(world.garden().isOpen());assertTrue(world.garden().isTransferLatched());
        assertFalse(world.garden().transferPressed());assertEquals(1,world.garden().transferOpening(),0);
        go(world,946,340);go(world,1060,340);go(world,1060,250);go(world,1250,250);waitFor(world,1200);
        assertEquals(BridgeEncounter.State.WON,world.getState());
    }
    @Test public void transferGateBlocksPeopleAndWholeCartUntilFullyOpen() {
        BridgeData data=data();BridgeCollisionSystem collision=new BridgeCollisionSystem(data);
        assertFalse(collision.walkable(660,250,true));assertFalse(collision.boxWalkableIgnoringCart(660,250,42,30,true));
        collision.setTransferGateOpen(true);assertTrue(collision.walkable(660,250,true));
        assertTrue(collision.boxWalkableIgnoringCart(660,250,42,30,true));
        collision.setTransferGateOpen(false);assertFalse(collision.clearPath(620,250,700,250,true));
    }
    @Test public void playerAloneOnHelperCannotLatchTransferOrOpenExitEarly() {
        BridgeData data=data();data.garden.cartStart=new float[]{986,264};
        BridgeCollisionSystem collision=new BridgeCollisionSystem(data);GardenGateSystem garden=new GardenGateSystem(data,collision);
        PuzzleHero player=new PuzzleHero(774,196),other=new PuzzleHero(300,350);
        for(int i=0;i<300;i++) garden.update(player,other,true,BridgeTuning.FIXED_STEP);
        assertTrue(garden.helperPressed());assertTrue(garden.weightPressed());assertFalse(garden.isTransferLatched());
        assertFalse(garden.isOpen());assertEquals(0,garden.getOpening(),0);
    }
    @Test public void temporaryGateWaitsForBodyInPassageAndClosesAfterItLeaves() {
        BridgeData data=data();data.garden.cartStart=new float[]{536,274};
        BridgeCollisionSystem collision=new BridgeCollisionSystem(data);GardenGateSystem garden=new GardenGateSystem(data,collision);
        PuzzleHero player=new PuzzleHero(494,274),other=new PuzzleHero(300,350);
        for(int i=0;i<180;i++) garden.update(player,other,true,BridgeTuning.FIXED_STEP);
        assertTrue(garden.interact(player,other));garden.movePlayer(player,other,-100,0,true);garden.interact(player,other);
        // Posições de fixture isolam a segurança do vão; o percurso completo acima não reposiciona corpos.
        player.setPosition(660,250);
        for(int i=0;i<180;i++) garden.update(player,other,true,BridgeTuning.FIXED_STEP);
        assertEquals(1,garden.transferOpening(),0);assertTrue(collision.walkable(660,250,true));
        player.setPosition(700,250);
        for(int i=0;i<180;i++) garden.update(player,other,true,BridgeTuning.FIXED_STEP);
        assertEquals(0,garden.transferOpening(),0);assertFalse(collision.walkable(660,250,true));
    }
    @Test public void temporaryGateAlsoWaitsForCartInPassage() {
        BridgeData data=data();data.garden.cartStart=new float[]{536,274};
        BridgeCollisionSystem collision=new BridgeCollisionSystem(data);GardenGateSystem garden=new GardenGateSystem(data,collision);
        PuzzleHero player=new PuzzleHero(494,274),other=new PuzzleHero(300,350);
        for(int i=0;i<180;i++) garden.update(player,other,true,BridgeTuning.FIXED_STEP);
        assertTrue(garden.interact(player,other));garden.movePlayer(player,other,124,0,true);
        for(int i=0;i<180;i++) garden.update(player,other,true,BridgeTuning.FIXED_STEP);
        assertEquals(660,garden.cartX(),.1f);assertEquals(1,garden.transferOpening(),0);
    }
    @Test public void pauseFreezesTemporaryOpeningAndCompanionApproach() {
        BridgeEncounter world=world();world.interact();go(world,496,274);
        world.update(0,0,BridgeTuning.FIXED_STEP);world.togglePause();float opening=world.garden().transferOpening();
        float x=world.hero(1).getX(),cart=world.garden().cartX();
        for(int i=0;i<100;i++) world.update(1,0,.05f);
        assertEquals(opening,world.garden().transferOpening(),0);assertEquals(x,world.hero(1).getX(),0);assertEquals(cart,world.garden().cartX(),0);
    }
    @Test public void originalGardenHasNoTransferAndKeepsItsOriginalStages() {
        BridgeData data=data();data.garden.transferPlate=null;data.garden.transferGate=null;
        GardenGateSystem garden=new GardenGateSystem(data,new BridgeCollisionSystem(data));
        assertFalse(garden.hasTransfer());assertEquals(3,garden.objectiveStep());assertEquals("gardenGoalAsk",garden.objectiveKey());
    }
}
