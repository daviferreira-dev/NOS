package com.nos.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.nos.data.RoomData;
import com.nos.entities.PuzzleHero;
import com.nos.world.MirrorPuzzle;

/** Apresenta a sala e seus sinais espaciais, sem lógica de colisão. */
public final class PuzzleRenderer implements Disposable {
    private final GameAssets assets=new GameAssets();
    private final SpriteBatch batch=new SpriteBatch();
    private final ShapeRenderer shapes=new ShapeRenderer();
    private final GlyphLayout layout=new GlyphLayout();
    private final Color ink=new Color(.23f,.22f,.36f,1),gold=new Color(.60f,.43f,.22f,1);
    private final Color paper=new Color(.98f,.95f,.87f,1),stone=new Color(.59f,.64f,.60f,1);
    private float animation;

    public void render(MirrorPuzzle room,Viewport viewport,float delta) {
        if(room.getState()==MirrorPuzzle.State.PLAYING) animation+=delta;
        viewport.apply();batch.setProjectionMatrix(viewport.getCamera().combined);shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();batch.setColor(Color.WHITE);
        // O início do panorama preserva as margens em aquarela, agora sem trilhas automáticas.
        batch.draw(assets.garden,0,0,960,540,0,0,assets.garden.getWidth()/3,assets.garden.getHeight(),false,false);
        for(float[] wall:room.getData().commonWalls) texturedWall(wall,Color.WHITE);
        for(float[] wall:room.getData().shadowWalls) texturedWall(wall,new Color(.48f,.44f,.75f,1));
        for(float[] wall:room.getData().reflectionWalls) texturedWall(wall,new Color(1,.80f,.49f,1));
        batch.setColor(Color.WHITE);
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        RoomData data=room.getData();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for(int i=0;i<6;i++) {
            shapes.setColor(.70f,.60f,.43f,1);shapes.rect(414+i*22,378,19,30);
            shapes.setColor(.84f,.74f,.55f,1);shapes.rect(414+i*22,382,17,3);
        }
        pad(data.shadowPad,ink,MirrorPuzzle.onPad(room.getShadow(),data.shadowPad));
        pad(data.reflectionPad,gold,MirrorPuzzle.onPad(room.getReflection(),data.reflectionPad));
        flower(data.shadowExit,ink,room.isGateOpen());flower(data.reflectionExit,gold,room.isGateOpen());
        anchor(room.getShadow(),ink,room.getAnchor().isShadowBound());
        anchor(room.getReflection(),gold,room.getAnchor().isReflectionBound());
        shapes.setColor(paper.r,paper.g,paper.b,.96f);shapes.rect(24,466,912,64);
        shapes.setColor(ink);shapes.rect(24,12,912,74);
        shapes.setColor(room.getAnchor().isShadowBound()?gold:stone);shapes.rect(630,26,130,43);
        shapes.setColor(room.getAnchor().isReflectionBound()?gold:stone);shapes.rect(780,26,130,43);
        shapes.setColor(paper.r,paper.g,paper.b,.3f);shapes.circle(90,50,31);
        shapes.setColor(paper);shapes.rectLine(72,50,108,50,2);shapes.rectLine(90,32,90,68,2);
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(ink.r,ink.g,ink.b,.28f);
        shapes.line(room.getShadow().getX(),room.getShadow().getY()+20,room.getReflection().getX(),room.getReflection().getY()+20);
        shapes.end();
        batch.begin();
        if(!room.isGateOpen()) {
            batch.setColor(Color.WHITE);batch.draw(assets.driftwood,406,372,148,45);
        }
        hero(room.getShadow(),assets.shadow,room.getState());hero(room.getReflection(),assets.reflection,room.getState());
        title(assets.text.get("puzzleTitle"),480,525,.45f);
        String goal=room.isGateOpen()?"puzzleExitHint":room.getAnchor().isReflectionBound()?"puzzleBoundHint":"puzzleGoal";
        centered(assets.text.get(goal),480,492,.35f,ink);
        text(assets.text.get("puzzleMove"),190,68,.33f,paper);
        text(assets.text.get("puzzleKeys"),190,41,.28f,paper);
        centered(assets.text.get(room.getAnchor().isShadowBound()?"puzzleReleaseQ":"puzzleBindQ"),695,53,.29f,paper);
        centered(assets.text.get(room.getAnchor().isReflectionBound()?"puzzleReleaseE":"puzzleBindE"),845,53,.29f,paper);
        centered(assets.text.get("puzzleShadowPad"),data.shadowPad[0],data.shadowPad[1]-25,.27f,ink);
        centered(assets.text.get("puzzleReflectionPad"),data.reflectionPad[0],data.reflectionPad[1]-25,.27f,ink);
        if(room.getAnchor().isShadowBound()) centered(assets.text.get("puzzleBound"),room.getShadow().getX(),room.getShadow().getY()+84,.27f,ink);
        if(room.getAnchor().isReflectionBound()) centered(assets.text.get("puzzleBound"),room.getReflection().getX(),room.getReflection().getY()+84,.27f,ink);
        batch.end();
        if(room.getState()!=MirrorPuzzle.State.PLAYING) overlay(room);
    }
    private void wall(float[] box,Color color) {
        int count=Math.max(1,(int)Math.ceil(Math.max(box[2],box[3])/29));
        boolean horizontal=box[2]>box[3];
        for(int i=0;i<count;i++) {
            float x=box[0]+(horizontal?i*box[2]/count:0),y=box[1]+(horizontal?0:i*box[3]/count);
            float w=horizontal?box[2]/count:box[2],h=horizontal?box[3]:box[3]/count;
            shapes.setColor(color.r*.72f,color.g*.72f,color.b*.72f,.95f);shapes.ellipse(x-2,y-3,w+3,h+3);
            shapes.setColor(color);shapes.ellipse(x,y,w,h);
            shapes.setColor(color.r*.8f+.16f,color.g*.8f+.16f,color.b*.8f+.16f,1);shapes.ellipse(x+3,y+h*.5f,w*.7f,h*.3f);
        }
    }
    private void texturedWall(float[] box,Color color) {
        int count=Math.max(1,(int)Math.ceil(Math.max(box[2],box[3])/33));
        boolean horizontal=box[2]>box[3];batch.setColor(color);
        for(int i=0;i<count;i++) {
            float x=box[0]+(horizontal?i*box[2]/count:0),y=box[1]+(horizontal?0:i*box[3]/count);
            float w=horizontal?box[2]/count:box[2],h=horizontal?box[3]:box[3]/count;
            batch.draw(assets.rock,x-5,y-5,w+10,h+10);
        }
    }
    private void flower(float[] point,Color color,boolean active) {
        shapes.setColor(color.r,color.g,color.b,active?.8f:.25f);shapes.circle(point[0],point[1],21);
        shapes.setColor(paper);
        for(int i=0;i<5;i++) {
            float angle=i*(float)Math.PI*2/5;
            shapes.circle(point[0]+(float)Math.cos(angle)*9,point[1]+(float)Math.sin(angle)*9,7);
        }
        shapes.setColor(color);shapes.circle(point[0],point[1],6);
    }
    private void pad(float[] point,Color color,boolean active) {
        shapes.setColor(color);shapes.circle(point[0],point[1],24);
        shapes.setColor(paper);shapes.circle(point[0],point[1],20);
        shapes.setColor(color.r,color.g,color.b,active?1:.25f);shapes.circle(point[0],point[1],15);
        shapes.setColor(paper);shapes.rectLine(point[0]-7,point[1],point[0]+7,point[1],2);
    }
    private void anchor(PuzzleHero hero,Color color,boolean bound) {
        shapes.setColor(color.r,color.g,color.b,.18f);shapes.ellipse(hero.getX()-14,hero.getY()-5,28,9);
        if(bound) { shapes.setColor(gold);shapes.circle(hero.getX(),hero.getY(),18);shapes.setColor(paper);shapes.circle(hero.getX(),hero.getY(),14); }
    }
    private void hero(PuzzleHero hero,TextureRegion texture,MirrorPuzzle.State state) {
        float bob=hero.isMoving()&&state==MirrorPuzzle.State.PLAYING?(float)Math.sin(animation*12)*1.5f:0;
        batch.setColor(Color.WHITE);
        boolean flip=hero.getFacing()<0;
        batch.draw(texture,hero.getX()+(flip?18:-18),hero.getY()-2+bob,flip?-36:36,77);
    }
    private void text(String value,float x,float y,float scale,Color color) {
        assets.font.getData().setScale(scale);assets.font.setColor(color);assets.font.draw(batch,value,x,y);
    }
    private void centered(String value,float x,float y,float scale,Color color) {
        assets.font.getData().setScale(scale);layout.setText(assets.font,value);text(value,x-layout.width/2,y,scale,color);
    }
    private void title(String value,float x,float y,float scale) {
        assets.title.getData().setScale(scale);assets.title.setColor(ink);layout.setText(assets.title,value);assets.title.draw(batch,value,x-layout.width/2,y);
    }
    private void overlay(MirrorPuzzle room) {
        Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);shapes.setColor(ink.r,ink.g,ink.b,.6f);shapes.rect(0,0,960,540);
        shapes.setColor(paper);shapes.rect(160,118,640,330);shapes.setColor(gold);shapes.rect(160,444,640,4);shapes.end();
        batch.begin();
        if(room.getState()==MirrorPuzzle.State.READY) {
            batch.setColor(Color.WHITE);batch.draw(assets.logo,395,345,170,85);
            title(assets.text.get("puzzleIntroTitle"),480,332,.5f);
            for(int i=1;i<=4;i++) centered(assets.text.get("puzzleIntro"+i),480,283-(i-1)*30,.34f,ink);
            centered(assets.text.get("puzzleStart"),480,151,.34f,gold);
        } else if(room.getState()==MirrorPuzzle.State.PAUSED) {
            title(assets.text.get("paused"),480,335,.6f);
            centered(assets.text.get("puzzleResume"),480,270,.4f,ink);
            centered(assets.text.get("puzzleKeys"),480,218,.32f,ink);
        } else {
            title(assets.text.get("puzzleWon"),480,345,.6f);
            centered(assets.text.get("puzzleWonDetail"),480,279,.4f,ink);
            centered(assets.text.get("puzzleAgain"),480,205,.35f,gold);
        }
        batch.end();
    }
    @Override public void dispose() { batch.dispose();shapes.dispose();assets.dispose(); }
}
