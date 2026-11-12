package com.nos.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.nos.data.ObservatoryData;
import com.nos.systems.ObservatorySystem;
import com.nos.world.BridgeEncounter;

/** Passarelas de latão e luz das comportas sobre a aquarela do astrolábio. */
public final class ObservatoryRenderer implements Disposable {
    private static final int RING_PANELS = 96;
    private final ShapeRenderer.ShapeType filled = ShapeRenderer.ShapeType.Filled;

    public ObservatoryRenderer(GameAssets assets) {
        // O observatório usa a pintura de fundo que a tela já carregou.
    }

    public void draw(BridgeEncounter world, SpriteBatch batch, ShapeRenderer shapes) {
        ObservatoryData data=world.getData().observatory;
        ObservatorySystem mechanism=(ObservatorySystem)world.challenge();
        float x=data.center[0], y=data.center[1];
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(filled);
        ring(shapes,x,y,data.outerInnerRadius,data.outerOuterRadius,false);
        ring(shapes,x,y,data.galleryInnerRadius,data.galleryOuterRadius,true);
        // Le seuil occidental mène au premier anneau par une passerelle droite.
        walkway(shapes,data.entry);
        for (float[] crossing : data.passages) walkway(shapes,crossing);
        for (int i=0; i<data.gates.length; i++) {
            shutter(shapes,data.gates[i],mechanism.isPassageOpen(i));
        }

        // Le disque de verre peint garde ses reflets sous la bordure gravée.
        ring(shapes,x,y,data.coreRadius-3,data.coreRadius,false);
        if (mechanism.isSignalling()) {
            float[] entrance=data.approaches[data.correctPassage];
            signal(shapes,world.hero(1).getX(),world.hero(1).getY()+4,
                entrance[0],entrance[1],world.actions().time());
        }
        shapes.end();
    }

    private static void ring(ShapeRenderer s,float cx,float cy,float inner,float outer,boolean gallery) {
        for (int i=0; i<RING_PANELS; i++) {
            float a = i*MathUtils.PI2/RING_PANELS, b=(i+1)*MathUtils.PI2/RING_PANELS;
            float ca=MathUtils.cos(a), sa=MathUtils.sin(a), cb=MathUtils.cos(b), sb=MathUtils.sin(b);
            float patina=.015f*MathUtils.sin(i*2.37f);
            if (gallery) s.setColor(.53f+patina,.50f+patina,.43f+patina,.54f);
            else s.setColor(.48f+patina,.42f+patina,.32f+patina,.61f);
            float ix=cx+inner*ca,iy=cy+inner*sa,ox=cx+outer*ca,oy=cy+outer*sa;
            float jx=cx+inner*cb,jy=cy+inner*sb,px=cx+outer*cb,py=cy+outer*sb;
            s.triangle(ix,iy,ox,oy,px,py);
            s.triangle(ix,iy,px,py,jx,jy);
            s.setColor(.84f,.69f,.45f,.78f);
            s.rectLine(ox,oy,px,py,1.5f);
            s.setColor(.16f,.13f,.16f,.95f);
            s.rectLine(ix,iy,jx,jy,2.4f);
            if (i%3==0) {
                s.setColor(.22f,.21f,.22f,.63f);
                s.rectLine(ix,iy,ox,oy,1);
                float r=outer-5f;
                s.setColor(.90f,.77f,.52f,.86f);
                s.circle(cx+r*ca,cy+r*sa,1.1f,8);
            }
        }
    }

    private static void walkway(ShapeRenderer s,float[] box) {
        float x=box[0],y=box[1],w=box[2],h=box[3];
        boolean horizontal=w>h;
        s.setColor(.10f,.10f,.14f,.36f);
        s.rect(x,y-2,w,h);
        s.setColor(.54f,.48f,.37f,.64f);
        s.rect(x,y,w,h);
        s.setColor(.21f,.20f,.21f,.65f);
        if (horizontal) {
            for (float t=19; t<w; t+=21) s.rect(x+t,y+2,1,h-4);
        } else {
            for (float t=19; t<h; t+=21) s.rect(x+2,y+t,w-4,1);
        }
        s.setColor(.79f,.66f,.43f,.95f);
        if (horizontal) {
            s.rect(x,y,w,1.8f);
            s.rect(x,y+h-2,w,1.8f);
        } else {
            s.rect(x,y,1.8f,h);
            s.rect(x+w-2,y,1.8f,h);
        }
    }

    private static void shutter(ShapeRenderer s,float[] gate,boolean open) {
        float x=gate[0], y=gate[1], w=gate[2], h=gate[3];
        boolean acrossX=w>h;
        float length=acrossX?w:h, thickness=acrossX?h:w;
        s.setColor(.16f,.15f,.18f,.9f);
        if (acrossX) {
            s.rect(x-2,y-2,4,h+4);
            s.rect(x+w-2,y-2,4,h+4);
        } else {
            s.rect(x-2,y-2,w+4,4);
            s.rect(x-2,y+h-2,w+4,4);
        }
        for (int i=0; i<6; i++) {
            float along = i*length/6;
            if (open && i>0 && i<5) continue;
            float reach=open?4:length/6;
            if (open && i==5) along=length-reach;
            s.setColor(.29f,.27f,.26f,.96f);
            if (acrossX) s.rect(x+along,y,reach,h);
            else s.rect(x,y+along,w,reach);
            s.setColor(.77f,.59f,.37f,.98f);
            if (acrossX) s.rectLine(x+along,y+2,x+along+reach,y+thickness-2,1.6f);
            else s.rectLine(x+2,y+along,x+thickness-2,y+along+reach,1.6f);
        }
    }

    private static void signal(ShapeRenderer s,float fromX,float fromY,float toX,float toY,float time) {
        float phase=time*.7f;
        s.setColor(.96f,.81f,.58f,.22f);
        s.rectLine(fromX,fromY,toX,toY,2);
        for (int i=0; i<3; i++) {
            float t=(phase+i/3f)%1f, x=MathUtils.lerp(fromX,toX,t), y=MathUtils.lerp(fromY,toY,t);
            float opacity=MathUtils.sin(t*MathUtils.PI);
            s.setColor(.98f,.85f,.60f,.18f*opacity);
            s.circle(x,y,5,12);
            s.setColor(1,.94f,.73f,opacity);
            s.circle(x,y,1.7f,10);
        }
    }

    @Override public void dispose() { /* O desenho não mantém recursos gráficos próprios. */ }
}
