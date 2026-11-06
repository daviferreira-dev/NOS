package com.nos;

import com.badlogic.gdx.Game;
import com.nos.screens.GameScreen;
import com.nos.screens.IntroScreen;

/** Ponto de entrada compartilhado; mantém o ciclo de vida da tela. */
public class NosGame extends Game {
    private final boolean skipIntro;
    public NosGame() { this(false); }
    public NosGame(boolean skipIntro) { this.skipIntro=skipIntro; }

    @Override
    public void create() {
        setScreen(skipIntro ? new GameScreen() : new IntroScreen(this));
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
    }
}
