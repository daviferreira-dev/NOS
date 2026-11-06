package com.nos.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nos.render.GameAssets;

/** Abertura em quadros animados, curta e pulável, sem vídeo externo. */
public final class IntroScreen extends ScreenAdapter {
    private final Game game;
    private final GameAssets assets=new GameAssets();
    private final SpriteBatch batch=new SpriteBatch();
    private final FitViewport viewport=new FitViewport(960,540);
    private final GlyphLayout layout=new GlyphLayout();
    private float time;
    private boolean skip, paused;
    private final InputAdapter input=new InputAdapter() {
        @Override public boolean keyDown(int key) { skip=true;return true; }
        @Override public boolean touchDown(int x,int y,int pointer,int button) { skip=true;return true; }
    };
    public IntroScreen(Game game) { this.game=game; }
    public void prepareSmokeScene() { time=4; }
    @Override public void show() { Gdx.input.setInputProcessor(input); }
    @Override public void render(float delta) {
        if(!paused) time+=Math.min(delta,.05f);
        if(skip||time>=10) { game.setScreen(new GameScreen());dispose();return; }
        ScreenUtils.clear(.97f,.94f,.87f,1);
        viewport.apply();batch.setProjectionMatrix(viewport.getCamera().combined);batch.begin();
        batch.setColor(1,1,1,.65f);
        batch.draw(assets.garden,-time*20,-20,1500,580);
        float presence=clamp((time-1)/2);
        float approach=clamp((time-2)/4);
        batch.setColor(1,1,1,presence);
        draw(assets.shadow,240+approach*170,195,82,175);
        draw(assets.reflection,638-approach*170,195,82,175);
        batch.setColor(1,1,1,clamp((time-4)/2));
        batch.draw(assets.knot,425,105,110,55);
        assets.title.getData().setScale(.48f);
        assets.title.setColor(.23f,.22f,.36f,presence*(1-clamp(time-6)));
        String verse=assets.text.get(time<4 ? "introVerse1" : "introVerse2");
        layout.setText(assets.title,verse);
        assets.title.draw(batch,verse,480-layout.width/2,424);
        if(time>7) {
            batch.setColor(1,1,1,clamp(time-7));
            batch.draw(assets.logo,355,320,250,125);
        }
        assets.font.getData().setScale(.3f);assets.font.setColor(.23f,.22f,.36f,.85f);
        assets.font.draw(batch,assets.text.get("skipIntro"),32,35);
        batch.setColor(Color.WHITE);batch.end();
    }
    private float clamp(float value) { return Math.max(0,Math.min(1,value)); }
    private void draw(TextureRegion region,float x,float y,float w,float h) { batch.draw(region,x,y,w,h); }
    @Override public void resize(int w,int h) { viewport.update(w,h,true); }
    @Override public void pause() { paused=true; }
    @Override public void resume() { paused=false; }
    @Override public void hide() { if(Gdx.input.getInputProcessor()==input) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { hide();batch.dispose();assets.dispose(); }
}
