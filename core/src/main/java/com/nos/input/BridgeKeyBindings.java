package com.nos.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

/** Teclas da travessia, com validação e aliases fixos para setas e Escape. */
public final class BridgeKeyBindings {
    public enum Action { UP,DOWN,LEFT,RIGHT,INTERACT,HELP,PAUSE,RESTART }
    public enum Assignment { ACCEPTED,RESERVED,IN_USE }
    private static final int[] DEFAULTS={Input.Keys.W,Input.Keys.S,Input.Keys.A,Input.Keys.D,Input.Keys.E,Input.Keys.H,Input.Keys.SPACE,Input.Keys.R};
    private final int[] keys=DEFAULTS.clone();
    public int key(Action action) { return keys[action.ordinal()]; }
    public Assignment assign(Action action,int key) {
        if(reserved(key)) return Assignment.RESERVED;
        for(Action other:Action.values()) if(other!=action&&key(other)==key) return Assignment.IN_USE;
        keys[action.ordinal()]=key;return Assignment.ACCEPTED;
    }
    public int translate(int key) {
        for(Action action:Action.values()) if(key(action)==key) return DEFAULTS[action.ordinal()];
        if(key==Input.Keys.UP) return Input.Keys.W;
        if(key==Input.Keys.DOWN) return Input.Keys.S;
        if(key==Input.Keys.LEFT) return Input.Keys.A;
        if(key==Input.Keys.RIGHT) return Input.Keys.D;
        if(key==Input.Keys.ESCAPE) return Input.Keys.SPACE;
        return -1;
    }
    public void reset() { System.arraycopy(DEFAULTS,0,keys,0,keys.length); }
    private static boolean reserved(int key) {
        return key<=0||key>=256||key==Input.Keys.ENTER||key==Input.Keys.ESCAPE||key==Input.Keys.TAB||key==Input.Keys.M
            ||key==Input.Keys.UP||key==Input.Keys.DOWN||key==Input.Keys.LEFT||key==Input.Keys.RIGHT
            ||key==Input.Keys.F6||key==Input.Keys.F7||key==Input.Keys.F8||key==Input.Keys.F11
            ||key==Input.Keys.SHIFT_LEFT||key==Input.Keys.SHIFT_RIGHT||key==Input.Keys.CONTROL_LEFT||key==Input.Keys.CONTROL_RIGHT
            ||key==Input.Keys.ALT_LEFT||key==Input.Keys.ALT_RIGHT;
    }
    public void load(Preferences preferences) {
        int[] loaded=new int[keys.length];
        for(Action action:Action.values()) loaded[action.ordinal()]=preferences.getInteger("key."+action.name(),DEFAULTS[action.ordinal()]);
        for(int i=0;i<loaded.length;i++) {
            if(reserved(loaded[i])) { reset();return; }
            for(int j=0;j<i;j++) if(loaded[i]==loaded[j]) { reset();return; }
        }
        System.arraycopy(loaded,0,keys,0,keys.length);
    }
    public void save(Preferences preferences) {
        for(Action action:Action.values()) preferences.putInteger("key."+action.name(),key(action));
        preferences.flush();
    }
}
