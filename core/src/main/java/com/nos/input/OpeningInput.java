package com.nos.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.nos.data.OpeningTuning;

/** O evento que pula o filme nunca é reaproveitado como comando de começar. */
public final class OpeningInput extends InputAdapter {
    public enum Action { NONE,SKIP,START,FROM_BEGINNING,REPLAY,MUSIC }
    private Action pending=Action.NONE;
    private boolean title,progressAvailable,enterHeld,skipHeld,replayHeld,freshHeld,click;
    private int clickX,clickY;
    private final Vector2 point=new Vector2();
    public void setTitle(boolean value) { if(title!=value) { pending=Action.NONE;click=false;title=value; } }
    public void setProgressAvailable(boolean value) { progressAvailable=value; }
    /** Impede que uma tecla ainda pressionada na tela anterior inicie outra partida. */
    public void seedHeldKeys(boolean enter,boolean replay,boolean fresh) {
        pending=Action.NONE;click=false;enterHeld=enter;replayHeld=replay;freshHeld=fresh;
    }
    @Override public boolean keyDown(int key) {
        if(key==Input.Keys.ENTER) { if(!enterHeld) pending=title?Action.START:Action.SKIP;enterHeld=true;return true; }
        if(key==Input.Keys.SPACE||key==Input.Keys.ESCAPE) { if(!skipHeld&&!title) pending=Action.SKIP;skipHeld=true;return true; }
        if(key==Input.Keys.R) { if(!replayHeld&&title) pending=Action.REPLAY;replayHeld=true;return true; }
        if(key==Input.Keys.N) { if(!freshHeld&&title&&progressAvailable) pending=Action.FROM_BEGINNING;freshHeld=true;return true; }
        return false;
    }
    @Override public boolean keyUp(int key) {
        if(key==Input.Keys.ENTER) enterHeld=false;
        if(key==Input.Keys.SPACE||key==Input.Keys.ESCAPE) skipHeld=false;
        if(key==Input.Keys.R) replayHeld=false;
        if(key==Input.Keys.N) freshHeld=false;
        return true;
    }
    @Override public boolean touchDown(int x,int y,int pointer,int button) {
        if(pointer!=0||button!=Input.Buttons.LEFT) return false;
        if(!title) pending=Action.SKIP;
        else { click=true;clickX=x;clickY=y; }
        return true;
    }
    public static Action buttonAt(float x,float y) {
        return buttonAt(x,y,false);
    }
    public static Action buttonAt(float x,float y,boolean progressAvailable) {
        if(inside(progressAvailable?OpeningTuning.RESUME_START:OpeningTuning.START,x,y)) return Action.START;
        if(progressAvailable&&inside(OpeningTuning.FROM_BEGINNING,x,y)) return Action.FROM_BEGINNING;
        if(inside(OpeningTuning.REPLAY,x,y)) return Action.REPLAY;
        if(inside(OpeningTuning.MUSIC,x,y)) return Action.MUSIC;
        return Action.NONE;
    }
    private static boolean inside(float[] box,float x,float y) { return x>=box[0]&&x<=box[0]+box[2]&&y>=box[1]&&y<=box[1]+box[3]; }
    public Action take(Viewport viewport) {
        if(click) {
            viewport.unproject(point.set(clickX,clickY));
            pending=buttonAt(point.x,point.y,progressAvailable);click=false;
        }
        Action result=pending;pending=Action.NONE;return result;
    }
    public void clear() { pending=Action.NONE;click=false;enterHeld=false;skipHeld=false;replayHeld=false;freshHeld=false; }
}
