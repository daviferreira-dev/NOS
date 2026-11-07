package com.nos.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Um corpo ativo, troca e uma única ação contextual. */
public final class BridgeInput extends InputAdapter {
    private final PuzzleInput delegate=new PuzzleInput();
    private boolean tabHeld,swap;
    private boolean helpHeld,help,touchControls=true,desktopTap;
    public void setTouchControls(boolean value) { touchControls=value; }
    @Override public boolean keyDown(int key) {
        if(key==Input.Keys.H) { if(!helpHeld) help=true;helpHeld=true;return true; }
        if(key==Input.Keys.TAB) { if(!tabHeld) swap=true;tabHeld=true;return true; }
        return delegate.keyDown(key);
    }
    @Override public boolean keyUp(int key) { if(key==Input.Keys.H) { helpHeld=false;return true; }if(key==Input.Keys.TAB) { tabHeld=false;return true; }return delegate.keyUp(key); }
    @Override public boolean touchDown(int x,int y,int p,int b) {
        if(touchControls) return delegate.touchDown(x,y,p,b);
        if(p==0&&b==com.badlogic.gdx.Input.Buttons.LEFT) { desktopTap=true;return true; }return false;
    }
    @Override public boolean touchDragged(int x,int y,int p) { return touchControls&&delegate.touchDragged(x,y,p); }
    @Override public boolean touchUp(int x,int y,int p,int b) { return touchControls&&delegate.touchUp(x,y,p,b); }
    @Override public boolean touchCancelled(int x,int y,int p,int b) { return touchControls&&delegate.touchCancelled(x,y,p,b); }
    public void project(Viewport v,int h) { if(touchControls) delegate.projectTouch(v,h); }
    public float x() { return delegate.axisX(); }
    public float y() { return delegate.axisY(); }
    public int action() { int a=delegate.takeAnchor();if(swap) { swap=false;return 1; }return a; }
    public boolean start() { boolean value=delegate.takeStart()||desktopTap;desktopTap=false;return value; }
    public boolean help() { boolean value=help;help=false;return value; }
    public boolean pause() { return delegate.takePause(); }
    public boolean restart() { return delegate.takeRestart(); }
    public void clear() { tabHeld=false;swap=false;helpHeld=false;help=false;desktopTap=false;delegate.clearHeld(); }
}
