package com.nos.world;

import com.nos.data.*;
import com.nos.entities.PuzzleHero;
import com.nos.systems.BridgeCollisionSystem;
import com.nos.systems.CompanionBehaviorSystem;
import com.nos.systems.GardenGateSystem;
import com.nos.systems.RelayGateSystem;
import com.nos.systems.EnvironmentalActionSystem;
import com.nos.systems.PhaseChallenge;
import com.nos.systems.FurnaceSystem;
import com.nos.systems.RidgeSystem;
import com.nos.systems.ObservatorySystem;

/** Sombra é controlada pelo jogador; Reflexo percebe e coopera com ações próprias. */
public final class BridgeEncounter {
    public enum State { READY, PLAYING, PAUSED, WON }
    private final BridgeData data;
    private final PhaseChallenge challenge;
    private final PuzzleHero[] heroes;
    private final BridgeCollisionSystem collision;
    private final CompanionBehaviorSystem companion;
    private final GardenGateSystem garden;
    private final RelayGateSystem relay;
    private final EnvironmentalActionSystem actions;
    private final float[] safeX=new float[2],safeY=new float[2],steps=new float[2];
    private State state=State.READY;
    private float support,velocityX,velocityY,noticeTime;
    private boolean locked;
    private String notice="bridgeWelcome";
    private boolean introducedNearbyHazard;
    public BridgeEncounter(BridgeData data) {
        this.data=data;collision=new BridgeCollisionSystem(data);
        if(data.furnace!=null) challenge=new FurnaceSystem(data,collision);
        else if(data.ridge!=null) challenge=new RidgeSystem(data,collision);
        else if(data.observatory!=null) challenge=new ObservatorySystem(data,collision);
        else challenge=null;
        companion=new CompanionBehaviorSystem(data,collision);
        garden=data.garden==null?null:new GardenGateSystem(data,collision);
        relay=data.relay==null?null:new RelayGateSystem(data,collision);
        actions=new EnvironmentalActionSystem(data,collision);
        heroes=new PuzzleHero[] {new PuzzleHero(data.shadowStart[0],data.shadowStart[1]),new PuzzleHero(data.reflectionStart[0],data.reflectionStart[1])};
        if(data.gardenOnly) { locked=true;support=1; }
        for(int i=0;i<2;i++) { safeX[i]=heroes[i].getX();safeY[i]=heroes[i].getY(); }
    }
    public void start() { if(state==State.READY) { state=State.PLAYING;if(challenge!=null) notify(data.furnace!=null?"furnaceWelcome":data.ridge!=null?"ridgeWelcome":"observatoryWelcome");else if(data.gardenOnly) notify(data.phaseNumber==4?"irisWelcome":isRelayActive()?"relayWelcome":"transferWelcome"); } }
    /** Reinício explícito do trecho já alcançado; não é um comando de ajuda. */
    public void startAtGardenCheckpoint() {
        float[] shadow=garden==null?data.checkpointShadow:data.garden.checkpointShadow;
        float[] reflection=garden==null?data.checkpointReflection:data.garden.checkpointReflection;
        if(shadow==null||reflection==null) { start();return; }
        locked=true;support=1;state=State.PLAYING;
        companion.releaseSupport();
        heroes[0].setPosition(shadow[0],shadow[1]);
        heroes[1].setPosition(reflection[0],reflection[1]);
        for(int i=0;i<2;i++) { safeX[i]=heroes[i].getX();safeY[i]=heroes[i].getY(); }
        stop();notify(data.checkpointShadow!=null?"waterCheckpoint":data.gardenOnly?"transferWelcome":"gardenCheckpoint");
    }
    public void togglePause() {
        if(state==State.PLAYING) state=State.PAUSED;
        else if(state==State.PAUSED) state=State.PLAYING;
        stop();
    }
    private void stop() { velocityX=0;velocityY=0;companion.resetNavigation();if(challenge!=null) challenge.resetNavigation();if(garden!=null) garden.resetNavigation();if(relay!=null) relay.resetNavigation();for(PuzzleHero h:heroes) h.setMotion(0,0); }
    private boolean near(PuzzleHero h,float[] point) {
        float dx=h.getX()-point[0],dy=h.getY()-point[1];
        return dx*dx+dy*dy<=BridgeTuning.INTERACT_DISTANCE*BridgeTuning.INTERACT_DISTANCE;
    }
    public void interact() {
        if(state!=State.PLAYING) return;
        PuzzleHero h=heroes[0];
        if(challenge!=null) {
            if(challenge.interact(h,heroes[1])) { stop();drainChallengeNotice(); }
            return;
        }
        if(isRelayActive()) {
            if(relay.interact(h,heroes[1])) { drainRelayNotice();stop(); }
            else notify("relayLookAround");
            return;
        }
        if(locked&&garden!=null) {
            if(garden.interact(h,heroes[1])) { drainGardenNotice();stop(); }
            else notify(garden.isOpen()?"gardenPassage":"gardenLookAround");
            return;
        }
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
        update(inputX,inputY,delta,false);
    }
    public void update(float inputX,float inputY,float delta,boolean sprint) {
        if(state!=State.PLAYING) return;
        if(!Float.isFinite(inputX)||!Float.isFinite(inputY)||!Float.isFinite(delta)||delta<0||delta>.1f)
            throw new IllegalArgumentException("Entrada inválida.");
        noticeTime=Math.max(0,noticeTime-delta);
        boolean held=companion.isHolding()&&near(heroes[1],data.winch);
        support=Math.max(0,Math.min(1,support+(held||locked?1:-1)*BridgeTuning.SUPPORT_SPEED*delta));
        float length=(float)Math.sqrt(inputX*inputX+inputY*inputY);
        if(length>1) { inputX/=length;inputY/=length; }
        float response=1-(float)Math.exp(-(length>.01f?BridgeTuning.ACCELERATION:BridgeTuning.STOP_ACCELERATION)*delta);
        float speed=garden!=null&&garden.isDragging()?GardenTuning.DRAG_SPEED:sprint?ActionTuning.SPRINT_SPEED:BridgeTuning.SPEED;
        velocityX+=(inputX*speed-velocityX)*response;
        velocityY+=(inputY*speed-velocityY)*response;
        if(length<.01f&&Math.abs(velocityX)+Math.abs(velocityY)<.1f) { velocityX=0;velocityY=0; }
        for(PuzzleHero h:heroes) h.setMotion(0,0);
        if(challenge instanceof RidgeSystem && ((RidgeSystem)challenge).isRiding()) { velocityX=0;velocityY=0; }
        move(0,velocityX*delta,velocityY*delta);
        float previousX=heroes[1].getX(),previousY=heroes[1].getY();
        boolean relayActive=isRelayActive(),gardenActive=garden!=null&&locked&&!relayActive;
        boolean companionControlled=relayActive?relay.controlsCompanion():gardenActive&&garden.controlsCompanion();
        if(!companionControlled&&(challenge==null||!challenge.controlsCompanion())) companion.update(heroes[1],heroes[0],isOpen(),locked,delta);
        if(gardenActive) {
            garden.update(heroes[0],heroes[1],isOpen(),delta);drainGardenNotice();
            if(relay!=null&&garden.isOpen()&&garden.isDragging()) {
                // A primeira etapa termina com a peça apoiada; o próximo pedido é o revezamento.
                garden.interact(heroes[0],heroes[1]);garden.pollNotice();stop();
            }
        }
        if(relayActive) { relay.update(heroes[0],heroes[1],isOpen(),delta);drainRelayNotice(); }
        if(challenge!=null) { challenge.update(heroes[0],heroes[1],delta);drainChallengeNotice(); }
        recordSteps(1,previousX,previousY);
        if(!held&&companion.isHolding()) notify("bridgeHeld");
        float beforeActionX=heroes[0].getX(),beforeActionY=heroes[0].getY();
        actions.setBridgeOpen(isOpen());actions.update(heroes[0],heroes[1],delta);
        if(garden!=null&&garden.isDragging()
            &&(beforeActionX!=heroes[0].getX()||beforeActionY!=heroes[0].getY())) {
            garden.interact(heroes[0],heroes[1]);drainGardenNotice();
        }
        if(actions.didRecover()) {
            stop();notify("actionRecovered");
        }
        introduceNearbyHazard();
        for(int i=0;i<2;i++) {
            PuzzleHero h=heroes[i];
            if(!collision.walkable(h.getX(),h.getY(),isOpen())) {
                h.setPosition(safeX[i],safeY[i]);notify("bridgeSafeReturn");stop();
            }
            if(h.getX()<data.river[0]-BridgeTuning.RADIUS||h.getX()>data.river[0]+data.river[2]+BridgeTuning.RADIUS) {
                safeX[i]=h.getX();safeY[i]=h.getY();
            }
        }
        if(locked&&(challenge==null||challenge.allowsExit())&&(garden==null||garden.isOpen())&&(relay==null||relay.isLatched())&&reachedDestination()) { state=State.WON;stop(); }
    }
    private void move(int index,float dx,float dy) {
        PuzzleHero h=heroes[index];float x=h.getX(),y=h.getY();
        if(index==0&&garden!=null&&locked&&!isRelayActive()) garden.movePlayer(h,heroes[1],dx,dy,isOpen());
        else collision.move(h,dx,dy,isOpen());
        recordSteps(index,x,y);
    }
    private void recordSteps(int index,float x,float y) {
        PuzzleHero h=heroes[index];
        float actualX=h.getX()-x,actualY=h.getY()-y;
        steps[index]+=(float)Math.sqrt(actualX*actualX+actualY*actualY)*BridgeTuning.WALK_FRAME_PER_UNIT;
    }
    private boolean reachedDestination() {
        if(data.checkpointShadow==null) return atExit(heroes[0])&&atExit(heroes[1]);
        float farBank=data.river[0]+data.river[2]+BridgeTuning.RADIUS;
        float dx=heroes[0].getX()-heroes[1].getX(),dy=heroes[0].getY()-heroes[1].getY();
        return heroes[0].getX()>=farBank && heroes[1].getX()>=farBank
            && dx*dx+dy*dy<=BridgeTuning.REUNION_DISTANCE*BridgeTuning.REUNION_DISTANCE;
    }
    private boolean atExit(PuzzleHero h) {
        float[] e=data.exit;
        return h.getX()>=e[0]&&h.getX()<=e[0]+e[2]&&h.getY()>=e[1]&&h.getY()<=e[1]+e[3];
    }
    private void notify(String key) { notice=key;noticeTime=BridgeTuning.NOTICE_DURATION; }
    private void introduceNearbyHazard() {
        if(introducedNearbyHazard || noticeTime>0 || (data.phaseNumber!=2 && data.phaseNumber!=3)) return;
        for(int i=0;i<actions.count();i++) {
            float dx=heroes[0].getX()-actions.hazardX(i);
            float dy=heroes[0].getY()-actions.hazardY(i);
            if(dx*dx+dy*dy>ActionTuning.HINT_DISTANCE*ActionTuning.HINT_DISTANCE) continue;
            notify("gust".equals(data.hazards[i].type)?"gustApproach":"emberApproach");
            introducedNearbyHazard=true;
            return;
        }
    }
    public PhaseChallenge challenge() { return challenge; }
    private void drainChallengeNotice() { String key=challenge.pollNotice();if(key!=null) notify(key); }
    private void drainGardenNotice() { String key=garden.pollNotice();if(key!=null) notify(key); }
    private void drainRelayNotice() { String key=relay.pollNotice();if(key!=null) notify(key); }
    public String notice() { return noticeTime>0?notice:null; }
    public float noticeVisibility() { return Math.min(1,noticeTime/.5f); }
    public int objectiveStep() {
        if(challenge!=null) return challenge.objectiveStep();
        if(isRelayActive()) return (garden==null?0:3)+relay.objectiveStep();
        return locked&&garden!=null?garden.objectiveStep():locked?3:companion.isHolding()?2:1;
    }
    public int objectiveCount() { if(challenge!=null) return challenge.objectiveCount();return garden!=null&&relay!=null?7:data.gardenOnly?4:garden==null?3:5; }
    public String objectiveKey() {
        if(challenge!=null) return challenge.objectiveKey();
        if(isRelayActive()) return relay.objectiveKey();
        if(locked&&garden!=null) return garden.objectiveKey();
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
        if(challenge!=null) { String key=challenge.actionKey(heroes[0]);return key==null?"bridgeInteract":key; }
        if(isRelayActive()) { String key=relay.actionKey(heroes[0]);return key==null?"bridgeInteract":key; }
        if(locked&&garden!=null) { String key=garden.actionKey(heroes[0]);return key==null?"bridgeInteract":key; }
        if(!locked&&isOpen()&&near(heroes[0],data.latch)) return "bridgeFixAction";
        if(!locked&&near(heroes[0],data.winch)) return companion.isSupportingOrApproaching()?"bridgeCallBackAction":"bridgeAskHelpAction";
        return "bridgeInteract";
    }
    public float[] interactionPoint() {
        if(challenge!=null) return challenge.interactionPoint(heroes[0]);
        if(isRelayActive()) return relay.interactionPoint(heroes[0]);
        if(locked&&garden!=null) return garden.interactionPoint(heroes[0]);
        if(!locked&&isOpen()&&near(heroes[0],data.latch)) return data.latch;
        if(!locked&&near(heroes[0],data.winch)) return data.winch;
        return null;
    }
    public String companionStatus() {
        if(challenge!=null) { String key=challenge.companionStatus();return key==null?companion.status():key; }
        if(isRelayActive()) { String key=relay.companionStatus();if(key!=null) return key; }
        else if(locked&&garden!=null) { String key=garden.companionStatus();if(key!=null) return key; }
        return companion.status();
    }
    public boolean hasGarden() { return garden!=null; }
    public GardenGateSystem garden() { return garden; }
    public boolean hasRelay() { return relay!=null; }
    public RelayGateSystem relay() { return relay; }
    public boolean isRelayActive() { return relay!=null&&(garden==null||garden.isOpen()); }
    public EnvironmentalActionSystem actions() { return actions; }
    public CompanionBehaviorSystem.State getCompanionState() { return companion.getState(); }
    public String companionSpeech() { return challenge!=null?challenge.speech():companion.speech(); }
    public boolean isOpen() { return support>=BridgeTuning.OPEN_THRESHOLD; }
    public boolean isLocked() { return locked; }
    public State getState() { return state; }
    public BridgeData getData() { return data; }
    public PuzzleHero hero(int index) { return heroes[index]; }
    public int getActive() { return 0; }
    public int getBound() {
        return (isRelayActive()?relay.isHoldingWest():garden!=null&&garden.isHelperHolding())||companion.isHolding()?1:-1;
    }
    public float getSupport() { return support; }
    public int frame(int index) { return heroes[index].isMoving()?(int)steps[index]%4:1; }
}
