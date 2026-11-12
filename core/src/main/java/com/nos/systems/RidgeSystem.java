package com.nos.systems;

import com.nos.data.BridgeData;
import com.nos.data.RidgeData;
import com.nos.data.RidgeTuning;
import com.nos.entities.PuzzleHero;

/** Sombra sobe, usa uma rajada lateral e chama Reflexo para reunir-se no abrigo. */
public final class RidgeSystem implements PhaseChallenge {
    public enum State { ASCENDING,WAITING_WIND,RIDING,TO_SHELTER,REUNITING,REUNITED }
    private final RidgeData data;
    private final BridgeCollisionSystem collision;
    private final CompanionFollowSystem navigation;
    private State state=State.ASCENDING;
    private double elapsed;
    private String notice;
    public RidgeSystem(BridgeData bridge,BridgeCollisionSystem collision) {
        if(bridge.ridge==null) throw new IllegalArgumentException("Planta da trilha ausente.");
        data=bridge.ridge;this.collision=collision;validate(data);
        navigation=new CompanionFollowSystem(bridge,collision);collision.setRidgePassageOpen(false);
    }
    private static void validate(RidgeData data) {
        if(data.path==null||data.path.length<2||!Float.isFinite(data.halfWidth)||data.halfWidth<=0)
            throw new IllegalArgumentException("Piso da trilha inválido.");
        for(float[] point:data.path) check(point,2);
        check(data.windBarrier,4);check(data.approachZone,4);check(data.launchZone,4);
        check(data.landingPoint,2);check(data.windWaitPoint,2);check(data.shelterPoint,2);check(data.reunionPoint,2);
    }
    private static void check(float[] values,int count) {
        if(values==null||values.length<count) throw new IllegalArgumentException("Geometria da trilha incompleta.");
        for(int i=0;i<count;i++) if(!Float.isFinite(values[i])) throw new IllegalArgumentException("Geometria da trilha inválida.");
        if(count==4&&(values[2]<=0||values[3]<=0)) throw new IllegalArgumentException("Área da trilha inválida.");
    }
    @Override public void update(PuzzleHero player,PuzzleHero other,float delta) {
        if(!Float.isFinite(delta)||delta<0||delta>RidgeTuning.MAX_DELTA) throw new IllegalArgumentException("Intervalo inválido.");
        if(delta==0) return;
        elapsed+=delta;
        if(state==State.ASCENDING&&inside(data.approachZone,player)) {
            state=State.WAITING_WIND;notice="ridgeWindReady";navigation.reset();
        }
        if(state==State.WAITING_WIND&&inside(data.launchZone,player)&&windActive()) {
            state=State.RIDING;notice="ridgeRiding";collision.setRidgePassageOpen(true);navigation.reset();
        }
        if(state==State.RIDING) {
            float dx=data.landingPoint[0]-player.getX(),dy=data.landingPoint[1]-player.getY();
            float distance=(float)Math.hypot(dx,dy);
            if(distance>RidgeTuning.LANDING_DISTANCE) {
                float travel=Math.min(distance,RidgeTuning.WIND_SPEED*delta);
                collision.move(player,dx/distance*travel,dy/distance*travel,true);
            }
            if(distance(player,data.landingPoint)<=RidgeTuning.LANDING_DISTANCE) {
                state=State.TO_SHELTER;notice="ridgeLanded";navigation.reset();
            }
        }
        if(state==State.WAITING_WIND||state==State.RIDING||state==State.TO_SHELTER) {
            other.setMotion(0,0);
            navigation.updateToward(other,data.windWaitPoint[0],data.windWaitPoint[1],true,delta,
                RidgeTuning.COMPANION_ARRIVAL,RidgeTuning.COMPANION_SPEED);
        } else if(state==State.REUNITING) {
            other.setMotion(0,0);
            navigation.updateToward(other,data.reunionPoint[0],data.reunionPoint[1],true,delta,
                RidgeTuning.COMPANION_ARRIVAL,RidgeTuning.COMPANION_SPEED);
            if(distance(other,data.reunionPoint)<=RidgeTuning.COMPANION_ARRIVAL+.01f
                &&distance(player,data.reunionPoint)<=RidgeTuning.REUNION_DISTANCE) {
                state=State.REUNITED;notice="ridgeReunited";navigation.reset();
            }
        }
    }
    private static boolean inside(float[] box,PuzzleHero hero) {
        return hero.getX()>=box[0]&&hero.getX()<=box[0]+box[2]&&hero.getY()>=box[1]&&hero.getY()<=box[1]+box[3];
    }
    private static float distance(PuzzleHero hero,float[] point) { return (float)Math.hypot(hero.getX()-point[0],hero.getY()-point[1]); }
    @Override public boolean interact(PuzzleHero player,PuzzleHero other) {
        if(state!=State.TO_SHELTER||distance(player,data.shelterPoint)>RidgeTuning.INTERACT_DISTANCE) return false;
        state=State.REUNITING;notice="ridgeCalled";navigation.reset();return true;
    }
    @Override public boolean controlsCompanion() { return state!=State.ASCENDING&&state!=State.REUNITED; }
    @Override public boolean allowsExit() { return state==State.REUNITED; }
    @Override public String objectiveKey() {
        if(state==State.ASCENDING) return "ridgeGoalAscent";
        if(state==State.WAITING_WIND||state==State.RIDING) return "ridgeGoalWind";
        return state==State.TO_SHELTER?"ridgeGoalShelter":state==State.REUNITING?"ridgeGoalReunite":"ridgeGoalExit";
    }
    @Override public int objectiveStep() { return state==State.ASCENDING?1:state==State.WAITING_WIND||state==State.RIDING?2:state==State.TO_SHELTER?3:4; }
    @Override public int objectiveCount() { return 4; }
    @Override public String actionKey(PuzzleHero player) {
        return state==State.TO_SHELTER&&distance(player,data.shelterPoint)<=RidgeTuning.INTERACT_DISTANCE?"ridgeCallAction":null;
    }
    @Override public float[] interactionPoint(PuzzleHero player) { return actionKey(player)==null?null:data.shelterPoint; }
    @Override public String companionStatus() {
        return state==State.ASCENDING||state==State.REUNITED?null:state==State.REUNITING?"ridgeCompanionApproaching":"ridgeCompanionSheltered";
    }
    @Override public String pollNotice() { String value=notice;notice=null;return value; }
    @Override public String speech() { return state==State.WAITING_WIND?"ridgeWhisper":null; }
    @Override public void resetNavigation() { navigation.reset(); }
    public State state() { return state; }
    public boolean isRiding() { return state==State.RIDING; }
    public boolean hasCrossed() { return state==State.TO_SHELTER||state==State.REUNITING||state==State.REUNITED; }
    public float time() { return (float)elapsed; }
    public boolean windActive() {
        double local=elapsed%(RidgeTuning.WIND_WARNING+RidgeTuning.WIND_ACTIVE+RidgeTuning.WIND_REST);
        return local>=RidgeTuning.WIND_WARNING&&local<RidgeTuning.WIND_WARNING+RidgeTuning.WIND_ACTIVE;
    }
    public float windWarning() {
        double local=elapsed%(RidgeTuning.WIND_WARNING+RidgeTuning.WIND_ACTIVE+RidgeTuning.WIND_REST);
        return local<RidgeTuning.WIND_WARNING?(float)(local/RidgeTuning.WIND_WARNING):0;
    }
}
