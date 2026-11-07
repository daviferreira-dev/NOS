package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.*;
import com.nos.entities.PuzzleHero;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompanionFollowSystemTest {
    private final BridgeData data=new Json().fromJson(BridgeData.class,
        new FileHandle(System.getProperty("nos.assets")+"/levels/bridge-encounter.json"));
    private final BridgeCollisionSystem collision=new BridgeCollisionSystem(data);
    private final CompanionFollowSystem system=new CompanionFollowSystem(data,collision);
    private void follow(PuzzleHero follower,PuzzleHero leader,boolean open,int frames) {
        for(int i=0;i<frames;i++) {
            system.update(follower,leader,open,BridgeTuning.FIXED_STEP);
            assertTrue("O parceiro deve permanecer sobre piso válido",collision.walkable(follower.getX(),follower.getY(),open));
        }
    }
    private float distance(PuzzleHero a,PuzzleHero b) { return (float)Math.hypot(a.getX()-b.getX(),a.getY()-b.getY()); }
    @Test public void detoursAroundRockInsteadOfStickingToItsSide() {
        PuzzleHero follower=new PuzzleHero(320,180),leader=new PuzzleHero(480,180);
        follow(follower,leader,false,800);
        assertTrue(distance(follower,leader)<=BridgeTuning.FOLLOW_DISTANCE+.1f);
        assertTrue(follower.getX()>405);
    }
    @Test public void returnsThroughBridgeWhenLeaderMovesDiagonallyOntoFarBank() {
        PuzzleHero follower=new PuzzleHero(535,270),leader=new PuzzleHero(1300,360);
        follow(follower,leader,true,1800);
        assertTrue(distance(follower,leader)<=BridgeTuning.FOLLOW_DISTANCE+.1f);
        assertTrue(follower.getX()>data.river[0]+data.river[2]);
    }
    @Test public void waitsAtClosedRiverThenReplansAsSoonAsBridgeOpens() {
        PuzzleHero follower=new PuzzleHero(535,270),leader=new PuzzleHero(980,270);
        follow(follower,leader,false,120);
        assertEquals(535,follower.getX(),0);
        follow(follower,leader,true,600);
        assertTrue(distance(follower,leader)<=BridgeTuning.FOLLOW_DISTANCE+.1f);
    }
    @Test public void staysStillWithinComfortDistance() {
        PuzzleHero follower=new PuzzleHero(200,270),leader=new PuzzleHero(240,270);
        follow(follower,leader,false,120);
        assertEquals(200,follower.getX(),0);assertEquals(270,follower.getY(),0);
    }
}
