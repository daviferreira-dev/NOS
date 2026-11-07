package com.nos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nos.data.*;
import com.nos.input.BridgeInput;
import com.nos.render.BridgeRenderer;
import com.nos.world.BridgeEncounter;

/** Apenas coordena entrada, simulação e apresentação do encontro. */
public final class BridgeScreen extends ScreenAdapter {
    private BridgeEncounter encounter=new BridgeEncounter(new LevelLoader().readBridge("levels/bridge-encounter.json"));
    private final BridgeInput input=new BridgeInput();
    private final BridgeRenderer renderer=new BridgeRenderer();
    private final FitViewport viewport=new FitViewport(960,540);
    private float accumulator;
    @Override public void show() { Gdx.input.setInputProcessor(input); }
    @Override public void render(float delta) {
        input.project(viewport,Gdx.graphics.getHeight());
        boolean start=input.start(),pause=input.pause(),restart=input.restart();
        int action=input.action();
        if(restart||(start&&encounter.getState()==BridgeEncounter.State.WON)) {
            encounter=new BridgeEncounter(new LevelLoader().readBridge("levels/bridge-encounter.json"));input.clear();accumulator=0;
        } else if(encounter.getState()==BridgeEncounter.State.READY&&(start||pause)) {
            encounter.start();input.clear();
        } else if(pause||(start&&encounter.getState()==BridgeEncounter.State.PAUSED)) {
            encounter.togglePause();input.clear();
        }
        if(action==1) encounter.swap();
        else if(action==2) encounter.interact();
        if(encounter.getState()==BridgeEncounter.State.PLAYING) {
            accumulator+=Math.min(.1f,delta);
            while(accumulator>=BridgeTuning.FIXED_STEP) {
                encounter.update(input.x(),input.y(),BridgeTuning.FIXED_STEP);
                accumulator-=BridgeTuning.FIXED_STEP;
            }
        } else accumulator=0;
        ScreenUtils.clear(.23f,.22f,.36f,1);renderer.render(encounter,viewport,Math.min(.1f,delta));
    }
    public void prepareSmokeScene() { prepareSmokeScene(false); }
    public void prepareSmokeScene(boolean atMechanism) {
        encounter.start();
        for(int i=0;i<300;i++) encounter.update(1,0,BridgeTuning.FIXED_STEP);
        if(atMechanism) {
            for(int i=0;i<80;i++) encounter.update(0,0,BridgeTuning.FIXED_STEP);
            renderer.snapTo(encounter);return;
        }
        encounter.interact();
        for(int i=0;i<100;i++) encounter.update(0,0,BridgeTuning.FIXED_STEP);
        for(int i=0;i<240;i++) encounter.update(1,0,BridgeTuning.FIXED_STEP);
        renderer.snapTo(encounter);
    }
    @Override public void resize(int w,int h) { viewport.update(w,h,true); }
    @Override public void pause() {
        input.clear();if(encounter.getState()==BridgeEncounter.State.PLAYING) encounter.togglePause();
    }
    @Override public void hide() { input.clear();if(Gdx.input.getInputProcessor()==input) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { hide();renderer.dispose(); }
}
