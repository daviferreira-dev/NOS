package com.nos.systems;

import com.nos.data.BridgeData;
import com.nos.data.BridgeTuning;
import com.nos.data.GardenData;
import com.nos.data.GardenTuning;
import com.nos.entities.PuzzleHero;

/** Contrapesos físicos do pátio: carregar peso, pedir ajuda e liberar a passagem. */
public final class GardenGateSystem {
    private final GardenData data;
    private final BridgeCollisionSystem collision;
    private final CompanionFollowSystem navigation;
    private final CompanionClearanceSystem clearance;
    private final float[] cart=new float[2],helperCenter=new float[2];
    private boolean dragging,requested,holding,needsRoom,smallPressed,largePressed,open;
    private float opening;
    private boolean transferPressed,transferLatched;
    private float transferOpening;
    private float attemptedX,attemptedY,advanced,blockedTime,noticeCooldown;
    private boolean blockedByCompanion;
    private String notice;

    public GardenGateSystem(BridgeData bridge,BridgeCollisionSystem collision) {
        if(bridge.garden==null) throw new IllegalArgumentException("Planta do pátio ausente.");
        data=bridge.garden;this.collision=collision;
        if((data.transferPlate==null)!=(data.transferGate==null))
            throw new IllegalArgumentException("A passagem de transferência exige pedal e grade.");
        navigation=new CompanionFollowSystem(bridge,collision);
        clearance=new CompanionClearanceSystem(collision);
        cart[0]=data.cartStart[0];cart[1]=data.cartStart[1];
        helperCenter[0]=data.helperPlate[0]+data.helperPlate[2]/2;
        helperCenter[1]=data.helperPlate[1]+data.helperPlate[3]/2;
        collision.setGardenCartPosition(cart[0],cart[1]);collision.setGardenGateOpen(false);collision.setTransferGateOpen(false);
    }
    public boolean interact(PuzzleHero player,PuzzleHero other) {
        int target=target(player);
        if(target==0) return false;
        if(target==1) {
            dragging=!dragging;
            notice=dragging?"gardenGrabbed":"gardenReleased";
        } else {
            requested=!requested;holding=false;needsRoom=false;navigation.reset();
            notice=requested?"gardenHelpRequested":"gardenHelpCancelled";
        }
        return true;
    }
    private int target(PuzzleHero player) {
        if(dragging) return 1;
        float cartDistance=distance(player.getX(),player.getY(),cart[0],cart[1]);
        float helperDistance=open?Float.POSITIVE_INFINITY:distance(player.getX(),player.getY(),helperCenter[0],helperCenter[1]);
        if(cartDistance<=GardenTuning.INTERACT_DISTANCE&&cartDistance<=helperDistance) return 1;
        return helperDistance<=GardenTuning.INTERACT_DISTANCE?2:0;
    }
    public void movePlayer(PuzzleHero player,PuzzleHero other,float dx,float dy,boolean bridgeOpen) {
        if(!Float.isFinite(dx)||!Float.isFinite(dy)) throw new IllegalArgumentException("Movimento inválido.");
        attemptedX=dx;attemptedY=dy;advanced=0;blockedByCompanion=false;
        if(!dragging) { collision.move(player,dx,dy,bridgeOpen);return; }
        float startX=player.getX(),startY=player.getY();
        int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(dx),Math.abs(dy))/GardenTuning.COLLISION_STEP));
        for(int i=0;i<steps;i++) {
            translate(player,other,dx/steps,0,bridgeOpen);
            translate(player,other,0,dy/steps,bridgeOpen);
        }
        player.setMotion(player.getX()-startX,player.getY()-startY);
        advanced=(float)Math.hypot(player.getX()-startX,player.getY()-startY);
    }
    private void translate(PuzzleHero player,PuzzleHero other,float dx,float dy,boolean bridgeOpen) {
        float nextX=cart[0]+dx,nextY=cart[1]+dy;
        if(!collision.walkableIgnoringCart(player.getX()+dx,player.getY()+dy,bridgeOpen)
            ||!collision.boxWalkableIgnoringCart(nextX,nextY,GardenTuning.CART_WIDTH,GardenTuning.CART_HEIGHT,bridgeOpen)) return;
        if(BridgeCollisionSystem.circleTouchesBox(other.getX(),other.getY(),BridgeTuning.RADIUS,
            nextX-GardenTuning.CART_WIDTH/2,nextY-GardenTuning.CART_HEIGHT/2,GardenTuning.CART_WIDTH,GardenTuning.CART_HEIGHT)) {
            blockedByCompanion=true;return;
        }
        player.setPosition(player.getX()+dx,player.getY()+dy);
        cart[0]=nextX;cart[1]=nextY;collision.setGardenCartPosition(nextX,nextY);
    }
    public void update(PuzzleHero player,PuzzleHero other,boolean bridgeOpen,float delta) {
        if(!Float.isFinite(delta)||delta<0||delta>.1f) throw new IllegalArgumentException("Intervalo inválido.");
        if(dragging&&!requested) {
            clearance.update(other,player,attemptedX,attemptedY,blockedByCompanion,bridgeOpen,delta);
        } else clearance.cancel();
        updateBlockedFeedback(delta);
        if(requested) {
            other.setMotion(0,0);
            needsRoom=!holding&&distance(player.getX(),player.getY(),helperCenter[0],helperCenter[1])<GardenTuning.HELPER_CLEARANCE;
            navigation.updateToward(other,helperCenter[0],helperCenter[1],bridgeOpen,delta,
                needsRoom?GardenTuning.HELPER_CLEARANCE:GardenTuning.HELPER_ARRIVAL,GardenTuning.HELPER_SPEED);
            boolean arrived=!needsRoom&&distance(other.getX(),other.getY(),helperCenter[0],helperCenter[1])<=GardenTuning.HELPER_ARRIVAL+.01f;
            if(arrived&&!holding) notice="gardenHeld";
            holding=arrived;
        }
        smallPressed=contains(data.helperPlate,player.getX(),player.getY(),GardenTuning.PLATE_MARGIN)
            ||contains(data.helperPlate,other.getX(),other.getY(),GardenTuning.PLATE_MARGIN);
        largePressed=contains(data.weightPlate,cart[0],cart[1],GardenTuning.WEIGHT_MARGIN);
        updateTransfer(player,other,delta);
        if(open) return;
        boolean exitWeighted=smallPressed&&largePressed&&(!hasTransfer()||transferLatched);
        opening=Math.max(0,Math.min(1,opening+(exitWeighted?GardenTuning.OPEN_SPEED:-GardenTuning.CLOSE_SPEED)*delta));
        if(opening>=1) {
            open=true;requested=false;holding=false;needsRoom=false;
            navigation.reset();collision.setGardenGateOpen(true);notice="gardenOpened";
        }
    }
    private void updateTransfer(PuzzleHero player,PuzzleHero other,float delta) {
        if(!hasTransfer()) return;
        transferPressed=contains(data.transferPlate,cart[0],cart[1],GardenTuning.WEIGHT_MARGIN);
        boolean wasOpen=transferOpening>=BridgeTuning.OPEN_THRESHOLD;
        if(!transferLatched&&holding&&wasOpen) {
            transferLatched=true;notice="transferLatched";
        }
        // A grade espera quem já ocupa o vão; não fecha sobre corpos nem sobre a jardineira.
        boolean occupied=wasOpen&&transferOccupied(player,other);
        boolean raise=transferPressed||transferLatched||occupied;
        transferOpening=Math.max(0,Math.min(1,transferOpening+(raise?GardenTuning.OPEN_SPEED:-GardenTuning.CLOSE_SPEED)*delta));
        boolean isOpen=transferOpening>=BridgeTuning.OPEN_THRESHOLD;
        if(!wasOpen&&isOpen&&!transferLatched) notice="transferOpened";
        if(wasOpen!=isOpen) navigation.reset();
        collision.setTransferGateOpen(isOpen);
    }
    private boolean transferOccupied(PuzzleHero player,PuzzleHero other) {
        float[] gate=data.transferGate;
        if(BridgeCollisionSystem.circleTouchesBox(player.getX(),player.getY(),BridgeTuning.RADIUS,gate[0],gate[1],gate[2],gate[3])
            ||BridgeCollisionSystem.circleTouchesBox(other.getX(),other.getY(),BridgeTuning.RADIUS,gate[0],gate[1],gate[2],gate[3])) return true;
        return cart[0]-GardenTuning.CART_WIDTH/2<gate[0]+gate[2]&&cart[0]+GardenTuning.CART_WIDTH/2>gate[0]
            &&cart[1]-GardenTuning.CART_HEIGHT/2<gate[1]+gate[3]&&cart[1]+GardenTuning.CART_HEIGHT/2>gate[1];
    }
    private void updateBlockedFeedback(float delta) {
        noticeCooldown=Math.max(0,noticeCooldown-delta);
        float attempted=(float)Math.hypot(attemptedX,attemptedY);
        boolean blocked=dragging&&attempted>GardenTuning.MOVEMENT_EPSILON
            &&advanced<attempted*GardenTuning.BLOCKED_MOVEMENT_RATIO;
        blockedTime=blocked?blockedTime+delta:0;
        if(blockedTime>=GardenTuning.BLOCKED_DELAY&&noticeCooldown<=0) {
            notice=blockedByCompanion?"gardenCompanionBlocked":"gardenPathBlocked";
            noticeCooldown=GardenTuning.BLOCKED_NOTICE_COOLDOWN;blockedTime=0;
        }
    }
    private static boolean contains(float[] box,float x,float y,float margin) {
        return x>=box[0]+margin&&x<=box[0]+box[2]-margin&&y>=box[1]+margin&&y<=box[1]+box[3]-margin;
    }
    private static float distance(float x,float y,float otherX,float otherY) { return (float)Math.hypot(x-otherX,y-otherY); }
    public boolean controlsCompanion() { return requested||dragging&&(clearance.isControlling()||blockedByCompanion); }
    public void resetNavigation() { navigation.reset(); }
    public boolean isDragging() { return dragging; }
    public boolean isOpen() { return open; }
    public boolean hasTransfer() { return data.transferGate!=null; }
    public float transferOpening() { return transferOpening; }
    public boolean transferPressed() { return transferPressed; }
    public boolean isTransferLatched() { return transferLatched; }
    public float getOpening() { return opening; }
    public boolean isHelperHolding() { return holding; }
    public boolean isHelperRequested() { return requested; }
    public boolean isCompanionYielding() { return clearance.isActive(); }
    public boolean helperPressed() { return smallPressed; }
    public boolean weightPressed() { return largePressed; }
    public float cartX() { return cart[0]; }
    public float cartY() { return cart[1]; }
    public String actionKey(PuzzleHero player) {
        int target=target(player);
        return target==1?(dragging?"gardenReleaseAction":"gardenGrabAction")
            :target==2?(requested?"gardenCancelHelpAction":"gardenAskHelpAction"):null;
    }
    public float[] interactionPoint(PuzzleHero player) {
        int target=target(player);return target==1?cart:target==2?helperCenter:null;
    }
    public String objectiveKey() {
        if(open) return "gardenGoalExit";
        if(hasTransfer()&&!transferLatched&&!transferPressed&&transferOpening<BridgeTuning.OPEN_THRESHOLD) return "transferGoalLoad";
        if(requested&&!holding) return needsRoom?"gardenGoalMakeRoom":"gardenGoalWaiting";
        if(transferLatched&&largePressed) return smallPressed?"gardenGoalUnlocking":"gardenGoalAsk";
        if(hasTransfer()) return transferLatched?"transferGoalMove":"transferGoalHelp";
        return smallPressed?"gardenGoalWeight":"gardenGoalAsk";
    }
    public int objectiveStep() {
        if(hasTransfer()) return open?4:transferLatched?3:transferPressed||transferOpening>=BridgeTuning.OPEN_THRESHOLD?2:1;
        return open?5:smallPressed?4:3;
    }
    public String companionStatus() {
        if(!requested&&clearance.isActive()) return "gardenCompanionYielding";
        return !requested?null:holding?"gardenCompanionHolding":needsRoom?"gardenCompanionNeedsRoom":"gardenCompanionApproaching";
    }
    public String pollNotice() { String result=notice;notice=null;return result; }
}
