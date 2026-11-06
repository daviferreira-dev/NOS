package com.nos.world;

/** Mantém as duas presenças visíveis e revela a paisagem adiante. */
public final class JourneyCamera {
    public static final float MAP_WIDTH = 3200;
    public float left, bottom, width = 960, height = 540;
    public void update(MirrorPair pair) {
        float a=pair.getWorldA().getHero().getX(), b=pair.getWorldB().getHero().getX();
        width=Math.max(960,Math.abs(a-b)+300);
        height=width*540/960;
        left=Math.max(0,Math.min(MAP_WIDTH-width,(a+b)*.5f+140-width*.5f));
        bottom=270-height*.5f;
    }
    public float worldX(float screenX) { return left+screenX*width/960; }
    public float worldY(float screenY) { return bottom+screenY*height/540; }
}
