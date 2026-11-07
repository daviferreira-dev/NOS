package com.nos;

import com.badlogic.gdx.Game;
import com.nos.screens.GameScreen;
import com.nos.screens.IntroScreen;
import com.nos.screens.PuzzleScreen;
import com.nos.screens.BridgeScreen;
import com.nos.audio.Soundtrack;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/** Ponto de entrada compartilhado; mantém o ciclo de vida da tela. */
public class NosGame extends Game {
    private final boolean skipIntro;
    private final boolean legacy;
    private final boolean silent;
    private Soundtrack soundtrack;
    public NosGame() { this(false); }
    public NosGame(boolean skipIntro) { this(skipIntro,false); }
    public NosGame(boolean skipIntro,boolean legacy) { this(skipIntro,legacy,false); }
    public NosGame(boolean skipIntro,boolean legacy,boolean silent) { this.skipIntro=skipIntro;this.legacy=legacy;this.silent=silent; }

    @Override
    public void create() {
        soundtrack=new Soundtrack(silent);
        setScreen(legacy ? new GameScreen() : skipIntro ? new BridgeScreen() : new IntroScreen(this));
        if(skipIntro) soundtrack.journey();
    }

    public Soundtrack soundtrack() { return soundtrack; }
    public void beginJourney() {
        com.badlogic.gdx.Screen previous=getScreen();
        setScreen(new BridgeScreen(true));soundtrack.journey();previous.dispose();
    }
    @Override public void render() {
        if(soundtrack!=null&&Gdx.input.isKeyJustPressed(Input.Keys.M)
            &&!(getScreen() instanceof BridgeScreen&&((BridgeScreen)getScreen()).isConfiguringControls())) soundtrack.toggleMute();
        super.render();
        if(soundtrack!=null) soundtrack.update(Math.min(.1f,Gdx.graphics.getDeltaTime()));
    }
    @Override public void pause() { super.pause();if(soundtrack!=null) soundtrack.pause(); }
    @Override public void resume() { super.resume();if(soundtrack!=null) soundtrack.resume(); }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        if(soundtrack!=null) soundtrack.dispose();
    }
}
