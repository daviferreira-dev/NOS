package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.*;
import com.nos.world.MirrorPuzzle;
import org.junit.Test;
import static org.junit.Assert.*;

public class MirrorPuzzleTest {
    private MirrorPuzzle room() {
        return new MirrorPuzzle(new Json().fromJson(RoomData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/knot-room.json")));
    }
    private void move(MirrorPuzzle room,float x,float y,float distance) {
        int steps=(int)Math.ceil(distance/(PuzzleTuning.MOVE_SPEED*PuzzleTuning.FIXED_STEP));
        float delta=distance/(steps*PuzzleTuning.MOVE_SPEED);
        for(int i=0;i<steps;i++) room.update(x,y,delta);
    }
    @Test public void noInputMeansNoMovementAndReadyDoesNotRun() {
        MirrorPuzzle r=room();r.update(1,0,.05f);
        assertEquals(180,r.getShadow().getX(),0);
        r.start();r.update(0,0,.05f);
        assertEquals(180,r.getShadow().getX(),0);
        assertEquals(780,r.getReflection().getX(),0);
    }
    @Test public void reflectionMirrorsHorizontalButSharesVerticalMovement() {
        MirrorPuzzle r=room();r.start();r.update(1,1,.05f);
        assertTrue(r.getShadow().getX()>180);
        assertTrue(r.getReflection().getX()<780);
        assertEquals(960,r.getShadow().getX()+r.getReflection().getX(),.001f);
        assertEquals(r.getShadow().getY(),r.getReflection().getY(),.001f);
        float dx=r.getShadow().getX()-180,dy=r.getShadow().getY()-210;
        assertEquals(PuzzleTuning.MOVE_SPEED*.05,Math.sqrt(dx*dx+dy*dy),.001);
    }
    @Test public void anchorLocksOnlyTheChosenPresenceAndTogglesOff() {
        MirrorPuzzle r=room();r.start();r.toggleAnchor(AnchorSystem.Target.REFLECTION);
        r.update(1,0,.05f);
        assertEquals(780,r.getReflection().getX(),0);assertTrue(r.getShadow().getX()>180);
        r.toggleAnchor(AnchorSystem.Target.SHADOW);
        float x=r.getShadow().getX();r.update(1,0,.05f);
        assertEquals(x,r.getShadow().getX(),0);assertTrue(r.getReflection().getX()<780);
        r.toggleAnchor(AnchorSystem.Target.SHADOW);
        assertEquals(AnchorSystem.Target.NONE,r.getAnchor().getTarget());
    }
    @Test public void wallsCollideOnlyWithTheirOwnLayer() {
        MirrorPuzzle r=room();RoomCollisionSystem c=new RoomCollisionSystem(r.getData());
        assertTrue(c.blocked(320,210,false,true));assertFalse(c.blocked(320,210,true,true));
        assertTrue(c.blocked(630,270,true,true));assertFalse(c.blocked(630,270,false,true));
    }
    @Test public void collisionDoesNotTunnelAndAllowsSliding() {
        MirrorPuzzle r=room();RoomCollisionSystem c=new RoomCollisionSystem(r.getData());
        c.move(r.getShadow(),1000,0,false,false);
        assertTrue(r.getShadow().getX()<300-PuzzleTuning.HERO_RADIUS+1);
        float y=r.getShadow().getY();c.move(r.getShadow(),10,30,false,false);
        assertTrue(r.getShadow().getY()>y);
    }
    @Test public void gateBlocksBridgeUntilBothPadsAreOccupiedAndStaysOpen() {
        MirrorPuzzle r=room();r.start();RoomCollisionSystem c=new RoomCollisionSystem(r.getData());
        assertTrue(c.blocked(450,390,false,r.isGateOpen()));
        r.getShadow().setPosition(330,350);r.update(0,0,.01f);assertFalse(r.isGateOpen());
        r.getReflection().setPosition(710,350);r.update(0,0,.01f);assertTrue(r.isGateOpen());
        r.getShadow().setPosition(450,350);r.update(0,0,.01f);assertTrue(r.isGateOpen());
        assertFalse(c.blocked(450,390,false,r.isGateOpen()));
    }
    @Test public void realRoomCanBeSolvedThroughMovementAndBindings() {
        MirrorPuzzle r=room();r.start();
        move(r,0,1,140);move(r,1,0,70);
        assertFalse(r.isGateOpen());
        r.toggleAnchor(AnchorSystem.Target.REFLECTION);move(r,1,0,80);
        assertTrue(r.isGateOpen());
        r.toggleAnchor(AnchorSystem.Target.REFLECTION);move(r,1,0,120);
        r.toggleAnchor(AnchorSystem.Target.SHADOW);move(r,1,0,80);
        r.toggleAnchor(AnchorSystem.Target.SHADOW);move(r,0,1,80);
        assertEquals(MirrorPuzzle.State.WON,r.getState());
    }
    @Test public void singleHeroAtExitIsNotVictoryAndPauseFreezesMovement() {
        MirrorPuzzle r=room();r.start();
        r.getShadow().setPosition(450,430);r.update(0,0,.01f);
        assertEquals(MirrorPuzzle.State.PLAYING,r.getState());
        r.togglePause();r.update(1,0,.05f);assertEquals(450,r.getShadow().getX(),0);
        assertEquals(MirrorPuzzle.State.PAUSED,r.getState());
    }
}
