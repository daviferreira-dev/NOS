package com.nos.systems;

import com.nos.data.RoomData;
import com.nos.data.PuzzleTuning;
import com.nos.entities.PuzzleHero;

/** Colisão por camada, com pequenos passos e deslizamento nos eixos. */
public final class RoomCollisionSystem {
    private final RoomData data;
    public RoomCollisionSystem(RoomData data) { this.data=data; }
    public boolean blocked(float x,float y,boolean reflection,boolean gateOpen) {
        if(overlapsAny(x,y,data.commonWalls)) return true;
        if(overlapsAny(x,y,reflection?data.reflectionWalls:data.shadowWalls)) return true;
        return !gateOpen&&overlaps(x,y,data.gate);
    }
    private boolean overlapsAny(float x,float y,float[][] walls) {
        for(float[] wall:walls) if(overlaps(x,y,wall)) return true;
        return false;
    }
    private boolean overlaps(float x,float y,float[] wall) {
        float closestX=Math.max(wall[0],Math.min(x,wall[0]+wall[2]));
        float closestY=Math.max(wall[1],Math.min(y,wall[1]+wall[3]));
        float dx=x-closestX,dy=y-closestY;
        return dx*dx+dy*dy<PuzzleTuning.HERO_RADIUS*PuzzleTuning.HERO_RADIUS;
    }
    public void move(PuzzleHero hero,float dx,float dy,boolean reflection,boolean gateOpen) {
        int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(dx),Math.abs(dy))/PuzzleTuning.COLLISION_STEP));
        float x=hero.getX(),y=hero.getY();
        for(int i=0;i<steps;i++) {
            if(!blocked(x+dx/steps,y,reflection,gateOpen)) x+=dx/steps;
            if(!blocked(x,y+dy/steps,reflection,gateOpen)) y+=dy/steps;
        }
        hero.setMotion(x-hero.getX(),y-hero.getY());
        hero.setPosition(x,y);
    }
}
