package com.nos.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.nos.data.HeroSpriteTuning;

/** Desenha todas as poses a partir dos pés, sem deslocar a posição de colisão. */
public final class HeroFrame {
    private final TextureRegion region;
    private final float pivot,sole,scale;
    public HeroFrame(TextureRegion region,float pivot,float sole,float top) {
        this.region=region;this.pivot=pivot;this.sole=sole;
        scale=HeroSpriteTuning.HEIGHT/(sole-top);
    }
    public void draw(SpriteBatch batch,float x,float y,boolean flip) {
        float width=region.getRegionWidth()*scale,height=region.getRegionHeight()*scale;
        batch.draw(region,x+(flip?pivot*scale:-pivot*scale),
            y-(region.getRegionHeight()-sole)*scale,flip?-width:width,height);
    }
}
