package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.*;
import com.nos.entities.PuzzleHero;
import org.junit.Test;
import static org.junit.Assert.*;

/** Colisão da trilha e transporte físico da rajada, sem contexto gráfico. */
public class RidgeSystemTest {
    private BridgeData data() {
        return new Json().fromJson(BridgeData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/water-relay.json"));
    }
    @Test public void voidAndClosedWindPassageCannotBeCrossedByWalking() {
        BridgeData data=data();BridgeCollisionSystem collision=new BridgeCollisionSystem(data);
        assertTrue(collision.walkable(1180,300,true));assertTrue(collision.walkable(700,430,true));
        assertFalse(collision.walkable(700,300,true));assertFalse(collision.walkable(850,900,true));
        assertFalse(collision.clearPath(1030,940,1260,940,true));
        assertFalse(collision.boxWalkableIgnoringCart(1130,940,42,30,true));
        collision.setRidgePassageOpen(true);assertTrue(collision.clearPath(1030,940,1260,940,true));
        assertTrue(collision.boxWalkableIgnoringCart(1130,940,42,30,true));
    }
    @Test public void windRequiresLaunchZoneAndThenMovesContinuouslyToTheLanding() {
        BridgeData data=data();BridgeCollisionSystem collision=new BridgeCollisionSystem(data);
        RidgeSystem ridge=new RidgeSystem(data,collision);
        PuzzleHero player=new PuzzleHero(1030,850),other=new PuzzleHero(1030,790);
        for(int i=0;i<1000;i++) ridge.update(player,other,BridgeTuning.FIXED_STEP);
        assertEquals(RidgeSystem.State.WAITING_WIND,ridge.state());assertEquals(1030,player.getX(),0);
        // Fixture da borda de lançamento; o transporte seguinte precisa atravessar todo o piso.
        player.setPosition(1030,920);boolean rode=false;
        for(int i=0;i<1500&&!ridge.hasCrossed();i++) {
            float x=player.getX(),y=player.getY();ridge.update(player,other,BridgeTuning.FIXED_STEP);
            rode|=ridge.isRiding();
            assertTrue(Math.hypot(player.getX()-x,player.getY()-y)<=RidgeTuning.WIND_SPEED*BridgeTuning.FIXED_STEP+.01f);
            assertTrue(collision.walkable(player.getX(),player.getY(),true));
        }
        assertTrue(rode);assertTrue(ridge.hasCrossed());assertFalse(ridge.allowsExit());
        assertTrue(Math.hypot(player.getX()-1260,player.getY()-940)<=RidgeTuning.LANDING_DISTANCE);
        assertFalse(ridge.interact(player,other));assertEquals("ridgeGoalShelter",ridge.objectiveKey());
    }
    @Test public void callingFromShelterDoesNotTeleportCompanionAndExitWaitsForTheirReunion() {
        BridgeData data=data();BridgeCollisionSystem collision=new BridgeCollisionSystem(data);
        RidgeSystem ridge=new RidgeSystem(data,collision);
        PuzzleHero player=new PuzzleHero(1030,920),other=new PuzzleHero(1030,850);
        for(int i=0;i<1600&&!ridge.hasCrossed();i++) ridge.update(player,other,BridgeTuning.FIXED_STEP);
        assertTrue(ridge.hasCrossed());player.setPosition(1370,1140);
        float x=other.getX(),y=other.getY();assertTrue(ridge.interact(player,other));
        assertEquals(x,other.getX(),0);assertEquals(y,other.getY(),0);assertFalse(ridge.allowsExit());
        for(int i=0;i<1600&&!ridge.allowsExit();i++) {
            float oldX=other.getX(),oldY=other.getY();ridge.update(player,other,BridgeTuning.FIXED_STEP);
            assertTrue(Math.hypot(other.getX()-oldX,other.getY()-oldY)<=RidgeTuning.COMPANION_SPEED*BridgeTuning.FIXED_STEP+.01f);
            assertTrue(collision.walkable(other.getX(),other.getY(),true));
        }
        assertTrue(ridge.allowsExit());assertEquals(4,ridge.objectiveStep());assertFalse(ridge.controlsCompanion());
    }
    @Test public void zeroIntervalFreezesTheWindAndInvalidIntervalsAreRejected() {
        BridgeData data=data();RidgeSystem ridge=new RidgeSystem(data,new BridgeCollisionSystem(data));
        PuzzleHero player=new PuzzleHero(1030,920),other=new PuzzleHero(1030,850);
        ridge.update(player,other,0);assertEquals(0,ridge.time(),0);assertEquals(RidgeSystem.State.ASCENDING,ridge.state());
        for(float delta:new float[]{-.1f,.2f,Float.NaN,Float.POSITIVE_INFINITY}) {
            try { ridge.update(player,other,delta);fail("Intervalo inválido foi aceito."); }
            catch(IllegalArgumentException expected) { }
        }
    }
}
