package com.nos.render;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.nos.data.BridgeData;
import com.nos.world.BridgeEncounter;
import org.junit.Test;
import static org.junit.Assert.*;

public class BridgeUiStateTest {
    private BridgeEncounter encounter() {
        return new BridgeEncounter(new Json().fromJson(BridgeData.class,
            new FileHandle(System.getProperty("nos.assets")+"/levels/bridge-encounter.json")));
    }
    @Test public void readingPauseDoesNotAgeObjectiveOrDismissInitialShortcuts() {
        BridgeEncounter encounter=encounter();encounter.start();
        BridgeUiState ui=new BridgeUiState();ui.update(encounter,0);
        encounter.togglePause();ui.update(encounter,600);
        assertTrue(ui.expandedObjective());assertTrue(ui.fullShortcuts());
        encounter.togglePause();ui.update(encounter,8);assertFalse(ui.expandedObjective());assertTrue(ui.fullShortcuts());
        encounter.togglePause();ui.update(encounter,600);assertTrue(ui.fullShortcuts());
        encounter.togglePause();ui.update(encounter,38);assertFalse(ui.fullShortcuts());
    }
    @Test public void requestingCooperationReexpandsNewObjectiveWithoutRepeatingOldShortcutTutorial() {
        BridgeEncounter encounter=encounter();encounter.start();
        BridgeUiState ui=new BridgeUiState();ui.update(encounter,60);
        assertFalse(ui.expandedObjective());assertFalse(ui.fullShortcuts());
        encounter.hero(0).setPosition(encounter.getData().winch[0]-42,encounter.getData().winch[1]);
        encounter.interact();assertEquals("bridgeGoalWaiting",encounter.objectiveKey());
        ui.update(encounter,.01f);assertTrue(ui.expandedObjective());assertFalse(ui.fullShortcuts());
    }
}
