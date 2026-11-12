package com.nos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nos.NosGame;

/** Tela de fim de jogo: mostra a pontuação da campanha e volta ao menu ou recomeça. */
public final class GameOverScreen extends ScreenAdapter {
    private final NosGame game;
    private final boolean won;
    private final int score,restarts;
    private final float seconds;
    private final FitViewport viewport=new FitViewport(960,540);
    private final SpriteBatch batch=new SpriteBatch();
    private final BitmapFont font=new BitmapFont();
    public GameOverScreen(NosGame game,boolean won,int score,float seconds,int restarts) {
        this.game=game;this.won=won;this.score=score;this.seconds=seconds;this.restarts=restarts;
    }
    @Override public void render(float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) { game.beginJourney(false);return; }
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)||Gdx.input.justTouched()) { game.returnToTitle();return; }
        ScreenUtils.clear(.05f,.06f,.09f,1);
        viewport.apply();batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        draw(won?"FIM DE JOGO - VITORIA":"FIM DE JOGO",3f,400);
        draw(won?"Sombra e Reflexo se reencontraram.":"As brasas venceram: acabaram as vidas.",1.4f,330);
        draw("Pontuação: "+score,2.2f,270);
        draw("Tempo: "+Math.round(seconds)+"s    Recomeços: "+restarts,1.3f,215);
        draw("Enter: menu inicial    R: jogar de novo",1.2f,130);
        batch.end();
    }
    private void draw(String text,float scale,float y) {
        font.getData().setScale(scale);font.draw(batch,text,0,y,960,com.badlogic.gdx.utils.Align.center,false);
    }
    @Override public void resize(int w,int h) { viewport.update(w,h,true); }
    @Override public void dispose() { batch.dispose();font.dispose(); }
}
