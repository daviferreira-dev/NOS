package com.nos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nos.NosGame;
import com.nos.input.OpeningInput;
import com.nos.render.IntroRenderer;
import com.nos.world.OpeningSequence;

/** Coordena filme, menu e início explícito; não inicia a travessia ao terminar o filme. */
public final class IntroScreen extends ScreenAdapter {
    private final NosGame game;
    private final OpeningSequence sequence=new OpeningSequence();
    private final OpeningInput input=new OpeningInput();
    private final IntroRenderer renderer=new IntroRenderer();
    private final FitViewport viewport=new FitViewport(960,540);
    private boolean resumeCheckpoint;
    private int previewProgress=-1;
    public IntroScreen(NosGame game) { this.game=game; }
    public void showTitle() {
        sequence.skip();input.setTitle(true);
        input.seedHeldKeys(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.ENTER),
            Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.R),Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.N));
    }
    @Override public void show() { Gdx.input.setInputProcessor(input);game.soundtrack().opening(); }
    @Override public void render(float delta) {
        int progress=menuProgress();input.setProgressAvailable(progress>0);
        OpeningInput.Action action=input.take(viewport);
        if(action==OpeningInput.Action.SKIP) sequence.skip();
        else if(action==OpeningInput.Action.START||action==OpeningInput.Action.FROM_BEGINNING) {
            resumeCheckpoint=action==OpeningInput.Action.START&&progress>0;
            sequence.start();game.soundtrack().fadeOut();
        }
        else if(action==OpeningInput.Action.REPLAY) { resumeCheckpoint=false;sequence.replay();game.soundtrack().replay(); }
        else if(action==OpeningInput.Action.MUSIC) game.soundtrack().toggleMute();
        sequence.update(Math.min(delta,.05f));input.setTitle(sequence.getState()==OpeningSequence.State.TITLE);
        if(sequence.getState()==OpeningSequence.State.READY_TO_PLAY) { game.beginJourney(resumeCheckpoint);return; }
        ScreenUtils.clear(.055f,.065f,.10f,1);renderer.render(sequence,game.soundtrack(),viewport,progress);
    }
    private int menuProgress() { return previewProgress>=0?previewProgress:game.progress().journeyProgress(); }
    public void prepareSmokeScene() { prepareSmokeScene("reflection"); }
    public void prepareSmokeScene(String shot) {
        previewProgress=shot.equals("continue")?1:shot.equals("complete")?2:-1;
        resumeCheckpoint=false;
        if(shot.equals("natural")) { for(int i=0;i<1200;i++) sequence.update(.05f); }
        else if(shot.equals("title")||shot.equals("start")||shot.equals("continue")||shot.equals("complete")) {
            sequence.skip();for(int i=0;i<20;i++) sequence.update(.05f);
            if(shot.equals("start")) { sequence.start();for(int i=0;i<30;i++) sequence.update(.05f); }
        }
        else { float target=shot.equals("wide")?3:shot.equals("return")?18:10;while(sequence.getTime()<target) sequence.update(.05f); }
        input.setTitle(sequence.getState()==OpeningSequence.State.TITLE);input.setProgressAvailable(menuProgress()>0);
    }
    @Override public void resize(int width,int height) { viewport.update(width,height,true); }
    @Override public void pause() { sequence.setPaused(true);input.clear(); }
    @Override public void resume() { sequence.setPaused(false); }
    @Override public void hide() { if(Gdx.input.getInputProcessor()==input) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { hide();renderer.dispose(); }
}
