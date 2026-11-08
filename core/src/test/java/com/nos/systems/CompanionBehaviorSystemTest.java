package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.*;
import com.nos.entities.PuzzleHero;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompanionBehaviorSystemTest {
    private static BridgeData bridgeOnly() {
        BridgeData data=new Json().fromJson(BridgeData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/bridge-encounter.json"));
        data.garden=null;return data;
    }
    private final BridgeData data=bridgeOnly();
    private final BridgeCollisionSystem collision=new BridgeCollisionSystem(data);
    private final CompanionBehaviorSystem behavior=new CompanionBehaviorSystem(data,collision);
    private void advance(PuzzleHero npc,PuzzleHero player,boolean open,boolean locked,int steps) {
        for(int i=0;i<steps;i++) {
            npc.setMotion(0,0);behavior.update(npc,player,open,locked,.05f);
            assertTrue(collision.walkable(npc.getX(),npc.getY(),open));
        }
    }
    @Test public void startsFollowingAfterItsOwnReactionInsteadOfCopyingFirstInputFrame() {
        PuzzleHero npc=new PuzzleHero(200,270),player=new PuzzleHero(330,270);
        advance(npc,player,false,false,6);assertEquals(200,npc.getX(),0);
        advance(npc,player,false,false,4);assertTrue(npc.getX()>200);
        advance(npc,player,false,false,100);
        assertTrue(Math.hypot(npc.getX()-player.getX(),npc.getY()-player.getY())<=BridgeTuning.COMPANION_STOP_DISTANCE+.5f);
        float x=npc.getX();advance(npc,player,false,false,100);assertEquals(x,npc.getX(),.01f);
    }
    @Test public void noticesWaterOnceAndReturnsWithoutAnEndlessIdleLoop() {
        PuzzleHero npc=new PuzzleHero(430,270),player=new PuzzleHero(480,270);
        advance(npc,player,false,false,1);
        assertEquals(CompanionBehaviorSystem.State.WATCHING_WATER,behavior.getState());
        assertEquals("bridgeWaterWhisper",behavior.speech());assertFalse(npc.isMoving());
        advance(npc,player,false,false,100);
        assertEquals(CompanionBehaviorSystem.State.FOLLOWING,behavior.getState());assertNull(behavior.speech());
        assertEquals(430,npc.getX(),0);
    }
    @Test public void helpRequestTakesPriorityOverCuriosityAndRequiresActuallyReachingWinch() {
        PuzzleHero npc=new PuzzleHero(430,270),player=new PuzzleHero(493,270);
        advance(npc,player,false,false,1);assertEquals(CompanionBehaviorSystem.State.WATCHING_WATER,behavior.getState());
        behavior.requestSupport();assertFalse(behavior.isHolding());float before=npc.getX();
        advance(npc,player,false,false,1);
        assertTrue(npc.getX()>before);assertTrue(npc.getX()<data.winch[0]-BridgeTuning.COMPANION_WINCH_RADIUS);
        advance(npc,player,false,false,60);assertTrue(behavior.isHolding());
        assertTrue(Math.hypot(npc.getX()-data.winch[0],npc.getY()-data.winch[1])<=BridgeTuning.COMPANION_WINCH_RADIUS);
        float x=npc.getX(),y=npc.getY();player.setPosition(980,270);advance(npc,player,true,false,200);
        assertEquals(x,npc.getX(),0);assertEquals(y,npc.getY(),0);
    }
    @Test public void asksForRoomInsteadOfEndingOnTopOfPlayerAndContinuesAfterPlayerMoves() {
        PuzzleHero npc=new PuzzleHero(430,270),player=new PuzzleHero(535,270);behavior.requestSupport();
        advance(npc,player,false,false,100);assertFalse(behavior.isHolding());assertTrue(behavior.needsRoom());
        assertEquals("bridgeCompanionNeedsRoom",behavior.status());
        assertTrue(Math.hypot(npc.getX()-player.getX(),npc.getY()-player.getY())>=BridgeTuning.COMPANION_CLEARANCE-1);
        player.setPosition(580,270);advance(npc,player,false,false,100);assertTrue(behavior.isHolding());assertFalse(behavior.needsRoom());
    }
    @Test public void cancelledApproachDoesNotLaterGrabMechanismOnItsOwn() {
        PuzzleHero npc=new PuzzleHero(430,270),player=new PuzzleHero(493,270);behavior.requestSupport();behavior.releaseSupport();
        advance(npc,player,false,false,200);
        assertFalse(behavior.isSupportingOrApproaching());assertEquals(CompanionBehaviorSystem.State.FOLLOWING,behavior.getState());
    }
    @Test public void returnsByActualBridgeAfterReleaseRatherThanCuttingAcrossWater() {
        PuzzleHero npc=new PuzzleHero(430,270),player=new PuzzleHero(493,270);behavior.requestSupport();
        advance(npc,player,false,false,100);assertTrue(behavior.isHolding());
        player.setPosition(1380,320);behavior.releaseSupport();
        float x=npc.getX();advance(npc,player,true,true,5);assertEquals(x,npc.getX(),0);
        advance(npc,player,true,true,250);
        assertEquals(CompanionBehaviorSystem.State.FOLLOWING,behavior.getState());assertTrue(npc.getX()>data.river[0]+data.river[2]);
        assertTrue(Math.hypot(npc.getX()-player.getX(),npc.getY()-player.getY())<=BridgeTuning.COMPANION_START_DISTANCE);
    }
}
