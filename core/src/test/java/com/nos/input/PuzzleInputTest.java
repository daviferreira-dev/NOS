package com.nos.input;

import com.badlogic.gdx.Input;
import org.junit.Test;
import static org.junit.Assert.*;

public class PuzzleInputTest {
    @Test public void keyReleaseStopsMovementAndOppositeKeysCancel() {
        PuzzleInput input=new PuzzleInput();input.keyDown(Input.Keys.W);
        assertEquals(1,input.axisY(),0);input.keyDown(Input.Keys.S);assertEquals(0,input.axisY(),0);
        input.keyUp(Input.Keys.S);assertEquals(1,input.axisY(),0);
        input.keyUp(Input.Keys.W);assertEquals(0,input.axisY(),0);
    }
    @Test public void keyRepeatDoesNotRepeatedlyToggleAnchor() {
        PuzzleInput input=new PuzzleInput();input.keyDown(Input.Keys.E);assertEquals(2,input.takeAnchor());
        input.keyDown(Input.Keys.E);assertEquals(0,input.takeAnchor());
        input.keyUp(Input.Keys.E);input.keyDown(Input.Keys.E);assertEquals(2,input.takeAnchor());
    }
    @Test public void losingFocusClearsHeldDirections() {
        PuzzleInput input=new PuzzleInput();input.keyDown(Input.Keys.D);input.clearHeld();assertEquals(0,input.axisX(),0);
    }
}
