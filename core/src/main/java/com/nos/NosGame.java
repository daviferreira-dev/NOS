package com.nos;

import com.badlogic.gdx.Game;
import com.nos.screens.GameScreen;
import com.nos.screens.IntroScreen;
import com.nos.screens.PuzzleScreen;
import com.nos.screens.BridgeScreen;
import com.nos.audio.Soundtrack;
import com.nos.data.SaveData;
import com.nos.data.JourneyProgress;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/** Ponto de entrada compartilhado; mantém o ciclo de vida da tela. */
public class NosGame extends Game {
    private final boolean skipIntro;
    private final boolean legacy;
    private final boolean silent;
    private Soundtrack soundtrack;
    private SaveData progress;
    public NosGame() { this(false); }
    public NosGame(boolean skipIntro) { this(skipIntro,false); }
    public NosGame(boolean skipIntro,boolean legacy) { this(skipIntro,legacy,false); }
    public NosGame(boolean skipIntro,boolean legacy,boolean silent) { this.skipIntro=skipIntro;this.legacy=legacy;this.silent=silent; }

    @Override
    public void create() {
        soundtrack=new Soundtrack(silent);
        progress=silent||skipIntro||legacy?new SaveData(null):new SaveData();
        if(legacy) setScreen(new GameScreen());
        else if(skipIntro) setScreen(new BridgeScreen(false,progress,false));
        else {
            IntroScreen intro=new IntroScreen(this);
            if(progress.canResumeJourney()) intro.showTitle();
            setScreen(intro);
        }
        if(skipIntro) soundtrack.journey();
    }

    public Soundtrack soundtrack() { return soundtrack; }
    public SaveData progress() { return progress; }
    public void returnToTitle() {
        com.badlogic.gdx.Screen previous=getScreen();IntroScreen menu=new IntroScreen(this);
        menu.showTitle();setScreen(menu);if(previous!=null) previous.dispose();
    }
    public void beginJourney() { beginJourney(progress.canResumeJourney()); }
    public void beginJourney(boolean resumeCheckpoint) {
        JourneyProgress.Stage stage=JourneyProgress.start(progress,resumeCheckpoint);
        if(stage==JourneyProgress.Stage.WATER_FOUR) { beginWaterFour();return; }
        if(stage==JourneyProgress.Stage.WATER_THREE) { beginWaterThree();return; }
        if(stage==JourneyProgress.Stage.WATER_TWO) { beginWaterTwo();return; }
        boolean resume=stage==JourneyProgress.Stage.GARDEN;
        com.badlogic.gdx.Screen previous=getScreen();
        setScreen(new BridgeScreen(true,progress,resume));
        if(resume) soundtrack.garden();else soundtrack.journey();
        if(previous!=null) previous.dispose();
    }
    public void beginWaterTwo() {
        if(!progress.isSecondJourneyAvailable()) { returnToTitle();return; }
        com.badlogic.gdx.Screen previous=getScreen();
        setScreen(new BridgeScreen(true,progress,false,true));soundtrack.garden();
        if(previous!=null) previous.dispose();
    }
    public void beginWaterThree() {
        if(!progress.isThirdJourneyAvailable()) { returnToTitle();return; }
        com.badlogic.gdx.Screen previous=getScreen();
        setScreen(new BridgeScreen(true,progress,false,3));soundtrack.garden();
        if(previous!=null) previous.dispose();
    }
    public void beginWaterFour() {
        if(!progress.isFourthJourneyAvailable()) { returnToTitle();return; }
        com.badlogic.gdx.Screen previous=getScreen();
        setScreen(new BridgeScreen(true,progress,false,4));soundtrack.garden();
        if(previous!=null) previous.dispose();
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
