package com.nos.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.nos.data.Tuning;
import com.nos.input.BrushInput;
import com.nos.systems.GazeTimeSystem;
import com.nos.systems.InkEncounterSystem.Crossing;
import com.nos.systems.ObstacleSystem.Gate;
import com.nos.world.*;

/** Ambiente, personagens e indicadores; nenhuma regra de jogo vive aqui. */
public final class PlayRenderer implements Disposable {
    private static final Color INK = new Color(.23f,.22f,.36f,1);
    private static final Color PAPER = new Color(.98f,.94f,.84f,1);
    private static final Color GOLD = new Color(.65f,.45f,.2f,1);
    private static final Color SAFE = new Color(.18f,.48f,.40f,1);
    private static final Color DANGER = new Color(.69f,.26f,.25f,1);
    private final GameAssets assets = new GameAssets();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final Vector2 previous = new Vector2(), current = new Vector2();
    private final GlyphLayout layout = new GlyphLayout();
    private final Matrix4 scene = new Matrix4();

    public void render(PlaySession session, Viewport viewport, BrushInput input, GazeTimeSystem gaze, int best, JourneyCamera camera) {
        viewport.apply();
        scene.setToOrtho2D(camera.left,camera.bottom,camera.width,camera.height);
        batch.setProjectionMatrix(scene);
        shapes.setProjectionMatrix(scene);
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(assets.garden, 0, 0, JourneyCamera.MAP_WIDTH, 540);
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawTrail(session.getPair().getWorldA(), INK);
        drawTrail(session.getPair().getWorldB(), GOLD);
        for (Crossing c : session.getEncounters().getCrossings()) {
            shapes.setColor(c.collected ? SAFE : PAPER);
            for (int i = 0; i < 4; i++) {
                float angle = i * (float)Math.PI / 2;
                shapes.circle(c.position.x + (float)Math.cos(angle) * 4, c.position.y + (float)Math.sin(angle) * 4, 3);
            }
            shapes.setColor(GOLD); shapes.circle(c.position.x, c.position.y, 2);
        }
        for (Gate gate : session.getObstacles().getGates()) drawGate(gate, session);
        drawAnchor(session.getPair().getWorldA(), INK);
        drawAnchor(session.getPair().getWorldB(), GOLD);
        shapes.end();
        batch.begin();
        batch.setColor(Color.WHITE);
        for(Gate gate:session.getObstacles().getGates()) {
            float x=gate.position.x,y=gate.position.y+gate.drift(session.getTime());
            batch.draw(assets.driftwood,x-65,y-30,65,30,130,60,1,1,82);
        }
        drawHero(session.getPair().getWorldA(), assets.shadow, session.getState());
        drawHero(session.getPair().getWorldB(), assets.reflection, session.getState());
        batch.end();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(.98f,.95f,.88f,.93f);
        shapes.rect(24, 447, 912, 83);
        shapes.setColor(INK); shapes.rect(24, 18, 912, 66);
        drawBar(42, 468, 230, session.getPair().getWorldA().getInk(), INK);
        drawBar(688, 468, 230, session.getPair().getWorldB().getInk(), GOLD);
        drawBar(366, 468, 228, session.getHarmony().getStrain(), DANGER);
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        if (input.isActive() && session.getState() == PlaySession.State.PLAYING) {
            shapes.setColor(INK);
            shapes.circle(input.getX(), input.getY(), 14);
            shapes.line(input.getX()-4,input.getY(),input.getX()+4,input.getY());
            shapes.line(input.getX(),input.getY()-4,input.getX(),input.getY()+4);
        }
        shapes.end();
        batch.begin();
        text(assets.text.get("chapter"), 42, 520, 1.05f, INK);
        centeredTitle(assets.text.get("level"),480,527,.42f,INK);
        text(assets.text.format("time", (int)session.getTime()), 810, 520, 1f, INK);
        text(assets.text.get("shadow"), 42, 496, .85f, INK);
        text(assets.text.get("harmony"), 366, 496, .85f, INK);
        text(assets.text.get("reflection"), 688, 496, .85f, INK);
        text(speed(session.getPair().getWorldA()), 42, 462, .8f, INK);
        centered(assets.text.format("attempts", session.getPot().getRemaining()), 480, 462, .8f, INK);
        text(speed(session.getPair().getWorldB()), 688, 462, .8f, INK);
        text(assets.text.get(session.getNotice()), 42, 67, .95f, PAPER);
        text(assets.text.get("controls"), 42, 41, .72f, PAPER);
        text(assets.text.get(gaze.getMode() == GazeTimeSystem.Mode.PROXIMITY ? "modeNear" : "modeHorizontal"), 739, 41, .72f, PAPER);
        if (session.hasCheckpoint()) centered(assets.text.get("checkpointLabel"), 480, 430, .75f, INK);
        batch.end();
        if (session.getState() != PlaySession.State.PLAYING) overlay(session, best);
    }

