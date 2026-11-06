package com.nos;

import com.badlogic.gdx.Game;
import com.nos.screens.GameScreen;

/** Ponto de entrada compartilhado; mantém o ciclo de vida da tela. */
public class NosGame extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen());
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
    }
}
