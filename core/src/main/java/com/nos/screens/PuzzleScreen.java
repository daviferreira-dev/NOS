package com.nos.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nos.data.LevelLoader;
import com.nos.data.PuzzleTuning;
import com.nos.input.PuzzleInput;
import com.nos.render.PuzzleRenderer;
import com.nos.systems.AnchorSystem;
import com.nos.world.MirrorPuzzle;

/** Orquestra a sala de teste sem regras de movimento na tela. */
public final class PuzzleScreen extends ScreenAdapter {
    private MirrorPuzzle room=new MirrorPuzzle(new LevelLoader().readRoom("levels/knot-room.json"));
    private final PuzzleInput input=new PuzzleInput();
    private final FitViewport viewport=new FitViewport(960,540);
    private final PuzzleRenderer renderer=new PuzzleRenderer();
    private float accumulator;
    @Override public void show() { Gdx.input.setInputProcessor(input); }
    @Override public void render(float delta) {
        input.projectTouch(viewport,Gdx.graphics.getHeight());
        boolean start=input.takeStart(),pause=input.takePause(),restart=input.takeRestart();
        int anchor=input.takeAnchor();
        if(restart||(start&&room.getState()==MirrorPuzzle.State.WON)) {
            room=new MirrorPuzzle(new LevelLoader().readRoom("levels/knot-room.json"));
            input.clearHeld();accumulator=0;
        } else if(room.getState()==MirrorPuzzle.State.READY&&(start||pause)) {
            room.start();input.clearHeld();
        } else if(pause||(start&&room.getState()==MirrorPuzzle.State.PAUSED)) {
            room.togglePause();input.clearHeld();
        }
        if(anchor>0) room.toggleAnchor(anchor==1?AnchorSystem.Target.SHADOW:AnchorSystem.Target.REFLECTION);
        if(room.getState()==MirrorPuzzle.State.PLAYING) {
            accumulator+=Math.min(.1f,delta);
            while(accumulator>=PuzzleTuning.FIXED_STEP) {
                room.update(input.axisX(),input.axisY(),PuzzleTuning.FIXED_STEP);
                accumulator-=PuzzleTuning.FIXED_STEP;
            }
        } else accumulator=0;
        ScreenUtils.clear(.23f,.22f,.36f,1);
        renderer.render(room,viewport,Math.min(.1f,delta));
    }
    /** Posição intermediária para inspeção gráfica sem janela visível. */
    public void prepareSmokeScene() {
        room.start();
        room.getShadow().setPosition(330,350);room.getReflection().setPosition(710,350);
        room.toggleAnchor(AnchorSystem.Target.REFLECTION);room.update(0,0,PuzzleTuning.FIXED_STEP);
    }
    @Override public void resize(int w,int h) { viewport.update(w,h,true); }
    @Override public void pause() {
        input.clearHeld();if(room.getState()==MirrorPuzzle.State.PLAYING) room.togglePause();
    }
    @Override public void hide() {
        input.clearHeld();if(Gdx.input.getInputProcessor()==input) Gdx.input.setInputProcessor(null);
    }
    @Override public void dispose() { hide();renderer.dispose(); }
}
