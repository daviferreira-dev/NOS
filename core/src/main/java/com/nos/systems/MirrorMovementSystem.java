package com.nos.systems;

import com.nos.entities.PuzzleHero;
import com.nos.data.PuzzleTuning;

/** Uma entrada, duas respostas: Reflexo inverte apenas o eixo horizontal. */
public final class MirrorMovementSystem {
    public void update(PuzzleHero shadow,PuzzleHero reflection,AnchorSystem anchor,RoomCollisionSystem collision,
                       float x,float y,float delta,boolean gateOpen) {
        if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(delta)||delta<0||delta>.1f)
            throw new IllegalArgumentException("Entrada de movimento inválida.");
        float length=(float)Math.sqrt(x*x+y*y);
        if(length>1) { x/=length;y/=length; }
        float dx=x*PuzzleTuning.MOVE_SPEED*delta,dy=y*PuzzleTuning.MOVE_SPEED*delta;
        if(anchor.isShadowBound()) shadow.setMotion(0,0);
        else collision.move(shadow,dx,dy,false,gateOpen);
        if(anchor.isReflectionBound()) reflection.setMotion(0,0);
        else collision.move(reflection,-dx,dy,true,gateOpen);
    }
}
