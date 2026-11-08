package com.nos.world;

import com.nos.data.OpeningTuning;

/** Terminar ou pular o filme leva somente ao menu; começar exige outra ação explícita. */
public final class OpeningSequence {
    public enum State { CUTSCENE,TITLE,STARTING,READY_TO_PLAY }
    private State state=State.CUTSCENE;
    private float time,transition,titleTime;
    private boolean paused;
    public void update(float delta) {
        if(!Float.isFinite(delta)||delta<0||delta>.25f) throw new IllegalArgumentException("Intervalo inválido.");
        if(paused) return;
        if(state==State.CUTSCENE) {
            time=Math.min(duration(),time+delta);
            if(time>=duration()) state=State.TITLE;
        } else if(state==State.TITLE) titleTime+=delta;
        else if(state==State.STARTING) {
            transition=Math.min(OpeningTuning.START_FADE,transition+delta);
            if(transition>=OpeningTuning.START_FADE) state=State.READY_TO_PLAY;
        }
    }
    public void skip() { if(state==State.CUTSCENE) { state=State.TITLE;time=duration();titleTime=0; } }
    public void start() { if(state==State.TITLE) { state=State.STARTING;transition=0; } }
    public void replay() { if(state==State.TITLE) { state=State.CUTSCENE;time=0;titleTime=0; } }
    public void setPaused(boolean value) { paused=value; }
    public State getState() { return state; }
    public float getTime() { return time; }
    public float duration() { return OpeningTuning.SHOT_ENDS[OpeningTuning.SHOT_ENDS.length-1]; }
    public int shot() {
        for(int i=0;i<OpeningTuning.SHOT_ENDS.length;i++) if(time<OpeningTuning.SHOT_ENDS[i]) return i;
        return OpeningTuning.SHOT_ENDS.length-1;
    }
    public float shotProgress() {
        int s=shot();float begin=s==0?0:OpeningTuning.SHOT_ENDS[s-1];
        return (time-begin)/(OpeningTuning.SHOT_ENDS[s]-begin);
    }
    public float sceneVisibility() {
        int s=shot();float begin=s==0?0:OpeningTuning.SHOT_ENDS[s-1];
        return Math.max(0,Math.min(1,Math.min(time-begin,OpeningTuning.SHOT_ENDS[s]-time)/OpeningTuning.SHOT_FADE));
    }
    public float startFade() { return transition/OpeningTuning.START_FADE; }
    public float titleVisibility() { return Math.min(1,titleTime/OpeningTuning.SHOT_FADE); }
}
