package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.*;
import com.nos.entities.PuzzleHero;
import org.junit.Test;
import static org.junit.Assert.*;

/** Percorre o mapa distribuído sem reposicionar nenhum corpo durante a solução. */
public class ObservatorySystemTest {
    private BridgeData read() {
        return new Json().fromJson(BridgeData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/iris-convergence.json"));
    }
    private static final class Run {
        final BridgeData data;
        final BridgeCollisionSystem collision;
        final ObservatorySystem system;
        final PuzzleHero player,other;
        Run(BridgeData data) {
            this.data=data;collision=new BridgeCollisionSystem(data);system=new ObservatorySystem(data,collision);
            player=new PuzzleHero(data.shadowStart[0],data.shadowStart[1]);other=new PuzzleHero(data.reflectionStart[0],data.reflectionStart[1]);
        }
        void tick() {
            float x=other.getX(),y=other.getY();
            system.update(player,other,BridgeTuning.FIXED_STEP);
            assertTrue(collision.walkable(player.getX(),player.getY(),true));
            assertTrue(collision.walkable(other.getX(),other.getY(),true));
            assertTrue("Reflexo deve caminhar",Math.hypot(other.getX()-x,other.getY()-y)<=ObservatoryTuning.COMPANION_SPEED*BridgeTuning.FIXED_STEP+.01f);
        }
        void waitFor(int ticks) { for(int i=0;i<ticks;i++) tick(); }
        void go(float x,float y) {
            for(int i=0;i<4000;i++) {
                float dx=x-player.getX(),dy=y-player.getY(),distance=(float)Math.hypot(dx,dy);
                if(distance<1) return;
                float move=Math.min(distance,BridgeTuning.SPEED*BridgeTuning.FIXED_STEP);
                collision.move(player,dx/distance*move,dy/distance*move,true);tick();
            }
            fail("Piso bloqueado rumo a "+x+","+y+" em "+player.getX()+","+player.getY());
        }
        void watch() { go(422,270);waitFor(1200);assertEquals(ObservatorySystem.State.SIGNALLING,system.state()); }
        void reachNorth() { go(438.59f,353.42f);go(485.85f,424.15f);go(556.58f,471.41f);go(640,484); }
    }
    @Test public void companionWalksItsOwnGalleryAndDoesNotFollowPlayerThroughTheVoid() {
        Run run=new Run(read());run.waitFor(500);
        assertEquals(786,run.other.getX(),0);assertEquals(270,run.other.getY(),0);
        run.go(422,270);
        for(int i=0;i<1200;i++) {
            run.tick();float radius=(float)Math.hypot(run.other.getX()-640,run.other.getY()-270);
            assertTrue(radius>=138-.1f&&radius<=154+.1f);
        }
        assertEquals(ObservatorySystem.State.SIGNALLING,run.system.state());
        assertEquals(640,run.other.getX(),2.1f);assertEquals(416,run.other.getY(),2.1f);
        assertFalse(run.system.allowsExit());assertEquals("observatorySpeech",run.system.speech());
    }
    @Test public void playerChoosesPassageAndBothReachTheCoreBySeparatePhysicalRoutes() {
        Run run=new Run(read());run.watch();
        assertTrue(run.system.interact(run.player,run.other));
        assertEquals("observatoryWrongPassage",run.system.pollNotice());
        assertFalse(run.system.isPassageOpen(0));assertFalse(run.system.allowsExit());
        run.reachNorth();float x=run.other.getX(),y=run.other.getY();
        assertTrue(run.system.interact(run.player,run.other));
        assertEquals(x,run.other.getX(),0);assertEquals(y,run.other.getY(),0);
        assertTrue(run.system.isPassageOpen(2));assertFalse(run.system.isPassageOpen(1));
        assertEquals(3,run.system.objectiveStep());
        run.go(640,270);assertFalse(run.system.allowsExit());
        run.waitFor(1800);
        assertTrue(run.system.allowsExit());assertEquals(4,run.system.objectiveStep());
        assertEquals(ObservatorySystem.State.REUNITED,run.system.state());
        assertTrue(Math.hypot(run.other.getX()-668,run.other.getY()-270)<2.1f);
        assertTrue(run.player.getX()>=run.data.exit[0]&&run.other.getX()<=run.data.exit[0]+run.data.exit[2]);
    }
    @Test public void closedPassagesAndEmptySpaceCannotBeCrossedByDirectMovement() {
        Run run=new Run(read());
        assertFalse(run.collision.walkable(640,450,true));
        assertFalse(run.collision.walkable(460,270,true));
        assertFalse(run.collision.walkable(820,270,true));
        assertFalse(run.collision.walkable(640,92,true));
        assertFalse(run.collision.walkable(640,170,true));
        assertFalse(run.collision.clearPath(422,270,640,270,true));
        run.go(422,270);
        for(int i=0;i<800;i++) run.collision.move(run.player,2,0,true);
        assertTrue(run.player.getX()<=438.01f);
        assertFalse(run.system.allowsExit());
    }
    @Test public void zeroTimeAndNavigationResetCannotAdvanceOrTeleportTheCompanion() {
        Run run=new Run(read());run.go(422,270);run.waitFor(10);
        float x=run.other.getX(),y=run.other.getY();ObservatorySystem.State state=run.system.state();
        run.system.resetNavigation();
        for(int i=0;i<200;i++) run.system.update(run.player,run.other,0);
        assertEquals(x,run.other.getX(),0);assertEquals(y,run.other.getY(),0);assertEquals(state,run.system.state());
        run.waitFor(1200);assertEquals(ObservatorySystem.State.SIGNALLING,run.system.state());
        assertThrows(IllegalArgumentException.class,()->run.system.update(run.player,run.other,Float.NaN));
        assertThrows(IllegalArgumentException.class,()->run.system.update(run.player,run.other,-.01f));
    }
}
