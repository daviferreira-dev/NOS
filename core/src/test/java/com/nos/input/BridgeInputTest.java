package com.nos.input;

import com.badlogic.gdx.Input;
import org.junit.Test;
import static org.junit.Assert.*;

public class BridgeInputTest {
    @Test public void tabAndFormerSwapKeyCannotSelectCompanion() {
        BridgeInput input=new BridgeInput();input.keyDown(Input.Keys.TAB);input.keyDown(Input.Keys.Q);
        assertEquals(0,input.action());assertEquals(0,input.x(),0);assertEquals(0,input.y(),0);
    }
    @Test public void contextualActionAndMovementReleaseAreIndependent() {
        BridgeInput input=new BridgeInput();input.keyDown(Input.Keys.D);input.keyDown(Input.Keys.E);
        assertEquals(2,input.action());assertEquals(1,input.x(),0);
        input.keyUp(Input.Keys.D);assertEquals(0,input.x(),0);assertEquals(0,input.action());
    }
    @Test public void desktopMouseDoesNotOperateHiddenTouchControls() {
        BridgeInput input=new BridgeInput();input.setTouchControls(false);
        input.touchDown(90,50,0,Input.Buttons.LEFT);input.project(null,540);
        assertEquals(0,input.x(),0);assertEquals(0,input.y(),0);assertEquals(0,input.action());
        assertTrue(input.start());assertFalse(input.start());
    }
    @Test public void helpTogglesOncePerPressWithoutChangingContextualAction() {
        BridgeInput input=new BridgeInput();input.keyDown(Input.Keys.H);assertTrue(input.help());
        input.keyDown(Input.Keys.H);assertFalse(input.help());
        input.keyDown(Input.Keys.E);assertEquals(2,input.action());
        input.keyUp(Input.Keys.H);input.keyDown(Input.Keys.H);assertTrue(input.help());
    }
    @Test public void remappedMovementAndInteractionReleaseWithoutLeavingKeysHeld() {
        BridgeKeyBindings bindings=new BridgeKeyBindings();
        bindings.assign(BridgeKeyBindings.Action.RIGHT,Input.Keys.L);
        bindings.assign(BridgeKeyBindings.Action.INTERACT,Input.Keys.F);
        BridgeInput input=new BridgeInput();input.setBindings(bindings);
        input.keyDown(Input.Keys.D);input.keyDown(Input.Keys.E);assertEquals(0,input.x(),0);assertEquals(0,input.action());
        input.keyDown(Input.Keys.L);input.keyDown(Input.Keys.F);assertEquals(1,input.x(),0);assertEquals(2,input.action());
        input.keyUp(Input.Keys.L);input.keyUp(Input.Keys.F);assertEquals(0,input.x(),0);assertEquals(0,input.action());
        input.keyDown(Input.Keys.RIGHT);assertEquals(1,input.x(),0);input.keyUp(Input.Keys.RIGHT);assertEquals(0,input.x(),0);
    }
    @Test public void menuClearsMovementWithoutRearmingHeldHelpPauseOrRestart() {
        BridgeInput input=new BridgeInput();input.keyDown(Input.Keys.W);input.keyDown(Input.Keys.H);
        assertTrue(input.help());input.clearForMenu();assertEquals(0,input.y(),0);
        input.keyDown(Input.Keys.H);assertFalse(input.help());input.keyUp(Input.Keys.H);input.keyDown(Input.Keys.H);assertTrue(input.help());
        input.keyDown(Input.Keys.SPACE);assertTrue(input.pause());input.clearForMenu();
        input.keyDown(Input.Keys.SPACE);assertFalse(input.pause());input.keyUp(Input.Keys.SPACE);input.keyDown(Input.Keys.SPACE);assertTrue(input.pause());
        input.keyDown(Input.Keys.R);assertTrue(input.restart());input.clearForMenu();input.keyDown(Input.Keys.R);assertFalse(input.restart());
    }
}
