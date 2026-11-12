package com.nos.systems;

import com.nos.data.ObservatoryData;
import com.nos.data.ObservatoryTuning;

/** A união dos anéis, corredores e núcleo define o piso; os vãos permanecem vazios. */
public final class ObservatoryGeometry {
    private final ObservatoryData data;
    public ObservatoryGeometry(ObservatoryData data) { this.data=data; }
    public boolean walkable(float x,float y,float radius) {
        if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(radius)||radius<0||!floor(x,y)) return false;
        for(int i=0;i<ObservatoryTuning.FLOOR_SAMPLES;i++) {
            double angle=i*Math.PI*2/ObservatoryTuning.FLOOR_SAMPLES;
            if(!floor(x+radius*(float)Math.cos(angle),y+radius*(float)Math.sin(angle))) return false;
        }
        return true;
    }
    private boolean floor(float x,float y) {
        float distance=(float)Math.hypot(x-data.center[0],y-data.center[1]);
        if(distance<=data.coreRadius
            ||distance>=data.outerInnerRadius&&distance<=data.outerOuterRadius
            ||distance>=data.galleryInnerRadius&&distance<=data.galleryOuterRadius) return true;
        if(inside(data.entry,x,y)) return true;
        for(float[] passage:data.passages) if(inside(passage,x,y)) return true;
        return false;
    }
    private boolean inside(float[] box,float x,float y) {
        return x>=box[0]&&x<=box[0]+box[2]&&y>=box[1]&&y<=box[1]+box[3];
    }
}
