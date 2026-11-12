package com.nos.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.nos.data.FurnaceData;
import com.nos.systems.FurnaceSystem;
import com.nos.world.BridgeEncounter;

/** Os dutos dividem a ferraria em três corredores com o acabamento da própria pintura. */
public final class FurnaceRenderer implements Disposable {
    private final TextureRegion stove, pipe, iron;

    public FurnaceRenderer(GameAssets assets) {
        Texture painting = assets.bridgeBackground;
        stove = crop(painting, .346f, .036f, .117f, .290f);
        pipe = crop(painting, .021f, .880f, .070f, .106f);
        iron = crop(painting, .218f, .930f, .091f, .052f);
    }

    private static TextureRegion crop(Texture source, float u, float v, float w, float h) {
        return new TextureRegion(source, (int)(source.getWidth()*u), (int)(source.getHeight()*v),
            (int)(source.getWidth()*w), (int)(source.getHeight()*h));
    }

    public void draw(BridgeEncounter world, SpriteBatch batch, ShapeRenderer shapes) {
        FurnaceData data = world.getData().furnace;
        FurnaceSystem furnace = (FurnaceSystem)world.challenge();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (float[] machine : world.getData().obstacles) {
            shapes.setColor(.075f, .036f, .049f, .65f);
            shapes.rect(machine[0]-2, machine[1]-4, machine[2]+4, machine[3]+4);
        }
        shapes.end();

        batch.begin();
        batch.setColor(1,1,1,1);
        for (float[] machine : world.getData().obstacles) {
            boolean furnaceBank = machine[1] >= data.hotZone[1] + data.hotZone[3];
            TextureRegion material = furnaceBank ? stove : machine[3] < 32 ? iron : pipe;
            int sections = Math.max(1, Math.round(machine[2] / (furnaceBank ? 102f : 79f)));
            float width = machine[2] / sections;
            for (int section=0; section<sections; section++) {
                batch.draw(material, machine[0]+width*section, machine[1], width, machine[3]);
            }
        }
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (float[] machine : world.getData().obstacles) {
            float x=machine[0], y=machine[1], width=machine[2], height=machine[3];
            shapes.setColor(.15f,.08f,.075f,.87f);
            shapes.rect(x,y,width,3);
            shapes.setColor(.72f,.45f,.26f,.76f);
            shapes.rect(x,y+height-1.5f,width,1.5f);
            for (int edge=0; edge<2; edge++) {
                float cap = x+(width-7)*edge;
                shapes.setColor(.20f,.13f,.12f,1);
                shapes.rect(cap,y,7,height);
                shapes.setColor(.69f,.49f,.31f,.8f);
                shapes.rect(cap+1,y+2,1,height-4);
                shapes.circle(cap+3.5f,y+5,1.3f,8);
                shapes.circle(cap+3.5f,y+height-5,1.3f,8);
            }
        }
        for (int i=0; i<world.actions().count(); i++) {
            float x=world.actions().hazardX(i), y=world.actions().hazardY(i);
            float heat=world.actions().isActive(i) ? 1f : world.actions().warning(i);
            vent(shapes,x,y,world.getData().hazards[i].radius,heat);
        }
        gate(shapes,data.gate,furnace.isDoorOpen());
        lever(shapes,data.lever[0],data.lever[1],furnace.isDoorOpen());
        shapes.end();
    }

    private static void vent(ShapeRenderer s,float x,float y,float radius,float heat) {
        s.setColor(.12f,.085f,.09f,.92f);
        s.ellipse(x-radius,y-radius*.37f,2*radius,.74f*radius,24);
        float half = radius*.8f;
        for (int i=-2; i<=2; i++) {
            float offset=i*radius*.11f;
            float length=half*MathUtils.cos(i*.39f);
            s.setColor(.45f+.53f*heat,.28f+.37f*heat,.19f-.05f*heat,1);
            s.rectLine(x-length,y+offset,x+length,y+offset,1.35f+heat);
        }
    }

    private static void gate(ShapeRenderer s,float[] box,boolean open) {
        float x=box[0], y=box[1], w=box[2], h=box[3];
        s.setColor(.19f,.12f,.12f,.85f);
        s.rect(x,y-2,w,4);
        s.rect(x,y+h-2,w,4);
        if (!open) {
            s.setColor(.15f,.11f,.12f,.8f);
            s.rect(x,y,w,h);
            for (float row=3; row<h; row+=7) {
                s.setColor(.43f,.32f,.25f,1);
                s.rect(x+1,y+row,w-2,3);
                s.setColor(.69f,.50f,.34f,1);
                s.rect(x+1,y+row+2,w-2,1);
            }
        }
        s.setColor(.73f,.51f,.34f,.95f);
        s.rect(x,y,2,open?6:h);
        s.rect(x+w-2,y+h-(open?6:h),2,open?6:h);
    }

    private static void lever(ShapeRenderer s,float x,float y,boolean open) {
        s.setColor(.10f,.07f,.09f,.5f);
        s.ellipse(x-13,y-4,32,9,20);
        s.setColor(.21f,.15f,.15f,1);
        s.ellipse(x-11,y-3,22,9,20);
        s.rect(x-8,y+2,16,9);
        s.setColor(.58f,.36f,.24f,1);
        s.ellipse(x-8,y+7,16,7,16);
        float end = open ? x+10 : x-9;
        s.setColor(.17f,.11f,.11f,1);
        s.rectLine(x,y+9,end,y+26,5);
        s.setColor(.83f,.61f,.38f,1);
        s.rectLine(x-1,y+10,end-1,y+26,1.4f);
        s.setColor(.92f,.75f,.52f,1);
        s.circle(end,y+27,3.6f,12);
    }

    @Override public void dispose() { /* O AssetManager é dono da pintura compartilhada. */ }
}
