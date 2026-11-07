package com.nos.systems;

import com.nos.data.GardenTuning;
import com.nos.entities.PuzzleHero;

/** Reflexo cede passagem à jardineira apenas quando existe um desvio físico seguro. */
public final class CompanionClearanceSystem {
    private final BridgeCollisionSystem collision;
    private boolean active;
    private float targetX,targetY,reaction,cooldown,elapsed;
    public CompanionClearanceSystem(BridgeCollisionSystem collision) { this.collision=collision; }
    public boolean isActive() { return active||reaction>0; }
    public boolean isControlling() { return isActive()||cooldown>0; }
    public void cancel() { active=false;reaction=0;elapsed=0;cooldown=0; }
    public void update(PuzzleHero companion,PuzzleHero player,float directionX,float directionY,
                       boolean blocked,boolean bridgeOpen,float delta) {
        if(!Float.isFinite(directionX)||!Float.isFinite(directionY)||!Float.isFinite(delta)||delta<0||delta>.1f)
            throw new IllegalArgumentException("Movimento de desvio inválido.");
        cooldown=Math.max(0,cooldown-delta);
        if(!active) {
            if(!blocked||cooldown>0) { reaction=0;return; }
            reaction+=delta;
            if(reaction<GardenTuning.YIELD_REACTION) return;
            reaction=0;
            float length=(float)Math.hypot(directionX,directionY);
            if(length<=GardenTuning.MOVEMENT_EPSILON||!chooseSide(companion,player,-directionY/length,directionX/length,bridgeOpen)) {
                cooldown=GardenTuning.YIELD_COOLDOWN;return;
            }
            active=true;elapsed=0;
        }
        float dx=targetX-companion.getX(),dy=targetY-companion.getY(),distance=(float)Math.hypot(dx,dy);
        elapsed+=delta;
        if(distance<=GardenTuning.YIELD_ARRIVAL||elapsed>=GardenTuning.YIELD_MAX_TIME
            ||!collision.clearPath(companion.getX(),companion.getY(),targetX,targetY,bridgeOpen)
            ||crossesPlayer(companion.getX(),companion.getY(),targetX,targetY,player)) {
            cancel();cooldown=GardenTuning.YIELD_COOLDOWN;return;
        }
        float travel=Math.min(distance,GardenTuning.YIELD_SPEED*delta);
        collision.move(companion,dx/distance*travel,dy/distance*travel,bridgeOpen);
    }
    private boolean chooseSide(PuzzleHero companion,PuzzleHero player,float sideX,float sideY,boolean bridgeOpen) {
        for(int sign=1;sign>=-1;sign-=2) {
            float x=companion.getX()+sign*sideX*GardenTuning.YIELD_DISTANCE;
            float y=companion.getY()+sign*sideY*GardenTuning.YIELD_DISTANCE;
            if(collision.clearPath(companion.getX(),companion.getY(),x,y,bridgeOpen)
                &&!crossesPlayer(companion.getX(),companion.getY(),x,y,player)) {
                targetX=x;targetY=y;return true;
            }
        }
        return false;
    }
    private boolean crossesPlayer(float x,float y,float endX,float endY,PuzzleHero player) {
        float dx=endX-x,dy=endY-y,lengthSquared=dx*dx+dy*dy;
        float t=lengthSquared==0?0:Math.max(0,Math.min(1,((player.getX()-x)*dx+(player.getY()-y)*dy)/lengthSquared));
        return Math.hypot(player.getX()-(x+t*dx),player.getY()-(y+t*dy))<GardenTuning.PERSON_CLEARANCE;
    }
}
