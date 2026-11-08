package com.nos.systems;

import com.nos.data.BridgeData;
import com.nos.data.BridgeTuning;
import com.nos.data.RelayData;
import com.nos.data.RelayTuning;
import com.nos.entities.PuzzleHero;

/** Primeiro Reflexo sustenta; depois Sombra segura a passagem para ele atravessar. */
public final class RelayGateSystem {
    public enum State { IDLE,APPROACHING_WEST,HOLDING_WEST,CROSSING,LATCHED }
    private final RelayData data;
    private final BridgeCollisionSystem collision;
    private final CompanionFollowSystem navigation;
    private final float[] westCenter=new float[2],eastCenter=new float[2];
    private State state=State.IDLE;
    private float opening;
    private boolean westPressed,eastPressed,needsRoom,waitingForEast;
    private String notice;

    public RelayGateSystem(BridgeData bridge,BridgeCollisionSystem collision) {
        if(bridge.relay==null) throw new IllegalArgumentException("Planta de revezamento ausente.");
        data=bridge.relay;this.collision=collision;
        navigation=new CompanionFollowSystem(bridge,collision);
        center(data.westPlate,westCenter);center(data.eastPlate,eastCenter);
        collision.setRelayGateOpen(false);
    }
    private static void center(float[] box,float[] result) { result[0]=box[0]+box[2]/2;result[1]=box[1]+box[3]/2; }
    public boolean interact(PuzzleHero player,PuzzleHero other) {
        int target=target(player);
        if(target==1) {
            state=state==State.IDLE?State.APPROACHING_WEST:State.IDLE;
            notice=state==State.IDLE?"relayHelpCancelled":"relayHelpRequested";
            needsRoom=false;navigation.reset();return true;
        }
        if(target==2) {
            state=State.CROSSING;waitingForEast=false;navigation.reset();notice="relayHandoff";return true;
        }
        return false;
    }
    private int target(PuzzleHero player) {
        if(state==State.LATCHED||state==State.CROSSING) return 0;
        if(distance(player,westCenter)<=RelayTuning.INTERACT_DISTANCE) return 1;
        return state==State.HOLDING_WEST&&contains(data.eastPlate,player)?2:0;
    }
    public void update(PuzzleHero player,PuzzleHero other,boolean bridgeOpen,float delta) {
        if(!Float.isFinite(delta)||delta<0||delta>RelayTuning.MAX_DELTA)
            throw new IllegalArgumentException("Intervalo inválido.");
        eastPressed=contains(data.eastPlate,player);
        if(controlsCompanion()) other.setMotion(0,0);
        if(state==State.APPROACHING_WEST||state==State.HOLDING_WEST) {
            needsRoom=state!=State.HOLDING_WEST&&distance(player,westCenter)<RelayTuning.PLAYER_CLEARANCE;
            navigation.updateToward(other,westCenter[0],westCenter[1],bridgeOpen,delta,
                needsRoom?RelayTuning.PLAYER_CLEARANCE:RelayTuning.ARRIVAL,RelayTuning.HELPER_SPEED);
            boolean arrived=!needsRoom&&distance(other,westCenter)<=RelayTuning.ARRIVAL+.01f;
            if(arrived&&state!=State.HOLDING_WEST) { state=State.HOLDING_WEST;notice="relayWestHeld"; }
            else if(!arrived) state=State.APPROACHING_WEST;
        } else if(state==State.CROSSING) {
            if(eastPressed) {
                navigation.updateToward(other,data.reunionPoint[0],data.reunionPoint[1],bridgeOpen,delta,
                    RelayTuning.ARRIVAL,RelayTuning.HELPER_SPEED);
                if(distance(other,data.reunionPoint)<=RelayTuning.ARRIVAL+.01f) {
                    state=State.LATCHED;navigation.reset();notice="relayLatched";
                }
            } else if(!waitingForEast) { navigation.reset();notice="relayWait"; }
            waitingForEast=!eastPressed;
        }
        westPressed=contains(data.westPlate,player)||contains(data.westPlate,other);
        boolean wasOpen=opening>=BridgeTuning.OPEN_THRESHOLD;
        // O vão permanece livre enquanto alguém o ocupa, mesmo sem nenhum pedal pressionado.
        boolean occupied=wasOpen&&(touchesGate(player)||touchesGate(other));
        boolean raise=state==State.LATCHED||state==State.HOLDING_WEST&&contains(data.westPlate,other)
            ||state==State.CROSSING&&eastPressed||occupied;
        opening=Math.max(0,Math.min(1,opening+(raise?RelayTuning.OPEN_SPEED:-RelayTuning.CLOSE_SPEED)*delta));
        boolean open=opening>=BridgeTuning.OPEN_THRESHOLD;
        collision.setRelayGateOpen(open);
        if(wasOpen!=open) navigation.reset();
    }
    private static boolean contains(float[] box,PuzzleHero hero) {
        float margin=RelayTuning.PLATE_MARGIN;
        return hero.getX()>=box[0]+margin&&hero.getX()<=box[0]+box[2]-margin
            &&hero.getY()>=box[1]+margin&&hero.getY()<=box[1]+box[3]-margin;
    }
    private boolean touchesGate(PuzzleHero hero) {
        return BridgeCollisionSystem.circleTouchesBox(hero.getX(),hero.getY(),BridgeTuning.RADIUS,
            data.gate[0],data.gate[1],data.gate[2],data.gate[3]);
    }
    private static float distance(PuzzleHero hero,float[] point) { return (float)Math.hypot(hero.getX()-point[0],hero.getY()-point[1]); }
    public boolean controlsCompanion() { return state!=State.IDLE&&state!=State.LATCHED; }
    public void resetNavigation() { navigation.reset(); }
    public boolean isLatched() { return state==State.LATCHED; }
    public boolean isHoldingWest() { return state==State.HOLDING_WEST; }
    public float opening() { return opening; }
    public boolean westPressed() { return westPressed; }
    public boolean eastPressed() { return eastPressed; }
    public State state() { return state; }
    public String objectiveKey() {
        if(state==State.LATCHED) return "relayGoalExit";
        if(state==State.CROSSING) return eastPressed?"relayGoalWait":"relayGoalHold";
        if(state==State.HOLDING_WEST) return "relayGoalCross";
        return state==State.APPROACHING_WEST?(needsRoom?"relayGoalMakeRoom":"relayGoalWaiting"):"relayGoalAsk";
    }
    public int objectiveStep() { return state==State.LATCHED?4:state==State.CROSSING?3:state==State.HOLDING_WEST?2:1; }
    public String actionKey(PuzzleHero player) {
        int target=target(player);return target==2?"relayHandoffAction":target==1?(state==State.IDLE?"relayAskAction":"relayCancelAction"):null;
    }
    public float[] interactionPoint(PuzzleHero player) { int target=target(player);return target==1?westCenter:target==2?eastCenter:null; }
    public String companionStatus() {
        if(state==State.LATCHED||state==State.IDLE) return null;
        if(state==State.HOLDING_WEST) return "relayCompanionHolding";
        if(state==State.CROSSING) return eastPressed?"relayCompanionCrossing":"relayCompanionWaiting";
        return needsRoom?"relayCompanionNeedsRoom":"relayCompanionApproaching";
    }
    public String pollNotice() { String result=notice;notice=null;return result; }
}
