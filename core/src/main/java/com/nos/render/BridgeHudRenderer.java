package com.nos.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.nos.world.BridgeEncounter;

/** Objetivo persistente, retorno temporário e comandos secundários têm hierarquias distintas. */
final class BridgeHudRenderer {
    private final GameAssets assets;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final GlyphLayout layout=new GlyphLayout();
    private final Color ink=new Color(.19f,.20f,.32f,1),paper=new Color(.985f,.96f,.89f,1),gold=new Color(.61f,.43f,.23f,1);
    BridgeHudRenderer(GameAssets assets,SpriteBatch batch,ShapeRenderer shapes) { this.assets=assets;this.batch=batch;this.shapes=shapes; }
    void render(BridgeEncounter encounter,boolean touch,boolean help) {
        String goal=assets.text.get("bridgeGoal"+encounter.objectiveStep());
        assets.font.getData().setScale(.48f);layout.setText(assets.font,goal,ink,332,Align.left,true);
        float height=Math.max(76,48+layout.height),bottom=516-height;
        String notice=encounter.getState()==BridgeEncounter.State.PLAYING?encounter.notice():null;
        float alpha=encounter.noticeVisibility();
        float noticeHeight=0;
        if(notice!=null) {
            assets.font.getData().setScale(.34f);layout.setText(assets.font,assets.text.get(notice),paper,472,Align.left,true);
            noticeHeight=Math.max(76,50+layout.height);
        }
        assets.font.getData().setScale(.31f);layout.setText(assets.font,assets.text.get("bridgeShortKeys"));
        float keysWidth=layout.width+32,keysX=936-keysWidth;
        fill();
        shapes.setColor(paper.r,paper.g,paper.b,.96f);shapes.rect(24,bottom,376,height);
        shapes.setColor(gold);shapes.rect(24,bottom,4,height);
        shapes.setColor(ink.r,ink.g,ink.b,.93f);shapes.rect(touch?178:24,20,touch?340:280,57);
        if(touch) {
            shapes.setColor(ink.r,ink.g,ink.b,.7f);shapes.circle(90,50,33);
            shapes.setColor(paper);shapes.rectLine(73,50,107,50,2);shapes.rectLine(90,33,90,67,2);
            shapes.setColor(ink.r,ink.g,ink.b,.9f);shapes.rect(630,26,130,43);
            shapes.setColor(gold.r,gold.g,gold.b,encounter.interactionPoint()!=null?1:.4f);shapes.rect(780,26,130,43);
        } else { shapes.setColor(ink.r,ink.g,ink.b,.82f);shapes.rect(keysX,20,keysWidth,34); }
        if(notice!=null) {
            shapes.setColor(ink.r,ink.g,ink.b,.95f*alpha);shapes.rect(424,516-noticeHeight,512,noticeHeight);
            shapes.setColor(gold.r,gold.g,gold.b,alpha);shapes.rect(424,516-noticeHeight,4,noticeHeight);
        }
        shapes.end();batch.begin();
        text(assets.text.get("bridgeStage"+encounter.objectiveStep()),44,510,.29f,gold);
        wrapped(goal,44,485,332,.48f,ink);
        float statusX=touch?198:44;
        text(assets.text.get(encounter.getActive()==0?"bridgeShadowControlled":"bridgeReflectionControlled"),statusX,68,.38f,paper);
        String partner=encounter.getBound()==encounter.getActive()?"bridgeYouHoldingShort":encounter.getBound()==0?"bridgeShadowHoldingShort"
            :encounter.getBound()==1?"bridgeReflectionHoldingShort":"bridgeFollowingShort";
        text(assets.text.get(partner),statusX,42,.30f,paper);
        if(touch) {
            centered(assets.text.get("bridgeSwap"),695,56,.29f,paper);
            centered(assets.text.get(encounter.action()),845,56,.27f,paper);
        } else text(assets.text.get("bridgeShortKeys"),keysX+16,46,.31f,paper);
        if(notice!=null) {
            Color tint=new Color(paper.r,paper.g,paper.b,alpha);
            text(assets.text.get(notice+"Title"),444,503,.40f,tint);
            wrapped(assets.text.get(notice),444,472,472,.34f,tint);
        }
        batch.end();
        if(help&&encounter.getState()==BridgeEncounter.State.PLAYING) help();
        if(encounter.getState()!=BridgeEncounter.State.PLAYING) overlay(encounter);
    }
    private void help() {
        fill();shapes.setColor(paper.r,paper.g,paper.b,.98f);shapes.rect(155,96,650,157);
        shapes.setColor(gold);shapes.rect(155,249,650,4);shapes.end();
        batch.begin();text(assets.text.get("bridgeHelpTitle"),177,240,.45f,ink);
        for(int i=1;i<=3;i++) wrapped(assets.text.get("bridgeHelp"+i),177,205-(i-1)*34,607,.31f,ink);
        batch.end();
    }
    private void overlay(BridgeEncounter encounter) {
        fill();shapes.setColor(ink.r,ink.g,ink.b,.56f);shapes.rect(0,0,960,540);
        shapes.setColor(paper);shapes.rect(175,135,610,300);shapes.setColor(gold);shapes.rect(175,431,610,4);shapes.end();
        batch.begin();batch.setColor(Color.WHITE);
        if(encounter.getState()==BridgeEncounter.State.READY) {
            batch.draw(assets.logo,405,341,150,75);title(assets.text.get("bridgeIntroTitle"),480,329,.45f);
            for(int i=1;i<=3;i++) centered(assets.text.get("bridgeIntro"+i),480,282-(i-1)*31,.34f,ink);
            centered(assets.text.get("bridgeStart"),480,164,.34f,gold);
        } else if(encounter.getState()==BridgeEncounter.State.PAUSED) {
            title(assets.text.get("paused"),480,340,.6f);centered(assets.text.get("bridgeResume"),480,265,.36f,ink);
            centered(assets.text.get("bridgeKeys"),480,212,.3f,ink);
        } else {
            title(assets.text.get("bridgeWon"),480,340,.54f);centered(assets.text.get("bridgeWonDetail"),480,267,.36f,ink);
            centered(assets.text.get("puzzleAgain"),480,192,.33f,gold);
        }
        batch.end();
    }
    private void fill() { Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);shapes.begin(ShapeRenderer.ShapeType.Filled); }
    private void text(String s,float x,float y,float scale,Color color) { assets.font.getData().setScale(scale);assets.font.setColor(color);assets.font.draw(batch,s,x,y); }
    private void wrapped(String s,float x,float y,float width,float scale,Color color) { assets.font.getData().setScale(scale);assets.font.setColor(color);assets.font.draw(batch,s,x,y,width,Align.left,true); }
    private void centered(String s,float x,float y,float scale,Color color) { assets.font.getData().setScale(scale);layout.setText(assets.font,s);text(s,x-layout.width/2,y,scale,color); }
    private void title(String s,float x,float y,float scale) { assets.title.getData().setScale(scale);assets.title.setColor(ink);layout.setText(assets.title,s);assets.title.draw(batch,s,x-layout.width/2,y); }
}
