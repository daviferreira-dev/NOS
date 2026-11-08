package com.nos.systems;

import com.nos.data.*;
import com.nos.entities.PuzzleHero;
import org.junit.Test;
import static org.junit.Assert.*;

/** Contato e antecipação independem de renderização, áudio ou relógio global. */
public class EnvironmentalActionSystemTest {
    private BridgeData data(HazardData... hazards) {
        BridgeData data=new BridgeData();data.width=1000;data.height=500;
        data.ground=new float[]{0,0,1000,500};data.river=new float[]{-500,0,0,500};
        data.bridge=new float[]{-500,0,0,500};data.shadowStart=new float[]{100,100};
        data.recoveryPoint=new float[]{100,100};data.obstacles=new float[0][];data.hazards=hazards;return data;
    }
    private HazardData ember() {
        HazardData hazard=new HazardData();hazard.x=300;hazard.y=200;hazard.radius=24;
        hazard.warningSeconds=0;hazard.activeSeconds=100;hazard.restSeconds=0;return hazard;
    }
    private EnvironmentalActionSystem system(BridgeData data) { return new EnvironmentalActionSystem(data,new BridgeCollisionSystem(data)); }
    @Test public void warningPrecedesActiveWindowAndRestIsSafe() {
        HazardData hazard=ember();hazard.warningSeconds=1;hazard.activeSeconds=2;hazard.restSeconds=2;
        EnvironmentalActionSystem system=system(data(hazard));PuzzleHero player=new PuzzleHero(100,100),other=new PuzzleHero(120,100);
        assertFalse(system.isActive(0));for(int i=0;i<5;i++) system.update(player,other,.1f);
        assertEquals(.5f,system.warning(0),.001f);assertFalse(system.isActive(0));
        for(int i=0;i<6;i++) system.update(player,other,.1f);
        assertTrue(system.isActive(0));assertEquals(0,system.warning(0),0);
        for(int i=0;i<20;i++) system.update(player,other,.1f);
        assertFalse(system.isActive(0));assertEquals(0,system.warning(0),0);
    }
    @Test public void emberRecoversOnlyPlayerToSafePointAndClearsMotion() {
        EnvironmentalActionSystem system=system(data(ember()));PuzzleHero player=new PuzzleHero(300,200),other=new PuzzleHero(300,200);
        player.setMotion(1,0);system.update(player,other,.05f);
        assertTrue(system.didRecover());assertEquals(100,player.getX(),0);assertEquals(100,player.getY(),0);assertFalse(player.isMoving());
        assertEquals(300,other.getX(),0);assertEquals(200,other.getY(),0);
        system.update(player,other,.05f);assertFalse(system.didRecover());
    }
    @Test public void cooldownPreventsRepeatedRecoveryAndEventuallyExpires() {
        EnvironmentalActionSystem system=system(data(ember()));PuzzleHero player=new PuzzleHero(300,200),other=new PuzzleHero(120,100);
        system.update(player,other,.01f);assertTrue(system.didRecover());
        for(int i=0;i<7;i++) { player.setPosition(300,200);system.update(player,other,.1f);assertFalse(system.didRecover()); }
        boolean recovered=false;
        for(int i=0;i<3;i++) { player.setPosition(300,200);system.update(player,other,.1f);recovered|=system.didRecover(); }
        assertTrue(recovered);
    }
    @Test public void unsafeRecoveryPointFallsBackInsteadOfPlacingPlayerInsideStone() {
        BridgeData data=data(ember());data.recoveryPoint=new float[]{500,200};data.obstacles=new float[][]{{480,180,40,40}};
        BridgeCollisionSystem collision=new BridgeCollisionSystem(data);EnvironmentalActionSystem system=new EnvironmentalActionSystem(data,collision);
        PuzzleHero player=new PuzzleHero(300,200);system.update(player,new PuzzleHero(120,100),.05f);
        assertTrue(system.didRecover());assertEquals(100,player.getX(),0);assertTrue(collision.walkable(player.getX(),player.getY(),true));
    }
    @Test public void gustPushesThroughNormalCollisionAndCannotCrossStone() {
        HazardData gust=ember();gust.type="gust";gust.radius=100;gust.pushX=300;
        BridgeData data=data(gust);data.obstacles=new float[][]{{350,100,30,200}};
        EnvironmentalActionSystem system=system(data);PuzzleHero player=new PuzzleHero(330,200),other=new PuzzleHero(330,200);
        system.update(player,other,.1f);assertTrue(player.getX()>330);assertTrue(player.getX()<=340.01f);
        assertFalse(system.didRecover());assertEquals(330,other.getX(),0);
    }
    @Test public void fastPlayerMovementAcrossEmberIsDetectedWithoutEndpointOverlap() {
        EnvironmentalActionSystem system=system(data(ember()));PuzzleHero player=new PuzzleHero(200,200),other=new PuzzleHero(120,100);
        system.update(player,other,.01f);assertFalse(system.didRecover());player.setPosition(400,200);system.update(player,other,.1f);
        assertTrue(system.didRecover());assertEquals(100,player.getX(),0);
    }
    @Test public void movingHazardHasDataDrivenTrajectoryAndResetRestoresClock() {
        HazardData hazard=ember();hazard.amplitudeX=50;hazard.amplitudeY=20;hazard.period=4;
        EnvironmentalActionSystem system=system(data(hazard));PuzzleHero player=new PuzzleHero(100,100),other=new PuzzleHero(120,100);
        for(int i=0;i<10;i++) system.update(player,other,.1f);
        assertEquals(1,system.time(),.001f);assertEquals(350,system.hazardX(0),.001f);assertEquals(220,system.hazardY(0),.001f);
        system.reset();assertEquals(0,system.time(),0);assertEquals(300,system.hazardX(0),0);assertFalse(system.didRecover());
    }
    @Test public void zeroDeltaDoesNotAdvanceOrResolveContacts() {
        EnvironmentalActionSystem system=system(data(ember()));PuzzleHero player=new PuzzleHero(300,200);
        system.update(player,new PuzzleHero(120,100),0);assertEquals(0,system.time(),0);assertFalse(system.didRecover());assertEquals(300,player.getX(),0);
    }
    @Test public void malformedIntervalsAndHazardsAreRejected() {
        EnvironmentalActionSystem system=system(data(ember()));PuzzleHero player=new PuzzleHero(100,100);
        for(float delta:new float[]{Float.NaN,Float.POSITIVE_INFINITY,-.1f,.2f}) {
            try { system.update(player,player,delta);fail("Aceitou intervalo inválido"); } catch(IllegalArgumentException expected) { }
        }
        HazardData hazard=ember();hazard.period=Float.NaN;
        try { system(data(hazard));fail("Aceitou trajeto inválido"); } catch(IllegalArgumentException expected) { }
    }
    @Test public void legacyMapsWithoutHazardsOrRecoveryPointRemainValid() {
        BridgeData data=data();data.hazards=null;data.recoveryPoint=null;
        EnvironmentalActionSystem system=system(data);PuzzleHero player=new PuzzleHero(100,100);
        system.update(player,player,.1f);assertEquals(0,system.count());assertFalse(system.didRecover());assertEquals(.1f,system.time(),.001f);
    }
}
