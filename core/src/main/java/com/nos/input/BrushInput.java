package com.nos.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Entrada comum ao mouse e toque, com conversão para o espaço da fase. */
public final class BrushInput extends InputAdapter {
    private final Vector2 position = new Vector2();
    private int screenX;
    private int screenY;
    private boolean active;
    private boolean inside;
    private boolean toggleRequested;
    private boolean restartRequested;
    private boolean pressRequested;
    private boolean pauseRequested;
    private int difficultyChoice;

    private void move(int x, int y) {
        screenX = x;
        screenY = y;
        active = true;
    }

    @Override
    public boolean mouseMoved(int x, int y) { move(x, y); return true; }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if (pointer != 0 || button != Input.Buttons.LEFT) return false;
        move(x, y);
        pressRequested = true;
        return true;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (pointer != 0) return false;
        move(x, y);
        return true;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        if (pointer != 0) return false;
        deactivate();
        return true;
    }

    @Override
    public boolean touchCancelled(int x, int y, int pointer, int button) {
        if (pointer != 0) return false;
        deactivate();
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.TAB) toggleRequested = true;
        else if (keycode == Input.Keys.R) restartRequested = true;
        else if (keycode == Input.Keys.ENTER) pressRequested = true;
        else if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ESCAPE) pauseRequested = true;
        else if (keycode == Input.Keys.NUM_1) difficultyChoice = 1;
        else if (keycode == Input.Keys.NUM_2) difficultyChoice = 2;
        else if (keycode == Input.Keys.NUM_3) difficultyChoice = 3;
        else return false;
        return true;
    }

    public void updatePosition(Viewport viewport, int screenHeight) {
        int bottomY = screenHeight - 1 - screenY;
        inside = active && screenX >= viewport.getScreenX()
            && screenX < viewport.getScreenX() + viewport.getScreenWidth()
            && bottomY >= viewport.getScreenY()
            && bottomY < viewport.getScreenY() + viewport.getScreenHeight();
        if (inside) viewport.unproject(position.set(screenX, screenY));
    }

    public boolean consumeToggle() {
        boolean requested = toggleRequested;
        toggleRequested = false;
        return requested;
    }

    public boolean consumeRestart() {
        boolean requested = restartRequested;
        restartRequested = false;
        return requested;
    }

    public void deactivate() { active = false; inside = false; }
    public boolean consumePress() { boolean value = pressRequested; pressRequested = false; return value; }
    public boolean consumePause() { boolean value = pauseRequested; pauseRequested = false; return value; }
    public int consumeDifficulty() { int value = difficultyChoice; difficultyChoice = 0; return value; }
    public boolean isActive() { return inside; }
    public float getX() { return position.x; }
    public float getY() { return position.y; }
}
