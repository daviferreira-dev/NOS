package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.*;
import com.nos.entities.PuzzleHero;
import org.junit.Test;
import static org.junit.Assert.*;

/** Duas escolhas percorridas no mapa distribuído, com brasas e colisão ligadas. */
public class FurnaceSystemTest {
    private static final class Run {
        final BridgeData data;
        final BridgeCollisionSystem collision;
        final FurnaceSystem furnace;
        final EnvironmentalActionSystem actions;
        final CompanionFollowSystem follow;
        final PuzzleHero player,other;
        Run() {
            data=new Json().fromJson(BridgeData.class,new FileHandle(System.getProperty("nos.assets")+"/levels/water-transfer.json"));
            collision=new BridgeCollisionSystem(data);furnace=new FurnaceSystem(data,collision);
            actions=new EnvironmentalActionSystem(data,collision);follow=new CompanionFollowSystem(data,collision);
            player=new PuzzleHero(data.shadowStart[0],data.shadowStart[1]);other=new PuzzleHero(data.reflectionStart[0],data.reflectionStart[1]);
        }
        void tick() {
            float oldX=other.getX(),oldY=other.getY();
            if(!furnace.controlsCompanion()) follow.update(other,player,true,BridgeTuning.FIXED_STEP);
            furnace.update(player,other,BridgeTuning.FIXED_STEP);actions.update(player,other,BridgeTuning.FIXED_STEP);
            assertTrue(collision.walkable(player.getX(),player.getY(),true));assertTrue(collision.walkable(other.getX(),other.getY(),true));
            assertTrue(Math.hypot(other.getX()-oldX,other.getY()-oldY)<=BridgeTuning.FOLLOW_SPEED*BridgeTuning.FIXED_STEP+.01f);
        }
        void waitFor(int ticks) { for(int i=0;i<ticks;i++) tick(); }
        void go(float x,float y) {
            for(int i=0;i<6000;i++) {
                float dx=x-player.getX(),dy=y-player.getY(),distance=(float)Math.hypot(dx,dy);
                if(distance<1) return;float travel=Math.min(distance,BridgeTuning.SPEED*BridgeTuning.FIXED_STEP);
                collision.move(player,dx/distance*travel,dy/distance*travel,true);tick();
            }
            fail("Percurso bloqueado rumo a "+x+","+y+" em "+player.getX()+","+player.getY());
        }
        void rest() {
            HazardData h=data.hazards[0];float rest=h.warningSeconds+h.activeSeconds,cycle=rest+h.restSeconds;
            for(int i=0;i<4000;i++) {
                float local=(actions.time()+h.phase)%cycle;
                if(local>=rest&&local<rest+BridgeTuning.FIXED_STEP*2) return;tick();
            }
            fail("Não chegou à janela de repouso.");
        }
    }
    @Test public void safeDetourLetsBothPeopleWalkTogetherWithoutLeverOrCart() {
        Run run=new Run();assertNull(run.data.garden);assertNull(run.data.relay);assertFalse(run.furnace.allowsExit());
        run.go(440,145);run.go(1050,145);run.go(1110,260);run.waitFor(1000);
        assertEquals(FurnaceSystem.Route.DETOUR,run.furnace.route());assertFalse(run.furnace.isDoorOpen());assertTrue(run.furnace.allowsExit());
        run.go(1490,270);run.waitFor(600);assertTrue(run.other.getX()>1400);assertFalse(run.actions.didRecover());
    }
    @Test public void hotShortcutRequiresTimingAndOpeningDoorForPhysicalReunion() {
        Run run=new Run();run.go(440,335);run.go(600,335);run.waitFor(300);
        assertEquals(FurnaceSystem.Route.SHORTCUT,run.furnace.route());assertFalse(run.furnace.allowsExit());
        assertTrue(run.other.getX()<500);run.rest();run.go(1080,335);run.go(1100,335);
        float x=run.other.getX(),y=run.other.getY();assertTrue(run.furnace.interact(run.player,run.other));
        assertEquals(x,run.other.getX(),0);assertEquals(y,run.other.getY(),0);assertFalse(run.furnace.allowsExit());
        assertTrue(run.furnace.isDoorOpen());assertTrue(run.collision.walkable(712,260,true));
        run.waitFor(1200);assertTrue(run.furnace.allowsExit());assertTrue(run.other.getX()>1050);
        run.go(1490,270);run.waitFor(600);assertTrue(run.other.getX()>1400);
    }
    @Test public void furnaceWallsMakeTheRoutesPhysicalAndClosedDoorBlocksCoolCorridor() {
        Run run=new Run();assertFalse(run.collision.walkable(650,205,true));assertFalse(run.collision.walkable(650,302,true));
        assertFalse(run.collision.clearPath(600,260,800,260,true));assertTrue(run.collision.clearPath(600,145,1000,145,true));
        assertTrue(run.collision.clearPath(600,335,1000,335,true));
        assertFalse(run.collision.boxWalkableIgnoringCart(712,260,42,30,true));
    }
    @Test public void playerCanChangeMindAndTakeSafeDetourAfterSeeingFire() {
        Run run=new Run();run.go(440,335);run.go(480,335);assertEquals(FurnaceSystem.Route.SHORTCUT,run.furnace.route());
        run.go(440,335);run.go(440,145);run.go(1050,145);run.go(1110,260);run.waitFor(1000);
        assertEquals(FurnaceSystem.Route.DETOUR,run.furnace.route());assertTrue(run.furnace.allowsExit());
    }
    @Test public void remoteInteractionDoesNotOpenDoorAndInvalidDeltaIsRejected() {
        Run run=new Run();assertFalse(run.furnace.interact(run.player,run.other));assertFalse(run.furnace.isDoorOpen());
        for(float delta:new float[]{Float.NaN,Float.POSITIVE_INFINITY,-.1f,.2f}) {
            try { run.furnace.update(run.player,run.other,delta);fail("Aceitou intervalo inválido"); }
            catch(IllegalArgumentException expected) { }
        }
    }
}
