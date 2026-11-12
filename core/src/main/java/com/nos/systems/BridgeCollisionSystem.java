package com.nos.systems;

import com.nos.data.*;
import com.nos.entities.PuzzleHero;

/** Água e terreno têm a mesma geometria exibida na cena. */
public final class BridgeCollisionSystem {
    private final BridgeData data;
    private boolean ridgePassageOpen;
    private final ObservatoryGeometry observatoryFloor;
    private final boolean[] observatoryPassagesOpen;
    private float cartX,cartY;
    private boolean hasCart,gardenGateOpen,transferGateOpen,relayGateOpen,furnaceDoorOpen;
    public BridgeCollisionSystem(BridgeData data) {
        this.data=data;
        observatoryFloor=data.observatory==null?null:new ObservatoryGeometry(data.observatory);
        observatoryPassagesOpen=data.observatory==null?new boolean[0]:new boolean[data.observatory.gates.length];
        if(data.garden!=null&&data.garden.cartStart!=null)
            setGardenCartPosition(data.garden.cartStart[0],data.garden.cartStart[1]);
    }
    public void setGardenCartPosition(float x,float y) { cartX=x;cartY=y;hasCart=true; }
    public void setGardenGateOpen(boolean open) { gardenGateOpen=open; }
    public void setTransferGateOpen(boolean open) { transferGateOpen=open; }
    public void setRelayGateOpen(boolean open) { relayGateOpen=open; }
    public void setRidgePassageOpen(boolean open) { ridgePassageOpen=open; }
    public void setObservatoryPassageOpen(int index,boolean open) { observatoryPassagesOpen[index]=open; }
    public void setFurnaceDoorOpen(boolean open) { furnaceDoorOpen=open; }
    public boolean walkable(float x,float y,boolean open) {
        return walkable(x,y,open,true);
    }
    public boolean walkableIgnoringCart(float x,float y,boolean open) {
        return walkable(x,y,open,false);
    }
    private boolean walkable(float x,float y,boolean open,boolean includeCart) {
        float r=BridgeTuning.RADIUS;
        float[] g=data.ground;
        if(x-r<g[0]||x+r>g[0]+g[2]||y-r<g[1]||y+r>g[1]+g[3]) return false;
        if(data.ridge!=null) {
            if(!RidgeFloorGeometry.contains(data.ridge,x,y,r)) return false;
            float[] gap=data.ridge.windBarrier;
            if(!ridgePassageOpen&&circleTouchesBox(x,y,r,gap[0],gap[1],gap[2],gap[3])) return false;
        }
        if(observatoryFloor!=null) {
            if(!observatoryFloor.walkable(x,y,r)) return false;
            for(int i=0;i<observatoryPassagesOpen.length;i++) {
                float[] gate=data.observatory.gates[i];
                if(!observatoryPassagesOpen[i]&&circleTouchesBox(x,y,r,gate[0],gate[1],gate[2],gate[3])) return false;
            }
        }
        boolean water=x+r>data.river[0]&&x-r<data.river[0]+data.river[2];
        if(water&&(!open||y-r<data.bridge[1]||y+r>data.bridge[1]+data.bridge[3])) return false;
        if(data.obstacles!=null) for(float[] box:data.obstacles)
            if(circleTouchesBox(x,y,r,box[0],box[1],box[2],box[3])) return false;
        if(data.garden!=null) {
            if(data.garden.barriers!=null) for(float[] box:data.garden.barriers)
                if(circleTouchesBox(x,y,r,box[0],box[1],box[2],box[3])) return false;
            float[] gate=data.garden.gate;
            if(!gardenGateOpen&&gate!=null&&circleTouchesBox(x,y,r,gate[0],gate[1],gate[2],gate[3])) return false;
            float[] transfer=data.garden.transferGate;
            if(!transferGateOpen&&transfer!=null&&circleTouchesBox(x,y,r,transfer[0],transfer[1],transfer[2],transfer[3])) return false;
        }
        if(data.relay!=null) {
            if(data.relay.barriers!=null) for(float[] box:data.relay.barriers)
                if(circleTouchesBox(x,y,r,box[0],box[1],box[2],box[3])) return false;
            float[] gate=data.relay.gate;
            if(!relayGateOpen&&circleTouchesBox(x,y,r,gate[0],gate[1],gate[2],gate[3])) return false;
        }
        if(data.furnace!=null&&!furnaceDoorOpen) {
            float[] gate=data.furnace.gate;
            if(circleTouchesBox(x,y,r,gate[0],gate[1],gate[2],gate[3])) return false;
        }
        if(includeCart&&hasCart&&circleTouchesBox(x,y,r,cartX-GardenTuning.CART_WIDTH/2,
            cartY-GardenTuning.CART_HEIGHT/2,GardenTuning.CART_WIDTH,GardenTuning.CART_HEIGHT)) return false;
        return true;
    }
    /** A jardineira consulta seu retângulo inteiro, sem colidir com a posição anterior dela. */
    public boolean boxWalkableIgnoringCart(float centerX,float centerY,float width,float height,boolean open) {
        float x=centerX-width/2,y=centerY-height/2;
        float[] ground=data.ground;
        if(x<ground[0]||x+width>ground[0]+ground[2]||y<ground[1]||y+height>ground[1]+ground[3]) return false;
        if(data.ridge!=null) {
            if(!RidgeFloorGeometry.containsBox(data.ridge,centerX,centerY,width,height)) return false;
            if(!ridgePassageOpen&&overlap(x,y,width,height,data.ridge.windBarrier)) return false;
        }
        if(observatoryFloor!=null) {
            if(!observatoryFloor.walkable(centerX,centerY,Math.max(width,height)/2)) return false;
            for(int i=0;i<observatoryPassagesOpen.length;i++)
                if(!observatoryPassagesOpen[i]&&overlap(x,y,width,height,data.observatory.gates[i])) return false;
        }
        if(data.river[2]>0&&x+width>data.river[0]&&x<data.river[0]+data.river[2]
            &&(!open||y<data.bridge[1]||y+height>data.bridge[1]+data.bridge[3])) return false;
        if(data.obstacles!=null) for(float[] box:data.obstacles) if(overlap(x,y,width,height,box)) return false;
        if(data.garden!=null) {
            if(data.garden.barriers!=null) for(float[] box:data.garden.barriers) if(overlap(x,y,width,height,box)) return false;
            if(!gardenGateOpen&&data.garden.gate!=null&&overlap(x,y,width,height,data.garden.gate)) return false;
            if(!transferGateOpen&&data.garden.transferGate!=null&&overlap(x,y,width,height,data.garden.transferGate)) return false;
        }
        if(data.relay!=null) {
            if(data.relay.barriers!=null) for(float[] box:data.relay.barriers) if(overlap(x,y,width,height,box)) return false;
            if(!relayGateOpen&&overlap(x,y,width,height,data.relay.gate)) return false;
        }
        if(data.furnace!=null&&!furnaceDoorOpen&&overlap(x,y,width,height,data.furnace.gate)) return false;
        return true;
    }
    public static boolean circleTouchesBox(float x,float y,float radius,float boxX,float boxY,float width,float height) {
        float dx=x-Math.max(boxX,Math.min(x,boxX+width));
        float dy=y-Math.max(boxY,Math.min(y,boxY+height));
        return dx*dx+dy*dy<radius*radius;
    }
    private static boolean overlap(float x,float y,float width,float height,float[] box) {
        return x<box[0]+box[2]&&x+width>box[0]&&y<box[1]+box[3]&&y+height>box[1];
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
