package com.nos.world;

import com.nos.data.RoomData;
import com.nos.data.PuzzleTuning;
import com.nos.entities.PuzzleHero;
import com.nos.systems.*;

/** Primeira sala: dois selos abrem a passagem, duas presenças concluem a saída. */
public final class MirrorPuzzle {
    public enum State { READY, PLAYING, PAUSED, WON }
    private final RoomData data;
    private final PuzzleHero shadow,reflection;
    private final AnchorSystem anchor=new AnchorSystem();
    private final MirrorMovementSystem movement=new MirrorMovementSystem();
    private final RoomCollisionSystem collision;
    private State state=State.READY;
    private boolean gateOpen;
    private int bindings;
    public MirrorPuzzle(RoomData data) {
        this.data=data;
        shadow=new PuzzleHero(data.shadowStart[0],data.shadowStart[1]);
        reflection=new PuzzleHero(data.reflectionStart[0],data.reflectionStart[1]);
        collision=new RoomCollisionSystem(data);
    }
    public void start() { if(state==State.READY) state=State.PLAYING; }
    public void togglePause() {
        if(state==State.PLAYING) state=State.PAUSED;
        else if(state==State.PAUSED) state=State.PLAYING;
        shadow.setMotion(0,0);reflection.setMotion(0,0);
    }
    public void toggleAnchor(AnchorSystem.Target target) {
        if(state!=State.PLAYING) return;
        anchor.toggle(target);bindings++;
    }
    public void update(float x,float y,float delta) {
        if(state!=State.PLAYING) return;
        movement.update(shadow,reflection,anchor,collision,x,y,delta,gateOpen);
        if(onPad(shadow,data.shadowPad)&&onPad(reflection,data.reflectionPad)) gateOpen=true;
        if(gateOpen&&onPad(shadow,data.shadowExit)&&onPad(reflection,data.reflectionExit)) {
            state=State.WON;shadow.setMotion(0,0);reflection.setMotion(0,0);
        }
    }
    public static boolean onPad(PuzzleHero hero,float[] pad) {
        float dx=hero.getX()-pad[0],dy=hero.getY()-pad[1];
        return dx*dx+dy*dy<=PuzzleTuning.PAD_RADIUS*PuzzleTuning.PAD_RADIUS;
    }
    public RoomData getData() { return data; }
    public State getState() { return state; }
    public PuzzleHero getShadow() { return shadow; }
    public PuzzleHero getReflection() { return reflection; }
    public AnchorSystem getAnchor() { return anchor; }
    public boolean isGateOpen() { return gateOpen; }
    public int getBindings() { return bindings; }
}
