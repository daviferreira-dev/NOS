package com.nos.world;

import com.nos.data.*;
import com.nos.entities.PuzzleHero;
import com.nos.systems.BridgeCollisionSystem;
import com.nos.systems.CompanionBehaviorSystem;

/** Sombra é controlada pelo jogador; Reflexo percebe e coopera com ações próprias. */
public final class BridgeEncounter {
    public enum State { READY, PLAYING, PAUSED, WON }
    private final BridgeData data;
    private final PuzzleHero[] heroes;
    private final BridgeCollisionSystem collision;
    private final CompanionBehaviorSystem companion;
    private final float[] safeX=new float[2],safeY=new float[2],steps=new float[2];
    private State state=State.READY;
    private float support,velocityX,velocityY,noticeTime;
    private boolean locked;
    private String notice="bridgeWelcome";
    public BridgeEncounter(BridgeData data) {
        this.data=data;collision=new BridgeCollisionSystem(data);
        companion=new CompanionBehaviorSystem(data,collision);
        heroes=new PuzzleHero[] {new PuzzleHero(data.shadowStart[0],data.shadowStart[1]),new PuzzleHero(data.reflectionStart[0],data.reflectionStart[1])};
        for(int i=0;i<2;i++) { safeX[i]=heroes[i].getX();safeY[i]=heroes[i].getY(); }
    }
    public void start() { if(state==State.READY) state=State.PLAYING; }
    public void togglePause() {
        if(state==State.PLAYING) state=State.PAUSED;
        else if(state==State.PAUSED) state=State.PLAYING;
        stop();
    }
    private void stop() { velocityX=0;velocityY=0;companion.resetNavigation();for(PuzzleHero h:heroes) h.setMotion(0,0); }
    private boolean near(PuzzleHero h,float[] point) {
        float dx=h.getX()-point[0],dy=h.getY()-point[1];
        return dx*dx+dy*dy<=BridgeTuning.INTERACT_DISTANCE*BridgeTuning.INTERACT_DISTANCE;
    }
    public void interact() {
        if(state!=State.PLAYING) return;
        PuzzleHero h=heroes[0];
        if(!locked&&isOpen()&&near(h,data.latch)) {
            locked=true;companion.releaseSupport();notify("bridgeFixed");stop();
        } else if(!locked&&near(h,data.winch)) {
            if(companion.isSupportingOrApproaching()) {
                companion.releaseSupport();notify("bridgeReleased");
            } else { companion.requestSupport();notify("bridgeHelpRequested"); }
            stop();
        } else notify(locked?"bridgeExit":"bridgeApproach");
    }
    public void update(float inputX,float inputY,float delta) {
        if(state!=State.PLAYING) return;
        if(!Float.isFinite(inputX)||!Float.isFinite(inputY)||!Float.isFinite(delta)||delta<0||delta>.1f)
            throw new IllegalArgumentException("Entrada inválida.");
        noticeTime=Math.max(0,noticeTime-delta);
        boolean held=companion.isHolding()&&near(heroes[1],data.winch);
        support=Math.max(0,Math.min(1,support+(held||locked?1:-1)*BridgeTuning.SUPPORT_SPEED*delta));
        float length=(float)Math.sqrt(inputX*inputX+inputY*inputY);
        if(length>1) { inputX/=length;inputY/=length; }
        float response=1-(float)Math.exp(-(length>.01f?BridgeTuning.ACCELERATION:BridgeTuning.STOP_ACCELERATION)*delta);
        velocityX+=(inputX*BridgeTuning.SPEED-velocityX)*response;
        velocityY+=(inputY*BridgeTuning.SPEED-velocityY)*response;
        if(length<.01f&&Math.abs(velocityX)+Math.abs(velocityY)<.1f) { velocityX=0;velocityY=0; }
        for(PuzzleHero h:heroes) h.setMotion(0,0);
        move(0,velocityX*delta,velocityY*delta);
        float previousX=heroes[1].getX(),previousY=heroes[1].getY();
        companion.update(heroes[1],heroes[0],isOpen(),locked,delta);
        recordSteps(1,previousX,previousY);
        if(!held&&companion.isHolding()) notify("bridgeHeld");
        for(int i=0;i<2;i++) {
            PuzzleHero h=heroes[i];
            if(!collision.walkable(h.getX(),h.getY(),isOpen())) {
                h.setPosition(safeX[i],safeY[i]);notify("bridgeSafeReturn");stop();
            }
            if(h.getX()<data.river[0]-BridgeTuning.RADIUS||h.getX()>data.river[0]+data.river[2]+BridgeTuning.RADIUS) {
                safeX[i]=h.getX();safeY[i]=h.getY();
            }
        }
        if(locked&&atExit(heroes[0])&&atExit(heroes[1])) { state=State.WON;stop(); }
    }
    private void move(int index,float dx,float dy) {
        PuzzleHero h=heroes[index];float x=h.getX(),y=h.getY();
        collision.move(h,dx,dy,isOpen());
        recordSteps(index,x,y);
    }
    private void recordSteps(int index,float x,float y) {
        PuzzleHero h=heroes[index];
        float actualX=h.getX()-x,actualY=h.getY()-y;
        steps[index]+=(float)Math.sqrt(actualX*actualX+actualY*actualY)*BridgeTuning.WALK_FRAME_PER_UNIT;
    }
    private boolean atExit(PuzzleHero h) {
        float[] e=data.exit;
        return h.getX()>=e[0]&&h.getX()<=e[0]+e[2]&&h.getY()>=e[1]&&h.getY()<=e[1]+e[3];
    }
    private void notify(String key) { notice=key;noticeTime=BridgeTuning.NOTICE_DURATION; }
    public String notice() { return noticeTime>0?notice:null; }
    public float noticeVisibility() { return Math.min(1,noticeTime/.5f); }
    public int objectiveStep() { return locked?3:companion.isHolding()?2:1; }
    public String objectiveKey() {
        if(companion.getState()==CompanionBehaviorSystem.State.APPROACHING_WINCH)
            return companion.needsRoom()?"bridgeGoalMakeRoom":"bridgeGoalWaiting";
        if(companion.isHolding()&&!isOpen()) return "bridgeGoalOpening";
        return "bridgeGoal"+objectiveStep();
    }
    public String hint() {
        if(noticeTime>0) return notice;
        return locked?"bridgeExit":companion.isHolding()?"bridgeCross":"bridgeWelcome";
    }
    public String action() {
        if(!locked&&isOpen()&&near(heroes[0],data.latch)) return "bridgeFixAction";
        if(!locked&&near(heroes[0],data.winch)) return companion.isSupportingOrApproaching()?"bridgeCallBackAction":"bridgeAskHelpAction";
        return "bridgeInteract";
    }
    public float[] interactionPoint() {
        if(!locked&&isOpen()&&near(heroes[0],data.latch)) return data.latch;
        if(!locked&&near(heroes[0],data.winch)) return data.winch;
        return null;
    }
    public String companionStatus() { return companion.status(); }
    public CompanionBehaviorSystem.State getCompanionState() { return companion.getState(); }
    public String companionSpeech() { return companion.speech(); }
    public boolean isOpen() { return support>=BridgeTuning.OPEN_THRESHOLD; }
    public boolean isLocked() { return locked; }
    public State getState() { return state; }
    public BridgeData getData() { return data; }
    public PuzzleHero hero(int index) { return heroes[index]; }
    public int getActive() { return 0; }
    public int getBound() { return companion.isHolding()?1:-1; }
    public float getSupport() { return support; }
    public int frame(int index) { return heroes[index].isMoving()?(int)steps[index]%4:1; }
}
