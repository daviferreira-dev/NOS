package com.nos.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nos.data.LevelLoader;
import com.nos.data.Tuning;
import com.nos.render.PrototypeRenderer;
import com.nos.world.MirrorPair;
import com.nos.input.BrushInput;
import com.nos.systems.GazeTimeSystem;

/** Orquestra a simulação e a apresentação do protótipo M1. */
public final class GameScreen extends ScreenAdapter {
    private MirrorPair pair = new LevelLoader().load("levels/m1.json");
    private final FitViewport viewport = new FitViewport(Tuning.VIEW_WIDTH, Tuning.VIEW_HEIGHT);
    private final PrototypeRenderer renderer = new PrototypeRenderer();
    private final BrushInput input = new BrushInput();
    private final GazeTimeSystem gaze = new GazeTimeSystem();

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);
    }

    @Override
    public void render(float delta) {
        if (input.consumeRestart()) pair = new LevelLoader().load("levels/m1.json");
        if (input.consumeToggle()) gaze.toggleMode();
        input.updatePosition(viewport, Gdx.graphics.getHeight());
        gaze.apply(pair, input.getX(), input.getY(), input.isActive(), viewport.getWorldWidth());
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
        hide();
        renderer.dispose();
    }

    @Override
    public void pause() {
        input.deactivate();
    }

    @Override
    public void hide() {
        input.deactivate();
        if (Gdx.input.getInputProcessor() == input) Gdx.input.setInputProcessor(null);
    }
}
