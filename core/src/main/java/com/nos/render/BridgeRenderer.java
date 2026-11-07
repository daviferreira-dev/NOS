package com.nos.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.nos.data.BridgeData;
import com.nos.entities.PuzzleHero;
import com.nos.world.BridgeEncounter;

/** A ponte, cordas e corpos compartilham a planta usada pela física. */
public final class BridgeRenderer implements Disposable {
    private final GameAssets assets=new GameAssets();
    private final SpriteBatch batch=new SpriteBatch();
    private final ShapeRenderer shapes=new ShapeRenderer();
    private final Matrix4 scene=new Matrix4();
    private final GlyphLayout layout=new GlyphLayout();
    private final Color ink=new Color(.23f,.22f,.36f,1),paper=new Color(.98f,.95f,.87f,1),gold=new Color(.64f,.45f,.22f,1);
    private float camera,time;
    public void snapTo(BridgeEncounter encounter) {
        camera=Math.max(0,Math.min(encounter.getData().width-960,encounter.hero(encounter.getActive()).getX()-360));
    }
    public void render(BridgeEncounter encounter,Viewport viewport,float delta) {
        if(encounter.getState()==BridgeEncounter.State.PLAYING) time+=delta;
        BridgeData data=encounter.getData();
        float[] interaction=encounter.interactionPoint();
        float target=Math.max(0,Math.min(data.width-960,encounter.hero(encounter.getActive()).getX()-360));
        camera+=(target-camera)*(1-(float)Math.exp(-5*delta));
        viewport.apply();scene.setToOrtho2D(camera,0,960,540);
        batch.setProjectionMatrix(scene);shapes.setProjectionMatrix(scene);
        batch.begin();batch.setColor(Color.WHITE);batch.draw(assets.bridgeBackground,0,0,data.width,data.height);
        for(float[] obstacle:data.obstacles) { batch.setColor(.82f,.86f,.76f,1);batch.draw(assets.rock,obstacle[0],obstacle[1],obstacle[2],obstacle[3]); }
        batch.setColor(Color.WHITE);batch.end();
        blend();shapes.begin(ShapeRenderer.ShapeType.Filled);
        float[] bridge=data.bridge;
        shapes.setColor(.16f,.20f,.22f,.2f);
        shapes.rect(bridge[0],bridge[1]-4,bridge[2]*encounter.getSupport(),bridge[3]+8);
        shapes.end();
        blend();shapes.begin(ShapeRenderer.ShapeType.Line);
        if(encounter.getSupport()>.05f) {
            shapes.setColor(.45f,.36f,.23f,.85f);
            float end=bridge[0]+bridge[2]*encounter.getSupport();
            shapes.line(bridge[0],bridge[1]+bridge[3],end,bridge[1]+bridge[3]);
            shapes.line(bridge[0],bridge[1],end,bridge[1]);
        }
        if(encounter.getBound()>=0) {
            PuzzleHero bound=encounter.hero(encounter.getBound());shapes.setColor(gold);
            shapes.line(bound.getX()+13,bound.getY()+34,data.winch[0]+16,data.winch[1]+20);
        }
        shapes.setColor(.84f,.87f,.86f,.25f);
        for(int i=0;i<13;i++) {
            float y=80+(i*39+time*13)%390;
            float x=data.river[0]+30+(i*71)%240;
            shapes.line(x,y,x+14,y+5);
        }
        shapes.end();
        batch.begin();batch.setColor(Color.WHITE);
        float visible=encounter.getSupport();
        if(visible>0) batch.draw(assets.bridgeDeck,bridge[0]-7,bridge[1]-5,(bridge[2]+14)*visible,bridge[3]+10,
            0,0,Math.max(1,(int)(assets.bridgeDeck.getWidth()*visible)),assets.bridgeDeck.getHeight(),false,false);
        mechanism(data.winch,0,encounter.getBound()>=0);
        mechanism(data.latch,1,encounter.isLocked());
        batch.end();
        blend();shapes.begin(ShapeRenderer.ShapeType.Filled);
        for(int i=0;i<2;i++) {
            PuzzleHero h=encounter.hero(i);
            shapes.setColor(ink.r,ink.g,ink.b,.16f);shapes.ellipse(h.getX()-15,h.getY()-5,30,10);
            if(i==encounter.getActive()) { shapes.setColor(gold.r,gold.g,gold.b,.75f);shapes.rectLine(h.getX()-8,h.getY()-5,h.getX()+8,h.getY()-5,2); }
        }
        shapes.end();batch.begin();batch.setColor(Color.WHITE);
        if(encounter.hero(0).getY()>encounter.hero(1).getY()) { hero(encounter,0);hero(encounter,1); }
        else { hero(encounter,1);hero(encounter,0); }
        batch.end();
        if(interaction!=null&&encounter.getState()==BridgeEncounter.State.PLAYING) {
            blend();shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(paper.r,paper.g,paper.b,.96f);
            shapes.rect(interaction[0]-62,interaction[1]+67,124,25);
            shapes.setColor(gold);shapes.rect(interaction[0]-62,interaction[1]+67,124,2);
            shapes.end();batch.begin();
            centered(assets.text.get(encounter.action()),interaction[0],interaction[1]+84,.29f,ink);
            batch.end();
        }
        batch.setProjectionMatrix(viewport.getCamera().combined);shapes.setProjectionMatrix(viewport.getCamera().combined);
        blend();shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(paper.r,paper.g,paper.b,.94f);shapes.rect(24,475,912,54);
        shapes.setColor(ink.r,ink.g,ink.b,.94f);shapes.rect(24,18,912,67);
        shapes.setColor(.72f,.69f,.61f,.4f);shapes.circle(90,50,28);
        shapes.setColor(paper);shapes.rectLine(75,50,105,50,2);shapes.rectLine(90,35,90,65,2);
        shapes.setColor(.49f,.51f,.49f,1);shapes.rect(630,26,130,43);
        shapes.setColor(gold.r,gold.g,gold.b,interaction!=null?1:.4f);shapes.rect(780,26,130,43);
        shapes.end();
        batch.begin();
        title(assets.text.get("bridgeTitle"),480,525,.43f);
        centered(assets.text.get(encounter.hint()),480,494,.33f,ink);
        text(assets.text.get("bridgeMove"),155,69,.33f,paper);
        text(assets.text.get(encounter.companionStatus()),155,42,.29f,paper);
        centered(assets.text.get("bridgeSwap"),695,53,.29f,paper);
        centered(assets.text.get(encounter.action()),845,53,.27f,paper);
        batch.end();
        if(encounter.getState()!=BridgeEncounter.State.PLAYING) overlay(encounter);
    }
    private void mechanism(float[] point,int type,boolean active) {
        batch.draw(assets.mechanisms[active?1:0][type],point[0]-33,point[1]-10,66,66);
    }
    private void hero(BridgeEncounter encounter,int index) {
        PuzzleHero hero=encounter.hero(index);
        HeroFrame frame=encounter.getBound()==index?assets.poses[index][1]
            :hero.isMoving()?assets.walk[index][encounter.frame(index)]:assets.poses[index][0];
        frame.draw(batch,hero.getX(),hero.getY(),hero.getFacing()<0);
    }
    private void blend() { Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA); }
    private void text(String s,float x,float y,float scale,Color color) {
        assets.font.getData().setScale(scale);assets.font.setColor(color);assets.font.draw(batch,s,x,y);
    }
    private void centered(String s,float x,float y,float scale,Color color) {
        assets.font.getData().setScale(scale);layout.setText(assets.font,s);text(s,x-layout.width/2,y,scale,color);
    }
    private void title(String s,float x,float y,float scale) {
        assets.title.getData().setScale(scale);assets.title.setColor(ink);layout.setText(assets.title,s);assets.title.draw(batch,s,x-layout.width/2,y);
    }
    private void overlay(BridgeEncounter encounter) {
        blend();shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(ink.r,ink.g,ink.b,.56f);shapes.rect(0,0,960,540);
        shapes.setColor(paper);shapes.rect(175,135,610,300);shapes.setColor(gold);shapes.rect(175,431,610,4);shapes.end();
        batch.begin();batch.setColor(Color.WHITE);
        if(encounter.getState()==BridgeEncounter.State.READY) {
            batch.draw(assets.logo,405,341,150,75);
            title(assets.text.get("bridgeIntroTitle"),480,329,.45f);
            for(int i=1;i<=3;i++) centered(assets.text.get("bridgeIntro"+i),480,282-(i-1)*31,.34f,ink);
            centered(assets.text.get("bridgeStart"),480,164,.34f,gold);
        } else if(encounter.getState()==BridgeEncounter.State.PAUSED) {
            title(assets.text.get("paused"),480,340,.6f);
            centered(assets.text.get("bridgeResume"),480,265,.36f,ink);
            centered(assets.text.get("bridgeKeys"),480,212,.3f,ink);
        } else {
            title(assets.text.get("bridgeWon"),480,340,.54f);
            centered(assets.text.get("bridgeWonDetail"),480,267,.36f,ink);
            centered(assets.text.get("puzzleAgain"),480,192,.33f,gold);
        }
        batch.end();
    }
    @Override public void dispose() { batch.dispose();shapes.dispose();assets.dispose(); }
}
