package com.nos.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Teclado e controles de toque da sala de exploração. */
public final class PuzzleInput extends InputAdapter {
    private final boolean[] held=new boolean[256];
    private boolean start,pause,restart,tap,touch;
    private int anchor,screenX,screenY;
    private final Vector2 point=new Vector2();
    private boolean validKey(int key) { return key>=0&&key<held.length; }
    @Override public boolean keyDown(int key) {
        if(!validKey(key)) return false;
        if(held[key]) return true;
        held[key]=true;
        if(key==Input.Keys.ENTER) start=true;
        else if(key==Input.Keys.SPACE||key==Input.Keys.ESCAPE) pause=true;
        else if(key==Input.Keys.R) restart=true;
        else if(key==Input.Keys.Q) anchor=1;
        else if(key==Input.Keys.E) anchor=2;
        return true;
    }
    @Override public boolean keyUp(int key) { if(validKey(key)) held[key]=false;return true; }
    @Override public boolean touchDown(int x,int y,int pointer,int button) {
        if(pointer!=0||button!=Input.Buttons.LEFT) return false;
        screenX=x;screenY=y;touch=true;tap=true;return true;
    }
    @Override public boolean touchDragged(int x,int y,int pointer) {
        if(pointer!=0) return false;
        screenX=x;screenY=y;return true;
    }
    @Override public boolean touchUp(int x,int y,int pointer,int button) { if(pointer==0) touch=false;return true; }
    @Override public boolean touchCancelled(int x,int y,int pointer,int button) { return touchUp(x,y,pointer,button); }
    public void projectTouch(Viewport viewport,int screenHeight) {
        int bottom=screenHeight-1-screenY;
        boolean inside=screenX>=viewport.getScreenX()&&screenX<viewport.getScreenX()+viewport.getScreenWidth()
            &&bottom>=viewport.getScreenY()&&bottom<viewport.getScreenY()+viewport.getScreenHeight();
        if(inside) viewport.unproject(point.set(screenX,screenY));
        else { point.set(-100,-100);touch=false; }
        if(tap&&point.y>=20&&point.y<=80) {
            if(point.x>=630&&point.x<=760) anchor=1;
            if(point.x>=780&&point.x<=910) anchor=2;
        }
    }
    public float axisX() {
        float value=(held[Input.Keys.D]||held[Input.Keys.RIGHT]?1:0)-(held[Input.Keys.A]||held[Input.Keys.LEFT]?1:0);
        if(touch&&point.x<180&&point.y<90&&point.x>=0&&point.y>=0) value+=(point.x-90)/45;
        return Math.max(-1,Math.min(1,value));
    }
    public float axisY() {
        float value=(held[Input.Keys.W]||held[Input.Keys.UP]?1:0)-(held[Input.Keys.S]||held[Input.Keys.DOWN]?1:0);
        if(touch&&point.x<180&&point.y<90&&point.x>=0&&point.y>=0) value+=(point.y-50)/35;
        return Math.max(-1,Math.min(1,value));
    }
    public boolean takeStart() { boolean v=start||tap;start=false;tap=false;return v; }
    public boolean takePause() { boolean v=pause;pause=false;return v; }
    public boolean takeRestart() { boolean v=restart;restart=false;return v; }
    public int takeAnchor() { int v=anchor;anchor=0;return v; }
    public void clearHeld() { java.util.Arrays.fill(held,false);touch=false; }
}
