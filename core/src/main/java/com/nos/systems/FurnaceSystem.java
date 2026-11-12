package com.nos.systems;

import com.nos.data.BridgeData;
import com.nos.data.FurnaceData;
import com.nos.data.FurnaceTuning;
import com.nos.entities.PuzzleHero;

/** Sombra escolhe um atalho e abre uma porta para Reflexo, ou os dois fazem o desvio. */
public final class FurnaceSystem implements PhaseChallenge {
    public enum Route { UNDECIDED,SHORTCUT,DETOUR }
    private final FurnaceData data;
    private final BridgeCollisionSystem collision;
    private final CompanionFollowSystem navigation;
    private Route route=Route.UNDECIDED;
    private boolean doorOpen,reunited,waitingForCompanion;
    private int waypoint;
    private float speechTime;
    private String notice;

    public FurnaceSystem(BridgeData bridge,BridgeCollisionSystem collision) {
        if(bridge.furnace==null) throw new IllegalArgumentException("Planta da fornalha ausente.");
        data=bridge.furnace;this.collision=collision;navigation=new CompanionFollowSystem(bridge,collision);
        collision.setFurnaceDoorOpen(false);
    }
    @Override public void update(PuzzleHero player,PuzzleHero other,float delta) {
        if(!Float.isFinite(delta)||delta<0||delta>FurnaceTuning.MAX_DELTA) throw new IllegalArgumentException("Intervalo inválido.");
        speechTime=Math.max(0,speechTime-delta);
        if(reunited) return;
        // A tela pode ter atualizado o acompanhamento comum antes da escolha deste quadro.
        if(route!=Route.DETOUR&&inside(data.detourZone,player)) { choose(Route.DETOUR);return; }
        else if(route==Route.UNDECIDED&&inside(data.hotZone,player)) { choose(Route.SHORTCUT);return; }
        if(route==Route.UNDECIDED) return;
        other.setMotion(0,0);
        if(route==Route.SHORTCUT&&!doorOpen) {
            float stop=distance(player,data.waitingPoint)<FurnaceTuning.PLAYER_CLEARANCE?FurnaceTuning.PLAYER_CLEARANCE:FurnaceTuning.ARRIVAL;
            navigation.updateToward(other,data.waitingPoint[0],data.waitingPoint[1],true,delta,stop,FurnaceTuning.COMPANION_SPEED);
            return;
        }
        float[][] path=route==Route.DETOUR?data.detourRoute:data.shortcutRoute;
        float[] last=path[path.length-1];
        waitingForCompanion=distance(player,last)<=FurnaceTuning.WAIT_DISTANCE;
        if(route==Route.DETOUR&&other.getX()>player.getX()+FurnaceTuning.DETOUR_LEAD) return;
        float[] goal=path[waypoint];
        navigation.updateToward(other,goal[0],goal[1],true,delta,FurnaceTuning.ARRIVAL,FurnaceTuning.COMPANION_SPEED);
        if(distance(other,goal)<=FurnaceTuning.ARRIVAL+.01f) {
            if(waypoint<path.length-1) { waypoint++;navigation.reset(); }
            else { reunited=true;navigation.reset();notice="furnaceReunited"; }
        }
    }
    private void choose(Route choice) {
        route=choice;waypoint=0;navigation.reset();
        notice=choice==Route.SHORTCUT?"furnaceShortcutChosen":"furnaceDetourChosen";
        if(choice==Route.SHORTCUT) speechTime=FurnaceTuning.SPEECH_DURATION;
    }
    @Override public boolean interact(PuzzleHero player,PuzzleHero other) {
        if(doorOpen||distance(player,data.lever)>FurnaceTuning.INTERACT_DISTANCE) return false;
        doorOpen=true;collision.setFurnaceDoorOpen(true);navigation.reset();notice="furnaceDoorOpened";
        if(route==Route.UNDECIDED) { route=Route.SHORTCUT;waypoint=0; }
        return true;
    }
    private static boolean inside(float[] box,PuzzleHero hero) {
        return hero.getX()>=box[0]&&hero.getX()<=box[0]+box[2]&&hero.getY()>=box[1]&&hero.getY()<=box[1]+box[3];
    }
    private static float distance(PuzzleHero hero,float[] point) { return (float)Math.hypot(hero.getX()-point[0],hero.getY()-point[1]); }
    @Override public boolean controlsCompanion() { return route!=Route.UNDECIDED&&!reunited; }
    @Override public boolean allowsExit() { return reunited; }
    public boolean isDoorOpen() { return doorOpen; }
    public Route route() { return route; }
    @Override public String objectiveKey() {
        if(reunited) return "furnaceGoalExit";
        if(route==Route.UNDECIDED) return "furnaceGoalChoose";
        if(route==Route.SHORTCUT) return doorOpen?"furnaceGoalWait":"furnaceGoalDoor";
        return waitingForCompanion?"furnaceGoalWait":"furnaceGoalDetour";
    }
    @Override public int objectiveStep() { return reunited?4:route==Route.UNDECIDED?1:doorOpen||waitingForCompanion?3:2; }
    @Override public int objectiveCount() { return 4; }
    @Override public String actionKey(PuzzleHero player) {
        return !doorOpen&&distance(player,data.lever)<=FurnaceTuning.INTERACT_DISTANCE?"furnaceOpenAction":null;
    }
    @Override public float[] interactionPoint(PuzzleHero player) { return actionKey(player)==null?null:data.lever; }
    @Override public String companionStatus() {
        if(reunited||route==Route.UNDECIDED) return null;
        return route==Route.DETOUR?"furnaceCompanionDetour":doorOpen?"furnaceCompanionCrossing":"furnaceCompanionWaiting";
    }
    @Override public String pollNotice() { String result=notice;notice=null;return result; }
    @Override public String speech() { return speechTime>0?"furnaceSpeechWait":null; }
    @Override public void resetNavigation() { navigation.reset(); }
}
