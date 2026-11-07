package com.nos.input;

import com.badlogic.gdx.Input;
import org.junit.Test;
import static org.junit.Assert.*;

public class BridgeInputTest {
    @Test public void tabSwitchIsEdgeTriggered() {
        BridgeInput input=new BridgeInput();input.keyDown(Input.Keys.TAB);assertEquals(1,input.action());
        input.keyDown(Input.Keys.TAB);assertEquals(0,input.action());
        input.keyUp(Input.Keys.TAB);input.keyDown(Input.Keys.TAB);assertEquals(1,input.action());
    }
    @Test public void contextualActionAndMovementReleaseAreIndependent() {
        BridgeInput input=new BridgeInput();input.keyDown(Input.Keys.D);input.keyDown(Input.Keys.E);
        assertEquals(2,input.action());assertEquals(1,input.x(),0);
        input.keyUp(Input.Keys.D);assertEquals(0,input.x(),0);assertEquals(0,input.action());
    }
}
