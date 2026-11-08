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
        assertTrue(world.garden().hasTransfer());assertFalse(world.garden().isOpen());
        assertEquals(4,world.objectiveCount());assertEquals(1,world.objectiveStep());
        assertEquals("transferGoalLoad",world.objectiveKey());
        assertTrue(new FileHandle(System.getProperty("nos.assets")+"/"+world.getData().backgroundPath).exists());
    }
    @Test public void shippedMapCanBeSolvedByTransferringTheSameCart() {
        BridgeEncounter world=create();
        go(world,290,270);world.interact();assertTrue(world.garden().isDragging());
        go(world,658,272);world.interact();waitFor(world,200);
        assertEquals(1,world.garden().transferOpening(),.001f);assertFalse(world.garden().isTransferLatched());
        go(world,658,218);go(world,760,218);go(world,760,270);go(world,930,270);go(world,982,186);
        world.interact();waitFor(world,1800);
        assertTrue("Reflexo precisa chegar ao pedal além da grade",world.garden().isHelperHolding());
        assertTrue(world.garden().isTransferLatched());assertFalse(world.garden().isOpen());
        go(world,930,270);go(world,760,270);go(world,760,218);go(world,658,218);go(world,658,272);
        world.interact();assertTrue(world.garden().isDragging());
        go(world,1178,309);world.interact();waitFor(world,300);
        assertTrue(world.garden().weightPressed());assertTrue(world.garden().isOpen());
        go(world,1230,248);go(world,1490,270);waitFor(world,1200);
        assertEquals(BridgeEncounter.State.WON,world.getState());
    }
    @Test public void allSecondLevelObjectivesHaveReadableCopies() {
        I18NBundle strings=I18NBundle.createBundle(new FileHandle(System.getProperty("nos.assets")+"/ui/strings"));
        for(String key:new String[]{"transferGoalLoad","transferGoalHelp","transferGoalMove","gardenGoalWaiting","gardenGoalMakeRoom","gardenGoalExit"}) {
            assertFalse(strings.get(key).isEmpty());assertFalse(strings.get(key+"Compact").isEmpty());
        }
    }
}
