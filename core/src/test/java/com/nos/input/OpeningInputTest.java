package com.nos.input;

import com.badlogic.gdx.Input;
import org.junit.Test;
import static org.junit.Assert.*;

public class OpeningInputTest {
    @Test public void heldEnterUsedToSkipCannotAlsoStartMenu() {
        OpeningInput input=new OpeningInput();input.keyDown(Input.Keys.ENTER);
        assertEquals(OpeningInput.Action.SKIP,input.take(null));input.setTitle(true);
        for(int i=0;i<20;i++) input.keyDown(Input.Keys.ENTER);
        assertEquals(OpeningInput.Action.NONE,input.take(null));
        input.keyUp(Input.Keys.ENTER);input.keyDown(Input.Keys.ENTER);
        assertEquals(OpeningInput.Action.START,input.take(null));
    }
    @Test public void clickingMovieOnlySkipsAndBackgroundDoesNotStartMenu() {
        OpeningInput input=new OpeningInput();input.touchDown(100,100,0,Input.Buttons.LEFT);
        assertEquals(OpeningInput.Action.SKIP,input.take(null));input.setTitle(true);
        assertEquals(OpeningInput.Action.NONE,input.take(null));
        assertEquals(OpeningInput.Action.NONE,OpeningInput.buttonAt(700,300));
        assertEquals(OpeningInput.Action.START,OpeningInput.buttonAt(200,210));
        assertEquals(OpeningInput.Action.REPLAY,OpeningInput.buttonAt(200,150));
        assertEquals(OpeningInput.Action.MUSIC,OpeningInput.buttonAt(200,105));
    }
    @Test public void spaceEscapeAndUnrelatedKeysNeverStartAtTitle() {
        OpeningInput input=new OpeningInput();input.setTitle(true);
        input.keyDown(Input.Keys.SPACE);input.keyDown(Input.Keys.ESCAPE);input.keyDown(Input.Keys.W);input.keyDown(Input.Keys.M);
        assertEquals(OpeningInput.Action.NONE,input.take(null));
    }
    @Test public void pendingMovieInputIsDiscardedIfNaturalEndingHappensFirst() {
        OpeningInput input=new OpeningInput();input.keyDown(Input.Keys.ENTER);input.setTitle(true);
        assertEquals(OpeningInput.Action.NONE,input.take(null));
    }
    @Test public void savedMenuHasExplicitResumeAndFreshStartTargets() {
        assertEquals(OpeningInput.Action.START,OpeningInput.buttonAt(200,240,true));
        assertEquals(OpeningInput.Action.FROM_BEGINNING,OpeningInput.buttonAt(200,190,true));
        assertEquals(OpeningInput.Action.REPLAY,OpeningInput.buttonAt(200,150,true));
        assertEquals(OpeningInput.Action.MUSIC,OpeningInput.buttonAt(200,105,true));
        assertEquals(OpeningInput.Action.NONE,OpeningInput.buttonAt(200,240,false));
        OpeningInput input=new OpeningInput();input.setTitle(true);input.keyDown(Input.Keys.N);
        assertEquals(OpeningInput.Action.NONE,input.take(null));
        input.keyUp(Input.Keys.N);input.setProgressAvailable(true);input.keyDown(Input.Keys.N);
        assertEquals(OpeningInput.Action.FROM_BEGINNING,input.take(null));
        input.keyUp(Input.Keys.N);input.keyDown(Input.Keys.ENTER);
        assertEquals(OpeningInput.Action.START,input.take(null));
    }
    @Test public void heldFreshStartKeyDuringMovieNeverStartsSavedMenu() {
        OpeningInput input=new OpeningInput();input.setProgressAvailable(true);input.keyDown(Input.Keys.N);
        assertEquals(OpeningInput.Action.NONE,input.take(null));input.setTitle(true);
        for(int i=0;i<20;i++) input.keyDown(Input.Keys.N);
        assertEquals(OpeningInput.Action.NONE,input.take(null));
        input.keyUp(Input.Keys.N);input.keyDown(Input.Keys.N);
        assertEquals(OpeningInput.Action.FROM_BEGINNING,input.take(null));
    }
    @Test public void savedProgressDoesNotChangeMovieSkipIsolation() {
        OpeningInput input=new OpeningInput();input.setProgressAvailable(true);
        input.touchDown(200,240,0,Input.Buttons.LEFT);
        assertEquals(OpeningInput.Action.SKIP,input.take(null));input.setTitle(true);
        assertEquals(OpeningInput.Action.NONE,input.take(null));
        input.keyDown(Input.Keys.ENTER);assertEquals(OpeningInput.Action.START,input.take(null));
    }
    @Test public void returningFromGameRequiresReleaseBeforeHeldMenuCommandsCanAct() {
        OpeningInput input=new OpeningInput();input.setTitle(true);input.setProgressAvailable(true);
        input.seedHeldKeys(true,true,true);
        for(int i=0;i<10;i++) { input.keyDown(Input.Keys.ENTER);input.keyDown(Input.Keys.R);input.keyDown(Input.Keys.N); }
        assertEquals(OpeningInput.Action.NONE,input.take(null));
        input.keyUp(Input.Keys.ENTER);input.keyDown(Input.Keys.ENTER);assertEquals(OpeningInput.Action.START,input.take(null));
        input.keyUp(Input.Keys.N);input.keyDown(Input.Keys.N);assertEquals(OpeningInput.Action.FROM_BEGINNING,input.take(null));
    }
    @Test public void mouseReturnDoesNotForceAnUnnecessaryFirstEnterRelease() {
        OpeningInput input=new OpeningInput();input.setTitle(true);input.seedHeldKeys(false,false,false);
        input.keyDown(Input.Keys.ENTER);assertEquals(OpeningInput.Action.START,input.take(null));
    }
}
