package com.nos.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Sombra recebe os comandos; Reflexo não pode ser selecionado pelo teclado. */
public final class BridgeInput extends InputAdapter {
    private final PuzzleInput delegate=new PuzzleInput();
    private final boolean[] physicalHeld=new boolean[256];
    private final int[] activeCommands=new int[256];
    private BridgeKeyBindings bindings=new BridgeKeyBindings();
    private boolean helpHeld,help,touchControls=true,desktopTap;
    public void setTouchControls(boolean value) { touchControls=value; }
    public void setBindings(BridgeKeyBindings value) { clear();bindings=value; }
    @Override public boolean keyDown(int physicalKey) {
        if(physicalKey<0||physicalKey>=physicalHeld.length) return false;
        if(physicalKey==Input.Keys.SHIFT_LEFT||physicalKey==Input.Keys.SHIFT_RIGHT) { physicalHeld[physicalKey]=true;return true; }
        int key=physicalKey==Input.Keys.ENTER?Input.Keys.ENTER:bindings.translate(physicalKey);
        if(key<0) return false;
        if(physicalHeld[physicalKey]) return true;
        physicalHeld[physicalKey]=true;
        boolean alreadyActive=isCommandActive(key);
        activeCommands[physicalKey]=key;
        if(alreadyActive) return true;
        if(key==Input.Keys.H) { if(!helpHeld) help=true;helpHeld=true;return true; }
        return delegate.keyDown(key);
    }
    @Override public boolean keyUp(int physicalKey) {
        if(physicalKey<0||physicalKey>=physicalHeld.length) return false;
        if(!physicalHeld[physicalKey]) return physicalKey==Input.Keys.ENTER||bindings.translate(physicalKey)>=0;
        physicalHeld[physicalKey]=false;
        int key=activeCommands[physicalKey];activeCommands[physicalKey]=0;
        if(key==0||isCommandActive(key)) return true;
        if(key==Input.Keys.H) { helpHeld=false;return true; }return delegate.keyUp(key);
    }
    private boolean isCommandActive(int command) {
        for(int active:activeCommands) if(active==command) return true;
        return false;
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
    public boolean isSprinting() { return physicalHeld[Input.Keys.SHIFT_LEFT]||physicalHeld[Input.Keys.SHIFT_RIGHT]; }
    public float y() { return delegate.axisY(); }
    public int action() { return delegate.takeAnchor()==2?2:0; }
    public boolean start() { boolean value=delegate.takeStart()||desktopTap;desktopTap=false;return value; }
    public boolean help() { boolean value=help;help=false;return value; }
    public boolean pause() { return delegate.takePause(); }
    public boolean restart() { return delegate.takeRestart(); }
    public void clearForMenu() {
        help=false;desktopTap=false;delegate.suspendMovement();
        // Teclas mantidas não voltam a mover por autorrepetição ao fechar o menu.
        for(int i=0;i<activeCommands.length;i++) {
            int key=activeCommands[i];
            if(key==Input.Keys.W||key==Input.Keys.A||key==Input.Keys.S||key==Input.Keys.D) activeCommands[i]=0;
        }
    }
    public void clear() {
        helpHeld=false;help=false;desktopTap=false;delegate.suspendMovement();delegate.clearHeld();
        java.util.Arrays.fill(physicalHeld,false);java.util.Arrays.fill(activeCommands,0);
    }
}
