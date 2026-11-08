package com.nos.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exercita eventos e limites sem janela ou OpenGL. */
public class BrushInputTest {
    private final BrushInput input = new BrushInput();
    private final Viewport viewport = new Viewport() {
        @Override public Vector2 unproject(Vector2 point) {
            return point.set(point.x - getScreenX(), 100 - point.y);
        }
    };

    public BrushInputTest() {
        viewport.setScreenBounds(20, 0, 100, 100);
    }

    @Test public void mouseAndTouchShareCoordinates() {
        input.mouseMoved(50, 40);
        input.updatePosition(viewport, 100);
        assertTrue(input.isActive());
        assertEquals(30f, input.getX(), 0);
        assertEquals(60f, input.getY(), 0);
        input.touchDown(50, 40, 0, Input.Buttons.LEFT);
        input.updatePosition(viewport, 100);
        assertEquals(30f, input.getX(), 0);
        assertEquals(60f, input.getY(), 0);
    }

    @Test public void letterboxBarsAndResizeDeactivateGaze() {
        input.mouseMoved(19, 40);
        input.updatePosition(viewport, 100);
        assertFalse(input.isActive());
        input.mouseMoved(20, 0);
        input.updatePosition(viewport, 100);
        assertTrue(input.isActive());
        viewport.setScreenBounds(40, 0, 80, 100);
        input.updatePosition(viewport, 100);
        assertFalse(input.isActive());
        input.mouseMoved(120, 40);
        input.updatePosition(viewport, 100);
        assertFalse(input.isActive());
    }

    @Test public void releaseAndCancellationDeactivateGaze() {
        input.touchDown(50, 40, 0, Input.Buttons.LEFT);
        input.updatePosition(viewport, 100);
        input.touchUp(50, 40, 0, Input.Buttons.LEFT);
        assertFalse(input.isActive());
        input.touchDown(50, 40, 0, Input.Buttons.LEFT);
        input.updatePosition(viewport, 100);
        input.touchCancelled(50, 40, 0, Input.Buttons.LEFT);
        assertFalse(input.isActive());
    }

    @Test public void secondaryTouchDoesNotReplacePrimaryGaze() {
        input.touchDown(50, 40, 0, Input.Buttons.LEFT);
        assertFalse(input.touchDragged(100, 80, 1));
        assertFalse(input.touchUp(100, 80, 1, Input.Buttons.LEFT));
        input.updatePosition(viewport, 100);
        assertTrue(input.isActive());
        assertEquals(30f, input.getX(), 0);
    }

    @Test public void debugRequestsAreConsumedOnce() {
        assertTrue(input.keyDown(Input.Keys.TAB));
        assertTrue(input.consumeToggle());
        assertFalse(input.consumeToggle());
        assertTrue(input.keyDown(Input.Keys.R));
        assertTrue(input.consumeRestart());
        assertFalse(input.consumeRestart());
        assertFalse(input.keyDown(Input.Keys.A));
    }
}
