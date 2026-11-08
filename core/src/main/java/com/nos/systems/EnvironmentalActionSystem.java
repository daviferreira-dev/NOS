package com.nos.systems;

import com.nos.data.ActionTuning;
import com.nos.data.BridgeData;
import com.nos.data.BridgeTuning;
import com.nos.data.HazardData;
import com.nos.entities.PuzzleHero;

/** Brasas antecipadas recuperam Sombra; rajadas a deslocam pela colisão normal. */
public final class EnvironmentalActionSystem {
    private final BridgeData data;
    private final BridgeCollisionSystem collision;
    private final HazardData[] hazards;
    private double elapsed;
    private float cooldown,previousX,previousY,safeX,safeY;
    private boolean recovered,observed,hasSafe,bridgeOpen=true;

    public EnvironmentalActionSystem(BridgeData data,BridgeCollisionSystem collision) {
        this.data=data;this.collision=collision;hazards=data.hazards==null?new HazardData[0]:data.hazards;
        for(HazardData hazard:hazards) validate(hazard);
        reset();
    }
    private static void validate(HazardData h) {
        if(h==null||!finite(h.x,h.y,h.amplitudeX,h.amplitudeY,h.phase,h.period,h.radius,
            h.warningSeconds,h.activeSeconds,h.restSeconds,h.pushX,h.pushY)
            ||h.period<=0||h.radius<=0||h.warningSeconds<0||h.activeSeconds<=0||h.restSeconds<0
            ||!("ember".equals(h.type)||"gust".equals(h.type)))
            throw new IllegalArgumentException("Perigo ambiental inválido.");
    }
    private static boolean finite(float... values) { for(float value:values) if(!Float.isFinite(value)) return false;return true; }
    public void setBridgeOpen(boolean value) { bridgeOpen=value; }
    public void update(PuzzleHero player,PuzzleHero companion,float delta) {
        if(!Float.isFinite(delta)||delta<0||delta>ActionTuning.MAX_DELTA)
            throw new IllegalArgumentException("Intervalo inválido.");
        if(!finite(player.getX(),player.getY())) throw new IllegalArgumentException("Posição inválida.");
        recovered=false;
        if(delta==0) return;
        float startX=observed?previousX:player.getX(),startY=observed?previousY:player.getY();
        float targetX=player.getX(),targetY=player.getY();
        boolean sweep=true;
        int steps=Math.max(1,(int)Math.ceil(delta/ActionTuning.CONTACT_STEP));
        float step=delta/steps;
        for(int tick=1;tick<=steps;tick++) {
            elapsed+=step;cooldown=Math.max(0,cooldown-step);
            float fraction=(float)tick/steps;
            float sampleX=sweep?startX+(targetX-startX)*fraction:player.getX();
            float sampleY=sweep?startY+(targetY-startY)*fraction:player.getY();
            for(int i=0;i<hazards.length;i++) {
                if(!isActive(i)) continue;
                HazardData hazard=hazards[i];float x=hazardX(i),y=hazardY(i),radius=hazard.radius+BridgeTuning.RADIUS;
                boolean touching=distanceSquared(sampleX,sampleY,x,y)<=radius*radius
                    ||distanceSquared(player.getX(),player.getY(),x,y)<=radius*radius;
                if(!touching) continue;
                if("ember".equals(hazard.type)) {
                    if(cooldown<=0&&recover(player)) { sweep=false;break; }
                } else push(player,hazard,x,y,step);
            }
        }
        previousX=player.getX();previousY=player.getY();observed=true;
        if(canRemember(player.getX(),player.getY())) { safeX=player.getX();safeY=player.getY();hasSafe=true; }
    }
    private boolean recover(PuzzleHero player) {
        float x,y;
        if(data.localRecoveryMargin>0 && hasSafe && canRemember(safeX,safeY)
            && collision.clearPath(player.getX(),player.getY(),safeX,safeY,bridgeOpen)) {
            x=safeX;y=safeY;
        } else if(data.recoveryPoint!=null&&data.recoveryPoint.length>=2&&safe(data.recoveryPoint[0],data.recoveryPoint[1])) {
            x=data.recoveryPoint[0];y=data.recoveryPoint[1];
        } else if(data.localRecoveryMargin<=0&&hasSafe&&safe(safeX,safeY)) { x=safeX;y=safeY; }
        else if(data.shadowStart!=null&&data.shadowStart.length>=2&&safe(data.shadowStart[0],data.shadowStart[1])) {
            x=data.shadowStart[0];y=data.shadowStart[1];
        } else return false;
        player.setPosition(x,y);player.setMotion(0,0);cooldown=ActionTuning.RECOVERY_COOLDOWN;recovered=true;return true;
    }
    private boolean safe(float x,float y) {
        if(!finite(x,y)||!collision.walkable(x,y,bridgeOpen)) return false;
        for(int i=0;i<hazards.length;i++) {
            float radius=hazards[i].radius+BridgeTuning.RADIUS;
            if(isActive(i)&&distanceSquared(x,y,hazardX(i),hazardY(i))<=radius*radius) return false;
        }
        return true;
    }
    private boolean canRemember(float x,float y) {
        if(!safe(x,y)) return false;
        if(data.localRecoveryMargin<=0) return true;
        // Mesmo em repouso, reserve o espaço que o perigo pode alcançar no próximo ciclo.
        for(HazardData hazard:hazards) {
            float dx=Math.max(0,Math.abs(x-hazard.x)-Math.abs(hazard.amplitudeX));
            float dy=Math.max(0,Math.abs(y-hazard.y)-Math.abs(hazard.amplitudeY));
            float clearance=hazard.radius+BridgeTuning.RADIUS+data.localRecoveryMargin;
            if(dx*dx+dy*dy<=clearance*clearance) return false;
        }
        return true;
    }
    private void push(PuzzleHero player,HazardData hazard,float x,float y,float delta) {
        float dx=hazard.pushX,dy=hazard.pushY;
        if(Math.abs(dx)+Math.abs(dy)<ActionTuning.DIRECTION_EPSILON) {
            dx=player.getX()-x;dy=player.getY()-y;float distance=(float)Math.hypot(dx,dy);
            if(distance<ActionTuning.DIRECTION_EPSILON) { dx=1;dy=0;distance=1; }
            dx=dx/distance*ActionTuning.GUST_SPEED;dy=dy/distance*ActionTuning.GUST_SPEED;
        }
        collision.move(player,dx*delta,dy*delta,bridgeOpen);
    }
    private static float distanceSquared(float ax,float ay,float bx,float by) { float dx=ax-bx,dy=ay-by;return dx*dx+dy*dy; }
    private double local(HazardData h) {
        double cycle=(double)h.warningSeconds+h.activeSeconds+h.restSeconds;
        double local=(elapsed+h.phase)%cycle;return local<0?local+cycle:local;
    }
    public float time() { return (float)elapsed; }
    public int count() { return hazards.length; }
    public float hazardX(int index) {
        HazardData h=hazards[index];return h.x+h.amplitudeX*(float)Math.sin((elapsed+h.phase)*Math.PI*2/h.period);
    }
    public float hazardY(int index) {
        HazardData h=hazards[index];return h.y+h.amplitudeY*(float)Math.sin((elapsed+h.phase)*Math.PI*2/h.period);
    }
    /** Progresso da antecipação, de zero a um; zero também indica ausência de aviso. */
    public float warning(int index) {
        HazardData h=hazards[index];double local=local(h);
        return h.warningSeconds>0&&local<h.warningSeconds?(float)(local/h.warningSeconds):0;
    }
    public boolean isActive(int index) {
        HazardData h=hazards[index];double local=local(h);
        return local>=h.warningSeconds&&local<h.warningSeconds+h.activeSeconds;
    }
    /** Pulso do último update; consultar não consome o valor. */
    public boolean didRecover() { return recovered; }
    public void reset() {
        elapsed=0;cooldown=0;recovered=false;observed=false;hasSafe=false;
        if(data.shadowStart!=null&&data.shadowStart.length>=2&&canRemember(data.shadowStart[0],data.shadowStart[1])) {
            safeX=data.shadowStart[0];safeY=data.shadowStart[1];hasSafe=true;
        }
    }
}
