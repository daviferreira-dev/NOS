package com.nos.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nos.data.LevelLoader;
import com.nos.data.Tuning;
import com.nos.render.PrototypeRenderer;
import com.nos.world.MirrorPair;

/** Orquestra a simulação e a apresentação do protótipo M1. */
public final class GameScreen extends ScreenAdapter {
    private final MirrorPair pair = new LevelLoader().load("levels/m1.json");
    private final FitViewport viewport = new FitViewport(Tuning.VIEW_WIDTH, Tuning.VIEW_HEIGHT);
    private final PrototypeRenderer renderer = new PrototypeRenderer();

    @Override
    public void render(float delta) {
        pair.update(delta);
        ScreenUtils.clear(0.08f, 0.08f, 0.08f, 1f);
        renderer.render(pair, viewport);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}
