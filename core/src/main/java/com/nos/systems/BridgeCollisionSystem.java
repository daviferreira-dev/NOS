package com.nos.systems;

import com.nos.data.*;
import com.nos.entities.PuzzleHero;

/** Água e terreno têm a mesma geometria exibida na cena. */
public final class BridgeCollisionSystem {
    private final BridgeData data;
    public BridgeCollisionSystem(BridgeData data) { this.data=data; }
    public boolean walkable(float x,float y,boolean open) {
        float r=BridgeTuning.RADIUS;
        float[] g=data.ground;
        if(x-r<g[0]||x+r>g[0]+g[2]||y-r<g[1]||y+r>g[1]+g[3]) return false;
        boolean water=x+r>data.river[0]&&x-r<data.river[0]+data.river[2];
        if(water&&(!open||y-r<data.bridge[1]||y+r>data.bridge[1]+data.bridge[3])) return false;
        for(float[] box:data.obstacles) {
            float dx=x-Math.max(box[0],Math.min(x,box[0]+box[2]));
            float dy=y-Math.max(box[1],Math.min(y,box[1]+box[3]));
            if(dx*dx+dy*dy<r*r) return false;
        }
        return true;
    }
    public boolean clearPath(float x,float y,float targetX,float targetY,boolean open) {
        int count=Math.max(1,(int)Math.ceil(Math.hypot(targetX-x,targetY-y)/BridgeTuning.COLLISION_STEP));
        for(int i=0;i<=count;i++)
            if(!walkable(x+(targetX-x)*i/count,y+(targetY-y)*i/count,open)) return false;
        return true;
    }
    public void move(PuzzleHero hero,float dx,float dy,boolean open) {
        int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(dx),Math.abs(dy))/BridgeTuning.COLLISION_STEP));
        float x=hero.getX(),y=hero.getY();
        for(int i=0;i<steps;i++) {
            if(walkable(x+dx/steps,y,open)) x+=dx/steps;
            if(walkable(x,y+dy/steps,open)) y+=dy/steps;
        }
        hero.setMotion(x-hero.getX(),y-hero.getY());hero.setPosition(x,y);
    }
}
