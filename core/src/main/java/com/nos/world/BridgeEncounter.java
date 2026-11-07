package com.nos.world;

import com.nos.data.*;
import com.nos.entities.PuzzleHero;
import com.nos.systems.BridgeCollisionSystem;

/** Um segura, o outro atravessa e fixa a ponte; depois ambos seguem. */
public final class BridgeEncounter {
    public enum State { READY, PLAYING, PAUSED, WON }
    private final BridgeData data;
    private final PuzzleHero[] heroes;
    private final BridgeCollisionSystem collision;
    private final float[] safeX=new float[2],safeY=new float[2],steps=new float[2];
    private State state=State.READY;
    private int active,bound=-1;
    private float support,velocityX,velocityY,noticeTime;
    private boolean locked;
    private String notice="bridgeWelcome";
    public BridgeEncounter(BridgeData data) {
        this.data=data;collision=new BridgeCollisionSystem(data);
        heroes=new PuzzleHero[] {new PuzzleHero(data.shadowStart[0],data.shadowStart[1]),new PuzzleHero(data.reflectionStart[0],data.reflectionStart[1])};
        for(int i=0;i<2;i++) { safeX[i]=heroes[i].getX();safeY[i]=heroes[i].getY(); }
    }
    public void start() { if(state==State.READY) state=State.PLAYING; }
    public void togglePause() {
        if(state==State.PLAYING) state=State.PAUSED;
        else if(state==State.PAUSED) state=State.PLAYING;
        stop();
    }
    private void stop() { velocityX=0;velocityY=0;for(PuzzleHero h:heroes) h.setMotion(0,0); }
    public void swap() { if(state==State.PLAYING) { active=1-active;stop(); } }
    private boolean near(PuzzleHero h,float[] point) {
        float dx=h.getX()-point[0],dy=h.getY()-point[1];
        return dx*dx+dy*dy<=BridgeTuning.INTERACT_DISTANCE*BridgeTuning.INTERACT_DISTANCE;
    }
    public void interact() {
        if(state!=State.PLAYING) return;
        PuzzleHero h=heroes[active];
        if(!locked&&isOpen()&&near(h,data.latch)) {
            locked=true;bound=-1;notify("bridgeFixed");stop();
        } else if(bound==active) {
            bound=-1;notify("bridgeReleased");stop();
        } else if(!locked&&near(h,data.winch)) {
            bound=active;h.setPosition(data.winch[0],data.winch[1]);
            active=1-active;notify("bridgeHeld");stop();
        } else notify(locked?"bridgeExit":"bridgeApproach");
    }
    public void update(float inputX,float inputY,float delta) {
        if(state!=State.PLAYING) return;
        if(!Float.isFinite(inputX)||!Float.isFinite(inputY)||!Float.isFinite(delta)||delta<0||delta>.1f)
            throw new IllegalArgumentException("Entrada inválida.");
        noticeTime=Math.max(0,noticeTime-delta);
        boolean held=bound>=0&&near(heroes[bound],data.winch);
        support=Math.max(0,Math.min(1,support+(held||locked?1:-1)*BridgeTuning.SUPPORT_SPEED*delta));
        float length=(float)Math.sqrt(inputX*inputX+inputY*inputY);
        if(length>1) { inputX/=length;inputY/=length; }
        float response=1-(float)Math.exp(-(length>.01f?BridgeTuning.ACCELERATION:BridgeTuning.STOP_ACCELERATION)*delta);
        velocityX+=(inputX*BridgeTuning.SPEED-velocityX)*response;
        velocityY+=(inputY*BridgeTuning.SPEED-velocityY)*response;
        if(length<.01f&&Math.abs(velocityX)+Math.abs(velocityY)<.1f) { velocityX=0;velocityY=0; }
        for(PuzzleHero h:heroes) h.setMotion(0,0);
        if(active!=bound) move(active,velocityX*delta,velocityY*delta);
        int other=1-active;
        if(other!=bound) {
            PuzzleHero follower=heroes[other],leader=heroes[active];
            float dx=leader.getX()-follower.getX(),dy=leader.getY()-follower.getY();
            float distance=(float)Math.sqrt(dx*dx+dy*dy);
            if(distance>BridgeTuning.FOLLOW_DISTANCE) {
                float travel=Math.min(distance-BridgeTuning.FOLLOW_DISTANCE,BridgeTuning.FOLLOW_SPEED*delta);
                move(other,dx/distance*travel,dy/distance*travel);
            }
        }
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
        float actualX=h.getX()-x,actualY=h.getY()-y;
        steps[index]+=(float)Math.sqrt(actualX*actualX+actualY*actualY)*BridgeTuning.WALK_FRAME_PER_UNIT;
    }
    private boolean atExit(PuzzleHero h) {
        float[] e=data.exit;
        return h.getX()>=e[0]&&h.getX()<=e[0]+e[2]&&h.getY()>=e[1]&&h.getY()<=e[1]+e[3];
    }
    private void notify(String key) { notice=key;noticeTime=3; }
    public String hint() {
        if(noticeTime>0) return notice;
        return locked?"bridgeExit":bound>=0?"bridgeCross":"bridgeWelcome";
    }
    public String action() {
        if(bound==active) return "bridgeReleaseAction";
        if(!locked&&isOpen()&&near(heroes[active],data.latch)) return "bridgeFixAction";
        if(!locked&&near(heroes[active],data.winch)) return "bridgeTieAction";
        return "bridgeInteract";
    }
    public boolean isOpen() { return support>=BridgeTuning.OPEN_THRESHOLD; }
    public boolean isLocked() { return locked; }
    public State getState() { return state; }
    public BridgeData getData() { return data; }
    public PuzzleHero hero(int index) { return heroes[index]; }
    public int getActive() { return active; }
    public int getBound() { return bound; }
    public float getSupport() { return support; }
    public int frame(int index) { return heroes[index].isMoving()?(int)steps[index]%4:1; }
}