    private String speed(World world) {
        if (world.getInk() == 0) return assets.text.get("inkLost");
        return assets.text.format("speed", Math.round(world.getSpeed()*100)/100f);
    }
    private void drawTrail(World world, Color color) {
        Trail trail = world.getTrail();
        trail.positionAtDistance(0, previous);
        for (int i = 1; i <= Tuning.TRAIL_SAMPLES; i++) {
            trail.positionAtDistance(trail.getLength() * i / Tuning.TRAIL_SAMPLES, current);
            shapes.setColor(PAPER.r,PAPER.g,PAPER.b,world.getInk()*.7f);
            shapes.rectLine(previous,current,7);
            shapes.setColor(color.r,color.g,color.b,world.getInk()*.85f);
            shapes.rectLine(previous,current,2.8f);
            previous.set(current);
        }
        trail.positionAtDistance(trail.getLength(), current);
        shapes.setColor(color); shapes.circle(current.x,current.y,15);
        shapes.setColor(PAPER); shapes.circle(current.x,current.y,11);
        shapes.setColor(color); shapes.circle(current.x,current.y,4);
    }
    private void drawAnchor(World world, Color color) {
        shapes.setColor(color.r,color.g,color.b,.18f);
        shapes.ellipse(world.getHero().getX()-14,world.getHero().getY()-4,28,8);
    }
    private void drawGate(Gate gate, PlaySession session) {
        float x=gate.position.x,y=gate.position.y+gate.drift(session.getTime());
        shapes.setColor(.33f,.29f,.23f,.17f); shapes.ellipse(x-8,y-25,16,60);
        shapes.setColor(.38f,.47f,.40f,.45f);
        for(int i=-2;i<=2;i++) shapes.rectLine(x-24,y+i*15,x-14,y+i*15+2,1);
    }
    private void drawBar(float x, float y, float width, float value, Color color) {
        shapes.setColor(.82f,.80f,.75f,1); shapes.rect(x,y,width,7);
        shapes.setColor(color); shapes.rect(x,y,width*value,7);
    }
    private void drawHero(World world, TextureRegion region, PlaySession.State state) {
        float bob = state == PlaySession.State.PLAYING && !world.getHero().hasArrived() && world.getInk()>0
            ? (float)Math.sin(world.getElapsedTime()*9)*1.2f : 0;
        batch.setColor(1,1,1,.35f+.65f*world.getInk());
        batch.draw(region, world.getHero().getX()-22, world.getHero().getY()-3+bob, 44, 94);
        batch.setColor(Color.WHITE);
    }
    private void text(String value, float x, float y, float scale, Color color) {
        assets.font.getData().setScale(scale*.375f);
        assets.font.setColor(color);
        assets.font.draw(batch, value, x,y);
    }
    private void centered(String value, float x, float y, float scale, Color color) {
        assets.font.getData().setScale(scale*.375f);
        layout.setText(assets.font,value);
        text(value,x-layout.width/2,y,scale,color);
    }
    private void centeredTitle(String value,float x,float y,float scale,Color color) {
        assets.title.getData().setScale(scale); assets.title.setColor(color);
        layout.setText(assets.title,value); assets.title.draw(batch,value,x-layout.width/2,y);
    }
    private void overlay(PlaySession session, int best) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(.12f,.12f,.19f,.68f); shapes.rect(0,0,960,540);
        shapes.setColor(PAPER); shapes.rect(155,101,650,359);
        shapes.setColor(GOLD); shapes.rect(155,456,650,4);
        shapes.end();
        batch.begin();
        PlaySession.State state=session.getState();
        if (state == PlaySession.State.READY) {
            batch.setColor(Color.WHITE); batch.draw(assets.logo,390,343,180,90);
            centeredTitle(assets.text.get("introTitle"),480,331,.45f,INK);
            for(int i=2;i<=5;i++) centered(assets.text.get("intro"+i),480,286-(i-2)*28,.82f,INK);
            centered(assets.text.get("difficulty"),480,163,.85f,INK);
            String difficulty=session.getDifficulty()<1 ? "easy" : session.getDifficulty()==1 ? "normal" : "hard";
            centered(assets.text.format("selected",assets.text.get(difficulty)),480,142,.8f,GOLD);
            centered(assets.text.get("start"),480,119,.85f,INK);
        } else if (state == PlaySession.State.PAUSED) {
            centeredTitle(assets.text.get("paused"),480,338,.6f,INK);
            centered(assets.text.get("resume"),480,265,1f,INK);
            centered(assets.text.get("controls"),480,205,.75f,INK);
        } else {
            centeredTitle(assets.text.get(state==PlaySession.State.WON ? "won" : "lost"),480,355,.48f,INK);
            centered(assets.text.format("result",session.getScore(),session.getEncounterCount(),session.getHits()),480,294,.95f,INK);
            centered(assets.text.format("best",best),480,255,1f,GOLD);
            String detail=state==PlaySession.State.WON ? assets.text.format("scoreSync",Math.round(session.getArrivalGap()*10)/10f)
                : assets.text.get(session.getNotice());
            centered(detail,480,217,.8f,INK);
            centered(assets.text.get("again"),480,157,1f,INK);
        }
        batch.end();
    }
    @Override public void dispose() { batch.dispose(); shapes.dispose(); assets.dispose(); }
}
