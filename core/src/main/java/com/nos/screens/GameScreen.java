package com.nos.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nos.data.LevelLoader;
import com.nos.data.SaveData;
import com.nos.data.Tuning;
import com.nos.input.BrushInput;
import com.nos.render.PlayRenderer;
import com.nos.systems.GazeTimeSystem;
import com.nos.world.PlaySession;
import com.nos.world.JourneyCamera;

/** Orquestra entrada, passos fixos e apresentação da primeira fase. */
public final class GameScreen extends ScreenAdapter {
    private PlaySession session = new PlaySession(new LevelLoader().read("levels/water-01.json"));
    private final FitViewport viewport = new FitViewport(Tuning.VIEW_WIDTH, Tuning.VIEW_HEIGHT);
    private final PlayRenderer renderer = new PlayRenderer();
    private final BrushInput input = new BrushInput();
    private final GazeTimeSystem gaze = new GazeTimeSystem();
    private final SaveData save = new SaveData();
    private float accumulator;
    private boolean recorded;
    private final JourneyCamera camera = new JourneyCamera();

    public GameScreen() { gaze.toggleMode(); }

    @Override public void show() { Gdx.input.setInputProcessor(input); }

    @Override public void render(float delta) {
        input.updatePosition(viewport, Gdx.graphics.getHeight());
        int difficulty = input.consumeDifficulty();
        if (difficulty > 0) session.chooseDifficulty(difficulty);
        boolean pressed = input.consumePress();
        boolean pause = input.consumePause();
        if (input.consumeRestart() || (pressed && (session.getState() == PlaySession.State.WON || session.getState() == PlaySession.State.LOST))) {
            session = new PlaySession(new LevelLoader().read("levels/water-01.json"));
            accumulator = 0;
            recorded = false;
        } else if (session.getState() == PlaySession.State.READY && (pressed || pause)) {
            session.start();
        } else if (pause || (pressed && session.getState() == PlaySession.State.PAUSED)) {
            session.togglePause();
        }
        if (input.consumeToggle()) gaze.toggleMode();
        if (session.getState() == PlaySession.State.PLAYING) {
            accumulator += Math.min(.1f, delta);
            while (accumulator >= Tuning.FIXED_STEP) {
                gaze.apply(session.getPair(), input.getX(), input.getY(), input.isActive(), viewport.getWorldWidth());
                camera.update(session.getPair());
                if (gaze.getMode() == GazeTimeSystem.Mode.PROXIMITY)
                    gaze.apply(session.getPair(),camera.worldX(input.getX()),camera.worldY(input.getY()),input.isActive(),viewport.getWorldWidth());
                session.update(Tuning.FIXED_STEP);
                accumulator -= Tuning.FIXED_STEP;
            }
        } else accumulator = 0;
        if (session.getState() == PlaySession.State.WON && !recorded) {
            save.record(session.getScore());
            recorded = true;
        }
        ScreenUtils.clear(.12f,.12f,.19f,1);
        camera.update(session.getPair());
        renderer.render(session, viewport, input, gaze, save.bestScore(), camera);
    }

    /** Usado somente pela verificação gráfica invisível do launcher desktop. */
    public void prepareSmokeScene() {
        session.start();
        for (int i = 0; i < 3600; i++) {
            session.getPair().setBalance(.15f+(float)Math.sin(session.getTime()*Math.PI*.5)*.55f);
            session.update(Tuning.FIXED_STEP);
        }
    }
    @Override public void resize(int width, int height) { viewport.update(width,height,true); }
    @Override public void pause() {
        input.deactivate();
        if (session.getState() == PlaySession.State.PLAYING) session.togglePause();
    }
    @Override public void hide() {
        input.deactivate();
        if (Gdx.input.getInputProcessor() == input) Gdx.input.setInputProcessor(null);
    }
    @Override public void dispose() { hide(); renderer.dispose(); }
}
