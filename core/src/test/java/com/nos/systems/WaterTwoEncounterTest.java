package com.nos.systems;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.I18NBundle;
import com.nos.data.*;
import com.nos.world.BridgeEncounter;
import org.junit.Test;
import static org.junit.Assert.*;

/** Valida o percurso do mapa distribuído, com deslocamento físico de ambos os corpos. */
public class WaterTwoEncounterTest {
    private BridgeEncounter create() {
        BridgeData data=new Json().fromJson(BridgeData.class,new FileHandle(System.getProperty("nos.assets")+"/"+LevelLoader.SECOND_WATER));
        BridgeEncounter world=new BridgeEncounter(data);world.start();return world;
    }
    private void waitFor(BridgeEncounter world,int ticks) { for(int i=0;i<ticks;i++) world.update(0,0,BridgeTuning.FIXED_STEP); }
    private void go(BridgeEncounter world,float x,float y) {
        for(int i=0;i<6000&&world.getState()==BridgeEncounter.State.PLAYING;i++) {
            float dx=x-world.hero(0).getX(),dy=y-world.hero(0).getY(),d=(float)Math.hypot(dx,dy);
            if(d<3) break;world.update(dx/d,dy/d,BridgeTuning.FIXED_STEP);
        }
        waitFor(world,40);
        assertTrue("Percurso bloqueado para "+x+","+y+" em "+world.hero(0).getX()+","+world.hero(0).getY(),world.getState()==BridgeEncounter.State.WON||Math.hypot(world.hero(0).getX()-x,world.hero(0).getY()-y)<9);
    }
    @Test public void levelStartsWithNewPuzzleAndClosedPassages() {
        BridgeEncounter world=create();
        assertTrue(world.isLocked());assertTrue(world.getData().gardenOnly);
        assertFalse(world.hasGarden());assertFalse(world.hasRelay());assertTrue(world.challenge() instanceof FurnaceSystem);
        assertFalse(((FurnaceSystem)world.challenge()).isDoorOpen());
        assertEquals(4,world.objectiveCount());assertEquals(1,world.objectiveStep());
        assertEquals("furnaceGoalChoose",world.objectiveKey());
        assertTrue(new FileHandle(System.getProperty("nos.assets")+"/"+world.getData().backgroundPath).exists());
    }
    @Test public void safeDetourCanBeCompletedByBothPeopleWithoutOpeningTheDoor() {
        BridgeEncounter world=create();
        go(world,440,145);go(world,1050,145);go(world,1110,260);waitFor(world,1000);
        FurnaceSystem furnace=(FurnaceSystem)world.challenge();assertTrue(furnace.allowsExit());assertFalse(furnace.isDoorOpen());
        go(world,1490,270);waitFor(world,1200);
        assertEquals(BridgeEncounter.State.WON,world.getState());
    }
    @Test public void hotShortcutOpensARealSafeRouteForCompanionInsteadOfTeleporting() {
        BridgeEncounter world=create();go(world,440,335);go(world,600,335);waitFor(world,300);
        FurnaceSystem furnace=(FurnaceSystem)world.challenge();assertEquals(FurnaceSystem.Route.SHORTCUT,furnace.route());
        assertTrue(world.hero(1).getX()<500);HazardData hazard=world.getData().hazards[0];
        float restStart=hazard.warningSeconds+hazard.activeSeconds,cycle=restStart+hazard.restSeconds;
        for(int i=0;i<4000;i++) {
            float local=(world.actions().time()+hazard.phase)%cycle;
            if(local>=restStart&&local<restStart+BridgeTuning.FIXED_STEP*2) break;
            world.update(0,0,BridgeTuning.FIXED_STEP);
        }
        go(world,1080,335);go(world,1100,335);
        float x=world.hero(1).getX(),y=world.hero(1).getY();world.interact();
        assertTrue(furnace.isDoorOpen());assertEquals(x,world.hero(1).getX(),0);assertEquals(y,world.hero(1).getY(),0);
        waitFor(world,1200);assertTrue(furnace.allowsExit());assertTrue(world.hero(1).getX()>1050);
        go(world,1490,270);waitFor(world,1200);assertEquals(BridgeEncounter.State.WON,world.getState());
    }
    @Test public void allSecondLevelObjectivesHaveReadableCopies() {
        I18NBundle strings=I18NBundle.createBundle(new FileHandle(System.getProperty("nos.assets")+"/ui/strings"));
        for(String key:new String[]{"furnaceGoalChoose","furnaceGoalDoor","furnaceGoalDetour","furnaceGoalWait","furnaceGoalExit"}) {
            assertFalse(strings.get(key).isEmpty());assertFalse(strings.get(key+"Compact").isEmpty());
        }
    }
}
