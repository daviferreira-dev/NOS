package com.nos.systems;

import com.nos.data.*;
import com.nos.entities.PuzzleHero;
import org.junit.Test;
import static org.junit.Assert.*;

/** Revezamento percorrido pelo piso real, sem selecionar nem reposicionar Reflexo. */
public class RelayGateSystemTest {
    private BridgeData data() {
        BridgeData data=new BridgeData();data.width=1600;data.height=540;
        data.ground=new float[]{60,145,1480,265};data.river=new float[]{-500,0,0,540};
        data.bridge=new float[]{-500,230,0,95};data.shadowStart=new float[]{170,255};data.reflectionStart=new float[]{240,300};
        data.obstacles=new float[0][];data.relay=new RelayData();
        data.relay.westPlate=new float[]{620,175,48,32};data.relay.eastPlate=new float[]{1010,175,48,32};
        data.relay.gate=new float[]{820,230,24,95};data.relay.reunionPoint=new float[]{1034,290};
        data.relay.barriers=new float[][]{{790,145,90,85},{790,325,90,85}};
        return data;
    }
    private static final class Run {
        final BridgeData data;
        final BridgeCollisionSystem collision;
        final RelayGateSystem relay;
        final CompanionFollowSystem follow;
        final PuzzleHero player,other;
        Run(BridgeData data) {
            this.data=data;collision=new BridgeCollisionSystem(data);relay=new RelayGateSystem(data,collision);
            follow=new CompanionFollowSystem(data,collision);
            player=new PuzzleHero(data.shadowStart[0],data.shadowStart[1]);other=new PuzzleHero(data.reflectionStart[0],data.reflectionStart[1]);
        }
        void tick() {
            if(!relay.controlsCompanion()) follow.update(other,player,true,BridgeTuning.FIXED_STEP);
            relay.update(player,other,true,BridgeTuning.FIXED_STEP);
            assertTrue(collision.walkable(player.getX(),player.getY(),true));
            assertTrue(collision.walkable(other.getX(),other.getY(),true));
        }
        void waitFor(int ticks) { for(int i=0;i<ticks;i++) tick(); }
        void go(float x,float y) {
            for(int i=0;i<4000;i++) {
                float dx=x-player.getX(),dy=y-player.getY(),d=(float)Math.hypot(dx,dy);
                if(d<1) return;
                float distance=Math.min(d,BridgeTuning.SPEED*BridgeTuning.FIXED_STEP);
                collision.move(player,dx/d*distance,dy/d*distance,true);tick();
            }
            fail("Percurso bloqueado: "+player.getX()+","+player.getY()+" rumo a "+x+","+y);
        }
        void requestWest() {
            float[] west=data.relay.westPlate;
            go(west[0]+west[2]/2-42,west[1]+west[3]/2);
            assertTrue(relay.interact(player,other));waitFor(800);
            assertEquals(RelayGateSystem.State.HOLDING_WEST,relay.state());assertEquals(1,relay.opening(),0);
        }
        void reachEast() {
            go(740,270);go(910,270);
            float[] east=data.relay.eastPlate;go(east[0]+east[2]/2,east[1]+east[3]/2);
        }
    }
    @Test public void completeRelayRequiresPhysicalHandoffAndBothCanLeave() {
        Run run=new Run(data());assertEquals("relayGoalAsk",run.relay.objectiveKey());run.requestWest();run.reachEast();
        float x=run.other.getX(),y=run.other.getY();assertTrue(run.relay.interact(run.player,run.other));
        assertEquals(x,run.other.getX(),0);assertEquals(y,run.other.getY(),0);assertFalse(run.relay.isLatched());
        run.waitFor(1000);assertTrue(run.relay.isLatched());assertFalse(run.relay.controlsCompanion());
        assertEquals(4,run.relay.objectiveStep());assertEquals("relayGoalExit",run.relay.objectiveKey());
        run.go(1490,270);run.waitFor(600);assertTrue(run.other.getX()>1410);assertEquals(1,run.relay.opening(),0);
    }
    @Test public void npcWaitsWhenPlayerLeavesEastAndResumesWhenPressureReturns() {
        Run run=new Run(data());run.requestWest();run.reachEast();assertTrue(run.relay.interact(run.player,run.other));run.waitFor(3);
        run.go(1034,350);float x=run.other.getX(),y=run.other.getY();run.waitFor(400);
        assertEquals(x,run.other.getX(),0);assertEquals(y,run.other.getY(),0);assertFalse(run.relay.isLatched());
        assertEquals(0,run.relay.opening(),0);assertEquals("relayGoalHold",run.relay.objectiveKey());
        run.go(1034,191);run.waitFor(1200);assertTrue(run.relay.isLatched());
    }
    @Test public void westRequestWaitsForPlayerSpaceAndCanOnlyBeCancelledNearby() {
        Run run=new Run(data());run.go(644,191);assertTrue(run.relay.interact(run.player,run.other));run.waitFor(600);
        assertEquals(RelayGateSystem.State.APPROACHING_WEST,run.relay.state());assertEquals(0,run.relay.opening(),0);
        assertEquals("relayGoalMakeRoom",run.relay.objectiveKey());
        run.go(550,270);assertFalse(run.relay.interact(run.player,run.other));run.waitFor(800);
        assertEquals(RelayGateSystem.State.HOLDING_WEST,run.relay.state());
        run.go(602,191);assertTrue(run.relay.interact(run.player,run.other));assertEquals(RelayGateSystem.State.IDLE,run.relay.state());
    }
    @Test public void playerAloneAndEarlyInteractionDoNotSkipHandoff() {
        Run run=new Run(data());run.go(644,191);run.waitFor(300);assertTrue(run.relay.westPressed());
        assertEquals(0,run.relay.opening(),0);assertFalse(run.relay.isLatched());
        run.player.setPosition(1034,191);assertFalse(run.relay.interact(run.player,run.other));
        run.waitFor(300);assertEquals(0,run.relay.opening(),0);
    }
    @Test public void gateHoldsOpenForCompanionInItsOpening() {
        Run run=new Run(data());run.requestWest();run.reachEast();run.relay.interact(run.player,run.other);
        // Fixture exclusiva para a borda de segurança; a solução completa não reposiciona pessoas.
        run.other.setPosition(832,270);run.player.setPosition(1034,350);run.waitFor(400);
        assertEquals(1,run.relay.opening(),0);assertTrue(run.collision.walkable(832,270,true));
        run.other.setPosition(900,270);run.waitFor(400);assertEquals(0,run.relay.opening(),0);
    }
    @Test public void closedGateBlocksNavigationAndRejectsInvalidIntervals() {
        Run run=new Run(data());assertFalse(run.collision.clearPath(740,270,910,270,true));
        assertFalse(run.collision.boxWalkableIgnoringCart(832,270,42,30,true));
        for(float delta:new float[]{Float.NaN,Float.POSITIVE_INFINITY,-.1f,.2f}) {
            try { run.relay.update(run.player,run.other,true,delta);fail("Aceitou intervalo inválido"); }
            catch(IllegalArgumentException expected) { }
        }
    }
    @Test public void preservedRelayFixtureCanBeSolvedThroughItsActualCollision() {
        BridgeData data=data();data.exit=new float[]{1410,235,130,80};
        Run run=new Run(data);run.requestWest();run.reachEast();run.relay.interact(run.player,run.other);run.waitFor(1200);
        assertTrue(run.relay.isLatched());run.go(1490,270);run.waitFor(600);
        assertTrue(run.other.getX()>data.exit[0]);
    }
}
