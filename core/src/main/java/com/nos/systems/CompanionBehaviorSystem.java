package com.nos.systems;

import com.nos.data.BridgeData;
import com.nos.data.BridgeTuning;
import com.nos.entities.PuzzleHero;

/** Reflexo reage, observa e coopera sem receber os comandos de movimento de Sombra. */
public final class CompanionBehaviorSystem {
    public enum State { FOLLOWING,WATCHING_WATER,APPROACHING_WINCH,HOLDING,REJOINING }
    private final BridgeData data;
    private final CompanionFollowSystem navigation;
    private State state=State.FOLLOWING;
    private float stateTime,reaction;
    private boolean observedWater,following,needsRoom;
    public CompanionBehaviorSystem(BridgeData data,BridgeCollisionSystem collision) {
        this.data=data;navigation=new CompanionFollowSystem(data,collision);
    }
    public void requestSupport() {
        if(state==State.HOLDING||state==State.APPROACHING_WINCH) return;
        transition(State.APPROACHING_WINCH);
    }
    public void releaseSupport() { if(isSupportingOrApproaching()) transition(State.REJOINING); }
    public boolean isSupportingOrApproaching() { return state==State.HOLDING||state==State.APPROACHING_WINCH; }
    public State getState() { return state; }
    public boolean isHolding() { return state==State.HOLDING; }
    public boolean needsRoom() { return needsRoom; }
    public void resetNavigation() { navigation.reset(); }
    private void transition(State next) {
        state=next;stateTime=0;reaction=0;following=false;needsRoom=false;navigation.reset();
    }
    public void update(PuzzleHero companion,PuzzleHero player,boolean open,boolean locked,float delta) {
        stateTime+=delta;
        float distance=(float)Math.hypot(player.getX()-companion.getX(),player.getY()-companion.getY());
        if(state==State.APPROACHING_WINCH) {
            needsRoom=Math.hypot(player.getX()-data.winch[0],player.getY()-data.winch[1])<BridgeTuning.COMPANION_CLEARANCE
                &&distance<BridgeTuning.COMPANION_CLEARANCE+BridgeTuning.COMPANION_APPROACH_SPEED*delta;
            if(needsRoom) return;
            navigation.updateToward(companion,data.winch[0],data.winch[1],open,delta,
                BridgeTuning.COMPANION_WINCH_RADIUS*.5f,BridgeTuning.COMPANION_APPROACH_SPEED);
            if(Math.hypot(companion.getX()-data.winch[0],companion.getY()-data.winch[1])<=BridgeTuning.COMPANION_WINCH_RADIUS) {
                transition(State.HOLDING);faceWater(companion);
            }
            return;
        }
        if(state==State.HOLDING) { faceWater(companion);return; }
        if(state==State.WATCHING_WATER) {
            faceWater(companion);
            if(stateTime>=BridgeTuning.COMPANION_OBSERVE_TIME||distance>BridgeTuning.COMPANION_OBSERVE_MAX_DISTANCE) transition(State.FOLLOWING);
            return;
        }
        if(state==State.REJOINING) {
            if(stateTime<BridgeTuning.COMPANION_RELEASE_DELAY) return;
            navigation.updateToward(companion,player.getX(),player.getY(),open,delta,
                BridgeTuning.COMPANION_STOP_DISTANCE,BridgeTuning.FOLLOW_SPEED);
            if(distance<=BridgeTuning.COMPANION_START_DISTANCE) transition(State.FOLLOWING);
            return;
        }
        if(!observedWater&&!locked&&player.getX()>data.river[0]-BridgeTuning.COMPANION_OBSERVE_PLAYER_RANGE
            &&player.getX()<data.river[0]&&companion.getX()>data.river[0]-BridgeTuning.COMPANION_OBSERVE_OWN_RANGE) {
            observedWater=true;transition(State.WATCHING_WATER);faceWater(companion);return;
        }
        // Uma pequena reação e distâncias distintas para partir/parar evitam copiar cada passo do jogador.
        if(!following&&distance>BridgeTuning.COMPANION_START_DISTANCE) {
            reaction+=delta;
            if(reaction>=BridgeTuning.COMPANION_REACTION) following=true;
        } else if(!following) reaction=0;
        if(following) {
            navigation.updateToward(companion,player.getX(),player.getY(),open,delta,BridgeTuning.COMPANION_STOP_DISTANCE,
                distance>BridgeTuning.COMPANION_CATCHUP_DISTANCE?BridgeTuning.FOLLOW_SPEED:BridgeTuning.COMPANION_WALK_SPEED);
            if(distance<=BridgeTuning.COMPANION_STOP_DISTANCE+.5f) { following=false;reaction=0; }
        }
    }
    private void faceWater(PuzzleHero hero) { hero.setMotion(1,0);hero.setMotion(0,0); }
    public String status() {
        if(state==State.WATCHING_WATER) return "bridgeCompanionLooking";
        if(state==State.APPROACHING_WINCH) return needsRoom?"bridgeCompanionNeedsRoom":"bridgeCompanionApproaching";
        if(state==State.HOLDING) return "bridgeCompanionHolding";
        if(state==State.REJOINING) return "bridgeCompanionReturning";
        return "bridgeFollowingShort";
    }
    public String speech() { return state==State.WATCHING_WATER?"bridgeWaterWhisper":null; }
}
