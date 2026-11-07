package com.nos;

import com.badlogic.gdx.Game;
import com.nos.screens.GameScreen;
import com.nos.screens.IntroScreen;
import com.nos.screens.PuzzleScreen;
import com.nos.screens.BridgeScreen;

/** Ponto de entrada compartilhado; mantém o ciclo de vida da tela. */
public class NosGame extends Game {
    private final boolean skipIntro;
    private final boolean legacy;
    public NosGame() { this(false); }
    public NosGame(boolean skipIntro) { this(skipIntro,false); }
    public NosGame(boolean skipIntro,boolean legacy) { this.skipIntro=skipIntro;this.legacy=legacy; }

    @Override
    public void create() {
        setScreen(legacy ? new GameScreen() : skipIntro ? new BridgeScreen() : new IntroScreen(this));
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
    }
}
