package com.nos.systems;

import com.nos.data.BridgeData;
import com.nos.data.BridgeTuning;
import com.nos.entities.PuzzleHero;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompanionClearanceSystemTest {
    private BridgeCollisionSystem area(float height) {
        BridgeData data=new BridgeData();data.ground=new float[]{0,0,500,height};
        data.river=new float[]{9999,0,0,height};data.bridge=new float[]{9999,0,0,height};
        data.obstacles=new float[0][];
        BridgeCollisionSystem collision=new BridgeCollisionSystem(data);
        collision.setGardenCartPosition(200,height/2);return collision;
    }
    @Test public void reactsThenWalksAsideWithoutTeleportOrWalkingThroughCart() {
        BridgeCollisionSystem collision=area(300);CompanionClearanceSystem system=new CompanionClearanceSystem(collision);
        PuzzleHero npc=new PuzzleHero(231,150),player=new PuzzleHero(158,150);
        system.update(npc,player,1,0,true,true,.05f);assertEquals(150,npc.getY(),0);
        for(int i=0;i<80;i++) {
            float x=npc.getX(),y=npc.getY();
            boolean blocked=BridgeCollisionSystem.circleTouchesBox(x,y,BridgeTuning.RADIUS,180,135,42,30);
            system.update(npc,player,1,0,blocked,true,.02f);
            assertTrue(Math.hypot(npc.getX()-x,npc.getY()-y)<=105*.02f+.01f);
            assertTrue(collision.walkable(npc.getX(),npc.getY(),true));
        }
        assertTrue(npc.getY()>175);assertEquals(231,npc.getX(),.01f);
    }
    @Test public void noSafeSideMeansWaitingInsteadOfCrossingBoundary() {
        BridgeCollisionSystem collision=area(60);CompanionClearanceSystem system=new CompanionClearanceSystem(collision);
        PuzzleHero npc=new PuzzleHero(231,30),player=new PuzzleHero(158,30);
        for(int i=0;i<80;i++) system.update(npc,player,1,0,true,true,.05f);
        assertEquals(231,npc.getX(),0);assertEquals(30,npc.getY(),0);
    }
    @Test public void choosesOtherSideIfFirstRouteContainsPlayer() {
        BridgeCollisionSystem collision=area(300);CompanionClearanceSystem system=new CompanionClearanceSystem(collision);
        PuzzleHero npc=new PuzzleHero(231,150),player=new PuzzleHero(231,185);
        for(int i=0;i<40;i++) system.update(npc,player,1,0,i<12,true,.02f);
        assertTrue(npc.getY()<130);assertTrue(collision.walkable(npc.getX(),npc.getY(),true));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsNonFiniteDirection() {
        new CompanionClearanceSystem(area(300)).update(new PuzzleHero(231,150),new PuzzleHero(158,150),Float.NaN,0,true,true,.05f);
    }
}
