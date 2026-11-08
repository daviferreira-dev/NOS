package com.nos.systems;

import com.nos.data.BridgeData;
import com.nos.data.BridgeTuning;
import com.nos.entities.PuzzleHero;
import java.util.Arrays;

/** Procura um desvio caminhável quando a linha até o parceiro passa por água ou pedra. */
public final class CompanionFollowSystem {
    private final BridgeCollisionSystem collision;
    private final float originX,originY;
    private final int columns,rows;
    private final int[] queue,parent;
    private float targetX,targetY,repath;
    private boolean hasTarget,lastOpen;
    public CompanionFollowSystem(BridgeData data,BridgeCollisionSystem collision) {
        this.collision=collision;
        originX=data.ground[0];originY=data.ground[1];
        columns=(int)Math.ceil(data.ground[2]/BridgeTuning.FOLLOW_GRID);
        rows=(int)Math.ceil(data.ground[3]/BridgeTuning.FOLLOW_GRID);
        queue=new int[columns*rows];parent=new int[queue.length];
    }
    public void reset() { hasTarget=false;repath=0; }
    public void update(PuzzleHero follower,PuzzleHero leader,boolean open,float delta) {
        updateToward(follower,leader.getX(),leader.getY(),open,delta,BridgeTuning.FOLLOW_DISTANCE,BridgeTuning.FOLLOW_SPEED);
    }
    public void updateToward(PuzzleHero follower,float goalX,float goalY,boolean open,float delta,float stopDistance,float speed) {
        float dx=goalX-follower.getX(),dy=goalY-follower.getY();
        float distance=(float)Math.hypot(dx,dy);
        repath-=delta;
        if(open!=lastOpen) { reset();lastOpen=open; }
        if(distance<=stopDistance) { reset();return; }
        if(collision.clearPath(follower.getX(),follower.getY(),goalX,goalY,open)) {
            reset();
            float travel=Math.min(distance-stopDistance,speed*delta);
            collision.move(follower,dx/distance*travel,dy/distance*travel,open);
            return;
        }
        if(hasTarget&&Math.hypot(targetX-follower.getX(),targetY-follower.getY())<BridgeTuning.FOLLOW_WAYPOINT)
            repath=0;
        if(repath<=0) {
            hasTarget=findTarget(follower,goalX,goalY,stopDistance,open);
            repath=BridgeTuning.FOLLOW_REPATH;
        }
        if(!hasTarget) return;
        dx=targetX-follower.getX();dy=targetY-follower.getY();distance=(float)Math.hypot(dx,dy);
        if(distance>0) {
            float travel=Math.min(distance,speed*delta);
            collision.move(follower,dx/distance*travel,dy/distance*travel,open);
        }
    }
    private float x(int index) { return originX+(index%columns+.5f)*BridgeTuning.FOLLOW_GRID; }
    private float y(int index) { return originY+(index/columns+.5f)*BridgeTuning.FOLLOW_GRID; }
    private boolean findTarget(PuzzleHero follower,float goalX,float goalY,float stopDistance,boolean open) {
        Arrays.fill(parent,-2);
        int head=0,tail=0;
        // Raízes próximas e visíveis evitam encaixar o corpo à força na grade.
        for(int i=0;i<parent.length;i++) {
            if(Math.hypot(x(i)-follower.getX(),y(i)-follower.getY())<=BridgeTuning.FOLLOW_GRID*1.5f
                &&collision.clearPath(follower.getX(),follower.getY(),x(i),y(i),open)) {
                parent[i]=-1;queue[tail++]=i;
            }
        }
        while(head<tail) {
            int index=queue[head++];
            if(Math.hypot(x(index)-goalX,y(index)-goalY)<=Math.max(stopDistance*.85f,BridgeTuning.FOLLOW_GRID*1.5f)
                &&collision.clearPath(x(index),y(index),goalX,goalY,open)) {
                // O ponto mais distante ainda visível elimina o zigue-zague da grade.
                for(int node=index;node>=0;node=parent[node]) {
                    if(collision.clearPath(follower.getX(),follower.getY(),x(node),y(node),open)) {
                        targetX=x(node);targetY=y(node);return true;
                    }
                }
            }
            int column=index%columns,row=index/columns;
            for(int side=0;side<4;side++) {
                int c=column+(side==0?1:side==1?-1:0),r=row+(side==2?1:side==3?-1:0);
                if(c<0||c>=columns||r<0||r>=rows) continue;
                int next=r*columns+c;
                if(parent[next]!=-2||!collision.clearPath(x(index),y(index),x(next),y(next),open)) continue;
                parent[next]=index;queue[tail++]=next;
            }
        }
        return false;
    }
}
