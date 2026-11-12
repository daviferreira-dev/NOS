package com.nos.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.nos.world.BridgeEncounter;

/** Brasas e folhas mostram os ciclos ambientais antes do contato. */
final class ActionHazardRenderer {
    void draw(BridgeEncounter world,SpriteBatch batch,ShapeRenderer shapes,GameAssets assets) {
        for(int i=0;i<world.actions().count();i++) {
            float warning=world.actions().warning(i);
            boolean active=world.actions().isActive(i);
            if(!active&&warning<=0) continue;
            Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
            float x=world.actions().hazardX(i),y=world.actions().hazardY(i),r=world.getData().hazards[i].radius;
            if("gust".equals(world.getData().hazards[i].type)) {
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                for(int leaf=0;leaf<12;leaf++) {
                    float p=(world.actions().time()*.7f+leaf/12f)%1;
                    float dx=world.getData().hazards[i].pushX,dy=world.getData().hazards[i].pushY,length=(float)Math.hypot(dx,dy);
                    if(length==0) { dx=1;dy=0;length=1; }dx/=length;dy/=length;
                    float across=(float)Math.sin(leaf*2.3f+p*5)*r*.6f;
                    float lx=x+dx*(p*2-1)*r-dy*across,ly=y+dy*(p*2-1)*r+dx*across;
                    shapes.setColor(.48f,.43f,.27f,active?.70f:.25f);
                    shapes.triangle(lx-3,ly,lx+4,ly+2,lx,ly+5);
                }
                shapes.end();
            } else {
                shapes.begin(ShapeRenderer.ShapeType.Filled);shapes.setColor(.67f,.28f,.08f,active?.2f:.08f+warning*.12f);
                shapes.ellipse(x-r,y-r*.4f,r*2,r*.8f);shapes.end();
                batch.begin();batch.setColor(1,1,1,active?1:.35f+warning*.35f);
                float flicker=active?1+.035f*(float)Math.sin(world.actions().time()*17):1;
                batch.draw(assets.ember,x-r*1.4f,y-r*.5f,r*2.8f*flicker,r*1.4f*flicker);batch.setColor(1,1,1,1);batch.end();
            }
        }
    }
}
