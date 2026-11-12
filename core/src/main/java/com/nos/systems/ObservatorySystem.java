package com.nos.systems;

import com.nos.data.BridgeData;
import com.nos.data.ObservatoryData;
import com.nos.data.ObservatoryTuning;
import com.nos.entities.PuzzleHero;

/** Reflexo indica uma passagem depois de percorrer a galeria; Sombra escolhe por onde entrar. */
public final class ObservatorySystem implements PhaseChallenge {
    public enum State { WAITING, WALKING_GALLERY, SIGNALLING, PASSAGE_OPEN, RETURNING, REUNITED }
    private final ObservatoryData data;
    private final BridgeCollisionSystem collision;
    private final CompanionFollowSystem navigation;
    private State state=State.WAITING;
    private int waypoint;
    private String notice;

    public ObservatorySystem(BridgeData bridge,BridgeCollisionSystem collision) {
        if(bridge.observatory==null) throw new IllegalArgumentException("Observatório ausente.");
        data=bridge.observatory;this.collision=collision;
        if(data.passages==null||data.gates==null||data.approaches==null||data.correctPassage<0
            ||data.correctPassage>=data.gates.length||data.gates.length!=data.approaches.length
            ||data.galleryRoute==null||data.galleryRoute.length==0||data.returnRoute==null||data.returnRoute.length==0)
            throw new IllegalArgumentException("Passagens do observatório inválidas.");
        navigation=new CompanionFollowSystem(bridge,collision);
        for(int i=0;i<data.gates.length;i++) collision.setObservatoryPassageOpen(i,false);
    }
    @Override public void update(PuzzleHero player,PuzzleHero other,float delta) {
        if(!Float.isFinite(delta)||delta<0||delta>.1f) throw new IllegalArgumentException("Intervalo inválido.");
        other.setMotion(0,0);
        if(delta==0) return;
        if(state==State.WAITING&&near(player,data.observationPoint,ObservatoryTuning.OBSERVE_DISTANCE)) beginGallery();
        if(state==State.WALKING_GALLERY) {
            if(follow(other,data.galleryRoute,delta)) { state=State.SIGNALLING;notice="observatorySignal"; }
        } else if(state==State.PASSAGE_OPEN&&near(player,data.center,ObservatoryTuning.CENTER_REACH)) {
            state=State.RETURNING;waypoint=0;navigation.reset();notice="observatoryReturning";
        } else if(state==State.RETURNING&&follow(other,data.returnRoute,delta)) {
            state=State.REUNITED;notice="observatoryReunited";
        }
    }
    private void beginGallery() { state=State.WALKING_GALLERY;waypoint=0;navigation.reset();notice="observatoryGallery"; }
    private boolean follow(PuzzleHero other,float[][] route,float delta) {
        float[] point=route[waypoint];
        navigation.updateToward(other,point[0],point[1],true,delta,ObservatoryTuning.ARRIVAL_DISTANCE,ObservatoryTuning.COMPANION_SPEED);
        if(!near(other,point,ObservatoryTuning.ARRIVAL_DISTANCE+.01f)) return false;
        if(++waypoint>=route.length) return true;
        navigation.reset();return false;
    }
    @Override public boolean interact(PuzzleHero player,PuzzleHero other) {
        if(state==State.WAITING&&near(player,data.observationPoint,ObservatoryTuning.INTERACT_DISTANCE)) { beginGallery();return true; }
        if(state!=State.SIGNALLING) return false;
        int selected=nearbyPassage(player);
        if(selected<0) return false;
        if(selected!=data.correctPassage) { notice="observatoryWrongPassage";return true; }
        collision.setObservatoryPassageOpen(selected,true);state=State.PASSAGE_OPEN;
        notice="observatoryPassageOpened";return true;
    }
    private int nearbyPassage(PuzzleHero player) {
        for(int i=0;i<data.approaches.length;i++) if(near(player,data.approaches[i],ObservatoryTuning.INTERACT_DISTANCE)) return i;
        return -1;
    }
    private static boolean near(PuzzleHero hero,float[] point,float distance) {
        return Math.hypot(hero.getX()-point[0],hero.getY()-point[1])<=distance;
    }
    public State state() { return state; }
    public int correctPassage() { return data.correctPassage; }
    public boolean isPassageOpen(int index) { return index==data.correctPassage&&state.ordinal()>=State.PASSAGE_OPEN.ordinal(); }
    public boolean isSignalling() { return state==State.SIGNALLING; }
    public float galleryProgress() { return state==State.WALKING_GALLERY?(float)waypoint/data.galleryRoute.length:state==State.WAITING?0:1; }
    @Override public boolean controlsCompanion() { return true; }
    @Override public boolean allowsExit() { return state==State.REUNITED; }
    @Override public int objectiveCount() { return 4; }
    @Override public int objectiveStep() { return state==State.WAITING||state==State.WALKING_GALLERY?1:state==State.SIGNALLING?2:state==State.PASSAGE_OPEN?3:4; }
    @Override public String objectiveKey() {
        return state==State.WAITING?"observatoryGoalApproach":state==State.WALKING_GALLERY?"observatoryGoalWatch"
            :state==State.SIGNALLING?"observatoryGoalChoose":state==State.PASSAGE_OPEN?"observatoryGoalCenter":"observatoryGoalReunite";
    }
    @Override public String actionKey(PuzzleHero player) {
        if(state==State.WAITING&&near(player,data.observationPoint,ObservatoryTuning.INTERACT_DISTANCE)) return "observatoryObserveAction";
        return state==State.SIGNALLING&&nearbyPassage(player)>=0?"observatoryPassageAction":null;
    }
    @Override public float[] interactionPoint(PuzzleHero player) {
        if(state==State.WAITING&&near(player,data.observationPoint,ObservatoryTuning.INTERACT_DISTANCE)) return data.observationPoint;
        int selected=state==State.SIGNALLING?nearbyPassage(player):-1;
        return selected<0?null:data.approaches[selected];
    }
    @Override public String companionStatus() {
        return state==State.WAITING?"observatoryCompanionWaiting":state==State.WALKING_GALLERY?"observatoryCompanionWalking"
            :state==State.SIGNALLING||state==State.PASSAGE_OPEN?"observatoryCompanionPointing":state==State.RETURNING?"observatoryCompanionReturning":"observatoryCompanionReunited";
    }
    @Override public String speech() { return state==State.SIGNALLING?"observatorySpeech":null; }
    @Override public String pollNotice() { String result=notice;notice=null;return result; }
    @Override public void resetNavigation() { navigation.reset(); }
}
