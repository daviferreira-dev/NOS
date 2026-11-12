package com.nos.systems;

import com.nos.data.RidgeData;

/** Piso da trilha como união de corredores arredondados, sem depender da apresentação. */
public final class RidgeFloorGeometry {
    private RidgeFloorGeometry() { }
    public static boolean contains(RidgeData data,float x,float y,float radius) {
        if(data==null) return true;
        if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(radius)||radius<0
            ||data.path==null||data.path.length<2||data.halfWidth<radius) return false;
        float limit=data.halfWidth-radius;
        for(int i=1;i<data.path.length;i++) {
            float[] a=data.path[i-1],b=data.path[i];
            float dx=b[0]-a[0],dy=b[1]-a[1],length=dx*dx+dy*dy;
            float t=length==0?0:Math.max(0,Math.min(1,((x-a[0])*dx+(y-a[1])*dy)/length));
            float distanceX=x-(a[0]+t*dx),distanceY=y-(a[1]+t*dy);
            if(distanceX*distanceX+distanceY*distanceY<=limit*limit) return true;
        }
        return false;
    }
    public static boolean containsBox(RidgeData data,float centerX,float centerY,float width,float height) {
        if(data==null) return true;
        for(int i=-1;i<=1;i++) for(int j=-1;j<=1;j++)
            if(!contains(data,centerX+i*width/2,centerY+j*height/2,0)) return false;
        return true;
    }
}
