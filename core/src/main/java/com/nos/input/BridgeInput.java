package com.nos.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Sombra recebe os comandos; Reflexo não pode ser selecionado pelo teclado. */
public final class BridgeInput extends InputAdapter {
    private final PuzzleInput delegate=new PuzzleInput();
    private BridgeKeyBindings bindings=new BridgeKeyBindings();
    private boolean helpHeld,help,touchControls=true,desktopTap;
    public void setTouchControls(boolean value) { touchControls=value; }
    public void setBindings(BridgeKeyBindings value) { clear();bindings=value; }
    @Override public boolean keyDown(int physicalKey) {
        int key=physicalKey==Input.Keys.ENTER?Input.Keys.ENTER:bindings.translate(physicalKey);
        if(key<0) return false;
        if(key==Input.Keys.H) { if(!helpHeld) help=true;helpHeld=true;return true; }
        return delegate.keyDown(key);
    }
    @Override public boolean keyUp(int physicalKey) {
        int key=physicalKey==Input.Keys.ENTER?Input.Keys.ENTER:bindings.translate(physicalKey);
        if(key<0) return false;
        if(key==Input.Keys.H) { helpHeld=false;return true; }return delegate.keyUp(key);
    }
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
    public int action() { return delegate.takeAnchor()==2?2:0; }
    public boolean start() { boolean value=delegate.takeStart()||desktopTap;desktopTap=false;return value; }
    public boolean help() { boolean value=help;help=false;return value; }
    public boolean pause() { return delegate.takePause(); }
    public boolean restart() { return delegate.takeRestart(); }
    public void clearForMenu() { help=false;desktopTap=false;delegate.suspendMovement(); }
    public void clear() { helpHeld=false;help=false;desktopTap=false;delegate.suspendMovement();delegate.clearHeld(); }
}
